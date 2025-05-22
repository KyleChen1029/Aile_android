package tw.com.chainsea.ce.sdk.database.sp;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import tw.com.chainsea.ce.sdk.bean.tenant.EnvironmentInfo;

/**
 * current by evan on 12/14/20
 *
 * @author Evan Wang
 * date 12/14/20
 */
public class TenantPref {
    private static TenantPref INSTANCE;
    private static SharedPreferences sp;

    public enum PreferencesKey {
        TENANT_UUID("TENANT_UUID"),
        TENANT_CODE("TENANT_CODE"),
        TENANT_NAME("TENANT_NAME"),
        TENANT_URL("TENANT_URL"),
        ACCOUNT_ID("ACCOUNT_ID"),
        ACCOUNT_NUMBER("ACCOUNT_NUMBER"),
        ACCOUNT_PSW("ACCOUNT_PSW"),
        JOIN_TIME("JOIN_TIME");

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

    private TenantPref(Context context) {
        String uuid = TokenPref.getInstance(context).getCurrentTenantId();
        sp = context.getSharedPreferences("tenant_" + uuid, Context.MODE_PRIVATE);
    }

    private TenantPref(Context context, String uuid) {
        sp = context.getSharedPreferences("tenant_" + uuid, Context.MODE_PRIVATE);
    }

    public static TenantPref getClosed(Context context, String uuid) {
        return new TenantPref(context, uuid);
    }

    public static TenantPref newInstance(Context context) {
        INSTANCE = new TenantPref(context);
        return INSTANCE;
    }

    public static TenantPref newInstance(Context context, String uuid) {
        INSTANCE = new TenantPref(context, uuid);
        return INSTANCE;
    }

    public static void init(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TenantPref(context);
        }
    }

    public static TenantPref getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TenantPref(context);
        }
        return INSTANCE;
    }

    public TenantPref setAccountID(String id) {
        sp.edit().putString(PreferencesKey.ACCOUNT_ID.key, id).apply();
        return this;
    }

    public TenantPref setAccountNumber(String id) {
        sp.edit().putString(PreferencesKey.ACCOUNT_NUMBER.key, id).apply();
        return this;
    }

    public TenantPref setAccountPsw(String psw) {
        sp.edit().putString(PreferencesKey.ACCOUNT_PSW.key, psw).apply();
        return this;
    }

    public EnvironmentInfo getEnvironment() {
        String uuid = sp.getString(PreferencesKey.TENANT_UUID.key, "");
        String code = sp.getString(PreferencesKey.TENANT_CODE.key, "");
        String url = sp.getString(PreferencesKey.TENANT_URL.key, "");
        String name = sp.getString(PreferencesKey.TENANT_NAME.key, "");
        String accountId = sp.getString(PreferencesKey.ACCOUNT_ID.key, "");
        String accountNumber = sp.getString(PreferencesKey.ACCOUNT_NUMBER.key, "");
        String accountPsw = sp.getString(PreferencesKey.ACCOUNT_PSW.key, "");
        long joinTime = sp.getLong(PreferencesKey.JOIN_TIME.key, -1);
        return new EnvironmentInfo(uuid, code, url, name, accountId, accountNumber, accountPsw, joinTime);
    }

    public TenantPref setEnvironment(EnvironmentInfo info) {
        String accountId = sp.getString(PreferencesKey.ACCOUNT_ID.key, "");
        String accountNumber = sp.getString(PreferencesKey.ACCOUNT_NUMBER.key, "");
        String accountPsw = sp.getString(PreferencesKey.ACCOUNT_PSW.key, "");
        long joinTime = sp.getLong(PreferencesKey.JOIN_TIME.key, -1);
        sp.edit()
            .putString(PreferencesKey.TENANT_UUID.key, info.getId())
            .putString(PreferencesKey.TENANT_CODE.key, info.getCode())
            .putString(PreferencesKey.TENANT_URL.key, info.getUrl())
            .putString(PreferencesKey.TENANT_NAME.key, info.getName())
            .putString(PreferencesKey.ACCOUNT_ID.key, accountId)
            .putString(PreferencesKey.ACCOUNT_NUMBER.key, accountNumber)
            .putString(PreferencesKey.ACCOUNT_PSW.key, accountPsw)
            .putLong(PreferencesKey.JOIN_TIME.key, joinTime)
            .apply();
        return this;
    }

    public TenantPref setJoinTime(long joinTime) {
        boolean success = sp.edit().putLong(PreferencesKey.JOIN_TIME.key, joinTime).commit();
        return this;
    }

    public void delete() {
        for (PreferencesKey k : PreferencesKey.values()) {
            boolean isDeleted = sp.edit().remove(k.key).commit();
        }
    }

    public static void clearSharedPreferences(Context ctx, String uuid) {
        File dir = new File(ctx.getFilesDir().getParent() + "/shared_prefs/" + "tenant_" + uuid + ".xml");
        boolean success = ctx.getSharedPreferences("tenant_" + uuid.replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
        boolean isDeleted = dir.delete();
//        String[] children = dir.list();
//        for (int i = 0; i < children.length; i++) {
//            // clear each preference file
//            ctx.getSharedPreferences(children[i].replace(".xml", ""), Context.MODE_PRIVATE).edit().clear().commit();
//            //delete the file
//            new File(dir, children[i]).delete();
//        }
    }

}
