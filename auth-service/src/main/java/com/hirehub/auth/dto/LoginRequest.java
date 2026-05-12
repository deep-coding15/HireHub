package com.hirehub.auth.dto;

/**
 * Corps JSON {@code POST /api/v1/auth/login}.
 */
public record LoginRequest(String email, String password) {
}
