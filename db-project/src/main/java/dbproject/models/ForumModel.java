package dbproject.models;


import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class ForumModel {
    private Integer posts;
    private Integer threads;
    private String slug;
    private String title;
    private String user;

    public ForumModel(@JsonProperty("posts") Integer posts,
               @JsonProperty("threads") Integer threads,
               @JsonProperty("slug") String slug,
               @JsonProperty("title") String title,
               @JsonProperty("user") String user) {
        this.posts = posts;
        this.threads = threads;
        this.slug = slug;
        this.title = title;
        this.user = user;
    }

    public Integer getPosts() {
        return posts;
    }

    public void setPosts(Integer posts) {
        this.posts = posts;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

}
