package tw.com.chainsea.ce.sdk.bean.msg;

import com.google.gson.annotations.SerializedName;

/**
 * current by evan on 2019-11-28
 */
public enum TargetType {
    @SerializedName(value = "User", alternate = {"USER", "user"}) USER("User"),
    @SerializedName(value = "All", alternate = {"ALL", "all"}) ALL("All"),
    @SerializedName("") UNDEF("");

    private String type;

    TargetType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}