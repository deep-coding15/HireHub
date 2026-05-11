package com.hirehub.candidature.exceptions;

public class CandidatureNotFoundException extends RuntimeException{
    public CandidatureNotFoundException(String message) {
        super(message);
    }
    public CandidatureNotFoundException() {
        super("Candidature non trouvée.");
    }
}
