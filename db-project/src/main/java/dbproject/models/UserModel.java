package dbproject.models;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class UserModel {
    private String about;
    private String email;
    private String fullname;
    private String nickname;

    public UserModel() {

    }

    public UserModel(@JsonProperty("about") String about,
                     @JsonProperty("fullname") String fullname,
                     @JsonProperty("nickname") String nickname,
                     @JsonProperty("email") String email) {
        this.about = about;
        this.email = email;
        this.fullname = fullname;
        this.nickname = nickname;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
