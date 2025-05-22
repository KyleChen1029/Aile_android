package tw.com.chainsea.ce.sdk.bean;

public class MsgStatusBean {
    private String messageId;
    private int sendNum;
    private long sendTime;

    public MsgStatusBean(String messageId, int sendNum, long sendTime) {
        this.messageId = messageId;
        this.sendNum = sendNum;
        this.sendTime = sendTime;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getSendNum() {
        return sendNum;
    }

    public void setSendNum(int sendNum) {
        this.sendNum = sendNum;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }
}
