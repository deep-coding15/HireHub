package com.hirehub.candidature.entities;

import com.hirehub.common.enums.CandidatureStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entité Candidature : suppression logique (soft delete).
 *
 * Les enregistrements ne sont jamais physiquement supprimés :
 * - {@code deletedAt} non-null indique que la candidature est retirée.
 * - {@code deletedBy} trace l'identifiant de l'auteur du retrait (audit RGPD).
 *
 * {@link SQLRestriction} ajoute automatiquement {@code WHERE deleted_at IS NULL}
 * à toutes les requêtes Hibernate générées : aucune modification des repositories
 * n'est nécessaire pour filtrer les enregistrements supprimés.
 *
 * L'index unique sur {@code (candidat_id, offre_id)} couvre toutes les lignes,
 * y compris les supprimées : un candidat ne peut pas re-postuler à la même offre
 * après avoir retiré sa candidature (comportement ATS + conformité RGPD).
 */
@Entity
@Table(name = "candidatures", indexes = {
        @Index(name = "idx_candidat_id",    columnList = "candidat_id"),
        @Index(name = "idx_offre_id",       columnList = "offre_id"),
        @Index(name = "idx_candidat_offre", columnList = "candidat_id, offre_id", unique = true)
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "candidat_id", nullable = false)
    private String candidatId;

    @Column(name = "candidat_email")
    private String candidatEmail;

    @Column(name = "offre_id", nullable = false)
    private String offreId;

    @Column(name = "cv_path")
    private String cvPath;

    @Column(name = "lettre_motivation_path")
    private String lettreMotivationPath;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CandidatureStatus status;

    @Column(name = "date_soumission", nullable = false, updatable = false)
    private LocalDateTime dateSoumission;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    // ── Soft delete ───────────────────────────────────────────────────────────

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** ID de l'utilisateur ayant déclenché le retrait (UUID sous forme de String). */
    @Column(name = "deleted_by")
    private String deletedBy;

    // ── Hooks JPA ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        dateSoumission  = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

}
