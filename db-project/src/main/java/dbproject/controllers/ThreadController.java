package dbproject.controllers;

import dbproject.models.ErrorModel;
import dbproject.models.PostModel;
import dbproject.models.ThreadUpdateModel;
import dbproject.models.VoteModel;
import dbproject.services.PostService;
import dbproject.services.ThreadService;
import org.apache.coyote.Response;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ThreadController {
    ThreadService threadService;
    PostService postService;
    ThreadController(ThreadService threadService, PostService postService) {
        this.threadService = threadService;
        this.postService = postService;
    }

    @RequestMapping(path = "api/thread/{slug_or_id}/create", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createPosts(@RequestBody(required = false) List<PostModel> posts,
                                      @PathVariable("slug_or_id") String slug_or_id) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(posts, slug_or_id));
        } catch (DuplicateKeyException ex) {
           return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorModel(ex.getMessage()));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel(ex.getMessage()));
        }
    }

    @RequestMapping(path = "api/thread/{slug_or_id}/details", method = RequestMethod.GET)
    public ResponseEntity threadDetails(@PathVariable("slug_or_id") String slug_or_id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(threadService.getThreadBySlugOrID(slug_or_id));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel(ex.getMessage()));
        }
    }

    @RequestMapping(path = "api/thread/{slug_or_id}/details", method = RequestMethod.POST)
    public ResponseEntity threadUpdate(@RequestBody ThreadUpdateModel threadData, @PathVariable("slug_or_id") String slug_or_id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(threadService.updateThread(threadData, slug_or_id));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel(ex.getMessage()));
        }
    }

    @RequestMapping(path = "api/thread/{slug_or_id}/vote", method = RequestMethod.POST)
    public ResponseEntity updateVoice(@RequestBody VoteModel vote, @PathVariable("slug_or_id") String slug_or_id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(threadService.insertOrUpdateVotes(vote, slug_or_id));
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorModel(ex.getMessage()));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel(ex.getMessage()));
        }
    }

    @RequestMapping(path = "api/thread/{slug_or_id}/posts", method = RequestMethod.GET)
    public ResponseEntity getPosts(@PathVariable("slug_or_id") String slug_or_id,
                                   @RequestParam(value = "limit", required = false) Integer limit,
                                   @RequestParam(value = "since", required = false) Integer since,
                                   @RequestParam(value = "sort", required = false) String sort,
                                   @RequestParam(value = "desc", required = false) Boolean desc) {
        return null;
    }

}
