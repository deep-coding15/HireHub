-- Executer dans la base hirehub_offre :
-- docker exec -i hirehub-postgres psql -U hirehub -d hirehub_offre < scripts/fix-offres-recruteur-id.sql

UPDATE offres
SET recruteur_id = '20eef518-ac5a-4aaf-a8a7-997c3d9933fb',
    recruteur_email = 'elassal.douae@etu.uae.ac.ma'
WHERE recruteur_id = '10';
