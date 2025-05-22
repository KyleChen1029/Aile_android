package tw.com.chainsea.chat.view.service.bean;

import java.io.Serializable;

/**
 * current by evan on 2020-07-28
 *
 * @author Evan Wang
 * date 2020-07-28
 */
public class MoreSettingsBean implements Serializable {
    private static final long serialVersionUID = -863890431721693904L;

    String title;
    String content;
    MoreSettingsType type;

    public MoreSettingsBean(String title, MoreSettingsType type) {
        this.title = title;
        this.type = type;
    }

    public MoreSettingsBean(String title, String content, MoreSettingsType type) {
        this.title = title;
        this.content = content;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public MoreSettingsType getType() {
        return type;
    }
}
