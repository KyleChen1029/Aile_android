package tw.com.chainsea.android.common;

import android.content.Context;

import tw.com.chainsea.android.common.version.VersionHelper;

public class CommonLib {
    private static Context appContext;
    private String versionName;
    private String versionCode;
    private static CommonLib instance;

    public static String packageName;

    public static void init(Context context){
        appContext = context;
        packageName = context.getPackageName();
    }
    public static Context getAppContext(){
        return appContext;
    }
    public static CommonLib getInstant(){
        if(instance == null) instance = new CommonLib();
        return instance;
    }
    public String getVersionCode(){
        if(versionCode == null) {
            versionCode = VersionHelper.getVersionCode(appContext);
        }
        return versionCode;
    }

    public String getVersionName(){
        if(versionName == null) {
            versionName = VersionHelper.getVersionName(appContext);
        }
        return versionName;
    }
}