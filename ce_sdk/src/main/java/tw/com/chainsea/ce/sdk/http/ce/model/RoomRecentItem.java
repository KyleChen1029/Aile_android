package tw.com.chainsea.ce.sdk.http.ce.model;

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.InputLogBean;

/**
 * Ian 2023.05.08 備註：增加 roomMemberIdentity 並且與"機器人服務中"聊天室共用
 *
 */
public class RoomRecentItem implements Serializable, Comparable<RoomRecentItem>{
    private String avatarId; //聊天室的頭像 Id
    private String serviceNumberId; //若聊天室為服務號成员聊天室，則會給服務號 Id
    private String ownerId; //聊天室擁有者 Id
    private String ownerUserType; //*本地用, 只有商務號在一般列表時會需要用到, 判斷使用者身分 後端不存在 by 20230720
    private String serviceNumberOwnerId; //若聊天室為服務號成员聊天室，則會給服務號擁有者 Id
    private String type; //聊天室類型，分為：friend | discuss | group | serviceMember
    private boolean transferFlag; //是否有轉接服務(有人服務而且按了換手)
    private String transferReason; //轉接理由 transferFlag為true時提供
    private int unReadNum; //聊天室未讀數量
    public int roomMemberIdentity;
    private long firstunReadMessageTime; //該聊天室第一句未讀訊息的時間
    private String id; //聊天室 Id
    private boolean isCustomName; //多人聊天室使用, true為聊天室title被使用者修改過
    private String serviceNumberType; //服務號類型
    private List<String> memberIds; //聊天室成員 Ids
    private List<String> provisionalIds; //聊天室臨時成員 Ids
    private String firstunReadMessageId; //第一句未讀訊息 Id
    private String serviceNumberStatus; //若聊天室為服務號成员聊天室，則會給服務號聊天室的服務狀態
    private String serviceNumberAvatarId; //若聊天室為服務號成员聊天室，則會給服務號的頭像 Id
    private String name; //聊天室名稱
    private long lastEndServiceTime; //若聊天室為服務號成员聊天室，則會給服務聊天室的最後服務時間
    private Set<String> serviceNumberOpenType; //若聊天室為服務號成员聊天室，則會給服務號的開放狀態，分為：P（限邀請對象）| I （對內）| O （對外）| C（咨詢）
    private LastMessage lastMessage; //最後訊息相關資料
    private boolean isMute; //聊天室是否靜音
    private String serviceNumberName; //若聊天室為服務號成员聊天室，則會給服務號名稱
    private boolean isTop; //是否至頂, 隱藏欄位, 規格沒有
    private boolean deleted; //*該聊天室是否被刪除
    private boolean member_deleted; //該會員是否從該服務號被移除
    private long dfrTime; //*該資料被刪除時間
    private long updateTime;
    private int lastSequence;
    private String serviceNumberAgentId;
    private String chatId;
    private boolean warned;
    //以下為本地欄位
    private long interactionTime;
    private LastMessage failedMessage;
    private InputLogBean editMessage;
    public boolean serviceNumberOwnerStop = false;
    public boolean isWarned() { return warned; }
    public String getChatId() {return chatId;}
    public void setChatId(String chatId) {this.chatId = chatId;}
    public int getLastSequence() { return lastSequence; }
    public void setLastSequence(int lastSequence) { this.lastSequence = lastSequence; }

    public boolean isServiceNumberOwnerStop() {
        return serviceNumberOwnerStop;
    }

    public void setServiceNumberOwnerStop(boolean isStop) {
        serviceNumberOwnerStop = isStop;
    }
    public void setServiceNumberAgentId(String serviceNumberAgentId) {
        this.serviceNumberAgentId = serviceNumberAgentId;
    }
    public String getServiceNumberAgentId() {return serviceNumberAgentId; }

    public String getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public String getServiceNumberId() {
        return serviceNumberId;
    }

