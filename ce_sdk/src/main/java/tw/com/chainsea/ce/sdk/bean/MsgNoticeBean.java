package tw.com.chainsea.ce.sdk.bean;

public class MsgNoticeBean {
    private String messageId;
    private int receivedNum;
    private int readNum;
    private int sendNum;

    public MsgNoticeBean(String messageId, int receivedNum, int readNum, int sendNum) {
        this.messageId = messageId;
        this.receivedNum = receivedNum;
        this.readNum = readNum;
        this.sendNum = sendNum;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getReceivedNum() {
        return receivedNum;
    }

    public void setReceivedNum(int receivedNum) {
        this.receivedNum = receivedNum;
    }

    public int getReadNum() {
        return readNum;
    }

    public void setReadNum(int readNum) {
        this.readNum = readNum;
    }

    public int getSendNum() {
        return sendNum;
    }

    public void setSendNum(int sendNum) {
        this.sendNum = sendNum;
    }
}
