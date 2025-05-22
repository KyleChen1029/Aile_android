package tw.com.chainsea.ce.sdk.socket.ce.bean;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeName;

/**
 * current by evan on 2020-10-23
 * @author Evan Wang
 * date 2020-10-23
 */
public class ReceiveMessageBean {
    @SerializedName("name")
    private final NoticeName name;
    private final String data;

    public ReceiveMessageBean(NoticeName name, String data) {
        this.name = name;
        this.data = data;
    }

    public NoticeName getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public static ReceiveMessageBean socketSpecialFrom(JSONObject json) throws JSONException {
        if (JsonHelper.getInstance().has(json, "name", "data")) {
            String name = json.getString("name");
            String data = json.getString("data");
            return new ReceiveMessageBean(NoticeName.of(name), data);
        } else {
            throw  new JSONException("From Error");
        }
    }
}
