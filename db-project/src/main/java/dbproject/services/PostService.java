package dbproject.services;

import dbproject.models.PostDetailModel;
import dbproject.models.PostModel;
import dbproject.models.PostUpdateModel;
import dbproject.models.ThreadModel;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
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


    public List<PostModel> create(List<PostModel> posts, String slug_or_id) throws DuplicateKeyException {

        // !!! Разобраться с Connection
        final String sqlCreate = "INSERT INTO Posts (id, user_id, created, forum_id, message, parent, thread_id, path)" +
                " VALUES(?, ?, ?, ?, ?, ?, ?, array_append(?, ?::INTEGER))";

        final String sqlAddCurrentIdToPath = "UPDATE Posts SET path = array_append(path, id) WHERE id = ?";
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
                                forumId, post.getMessage(), parentId, thread.getId(), path, id);
            //jdbcTemplate.update(sqlAddCurrentIdToPath, id);
            post.setId(id);

            // Обновляем количество постов в ветке
            forumService.updatePostCount(forumId, 1);
        }

        return posts;
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
