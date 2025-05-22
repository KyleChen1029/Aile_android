package tw.com.chainsea.ce.sdk.database.sp;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import tw.com.chainsea.android.common.json.JsonHelper;

/**
 * UserPref
 * Created by 90Chris on 2016/6/14.
 */
public class UserPref {
    private static UserPref INSTANCE;
    private final SharedPreferences sp;

    public enum PreferencesKey {
        PERSON_ROOM_ID("PERSON_ROOM_ID"),
        USER_NAME("USER_NAME"),
        USER_AVATAR_ID("USER_AVATAR_ID"),
        USER_HOME_PAGE_BACKGROUND_URL("USER_HOME_PAGE_BACKGROUND_URL"),
        USER_TYPE("USER_TYPE"),
        CURRENT_ROOM_ID("CURRENT_ROOM_ID"),
        CURRENT_ROOM_IDS("CURRENT_ROOM_IDS"),
        CURRENT_CONSULTATION_ROOM_ID("CURRENT_CONSULTATION_ROOM_ID"),
        BRAND("BRAND"),
        LOVE_LABEL_ID("LOVE_LABEL_ID"),
        ROOM_RECENT_LAST_REFRESH_TIME("ROOM_RECENT_LAST_REFRESH_TIME_v15"),
        HAS_BUSINESS_SYSTEM("HAS_BUSINESS_SYSTEM"),
        HAS_BIND_EMPLOYEE("HAS_BIND_EMPLOYEE"),
        IS_SERVICE_ROOM_INTRO_CHANNEL("IS_SERVICE_ROOM_INTRO_CHANNEL"),
        IS_MESSAGE_NEW_SOUND_NOTIFY("IS_MESSAGE_NEW_SOUND_NOTIFY"),
        SERVICE_NUMBER_SECTIONED("SERVICE_NUMBER_SECTIONED"),
        HAS_TREATMENTS_1_14_0_BUSINESS_EXECUTOR("HAS_TREATMENTS_1_14_0_BUSINESS_EXECUTOR"),
        MOBILE_VISIBLE("mobileVisible");

        String key;

        PreferencesKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public static void newInstance(Context context) {
        INSTANCE = new UserPref(context);
    }

