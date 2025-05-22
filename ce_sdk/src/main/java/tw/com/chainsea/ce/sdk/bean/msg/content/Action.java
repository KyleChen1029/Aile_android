package tw.com.chainsea.ce.sdk.bean.msg.content;

import java.io.Serializable;

public class Action implements Serializable {
    private String label;
    private String type;
    private String url;
    private String data;
    private String text;
    private String direction;
    private String id;

    private String link;

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getData() {
        return data;
    }

    public String getText() {
        return text;
    }

    public String getDirection() {
        return direction;
    }

    public String getId() {
        return id;
    }

    public String getLink() { return link;}
}
