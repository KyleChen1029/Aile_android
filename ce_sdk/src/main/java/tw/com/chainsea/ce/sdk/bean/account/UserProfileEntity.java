package tw.com.chainsea.ce.sdk.bean.account;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.Entity;
import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.ServiceNumberPrivilege;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.http.ce.model.Item;
import tw.com.chainsea.ce.sdk.http.ce.model.Scope;

/**
 * account extend
 * Created by 90Chris on 2016/6/27.
 * profile
 */
public class UserProfileEntity implements Serializable, Entity, Cloneable {
    private static final long serialVersionUID = 7496523599672792031L;
    private String id; //员工ID
    private String nickName; //昵称
    private String name; //姓名
    private String avatarId; //头图ID
    private UserType userType = UserType.CONTACT; //用户类型，固定为employee, employee or  contact
    private String extension; //员工分机
    private String duty; //职务
    private String department; //部门
    private String openId; //对应CP的openId
    private String mood; //心情
    private List<String> serviceNumberIds; //客户所订阅的服务号ID数组
    private List<Scope> scopeArray; //绑定渠道相关资料
    private String status; //用戶狀態, 目前是Enable、Disable、Deleted
    private boolean isCollection;
    private boolean isBlock;
    private String otherPhone;
    private long mobile;
    private String birthday;
    private String alias;
    private String email;
    private String roomId;
    private String loginName;
    private String googleId;
    private String fbId;
    private String lineId;
    private List<Label> labelNames;
    private List<Label> labels;
    private AccountType type;
    private Gender gender;
    private String customerDescription;
    private String customerName;
    private String customerBusinessCardUrl;

    private Boolean isAddressBook;
    private boolean isSelected = false; //Adapter是否有被點選

    @SerializedName("privilege")
    private ServiceNumberPrivilege privilege = ServiceNumberPrivilege.UNDEF;

    private GroupPrivilegeEnum groupPrivilege = GroupPrivilegeEnum.Common;

    private boolean isOwner = false;

    boolean hasBindEmployee;
    String personRoomId;
    List<HomePagePic> homePagePics = Lists.newArrayList();

    //手機號碼是否可見
    boolean mobileVisible;

    boolean bindAile; //若客戶已加入過企業會員為true

    public UserProfileEntity(String id, String nickName, String name, String avatarId, UserType userType, String extension, String duty, String department, String openId, String mood, List<String> serviceNumberIds, List<Scope> scopeArray, String status, boolean isCollection, boolean isBlock, String otherPhone, long mobile, String birthday, String alias, String email, String roomId, String loginName, String googleId, String fbId, String lineId, List<Label> labelNames, List<Label> labels, AccountType type, Gender gender, String customerDescription, String customerName, String customerBusinessCardUrl, Boolean isAddressBook, ServiceNumberPrivilege privilege, boolean hasBindEmployee, String personRoomId, List<HomePagePic> homePagePics) {
        this.id = id;
        this.nickName = nickName;
        this.name = name;
        this.avatarId = avatarId;
        this.userType = userType;
        this.extension = extension;
        this.duty = duty;
        this.department = department;
        this.openId = openId;
        this.mood = mood;
        this.serviceNumberIds = serviceNumberIds;
        this.scopeArray = scopeArray;
        this.status = status;
        this.isCollection = isCollection;
        this.isBlock = isBlock;
        this.otherPhone = otherPhone;
        this.mobile = mobile;
        this.birthday = birthday;
        this.alias = alias;
        this.email = email;
        this.roomId = roomId;
        this.loginName = loginName;
        this.googleId = googleId;
        this.fbId = fbId;
        this.lineId = lineId;
        this.labelNames = labelNames;
        this.labels = labels;
        this.type = type;
        this.gender = gender;
        this.customerDescription = customerDescription;
        this.customerName = customerName;
        this.customerBusinessCardUrl = customerBusinessCardUrl;
        this.isAddressBook = isAddressBook;
        this.privilege = privilege;
        this.hasBindEmployee = hasBindEmployee;
        this.personRoomId = personRoomId;
        this.homePagePics = homePagePics;
    }

    public UserProfileEntity() {
    }

    private static UserType $default$userType() {
        return UserType.CONTACT;
    }

    private static ServiceNumberPrivilege $default$privilege() {
        return ServiceNumberPrivilege.UNDEF;
    }

