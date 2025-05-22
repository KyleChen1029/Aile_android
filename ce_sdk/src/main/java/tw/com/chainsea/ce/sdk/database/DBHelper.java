package tw.com.chainsea.ce.sdk.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.SdkLib;


/**
 * DBHelper
 * Created by 90Chris on 2015/7/5.
 */
public class DBHelper extends DataBaseVersionHelper {
    public DBHelper(String name) {
        super(SdkLib.getAppContext(), name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CELog.e(String.format("DataBase Version [oldVersion:: %s, newVersion:: %s]", oldVersion, newVersion));
        switch (oldVersion){
            case 49:
                v49_to_v50(db);
            case 50:
                v50_to_v51(db);
            case 51:
            case 52:
                v52_to_v53(db);
            case 53:
                v53_to_v54(db);
            case 54:
                v54_to_v55(db);
            case 55:
                v55_to_v56(db);
            case 56:
                v56_to_v57(db);
            case 57:
                v57_to_v58(db);
            case 58:
            case 59:
                v59_to_v60(db);
            case 60:
                v60_to_v61(db);
            case 61:
            case 62:
            case 63:
            case 64:
            case 65:
                v65_to_v66(db);
            case 66:
                v66_to_v67(db);
            case 67:
            case 68:
            case 69:
                v69_to_v70(db);
            case 70:
                v70_to_v71(db);
            case 71:
            case 72:
                v71_to_v73(db);
            case 73:
            case 74:
                v73_to_v75(db);
            case 75:
                v75_to_v76(db);
            case 76:
                v76_to_v77(db);
            case 77:
                v77_to_v78(db);
            case 78:
                v78_to_v79(db);
            case 79:
                v79_to_v80(db);
            case 80:
                v80_to_v81(db);
            case 81:
                v81_to_v82(db);
            case 82:
                v82_to_v83(db);
            case 83:
                v83_to_v84(db);
        }
    }

    /**
     *
     *
     * pragma auto_vacuum = 0|1;  没有数据有效，有数据无效，顾应该在创建表之前
     * pragma cache_size=9000；default_cache_size
     * pragma page_size = bytes;
     * pragma synchronous = Full/Normal/off
     * pragma temp_store = Memory/File/Default
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setPageSize(32768);
        db.disableWriteAheadLogging();
        Cursor c1 = db.rawQuery("PRAGMA journal_mode=WAL", null);
        Cursor c2 = db.rawQuery("PRAGMA synchronous=Normal", null);
        Cursor c3 = db.rawQuery("PRAGMA cache_size=27000", null);
        Cursor c4 = db.rawQuery("PRAGMA temp_store=Memory", null);
        c1.close();
        c2.close();
        c3.close();
        c4.close();
    }



    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public synchronized void close() {
        super.close();
    }


}
