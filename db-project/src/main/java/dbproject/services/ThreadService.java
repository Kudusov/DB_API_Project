package dbproject.services;

import dbproject.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static dbproject.rowmappers.RowMappers.readThread;
@Service
public class ThreadService {
    private JdbcTemplate jdbcTemplate;

    public ThreadService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @SuppressWarnings("unused")
    public ThreadModel create2(ThreadModel thread, String forumSlug) {
        final String createThread;
        if (thread.getCreated() != null) {
            createThread = "INSERT INTO Threads (user_id, created, forum_id, slug, message, title) " +
                    "VALUES((SELECT id FROM Users WHERE nickname = ?), ?, (SELECT id FROM Forums WHERE slug = ?), ?, ?, ?)" +
                    "RETURNING id";
        }

        else {
            createThread = "INSERT INTO Threads (user_id, forum_id, slug, message, title) " +
                    "VALUES((SELECT id FROM Users WHERE nickname = ?), (SELECT id FROM Forums WHERE slug = ?), ?, ?, ?)" +
                    "RETURNING id";
        }

        final Integer id;
        if (thread.getCreated() != null) {
            /* добаляем thread*/
            /* Можно сделать rowmapper который возвращает user_id, forum_id, thread_id сразу после добаления в таблицу */
            id = jdbcTemplate.queryForObject(createThread, Integer.class, thread.getAuthor(), thread.getCreated(), forumSlug,
                    thread.getSlug(), thread.getMessage(), thread.getTitle());
        }

        else {
            id = jdbcTemplate.queryForObject(createThread, Integer.class, thread.getAuthor(), forumSlug,
                    thread.getSlug(), thread.getMessage(), thread.getTitle());
        }
        /* обновляем количество тредов в форуме */
        updateThreadCount(forumSlug, 1);
        return getThreadById(id);

    }

    public ThreadModel create(ThreadModel thread, String forumSlug) {

        final StringBuilder sqlCreate = new StringBuilder();
        final List<Object> params = new ArrayList<>();

        sqlCreate.append("INSERT INTO Threads (user_id, ");
        params.add(thread.getAuthor());

        if (thread.getCreated() != null) {
            sqlCreate.append("created, ");
            params.add(thread.getCreated());
        }

        sqlCreate.append("forum_id, slug, message, title) VALUES((SELECT id FROM Users WHERE nickname = ?), ");
        if (thread.getCreated() != null) {
            sqlCreate.append("?, ");
        }

        sqlCreate.append("(SELECT id FROM Forums WHERE slug = ?), ?, ?, ?) RETURNING id");
        params.add(forumSlug);
        params.add(thread.getSlug());
        params.add(thread.getMessage());
        params.add(thread.getTitle());

        final Integer id = jdbcTemplate.queryForObject(sqlCreate.toString(), Integer.class, params.toArray());
        updateThreadCount(forumSlug, 1);
        return getThreadById(id);

    }

    public ThreadModel getThreadById(Integer id) {
        final String sqlGetThreadById = "SELECT *, (SELECT nickname FROM users WHERE threads.user_id = id) as author," +
                " (SELECT slug FROM forums WHERE threads.forum_id = id) as forum FROM Threads where id = ?";
        return  jdbcTemplate.queryForObject(sqlGetThreadById, readThread, id);
    }

    public ThreadModel getThreadBySlug(String slug) {
        final String sqlGetThreadBySlug = "SELECT *, (SELECT nickname FROM users WHERE threads.user_id = id) as author," +
                " (SELECT slug FROM forums WHERE threads.forum_id = id) as forum FROM Threads where slug = ?";

        return  jdbcTemplate.queryForObject(sqlGetThreadBySlug, readThread, slug);
    }

    public void updateThreadCount(String slug, Integer count) {
        final String sqlUpdateThreadCount = "UPDATE Forums SET threads = threads + ? WHERE slug = ?";
        jdbcTemplate.update(sqlUpdateThreadCount, count, slug);
    }
}
