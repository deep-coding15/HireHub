package com.hirehub.candidature.config;

import com.hirehub.common.enums.CandidatureStatus;

/**
 * Machine d'états pour les statuts de candidature
 * Définit les transitions autorisées entre les différents statuts
 */
public class CandidatureStateMachine {

    /**
     * Vérifie si une transition de statut est valide
     *
     * Transitions autorisées:
     * EN_COURS → ACCEPTÉE
     * EN_COURS → REJETÉE
     * EN_COURS → EN_ATTENTE
     * EN_ATTENTE → ACCEPTÉE
     * EN_ATTENTE → REJETÉE
     *
     * @param fromStatus statut actuel
     * @param toStatus nouveau statut demandé
     * @return true si la transition est valide, false sinon
     */
    public static boolean isTransitionValid(String fromStatus, String toStatus) {
        if (fromStatus == null || toStatus == null) {
            return false;
        }

        return switch (fromStatus) {
            case "SOUMISE" -> toStatus.equals(CandidatureStatus.EN_COURS.getLabel())
                    || toStatus.equals(CandidatureStatus.REFUSEE.getLabel());

            case "EN_COURS" -> toStatus.equals(CandidatureStatus.ENTRETIEN.getLabel())
                    || toStatus.equals(CandidatureStatus.REFUSEE.getLabel());

            case "ENTRETIEN" -> toStatus.equals(CandidatureStatus.ACCEPTEE.getLabel())
                    || toStatus.equals(CandidatureStatus.REFUSEE.getLabel());

            case "ACCEPTEE" -> false; // Terminal state
            case "REFUSEE" -> false;  // Terminal state

            default -> false;
        };
    }

    /**
     * Obtient une description lisible de la machine d'états
     */
    public static String getStateMachineDescription() {
        return """
            Candidature Status Machine (FSM)
            ================================
            
            States:
            - SOUMISE: La candidature est en cours d'examen par le recruteur
            - EN_COURS: En attente de deuxième tour ou complément d'info
            - ACCEPTEE: Candidature acceptée par le recruteur (état terminal)
            - REFUSEE: Candidature rejetée par le recruteur (état terminal)
            
            Transitions:
            EN_COURS
              ├─> ACCEPTEE (recruteur approuve)
              ├─> REFUSEE (recruteur refuse)
              └─> EN_COURS (en attente)
            
            EN_ATTENTE
              ├─> ACCEPTEE (recruteur approuve après attente)
              └─> REFUSEE (recruteur refuse après attente)
            
            ACCEPTEE et REFUSEE = états terminaux (pas de transition possible)
            """;
    }
}

