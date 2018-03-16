package dbproject.models;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class VoteModel {
    private String nickname;
    private Integer voice;

    public VoteModel(@JsonProperty("nickname") String nickname,
                     @JsonProperty("voice") Integer voice) {
        this.nickname = nickname;
        this.voice = voice;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getVoice() {
        return voice;
    }

    public void setVoice(Integer voice) {
        this.voice = voice;
    }

}
