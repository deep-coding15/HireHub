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

-- ================================================================
-- APPLICATION-SERVICE — seeds/V2__test_candidatures.sql
-- Complète V1 (Alice → offre #1, ENTRETIEN) pour couvrir
-- tous les statuts métier et cas limites
-- ================================================================


-- ────────────────────────────────────────────────────────────────
-- CANDIDATURE SOUMISE — Alice → offre #5 (Product Manager)
-- → teste : statut initial après dépôt
--           visible dans GET /candidatures?statut=SOUMISE
--           Paul voit une nouvelle entrée dans son dashboard
--           event RabbitMQ APPLICATION_SUBMITTED publié
-- ────────────────────────────────────────────────────────────────
INSERT INTO candidatures (
    id, candidat_id, candidat_email, offre_id,
    cv_path, lettre_motivation_path,
    status, date_soumission, date_modification
) VALUES (
             'dddddddd-dddd-dddd-dddd-dddddddddddd',
             'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',   -- Alice Martin
             'alice.martin@candidat.fr',
             '5',                                       -- offre Product Manager
             '/uploads/cv/alice_martin_cv.pdf',
             NULL,                                      -- pas de lettre → teste champ optionnel
             'SOUMISE',
             NOW() - INTERVAL '1 day',
             NOW() - INTERVAL '1 day'
         ) ON CONFLICT DO NOTHING;

INSERT INTO historique_statut (
    id, candidature_id,
    ancien_status, nouveau_status,
    commentaire, utilisateur_id, date_changement
) VALUES (
             'dddd1111-dddd-dddd-dddd-dddddddddddd',
             'dddddddd-dddd-dddd-dddd-dddddddddddd',
             NULL, 'SOUMISE',
             'Candidature déposée par le candidat.',
             'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
             NOW() - INTERVAL '1 day'
         ) ON CONFLICT (id) DO NOTHING;


-- ────────────────────────────────────────────────────────────────
-- CANDIDATURE REFUSÉE — Alice → offre #3 (Data Analyst, EXPIRÉE)
-- → teste : statut REFUSEE visible dans l'historique candidat
--           candidat ne peut pas re-postuler sur la même offre
--           notification CANDIDATURE_REFUSEE envoyée
--           offre expirée : refus auto déclenché par le scheduler
-- ────────────────────────────────────────────────────────────────
INSERT INTO candidatures (
    id, candidat_id, candidat_email, offre_id,
    cv_path, lettre_motivation_path,
    status, date_soumission, date_modification
) VALUES (
             '11111111-1111-1111-1111-111111111111',
             'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',   -- Alice Martin
             'alice.martin@candidat.fr',
             '3',                                       -- offre Data Analyst (EXPIRÉE)
             '/uploads/cv/alice_martin_cv.pdf',
             '/uploads/lettres/alice_martin_lettre_data.pdf',
             'REFUSEE',
             NOW() - INTERVAL '20 days',
             NOW() - INTERVAL '14 days'
         ) ON CONFLICT DO NOTHING;

INSERT INTO historique_statut (
    id, candidature_id,
    ancien_status, nouveau_status,
    commentaire, utilisateur_id, date_changement
) VALUES
      (
          '11111111-1111-1111-1111-aaaaaaaaaaaa',
          '11111111-1111-1111-1111-111111111111',
          NULL, 'SOUMISE',
          'Candidature déposée par le candidat.',
          'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
          NOW() - INTERVAL '20 days'
      ),
      (
          '11111111-1111-1111-1111-bbbbbbbbbbbb',
          '11111111-1111-1111-1111-111111111111',
          'SOUMISE', 'REFUSEE',
          'Offre arrivée à expiration — candidature clôturée automatiquement.',
          'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
          NOW() - INTERVAL '14 days'
      )
    ON CONFLICT (id) DO NOTHING;


-- ────────────────────────────────────────────────────────────────
-- CANDIDATURE ACCEPTÉE — Alice → offre #1 (après l'entretien)
-- Représente le futur de la candidature cccc… une fois l'entretien passé
-- → On crée une 2ème candidature distincte pour ne pas casser le happy path
--   (en pratique : même offre, même candidat, mais état final simulé)
-- → teste : statut ACCEPTEE dans le dashboard candidat
--           badge "Félicitations" côté UI
--           event APPLICATION_ACCEPTED → notification-service
--           recruteur ne peut plus modifier le statut
-- ────────────────────────────────────────────────────────────────
INSERT INTO candidatures (
    id, candidat_id, candidat_email, offre_id,
    cv_path, lettre_motivation_path,
    status, date_soumission, date_modification
) VALUES (
             '22222222-2222-2222-2222-222222222222',
             'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',   -- Alice Martin
             'alice.martin@candidat.fr',
             '5',                                       -- offre Product Manager
             '/uploads/cv/alice_martin_cv_v2.pdf',
             '/uploads/lettres/alice_martin_lettre_pm.pdf',
             'ACCEPTEE',
             NOW() - INTERVAL '30 days',
             NOW() - INTERVAL '2 days'
         ) ON CONFLICT DO NOTHING;

INSERT INTO historique_statut (
    id, candidature_id,
    ancien_status, nouveau_status,
    commentaire, utilisateur_id, date_changement
) VALUES
      (
          '22222222-2222-2222-2222-aaaaaaaaaaaa',
          '22222222-2222-2222-2222-222222222222',
          NULL, 'SOUMISE',
          'Candidature déposée par le candidat.',
          'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
          NOW() - INTERVAL '30 days'
      ),
      (
          '22222222-2222-2222-2222-bbbbbbbbbbbb',
          '22222222-2222-2222-2222-222222222222',
          'SOUMISE', 'EN_COURS',
          'Profil retenu pour une revue approfondie.',
          'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
          NOW() - INTERVAL '25 days'
      ),
      (
          '22222222-2222-2222-2222-cccccccccccc',
          '22222222-2222-2222-2222-222222222222',
          'EN_COURS', 'ENTRETIEN',
          'Entretien RH planifié.',
          'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
          NOW() - INTERVAL '10 days'
      ),
      (
          '22222222-2222-2222-2222-dddddddddddd',
          '22222222-2222-2222-2222-222222222222',
          'ENTRETIEN', 'ACCEPTEE',
          'Candidat retenu à l''issue des entretiens. Offre transmise par email.',
          'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
          NOW() - INTERVAL '2 days'
      )
    ON CONFLICT (id) DO NOTHING;


-- ────────────────────────────────────────────────────────────────
-- CANDIDATURE D'UN UTILISATEUR BLOQUÉ — Jean → offre #1
-- → teste : middleware vérifie blocked=true avant persistence
--           la candidature NE DEVRAIT PAS exister si le filtre fonctionne
--           utile pour tester le cas où la vérification est bypassée en base
-- ────────────────────────────────────────────────────────────────
INSERT INTO candidatures (
    id, candidat_id, candidat_email, offre_id,
    cv_path, lettre_motivation_path,
    status, date_soumission, date_modification
) VALUES (
             '33333333-3333-3333-3333-333333333333',
             'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',   -- Jean Moreau (bloqué)
             'jean.moreau@spam.fr',
             '1',                                       -- offre Java Spring Boot
             '/uploads/cv/jean_moreau_cv.pdf',
             NULL,
             'SOUMISE',
             NOW() - INTERVAL '10 days',
             NOW() - INTERVAL '10 days'
         ) ON CONFLICT DO NOTHING;

INSERT INTO historique_statut (
    id, candidature_id,
    ancien_status, nouveau_status,
    commentaire, utilisateur_id, date_changement
) VALUES (
             '33333333-3333-3333-3333-aaaaaaaaaaaa',
             '33333333-3333-3333-3333-333333333333',
             NULL, 'SOUMISE',
             '[TEST] Candidature insérée directement en base — bypass du filtre candidat bloqué.',
             'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
             NOW() - INTERVAL '10 days'
         ) ON CONFLICT (id) DO NOTHING;