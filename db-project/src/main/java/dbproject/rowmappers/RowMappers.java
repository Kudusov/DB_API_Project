package dbproject.rowmappers;

import dbproject.models.ForumModel;
import dbproject.models.UserModel;

import org.springframework.jdbc.core.RowMapper;

public class RowMappers {
    public static RowMapper<UserModel> readUser = (rs, i) ->
            new UserModel(rs.getString("about"),
                          rs.getString("fullname"),
                    rs.getString("nickname"),
                    rs.getString("email"));

    public static RowMapper<ForumModel> readForum = (rs, i) ->
            new ForumModel(rs.getInt("posts"),
                           rs.getInt("threads"),
                           rs.getString("slug"),
                           rs.getString("title"),
                           rs.getString("user"));

}
