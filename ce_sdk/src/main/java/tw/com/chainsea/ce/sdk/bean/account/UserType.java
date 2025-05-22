package tw.com.chainsea.ce.sdk.bean.account;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.Set;

/**
 * current by evan on 2020-08-28
 *
 * @author Evan Wang
 * date 2020-08-28
 */
public enum UserType {
    @SerializedName("employee") EMPLOYEE("employee"),
    @SerializedName("employee") employee("employee"),
    @SerializedName("contact") CONTACT("contact"),
    @SerializedName("visitor") VISITOR("visitor"),
    @SerializedName("undef") UNDEF("undef"),
    @SerializedName("isAdd") IS_ADD("isAdd");
    private String userType;

    public static class UserTypeAdapter {
        @ToJson
        String toJson(UserType type) {
            return type.name();
        }

        @FromJson
        UserType fromJson(String type) {
            return UserType.of(type);
        }
    }

    UserType(String userType) {
        this.userType = userType;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public static UserType of(String type) {
        if (Strings.isNullOrEmpty(type)) {
            return UNDEF;
        }

        for (UserType t : EMPLOYEE_or_CONTACT_or_VISITOR) {
            if (t.userType.toUpperCase().equals(type.toUpperCase())) {
                return t;
            }
        }
        return UNDEF;
    }

    public static Set<UserType> EMPLOYEE_or_CONTACT_or_VISITOR = Sets.newHashSet(EMPLOYEE, CONTACT, VISITOR);

    @Override
    @NonNull
    public String toString() {
        return name();
    }
}
