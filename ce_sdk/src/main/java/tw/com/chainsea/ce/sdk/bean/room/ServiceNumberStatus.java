package tw.com.chainsea.ce.sdk.bean.room;

import androidx.annotation.StringRes;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.Set;
/**
 * current by evan on 12/17/20
 *
 * @author Evan Wang
 * date 12/17/20
 */
public enum ServiceNumberStatus {
    @SerializedName("online") ON_LINE("online", 0),
    @SerializedName("offline") OFF_LINE("offline", 0),
    @SerializedName("timeout") TIME_OUT("timeout", 0),
    @SerializedName("undef") UNDEF("undef", 0),
    @SerializedName("robotStop") ROBOT_STOP("robotStop", 0),
    @SerializedName("robotService") ROBOT_SERVICE("robotService", 0);

    private final String status;

    @StringRes
    private final int statusResId;

    /**
     * 給 moshi 用的 Adapter
     * 因為 moshi會拿 NAME 去做 parser 而這邊定義的都是大寫
     * */
    public static class ServiceNumberStatusTypeAdapter {
        @ToJson
        String toJson(ServiceNumberStatus type) {
            return type.status;
        }

        @FromJson
        ServiceNumberStatus fromJson(String type) {
            return ServiceNumberStatus.of(type);
        }
    }


    public static ServiceNumberStatus of(String code) {
        if (Strings.isNullOrEmpty(code)) {
            return UNDEF;
        }
        for (ServiceNumberStatus s : ServiceNumberStatus.values()) {
            if (s.status.equals(code)) {
                return s;
            }
        }
        return UNDEF;
    }

    public static Set<ServiceNumberStatus> ON_LINE_or_TIME_OUT = Sets.newHashSet(ON_LINE, TIME_OUT);
    public static Set<ServiceNumberStatus> OFF_LINE_or_TIME_OUT = Sets.newHashSet(OFF_LINE, TIME_OUT);

    ServiceNumberStatus(String status, int statusResId) {
        this.status = status;
        this.statusResId = statusResId;
    }

    public String getStatus() {
        return status;
    }

    public int getStatusResId() {
        return statusResId;
    }
}