    public static UserProfileEntityBuilder Build() {
        return new UserProfileEntityBuilder();
    }


    public boolean isHardCode() {
        return "00000000-0000-0000-0000-000000000000".equals(id);
    }

    public boolean isAddCell() {
        return isHardCode() && UserType.IS_ADD.equals(userType);
    }

    /**
     * UI operation usage, informal information
     */
    public static UserProfileEntity newAddCell(String title, String content) {
        return UserProfileEntity.Build()
            .id("00000000-0000-0000-0000-000000000000")
            .userType(UserType.IS_ADD)
            .nickName(title)
            .alias(content)
            .build();
    }

    public UserProfileEntity(AileTokenApply.Resp.User user) {
        this.id = user.getId();
        this.avatarId = user.getAvatarId();
        this.nickName = user.getNickName();
        this.roomId = user.getPersonRoomId();
    }

    public UserProfileEntity(Item item) {
        this.id = item.getId();
        if (item.getAddressBook() != null) {
            this.isAddressBook = item.getAddressBook();
            this.type = (this.isAddressBook) ? AccountType.FRIEND : AccountType.UNDEF;
        } else {
            this.type = AccountType.SELF;
        }

        this.avatarId = item.getAvatarId();
        this.nickName = item.getNickName();
        this.duty = item.getDuty();
        this.department = item.getDepartment();
        this.name = item.getName();
        this.roomId = item.getRoomId();
        this.openId = item.getOpenId();
        this.userType = (item.getUserType() != null) ? UserType.of(item.getUserType()) : UserType.CONTACT;
    }

