package tw.com.chainsea.ce.sdk.http.ce.request;

import android.content.Context;

import com.google.common.collect.Lists;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.http.ce.base.NewRequestBase;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;

/**
 * current by evan on 2020-07-28
 *
 * @author Evan Wang
 * date 2020-07-28
 */
public class ServiceNumberItem2Request extends NewRequestBase {
    private final ApiListener<ServiceNumberEntity> listener;

    public ServiceNumberItem2Request(Context ctx, ApiListener<ServiceNumberEntity> listener) {
        super(ctx, "/" + ApiPath.serviceNumberItem);
        this.listener = listener;
    }

    @Override
    protected void success(JSONObject jsonObject, String s) throws JSONException {
        ServiceNumberEntity resp = JsonHelper.getInstance().from(s, ServiceNumberEntity.class);
        if (this.listener != null && resp != null) {
            if (resp.getOwnerId() != null && !resp.getOwnerId().isEmpty()) {
                resp.setOwner(userId.equals(resp.getOwnerId()));
            }
            listener.onSuccess(resp);
        }
    }

    @Override
    protected void failed(ErrCode code, String errorMessage) {
        if (this.listener != null) {
            listener.onFailed(errorMessage);
        }
    }

    public static class ServiceItemResp implements Serializable {
        private static final long serialVersionUID = -8045909158948684914L;
        String serviceNumberId;
        String description;
        String name;
        String serviceNumberAvatarId;
        List<ServiceNumberAgent> memberItems = Lists.newArrayList();
        boolean isSubscribe;
        String broadcastRoomId;

        public String getServiceNumberId() {
            return serviceNumberId;
        }

        public void setServiceNumberId(String serviceNumberId) {
            this.serviceNumberId = serviceNumberId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServiceNumberAvatarId() {
            return serviceNumberAvatarId;
        }

        public void setServiceNumberAvatarId(String serviceNumberAvatarId) {
            this.serviceNumberAvatarId = serviceNumberAvatarId;
        }

        public List<ServiceNumberAgent> getMemberItems() {
            return memberItems;
        }

        public void setMemberItems(List<ServiceNumberAgent> memberItems) {
            this.memberItems = memberItems;
        }

        public boolean isSubscribe() {
            return isSubscribe;
        }

        public void setSubscribe(boolean subscribe) {
            isSubscribe = subscribe;
        }

        public String getBroadcastRoomId() {
            return broadcastRoomId;
        }

        public void setBroadcastRoomId(String broadcastRoomId) {
            this.broadcastRoomId = broadcastRoomId;
        }
    }

    public static class ServiceNumberAgent implements Serializable {
        private static final long serialVersionUID = -7576489130341371561L;
        String id;
        boolean isAddressBook;
        String avatarId;
        String duty;
        String nickName;
        String department;
        String privilege;
        String name;
        String mood;
        boolean isMobile;
        String userType;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isAddressBook() {
            return isAddressBook;
        }

        public void setAddressBook(boolean addressBook) {
            isAddressBook = addressBook;
        }

        public String getAvatarId() {
            return avatarId;
        }

        public void setAvatarId(String avatarId) {
            this.avatarId = avatarId;
        }

        public String getDuty() {
            return duty;
        }

        public void setDuty(String duty) {
            this.duty = duty;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public String getDepartment() {
            return department;
        }

        public void setDepartment(String department) {
            this.department = department;
        }

        public String getPrivilege() {
            return privilege;
        }

        public void setPrivilege(String privilege) {
            this.privilege = privilege;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMood() {
            return mood;
        }

        public void setMood(String mood) {
            this.mood = mood;
        }

        public boolean isMobile() {
            return isMobile;
        }

        public void setMobile(boolean mobile) {
            isMobile = mobile;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }
    }
}
