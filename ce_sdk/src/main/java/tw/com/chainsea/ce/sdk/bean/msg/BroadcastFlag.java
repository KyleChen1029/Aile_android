package tw.com.chainsea.ce.sdk.bean.msg;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import java.util.Set;
/**
 * current by evan on 2020-08-20
 *
 * @author Evan Wang
 * date 2020-08-20
 * <p>
 *  0 == Indicates that it has not been assigned (in reservation),
 *  1 == Indicates that it is being dispatched,
 *  2 == Indicates that the assignment is complete,
 *  -1 == been deleted,
 *  -2 == Local judgment unknown
 * </p>
 */
public enum BroadcastFlag {
    @SerializedName("0") BOOKING(0),
    @SerializedName("1") DISPATCHING(1),
    @SerializedName("2") DOME(2),
    @SerializedName("-1") DELETED(-1),
    @SerializedName("-2") UNDEF(-2);

    private int flag;

    public static BroadcastFlag of(int flag) {
        for (BroadcastFlag bf : values()) {
            if (bf.flag == flag) {
                return bf;
            }
        }
        return BroadcastFlag.UNDEF;
    }
    public static Set<BroadcastFlag> BOOKING_of_DISPATCHING = Sets.newHashSet(BOOKING, DISPATCHING);

    BroadcastFlag(int flag) {
        this.flag = flag;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
