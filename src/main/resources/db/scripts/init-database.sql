DROP DATABASE IF EXISTS smartlist;
CREATE DATABASE smartlist;

\c smartlist;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT FROM pg_catalog.pg_roles WHERE rolname = 'smartlist_user'
    ) THEN
        CREATE USER smartlist_user WITH PASSWORD '<senha-ficticia>';
    END IF;
END $$;

GRANT CONNECT ON DATABASE smartlist TO smartlist_user;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO smartlist_user;

GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO smartlist_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO smartlist_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO smartlist_user;
