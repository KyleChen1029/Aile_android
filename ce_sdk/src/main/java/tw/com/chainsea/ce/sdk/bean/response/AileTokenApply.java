package tw.com.chainsea.ce.sdk.bean.response;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.response.base.RequestBean;
import tw.com.chainsea.ce.sdk.bean.response.base.ResponseBean;
import tw.com.chainsea.ce.sdk.config.AppConfig;

/**
 * current by evan on 2020-10-22
 *
 * @author Evan Wang
 * date 2020-10-22
 */
public class AileTokenApply {

    public enum ConnectType {
        @SerializedName("Socket.IO") SOCKET_IO("Socket.IO"),
        @SerializedName("Jocket") JOCKET("Jocket"),
        @SerializedName("UNDEF") UNDEF("UNDEF");

        private String type;

        ConnectType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public static class Req extends RequestBean implements Serializable {
        private static final long serialVersionUID = -3727395852892002235L;

        private String loginMode;
        private String countryCode;
        private String loginName;
        private String password;
        private String deviceType;
        private String deviceName;
        private String osType;
        private String uniqueID;
        private String tenantCode;
        private String authToken;

        @Override
        public String toRequestString() {
            try {
                 if("AuthTokenLogin".equals(getLoginMode())){
                     return new JSONObject().put("_header_", new JSONObject().put("language", AppConfig.LANGUAGE))
                             .put("loginMode", getLoginMode())
                             .put("tenantCode", getTenantCode())
                             .put("deviceType", getDeviceType())
                             .put("deviceName", getDeviceName())
                             .put("osType", getOsType())
                             .put("uniqueID", getUniqueID())
                             .put("authToken", getAuthToken())
                             .toString();
                 }else {
                     return new JSONObject().put("_header_", new JSONObject().put("language", AppConfig.LANGUAGE))
                             .put("countryCode", getCountryCode())
                             .put("deviceName", getDeviceName())
                             .put("deviceType", getDeviceType())
                             .put("loginMode", getLoginMode())
                             .put("loginName", getLoginName())
                             .put("osType", getOsType())
                             .put("password", getPassword())
                             .put("tenantCode", getTenantCode())
                             .put("uniqueID", getUniqueID())
                             .toString();
                 }
            } catch (Exception e) {
                return "";
            }
        }


        @Override
        public void close() throws Exception {

        }

        public String getLoginMode() {
            return loginMode;
        }

        public void setLoginMode(String loginMode) {
            this.loginMode = loginMode;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getLoginName() {
            return loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getOsType() {
            return osType;
        }

        public void setOsType(String osType) {
            this.osType = osType;
        }

        public String getUniqueID() {
            return uniqueID;
        }

        public void setUniqueID(String uniqueID) {
            this.uniqueID = uniqueID;
        }

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }
    }

    public static class Resp extends ResponseBean implements Serializable {
        private static final long serialVersionUID = -145252097559103811L;
        private String bossServiceNumberId;
        private String tokenId;
        private String employeeTokenId;
        private String contactTokenId;
        private TenantInfo tenantInfo;
        private Configuration configuration;
        private String refreshTokenId;
        private User user;
        private String deviceId;
        private String authToken;
        private ArrayList<AiffInfo> aiffInfo;

        @Override
        public void close() throws Exception {

        }

        public String getRefreshTokenId() {
            return refreshTokenId;
        }

        public void setRefreshTokenId(String refreshTokenId) {
            this.refreshTokenId = refreshTokenId;
        }

        public String getAuthToken() { return authToken; }
        public String getTokenId() {
            return tokenId;
        }

        public String getBossServiceNumberId() {
            return bossServiceNumberId;
        }
        public void setTokenId(String tokenId) {
            this.tokenId = tokenId;
        }

        public String getEmployeeTokenId() {
            return employeeTokenId;
        }

        public void setEmployeeTokenId(String employeeTokenId) {
            this.employeeTokenId = employeeTokenId;
        }

        public String getContactTokenId() {
            return contactTokenId;
        }

        public void setContactTokenId(String contactTokenId) {
            this.contactTokenId = contactTokenId;
        }

        public TenantInfo getTenantInfo() {
            return tenantInfo;
        }

        public void setTenantInfo(TenantInfo tenantInfo) {
            this.tenantInfo = tenantInfo;
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public void setConfiguration(Configuration configuration) {
            this.configuration = configuration;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public ArrayList<AiffInfo> getAiffInfo() {
            return aiffInfo;
        }

        public void setAiffInfo(ArrayList<AiffInfo> aiffInfo) {
            this.aiffInfo = aiffInfo;
        }

        public static class Configuration implements Serializable {
            private static final long serialVersionUID = 6404756464868436728L;
            private String socketIoUrl;
            private boolean enableAck;
            private String socketIoNamespace;
            @SerializedName("connectType")
            private ConnectType connectType;
            private String socketIoPassword;
            private String systemUserAvatarId;
            private String systemUserName;
            private String systemUserId;

            public String getSocketIoUrl() {
                return socketIoUrl;
            }

            public void setSocketIoUrl(String socketIoUrl) {
                this.socketIoUrl = socketIoUrl;
            }

            public boolean isEnableAck() {
                return enableAck;
            }

            public void setEnableAck(boolean enableAck) {
                this.enableAck = enableAck;
            }

            public String getSocketIoNamespace() {
                return socketIoNamespace;
            }

            public void setSocketIoNamespace(String socketIoNamespace) {
                this.socketIoNamespace = socketIoNamespace;
            }

            public String getSystemUserAvatarId() {
                return systemUserAvatarId;
            }

            public String getSystemUserId() {
                return systemUserId;
            }

            public String getSystemUserName() {
                return systemUserName;
            }

            public ConnectType getConnectType() {
                return connectType;
            }

            public void setConnectType(ConnectType connectType) {
                this.connectType = connectType;
            }

            public String getSocketIoPassword() {
                return socketIoPassword;
            }

            public void setSocketIoPassword(String socketIoPassword) {
                this.socketIoPassword = socketIoPassword;
            }
        }

        public static class User implements Serializable {
            private static final long serialVersionUID = -8793536758946988727L;
            private String id;
            private String avatarId;

            private boolean isBindAile;

            private String bindUrl;

            private boolean isCollectInfo;
            private String nickName;
            //        private Object userRight;
            @SerializedName("userType")
            private UserType userType;
            private SipwayInfo sipwayInfo;
            private Employee employee;
            private List<OnlineDeviceInfo> onlineDeviceInfo;

            private boolean hasBusinessSystem;
            private boolean hasBindEmployee;
            private boolean isMute;
            private String personRoomId;

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
                User other = (User) obj;
                if (this.id == null) {
                    return other.getId() == null;
                } else return this.id.equals(other.getId());
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getAvatarId() {
                return avatarId;
            }

            public void setAvatarId(String avatarId) {
                this.avatarId = avatarId;
            }

            public String getNickName() {
                return nickName;
            }

            public void setNickName(String nickName) {
                this.nickName = nickName;
            }

            public UserType getUserType() {
                return userType;
            }

            public void setUserType(UserType userType) {
                this.userType = userType;
            }

            public SipwayInfo getSipwayInfo() {
                return sipwayInfo;
            }

            public void setSipwayInfo(SipwayInfo sipwayInfo) {
                this.sipwayInfo = sipwayInfo;
            }

            public Employee getEmployee() {
                return employee;
            }

            public void setEmployee(Employee employee) {
                this.employee = employee;
            }

            public List<OnlineDeviceInfo> getOnlineDeviceInfo() {
                return onlineDeviceInfo;
            }

            public void setOnlineDeviceInfo(List<OnlineDeviceInfo> onlineDeviceInfo) {
                this.onlineDeviceInfo = onlineDeviceInfo;
            }

            public boolean isHasBusinessSystem() {
                return hasBusinessSystem;
            }

            public void setHasBusinessSystem(boolean hasBusinessSystem) {
                this.hasBusinessSystem = hasBusinessSystem;
            }

            public boolean isHasBindEmployee() {
                return hasBindEmployee;
            }

            public void setHasBindEmployee(boolean hasBindEmployee) {
                this.hasBindEmployee = hasBindEmployee;
            }

            public boolean isMute() {
                return isMute;
            }

            public void setMute(boolean mute) {
                isMute = mute;
            }

            public String getPersonRoomId() {
                return personRoomId;
            }

            public void setPersonRoomId(String personRoomId) {
                this.personRoomId = personRoomId;
            }

            public boolean getIsBindAile() { return isBindAile; }

            public String getBindUrl() { return bindUrl; }

            public boolean getIsCollectInfo() {return isCollectInfo; }
        }

        public static class TenantInfo implements Serializable {
            private static final long serialVersionUID = 6477371966763214070L;
            private int tokenValidSeconds;
            private String businessSystemUrl;
            private boolean isEnableCall;
            private long uploadFileMaxSize;

            private int retractValidMinute;
            private String themeItem;

            public int getTokenValidSeconds() {
                return tokenValidSeconds;
            }

            public void setTokenValidSeconds(int tokenValidSeconds) {
                this.tokenValidSeconds = tokenValidSeconds;
            }

            public int getRetractValidMinute() {return retractValidMinute; }
            public String getBusinessSystemUrl() {
                return businessSystemUrl;
            }

            public void setBusinessSystemUrl(String businessSystemUrl) {
                this.businessSystemUrl = businessSystemUrl;
            }

            public boolean isEnableCall() {
                return isEnableCall;
            }

            public void setEnableCall(boolean enableCall) {
                isEnableCall = enableCall;
            }

            public long getUploadFileMaxSize() {
                return uploadFileMaxSize;
            }

            public void setUploadFileMaxSize(int uploadFileMaxSize) {
                this.uploadFileMaxSize = uploadFileMaxSize;
            }
            public String getThemeItem() {
                return themeItem;
            }
            public void setThemeItem(String themeItem) {
                this.themeItem = themeItem;
            }
        }

        public static class SipwayInfo implements Serializable {
            private static final long serialVersionUID = -4207351418464210709L;
            private String authPass;
            private String sipxExtensionURL;
            private String sipxURL;
            private int duration;
            private String userNo;
            private String corpVccId;
            private String webCallURL;
            private boolean isExtension;

            public String getAuthPass() {
                return authPass;
            }

            public void setAuthPass(String authPass) {
                this.authPass = authPass;
            }

            public String getSipxExtensionURL() {
                return sipxExtensionURL;
            }

            public void setSipxExtensionURL(String sipxExtensionURL) {
                this.sipxExtensionURL = sipxExtensionURL;
            }

            public String getSipxURL() {
                return sipxURL;
            }

            public void setSipxURL(String sipxURL) {
                this.sipxURL = sipxURL;
            }

            public int getDuration() {
                return duration;
            }

            public void setDuration(int duration) {
                this.duration = duration;
            }

            public String getUserNo() {
                return userNo;
            }

            public void setUserNo(String userNo) {
                this.userNo = userNo;
            }

            public String getCorpVccId() {
                return corpVccId;
            }

            public void setCorpVccId(String corpVccId) {
                this.corpVccId = corpVccId;
            }

            public String getWebCallURL() {
                return webCallURL;
            }

            public void setWebCallURL(String webCallURL) {
                this.webCallURL = webCallURL;
            }

            public boolean isExtension() {
                return isExtension;
            }

            public void setExtension(boolean extension) {
                isExtension = extension;
            }
        }

        public static class OnlineDeviceInfo implements Serializable {
            private static final long serialVersionUID = 1693199518647057662L;
            private long loginTime;
            private String deviceType;
            private String osType;

            public long getLoginTime() {
                return loginTime;
            }

            public void setLoginTime(long loginTime) {
                this.loginTime = loginTime;
            }

            public String getDeviceType() {
                return deviceType;
            }

            public void setDeviceType(String deviceType) {
                this.deviceType = deviceType;
            }

            public String getOsType() {
                return osType;
            }

            public void setOsType(String osType) {
                this.osType = osType;
            }
        }

        public static class Employee implements Serializable {
            private static final long serialVersionUID = -4306053841961179243L;
            private String id;
            private String extension;
            private String duty;
            private String department;
            private String name;
            private String businessSystemUrl;
            private String loginName;

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
                Employee other = (Employee) obj;
                if (this.id == null) {
                    return other.getId() == null;
                } else return this.id.equals(other.getId());
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getExtension() {
                return extension;
            }

            public void setExtension(String extension) {
                this.extension = extension;
            }

            public String getDuty() {
                return duty;
            }

            public void setDuty(String duty) {
                this.duty = duty;
            }

            public String getDepartment() {
                return department;
            }

            public void setDepartment(String department) {
                this.department = department;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getBusinessSystemUrl() {
                return businessSystemUrl;
            }

            public void setBusinessSystemUrl(String businessSystemUrl) {
                this.businessSystemUrl = businessSystemUrl;
            }

            public String getLoginName() {
                return loginName;
            }

            public void setLoginName(String loginName) {
                this.loginName = loginName;
            }
        }

        public static class AiffInfo implements Serializable {
            private String tenantId;
            private Integer index;
            private String aiffKey;
            private String status;
            private String displayLocation;
            private String pictureId;
            private String url;
            private String userType;
            private String supportDevice;
            private String id;
            private String embedLocation;
            private String title;
            private String pictureUrl;
            private String displayType;
            private String userType_APP;
            private String name;
            private String aiffURL;
            private String applyType;
            private Boolean closeInAiff;
            private String tenantId$;
            private String incomingAiff;

            private String description;
            private List<String> serviceNumberIds = Lists.newArrayList();
            private List<String> serviceNumberNames = Lists.newArrayList();

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
                Employee other = (Employee) obj;
                if (this.id == null) {
                    return other.getId() == null;
                } else return this.id.equals(other.getId());
            }

            public String getTenantId() {
                return tenantId;
            }

            public void setTenantId(String tenantId) {
                this.tenantId = tenantId;
            }

            public Integer getIndex() {
                return index;
            }

            public void setIndex(Integer index) {
                this.index = index;
            }

            public String getDisplayLocation() {
                return displayLocation;
            }

            public void setDisplayLocation(String displayLocation) {
                this.displayLocation = displayLocation;
            }

            public String getSupportDevice() {
                return supportDevice;
            }

            public String getUserType_APP() {
                return userType_APP;
            }

            public String getPictureId() {
                return pictureId;
            }

            public void setPictureId(String pictureId) {
                this.pictureId = pictureId;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getUserType() {
                return userType;
            }

            public void setUserType(String userType) {
                this.userType = userType;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getEmbedLocation() {
                return embedLocation;
            }

            public void setEmbedLocation(String embedLocation) {
                this.embedLocation = embedLocation;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getDisplayType() {
                return displayType;
            }

            public void setDisplayType(String displayType) {
                this.displayType = displayType;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getAiffURL() {
                return aiffURL;
            }

            public void setAiffURL(String aiffURL) {
                this.aiffURL = aiffURL;
            }

            public String getApplyType() {
                return applyType;
            }

            public void setApplyType(String applyType) {
                this.applyType = applyType;
            }

            public Boolean getCloseInAiff() {
                return closeInAiff;
            }

            public void setCloseInAiff(Boolean closeInAiff) {
                this.closeInAiff = closeInAiff;
            }

            public String getTenantId$() {
                return tenantId$;
            }

            public void setTenantId$(String tenantId$) {
                this.tenantId$ = tenantId$;
            }

            public void setIncomingAiff(String incomingAiff) {this.incomingAiff = incomingAiff;}

            public String getIncomingAiff() {return incomingAiff; }

            public void setServiceNumberIds(List<String> serviceNumberIds) {this.serviceNumberIds = serviceNumberIds;}

            public List<String> getServiceNumberIds(){return serviceNumberIds;}

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public List<String> getServiceNumberNames() {
                return serviceNumberNames;
            }
            public void setServiceNumberNames(List<String> serviceNumberNames) {
                this.serviceNumberNames = serviceNumberNames;
            }
            public String getStatus() {
                return status;
            }
         }
    }

}
