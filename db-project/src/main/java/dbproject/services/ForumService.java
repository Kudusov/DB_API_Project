package dbproject.services;

import dbproject.models.ForumModel;
import dbproject.models.ThreadModel;
import dbproject.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dbproject.rowmappers.RowMappers.readForum;
import static dbproject.rowmappers.RowMappers.readThread;
import static dbproject.rowmappers.RowMappers.readUser;

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

    public Integer getForumIdBySlug(String slug) {
        final String sql = "SELECT id FROM forums WHERE slug = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, slug);
    }

    public String getSqlForumOrThreadUsers(String table, String since, Boolean desc) {
        final StringBuilder sqlQuery = new StringBuilder();
        sqlQuery.append(" SELECT u.nickname, u.fullname, u.email, u.about FROM ");
        sqlQuery.append(table);
        sqlQuery.append(" t ");
        sqlQuery.append(" JOIN users u ON t.user_id = u.id ");
        if (since != null) {
            sqlQuery.append(" and nickname ");
            if ( desc != null && desc.equals(Boolean.TRUE)) {
                sqlQuery.append(" < ? ");
            } else {
                sqlQuery.append(" > ? ");
            }
        }
        sqlQuery.append(" WHERE t.forum_id = ? ");

        return sqlQuery.toString();
    }


    /*
        Оптимизировать getUsers в первую очередь!!!
     */


    /*
SELECT u.nickname, u.fullname, u.email, u.about
FROM threads t JOIN users u ON t.user_id = u.id and nickname > 'C183Qmq78N1Vj._joe' WHERE t.forum_id = (SELECT id FROM forums WHERE slug = '9ncMq6g894v2s')
UNION DISTINCT
SELECT u.nickname, u.fullname, u.email, u.about
FROM posts p JOIN users u ON p.user_id = u.id and nickname > 'C183Qmq78N1Vj._joe' WHERE p.forum_id = (SELECT id FROM forums WHERE slug = '9ncMq6g894v2s')
ORDER BY fullname
     */
    public List<UserModel> getUsers2(String slug, String since, Boolean desc, Integer limit) {
        final Integer forumId = getForumIdBySlug(slug);
        final StringBuilder sqlQuery = new StringBuilder();
        final List<Object> params = new ArrayList<>();
        sqlQuery.append(getSqlForumOrThreadUsers("threads", since, desc));
        if (since != null) {
            params.add(since);
        }
        params.add(forumId);
        params.addAll(params);
        sqlQuery.append(" UNION DISTINCT ");
        sqlQuery.append(getSqlForumOrThreadUsers("posts", since, desc));
        sqlQuery.append(" ORDER BY nickname ");
        if (desc != null) {
            if (desc.equals(Boolean.TRUE)) {
                sqlQuery.append(" DESC ");
            }
        }

        if (limit != null) {
            sqlQuery.append(" LIMIT ? ");
            params.add(limit);
        }

        return jdbcTemplate.query(sqlQuery.toString(), readUser, params.toArray());
    }

    public List<UserModel> getUsers(String slug, String since, Boolean desc, Integer limit) {
        final Integer forumId = getForumIdBySlug(slug);
        final StringBuilder sqlQuery = new StringBuilder();
        final List<Object> params = new ArrayList<>();

        sqlQuery.append("SELECT u.nickname, u.fullname, u.email, u.about FROM users u Where u.id IN " +
                        "(SELECT user_id  FROM forum_users WHERE forum_id = ?)");
        params.add(forumId);
        if (since != null) {
            sqlQuery.append(" and u.nickname ");
            if ( desc != null && desc.equals(Boolean.TRUE)) {
                sqlQuery.append(" < ? ");
            } else {
                sqlQuery.append(" > ? ");
            }
            params.add(since);
        }
        sqlQuery.append(" ORDER BY u.nickname ");
        if (desc != null) {
            if (desc.equals(Boolean.TRUE)) {
                sqlQuery.append(" DESC ");
            }
        }

        if (limit != null) {
            sqlQuery.append(" LIMIT ? ");
            params.add(limit);
        }

        return jdbcTemplate.query(sqlQuery.toString(), readUser, params.toArray());
    }

    public List<ThreadModel> getThreads(String slug, String since, Boolean desc, Integer limit) {
        final Integer forumId = getForumIdBySlug(slug);
        final StringBuilder sqlCreate = new StringBuilder();
        final List<Object> params = new ArrayList<>();

        sqlCreate.append("SELECT nickname as author, created, f.slug as forum, t.id, message, t.slug, t.title, votes " +
                " FROM threads t JOIN forums f ON t.forum_id = f.id JOIN Users u ON t.user_id = u.id " +
                " WHERE f.id = ? ");
        params.add(forumId);

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

    public List<ThreadModel> getThreads2(String slug, String since, Boolean desc, Integer limit) {
        final Integer forumId = getForumIdBySlug(slug);
        final StringBuilder sqlCreate = new StringBuilder();
        final List<Object> params = new ArrayList<>();

        sqlCreate.append("SELECT nickname AS author, created, ? AS forum, t.id, message, t.slug, t.title, votes " +
                " FROM threads t JOIN users u ON t.user_id = u.id WHERE t.forum_id = ? ");

        params.add(slug);
        params.add(forumId);

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

    public Integer getRowCount() {
        final String sqlQuery = "SELECT COUNT(*) FROM forums";
        return jdbcTemplate.queryForObject(sqlQuery, Integer.class);
    }

    public void clear() {
        final String sqlQuery = "DELETE FROM Forums";
        jdbcTemplate.execute(sqlQuery);
    }
}
