package tw.com.chainsea.ce.sdk.bean.broadcast;

import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;

/**
 * current by evan on 2020-08-25
 *
 * @author Evan Wang
 * date 2020-08-25
 */
public enum BroadcastEvent {
    @SerializedName(value = "Delete", alternate = {"delete"}) DELETE("Delete"),
    @SerializedName(value = "Update", alternate = {"Update"}) UPDATE("Update"),
    @SerializedName(value = "AssignStart", alternate = {"assignStart"}) ASSIGN_START("AssignStart"),
    @SerializedName(value = "AssignComplete", alternate = {"assignComplete"}) ASSIGN_COMPLETE("AssignComplete"),
    @SerializedName("None") UNDEF("None");


    BroadcastEvent(String event) {
        this.event = event;
    }

    private String event;

    public void setEvent(String event) {
        this.event = event;
    }

    public static BroadcastEvent of(String event) {
        if (Strings.isNullOrEmpty(event)) {
            return UNDEF;
        }

        for (BroadcastEvent e : values()) {
            if (e.event.toUpperCase().equals(event.toUpperCase())) {
                return e;
            }
        }

        return UNDEF;
    }

}
