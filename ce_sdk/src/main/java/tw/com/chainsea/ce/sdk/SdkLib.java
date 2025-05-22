package tw.com.chainsea.ce.sdk;

import android.content.Context;

import tw.com.chainsea.ce.sdk.database.DBHelper;
import tw.com.chainsea.ce.sdk.database.DBManager;

public class SdkLib {
    private static Context appContext;
    public static DBHelper dbHelper;

    public static void init(Context context) {
        appContext = context;
        DBManager.getInstance().initDB();
//        dbHelper = new DBHelper(TokenPref.getInstance(SdkLib.getAppContext()).getUserId()+".db");
//        dbHelper.setWriteAheadLoggingEnabled(false);
    }

    public static Context getAppContext() {
        return appContext;
    }
}
