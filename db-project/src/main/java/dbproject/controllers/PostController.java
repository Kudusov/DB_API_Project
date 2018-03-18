package dbproject.controllers;

import dbproject.models.PostUpdateModel;
import dbproject.services.PostService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PostController {
    PostService postService;

    PostController(PostService postService) {
        this.postService = postService;
    }

    @RequestMapping(path = "api/post/{id}/details", method = RequestMethod.GET)
    public ResponseEntity postInfo(@RequestParam(value = "related", required = false) List<String> related,
                                   @PathVariable("id") Integer id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(postService.getInfo(id, related));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error(ex.getMessage()));
        }
    }

    @RequestMapping(path = "api/post/{id}/details", method = RequestMethod.POST)
    public ResponseEntity postUpdate(@RequestBody PostUpdateModel post,
                                   @PathVariable("id") Integer id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(postService.updatePost(id, post));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Error(ex.getMessage()));
        }
    }
}
