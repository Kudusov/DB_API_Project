package dbproject.controllers;

import dbproject.models.ErrorModel;
import dbproject.models.ForumModel;
import dbproject.services.ForumService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
public class ForumController {
    @NotNull
    private ForumService forums;

    public  ForumController(@NotNull ForumService forumService) {
        this.forums = forumService;
    }

    @RequestMapping(path = "/api/forum/create", method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody ForumModel forumData) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(forums.create(forumData));
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorModel(ex.getMessage()));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel(ex.getMessage()));
        }
    }
}
