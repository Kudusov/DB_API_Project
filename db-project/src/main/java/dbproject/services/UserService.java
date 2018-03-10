package dbproject.services;

import dbproject.models.UserModel;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

@SuppressWarnings("unused")
@Service
public class UserService {
    private ArrayList<UserModel> users = new ArrayList<>();

    public enum ErrorCodes {
        @SuppressWarnings("EnumeratedConstantNamingConvention") OK,
        INVALID_LOGIN,
        INCORRECT_PASSWORD,
        LOGIN_OCCUPIED,
        EMAIL_OCCUPIED,
        INVALID_AUTH_DATA,
        INVALID_REG_DATA,
    }

    private UserModel getUser(@NotNull UserModel userData) {
        for (UserModel user: users) {
            if (user.getNickname().equals(userData.getNickname()) || user.getEmail().equals(userData.getEmail())) {
                return user;
            }
        }
        return null;
    }

    private UserModel getUserByEmail(@NotNull String email) {
        for (UserModel user: users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private UserModel getUserByNickname(@NotNull String nickname) {
        for (UserModel user: users) {
            if (user.getNickname().equals(nickname)) {
                return user;
            }
        }
        return null;
    }


    public ErrorCodes signup(UserModel newUser, UserModel result) {
        if (newUser == null || newUser.getEmail() == null || newUser.getNickname() == null) {
            return ErrorCodes.INVALID_AUTH_DATA;
        }

        if (newUser.getEmail() != null && newUser.getNickname() != null && getUser(newUser) == null) {
            users.add(newUser);
            result.setAbout(newUser.getAbout());
            result.setNickname(newUser.getNickname());
            result.setEmail(newUser.getEmail());
            result.setFullname(newUser.getFullname());
            return ErrorCodes.OK;
        } else {
            final UserModel user = getUser(newUser);
            assert user != null;
            result.setAbout(user.getAbout());
            result.setNickname(user.getNickname());
            result.setEmail(user.getEmail());
            result.setFullname(user.getFullname());
            return ErrorCodes.EMAIL_OCCUPIED;
        }

    }
}
