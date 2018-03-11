package dbproject.controllers;

import dbproject.models.ErrorModel;
import dbproject.models.UserModel;
import dbproject.services.UserService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

@RestController
public class UserController {
    @NotNull
    private UserService users;

    public UserController(@NotNull UserService userService) {
        this.users = userService;
    }

    @RequestMapping(path = "/api/user/{nickname}/create", method = RequestMethod.POST)
    public ResponseEntity create(@RequestBody UserModel userData, @PathVariable("nickname") String nickname) {
        userData.setNickname(nickname);
        try {
            users.create(nickname, userData);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(users.getUsers(userData));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(userData);

    }

    @RequestMapping(path = "/api/user/{nickname}/profile", method = RequestMethod.GET)
    public ResponseEntity info(@PathVariable("nickname") String nickname) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(users.getUserByNickname(nickname));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel(ex.getMessage()));
        }
    }

    @RequestMapping(path = "/api/user/{nickname}/profile", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> update(@RequestBody(required = false) UserModel user,  @PathVariable("nickname") String nickname) {
        if (user == null) {
            user = new UserModel();
        }

        user.setNickname(nickname);
        try {
            users.update(user);
            return ResponseEntity.status(HttpStatus.OK).body(users.getUserByNickname(nickname));
        } catch (DuplicateKeyException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorModel(ex.getMessage()));
        } catch (DataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorModel(ex.getMessage()));
        }
    }
}
