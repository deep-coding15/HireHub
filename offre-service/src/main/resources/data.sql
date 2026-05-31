-- Seed : offre publiée par le recruteur de test
INSERT INTO offres (id, titre, description, type_contrat, ville, salaire,
                    date_creation, date_expiration, statut, recruteur_id, recruteur_email)
VALUES (
    1,
    'Développeur Java Spring Boot',
    'Nous recherchons un développeur Java confirmé pour rejoindre notre équipe produit.
    Vous interviendrez sur nos APIs REST, nos microservices Spring Boot et nos pipelines CI/CD.
    Stack : Java 17, Spring Boot 3, PostgreSQL, RabbitMQ, Docker.',
    'CDI',
    'Paris',
    55000,
    NOW(),
    NOW() + INTERVAL '60 days',
    'PUBLIEE',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'paul.dupont@techcorp.fr'
) ON CONFLICT (id) DO NOTHING;

-- ================================================================
-- JOB-SERVICE — seeds/V2__test_offres.sql
-- Complète V1 (offre Java #1) pour couvrir tous les statuts métier
-- ================================================================

-- ────────────────────────────────────────────────────────────────
-- OFFRE EN BROUILLON (non visible par les candidats)
-- → teste : GET /offres/2 par un CANDIDAT → 404 ou 403
--           GET /offres/mes-offres par Paul → visible
--           pas indexée dans la recherche publique
-- ────────────────────────────────────────────────────────────────
INSERT INTO offres (
    id, titre, description, type_contrat, ville, salaire,
    date_creation, date_expiration, statut,
    recruteur_id, recruteur_email
) VALUES (
             2,
             'DevOps Engineer – CI/CD & Kubernetes',
             'TechCorp SAS recrute un ingénieur DevOps pour renforcer son équipe infrastructure.
         Vous serez responsable de la mise en place et de la maintenance des pipelines CI/CD,
         de la supervision des clusters Kubernetes et de l''automatisation des déploiements.
         Stack : GitLab CI, Kubernetes, Helm, Terraform, Prometheus, Grafana.',
             'CDI',
             'Bordeaux',
             58000,
             NOW() - INTERVAL '2 days',
             NOW() + INTERVAL '58 days',
             'BROUILLON',
             'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',  -- Paul Dupont
             'paul.dupont@techcorp.fr'
         ) ON CONFLICT (id) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- OFFRE EXPIRÉE
-- → teste : GET /offres/3 → visible mais badge "Expirée"
--           POST /candidatures sur offre expirée → 409 CONFLICT
--           exclue des résultats de recherche par défaut
-- ────────────────────────────────────────────────────────────────
INSERT INTO offres (
    id, titre, description, type_contrat, ville, salaire,
    date_creation, date_expiration, statut,
    recruteur_id, recruteur_email
) VALUES (
             3,
             'Data Analyst – Power BI & Python',
             'Nous recherchons un Data Analyst pour analyser les données RH et produire
         des tableaux de bord décisionnels. Vous travaillerez en binôme avec les équipes
         produit et finance. Stack : Python, Pandas, Power BI, SQL Server.',
             'CDD',
             'Lyon',
             42000,
             NOW() - INTERVAL '75 days',
             NOW() - INTERVAL '15 days',    -- expirée il y a 15 jours
             'FERMEE',
             'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
             'paul.dupont@techcorp.fr'
         ) ON CONFLICT (id) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- OFFRE ARCHIVÉE (suppression logique)
-- → teste : absente de tous les listings publics et recruteur
--           accessible uniquement via /admin/offres ou audit
--           candidatures liées toujours en base (intégrité)
-- ────────────────────────────────────────────────────────────────
INSERT INTO offres (
    id, titre, description, type_contrat, ville, salaire,
    date_creation, date_expiration, statut,
    recruteur_id, recruteur_email
) VALUES (
             4,
             'UX Designer – Design System & Figma',
             'Poste pourvu. Offre conservée pour archivage et reporting interne.
         L''équipe Design recherchait un profil UX senior pour co-construire
         le design system de la plateforme. Stack : Figma, Storybook, Zeroheight.',
             'CDI',
             'Nantes',
             47000,
             NOW() - INTERVAL '120 days',
             NOW() - INTERVAL '60 days',
             'FERMEE',
             'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
             'paul.dupont@techcorp.fr'
         ) ON CONFLICT (id) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- 2ÈME OFFRE PUBLIÉE (recruteur différent simulé via même compte)
-- → teste : pagination GET /offres?page=0&size=10 → 2 résultats
--           tri par date, filtre par ville, filtre par type_contrat
--           candidature d'Alice sur 2 offres simultanément
-- ────────────────────────────────────────────────────────────────
INSERT INTO offres (
    id, titre, description, type_contrat, ville, salaire,
    date_creation, date_expiration, statut,
    recruteur_id, recruteur_email
) VALUES (
             5,
             'Product Manager – Plateforme SaaS B2B',
             'Vous piloterez la roadmap produit de notre solution GRH en lien direct
         avec les équipes engineering et les clients grands comptes. Expérience en
         product management SaaS B2B exigée. Outils : Jira, Confluence, Amplitude.',
             'CDI',
             'Paris',
             62000,
             NOW() - INTERVAL '5 days',
             NOW() + INTERVAL '55 days',
             'PUBLIEE',
             'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
             'paul.dupont@techcorp.fr'
         ) ON CONFLICT (id) DO NOTHING;

-- ────────────────────────────────────────────────────────────────
-- Repositionne la séquence au-delà du dernier id réservé
-- ────────────────────────────────────────────────────────────────
SELECT setval('offres_id_seq', GREATEST((SELECT MAX(id) FROM offres), 5));
