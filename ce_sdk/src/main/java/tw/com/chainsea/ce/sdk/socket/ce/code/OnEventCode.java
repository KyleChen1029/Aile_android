package tw.com.chainsea.ce.sdk.socket.ce.code;

import com.google.gson.annotations.SerializedName;

/**
 * current by evan on 2020-09-29
 *
 * @author Evan Wang
 * date 2020-09-29
 */
public enum OnEventCode {
    @SerializedName("connection") CONNECTION("connection"),
    @SerializedName("connect") CONNECT("connect"),
    @SerializedName("message") MESSAGE("message"),
    @SerializedName("ping") PING("ping"),
    @SerializedName("pong") PONG("pong"),
    @SerializedName("noop") NOOP("noop"),
    @SerializedName("close") CLOSE("close"),
    @SerializedName("disconnect") DISCONNECT("disconnect");

    private String code;

    OnEventCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
