package dbproject.rowmappers;

import dbproject.models.ForumModel;
import dbproject.models.ThreadModel;
import dbproject.models.UserModel;

import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


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
                           rs.getString("nickname"));

    public static RowMapper<ThreadModel> readThread = (rs, i) -> {
        final Timestamp timestamp = rs.getTimestamp("created");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return new ThreadModel(rs.getString("author"),
                                dateFormat.format(timestamp.getTime()),
                                rs.getString("forum"),
                                rs.getInt("id"),
                                rs.getString("message"),
                                rs.getString("slug"),
                                rs.getString("title"),
                                rs.getInt("votes"));
    };
}
