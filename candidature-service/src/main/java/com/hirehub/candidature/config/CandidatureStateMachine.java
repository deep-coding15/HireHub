package com.hirehub.candidature.config;

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
            case "EN_COURS" -> toStatus.equals("ACCEPTÉE")
                            || toStatus.equals("REJETÉE")
                            || toStatus.equals("EN_ATTENTE");

            case "EN_ATTENTE" -> toStatus.equals("ACCEPTÉE")
                              || toStatus.equals("REJETÉE");

            case "ACCEPTÉE" -> false; // Terminal state
            case "REJETÉE" -> false;  // Terminal state

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
            - EN_COURS: La candidature est en cours d'examen par le recruteur
            - EN_ATTENTE: En attente de deuxième tour ou complément d'info
            - ACCEPTÉE: Candidature acceptée par le recruteur (état terminal)
            - REJETÉE: Candidature rejetée par le recruteur (état terminal)
            
            Transitions:
            EN_COURS
              ├─> ACCEPTÉE (recruteur approuve)
              ├─> REJETÉE (recruteur refuse)
              └─> EN_ATTENTE (en attente)
            
            EN_ATTENTE
              ├─> ACCEPTÉE (recruteur approuve après attente)
              └─> REJETÉE (recruteur refuse après attente)
            
            ACCEPTÉE et REJETÉE = états terminaux (pas de transition possible)
            """;
    }
}

