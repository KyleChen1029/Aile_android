package tw.com.chainsea.ce.sdk.bean.servicenumber.type;

import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

/**
 * current by evan on 12/9/20
 *
 * @author Evan Wang
 * date 12/9/20
 */
public enum ServiceNumberPrivilege {
    @SerializedName("Owner") OWNER("Owner", 0),
    @SerializedName("Manager") MANAGER("Manager", 1),
    @SerializedName("Common") COMMON("Common", 2),
    @SerializedName("") UNDEF("", 99);

    public static class ServiceNumberPrivilegeAdapter {
        @ToJson
        String toJson(ServiceNumberPrivilege type) {
            return type.name();
        }

        @FromJson
        ServiceNumberPrivilege fromJson(String type) {
            return ServiceNumberPrivilege.of(type);
        }
    }

    public static ServiceNumberPrivilege of(String type) {
        for (ServiceNumberPrivilege value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return UNDEF;
    }

    ServiceNumberPrivilege(String type, int index) {
        this.type = type;
        this.index = index;
    }

    private String type;
    private int index;

    public String getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }
}
