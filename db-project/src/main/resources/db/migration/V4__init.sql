CREATE INDEX post_root_id_path_idx ON posts(root_id, path);
CREATE INDEX post_thread_parent_id_idx ON posts(thread_id, parent, id);
