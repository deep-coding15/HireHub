-- =============================================================
--  HireHub – Script de seed PostgreSQL
--  Base : hirehub | Généré pour environnement de développement
-- =============================================================

\connect hirehub

SET client_encoding    = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET row_security = off;

-- ⚠️  Nettoyage préalable (ordre respectant les FK)
TRUNCATE public.historique_statut   CASCADE;
TRUNCATE public.candidatures        CASCADE;
TRUNCATE public.email_events_processed CASCADE;
TRUNCATE public.event_audit_log     CASCADE;
TRUNCATE public.password_reset_tokens CASCADE;
TRUNCATE public.signup_email_challenges CASCADE;
TRUNCATE public.users               CASCADE;


-- =============================================================
--  1. USERS
--     Roles : ADMIN · RECRUTEUR · CANDIDAT
--     password_hash = BCrypt de "Password123!"
-- =============================================================

INSERT INTO public.users (
    id, full_name, email, password_hash, role,
    blocked, recruiter_approved, verification_status,
    company_name, company_siret, company_presentation
) VALUES

-- ── Admin ────────────────────────────────────────────────────
(
    'a0000000-0000-0000-0000-000000000001',
    'Administrateur HireHub',
    'admin@hirehub.ma',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'ADMIN',
    false, true, 'APPROVED',
    NULL, NULL, NULL
),

-- ── Recruteurs ───────────────────────────────────────────────
(
    'b0000000-0000-0000-0000-000000000001',
    'Karim Benali',
    'k.benali@techcorp.ma',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'RECRUTEUR',
    false, true, 'APPROVED',
    'TechCorp Maroc',
    '12345678901234',
    'Cabinet de recrutement spécialisé dans les profils IT et Digital au Maroc et en Afrique du Nord. Nous accompagnons les startups et les grands groupes dans leurs recrutements techniques depuis 2015.'
),
(
    'b0000000-0000-0000-0000-000000000002',
    'Imane Chaoui',
    'i.chaoui@innova-rh.ma',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'RECRUTEUR',
    false, true, 'APPROVED',
    'InnovaRH',
    '98765432109876',
    'Agence RH innovante proposant des solutions de recrutement agile pour les PME marocaines. Spécialités : développement logiciel, data, et cloud.'
),
(
    'b0000000-0000-0000-0000-000000000003',
    'Youssef Tahir',
    'y.tahir@afritech-solutions.com',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'RECRUTEUR',
    false, false, 'PENDING_AUTO_CHECK',
    'AfriTech Solutions',
    '11223344556677',
    'Société de conseil IT basée à Casablanca, accompagnant la transformation numérique des entreprises africaines.'
),
(
    'b0000000-0000-0000-0000-000000000004',
    'Sara Mrani',
    's.mrani@blockedrecruiter.com',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'RECRUTEUR',
    true, false, 'REJECTED',
    'Suspicious Corp',
    '00000000000000',
    NULL
),

-- ── Candidats ────────────────────────────────────────────────
(
    'c0000000-0000-0000-0000-000000000001',
    'Nalova Essomba',
    'nalova.essomba@gmail.com',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'CANDIDAT',
    false, false, 'APPROVED',
    NULL, NULL, NULL
),
(
    'c0000000-0000-0000-0000-000000000002',
    'Douae Hamdoune',
    'douae.hamdoune@outlook.com',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'CANDIDAT',
    false, false, 'APPROVED',
    NULL, NULL, NULL
),
(
    'c0000000-0000-0000-0000-000000000003',
    'Wissal Idrissi',
    'wissal.idrissi@gmail.com',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'CANDIDAT',
    false, false, 'APPROVED',
    NULL, NULL, NULL
),
(
    'c0000000-0000-0000-0000-000000000004',
    'Lydivine Nkomo',
    'lydivine.nkomo@yahoo.fr',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'CANDIDAT',
    false, false, 'PENDING_AUTO_CHECK',
    NULL, NULL, NULL
),
(
    'c0000000-0000-0000-0000-000000000005',
    'Mehdi Ouazzani',
    'mehdi.ouazzani@gmail.com',
    '$2a$12$Xy8KqP3nLmW5vRtZuA2eOuHs6dFjNbCwI4oVgYeT7kXpQ9sM1rU0W',
    'CANDIDAT',
    false, false, 'APPROVED',
    NULL, NULL, NULL
);


-- =============================================================
--  2. CANDIDATURES
--     offre_id / candidat_id : UUIDs issus de job-service /
--     candidate-service (simulés ici)
-- =============================================================

INSERT INTO public.candidatures (
    id, candidat_id, offre_id, status,
    date_soumission, date_modification,
    cv_path, lettre_motivation_path
) VALUES

