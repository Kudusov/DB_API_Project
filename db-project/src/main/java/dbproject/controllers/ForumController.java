package dbproject.controllers;

import dbproject.models.ErrorModel;
import dbproject.models.ForumModel;
import dbproject.models.ThreadModel;
import dbproject.services.ForumService;
import dbproject.services.ThreadService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
public class ForumController {
    @NotNull
    private ForumService forums;
    private ThreadService threads;
    public  ForumController(@NotNull ForumService forumService, @NotNull ThreadService threads) {
        this.forums = forumService;
        this.threads = threads;
    }

    @RequestMapping(path = "/api/forum/create", method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody ForumModel forumData) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(forums.create(forumData));
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forums.getBySlug(forumData.getSlug()));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel(ex.getMessage()));
        }
    }

    @RequestMapping(path = "/api/forum/{slug}/details", method = RequestMethod.GET)
    public ResponseEntity forumInfo(@PathVariable("slug") String slug) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(forums.getBySlug(slug));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel(ex.getMessage()));
        }
    }

    @RequestMapping(path = "api/forum/{slug}/create", method = RequestMethod.POST)
    public ResponseEntity createThread(@RequestBody ThreadModel threadData, @PathVariable("slug") String slug){
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(threads.create(threadData, slug));
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
