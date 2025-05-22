package tw.com.chainsea.ce.sdk.bean.business;

import androidx.annotation.DrawableRes;

import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;

import tw.com.chainsea.ce.sdk.R;

/**
 * current by evan on 2020-06-02
 *
 * @author Evan Wang
 * date 2020-06-02
 */

public enum BusinessCode {
    @SerializedName("Ecp.Task") TASK("Ecp.Task", "任務", R.drawable.icon_task_35dp, "任務(%s)"),
    @SerializedName("Ecp.Opportunity") OPPORTUNITY("Ecp.Opportunity", "商機", R.drawable.icon_task_opportunity_35dp, "商機(%s)"),
    @SerializedName("Ecp.ServiceRequest") SERVICE_REQUEST("Ecp.ServiceRequest", "服務請求", R.drawable.icon_task_service_request_35dp, "服務(%s)"),
    @SerializedName("UNDEF") UNDEF("UNDEF", "未知", R.drawable.icon_task_service_request_35dp, "未知(%s)");

    private String code;

    private String name;

    @DrawableRes
    private int iconId;

    private String simpleName;

    BusinessCode(String code, String name, int iconId, String simpleName) {
        this.code = code;
        this.name = name;
        this.iconId = iconId;
        this.simpleName = simpleName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public static BusinessCode of(String code) {
        if (Strings.isNullOrEmpty(code)) {
            return TASK;
        }
        for (BusinessCode bc : values()) {
            if (bc.getCode().equals(code)) {
                return bc;
            }
        }
        return TASK;
    }
}