package tw.com.chainsea.ce.sdk.socket.ce.bean;

import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeCode;

/**
 * current by evan on 2020-10-28
 *
 * @author Evan Wang
 * date 2020-10-28
 */
public class NoticeBean {
    @SerializedName("code")
    private NoticeCode code = NoticeCode.UNDEF;
    @SerializedName("content")
    private JSONObject content;

    public NoticeCode getCode() {
        return code;
    }

    public JSONObject getContent() {
        return content;
    }
}
