-- A executer dans pgAdmin (Query Tool) sur le serveur PostgreSQL utilise par HireHub.
-- Utile si les bases email/event n'existent pas (volume Docker cree avant 01-databases.sql complet).

CREATE DATABASE hirehub_email OWNER hirehub;
CREATE DATABASE hirehub_event OWNER hirehub;
