package tw.com.chainsea.chat.widget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sunhui on 2018/6/26.
 */

public class CallData {
    /*type*/
    public static int SINGLE = 1;
    public static int GROUP = 2;
    public static int EXTENSION = 3;
    public static int SERVICE = 4;
    public static int TRANSFER_CALL = 5;
    /*status*/
    public static int COMMING = 101;
    public static int CALLING = 102;
    public static int CONNECTED = 103;
    public static int JOINING = 104;
    public static int TRANSFER = 105;
    public static int WAIT = 106;
    private int type;
    private int status;

    private boolean isSpeaker;
    private boolean isMute;

    private String mCallerId;
    private String mRoomId;
    private String mMeetingId;
    private String mCallKey;
    private String mCallerType;
    private int memberSize;
    private List<String> joinIds = new ArrayList<>();
    private long millis;
    private String osType;
    private boolean isTranfer;
    private String deviceType;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSpeaker() {
        return isSpeaker;
    }

    public void setSpeaker(boolean speaker) {
        isSpeaker = speaker;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public String getCallerId() {
        return mCallerId;
    }

    public void setCallerId(String callerId) {
        mCallerId = callerId;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void setRoomId(String roomId) {
        mRoomId = roomId;
    }

    public String getMeetingId() {
        return mMeetingId;
    }

    public void setMeetingId(String meetingId) {
        mMeetingId = meetingId;
    }

    public int getMemberSize() {
        return memberSize;
    }

    public void setMemberSize(int memberSize) {
        this.memberSize = memberSize;
    }

    public void addJoinId(String userId) {
        joinIds.add(userId);
    }
//
//    public void addJoinIds(List<String> joinIds) {
//        joinIds.addAll(joinIds);
//    }

    public void setJoinIds(List<String> joinIds) {
        this.joinIds = joinIds;
    }

    public void removeJoinId(String userId) {
        joinIds.remove(userId);
    }

    public int getJoinNum() {
        return joinIds.size();
    }

    public List<String> getJoinIds() {
        return joinIds;
    }

    public long getMillis() {
        return millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public String getCallKey() {
        return mCallKey;
    }

    public void setCallKey(String mCallKey) {
        this.mCallKey = mCallKey;
    }

    public String getCallerType() {
        return mCallerType;
    }

    public void setCallerType(String mCallerType) {
        this.mCallerType = mCallerType;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public boolean isTranfer() {
        return isTranfer;
    }

    public void setTranfer(boolean tranfer) {
        isTranfer = tranfer;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void clearDeviceType() {
        this.deviceType = null;
    }

    public void clear() {
        type = 0;
        status = 0;
        isSpeaker = false;
        isMute = false;
        mCallerId = null;
        mRoomId = null;
        mMeetingId = null;
        millis = 0;
        joinIds.clear();
    }
}
