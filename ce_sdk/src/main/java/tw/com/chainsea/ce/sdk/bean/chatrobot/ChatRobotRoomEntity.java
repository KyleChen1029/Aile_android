package tw.com.chainsea.ce.sdk.bean.chatrobot;

import java.io.Serializable;
import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;

public class ChatRobotRoomEntity implements Serializable {

    public String avatarId;
    public String serviceNumberId;
    public long updateTime;
    public String ownerId;
    public String serviceNumberOwnerId;
    public String type;
    public Boolean deleted;
    public int unReadNum;
    public int roomMemberIdentity;
    public String id;
    public List<String> memberIds;
    public String serviceNumberType;
    public String serviceNumberStatus;
    public String serviceNumberAvatarId;
    public String name;
    public int lastEndServiceTime;
    public List<String> serviceNumberOpenType;
    public MessageEntity lastMessage;
    public boolean isMute;
    public String serviceNumberName;

}
