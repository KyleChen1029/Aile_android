package tw.com.chainsea.ce.sdk.bean.common;


import com.google.gson.annotations.SerializedName;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * date 2020-04-15
 */
public enum EnableType {
    @SerializedName(value = "Y", alternate = {"true"}) Y("Y", true),
    @SerializedName(value = "N", alternate = {"false"}) N("N", false);

    private String type;
    private boolean status;

    EnableType(String type, boolean status) {
        this.type = type;
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public static EnableType of(boolean status) {
        return status ? EnableType.Y : EnableType.N;
    }
}
