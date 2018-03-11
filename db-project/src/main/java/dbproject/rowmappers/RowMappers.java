package dbproject.rowmappers;

import dbproject.models.UserModel;

import org.springframework.jdbc.core.RowMapper;

public class RowMappers {
    public static RowMapper<UserModel> readUser = (rs, i) ->
            new UserModel(rs.getString("about"),
                          rs.getString("fullname"),
                    rs.getString("nickname"),
                    rs.getString("email"));
}
