package dbproject.services;

import dbproject.models.ForumModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import static dbproject.rowmappers.RowMappers.readForum;

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
        final String sql = "SELECT f.slug, f.title, f.threads, f.posts, (Select nickname from users where id = f.user_id) as user " +
                "from forums as f where slug = ?";

        return jdbcTemplate.queryForObject(sql, readForum, slug);
    }
}
