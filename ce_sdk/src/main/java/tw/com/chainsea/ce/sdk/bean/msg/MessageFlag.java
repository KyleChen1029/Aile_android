package tw.com.chainsea.ce.sdk.bean.msg;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.Set;

/**
 * current by evan on 2020-02-04
 * <p>
 * 0 == send
 * 1 == arrived
 * 2 == read
 * 3 == retract
 * -1 == self send
 * -99 == unknown
 * <p/>
 */
public enum MessageFlag {
    @SerializedName("-2") DELETED(-2),
    @SerializedName("-1") OWNER(-1),
    @SerializedName("0") SEND(0),
    @SerializedName("1") ARRIVED(1),
    @SerializedName("2") READ(2),
    @SerializedName("3") RETRACT(3),
    @SerializedName("-99") UNDEF(-99),
    @SerializedName("99") BECOME_OWNER(99);

    public static class MessageFlagAdapter {
        @ToJson
        int toJson(MessageFlag type) {
            return type.flag;
        }

        @FromJson
        MessageFlag fromJson(int type) {
            return MessageFlag.of(type);
        }
    }

    private int flag;

    public static MessageFlag of(int flag) {
        for (MessageFlag e : values()) {
            if (e.flag == flag) {
                return e;
            }
        }
        return MessageFlag.UNDEF;
    }

    public static Set<MessageFlag> OWNER_or_SEND_or_ARRIVED = Sets.newHashSet(OWNER, SEND, ARRIVED);

    MessageFlag(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
