package tw.com.chainsea.ce.sdk.http.ce.model;

import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.AD;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.AT;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.BUSINESS;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.FILE;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.IMAGE;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.LOCATION;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.STICKER;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.TEXT;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.Template;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.VIDEO;
import static tw.com.chainsea.ce.sdk.http.ce.model.LastMessageType.ContentType.VOICE;

import tw.com.chainsea.ce.sdk.service.ChatRoomService;

public class LastMessage {
    private String sourceType; //該訊息的狀態，分為：user | system|Login
    private String msgSrc; //如果為forward訊息會塞入前一筆的來源id
    private long sendTime; //訊息發送時間
    private int readedNum; //訊息已讀取數量
    private int sendNum; //訊息發送對象總數
    private String roomId; //聊天室 Id
    private String senderId; //訊息發送者 Id
    private String from; //訊息來源，分為：line | facebook | ce (外部服務號進線會填入)
    private String type = ""; //訊息類型
    private String content; //訊息內容
    private String id; //訊息 Id
    private String senderName = ""; //string	訊息發送者的名稱
    private int flag; //訊息狀態，分為：-1（當前登入者發的）| 0 | 1（已到）| 2（已讀）| 3（收回）
    private int receivedNum; //訊息已到達對方載具總數

    private String tag;
    private String chatId;
    private int sequence;

    public int getSequence() {
        return sequence;
    }

    public String getChatId() {
        return chatId;
    }

    public String getTag() {
        return tag;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setSourceType(String type){
        this.sourceType = type;
    }
    public void setId(String id){
        this.id = id;
    }

    public void setMsgSrc(String msgSrc) {
        this.msgSrc = msgSrc;
    }

    public void setType(String type) {
        this.type = type;
    }
    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setReadedNum(int readedNum) {
        this.readedNum = readedNum;
    }

    public void setReceivedNum(int receivedNum) {
        this.receivedNum = receivedNum;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setSendNum(int sendNum) {
        this.sendNum = sendNum;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getMsgSrc() {
        return msgSrc;
    }

    public long getSendTime() {
        return sendTime;
    }

    public int getReadedNum() {
        return readedNum;
    }

    public int getSendNum() {
        return sendNum;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getFrom() {
        return from;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }

    public String getSenderName() {
        return senderName;
    }

    public int getFlag() {
        return flag;
    }

    public int getReceivedNum() {
        return receivedNum;
    }

    public CharSequence getFormatContent(){
        if(type == null) return "";
        switch (type){
            case AT: return ChatRoomService.getAtContent(content);
            case VIDEO: return "[影片]";
            case LOCATION: return "[位置]";
            case BUSINESS: return "[物件]";
            case FILE: return "[文件]";
            case VOICE: return "[語音]";
            case IMAGE: return "[圖片]";
            case AD: return "[廣告]";
            case STICKER: return "[表情]";
            case TEXT: return content;
            case Template: return "[卡片訊息]";
            default:
                return "[未知訊息格式]";
        }
    }
}
