package tw.com.chainsea.chat.service.fcm.model;

import androidx.annotation.NonNull;

public class Content {
    String roomId;
    Notification notification;
    int flag;
    String messageId;

    public String getRoomId() {
        return roomId;
    }

    public Notification getNotification() {
        return notification;
    }

    public int getFlag() {
        return flag;
    }

    public String getMessageId() {
        return messageId;
    }

    @Override
    @NonNull
    public String toString() {
        return "Content [roomId=" + roomId + ", notification=" + notification + ", flag=" + flag + ", messageId="
            + messageId + "]";
    }
}