    public void setServiceNumberId(String serviceNumberId) {
        this.serviceNumberId = serviceNumberId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUserType() {
        return ownerUserType;
    }

    public void setOwnerUserType(String ownerUserType) {
        this.ownerUserType = ownerUserType;
    }

    public String getServiceNumberOwnerId() {
        return serviceNumberOwnerId;
    }

    public void setServiceNumberOwnerId(String serviceNumberOwnerId) {
        this.serviceNumberOwnerId = serviceNumberOwnerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isTransferFlag() {
        return transferFlag;
    }

    public void setTransferFlag(boolean transferFlag) {
        this.transferFlag = transferFlag;
    }

    public String getTransferReason() {
        return transferReason;
    }

    public void setTransferReason(String transferReason) {
        this.transferReason = transferReason;
    }

    public int getUnReadNum() {
        return unReadNum;
    }

    public void setUnReadNum(int unReadNum) {
        this.unReadNum = unReadNum;
    }

    public long getFirstunReadMessageTime() {
        return firstunReadMessageTime;
    }

    public void setFirstunReadMessageTime(long firstunReadMessageTime) {
        this.firstunReadMessageTime = firstunReadMessageTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isCustomName() {
        return isCustomName;
    }

    public void setCustomName(boolean customName) {
        isCustomName = customName;
    }

    public String getServiceNumberType() {
        return serviceNumberType;
    }

    public void setServiceNumberType(String serviceNumberType) {
        this.serviceNumberType = serviceNumberType;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public List<String> getProvisionalIds() {
        return provisionalIds;
    }

    public void setProvisionalIds(List<String> provisionalIds) {
        this.provisionalIds = provisionalIds;
    }

    public String getFirstunReadMessageId() {
        return firstunReadMessageId;
    }

    public void setFirstunReadMessageId(String firstunReadMessageId) {
        this.firstunReadMessageId = firstunReadMessageId;
    }

    public String getServiceNumberStatus() {
        return serviceNumberStatus;
    }

    public void setServiceNumberStatus(String serviceNumberStatus) {
        this.serviceNumberStatus = serviceNumberStatus;
    }

    public String getServiceNumberAvatarId() {
        return serviceNumberAvatarId;
    }

    public void setServiceNumberAvatarId(String serviceNumberAvatarId) {
        this.serviceNumberAvatarId = serviceNumberAvatarId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastEndServiceTime() {
        return lastEndServiceTime;
    }

    public void setLastEndServiceTime(long lastEndServiceTime) {
        this.lastEndServiceTime = lastEndServiceTime;
    }

    public Set<String> getServiceNumberOpenType() {
        return serviceNumberOpenType;
    }

    public void setServiceNumberOpenType(Set<String> serviceNumberOpenType) {
        this.serviceNumberOpenType = serviceNumberOpenType;
    }

    public LastMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(LastMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public String getServiceNumberName() {
        return serviceNumberName;
    }

    public void setServiceNumberName(String serviceNumberName) {
        this.serviceNumberName = serviceNumberName;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean top) {
        isTop = top;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getDfrTime() {
        return dfrTime;
    }

    public void setDfrTime(long dfrTime) {
        this.dfrTime = dfrTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getInteractionTime() {
        return interactionTime;
    }

    public void setInteractionTime(long interactionTime) {
        this.interactionTime = interactionTime;
    }

    public LastMessage getFailedMessage() {
        return failedMessage;
    }

    public void setFailedMessage(LastMessage failedMessage) {
        this.failedMessage = failedMessage;
    }

    public InputLogBean getEditMessage() {
        return editMessage;
    }

    public void setEditMessage(InputLogBean editMessage) {
        this.editMessage = editMessage;
    }

    @Override
    public int compareTo(RoomRecentItem o) {
        return ComparisonChain.start()
                .compare(o.getWeights(), this.getWeights())
                .compare(o.getTimeWeights(), this.getTimeWeights())
                .result();
    }

    private double getTimeWeights(){
        long sendTime = (lastMessage != null && lastMessage.getSendTime() > 0) ? lastMessage.getSendTime() : 0;
        if(unReadNum > 0){
            return sendTime;
        }
        return Math.max(interactionTime, sendTime);
    }
    /**
     * sorting Weight Logic Of Chat Room List
     * Unread chat room 128
     * Top 64
     * My favorite 32 ## Must have unread
     * Someone@Me 16
     * business 8
     * Object chat room 4 ## To have unread
     * Send failure 2
     * Draft 1
     */
    private double getWeights() {
        double weight = 0.0d;
        weight += Math.abs(this.unReadNum) > 0 ? 128.0d : 0.0d;
        weight += this.isTop ? 64.0d : 0.0d;
//        weight += this.isFavourite ? (Math.abs(this.unReadNum) > 0 ? 32.0d : 0.0d) : 0.0d;
//        weight += this.isAtMe ? 16.0d : 0.0d;
//        weight += !Strings.isNullOrEmpty(this.businessId) ? (Math.abs(this.unReadNum) > 0 ? 8.0d : 0.0d) : 0.0d;
//        weight += this.failedMessage != null ? 2.0d : 0.0d;
//        InputLogBean bean = InputLogBean.from(this.getUnfinishedEdited());
//        weight += bean != null && !Strings.isNullOrEmpty(bean.getText()) ? 1.0d : 0.0d;
        return weight;
    }
}