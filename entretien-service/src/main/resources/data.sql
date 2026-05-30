-- Seed : entretien visio planifié pour Alice (dans 2 jours)
INSERT INTO entretiens (id, candidature_id, candidat_id, recruteur_id,
                        date_heure, lieu, lien_visio, type, notes_internes,
                        status, date_creation, date_modification)
VALUES (
    'dddddddd-dddd-dddd-dddd-dddddddddddd',
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    NOW() + INTERVAL '2 days',
    NULL,
    'https://meet.google.com/abc-defg-hij',
    'VISIO',
    'Entretien technique 45 min : algo, Java, Spring Boot. Vérifier expérience microservices.',
    'PLANIFIE',
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day'
) ON CONFLICT (id) DO NOTHING;
