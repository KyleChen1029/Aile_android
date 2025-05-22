package tw.com.chainsea.ce.sdk.bean;

import android.content.ContentValues;

import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.http.ce.model.Scope;

public class CustomerEntity implements Serializable {
    private String id;
    private String name;
    private String nickName;
    private String customerName;
    private String customerDescription;
    private String customerBusinessCardUrl;
    private String avatarId;
    private boolean isAddressBook;
    private boolean isMobile;
    private String userType;
    private String roomId;
    private String openId;
    private List<ScopeInfo> scopeInfos;
    private List<String> serviceNumberIds; //客户所订阅的服务号ID数组
    private List<Scope> scopeArray; //绑定渠道相关资料
    private long updateTime;
    private boolean mobileVisible;
    private String status;

    public CustomerEntity(String id, String name, String nickName, String customerName, String customerDescription, String customerBusinessCardUrl, String avatarId, boolean isAddressBook, boolean isMobile, String dbString, String userType, String roomId, String openId, String scopeInfos, String serviceNumberIds, boolean mobileVisible, String status) {
        this.id = id;
        this.name = name;
        this.nickName = nickName;
        this.customerName = customerName;
        this.customerDescription = customerDescription;
        this.customerBusinessCardUrl = customerBusinessCardUrl;
        this.avatarId = avatarId;
        this.isAddressBook = isAddressBook;
        this.isMobile = isMobile;
        this.userType = userType;
        this.roomId = roomId;
        this.openId = openId;
        if (scopeInfos != null) {
            try {
                this.scopeInfos = JsonHelper.getInstance().from(scopeInfos, new TypeToken<List<ScopeInfo>>() {
                }.getType());
            } catch (Exception ignored) {
            }
        }
        this.serviceNumberIds = JsonHelper.getInstance().from(serviceNumberIds, new TypeToken<List<String>>() {
        }.getType());
        this.mobileVisible = mobileVisible;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNickName() {
        return nickName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerDescription() {
        return customerDescription;
    }

    public String getCustomerBusinessCardUrl() {
        return customerBusinessCardUrl;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public boolean isAddressBook() {
        return isAddressBook;
    }

    public boolean isMobile() {
        return isMobile;
    }

    public String getUserType() {
        return userType;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getOpenId() {
        return openId;
    }

    public List<ScopeInfo> getScopeInfos() {
        return scopeInfos;
    }

    public List<String> getServiceNumberIds() {
        return serviceNumberIds;
    }

    public List<Scope> getScopeArray() {
        return scopeArray;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public String getStatus() {
        return status;
    }

    public static ContentValues getContentValues(CustomerEntity entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBContract.BossServiceNumberContactEntry.ID, entity.id);
        contentValues.put(DBContract.BossServiceNumberContactEntry.NAME, entity.name);
        contentValues.put(DBContract.BossServiceNumberContactEntry.NICKNAME, entity.nickName);
        contentValues.put(DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME, entity.customerName);
        contentValues.put(DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION, entity.customerDescription);
        contentValues.put(DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL, entity.customerBusinessCardUrl);
        contentValues.put(DBContract.BossServiceNumberContactEntry.AVATAR_ID, entity.avatarId);
        contentValues.put(DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK, (entity.isAddressBook) ? 1 : 0);
        contentValues.put(DBContract.BossServiceNumberContactEntry.IS_MOBILE, (entity.isMobile) ? 1 : 0);
        contentValues.put(DBContract.BossServiceNumberContactEntry.USER_TYPE, entity.userType);
        contentValues.put(DBContract.BossServiceNumberContactEntry.ROOM_ID, entity.roomId);
        contentValues.put(DBContract.BossServiceNumberContactEntry.OPEN_ID, entity.openId);
        contentValues.put(DBContract.BossServiceNumberContactEntry.SCOPE_INFOS, JsonHelper.getInstance().toJson(entity.scopeInfos));
        contentValues.put(DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS, JsonHelper.getInstance().toJson(entity.serviceNumberIds));
        contentValues.put(DBContract.BossServiceNumberContactEntry.SCOPE_ARRAY, JsonHelper.getInstance().toJson(entity.scopeArray));
        contentValues.put(DBContract.BossServiceNumberContactEntry.UPDATE_TIME, entity.updateTime);
        contentValues.put(DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE, (entity.mobileVisible) ? 1 : 0);
        contentValues.put(DBContract.BossServiceNumberContactEntry.STATUS, entity.status);
        return contentValues;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() == obj.getClass())
            return true;
        CustomerEntity other = (CustomerEntity) obj;
        return this.id.equals(other.id) && this.roomId.equals(other.roomId);
    }
}
