-- Seed : candidature Alice → offre 1, statut ENTRETIEN
INSERT INTO candidatures (id, candidat_id, candidat_email, offre_id,
                          cv_path, lettre_motivation_path, status,
                          date_soumission, date_modification)
VALUES (
    'cccccccc-cccc-cccc-cccc-cccccccccccc',
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'alice.martin@candidat.fr',
    '1',
    '/uploads/cv/alice_martin_cv.pdf',
    '/uploads/lettres/alice_martin_lettre.pdf',
    'ENTRETIEN',
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '1 day'
) ON CONFLICT DO NOTHING;

-- Seed : historique des transitions de statut
INSERT INTO historique_statut (id, candidature_id, ancien_status, nouveau_status,
                               commentaire, utilisateur_id, date_changement)
VALUES
    ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
     'cccccccc-cccc-cccc-cccc-cccccccccccc',
     NULL, 'SOUMISE',
     'Candidature déposée par le candidat.',
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
     NOW() - INTERVAL '5 days'),

    ('ffffffff-ffff-ffff-ffff-ffffffffffff',
     'cccccccc-cccc-cccc-cccc-cccccccccccc',
     'SOUMISE', 'EN_COURS',
     'Profil intéressant, passage en revue détaillée.',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     NOW() - INTERVAL '3 days'),

    ('11111111-2222-3333-4444-555555555555',
     'cccccccc-cccc-cccc-cccc-cccccccccccc',
     'EN_COURS', 'ENTRETIEN',
     'Entretien technique planifié.',
     'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
     NOW() - INTERVAL '1 day')

ON CONFLICT (id) DO NOTHING;
