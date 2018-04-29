DROP INDEX IF EXISTS  users_nickname_idx;
CREATE INDEX IF NOT EXISTS users_nickname_idx ON users(nickname);

DROP INDEX IF EXISTS  forum_slug_idx;
CREATE INDEX IF NOT EXISTS forum_slug_idx ON forums(slug);

DROP INDEX IF EXISTS  threads_slug_idx;
CREATE INDEX IF NOT EXISTS threads_slug_idx ON threads(slug);

