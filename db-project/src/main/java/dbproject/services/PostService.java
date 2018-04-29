package dbproject.services;

import dbproject.models.PostDetailModel;
import dbproject.models.PostModel;
import dbproject.models.PostUpdateModel;
import dbproject.models.ThreadModel;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static dbproject.rowmappers.RowMappers.readPost;
@Service
public class PostService {
    private JdbcTemplate jdbcTemplate;
    ThreadService threadService;
    ForumService forumService;
    UserService userService;
    public PostService(JdbcTemplate jdbcTemplate, ThreadService threadService,
                       ForumService forumService, UserService userService) {
        this.jdbcTemplate = jdbcTemplate;
        this.threadService = threadService;
        this.forumService = forumService;
        this.userService = userService;
    }

    @Transactional(rollbackFor = {DuplicateKeyException.class, DataAccessException.class})
    public List<PostModel> create2(List<PostModel> posts, String slug_or_id) throws DuplicateKeyException {

        // !!! Разобраться с Connection
        final String sqlCreate = "INSERT INTO Posts (id, user_id, created, forum_id, message, parent, thread_id, path, root_id)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, array_append(?, ?::INTEGER), ?)";

//        final String sqlAddCurrentIdToPath = "UPDATE Posts SET path = array_append(path, id) WHERE id = ?";
        final ThreadModel thread = threadService.getThreadBySlugOrID(slug_or_id);

        if (posts == null || posts.isEmpty()) {
            return new ArrayList<>();
        }

        final Integer forumId = threadService.getForumIdByThreadId(thread.getId());
        final String forumSlug = forumService.getForumSlugById(forumId);
        final String currentTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

        for (PostModel post: posts) {
            post.setForum(forumSlug);
            post.setThread(thread.getId());
            post.setCreated(currentTime);

            final Integer userId = userService.getUserIdByNickname(post.getAuthor());
            Integer parentId = 0;
            if (post.getParent() != null && post.getParent() != 0) {
                try {
                    parentId = getParentId(post.getParent(), thread.getId());
                } catch (DataAccessException ex) {
                    throw new DuplicateKeyException("parent not found");
                }
            }

            post.setParent(parentId);
            final Array path = parentId == 0 ? null : jdbcTemplate.queryForObject("SELECT path FROM Posts  WHERE id = ?", Array.class, parentId);
            final Integer id = getNextId();
            Integer root_id;
            try {
                root_id = parentId == 0 ? id : ((Integer[]) path.getArray())[0];
            } catch (SQLException e) {
                root_id = id;
            }

//            Integer[] ints = null;
//            try {
//                if (path != null) {
//                    ints = (Integer[]) path.getArray();
//                }
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//            if (ints != null) {
//                for (Integer a : ints) {
//                    System.out.print(a.toString() + '.');
//                }
//                System.out.println();
//            }

            jdbcTemplate.update(sqlCreate, id, userId, currentTime,
                                forumId, post.getMessage(), parentId, thread.getId(), path, id, root_id);
            //jdbcTemplate.update(sqlAddCurrentIdToPath, id);
            post.setId(id);

            // Обновляем количество постов в ветке
            forumService.updatePostCount(forumId, 1);
        }

        return posts;
    }

