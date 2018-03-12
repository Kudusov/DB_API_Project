package dbproject.services;

import dbproject.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import static dbproject.rowmappers.RowMappers.readThread;
@Service
public class ThreadService {
    private JdbcTemplate jdbcTemplate;

    public ThreadService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ThreadModel create(ThreadModel thread, String forumSlug) {
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

        final String updateThreadCount = "UPDATE Forums SET threads = threads + ? WHERE slug = ?";

        final String getThreadById = "SELECT *, (SELECT nickname FROM users WHERE threads.user_id = id) as author," +
                "  (SELECT slug FROM forums WHERE threads.forum_id = id) as forum FROM Threads where id = ?";

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
        jdbcTemplate.update(updateThreadCount, 1, forumSlug);

        return jdbcTemplate.queryForObject(getThreadById, readThread, id);
    }
}
