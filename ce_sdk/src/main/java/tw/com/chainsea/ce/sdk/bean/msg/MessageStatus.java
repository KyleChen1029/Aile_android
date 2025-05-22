package tw.com.chainsea.ce.sdk.bean.msg;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

/**
 * status of message
 * Created by 90Chris on 2016/4/19.
 *
 * @version 1.9.1
 */
public enum MessageStatus {
    ERROR(-1),    // message caught an error
    SENDING(0),   // message is sending
    SUCCESS(1),   // message sent successfully, but not be read
    FAILED(2),    // message sent failed
    READ(3),      // message has been read by receiver
    RECEIVED(4),  // message is received successfully
    IS_REMOTE(5),  // api get Message
    UPDATE_ERROR(6);

    private int value;

    MessageStatus(int value) {
        this.value = value;
    }

    public static MessageStatus of(int value) {
        for (MessageStatus status : values()) {
            if (value == status.getValue()) {
                return status;
            }
        }
        return ERROR;
    }

    public final int getValue() {
        return this.value;
    }


    public static List<MessageStatus> getValids() {
        return Lists.newArrayList(SENDING, SUCCESS, READ, RECEIVED, IS_REMOTE);
    }


    public static Set<MessageStatus> FAILED_or_ERROR = Sets.newHashSet(ERROR, FAILED);

    public static  Integer[] getFailedErrorStatus(){
        return new Integer[]{ERROR.value, FAILED.value};
    }

    public static Integer[] getValidStatus() {
        return new Integer[]{SENDING.value, SUCCESS.value, READ.value, RECEIVED.value, IS_REMOTE.value};
    }

    public static Integer[] getLocalValidStatus() {
        return new Integer[]{SENDING.value, SUCCESS.value, READ.value, RECEIVED.value};
    }

    public static Integer[] getValidValues() {
        List<MessageStatus> valids = getValids();
        Integer[] values = new Integer[valids.size()];
        for (int i = 0; i < valids.size(); i++) {
            values[i] = valids.get(i).value;
        }
        return values;
    }


}
