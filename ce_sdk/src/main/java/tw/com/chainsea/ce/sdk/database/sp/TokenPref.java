package tw.com.chainsea.ce.sdk.database.sp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
import tw.com.chainsea.ce.sdk.bean.tenant.EnvironmentInfo;
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;

/**
 * current by evan on 2020-10-22
 *
 * @author Evan Wang
 * date 2020-10-22
 */
public class TokenPref {
    private static WeakReference<Context> mContext;
    private static volatile TokenPref INSTANCE;
    private static SharedPreferences sp;

    public enum PreferencesKey {
        IS_FIRST_TIME("IS_FIRST_TIME"),
        TOKEN_ID("TOKEN_ID"),
        CE_REFRESH_TOKEN_ID("CE_REFRESH_TOKEN_ID"),
        USER_ID("USER_ID_v15"),
        IS_REMEMBER_ME("IS_REMEMBER_ME"),
        DATABASE_VERSION("DATABASE_VERSION"),
        TOKEN_REQUEST_SUCCESS_RECORD_TIME("TOKEN_REQUEST_SUCCESS_RECORD_TIME"),
        TOKEN_VALID_SECONDS("TOKEN_VALID_SECONDS"),
        TOKEN_CE_VALID_SECONDS("TOKEN_CE_VALID_SECONDS"),
        CUSTOM_URL("CUSTOM_URL"),
        CURRENT_ENVIRONMENT("CURRENT_ENVIRONMENT"),
        RECORD_ENVIRONMENTS("RECORD_ENVIRONMENTS"),
        CURRENT_TENANT_CODE("CURRENT_TENANT_CODE"),
        CURRENT_TENANT_ID("CURRENT_TENANT_ID"),
        CURRENT_TENANT_URL("CURRENT_TENANT_URL"),
        DEVELOPER_ENVIRONMENTS("DEVELOPER_ENVIRONMENTS"),

        COUNTRY_CODE("COUNTRY_CODE"),
        ACCOUNT_NUMBER("ACCOUNT_NUMBER"),
        PASSWORD("PASSWORD"),

        LOGIN_MODE("LOGIN_MODE"),
        DEVICE_TYPE("DEVICE_TYPE"),
        DEVICE_NAME("DEVICE_NAME"),
        AUTH_TOKEN("AUTH_TOKEN"),
        TENANT_CODE("TENANT_CODE"),
        OS_TYPE("OS_TYPE"),
        DEVICE_ID("DEVICE_ID"),
        UNIQUE_ID("UNIQUE_ID"),
        LANGUAGE("LANGUAGE"),
        IS_ENABLE_CALL("IS_ENABLE_CALL"),
        DEVICE_LOGIN_TIME("DEVICE_LOGIN_TIME"),
        SOCKET_IO_URL("SOCKET_IO_URL"),
        SOCKET_ACK_ENABLE("SOCKET_ACK_ENABLE"),
        SOCKET_IO_NAME_SPACE("SOCKET_IO_NAME_SPACE"),
        CP_NAME("CP_NAME"),
        CP_ACCOUNT_ID("CP_ACCOUNT_ID"),
        CP_REFRESH_TOKEN_ID("CP_REFRESH_TOKEN_ID"),
        CP_TOKEN_ID("CP_TOKEN_ID"),
        CP_SOCKET_URL("CP_SOCKET_URL"),
        CP_SOCKET_NAME_SPACE("CP_SOCKET_NAME_SPACE"),
        CP_SOCKET_DEVICE_ID("CP_SOCKET_DEVICE_ID"),
        CP_SOCKET_NAME("CP_SOCKET_NAME"),
        CP_RELATION_TENANT("CP_RELATION_TENANT"),
        CP_CURRENT_TENANT("CP_CURRENT_TENANT"),
        CP_TRANS_TENANT_INFO("CP_TRANS_TENANT_INFO"),
        CP_TRANS_TENANT_ID("CP_TRANS_TENANT_ID"),
        USER_RESP("USER_RESP"),
        TENANT_INFO("TENANT_INFO"),
        CONNECT_TYPE("CONNECT_TYPE"),
        REMIND_NOTICE("REMIND_NOTICE"),
        IS_MUTE("IS_MUTE"),
        CURRENT_SERVER("CURRENT_SERVER"),

        APP_SERVER_VERSION("APP_SERVER_VERSION"),

