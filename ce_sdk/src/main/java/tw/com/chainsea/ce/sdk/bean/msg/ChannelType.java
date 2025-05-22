package tw.com.chainsea.ce.sdk.bean.msg;

import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

/**
 * ReceiveMsgType
 * Created by 90Chris on 2014/11/10.
 */
public enum ChannelType {
    @SerializedName(value = "facebook", alternate = {"Facebook"}) FB("facebook", 0),
    @SerializedName(value = "ce", alternate = {"CE"}) CE("ce", 1),
    @SerializedName(value = "wechat", alternate = {"WeChat"}) WEICHAT("wechat", 2),
    @SerializedName(value = "line", alternate = {"Line", "LINE"}) LINE("line", 3),
    @SerializedName("qbi") QBI("qbi", 4),
    @SerializedName(value = "web", alternate = {"ailewebchat", "AileWebChat"}) AILE_WEB_CHAT("web",5),
    @SerializedName(value = "google", alternate = {"Google", "GOOGLE"}) GOOGLE("google", 6),
    @SerializedName(value = "instagram", alternate = {"Instagram", "ig", "IG"}) IG("instagram", 7),
    @SerializedName("None") UNDEF("None", 8),
    @SerializedName("aiwow") AIWOW("aiwow", 9);
    private String type;
    private int index;

    public static class ChannelTypeAdapter  {
        @ToJson
        String toJson(ChannelType type) {
            return type.type;
        }

        @FromJson
        ChannelType fromJson(String type) {
            return ChannelType.of(type);
        }
    }

    ChannelType(String type, int index) {
        this.type = type;
        this.index = index;
    }

    public String getValue() {
        return this.type;
    }

    public int getIndex() {
        return this.index;
    }

    /**
     * get client enum code from server string code
     *
     * @param severCode server string code
     * @return client enum code
     */
    public static ChannelType of(String severCode) {
        for (ChannelType item : values()) {
            if (item.getValue().equals(severCode)) {
                return item;
            }
        }
        return UNDEF;
    }
}