    public static UserProfileEntity getEntity(@NonNull Cursor cursor) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.id = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ID);
        entity.nickName = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_NICKNAME);
        entity.name = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_NAME);
        entity.avatarId = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_AVATAR_URL);
        entity.userType = UserType.of(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_USER_TYPE));
        entity.extension = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_EXTENSION);
        entity.duty = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_DUTY);
        entity.department = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_DEPARTMENT);
        entity.openId = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_OPEN_ID);
        entity.mood = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_MOOD);
        entity.serviceNumberIds = JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_SERVICE_NUMBER_IDS), new TypeToken<List<String>>() {
        }.getType());
        entity.scopeArray = JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_SCOPE_ARRAY), new TypeToken<List<Scope>>() {
        }.getType());
        entity.status = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_STATUS);

        entity.customerName = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_CUSTOMER_NAME);
        entity.customerDescription = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION);
        entity.customerBusinessCardUrl = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL);
        entity.type = AccountType.of(Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_RELATION));
        entity.alias = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ALIAS);
        entity.roomId = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ROOM_ID);
        entity.isBlock = 1 == Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_BLOCK);
        entity.isCollection = "true".equals(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_COLLECTION));
        entity.otherPhone = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_OTHER_PHONE);
        return entity;
    }

    public static ContentValues getFriendValues(UserProfileEntity entity) {
        ContentValues contentValues = new ContentValues();
        if (entity.id != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_ID, entity.id);
        }
        if (entity.nickName != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_NICKNAME, entity.nickName);
        }
        if (entity.name != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_NAME, entity.name);
        }
        if (entity.avatarId != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_AVATAR_URL, entity.avatarId);
        }
        if (entity.userType != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_USER_TYPE, entity.userType.getUserType());
        }
        if (entity.extension != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_EXTENSION, entity.extension);
        }
        if (entity.duty != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_DUTY, entity.duty);
        }
        if (entity.department != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_DEPARTMENT, entity.department);
        }
        if (entity.openId != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_OPEN_ID, entity.openId);
        }
        if (entity.mood != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_MOOD, entity.mood);
        }
        if (entity.serviceNumberIds != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_SERVICE_NUMBER_IDS, JsonHelper.getInstance().toJson(entity.serviceNumberIds));
        }
        if (entity.scopeArray != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_SCOPE_ARRAY, JsonHelper.getInstance().toJson(entity.scopeArray));
        }
        if (entity.status != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_STATUS, entity.status);
        }
        if (entity.customerName != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_CUSTOMER_NAME, entity.customerName);
        }
        if (entity.customerDescription != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION, entity.customerDescription);
        }
        if (entity.customerBusinessCardUrl != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL, entity.customerBusinessCardUrl);
        }
        if (entity.mood != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_SIGNATURE, entity.mood);
        }
        if (entity.alias != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_ALIAS, entity.alias);
        }
        if (entity.roomId != null) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_ROOM_ID, entity.roomId);
        }
        contentValues.put(DBContract.UserProfileEntry.COLUMN_COLLECTION, entity.isCollection ? "true" : "false");

        if (entity.isAddressBook != null && entity.isAddressBook) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_RELATION, "101");
        } else {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_RELATION, "100");
        }

        if (!Strings.isNullOrEmpty(entity.otherPhone)) {
            contentValues.put(DBContract.UserProfileEntry.COLUMN_OTHER_PHONE, entity.otherPhone);
        }
        return contentValues;
    }

    public String getId() {
        return id;
    }

    public String getAvatarId() {
        return avatarId;
    }

    public String getNickName() {
        if (alias != null && !alias.isEmpty()) {
            return alias;
        }
        return nickName;
    }

    public String getOriginName() {
        return nickName;
    }

    public boolean isMobileVisible() {
        return mobileVisible;
    }

    public void setMobileVisible(boolean mobileVisible) {
        this.mobileVisible = mobileVisible;
    }

    public void setBindAile(boolean bindAile) {
        this.bindAile = bindAile;
    }

    public boolean isBindAile() {
        return bindAile;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserProfileEntity other = (UserProfileEntity) obj;
        if (this.id == null) {
            return other.getId() == null;
        } else if (this.isSelected != ((UserProfileEntity) obj).isSelected) {
            return false;
        } else return this.id.equals(other.getId());
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    @Override
    @NonNull
    public UserProfileEntity clone() {
        try {
            return (UserProfileEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public String getName() {
        return this.name;
    }

    public UserType getUserType() {
        return this.userType;
    }

    public String getExtension() {
        return this.extension;
    }

    public String getDuty() {
        return this.duty;
    }

    public String getDepartment() {
        return this.department;
    }

    public String getOpenId() {
        return this.openId;
    }

    public String getMood() {
        return this.mood;
    }

    public List<String> getServiceNumberIds() {
        return this.serviceNumberIds;
    }

    public List<Scope> getScopeArray() {
        return this.scopeArray;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isCollection() {
        return this.isCollection;
    }

    public boolean isBlock() {
        return this.isBlock;
    }

    public String getOtherPhone() {
        return this.otherPhone;
    }

    public long getMobile() {
        return this.mobile;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public String getAlias() {
        return this.alias;
    }

    public String getEmail() {
        return this.email;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public String getLoginName() {
        return this.loginName;
    }

    public String getGoogleId() {
        return this.googleId;
    }

    public String getFbId() {
        return this.fbId;
    }

    public String getLineId() {
        return this.lineId;
    }

    public List<Label> getLabelNames() {
        return this.labelNames;
    }

    public List<Label> getLabels() {
        return this.labels;
    }

    public AccountType getType() {
        return this.type;
    }

    public Gender getGender() {
        return this.gender;
    }

    public String getCustomerDescription() {
        return this.customerDescription;
    }

    public String getCustomerName() {
        return this.customerName;
    }

    public String getCustomerBusinessCardUrl() {
        return this.customerBusinessCardUrl;
    }

    public Boolean getIsAddressBook() {
        return this.isAddressBook;
    }

    public ServiceNumberPrivilege getPrivilege() {
        return this.privilege;
    }

    public boolean isHasBindEmployee() {
        return this.hasBindEmployee;
    }

    public String getPersonRoomId() {
        return this.personRoomId;
    }

    public List<HomePagePic> getHomePagePics() {
        return this.homePagePics;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatarId(String avatarId) {
        this.avatarId = avatarId;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setServiceNumberIds(List<String> serviceNumberIds) {
        this.serviceNumberIds = serviceNumberIds;
    }

    public void setScopeArray(List<Scope> scopeArray) {
        this.scopeArray = scopeArray;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }

    public void setBlock(boolean isBlock) {
        this.isBlock = isBlock;
    }

    public void setOtherPhone(String otherPhone) {
        this.otherPhone = otherPhone;
    }

    public void setMobile(long mobile) {
        this.mobile = mobile;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public void setFbId(String fbId) {
        this.fbId = fbId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public void setLabelNames(List<Label> labelNames) {
        this.labelNames = labelNames;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setCustomerDescription(String customerDescription) {
        this.customerDescription = customerDescription;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setCustomerBusinessCardUrl(String customerBusinessCardUrl) {
        this.customerBusinessCardUrl = customerBusinessCardUrl;
    }

    public GroupPrivilegeEnum getGroupPrivilege() {
        return groupPrivilege;
    }

    public void setGroupPrivilege(
        GroupPrivilegeEnum groupPrivilege) {
        this.groupPrivilege = groupPrivilege;
    }

    public void setIsAddressBook(Boolean isAddressBook) {
        this.isAddressBook = isAddressBook;
    }

    public void setPrivilege(ServiceNumberPrivilege privilege) {
        this.privilege = privilege;
    }

    public void setHasBindEmployee(boolean hasBindEmployee) {
        this.hasBindEmployee = hasBindEmployee;
    }

    public void setPersonRoomId(String personRoomId) {
        this.personRoomId = personRoomId;
    }

    public void setHomePagePics(List<HomePagePic> homePagePics) {
        this.homePagePics = homePagePics;
    }

    @NonNull
    public String toString() {
        return "UserProfileEntity(id=" + this.getId() + ", nickName=" + this.getNickName() + ", name=" + this.getName() + ", avatarId=" + this.getAvatarId() + ", userType=" + this.getUserType() + ", extension=" + this.getExtension() + ", duty=" + this.getDuty() + ", department=" + this.getDepartment() + ", openId=" + this.getOpenId() + ", mood=" + this.getMood() + ", serviceNumberIds=" + this.getServiceNumberIds() + ", scopeArray=" + this.getScopeArray() + ", status=" + this.getStatus() + ", isCollection=" + this.isCollection() + ", isBlock=" + this.isBlock() + ", otherPhone=" + this.getOtherPhone() + ", mobile=" + this.getMobile() + ", birthday=" + this.getBirthday() + ", alias=" + this.getAlias() + ", email=" + this.getEmail() + ", roomId=" + this.getRoomId() + ", loginName=" + this.getLoginName() + ", googleId=" + this.getGoogleId() + ", fbId=" + this.getFbId() + ", lineId=" + this.getLineId() + ", labelNames=" + this.getLabelNames() + ", labels=" + this.getLabels() + ", type=" + this.getType() + ", gender=" + this.getGender() + ", customerDescription=" + this.getCustomerDescription() + ", customerName=" + this.getCustomerName() + ", customerBusinessCardUrl=" + this.getCustomerBusinessCardUrl() + ", isAddressBook=" + this.getIsAddressBook() + ", privilege=" + this.getPrivilege() + ", hasBindEmployee=" + this.isHasBindEmployee() + ", personRoomId=" + this.getPersonRoomId() + ", homePagePics=" + this.getHomePagePics() + ")";
    }

    public UserProfileEntityBuilder toBuilder() {
        return new UserProfileEntityBuilder().id(this.id).nickName(this.nickName).name(this.name).avatarId(this.avatarId).userType(this.userType).extension(this.extension).duty(this.duty).department(this.department).openId(this.openId).mood(this.mood).serviceNumberIds(this.serviceNumberIds).scopeArray(this.scopeArray).status(this.status).isCollection(this.isCollection).isBlock(this.isBlock).otherPhone(this.otherPhone).mobile(this.mobile).birthday(this.birthday).alias(this.alias).email(this.email).roomId(this.roomId).loginName(this.loginName).googleId(this.googleId).fbId(this.fbId).lineId(this.lineId).labelNames(this.labelNames).labels(this.labels).type(this.type).gender(this.gender).customerDescription(this.customerDescription).customerName(this.customerName).customerBusinessCardUrl(this.customerBusinessCardUrl).isAddressBook(this.isAddressBook).privilege(this.privilege).hasBindEmployee(this.hasBindEmployee).personRoomId(this.personRoomId).homePagePics(this.homePagePics);
    }

    public static class HomePagePic {
        String id;
        String picUrl;
        int sequence;

        HomePagePic(String id, String picUrl, int sequence) {
            this.id = id;
            this.picUrl = picUrl;
            this.sequence = sequence;
        }

        public static HomePagePicBuilder Build() {
            return new HomePagePicBuilder();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPicUrl() {
            return picUrl;
        }

        public void setPicUrl(String picUrl) {
            this.picUrl = picUrl;
        }

        public int getSequence() {
            return sequence;
        }

        public void setSequence(int sequence) {
            this.sequence = sequence;
        }

        public HomePagePicBuilder toBuilder() {
            return new HomePagePicBuilder().id(this.id).picUrl(this.picUrl).sequence(this.sequence);
        }

        public static class HomePagePicBuilder {
            private String id;
            private String picUrl;
            private int sequence;

            HomePagePicBuilder() {
            }

            public HomePagePicBuilder id(String id) {
                this.id = id;
                return this;
            }

            public HomePagePicBuilder picUrl(String picUrl) {
                this.picUrl = picUrl;
                return this;
            }

            public HomePagePicBuilder sequence(int sequence) {
                this.sequence = sequence;
                return this;
            }

            public HomePagePic build() {
                return new HomePagePic(id, picUrl, sequence);
            }

            @NonNull
            public String toString() {
                return "UserProfileEntity.HomePagePic.HomePagePicBuilder(id=" + this.id + ", picUrl=" + this.picUrl + ", sequence=" + this.sequence + ")";
            }
        }
    }

    public static class UserProfileEntityBuilder {
        private String id;
        private String nickName;
        private String name;
        private String avatarId;
        private UserType userType$value;
        private boolean userType$set;
        private String extension;
        private String duty;
        private String department;
        private String openId;
        private String mood;
        private List<String> serviceNumberIds;
        private List<Scope> scopeArray;
        private String status;
        private boolean isCollection;
        private boolean isBlock;
        private String otherPhone;
        private long mobile;
        private String birthday;
        private String alias;
        private String email;
        private String roomId;
        private String loginName;
        private String googleId;
        private String fbId;
        private String lineId;
        private List<Label> labelNames;
        private List<Label> labels;
        private AccountType type;
        private Gender gender;
        private String customerDescription;
        private String customerName;
        private String customerBusinessCardUrl;
        private Boolean isAddressBook;
        private ServiceNumberPrivilege privilege$value;
        private boolean privilege$set;
        private boolean hasBindEmployee;
        private String personRoomId;
        private List<HomePagePic> homePagePics;

        UserProfileEntityBuilder() {
        }

        public UserProfileEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserProfileEntityBuilder nickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public UserProfileEntityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserProfileEntityBuilder avatarId(String avatarId) {
            this.avatarId = avatarId;
            return this;
        }

        public UserProfileEntityBuilder userType(UserType userType) {
            this.userType$value = userType;
            this.userType$set = true;
            return this;
        }

        public UserProfileEntityBuilder extension(String extension) {
            this.extension = extension;
            return this;
        }

        public UserProfileEntityBuilder duty(String duty) {
            this.duty = duty;
            return this;
        }

        public UserProfileEntityBuilder department(String department) {
            this.department = department;
            return this;
        }

        public UserProfileEntityBuilder openId(String openId) {
            this.openId = openId;
            return this;
        }

        public UserProfileEntityBuilder mood(String mood) {
            this.mood = mood;
            return this;
        }

        public UserProfileEntityBuilder serviceNumberIds(List<String> serviceNumberIds) {
            this.serviceNumberIds = serviceNumberIds;
            return this;
        }

        public UserProfileEntityBuilder scopeArray(List<Scope> scopeArray) {
            this.scopeArray = scopeArray;
            return this;
        }

        public UserProfileEntityBuilder status(String status) {
            this.status = status;
            return this;
        }

        public UserProfileEntityBuilder isCollection(boolean isCollection) {
            this.isCollection = isCollection;
            return this;
        }

        public UserProfileEntityBuilder isBlock(boolean isBlock) {
            this.isBlock = isBlock;
            return this;
        }

        public UserProfileEntityBuilder otherPhone(String otherPhone) {
            this.otherPhone = otherPhone;
            return this;
        }

        public UserProfileEntityBuilder mobile(long mobile) {
            this.mobile = mobile;
            return this;
        }

        public UserProfileEntityBuilder birthday(String birthday) {
            this.birthday = birthday;
            return this;
        }

        public UserProfileEntityBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public UserProfileEntityBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserProfileEntityBuilder roomId(String roomId) {
            this.roomId = roomId;
            return this;
        }

        public UserProfileEntityBuilder loginName(String loginName) {
            this.loginName = loginName;
            return this;
        }

        public UserProfileEntityBuilder googleId(String googleId) {
            this.googleId = googleId;
            return this;
        }

        public UserProfileEntityBuilder fbId(String fbId) {
            this.fbId = fbId;
            return this;
        }

        public UserProfileEntityBuilder lineId(String lineId) {
            this.lineId = lineId;
            return this;
        }

        public UserProfileEntityBuilder labelNames(List<Label> labelNames) {
            this.labelNames = labelNames;
            return this;
        }

        public UserProfileEntityBuilder labels(List<Label> labels) {
            this.labels = labels;
            return this;
        }

        public UserProfileEntityBuilder type(AccountType type) {
            this.type = type;
            return this;
        }

        public UserProfileEntityBuilder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public UserProfileEntityBuilder customerDescription(String customerDescription) {
            this.customerDescription = customerDescription;
            return this;
        }

        public UserProfileEntityBuilder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public UserProfileEntityBuilder customerBusinessCardUrl(String customerBusinessCardUrl) {
            this.customerBusinessCardUrl = customerBusinessCardUrl;
            return this;
        }

        public UserProfileEntityBuilder isAddressBook(Boolean isAddressBook) {
            this.isAddressBook = isAddressBook;
            return this;
        }

        public UserProfileEntityBuilder privilege(ServiceNumberPrivilege privilege) {
            this.privilege$value = privilege;
            this.privilege$set = true;
            return this;
        }

        public UserProfileEntityBuilder hasBindEmployee(boolean hasBindEmployee) {
            this.hasBindEmployee = hasBindEmployee;
            return this;
        }

        public UserProfileEntityBuilder personRoomId(String personRoomId) {
            this.personRoomId = personRoomId;
            return this;
        }

        public UserProfileEntityBuilder homePagePics(List<HomePagePic> homePagePics) {
            this.homePagePics = homePagePics;
            return this;
        }

        public UserProfileEntity build() {
            UserType userType$value = this.userType$value;
            if (!this.userType$set) {
                userType$value = UserProfileEntity.$default$userType();
            }
            ServiceNumberPrivilege privilege$value = this.privilege$value;
            if (!this.privilege$set) {
                privilege$value = UserProfileEntity.$default$privilege();
            }
            return new UserProfileEntity(id, nickName, name, avatarId, userType$value, extension, duty, department, openId, mood, serviceNumberIds, scopeArray, status, isCollection, isBlock, otherPhone, mobile, birthday, alias, email, roomId, loginName, googleId, fbId, lineId, labelNames, labels, type, gender, customerDescription, customerName, customerBusinessCardUrl, isAddressBook, privilege$value, hasBindEmployee, personRoomId, homePagePics);
        }

        @NonNull
        public String toString() {
            return "UserProfileEntity.UserProfileEntityBuilder(id=" + this.id + ", nickName=" + this.nickName + ", name=" + this.name + ", avatarId=" + this.avatarId + ", userType$value=" + this.userType$value + ", extension=" + this.extension + ", duty=" + this.duty + ", department=" + this.department + ", openId=" + this.openId + ", mood=" + this.mood + ", serviceNumberIds=" + this.serviceNumberIds + ", scopeArray=" + this.scopeArray + ", status=" + this.status + ", isCollection=" + this.isCollection + ", isBlock=" + this.isBlock + ", otherPhone=" + this.otherPhone + ", mobile=" + this.mobile + ", birthday=" + this.birthday + ", alias=" + this.alias + ", email=" + this.email + ", roomId=" + this.roomId + ", loginName=" + this.loginName + ", googleId=" + this.googleId + ", fbId=" + this.fbId + ", lineId=" + this.lineId + ", labelNames=" + this.labelNames + ", labels=" + this.labels + ", type=" + this.type + ", gender=" + this.gender + ", customerDescription=" + this.customerDescription + ", customerName=" + this.customerName + ", customerBusinessCardUrl=" + this.customerBusinessCardUrl + ", isAddressBook=" + this.isAddressBook + ", privilege$value=" + this.privilege$value + ", hasBindEmployee=" + this.hasBindEmployee + ", personRoomId=" + this.personRoomId + ", homePagePics=" + this.homePagePics + ")";
        }
    }
}