-- Nalova → Offre Backend Java (TechCorp)
(
    'cand-0001-0001',
    'c0000000-0000-0000-0000-000000000001',
    'offre-techcorp-java-001',
    'ENTRETIEN',
    '2026-04-15 09:30:00', '2026-04-22 14:00:00',
    'cvs/nalova_essomba_cv.pdf',
    'motivations/nalova_essomba_lm.pdf'
),

-- Nalova → Offre DevOps (InnovaRH)
(
    'cand-0001-0002',
    'c0000000-0000-0000-0000-000000000001',
    'offre-innova-devops-002',
    'SOUMISE',
    '2026-05-01 11:00:00', NULL,
    'cvs/nalova_essomba_cv.pdf',
    NULL
),

-- Douae → Offre Fullstack React/Spring (TechCorp)
(
    'cand-0002-0001',
    'c0000000-0000-0000-0000-000000000002',
    'offre-techcorp-fullstack-003',
    'EN_COURS',
    '2026-04-20 10:15:00', '2026-04-28 09:00:00',
    'cvs/douae_hamdoune_cv.pdf',
    'motivations/douae_hamdoune_lm.pdf'
),

-- Wissal → Offre QA Engineer (InnovaRH)
(
    'cand-0003-0001',
    'c0000000-0000-0000-0000-000000000003',
    'offre-innova-qa-004',
    'ACCEPTEE',
    '2026-03-10 08:45:00', '2026-04-05 16:30:00',
    'cvs/wissal_idrissi_cv.pdf',
    'motivations/wissal_idrissi_lm.pdf'
),

-- Wissal → Offre Backend PHP (AfriTech)
(
    'cand-0003-0002',
    'c0000000-0000-0000-0000-000000000003',
    'offre-afritech-php-005',
    'REFUSEE',
    '2026-03-05 14:00:00', '2026-03-20 10:00:00',
    'cvs/wissal_idrissi_cv.pdf',
    NULL
),

-- Lydivine → Offre Data Engineer (TechCorp)
(
    'cand-0004-0001',
    'c0000000-0000-0000-0000-000000000004',
    'offre-techcorp-data-006',
    'SOUMISE',
    '2026-05-08 16:00:00', NULL,
    'cvs/lydivine_nkomo_cv.pdf',
    'motivations/lydivine_nkomo_lm.pdf'
),

-- Mehdi → Offre Backend Java (TechCorp)
(
    'cand-0005-0001',
    'c0000000-0000-0000-0000-000000000005',
    'offre-techcorp-java-001',
    'EN_COURS',
    '2026-04-18 13:00:00', '2026-04-25 11:00:00',
    'cvs/mehdi_ouazzani_cv.pdf',
    'motivations/mehdi_ouazzani_lm.pdf'
);


-- =============================================================
--  3. HISTORIQUE_STATUT
--     Traçabilité des transitions de chaque candidature
-- =============================================================

INSERT INTO public.historique_statut (
    id, candidature_id,
    ancien_status, nouveau_status,
    date_changement, commentaire, utilisateur_id
) VALUES

-- cand-0001-0001 : SOUMISE → EN_COURS → ENTRETIEN
(
    'hist-0001-0001', 'cand-0001-0001',
    'SOUMISE', 'EN_COURS',
    '2026-04-17 10:00:00',
    'Profil retenu pour pré-sélection.',
    'b0000000-0000-0000-0000-000000000001'
),
(
    'hist-0001-0002', 'cand-0001-0001',
    'EN_COURS', 'ENTRETIEN',
    '2026-04-22 14:00:00',
    'Entretien technique planifié le 28 avril.',
    'b0000000-0000-0000-0000-000000000001'
),

-- cand-0002-0001 : SOUMISE → EN_COURS
(
    'hist-0002-0001', 'cand-0002-0001',
    'SOUMISE', 'EN_COURS',
    '2026-04-28 09:00:00',
    'CV validé par le recruteur.',
    'b0000000-0000-0000-0000-000000000002'
),

-- cand-0003-0001 : SOUMISE → EN_COURS → ENTRETIEN → ACCEPTEE
(
    'hist-0003-0001', 'cand-0003-0001',
    'SOUMISE', 'EN_COURS',
    '2026-03-12 09:00:00',
    'Candidature conforme aux critères.',
    'b0000000-0000-0000-0000-000000000002'
),
(
    'hist-0003-0002', 'cand-0003-0001',
    'EN_COURS', 'ENTRETIEN',
    '2026-03-20 14:30:00',
    'Entretien RH réussi. Passage technique programmé.',
    'b0000000-0000-0000-0000-000000000002'
),
(
    'hist-0003-0003', 'cand-0003-0001',
    'ENTRETIEN', 'ACCEPTEE',
    '2026-04-05 16:30:00',
    'Excellent entretien technique. Offre envoyée.',
    'b0000000-0000-0000-0000-000000000002'
),

