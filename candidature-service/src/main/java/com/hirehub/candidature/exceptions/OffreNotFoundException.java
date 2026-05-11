package com.hirehub.candidature.exceptions;

/**
 * Exception levée quand une offre n'est pas trouvée ou n'est pas publiée
 */
public class OffreNotFoundException extends RuntimeException {

    public OffreNotFoundException(String message) {
        super(message);
    }

    public OffreNotFoundException(String offerId, Throwable cause) {
        super("Offre non trouvée: " + offerId, cause);
    }
}

