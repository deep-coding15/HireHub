
-- Créer la table event_audit_log pour l'audit
CREATE TABLE IF NOT EXISTS event_audit_log (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    source_service VARCHAR(100),
    destination_service VARCHAR(100),
    status VARCHAR(20) DEFAULT 'SUCCESS',
    error_message TEXT
);

-- Index pour améliorer les requêtes
CREATE INDEX idx_event_id ON event_audit_log(event_id);
CREATE INDEX idx_event_type ON event_audit_log(event_type);
CREATE INDEX idx_created_at ON event_audit_log(created_at);
CREATE INDEX idx_status ON event_audit_log(status);
