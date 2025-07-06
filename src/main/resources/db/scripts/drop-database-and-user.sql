SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'smartlist';

REVOKE ALL PRIVILEGES ON DATABASE smartlist FROM smartlist_user;

DROP DATABASE IF EXISTS smartlist;

SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE usename = 'smartlist_user';

DROP USER IF EXISTS smartlist_user;
