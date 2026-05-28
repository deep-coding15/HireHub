package com.hirehub.entretien.services;

import com.hirehub.common.dtos.ApiResponse;
import com.hirehub.common.enums.CandidatureStatus;
import com.hirehub.common.enums.InterviewStatus;
import com.hirehub.entretien.clients.CandidatureClient;
import com.hirehub.entretien.clients.CandidatureSnapshot;
import com.hirehub.entretien.dtos.CreateEntretienRequest;
import com.hirehub.entretien.entities.Entretien;
import com.hirehub.entretien.entities.EntretienType;
import com.hirehub.entretien.repository.EntretienRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EntretienServiceImpl implements EntretienService {

    private static final int SLOT_MINUTES = 60;

    private final EntretienRepository entretienRepository;
    private final CandidatureClient candidatureClient;
    private final EntretienNotificationPublisher notificationPublisher;
    private final Clock clock;

    public EntretienServiceImpl(
            EntretienRepository entretienRepository,
            CandidatureClient candidatureClient,
            EntretienNotificationPublisher notificationPublisher,
            Clock clock) {
        this.entretienRepository   = entretienRepository;
        this.candidatureClient     = candidatureClient;
        this.notificationPublisher = notificationPublisher;
        this.clock                 = clock;
    }

    @Override
    public Entretien create(CreateEntretienRequest request) {
        log.info("[ENTRETIEN] Creation: candidatureId={}, recruteurId={}, dateHeure={}",
                request.getCandidatureId(), request.getRecruteurId(), request.getDateHeure());

        validateRequest(request);
        CandidatureSnapshot candidature = loadCandidature(request.getCandidatureId());

        if (entretienRepository.existsByCandidatureIdAndStatus(
                request.getCandidatureId(), InterviewStatus.PLANIFIE)) {
            log.warn("[ENTRETIEN] Conflit: entretien deja planifie pour candidatureId={}", request.getCandidatureId());
            throw new IllegalArgumentException("Un entretien est deja planifie pour cette candidature");
        }

        LocalDateTime start = request.getDateHeure().minusMinutes(SLOT_MINUTES - 1L);
        LocalDateTime end   = request.getDateHeure().plusMinutes(SLOT_MINUTES - 1L);

        if (entretienRepository.existsByRecruteurIdAndStatusAndDateHeureBetween(
                request.getRecruteurId(), InterviewStatus.PLANIFIE, start, end)) {
            log.warn("[ENTRETIEN] Conflit de creneau pour recruteurId={}", request.getRecruteurId());
            throw new IllegalArgumentException("Le recruteur a deja un entretien sur ce creneau");
        }
        if (entretienRepository.existsByCandidatIdAndStatusAndDateHeureBetween(
                candidature.getCandidatId(), InterviewStatus.PLANIFIE, start, end)) {
            log.warn("[ENTRETIEN] Conflit de creneau pour candidatId={}", candidature.getCandidatId());
            throw new IllegalArgumentException("Le candidat a deja un entretien sur ce creneau");
        }

        Entretien entretien = new Entretien();
        entretien.setCandidatureId(request.getCandidatureId());
        entretien.setCandidatId(candidature.getCandidatId());
        entretien.setRecruteurId(request.getRecruteurId());
        entretien.setDateHeure(request.getDateHeure());
        entretien.setLieu(request.getLieu());
        entretien.setLienVisio(request.getLienVisio());
        entretien.setType(request.getType());
        entretien.setNotesInternes(request.getNotesInternes());
        entretien.setStatus(InterviewStatus.PLANIFIE);

        Entretien saved = entretienRepository.save(entretien);
        candidatureClient.updateStatus(saved.getCandidatureId(), CandidatureStatus.ENTRETIEN.name());
        notificationPublisher.publish(saved, false);
        log.info("[ENTRETIEN] Cree avec succes: entretienId={}, candidatId={}", saved.getId(), saved.getCandidatId());
        return saved;
    }

    @Override
    public List<Entretien> listByCandidature(String candidatureId) {
        if (!StringUtils.hasText(candidatureId))
            throw new IllegalArgumentException("candidatureId est obligatoire");
        return entretienRepository.findByCandidatureIdOrderByDateHeureDesc(candidatureId);
    }

    @Override
    public List<Entretien> listByRecruteur(String recruteurId) {
        if (!StringUtils.hasText(recruteurId))
            throw new IllegalArgumentException("recruteurId est obligatoire");
        return entretienRepository.findByRecruteurIdOrderByDateHeureAsc(recruteurId);
    }

    @Override
    public List<Entretien> listByCandidat(String candidatId) {
        if (!StringUtils.hasText(candidatId))
            throw new IllegalArgumentException("candidatId est obligatoire");
        return entretienRepository.findByCandidatIdOrderByDateHeureAsc(candidatId);
    }

    @Override
    public Entretien cancel(String entretienId, String recruteurId) {
        log.info("[ENTRETIEN] Annulation: entretienId={}, recruteurId={}", entretienId, recruteurId);

        if (!StringUtils.hasText(recruteurId))
            throw new IllegalArgumentException("recruteurId est obligatoire");

        Entretien entretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> {
                    log.warn("[ENTRETIEN] Entretien non trouve: id={}", entretienId);
                    return new IllegalArgumentException("Entretien non trouve");
                });

        if (!recruteurId.equals(entretien.getRecruteurId()))
            throw new SecurityException("Le recruteur ne peut pas annuler cet entretien");

        if (entretien.getStatus() == InterviewStatus.ANNULE) {
            log.warn("[ENTRETIEN] Deja annule: entretienId={}", entretienId);
            return entretien;
        }

        entretien.setStatus(InterviewStatus.ANNULE);
        entretien.setDateAnnulation(LocalDateTime.now(clock));
        Entretien saved = entretienRepository.save(entretien);
        notificationPublisher.publish(saved, true);
        log.info("[ENTRETIEN] Annule avec succes: entretienId={}", saved.getId());
        return saved;
    }

    private void validateRequest(CreateEntretienRequest request) {
        if (request == null)
            throw new IllegalArgumentException("La requete est obligatoire");
        if (!StringUtils.hasText(request.getCandidatureId()))
            throw new IllegalArgumentException("candidatureId est obligatoire");
        if (!StringUtils.hasText(request.getRecruteurId()))
            throw new IllegalArgumentException("recruteurId est obligatoire");
        if (request.getDateHeure() == null || !request.getDateHeure().isAfter(LocalDateTime.now(clock)))
            throw new IllegalArgumentException("La date de l'entretien doit etre dans le futur");
        if (request.getType() == null)
            throw new IllegalArgumentException("type est obligatoire");
        if (request.getType() == EntretienType.PRESENTIEL && !StringUtils.hasText(request.getLieu()))
            throw new IllegalArgumentException("Le lieu est obligatoire pour un entretien presentiel");
        if (request.getType() == EntretienType.VISIO && !StringUtils.hasText(request.getLienVisio()))
            throw new IllegalArgumentException("Le lien visio est obligatoire pour un entretien visio");
    }

    private CandidatureSnapshot loadCandidature(String candidatureId) {
        ApiResponse<CandidatureSnapshot> response;
        try {
            response = candidatureClient.getCandidatureById(candidatureId);
        } catch (FeignException.NotFound e) {
            throw new IllegalArgumentException("Candidature inexistante");
        }
        if (response == null || !response.isSuccess() || response.getData() == null)
            throw new IllegalArgumentException("Candidature inexistante");
        return response.getData();
    }
}