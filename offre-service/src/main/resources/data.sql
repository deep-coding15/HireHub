-- Seed : offre publiée par le recruteur de test
INSERT INTO offres (id, titre, description, type_contrat, ville, salaire,
                    date_creation, date_expiration, statut, recruteur_id, recruteur_email)
VALUES (
    1,
    'Développeur Java Spring Boot',
    'Nous recherchons un développeur Java confirmé pour rejoindre notre équipe produit. '
    'Vous interviendrez sur nos APIs REST, nos microservices Spring Boot et nos pipelines CI/CD. '
    'Stack : Java 17, Spring Boot 3, PostgreSQL, RabbitMQ, Docker.',
    'CDI',
    'Paris',
    55000,
    NOW(),
    NOW() + INTERVAL '60 days',
    'PUBLIEE',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'paul.dupont@techcorp.fr'
) ON CONFLICT (id) DO NOTHING;

-- Repositionne la séquence au-delà de l'id réservé pour éviter les conflits futurs
SELECT setval('offres_id_seq', GREATEST((SELECT MAX(id) FROM offres), 1));
