package dbproject.services;

import dbproject.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import static dbproject.rowmappers.RowMappers.readUser;

@SuppressWarnings("unused")
@Service
public class UserService {

    private JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(String nickname, UserModel user) {
        final String createQuery = "INSERT INTO Users (about, fullname, nickname, email) VALUES(?, ?, ?, ?)";
        jdbcTemplate.update(createQuery, user.getAbout(), user.getFullname(), user.getNickname(), user.getEmail());
    }

    public List<UserModel> getUsers(UserModel user) {
        final String getUsers = "SELECT * FROM USERS WHERE email = ? OR nickname = ?";
        return jdbcTemplate.query(getUsers, readUser, user.getEmail(), user.getNickname());
    }

    public UserModel getUserByNickname(String nickname) {
        final String getUser = "SELECT * FROM USERS WHERE nickname = ?";
        return jdbcTemplate.queryForObject(getUser, readUser, nickname);
    }

    public Integer getUserIdByNickname(String nickname) {
        final String getUser = "SELECT id FROM USERS WHERE nickname = ?";
        return jdbcTemplate.queryForObject(getUser, Integer.class, nickname);
    }

    public void update(UserModel user) {

        if (user == null) {
            return;
        }

        final StringBuilder getUser = new StringBuilder("UPDATE Users SET");
        final ArrayList<Object> params = new ArrayList<>();

        if (user.getEmail() != null) {
            getUser.append(" email = ?,");
            params.add(user.getEmail());
        }

        if (user.getAbout() != null) {
            getUser.append(" about = ?,");
            params.add(user.getAbout());
        }

        if (user.getFullname() != null) {
            getUser.append(" fullname = ?,");
            params.add(user.getFullname());
        }

        if (params.isEmpty())
            return;

        getUser.deleteCharAt(getUser.length() - 1);
        getUser.append(" WHERE nickname = ?");
        params.add(user.getNickname());
        jdbcTemplate.update(getUser.toString(), params.toArray());

    }

    public Integer getRowCount() {
        final String sqlQuery = "SELECT COUNT(*) FROM Users";
        return jdbcTemplate.queryForObject(sqlQuery, Integer.class);
    }

    public void clear() {
        final String sqlQuery = "DELETE FROM Users";
        jdbcTemplate.execute(sqlQuery);
    }
}
