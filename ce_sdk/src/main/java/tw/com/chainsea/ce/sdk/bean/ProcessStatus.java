package tw.com.chainsea.ce.sdk.bean;

import com.google.gson.annotations.SerializedName;

/**
 * current by evan on 2020-07-15
 *
 * @author Evan Wang
 * date 2020-07-15
 */
public enum ProcessStatus {
    @SerializedName(value = "UN_SYNC_CREATE")
    UN_SYNC_CREATE("UN_SYNC_CREATE", "未同步新增"),
    @SerializedName(value = "UN_SYNC_UPDATE")
    UN_SYNC_UPDATE("UN_SYNC_UPDATE", "未同步更新"),
    @SerializedName(value = "UN_SYNC_DELETE")
    UN_SYNC_DELETE("UN_SYNC_DELETE", "未同步刪除"),
    @SerializedName(value = "UN_SYNC_COMPLETE")
    UN_SYNC_COMPLETE("UN_SYNC_COMPLETE", "未同步完成"),
    @SerializedName(value = "UNDEF")
    UNDEF("UNDEF", "無需處理");

    private String status;
    private String name;

    public static ProcessStatus parse(String value) {
        if (value != null && !value.isEmpty()) {
            return valueOf(value);
        }
        return UNDEF;
    }

    ProcessStatus(String status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }
}