        CE_AUTH_TOKEN("CE_AUTH_TOKEN"),
        IS_AUTO_CLOSE_SUB_CHAT("IS_AUTO_CLOSE_SUB_CHAT"),
        BOSS_SERVICE_NUMBER_ID("BOSS_SERVICE_NUMBER_ID"),
        RETRACT_VALID_MINUTE("RETRACT_VALID_MINUTE"),
        RETRACT_REMIND("RETRACT_REMIND"),
        FORCE_REFRESH_DATA("FORCE_REFRESH_DATA"),
        LOCATION_VERSION("LOCATION_VERSION"),

        PRELOAD_STEP("PRELOAD_STEP"),
        TENANT_NEED_PRELOAD("TENANT_NEED_PRELOAD"),

        SYSTEM_USER_AVATAR_ID("SYSTEM_USER_AVATAR_ID"),

        SYSTEM_USER_NAME("SYSTEM_USER_NAME"),

        SYSTEM_USER_ID("SYSTEM_USER_ID"),
        THEME_ITEM("THEME_ITEM"),
        PERMISSION_CREATE_TENANT("PERMISSION_CREATE_TENANT"),
        PERMISSION_JOIN_TENANT("PERMISSION_JOIN_TENANT"),
        SELF_DEFINE_ENVIRONMENT("SELF_DEFINE_ENVIRONMENT");

        PreferencesKey(String key) {
            this.key = key;
        }

