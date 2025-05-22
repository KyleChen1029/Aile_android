package tw.com.chainsea.ce.sdk.bean.account;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

/**
 * Created by 90Chris on 2016/6/20.
 */
public enum Gender {
    UNDEF("UNDEF"),
    MALE("M"),
    FEMALE("F");


    public static class GenderAdapter {
        @ToJson
        String toJson(Gender type) {
            return type.name();
        }

        @FromJson
        Gender fromJson(String type) {
            return Gender.ofValue(type);
        }
    }

    private String mValue;

    Gender(String value) {
        mValue = value;
    }

    public final String getValue() {
        return mValue;
    }

    public static Gender ofValue(String value) {
        for (Gender item : values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return UNDEF;
    }
}
