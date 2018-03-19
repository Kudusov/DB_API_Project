package dbproject.controllers;

import dbproject.models.StatusModel;
import dbproject.services.ForumService;
import dbproject.services.PostService;
import dbproject.services.ThreadService;
import dbproject.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServerController {
    UserService userService;
    ForumService forumService;
    PostService postService;
    ThreadService threadService;

    public ServerController(UserService userService,
                            ForumService forumService,
                            PostService postService,
                            ThreadService threadService) {
        this.userService = userService;
        this.forumService = forumService;
        this.postService = postService;
        this.threadService = threadService;
    }

    @RequestMapping(path = "api/service/status", method = RequestMethod.GET)
    public ResponseEntity status() {
        final Integer users = userService.getRowCount();
        final Integer posts = postService.getRowCount();
        final Integer forums = forumService.getRowCount();
        final Integer threads = threadService.getRowCount();

        return ResponseEntity.status(HttpStatus.OK).body(new StatusModel(users, forums, threads, posts));
    }

    @RequestMapping(path = "api/service/clear", method = RequestMethod.POST)
    public ResponseEntity clear() {
        userService.clear();
        postService.clear();
        forumService.clear();
        threadService.clear();
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