    public static UserPref getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UserPref(context);
        }
        return INSTANCE;
    }

    private UserPref(Context context) {
        String userId = TokenPref.getInstance(context).getUserId();
        sp = context.getSharedPreferences("user_" + userId, Context.MODE_PRIVATE);
    }

    /**
     * Current chat room ID
     */
    public String getCurrentRoomId() {
        return sp.getString(PreferencesKey.CURRENT_ROOM_ID.getKey(), "");
    }

    public List<String> getCurrentRoomIds() {
        Type typeToken = new TypeToken<List<String>>() {}.getType();
        String ids = sp.getString(PreferencesKey.CURRENT_ROOM_IDS.getKey(), "");
        List<String> idList =  JsonHelper.getInstance().from(ids, typeToken);
        if (idList == null) {
            idList = new ArrayList<>();
        }
        return idList;
    }

    public boolean isInCurrentRoomId(String roomId) {
        List<String> idList = getCurrentRoomIds();
        return idList.contains(roomId);
    }

    public void setCurrentRoomIds(String roomId) {
        List<String> idList = getCurrentRoomIds();
        if (!idList.contains(roomId)) {
            idList.add(roomId);
        }
        sp.edit().putString(PreferencesKey.CURRENT_ROOM_IDS.getKey(), JsonHelper.getInstance().toJson(idList)).apply();
    }

    public void removeCurrentRoomId(String roomId) {
        List<String> idList = getCurrentRoomIds();
        idList.remove(roomId);
        sp.edit().putString(PreferencesKey.CURRENT_ROOM_IDS.getKey(), JsonHelper.getInstance().toJson(idList)).apply();
    }

    public void removeAllCurrentRoomId() {
        sp.edit().putString(PreferencesKey.CURRENT_ROOM_IDS.getKey(), "").apply();
    }

    public void setCurrentRoomId(String currentRoomId) {
        sp.edit().putString(PreferencesKey.CURRENT_ROOM_ID.getKey(), currentRoomId).apply();
    }

    public String getCurrentConsultationRoomId() {
        return sp.getString(PreferencesKey.CURRENT_CONSULTATION_ROOM_ID.getKey(), "");
    }

    public void setCurrentConsultationRoomId(String currentConsultationRoomId) {
        sp.edit().putString(PreferencesKey.CURRENT_CONSULTATION_ROOM_ID.getKey(), currentConsultationRoomId).apply();
    }


    /**
     * Update the number of corner marks
     */

    public void setMobileVisible(boolean mobileVisible) {
        sp.edit().putBoolean(PreferencesKey.MOBILE_VISIBLE.getKey(), mobileVisible).apply();
    }

    public boolean getMobileVisible() {
        return sp.getBoolean(PreferencesKey.MOBILE_VISIBLE.getKey(), false);
    }

    public void setBrand(int brand) {
        sp.edit().putInt(PreferencesKey.BRAND.getKey(), brand).apply();
    }

    public int getBrand() {
        return sp.getInt(PreferencesKey.BRAND.getKey(), 0);
    }

    public String getPersonRoomId() {
        return sp.getString(PreferencesKey.PERSON_ROOM_ID.getKey(), "");
    }

    public UserPref setPersonRoomId(String personRoomId) {
        sp.edit().putString(PreferencesKey.PERSON_ROOM_ID.getKey(), personRoomId).apply();
        return this;
    }

    public String getUserName() {
        return sp.getString(PreferencesKey.USER_NAME.getKey(), "");
    }

    public UserPref setUserName(String userName) {
        sp.edit().putString(PreferencesKey.USER_NAME.getKey(), userName).apply();
        return this;
    }

    public String getUserAvatarId() {
        return sp.getString(PreferencesKey.USER_AVATAR_ID.getKey(), "");
    }

    public UserPref setUserAvatarId(String userAvatarId) {
        sp.edit().putString(PreferencesKey.USER_AVATAR_ID.getKey(), userAvatarId).apply();
        return this;
    }


    public String getUserHomePageBackgroundUrl() {
        return sp.getString(PreferencesKey.USER_HOME_PAGE_BACKGROUND_URL.getKey(), "");
    }

    public UserPref setUserHomePageBackgroundUrl(String userHomePageBackgroundUrl) {
        sp.edit().putString(PreferencesKey.USER_HOME_PAGE_BACKGROUND_URL.getKey(), userHomePageBackgroundUrl).apply();
        return this;
    }

    /**
     * Service account chat room guide channel button
     */
    public boolean isServiceRoomIntroChannel(String tag) {
        return sp.getBoolean(PreferencesKey.IS_SERVICE_ROOM_INTRO_CHANNEL.getKey() + "_" + tag, false);
    }

    public void setServiceRoomIntroChannel(boolean isServiceRoomIntroChannel, String tag) {
        sp.edit().putBoolean(PreferencesKey.IS_SERVICE_ROOM_INTRO_CHANNEL.getKey() + "_" + tag, isServiceRoomIntroChannel)
                .apply();
    }

    /**
     * Whether the business function is enabled
     */
    public boolean hasBusinessSystem() {
        return false;
    }

    public UserPref setHasBusinessSystem(boolean hasBusinessSystem) {
        sp.edit().putBoolean(PreferencesKey.HAS_BUSINESS_SYSTEM.getKey(), hasBusinessSystem)
                .apply();
        return this;
    }

    public UserPref setHasBindEmployee(boolean hasBindEmployee) {
        sp.edit().putBoolean(PreferencesKey.HAS_BIND_EMPLOYEE.getKey(), hasBindEmployee)
                .apply();
        return this;
    }

    public Set<String> getServiceNumberSectioned() {
        String json = sp.getString(PreferencesKey.SERVICE_NUMBER_SECTIONED.getKey(), "[]");
        return JsonHelper.getInstance().fromToSet(json, String[].class);
    }

    public void setServiceNumberSectioned(Set<String> serviceNumberSectioned) {
        sp.edit().putString(PreferencesKey.SERVICE_NUMBER_SECTIONED.getKey(), JsonHelper.getInstance().toJson(serviceNumberSectioned))
                .apply();
    }

    /**
     * Delete data according to the set key
     */
    public UserPref clearByKey(PreferencesKey key) {
        sp.edit().remove(key.getKey()).apply();
        return this;
    }

    public void saveLoveLabelId(String labelId) {
        sp.edit().putString(PreferencesKey.LOVE_LABEL_ID.getKey(), labelId)
                .apply();
    }

    public String getLoveLabelId() {
        return sp.getString(PreferencesKey.LOVE_LABEL_ID.getKey(), "");
    }

    public void saveUserType(String userType) {
        sp.edit().putString(PreferencesKey.USER_TYPE.getKey(), userType)
                .apply();
    }

    public String getUserType() {
        return sp.getString(PreferencesKey.USER_TYPE.getKey(), "");
    }

    public boolean hasTreatments_1_14_0_BusinessExecutor(String source) {
        return sp.getBoolean(source + "_" + PreferencesKey.HAS_TREATMENTS_1_14_0_BUSINESS_EXECUTOR.getKey(), false);
    }

    public void setTreatments_1_14_0_BusinessExecutor(String source, boolean status) {
        sp.edit().putBoolean(source + "_" + PreferencesKey.HAS_TREATMENTS_1_14_0_BUSINESS_EXECUTOR.getKey(), status).apply();
    }

}
