package dbproject.services;

import dbproject.models.ForumModel;
import dbproject.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dbproject.rowmappers.RowMappers.readForum;
import static dbproject.rowmappers.RowMappers.readThread;

@Service
public class ForumService {

    private JdbcTemplate jdbcTemplate;

    public ForumService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ForumModel create(ForumModel forum) {
        final String sql = "INSERT INTO Forums (slug, title, user_id) VALUES (?, ?, (SELECT id FROM Users WHERE nickname = ?))";
        jdbcTemplate.update(sql, forum.getSlug(), forum.getTitle(), forum.getUser());
        return getBySlug(forum.getSlug());
    }

    public ForumModel getBySlug(String slug) {
        final String sql = "SELECT f.slug, f.title, f.threads, f.posts, (Select nickname from users where id = f.user_id) as nickname " +
                "from forums as f where slug = ?";

        return jdbcTemplate.queryForObject(sql, readForum, slug);
    }

    public String getForumSlugById(Integer id) {
        final String sql = "SELECT slug FROM forums WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, String.class, id);
    }

    public List<ThreadModel> getThreads(String slug, String since, Boolean desc, Integer limit) {
        final StringBuilder sqlCreate = new StringBuilder();
        final List<Object> params = new ArrayList<>();

        sqlCreate.append("SELECT nickname as author, created, f.slug as forum, t.id, message, t.slug, t.title, votes " +
                " FROM threads t JOIN forums f ON t.forum_id = f.id JOIN Users u ON t.user_id = u.id " +
                " WHERE f.slug = ? ");
        params.add(slug);

        if (since != null) {
            if (Objects.equals(desc, Boolean.TRUE)) {
                sqlCreate.append("and created <= ? ");
            } else {
                sqlCreate.append("and created >= ? ");
            }
            params.add(since);
        }

        sqlCreate.append("ORDER BY created ");

        sqlCreate.append(Objects.equals(desc, Boolean.TRUE) ? " DESC " : "");

        if (limit != null) {
            sqlCreate.append("LIMIT ?");
            params.add(limit);
        }

        return jdbcTemplate.query(sqlCreate.toString(), readThread, params.toArray());
    }

    public void updatePostCount(Integer postId, Integer diff) {
        final String sqlUpdatePostCount = "UPDATE Forums SET posts = posts + ? WHERE id = ?";
        jdbcTemplate.update(sqlUpdatePostCount, diff, postId);
    }

}
