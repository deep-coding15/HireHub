CREATE TABLE IF NOT EXISTS entretiens (
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    candidature_id VARCHAR(255) NOT NULL,
    candidat_id    VARCHAR(255) NOT NULL,
    recruteur_id   VARCHAR(255) NOT NULL,
    date_heure     TIMESTAMP   NOT NULL,
    lieu           VARCHAR(500),
    lien_visio     VARCHAR(1000),
    type           VARCHAR(50)  NOT NULL,
    notes_internes VARCHAR(2000),
    status         VARCHAR(50)  NOT NULL,
    date_creation     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    date_annulation   TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_entretien_candidature    ON entretiens(candidature_id);
CREATE INDEX IF NOT EXISTS idx_entretien_recruteur_date ON entretiens(recruteur_id, date_heure);
CREATE INDEX IF NOT EXISTS idx_entretien_candidat_date  ON entretiens(candidat_id,  date_heure);