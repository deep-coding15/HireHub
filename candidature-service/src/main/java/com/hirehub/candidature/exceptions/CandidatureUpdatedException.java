package com.hirehub.candidature.exceptions;

public class CandidatureUpdatedException extends RuntimeException {
    public CandidatureUpdatedException(String message) {
        super(message);
    }
    public CandidatureUpdatedException() {
        super("Il y'a un problème avec la mise à jour de votre candidature.");
    }
}
