package tw.com.chainsea.ce.sdk.bean.account;

import com.google.gson.annotations.SerializedName;
/**
 * AccountType
 * Created by 90Chris on 2016/4/21.
 */
public enum AccountType {
    @SerializedName("100") UNDEF(100),
    @SerializedName("101") FRIEND(101),
    @SerializedName("102") SELF(102);

    private final int value;

    public static AccountType of(int type) {
        for (AccountType a : AccountType.values()) {
            if (type == a.value) {
                return a;
            }
        }
        return AccountType.UNDEF;

    }

    AccountType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
