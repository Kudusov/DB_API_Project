package dbproject.models;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class StatusModel {
    Integer user;
    Integer forum;
    Integer thread;
    Integer post;

    public StatusModel(@JsonProperty("user") Integer user,
                       @JsonProperty("forum") Integer forum,
                       @JsonProperty("thread") Integer thread,
                       @JsonProperty("post") Integer post) {
        this.user = user;
        this.forum = forum;
        this.thread = thread;
        this.post = post;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Integer getForum() {
        return forum;
    }

    public void setForum(Integer forum) {
        this.forum = forum;
    }

    public Integer getThread() {
        return thread;
    }

    public void setThread(Integer thread) {
        this.thread = thread;
    }

    public Integer getPost() {
        return post;
    }

    public void setPost(Integer post) {
        this.post = post;
    }
}
