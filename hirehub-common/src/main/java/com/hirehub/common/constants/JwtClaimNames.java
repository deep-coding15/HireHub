package com.hirehub.common.constants;

/**
 * Noms des claims métier alignés sur {@code docs/API-CONTRACTS.md} (JWT access token).
 */
public final class JwtClaimNames {

    private JwtClaimNames() {}

    /** Claim standard : identifiant utilisateur (UUID string). */
    public static final String SUB = "sub";

    public static final String EMAIL = "email";

    /** Valeur enum {@link com.hirehub.common.enums.UserRole#name()}. */
    public static final String ROLE = "role";

    /** Booléen : pertinent pour le rôle RECRUTEUR. */
    public static final String RECRUTEUR_APPROUVE = "recruteurApprouve";
}
