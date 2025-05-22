package tw.com.chainsea.ce.sdk.bean.msg;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.Json;
import com.squareup.moshi.ToJson;

/**
 * current by evan on 2020-02-07
 */
public enum SourceType {
    @Json(name = "User") @SerializedName("User") USER("User"),
    @Json(name = "System") @SerializedName("System") SYSTEM("System"),//系統訊息，系統告知使用者的訊息
    @Json(name = "broadcast") @SerializedName("Broadcast") BROADCAST("Broadcast"),//廣播功能
    @Json(name = "Robot") @SerializedName("Robot") ROBOT("Robot"),
    @Json(name = "Login") @SerializedName("Login") LOGIN("Login"), //登入訊息，電腦版Client登入的通知
    @Json(name = "Satisfaction") @SerializedName("Satisfaction") SATISFACTION("Satisfaction"),//滿意度調查
    @Json(name = "Undef") @SerializedName("Undef") UNDEF("Undef");

    SourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    private String sourceType;

    /**
     * 給 moshi 用的 Adapter
     * 因為 moshi會拿 NAME 去做 parser 而這邊定義的都是大寫
     */
    public static class SourceTypeAdapter {
        @ToJson
        String toJson(SourceType type) {
            return type.name();
        }

        @FromJson
        SourceType fromJson(String type) {
            return SourceType.of(type);
        }
    }

    public static SourceType of(String sourceType) {
        if (Strings.isNullOrEmpty(sourceType)) {
            return UNDEF;
        }
        for (SourceType t : values()) {
            if (t.sourceType.equalsIgnoreCase(sourceType)) {
                return t;
            }
        }
        return UNDEF;
    }

    @Override
    @NonNull
    public String toString() {
        return name();
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
