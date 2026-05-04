package com.hirehub.candidature.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer les endpoints qui nécessitent une authentification JWT.
 * Le contrôleur ou la méthode doit être décorée avec cette annotation
 * pour vérifier que l'utilisateur est authentifié.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {
    /**
     * Rôles autorisés (optionnel).
     * Si vide, tous les utilisateurs authentifiés sont acceptés.
     */
    String[] roles() default {};
}

