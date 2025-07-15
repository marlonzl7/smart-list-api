CREATE TRIGGER set_updated_at
BEFORE UPDATE ON refresh_token
FOR EACH ROW
EXECUTE FUNCTION trigger_set_updated_at();