-- cand-0003-0002 : SOUMISE → EN_COURS → REFUSEE
(
    'hist-0004-0001', 'cand-0003-0002',
    'SOUMISE', 'EN_COURS',
    '2026-03-08 10:00:00',
    NULL,
    'b0000000-0000-0000-0000-000000000003'
),
(
    'hist-0004-0002', 'cand-0003-0002',
    'EN_COURS', 'REFUSEE',
    '2026-03-20 10:00:00',
    'Profil ne correspond pas aux exigences techniques du poste.',
    'b0000000-0000-0000-0000-000000000003'
),

-- cand-0005-0001 : SOUMISE → EN_COURS
(
    'hist-0005-0001', 'cand-0005-0001',
    'SOUMISE', 'EN_COURS',
    '2026-04-25 11:00:00',
    'Candidature en cours d''examen.',
    'b0000000-0000-0000-0000-000000000001'
);


-- =============================================================
--  4. EMAIL_EVENTS_PROCESSED
--     Événements traités par notification-service via RabbitMQ
-- =============================================================

INSERT INTO public.email_events_processed (
    id, event_id, event_type, recipient_email,
    status, processed_at, error_message, retry_count, created_at
) VALUES

(1,  'evt-uuid-0001', 'CANDIDATURE_SOUMISE',       'nalova.essomba@gmail.com',   'SUCCESS', '2026-04-15 09:31:00', NULL, 0, '2026-04-15 09:31:00'),
(2,  'evt-uuid-0002', 'STATUT_CHANGE',              'nalova.essomba@gmail.com',   'SUCCESS', '2026-04-17 10:01:00', NULL, 0, '2026-04-17 10:01:00'),
(3,  'evt-uuid-0003', 'STATUT_CHANGE',              'nalova.essomba@gmail.com',   'SUCCESS', '2026-04-22 14:01:00', NULL, 0, '2026-04-22 14:01:00'),
(4,  'evt-uuid-0004', 'CANDIDATURE_SOUMISE',        'nalova.essomba@gmail.com',   'SUCCESS', '2026-05-01 11:01:00', NULL, 0, '2026-05-01 11:01:00'),
(5,  'evt-uuid-0005', 'CANDIDATURE_SOUMISE',        'douae.hamdoune@outlook.com', 'SUCCESS', '2026-04-20 10:16:00', NULL, 0, '2026-04-20 10:16:00'),
(6,  'evt-uuid-0006', 'STATUT_CHANGE',              'douae.hamdoune@outlook.com', 'SUCCESS', '2026-04-28 09:01:00', NULL, 0, '2026-04-28 09:01:00'),
(7,  'evt-uuid-0007', 'CANDIDATURE_SOUMISE',        'wissal.idrissi@gmail.com',   'SUCCESS', '2026-03-10 08:46:00', NULL, 0, '2026-03-10 08:46:00'),
(8,  'evt-uuid-0008', 'STATUT_CHANGE',              'wissal.idrissi@gmail.com',   'SUCCESS', '2026-04-05 16:31:00', NULL, 0, '2026-04-05 16:31:00'),
(9,  'evt-uuid-0009', 'RECRUTEUR_INSCRIPTION',      'k.benali@techcorp.ma',       'SUCCESS', '2026-01-10 08:00:00', NULL, 0, '2026-01-10 08:00:00'),
(10, 'evt-uuid-0010', 'RECRUTEUR_APPROUVE',         'k.benali@techcorp.ma',       'SUCCESS', '2026-01-11 09:00:00', NULL, 0, '2026-01-11 09:00:00'),
(11, 'evt-uuid-0011', 'RECRUTEUR_INSCRIPTION',      'y.tahir@afritech-solutions.com', 'SUCCESS', '2026-04-30 10:00:00', NULL, 0, '2026-04-30 10:00:00'),
(12, 'evt-uuid-0012', 'NOTIFICATION_EMAIL_FAILED',  'lydivine.nkomo@yahoo.fr',    'FAILED',  '2026-05-08 16:05:00', 'SMTP timeout après 3 tentatives', 3, '2026-05-08 16:05:00');

-- Réinitialiser la séquence après insertion manuelle
SELECT setval('public.email_events_processed_id_seq', 12, true);


-- =============================================================
--  5. EVENT_AUDIT_LOG
--     Journal des événements inter-services (RabbitMQ)
-- =============================================================

INSERT INTO public.event_audit_log (
    id, event_id, event_type, message,
    source_service, destination_service,
    status, error_message, created_at
) VALUES

