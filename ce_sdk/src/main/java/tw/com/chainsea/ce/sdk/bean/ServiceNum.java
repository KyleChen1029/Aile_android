package tw.com.chainsea.ce.sdk.bean;

import androidx.annotation.NonNull;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.servicenumber.BusinessCardInfo;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.http.ce.model.Member;

/**
 * Created by sunhui on 2018/5/10.
 */

public class ServiceNum implements Cloneable {
    public String serviceNumberId; //服务号ID
    public String name; //服务号名称
    public String serviceNumberAvatarId; //O服务号头像，官方和管理服务号没有
    public String description; //服务号描述
    private String broadcastRoomId;
    private String ownerId;
    private String serviceWelcomeMessage; //首次欢迎语
    private String everyContactMessage; //每次欢迎语
    private String serviceIdleMessage; //无人应答欢迎语
    private int serviceIdleTime; //无人应答等待时长，单位：秒
    private int serviceTimeoutTime; //服务超时时长，单位：秒
    private String serviceNumberType; //服务号类型
    public String status = "Enable"; //服务号状态
    public List<String> serviceOpenType; //服务号公开类型(規格書上的型態是錯的)
    private String serviceMemberRoomId; //服务号成员聊天室ID
    private boolean robotServiceFlag; //是否启用机器人服务
    private String robotId; //O机器人ID，启用机器人服务时可用
    private String robotName; //O机器人名称，启用机器人服务时可用
    public List<Member> memberItems; //服务号成员数组
    public String roomId; //服务号订阅聊天室ID
    public boolean isSubscribe; //是否有訂閱
    public long updateTime;

    private boolean isSelected = false; //Adapter是否有被點選
    private BusinessCardInfo businessCardInfo;

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public static ServiceNum of(ServiceNumberEntity entity) {
        ServiceNum data = new ServiceNum();
        data.description = entity.getDescription();
        data.name = entity.getName();
        data.serviceNumberAvatarId = entity.getAvatarId();
        data.isSubscribe = entity.isSubscribe();
        data.roomId = entity.getRoomId();
        data.serviceNumberId = entity.getServiceNumberId();
        data.serviceOpenType = entity.getServiceOpenType();
        return data;
    }

    public String getServiceNumberId() {
        return serviceNumberId;
    }

    public String getName() {
        return name;
    }

    public String getServiceNumberAvatarId() {
        return serviceNumberAvatarId;
    }

    public String getDescription() {
        return description;
    }

    public String getBroadcastRoomId() {
        return broadcastRoomId;
    }

    public void setBroadcastRoomId(String broadcastRoomId) {
        this.broadcastRoomId = broadcastRoomId;
    }

    public void setServiceOpenType(List<String> serviceOpenType) {
        this.serviceOpenType = serviceOpenType;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getServiceWelcomeMessage() {
        return serviceWelcomeMessage;
    }

    public String getEveryContactMessage() {
        return everyContactMessage;
    }

    public String getServiceIdleMessage() {
        return serviceIdleMessage;
    }

    public int getServiceIdleTime() {
        return serviceIdleTime;
    }

    public int getServiceTimeoutTime() {
        return serviceTimeoutTime;
    }

    public String getServiceNumberType() {
        return serviceNumberType;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getServiceOpenType() {
        return serviceOpenType;
    }

    public String getServiceMemberRoomId() {
        return serviceMemberRoomId;
    }

    public boolean isRobotServiceFlag() {
        return robotServiceFlag;
    }

    public String getRobotId() {
        return robotId;
    }

    public String getRobotName() {
        return robotName;
    }

    public List<Member> getMemberItems() {
        return memberItems;
    }

    public String getRoomId() {
        return roomId;
    }

    public boolean isSubscribe() {
        return isSubscribe;
    }

    public BusinessCardInfo getBusinessCardInfo() {
        return businessCardInfo;
    }

    @Override
    @NonNull
    public ServiceNum clone() {
        try {
            return (ServiceNum) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServiceNum other = (ServiceNum) obj;
        if (this.isSelected != ((ServiceNum) obj).isSelected) {
            return false;
        } else return this.serviceNumberId.equals(other.getServiceNumberId());
    }
}
