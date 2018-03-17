package dbproject.models;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class ThreadUpdateModel {
    private String message;
    private String title;

    public ThreadUpdateModel(@JsonProperty("message") String message,
                             @JsonProperty("title") String title) {
        this.message = message;
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
