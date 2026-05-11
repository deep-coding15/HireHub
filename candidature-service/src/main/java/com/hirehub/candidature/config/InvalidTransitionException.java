package com.hirehub.candidature.config;

/**
 * Exception levée quand une transition de statut est invalide
 */
public class InvalidTransitionException extends RuntimeException {

    public InvalidTransitionException(String fromStatus, String toStatus) {
        super("Transition invalide: " + fromStatus + " -> " + toStatus);
    }

    public InvalidTransitionException(String message) {
        super(message);
    }
}

