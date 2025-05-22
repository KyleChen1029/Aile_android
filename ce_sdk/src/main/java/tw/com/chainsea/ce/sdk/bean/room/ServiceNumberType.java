package tw.com.chainsea.ce.sdk.bean.room;

import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

/**
 * current by evan on 2020-03-10
 */
public enum ServiceNumberType {
    @SerializedName("Boss") BOSS("Boss"), // 商業服務號
    @SerializedName("Professional") PROFESSIONAL("Professional"), // 專業服務號
    @SerializedName("Normal") NORMAL("Normal"), // 一般服務號
    @SerializedName("Normal") OFFICIAL("Official"), // 官方服務號
    @SerializedName("Normal") MANAGER("Manage"), // 管理服務號
    @SerializedName("None") NONE("None");

    /**
     * 給 moshi 用的 Adapter
     * 因為 moshi會拿 NAME 去做 parser 而這邊定義的都是大寫
     * */
    public static class ServiceNumberTypeAdapter {
        @ToJson
        String toJson(ServiceNumberType type) {
            return type.name();
        }

        @FromJson
        ServiceNumberType fromJson(String type) {
            return ServiceNumberType.of(type);
        }
    }

    private String type;

    public static ServiceNumberType of(String serviceNumberType) {
        for (ServiceNumberType type : ServiceNumberType.values()) {
            if (type.type.equals(serviceNumberType) || type.name().equals(serviceNumberType)) {
                return type;
            }
        }
        return NONE;
    }

    ServiceNumberType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}