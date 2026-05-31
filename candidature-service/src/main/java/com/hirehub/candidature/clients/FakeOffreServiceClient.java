package com.hirehub.candidature.clients;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Faux client local pour simuler offre-service pendant le développement.
 * Actif uniquement avec le profil `sandbox`.
 */
@Component
@Profile("sandbox")
public class FakeOffreServiceClient implements IOffreServiceClient {

    private final Map<String, OffreDTO> offers = new HashMap<>();

    public FakeOffreServiceClient() {
        offers.put("1001", new OffreDTO(
                "1001",
                "Développeur Java Spring Boot",
                "Créer et maintenir des microservices HireHub",
                "recruteur-1",
                "PUBLIEE"));

        offers.put("1002", new OffreDTO(
                "1002",
                "Développeur Frontend Angular",
                "Intégration frontend avec les API microservices",
                "recruteur-2",
                "PUBLIEE"));

        offers.put("1003", new OffreDTO(
                "1003",
                "Offre brouillon non publiée",
                "Cette offre sert à tester le cas non publié",
                "recruteur-1",
                "BROUILLON"));
    }

    @Override
    public OffreDTO getOffre(String id) {
        return offers.get(id);
    }

    @Override
    public boolean offreExists(String id) {
        OffreDTO offre = offers.get(id);
        return offre != null && offre.isPublished();
    }

    @Override
    public boolean isRecruteurOwner(String offreId, String recruteurId) {
        OffreDTO offre = offers.get(offreId);
        return offre != null && offre.isPublished() && recruteurId != null && recruteurId.equals(offre.getRecruteurId());
    }
}
