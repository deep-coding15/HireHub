-- Seed : candidat de test
INSERT INTO users (id, email, password_hash, role, full_name, recruiter_approved, verification_status, blocked)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'alice.martin@candidat.fr',
    '$2a$10$LmL1u7ArGWxpxDmp3dapJe3zYxXW32SbUQewDGIu/1LzP5E0iSraO',
    'CANDIDAT',
    'Alice Martin',
    false,
    NULL,
    false
) ON CONFLICT (email) DO NOTHING;

-- Seed : recruteur de test (approuvé)
INSERT INTO users (id, email, password_hash, role, full_name, recruiter_approved, verification_status,
                   company_name, company_siret, company_presentation, blocked)
VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'paul.dupont@techcorp.fr',
    '$2a$10$LmL1u7ArGWxpxDmp3dapJe3zYxXW32SbUQewDGIu/1LzP5E0iSraO',
    'RECRUTEUR',
    'Paul Dupont',
    true,
    'APPROVED',
    'TechCorp SAS',
    '12345678901234',
    'Éditeur de solutions SaaS B2B spécialisé dans la GRH.',
    false
) ON CONFLICT (email) DO NOTHING;

-- ================================================================
-- USER-SERVICE — seeds/V2__test_users.sql
-- Complète V1 (Alice + Paul) pour couvrir tous les états métier
-- ================================================================

-- ────────────────────────────────────────────────────────────────
-- RECRUTEUR EN ATTENTE D'APPROBATION
-- → teste : GET /admin/recruiters/pending, event RabbitMQ
--           RECRUITER_REGISTRATION_PENDING, mail de confirmation
-- ────────────────────────────────────────────────────────────────
INSERT INTO users (
    id, email, password_hash, role, full_name,
    recruiter_approved, verification_status,
    company_name, company_siret, company_presentation,
    blocked
) VALUES (
             'cccccccc-cccc-cccc-cccc-cccccccccccc',
             'marc.lefebvre@startuprhx.fr',
             '$2a$10$LmL1u7ArGWxpxDmp3dapJe3zYxXW32SbUQewDGIu/1LzP5E0iSraO',
             'RECRUTEUR',
             'Marc Lefebvre',
             false,                    -- pas encore approuvé
             'PENDING_AUTO_CHECK',
             'StartupRHX',
             '98765432109876',
             'Startup spécialisée en recrutement tech et matching IA.',
             false
         ) ON CONFLICT (email) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- RECRUTEUR REJETÉ PAR L'ADMIN
-- → teste : tentative de login → 403 FORBIDDEN
--           message d'erreur métier lisible
--           impossibilité de publier des offres
-- ────────────────────────────────────────────────────────────────
INSERT INTO users (
    id, email, password_hash, role, full_name,
    recruiter_approved, verification_status,
    company_name, company_siret, company_presentation,
    blocked
) VALUES (
             'dddddddd-dddd-dddd-dddd-dddddddddddd',
             'sophie.renard@douteux.fr',
             '$2a$10$LmL1u7ArGWxpxDmp3dapJe3zYxXW32SbUQewDGIu/1LzP5E0iSraO',
             'RECRUTEUR',
             'Sophie Renard',
             false,
             'REJECTED',
             'Cabinet Douteux SARL',
             '11111111111111',
             'Société dont le SIRET na pas pu être vérifié.',
             false
         ) ON CONFLICT (email) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- CANDIDAT BLOQUÉ (banned)
-- → teste : login → 403, JWT refusé par l'API Gateway
--           accès aux offres refusé
--           event USER_BLOCKED dans audit-service
-- ────────────────────────────────────────────────────────────────
INSERT INTO users (
    id, email, password_hash, role, full_name,
    recruiter_approved, verification_status,
    blocked
) VALUES (
             'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
             'jean.moreau@spam.fr',
             '$2a$10$LmL1u7ArGWxpxDmp3dapJe3zYxXW32SbUQewDGIu/1LzP5E0iSraO',
             'CANDIDAT',
             'Jean Moreau',
             false,
             NULL,
             true                      -- bloqué
         ) ON CONFLICT (email) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- ADMIN SYSTÈME
-- → teste : approbation / rejet recruteur, blocage candidat
--           endpoints /admin/**, rôle ADMIN dans le JWT
-- ────────────────────────────────────────────────────────────────
INSERT INTO users (
    id, email, password_hash, role, full_name,
    recruiter_approved, verification_status,
    blocked
) VALUES (
             'ffffffff-ffff-ffff-ffff-ffffffffffff',
             'admin@hirehub.io',
             '$2a$10$LmL1u7ArGWxpxDmp3dapJe3zYxXW32SbUQewDGIu/1LzP5E0iSraO',
             'ADMIN',
             'HireHub Admin',
             false,
             NULL,
             false
         ) ON CONFLICT (email) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- Repositionne la séquence au-delà du dernier id réservé
-- ────────────────────────────────────────────────────────────────