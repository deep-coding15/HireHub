-- Seed : candidat de test
INSERT INTO users (id, email, password_hash, role, full_name, recruiter_approved, verification_status, blocked)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'alice.martin@candidat.fr',
    '$2a$10$jmTQ2gMkNoU6UkDabZIfpurlHOGWdrRCB.p96lNq0X1P66j0u//O2',
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
    '$2a$10$jmTQ2gMkNoU6UkDabZIfpurlHOGWdrRCB.p96lNq0X1P66j0u//O2',
    'RECRUTEUR',
    'Paul Dupont',
    true,
    'APPROVED',
    'TechCorp SAS',
    '12345678901234',
    'Éditeur de solutions SaaS B2B spécialisé dans la GRH.',
    false
) ON CONFLICT (email) DO NOTHING;
