package tw.com.chainsea.chat.service.fcm.model;

import androidx.annotation.NonNull;

public class Notification {
    String body;
    String title;
    String code;
    int flag;
    String messageId;
    String room;

    @Override
    @NonNull
    public String toString() {
        return "Notification [body=" + body + ", title=" + title + ", code=" + code + ", flag=" + flag
            + ", messageId=" + messageId + ", room=" + room + "]";
    }
}
