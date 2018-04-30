CREATE INDEX posts_thread_id_post_id_idx ON posts(thread_id, id);
CREATE INDEX threads_created_idx ON threads(created);