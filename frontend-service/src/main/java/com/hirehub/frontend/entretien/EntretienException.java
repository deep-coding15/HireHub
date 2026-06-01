package com.hirehub.frontend.entretien;

public class EntretienException extends RuntimeException {
    public EntretienException(String message) { super(message); }
    public EntretienException(String message, Throwable cause) { super(message, cause); }
}
