package dbproject.services;

import dbproject.models.PostModel;
import dbproject.models.ThreadModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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



    public List<PostModel> create(List<PostModel> posts, String slug_or_id) {

        // Можно сначала запросить id, и сделать просто Insert без returning
        final String sqlCreate = "INSERT INTO Posts (user_id, created, forum_id, message, parent, thread_id)" +
                " VALUES(?, ?, ?, ?, ?, ?) RETURNING id";

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
                parentId = isCorrectParent(post.getParent(), thread.getId());
            }

            post.setParent(parentId);
            final Integer id = jdbcTemplate.queryForObject(sqlCreate, Integer.class, userId, currentTime, forumId, post.getMessage(), parentId, thread.getId());
            post.setId(id);
        }

        return posts;
    }

    public Integer isCorrectParent(Integer postId, Integer threadId) {
        final String sqlIsCorrectParent = "SELECT id FROM posts WHERE id = ? and thread_id = ?";
        return jdbcTemplate.queryForObject(sqlIsCorrectParent, Integer.class, postId, threadId);
    }
}
