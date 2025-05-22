package tw.com.chainsea.ce.sdk.bean;

public class SearchBean {
    private int id;
    private String content;
    private String time;

    public SearchBean(int id, String content, String time) {
        this.id = id;
        this.content = content;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
