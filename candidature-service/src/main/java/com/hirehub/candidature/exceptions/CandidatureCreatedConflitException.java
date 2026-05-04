package com.hirehub.candidature.exceptions;

public class CandidatureCreatedConflitException extends RuntimeException {
    public CandidatureCreatedConflitException(String message) {
        super(message);
    }
    public CandidatureCreatedConflitException() {
        super("Vous avez déjà postulé à cette offre");
    }
}
