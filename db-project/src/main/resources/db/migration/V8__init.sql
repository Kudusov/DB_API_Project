DROP INDEX IF EXISTS forum_users_forum_id_idx;
DROP INDEX IF EXISTS forum_users_user_id_idx;

CREATE INDEX IF NOT EXISTS forum_users_user_id_idx ON forum_users(user_id);
CREATE INDEX IF NOT EXISTS forum_users_forum_id_idx ON forum_users(forum_id);