    public void create(List<PostModel> posts, String slug_or_id) throws DuplicateKeyException {
        final String sqlCreate = "INSERT INTO Posts (id, user_id, created, forum_id, message, parent, thread_id, path, root_id)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, array_append(?, ?::INTEGER), ?)";
        final String sqlUpdateForumUsers = "INSERT INTO forum_users (user_id, forum_id) VALUES (?, ?) ON CONFLICT (user_id, forum_id) DO NOTHING";
//        final String sqlAddCurrentIdToPath = "UPDATE Posts SET path = array_append(path, id) WHERE id = ?";
        final ThreadModel thread = threadService.getThreadBySlugOrID(slug_or_id);

        if (posts == null || posts.isEmpty()) {
            return;
//            return new ArrayList<>();
        }

        final Integer forumId = threadService.getForumIdByThreadId(thread.getId());
        final String forumSlug = forumService.getForumSlugById(forumId);
        final String currentTime = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlCreate, Statement.NO_GENERATED_KEYS);
                 PreparedStatement preparedStatementUpdateForumUsers = connection.prepareStatement(sqlUpdateForumUsers, Statement.NO_GENERATED_KEYS)) {
                for (PostModel post : posts) {
                    final Integer id = getNextId();
                    final Integer userId = userService.getUserIdByNickname(post.getAuthor());
                    Integer parentId = 0;
                    if (post.getParent() != null && post.getParent() != 0) {
                        try {
                            parentId = getParentId(post.getParent(), thread.getId());
                        } catch (DataAccessException ex) {
                            throw new DuplicateKeyException("parent not found");
                        }
                    }


                    post.setParent(parentId);
                    preparedStatement.setInt(1, id);
                    preparedStatement.setInt(9, id);
                    preparedStatement.setInt(2, userId);
                    preparedStatement.setString(3, currentTime);
                    preparedStatement.setInt(4, forumId);
                    preparedStatement.setString(5, post.getMessage());
                    preparedStatement.setInt(6, parentId);
                    preparedStatement.setInt(7, thread.getId());

                    final Array path = parentId == 0 ? null : jdbcTemplate.queryForObject("SELECT path FROM Posts  WHERE id = ?", Array.class, parentId);

                    Integer root_id;
                    try {
                        root_id = parentId == 0 ? id : ((Integer[]) path.getArray())[0];
                    } catch (SQLException e) {
                        root_id = id;
                    }
                    preparedStatement.setArray(8, path);
                    preparedStatement.setInt(10, root_id);
                    preparedStatement.addBatch();

                    preparedStatementUpdateForumUsers.setInt(1, userId);
                    preparedStatementUpdateForumUsers.setInt(2,forumId);
                    preparedStatementUpdateForumUsers.addBatch();

                    post.setId(id);
                    post.setCreated(currentTime);
                    post.setForum(forumSlug);
                    post.setParent(parentId);
                    post.setThread(thread.getId());

                }

                preparedStatement.executeBatch();
                preparedStatementUpdateForumUsers.executeBatch();
                connection.commit();
                forumService.updatePostCount(forumId, posts.size());
            } catch (SQLException ex) {
                connection.rollback();
                throw new DataRetrievalFailureException(ex.getMessage());
            } finally {
                connection.setAutoCommit(true);
            }
        return;
        } catch (SQLException ex) {
            throw new DataRetrievalFailureException(ex.getMessage());
        }
    }

    public Integer getParentId(Integer postId, Integer threadId) {
        final String sqlIsCorrectParent = "SELECT id FROM posts WHERE id = ? and thread_id = ?";
        return jdbcTemplate.queryForObject(sqlIsCorrectParent, Integer.class, postId, threadId);
    }

    public PostModel getPostById(Integer postId) {
        final String sqlGetPost = "SELECT (SELECT nickname from users WHERE id = p.user_id) as author, " +
                " (SELECT slug FROM Forums WHERE id = p.forum_id) as forum, " +
                " created, id, is_edited as isEdited, message, parent, thread_id as thread " +
                " from posts p where id = ?";

         return jdbcTemplate.queryForObject(sqlGetPost, readPost, postId);
    }

    public PostDetailModel getInfo(Integer postId, List<String> related) {

        final PostDetailModel postDetail = new PostDetailModel();
        final PostModel post = getPostById(postId);
        postDetail.setPost(post);

        if (related == null) {
            return postDetail;
        }

        if (related.contains("user")) {
            postDetail.setAuthor(userService.getUserByNickname(post.getAuthor()));
        }

        if (related.contains("thread")) {

            postDetail.setThread(threadService.getThreadById(post.getThread()));
        }

        if (related.contains("forum")) {
            postDetail.setForum(forumService.getBySlug(post.getForum()));
        }

        return postDetail;
    }

    public PostModel updatePost(Integer id, PostUpdateModel postUpdateModel) {
        final String sqlUpdatePost = "UPDATE Posts SET message = ?, is_edited = TRUE WHERE id = ?";

        // Можно написать метод, который возращает message по postID
        final PostModel post = getPostById(id);
        if (postUpdateModel.getMessage() != null && !postUpdateModel.getMessage().equals(post.getMessage())) {
            jdbcTemplate.update(sqlUpdatePost, postUpdateModel.getMessage(), id);
        }
        return getPostById(id);
    }

    public Integer getNextId() {
        final String sqlGetNext = "SELECT nextval(pg_get_serial_sequence('posts', 'id'))";
        return jdbcTemplate.queryForObject(sqlGetNext, Integer.class);
    }

    public Integer getRowCount() {
        final String sqlQuery = "SELECT COUNT(*) FROM Posts";
        return jdbcTemplate.queryForObject(sqlQuery, Integer.class);
    }

    public void clear() {
        final String sqlQuery = "DELETE FROM Posts";
        jdbcTemplate.execute(sqlQuery);
    }

}