(1,  'evt-uuid-0001', 'CANDIDATURE_SOUMISE',   'Candidature soumise par nalova.essomba',       'cv-service',       'notification-service', 'SUCCESS', NULL, '2026-04-15 09:31:00'),
(2,  'evt-uuid-0002', 'STATUT_CHANGE',         'Statut passé à EN_COURS (cand-0001-0001)',      'candidatures-service', 'notification-service', 'SUCCESS', NULL, '2026-04-17 10:01:00'),
(3,  'evt-uuid-0003', 'STATUT_CHANGE',         'Statut passé à ENTRETIEN (cand-0001-0001)',     'candidatures-service', 'notification-service', 'SUCCESS', NULL, '2026-04-22 14:01:00'),
(4,  'evt-uuid-0004', 'CANDIDATURE_SOUMISE',   'Candidature DevOps soumise par nalova.essomba', 'cv-service',      'notification-service', 'SUCCESS', NULL, '2026-05-01 11:01:00'),
(5,  'evt-uuid-0005', 'CANDIDATURE_SOUMISE',   'Candidature soumise par douae.hamdoune',        'cv-service',      'notification-service', 'SUCCESS', NULL, '2026-04-20 10:16:00'),
(6,  'evt-uuid-0006', 'STATUT_CHANGE',         'Statut passé à EN_COURS (cand-0002-0001)',      'candidatures-service', 'notification-service', 'SUCCESS', NULL, '2026-04-28 09:01:00'),
(7,  'evt-uuid-0007', 'CANDIDATURE_SOUMISE',   'Candidature QA soumise par wissal.idrissi',     'cv-service',      'notification-service', 'SUCCESS', NULL, '2026-03-10 08:46:00'),
(8,  'evt-uuid-0008', 'CANDIDATURE_ACCEPTEE',  'Candidature cand-0003-0001 ACCEPTEE',           'candidatures-service', 'notification-service', 'SUCCESS', NULL, '2026-04-05 16:31:00'),
(9,  'evt-uuid-0009', 'RECRUTEUR_INSCRIPTION', 'Nouveau recruteur : k.benali@techcorp.ma',      'auth-service',    'notification-service', 'SUCCESS', NULL, '2026-01-10 08:00:00'),
(10, 'evt-uuid-0010', 'RECRUTEUR_APPROUVE',    'Recruteur k.benali approuvé par admin',         'auth-service',    'notification-service', 'SUCCESS', NULL, '2026-01-11 09:00:00'),
(11, 'evt-uuid-0011', 'RECRUTEUR_INSCRIPTION', 'Nouveau recruteur : y.tahir@afritech-solutions.com', 'auth-service', 'notification-service', 'SUCCESS', NULL, '2026-04-30 10:00:00'),
(12, 'evt-uuid-0012', 'NOTIFICATION_EMAIL',    'Tentative email vers lydivine.nkomo@yahoo.fr',  'notification-service', 'smtp-relay',        'FAILED',  'SMTP timeout', '2026-05-08 16:05:00');

-- Réinitialiser la séquence
SELECT setval('public.event_audit_log_id_seq', 12, true);


-- =============================================================
--  6. SIGNUP_EMAIL_CHALLENGES  (exemples non-consommés)
--     code_hash = SHA-256 de "123456" (fictif)
-- =============================================================

INSERT INTO public.signup_email_challenges (
    id, email, nom_complet, role,
    code_hash, consumed, expires_at
) VALUES
(
    'chall-0000-0000-0000-000000000001',
    'nouveau.candidat@test.com',
    'Nouveau Candidat Test',
    'CANDIDAT',
    'e10adc3949ba59abbe56e057f20f883e',
    false,
    NOW() + INTERVAL '15 minutes'
),
(
    'chall-0000-0000-0000-000000000002',
    'nouveau.recruteur@company.ma',
    'Nouveau Recruteur Test',
    'RECRUTEUR',
    'e10adc3949ba59abbe56e057f20f883e',
    false,
    NOW() + INTERVAL '15 minutes'
);


-- =============================================================
--  Vérification rapide
-- =============================================================

DO $$
DECLARE
    nb_users         INT;
    nb_candidatures  INT;
    nb_historique    INT;
    nb_emails        INT;
    nb_audit         INT;
BEGIN
    SELECT COUNT(*) INTO nb_users        FROM public.users;
    SELECT COUNT(*) INTO nb_candidatures FROM public.candidatures;
    SELECT COUNT(*) INTO nb_historique   FROM public.historique_statut;
    SELECT COUNT(*) INTO nb_emails       FROM public.email_events_processed;
    SELECT COUNT(*) INTO nb_audit        FROM public.event_audit_log;

    RAISE NOTICE '=== HireHub Seed – Résumé ===';
    RAISE NOTICE 'users                  : %', nb_users;
    RAISE NOTICE 'candidatures           : %', nb_candidatures;
    RAISE NOTICE 'historique_statut      : %', nb_historique;
    RAISE NOTICE 'email_events_processed : %', nb_emails;
    RAISE NOTICE 'event_audit_log        : %', nb_audit;
    RAISE NOTICE '==============================';
END $$;
