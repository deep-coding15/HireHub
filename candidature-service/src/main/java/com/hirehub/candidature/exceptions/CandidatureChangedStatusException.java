package com.hirehub.candidature.exceptions;

public class CandidatureChangedStatusException extends RuntimeException {

    public CandidatureChangedStatusException(String message) {
        super(message);
    }

    public CandidatureChangedStatusException() {
        super("Vous ne pouvez pas modifier le statut de cette candidature");
    }

}
