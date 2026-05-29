package com.hirehub.frontend.candidature;

public class CandidatureServiceException extends RuntimeException {

    public CandidatureServiceException(String message) {
        super(message);
    }

    public CandidatureServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