        public String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    private TokenPref(Context context) {
        sp = context.getSharedPreferences("aile_token", Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        mContext = new WeakReference<>(context);
        if (INSTANCE == null) {
            INSTANCE = new TokenPref(context);
        }
    }

    public static TokenPref getInstance(Context context) {
        mContext = new WeakReference<>(context);
        if (INSTANCE == null) {
            INSTANCE = new TokenPref(context);
        }
        return INSTANCE;
    }

    public void setIsFirstTime(boolean isFirstTime) {
        sp.edit().putBoolean(PreferencesKey.IS_FIRST_TIME.getKey(), isFirstTime).apply();
    }

    public boolean isFirstTime() {
        return sp.getBoolean(PreferencesKey.IS_FIRST_TIME.getKey(), true);
    }

    public TokenPref setTokenId(String tokenId) {
        boolean success = sp.edit().putString(PreferencesKey.TOKEN_ID.key, tokenId).commit();
        return this;
    }

    public TokenPref setCeTokenRefreshId(String tokenRefreshId) {
        sp.edit().putString(PreferencesKey.CE_REFRESH_TOKEN_ID.key, tokenRefreshId).apply();
        return this;
    }

    public String getCeTokenRefreshId() {
        return sp.getString(PreferencesKey.CE_REFRESH_TOKEN_ID.key, "");
    }

    public String getTokenId() {
        return sp.getString(PreferencesKey.TOKEN_ID.key, "");
    }

    public TokenPref setUserId(String userId) {
        boolean success = sp.edit().putString(PreferencesKey.USER_ID.key, userId).commit();
        return this;
    }

    public void setRememberMe(boolean isRememberMe) {
        sp.edit().putBoolean(PreferencesKey.IS_REMEMBER_ME.getKey(), isRememberMe).apply();
    }

    public boolean isRememberMe() {
        return sp.getBoolean(PreferencesKey.IS_REMEMBER_ME.getKey(), false);
    }

    /**
     * 更新DB 版本
     */
    public void setDBVersion(int version) {
        sp.edit().putInt(PreferencesKey.DATABASE_VERSION.getKey(), version).apply();
    }

    public int getDBVersion() {
        return sp.getInt(PreferencesKey.DATABASE_VERSION.getKey(), 30);
    }


    public String getUserId() {
        return sp.getString(PreferencesKey.USER_ID.key, "");
    }

    public String getCurrentTenantCode() {
        return sp.getString(PreferencesKey.CURRENT_TENANT_CODE.key, "84459043-01");
    }

    public TokenPref setCurrentTenantCode(String currentTenantCode) {
        boolean success = sp.edit().putString(PreferencesKey.CURRENT_TENANT_CODE.key, currentTenantCode).commit();
        return this;
    }

    public String getCurrentTenantId() {
        return sp.getString(PreferencesKey.CURRENT_TENANT_ID.key, "dev_formal_aile");
    }

    public TokenPref setCurrentTenantId(String TenantId) {
        boolean success = sp.edit().putString(PreferencesKey.CURRENT_TENANT_ID.key, TenantId).commit();
        return this;
    }

    public String getCurrentTenantUrl() {
        return sp.getString(PreferencesKey.CURRENT_TENANT_URL.key, "https://csce.qbicloud.com:16922/ce");
    }

    public TokenPref setCurrentTenantUrl(String url) {
        boolean success = sp.edit().putString(PreferencesKey.CURRENT_TENANT_URL.key, url).commit();
        return this;
    }

    public long getTokenValidSecond() {
        return sp.getLong(PreferencesKey.TOKEN_VALID_SECONDS.key, 0L);
    }

    public void setTokenValidSecond(long tokenValidSecond) {
        sp.edit().putLong(PreferencesKey.TOKEN_VALID_SECONDS.key, tokenValidSecond).apply();
    }

    public long getCeTokenValidSecond() {
        return sp.getLong(PreferencesKey.TOKEN_CE_VALID_SECONDS.key, 0L);
    }

    public void setCeTokenValidSecond(long tokenValidSecond) {
        sp.edit().putLong(PreferencesKey.TOKEN_CE_VALID_SECONDS.key, tokenValidSecond).apply();
    }

    public TokenPref setLoginMode(String loginMode) {
        sp.edit().putString(PreferencesKey.LOGIN_MODE.key, loginMode).apply();
        return this;
    }

    public TokenPref setCountryCode(String countryCode) {
        sp.edit().putString(PreferencesKey.COUNTRY_CODE.key, countryCode).apply();
        return this;
    }

    public String getCountryCode() {
        return sp.getString(PreferencesKey.COUNTRY_CODE.key, "");
    }

    public TokenPref setAccountNumber(String accountNumber) {
        sp.edit().putString(PreferencesKey.ACCOUNT_NUMBER.key, accountNumber).apply();
        return this;
    }

    public String getAccountNumber() {
        return sp.getString(PreferencesKey.ACCOUNT_NUMBER.key, "");
    }

    public TokenPref setDeviceType(String deviceType) {
        sp.edit().putString(PreferencesKey.DEVICE_TYPE.key, deviceType).apply();
        return this;
    }

    public String getDeviceType() {
        return sp.getString(PreferencesKey.DEVICE_TYPE.key, "");
    }

    public TokenPref setDeviceName(String deviceName) {
        sp.edit().putString(PreferencesKey.DEVICE_NAME.key, deviceName).apply();
        return this;
    }

    public String getTenantCode() {
        return sp.getString(PreferencesKey.TENANT_CODE.key, "");
    }

    public TokenPref setTenantCode(String code) {
        sp.edit().putString(PreferencesKey.TENANT_CODE.key, code).apply();
        return this;
    }

    public String getAuthToken() {
        return sp.getString(PreferencesKey.AUTH_TOKEN.key, "");
    }

    public void setAuthToken(String token) {
        sp.edit().putString(PreferencesKey.AUTH_TOKEN.key, token).apply();
    }

    public String getCEAuthToken() {
        return sp.getString(PreferencesKey.CE_AUTH_TOKEN.key, "");
    }

    public void setCEAuthToken(String token) {
        boolean success = sp.edit().putString(PreferencesKey.CE_AUTH_TOKEN.key, token).commit();
    }

    public void setBossServiceNumberId(String id) {
        sp.edit().putString(PreferencesKey.BOSS_SERVICE_NUMBER_ID.key, id).apply();
    }

    public String getBossServiceNumberId() {
        return sp.getString(PreferencesKey.BOSS_SERVICE_NUMBER_ID.key, "");
    }

    public void setRetractValidMinute(int minute) {
        sp.edit().putInt(PreferencesKey.RETRACT_VALID_MINUTE.key, minute).apply();
    }

    public int getRetractValidMinute() {
        return sp.getInt(PreferencesKey.RETRACT_VALID_MINUTE.key, 0);
    }

    public void setRetractRemind(Boolean isChecked) {
        sp.edit().putBoolean(PreferencesKey.RETRACT_REMIND.key, isChecked).apply();
    }

    public Boolean getRetractRemind() {
        return sp.getBoolean(PreferencesKey.RETRACT_REMIND.key, false);
    }

    public String getDeviceName() {
        return sp.getString(PreferencesKey.DEVICE_NAME.key, "");
    }

    public String getOsType() {
        return sp.getString(PreferencesKey.OS_TYPE.key, "android");
    }

    public TokenPref setOsType(String osType) {
        boolean success = sp.edit().putString(PreferencesKey.OS_TYPE.key, osType).commit();
        return this;
    }

    public String getDeviceId() {
        return sp.getString(PreferencesKey.DEVICE_ID.key, "");
    }

    public void setDeviceId(String deviceId) {
        boolean success = sp.edit().putString(PreferencesKey.DEVICE_ID.key, deviceId).commit();
    }

    public String getUniqueID() {
        String uuid = UUID.randomUUID().toString();
        return sp.getString(PreferencesKey.UNIQUE_ID.key, uuid);
    }

    public TokenPref setUniqueID(String uniqueID) {
        boolean success = sp.edit().putString(PreferencesKey.UNIQUE_ID.key, uniqueID).commit();
        return this;
    }

    public String getPassword() {
        return sp.getString(PreferencesKey.PASSWORD.key, "");
    }

    public TokenPref setPassword(String password) {
        sp.edit().putString(PreferencesKey.PASSWORD.key, password).apply();
        return this;
    }

    public String getLanguage() {
        return sp.getString(PreferencesKey.LANGUAGE.key, "zh-tw");
    }

    public TokenPref setLanguage(String language) {
        boolean success = sp.edit().putString(PreferencesKey.LANGUAGE.key, language).commit();
        return this;
    }

    public boolean isEnableCall() {
        return sp.getBoolean(PreferencesKey.IS_ENABLE_CALL.key, false);
    }

    public TokenPref setEnableCall(boolean enableCall) {
        boolean success = sp.edit().putBoolean(PreferencesKey.IS_ENABLE_CALL.key, enableCall).commit();
        return this;
    }

    public String getCpName() {
        return sp.getString(PreferencesKey.CP_NAME.key, "");
    }

    public void setCpName(String name) {
        boolean success = sp.edit().putString(PreferencesKey.CP_NAME.key, name).commit();
    }

    public String getCpAccountId() {
        return sp.getString(PreferencesKey.CP_ACCOUNT_ID.key, "");
    }

    public void setCpAccountId(String accountId) {
        boolean success = sp.edit().putString(PreferencesKey.CP_ACCOUNT_ID.key, accountId).commit();
    }

    public String getCpRefreshTokenId() {
        return sp.getString(PreferencesKey.CP_REFRESH_TOKEN_ID.key, "");
    }

    public TokenPref setCpRefreshTokenId(String tokenId) {
        boolean success = sp.edit().putString(PreferencesKey.CP_REFRESH_TOKEN_ID.key, tokenId).commit();
        return this;
    }

    public String getCpTokenId() {
        return sp.getString(PreferencesKey.CP_TOKEN_ID.key, "");
    }

    public TokenPref setCpTokenId(String tokenId) {
        boolean success = sp.edit().putString(PreferencesKey.CP_TOKEN_ID.key, tokenId).commit();
        return this;
    }

    public String getCpSocketUrl() {
        return sp.getString(PreferencesKey.CP_SOCKET_URL.key, "");
    }

    public TokenPref setCpSocketUrl(String socketIoUrl) {
        boolean success = sp.edit().putString(PreferencesKey.CP_SOCKET_URL.key, socketIoUrl).commit();
        return this;
    }

    public String getCpSocketNameSpace() {
        return sp.getString(PreferencesKey.CP_SOCKET_NAME_SPACE.key, "");
    }

    public void setCpSocketNameSpace(String cpSocketNameSpace) {
        boolean success = sp.edit().putString(PreferencesKey.CP_SOCKET_NAME_SPACE.key, cpSocketNameSpace).commit();
    }

    public String getCpSocketDeviceId() {
        return sp.getString(PreferencesKey.CP_SOCKET_DEVICE_ID.key, "");
    }

    public TokenPref setCpSocketDeviceId(String cpSocketDeviceId) {
        boolean success = sp.edit().putString(PreferencesKey.CP_SOCKET_DEVICE_ID.key, cpSocketDeviceId).commit();
        return this;
    }

    public String getCpSocketName() {
        return sp.getString(PreferencesKey.CP_SOCKET_NAME.key, "");
    }

    public TokenPref setCpSocketName(String cpSocketName) {
        boolean success = sp.edit().putString(PreferencesKey.CP_SOCKET_NAME.key, cpSocketName).commit();
        return this;
    }

    public void setTenantList(Map<String, Boolean> map) {
        boolean success = sp.edit().putString(PreferencesKey.TENANT_NEED_PRELOAD.key, JsonHelper.getInstance().toJson(map)).commit();
    }

    public Map<String, Boolean> getTenantList() {
        String data = sp.getString(PreferencesKey.TENANT_NEED_PRELOAD.key, null);
        Map<String, Boolean> map = JsonHelper.getInstance().from(data, new TypeToken<Map<String, Boolean>>() {
        }.getType());
        if (map == null) {
            return new HashMap<>();
        } else {
            return JsonHelper.getInstance().from(data, new TypeToken<Map<String, Boolean>>() {
            }.getType());
        }
    }

    public boolean getIsNeedTenantPreload() {
        RelationTenant currentTenant = getCpCurrentTenant();
        String data = sp.getString(PreferencesKey.TENANT_NEED_PRELOAD.key, null);
        Map<String, Boolean> map = JsonHelper.getInstance().from(data, new TypeToken<Map<String, Boolean>>() {
        }.getType());
        if (map == null) return true;
        if (map.get(currentTenant.getTenantId()) == null) return true;
        return map.get(currentTenant.getTenantId());
    }

    public List<RelationTenant> getCpRelationTenantList() {
        String cpRelationTenantList = sp.getString(PreferencesKey.CP_RELATION_TENANT.key, null);
        if (Strings.isNullOrEmpty(cpRelationTenantList)) return new ArrayList<>();
        return JsonHelper.getInstance().from(cpRelationTenantList, new TypeToken<List<RelationTenant>>() {
        }.getType());
    }

    public void setCpRelationTenantList(List<RelationTenant> relationTenants) {
        boolean success = sp.edit().putString(PreferencesKey.CP_RELATION_TENANT.key, JsonHelper.getInstance().toJson(relationTenants)).commit();
    }

    public String getCpTransTenantId() {
        return sp.getString(PreferencesKey.CP_TRANS_TENANT_ID.key, "");
    }

    public void setCpTransTenantId(String qrcode) {
        boolean success = sp.edit().putString(PreferencesKey.CP_TRANS_TENANT_ID.key, qrcode).commit();
    }

    public TransTenantInfo getCpTransTenantInfo() {
        return JsonHelper.getInstance().from(sp.getString(PreferencesKey.CP_TRANS_TENANT_INFO.key, null), TransTenantInfo.class);
    }

    public void setCpTransTenantInfo(TransTenantInfo transTenantInfo) {
        boolean success = sp.edit().putString(PreferencesKey.CP_TRANS_TENANT_INFO.key, JsonHelper.getInstance().toJson(transTenantInfo)).commit();
    }

    public void setRemindNotice(Long dataTime) {
        sp.edit().putLong(PreferencesKey.REMIND_NOTICE.key, dataTime).apply();
    }

    public Long getRemindNotice() {
        return sp.getLong(PreferencesKey.REMIND_NOTICE.key, 0L);
    }

    public RelationTenant getCpCurrentTenant() {
        return JsonHelper.getInstance().from(sp.getString(PreferencesKey.CP_CURRENT_TENANT.key, null), RelationTenant.class);
    }

    public TokenPref setCpCurrentTenant(RelationTenant relationTenant) {
        boolean success = sp.edit().putString(PreferencesKey.CP_CURRENT_TENANT.key, JsonHelper.getInstance().toJson(relationTenant)).commit();
        return this;
    }

    public TokenPref setUserResp(AileTokenApply.Resp.User user) {
        boolean success = sp.edit().putString(PreferencesKey.USER_RESP.key, JsonHelper.getInstance().toJson(user)).commit();
        return this;
    }

    public AileTokenApply.Resp.User getUserResp() {
        return JsonHelper.getInstance().from(sp.getString(PreferencesKey.USER_RESP.key, null), AileTokenApply.Resp.User.class);
    }

    public void setTenantInfo(AileTokenApply.Resp.TenantInfo tenantInfo) {
        boolean success = sp.edit().putString(PreferencesKey.TENANT_INFO.key, JsonHelper.getInstance().toJson(tenantInfo)).commit();
    }

    public AileTokenApply.Resp.TenantInfo getTenantInfo() {
        return JsonHelper.getInstance().from(sp.getString(PreferencesKey.TENANT_INFO.key, null), AileTokenApply.Resp.TenantInfo.class);
    }

    public String getSocketIoUrl() {
        return sp.getString(PreferencesKey.SOCKET_IO_URL.key, "");
    }

    public TokenPref setSocketIoUrl(String socketIoUrl) {
        boolean success = sp.edit().putString(PreferencesKey.SOCKET_IO_URL.key, socketIoUrl).commit();
        return this;
    }

    public String getSocketIoNameSpace() {
        return sp.getString(PreferencesKey.SOCKET_IO_NAME_SPACE.key, "");
    }

    public TokenPref setSocketIoNameSpace(String socketIoNameSpace) {
        boolean success = sp.edit().putString(PreferencesKey.SOCKET_IO_NAME_SPACE.key, socketIoNameSpace).commit();
        return this;
    }

    public AileTokenApply.ConnectType getConnectType() {
        return AileTokenApply.ConnectType.valueOf(sp.getString(PreferencesKey.CONNECT_TYPE.key, "UNDEF"));
    }

    public TokenPref setSystemUserAvatarId(String avatarId) {
        sp.edit().putString(PreferencesKey.SYSTEM_USER_AVATAR_ID.key, avatarId).apply();
        return this;
    }

    public TokenPref setSystemUserName(String name) {
        boolean success = sp.edit().putString(PreferencesKey.SYSTEM_USER_NAME.key, name).commit();
        return this;
    }

    public TokenPref setSystemUserId(String id) {
        boolean success = sp.edit().putString(PreferencesKey.SYSTEM_USER_ID.key, id).commit();
        return this;
    }

    public String getSystemUserAvatarId() {
        return sp.getString(PreferencesKey.SYSTEM_USER_AVATAR_ID.key, null);
    }

    public String getSystemUserName() {
        return sp.getString(PreferencesKey.SYSTEM_USER_NAME.key, null);
    }

    public String getSystemUserId() {
        return sp.getString(PreferencesKey.SYSTEM_USER_ID.key, null);
    }

    public void setConnectType(AileTokenApply.ConnectType connectType) {
        if (connectType == null) {
            sp.edit().putString(PreferencesKey.CONNECT_TYPE.key, "JOCKET").apply();
        } else {
            sp.edit().putString(PreferencesKey.CONNECT_TYPE.key, connectType.name()).apply();
        }
    }

    public boolean isMute() {
        return sp.getBoolean(PreferencesKey.IS_MUTE.key, true);
    }

    public TokenPref setMute(boolean isMute) {
        sp.edit().putBoolean(PreferencesKey.IS_MUTE.key, isMute).apply();
        return this;
    }

    public void setIsAutoCloseSubChat(boolean enable) {
        sp.edit().putBoolean(PreferencesKey.IS_AUTO_CLOSE_SUB_CHAT.key, enable).apply();
    }

    public boolean isAutoCloseSubChat() {
        return sp.getBoolean(PreferencesKey.IS_AUTO_CLOSE_SUB_CHAT.key, false);
    }

    public String getCurrentServer() {
        return sp.getString(PreferencesKey.CURRENT_SERVER.key, "F");
    }

    public void setCurrentServer(String currentServer) {
        boolean success = sp.edit().putString(PreferencesKey.CURRENT_SERVER.key, currentServer).commit();
    }

    public void setSelfDefineEnvironment(String environment) {
        boolean success = sp.edit().putString(PreferencesKey.SELF_DEFINE_ENVIRONMENT.key, environment).commit();
    }

    public String getSelfDefineEnvironment() {
        return sp.getString(PreferencesKey.SELF_DEFINE_ENVIRONMENT.key, "");
    }

    public void setRecordEnvironment(String environmentCode) {
        Set<String> environments = getRecordEnvironments();
        environments.add(environmentCode);
        sp.edit().putStringSet(PreferencesKey.RECORD_ENVIRONMENTS.key, Sets.newHashSet(environments)).apply();
    }

    public Set<String> getRecordEnvironments() {
        Set<String> set = Sets.newHashSet();
        set.addAll(sp.getStringSet(PreferencesKey.RECORD_ENVIRONMENTS.key, Sets.newHashSet()));
        return set;
    }

    public boolean hasRecordEnvironments() {
        return !getEnvironmentList().isEmpty();
    }

    public void removeRecordEnvironment(String environmentCode) {
        Set<String> environments = getRecordEnvironments();
        environments.remove(environmentCode);
        sp.edit().putStringSet(PreferencesKey.RECORD_ENVIRONMENTS.key, Sets.newHashSet(environments)).apply();
    }

    public void setDeveloperEnvironment(String environmentCode) {
        Set<String> environments = getDeveloperEnvironment();
        environments.add(environmentCode);
        sp.edit().putStringSet(PreferencesKey.DEVELOPER_ENVIRONMENTS.key, Sets.newHashSet(environments)).apply();
    }

    public Set<String> getDeveloperEnvironment() {
        Set<String> set = Sets.newHashSet();
        set.addAll(sp.getStringSet(PreferencesKey.DEVELOPER_ENVIRONMENTS.key, Sets.newHashSet()));
        return set;
    }

    public List<EnvironmentInfo> getEnvironmentList() {
        Set<String> environmentIds = getRecordEnvironments();
        List<EnvironmentInfo> list = Lists.newArrayList();
        for (String id : environmentIds) {
            if (StringHelper.isValidUUID(id)) {
                EnvironmentInfo info = TenantPref.getClosed(mContext.get(), id).getEnvironment();
                if (info.getJoinTime() > 0) {
                    list.add(info);
                }
            }
        }
        return list;
    }

    public EnvironmentInfo getCurrentEnvironment() {
        return TenantPref.getClosed(mContext.get(), getCurrentTenantId()).getEnvironment();
    }

    public List<EnvironmentInfo> getDeveloperEnvironmentList() {
        Set<String> environmentIds = getDeveloperEnvironment();
        List<EnvironmentInfo> list = Lists.newArrayList();
        for (String id : environmentIds) {
            EnvironmentInfo info = TenantPref.getClosed(mContext.get(), id).getEnvironment();
            list.add(info);
        }
        return list;
    }

    public TokenPref clearByKey(PreferencesKey key) {
        boolean success = sp.edit().remove(key.getKey()).commit();
        return this;
    }

    public String getSocketQuery() {
        String query = "deviceId=" + getDeviceId()
            + "&" + "name=" + getDeviceType()
            + "&" + "osType=" + getOsType()
            + "&" + "index=" + "2"
            + "&" + "enableAck=1"
            + "&" + "language=" + getLanguage();
        CELog.i("socket query str:: " + query);
        return query;
    }

    public void setCurrentAppVersionFromServer(int version) {
        boolean success = sp.edit().putInt(PreferencesKey.APP_SERVER_VERSION.getKey(), version).commit();
    }

    public int getCurrentAppVersionFromServer() {
        return sp.getInt(PreferencesKey.APP_SERVER_VERSION.key, 0);
    }

    public void setLocationVersion(String locationVersion) {
        sp.edit().putString(PreferencesKey.LOCATION_VERSION.key, locationVersion).apply();
    }

    public String getLocationVersion() {
        return sp.getString(PreferencesKey.LOCATION_VERSION.key, "");
    }

    public void setPreloadStep(int step) {
        boolean success = sp.edit().putInt(PreferencesKey.PRELOAD_STEP.key, step).commit();
    }

    public int getPreloadStep() {
        return sp.getInt(PreferencesKey.PRELOAD_STEP.key, 0);
    }

    public String getThemeItemInfo() {
        return sp.getString(PreferencesKey.THEME_ITEM.key, "Default");
    }

    public void setThemeItemInfo(String themeItem) {
        boolean success = sp.edit().putString(PreferencesKey.THEME_ITEM.key, themeItem).commit();
    }

    public int getCreateTenantPermission() {
        return sp.getInt(PreferencesKey.PERMISSION_CREATE_TENANT.key, -1);
    }

    public void setCreateTenantPermission(int permission) {
        boolean success = sp.edit().putInt(PreferencesKey.PERMISSION_CREATE_TENANT.key, permission).commit();
    }

    public int getJoinTenantPermission() {
        return sp.getInt(PreferencesKey.PERMISSION_JOIN_TENANT.key, -1);
    }

    public void setJoinTenantPermission(int permission) {
        boolean success = sp.edit().putInt(PreferencesKey.PERMISSION_JOIN_TENANT.key, permission).commit();
    }
}
