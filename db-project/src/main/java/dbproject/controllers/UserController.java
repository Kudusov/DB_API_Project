package dbproject.controllers;

import dbproject.models.UserModel;
import dbproject.services.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
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

//        if (userData.getNickname() == null){
//            userData.setNickname(nickname);
//        }

//        final UserModel user = new UserModel();
//        final UserService.ErrorCodes err = users.signup(userData, user);
//        switch (err) {
//            case EMAIL_OCCUPIED:
//                return ResponseEntity.status(HttpStatus.CONFLICT).body(user);
//            case OK:
//                return ResponseEntity.status(HttpStatus.CREATED).body(user);
//            case INVALID_AUTH_DATA:
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("error json");
//            default:
//                break;
//        }
//        return ResponseEntity.status(HttpStatus.CREATED).body(userData);
    }
}
