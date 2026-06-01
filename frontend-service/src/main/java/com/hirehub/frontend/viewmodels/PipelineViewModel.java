package com.hirehub.frontend.viewmodels;

import com.hirehub.frontend.clients.CandidatureDTO;
import com.hirehub.frontend.offre.OffreView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineViewModel {
    private String candidatureId;
    private String candidatId;
    private String candidatEmail;
    private String offreId;
    private String status;
    private LocalDateTime dateSoumission;
    private LocalDateTime dateModification;
    private String cvPath;
    private String lettreMotivationPath;
    private String statusLabel;
    private String statusBadgeClass;

    // Données enrichies depuis offre-service
    private String offreTitre;
    private String offreVille;
    private String offreTypeContrat;
    private Double offreSalaire;

    public void enrichWithOffre(OffreView offre) {
        if (offre == null) return;
        this.offreTitre = offre.getTitre();
        this.offreVille = offre.getVille();
        this.offreTypeContrat = offre.getTypeContrat();
        this.offreSalaire = offre.getSalaire();
    }

    public String getDisplayTitre() {
        return offreTitre != null ? offreTitre : "Offre #" + offreId;
    }

    public String getDisplayCandidat() {
        return candidatEmail != null && !candidatEmail.isBlank() ? candidatEmail : candidatId;
    }

    public static PipelineViewModel fromDTO(CandidatureDTO dto) {
        PipelineViewModel vm = new PipelineViewModel();
        vm.setCandidatureId(dto.getId());
        vm.setCandidatId(dto.getCandidatId());
        vm.setCandidatEmail(dto.getCandidatEmail());
        vm.setOffreId(dto.getOffreId());
        vm.setStatus(dto.getStatus());
        vm.setDateSoumission(dto.getDateSoumission());
        vm.setDateModification(dto.getDateModification());
        vm.setCvPath(dto.getCvPath());
        vm.setLettreMotivationPath(dto.getLettreMotivationPath());

        if (dto.getStatus() != null) {
            switch (dto.getStatus()) {
                case "SOUMISE":
                    vm.setStatusLabel("Soumise");
                    vm.setStatusBadgeClass("badge bg-info");
                    break;
                case "EN_COURS":
                    vm.setStatusLabel("En cours");
                    vm.setStatusBadgeClass("badge bg-warning text-dark");
                    break;
                case "ENTRETIEN":
                    vm.setStatusLabel("Entretien");
                    vm.setStatusBadgeClass("badge bg-primary");
                    break;
                case "ACCEPTEE":
                    vm.setStatusLabel("Acceptée");
                    vm.setStatusBadgeClass("badge bg-success");
                    break;
                case "REFUSEE":
                    vm.setStatusLabel("Refusée");
                    vm.setStatusBadgeClass("badge bg-danger");
                    break;
                default:
                    vm.setStatusLabel(dto.getStatus());
                    vm.setStatusBadgeClass("badge bg-secondary");
            }
        }
        return vm;
    }

    public String getDateSoumissionFormatted() {
        if (dateSoumission == null) return "-";
        return dateSoumission.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getDateModificationFormatted() {
        if (dateModification == null) return "-";
        return dateModification.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public boolean hasCv() {
        return cvPath != null && !cvPath.isEmpty();
    }

    public boolean hasLettre() {
        return lettreMotivationPath != null && !lettreMotivationPath.isEmpty();
    }

    /** Transitions autorisées depuis le statut courant — miroir de CandidatureStateMachine. */
    public List<String[]> getAllowedTransitions() {
        if (status == null) return List.of();
        return switch (status) {
            case "SOUMISE"   -> List.of(
                    new String[]{"EN_COURS",  "Mettre en cours"},
                    new String[]{"REFUSEE",   "Refuser"});
            case "EN_COURS"  -> List.of(
                    new String[]{"ENTRETIEN", "Planifier entretien"},
                    new String[]{"REFUSEE",   "Refuser"});
            case "ENTRETIEN" -> List.of(
                    new String[]{"ACCEPTEE",  "Accepter"},
                    new String[]{"REFUSEE",   "Refuser"});
            default          -> List.of(); // ACCEPTEE / REFUSEE : états terminaux
        };
    }

    public boolean isTerminal() {
        return "ACCEPTEE".equals(status) || "REFUSEE".equals(status);
    }
}
