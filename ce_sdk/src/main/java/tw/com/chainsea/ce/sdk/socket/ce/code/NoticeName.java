package tw.com.chainsea.ce.sdk.socket.ce.code;

import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;

/**
 * current by evan on 2020-10-23
 *
 * @author Evan Wang
 * date 2020-10-23
 */
public enum NoticeName {
    @SerializedName("Chat.Notice") NOTICE("Chat.Notice"),
    @SerializedName("Chat.Message.Confirm") CONFIRM("Chat.Message.Confirm"),
    @SerializedName("Chat.Message.New") MESSAGE_NEW("Chat.Message.New"),
    @SerializedName("Call.Event") CALL_EVENT("Call.Event"),
    @SerializedName("Socket.Error") SOCKET_ERROR("Socket.Error"),
    @SerializedName("Chat.Message.Offline") MESSAGE_OFFLINE("Chat.Message.Offline"),
    @SerializedName("UNDEF") UNDEF("UNDEF");

    private final String name;

    public static NoticeName of(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return UNDEF;
        }
        for (NoticeName n : NoticeName.values()) {
            if (n.name.equals(name)) {
                return n;
            }
        }
        return UNDEF;
    }

    NoticeName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
