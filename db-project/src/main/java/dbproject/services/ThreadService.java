package dbproject.services;

import dbproject.models.PostModel;
import dbproject.models.ThreadModel;
import dbproject.models.ThreadUpdateModel;
import dbproject.models.VoteModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static dbproject.rowmappers.RowMappers.readThread;
import static dbproject.rowmappers.RowMappers.readPost;

@Service
public class ThreadService {
    private JdbcTemplate jdbcTemplate;
    private UserService userService;

    public ThreadService(JdbcTemplate jdbcTemplate, UserService userService) {
        this.jdbcTemplate = jdbcTemplate;
        this.userService = userService;
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

    public ThreadModel getThreadBySlugOrID(String slug_or_id) {
        if (slug_or_id.matches("\\d+")) {
            return getThreadById(Integer.parseInt(slug_or_id));
        }
        return getThreadBySlug(slug_or_id);
    }

    public void updateThreadCount(String slug, Integer count) {
        final String sqlUpdateThreadCount = "UPDATE Forums SET threads = threads + ? WHERE slug = ?";
        jdbcTemplate.update(sqlUpdateThreadCount, count, slug);
    }

    public Integer getForumIdByThreadId(Integer id) {
        final String sqlGetForumId = "SELECT forum_id from Threads where id = ?";

        return jdbcTemplate.queryForObject(sqlGetForumId, Integer.class, id);
    }

    public ThreadModel insertOrUpdateVotes(VoteModel voteModel, String slug_or_id) {
        final String sqlInsertOrUpdateVotes = "INSERT INTO Votes (user_id, thread_id, voice) VALUES (?, ?, ?)" +
                " ON CONFLICT (user_id, thread_id) DO UPDATE SET voice = EXCLUDED.voice";

        final String sqlUpdateVotes = "UPDATE Threads SET votes = (SELECT SUM(voice) FROM Votes WHERE thread_id = ?) " +
                "WHERE id = ?";

        final Integer userId = userService.getUserIdByNickname(voteModel.getNickname());
        final ThreadModel thread = getThreadBySlugOrID(slug_or_id);
        jdbcTemplate.update(sqlInsertOrUpdateVotes, userId, thread.getId(), voteModel.getVoice());
        jdbcTemplate.update(sqlUpdateVotes, thread.getId(), thread.getId());
        return getThreadBySlugOrID(slug_or_id);
    }

    public ThreadModel updateThread(ThreadUpdateModel updateData, String slug_or_id) {
        final ThreadModel thread = getThreadBySlugOrID(slug_or_id);

        final String sqlUpdate =  "UPDATE Threads SET message = ?, title = ? WHERE id = ?";
        Boolean isEmptyData = true;
        if (updateData.getMessage() != null) {
            thread.setMessage(updateData.getMessage());
            isEmptyData = false;
        }

        if (updateData.getTitle() != null) {
            thread.setTitle(updateData.getTitle());
            isEmptyData = false;
        }

        if (!isEmptyData) {
            jdbcTemplate.update(sqlUpdate, thread.getMessage(), thread.getTitle(), thread.getId());
        }

        return thread;
    }

    public List<PostModel> getPosts(String slug_or_id, Integer limit, Integer since, String sort, Boolean desc) {
        final ThreadModel thread = getThreadBySlugOrID(slug_or_id);
        if (sort == null)
            sort = "flat";
        switch (sort) {
            case "tree":
                return getSqlSortTree(thread.getId(), limit, since, desc);
            case "parent_tree":
                return getSqlSortParentTree(thread.getId(), limit, since, desc);
            default:
                return getSqlSortFlat(thread.getId(), limit, since, desc);
        }

    }

    public List<PostModel> getSqlSortFlat(Integer thread_id, Integer limit, Integer since, Boolean desc) {
        final StringBuilder sqlQuery = new StringBuilder();
        final ArrayList<Object> params = new ArrayList<>();
        sqlQuery.append("SELECT u.nickname as author, p.created, f.slug as forum, p.id, p.is_edited as isEdited, p.message, p.parent, p.thread_id as thread " +
                " FROM Posts p JOIN users u ON p.user_id = u.id JOIN forums f on p.forum_id = f.id WHERE thread_id = ? ");
        params.add(thread_id);

        if (since != null) {
            sqlQuery.append(" AND p.id " );

            if (desc != null && desc.equals(Boolean.TRUE)) {
                sqlQuery.append(" < ? ");
            } else {
                sqlQuery.append(" > ? ");
            }

            params.add(since);
        }

        sqlQuery.append(" ORDER BY (p.id) ");
        if (desc != null && desc.equals(Boolean.TRUE)) {
            sqlQuery.append(" DESC ");
        }

        if (limit != null) {
            sqlQuery.append(" LIMIT ? ");
            params.add(limit);
        }

        return jdbcTemplate.query(sqlQuery.toString(), readPost, params.toArray());
    }

    public List<PostModel> getSqlSortTree(Integer thread_id, Integer limit, Integer since, Boolean desc) {
        final StringBuilder sqlQuery = new StringBuilder();
        final ArrayList<Object> params = new ArrayList<>();
        sqlQuery.append("SELECT u.nickname as author, p.created, f.slug as forum, p.id, p.is_edited as isEdited, p.message, p.parent, p.thread_id as thread " +
                " FROM Posts p JOIN users u ON p.user_id = u.id JOIN forums f on p.forum_id = f.id WHERE thread_id = ? ");
        params.add(thread_id);

        if (since != null) {
            sqlQuery.append(" AND p.path " );

            if (desc != null && desc.equals(Boolean.TRUE)) {
                sqlQuery.append(" < ");
            } else {
                sqlQuery.append(" > ");
            }

            sqlQuery.append(" (SELECT path from Posts Where id = ?) ");
            params.add(since);
        }

        sqlQuery.append(" ORDER BY p.path ");
        if (desc != null && desc.equals(Boolean.TRUE)) {
            sqlQuery.append(" DESC ");
        }

        if (limit != null) {
            sqlQuery.append(" LIMIT ? ");
            params.add(limit);
        }

        return jdbcTemplate.query(sqlQuery.toString(), readPost, params.toArray());
    }

    public List<PostModel> getSqlSortParentTree(Integer thread_id, Integer limit, Integer since, Boolean desc) {
        final StringBuilder sqlQuery = new StringBuilder();
        final ArrayList<Object> params = new ArrayList<>();
        sqlQuery.append("SELECT u.nickname as author, p.created, f.slug as forum, p.id, p.is_edited as isEdited, p.message, p.parent, p.thread_id as thread " +
                " FROM Posts p JOIN users u ON p.user_id = u.id JOIN forums f on p.forum_id = f.id " +
                " WHERE p.root_id IN ( SELECT id FROM posts WHERE thread_id = ? AND parent = 0 ");
        params.add(thread_id);

        if (since != null) {
            sqlQuery.append(" AND id " );

            if (desc != null && desc.equals(Boolean.TRUE)) {
                sqlQuery.append(" < ");
            } else {
                sqlQuery.append(" > ");
            }

            sqlQuery.append(" (SELECT root_id from Posts Where id = ?) ");
            params.add(since);
        }

        sqlQuery.append(" ORDER BY id ");
        if (desc != null && desc.equals(Boolean.TRUE)) {
            sqlQuery.append(" DESC ");
        }

        if (limit != null) {
            sqlQuery.append(" LIMIT ? ");
            params.add(limit);
        }

        sqlQuery.append(" ) ORDER BY ");
        if (desc != null && desc.equals(Boolean.TRUE)) {
            sqlQuery.append(" p.root_id DESC, ");
        }
        sqlQuery.append(" p.path ");

        return jdbcTemplate.query(sqlQuery.toString(), readPost, params.toArray());
    }

    public Integer getRowCount() {
        final String sqlQuery = "SELECT COUNT(*) FROM Threads";
        return jdbcTemplate.queryForObject(sqlQuery, Integer.class);
    }

    public void clear() {
        final String sqlQuery = "DELETE FROM Threads";
        jdbcTemplate.execute(sqlQuery);
    }
}
