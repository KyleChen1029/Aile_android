package tw.com.chainsea.chat.keyboard.emoticon.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.chat.keyboard.emoticon.EmoticonBean;


public class EmoticonDBHelper {
    private final static String TAG = "EmoticonDBHelper";
    private static final int VERSION = 5;

    private static final String DATABASE_NAME = "xhsemoticons.db";
    private static final String TABLE_NAME_EMOTICONS = "emoticons";
    private static final String TABLE_NAME_EMOTICONSET = "emoticonset";

    private final DBOpenHelper mOpenDbHelper;

    public EmoticonDBHelper(Context context) {
        mOpenDbHelper = new DBOpenHelper(context);
    }

    public synchronized String getUriByTag(String tag) {
        SQLiteDatabase db = mOpenDbHelper.getReadableDatabase();

        // 使用 query 方法
        String[] columns = {TableColumns.EmoticonColumns.MSG_URI, TableColumns.EmoticonColumns.ICON_URI};
        String selection = TableColumns.EmoticonColumns.TAG + " = ?";
        String[] selectionArgs = {tag};

        Cursor cursor = db.query(TABLE_NAME_EMOTICONS, columns, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String msgUri = cursor.getString(cursor.getColumnIndexOrThrow(TableColumns.EmoticonColumns.MSG_URI));
            if (msgUri != null) {
                cursor.close();
                return msgUri;
            }
            String result = cursor.getString(cursor.getColumnIndexOrThrow(TableColumns.EmoticonColumns.ICON_URI));
            cursor.close();
            return result;
        }

        return null;
    }

    public synchronized ArrayList<EmoticonBean> queryEmoticonBeanList(String selection, String[] selectionArgs) {
        ArrayList<EmoticonBean> beanList = new ArrayList<>();
        try {
            SQLiteDatabase db = mOpenDbHelper.getReadableDatabase();
            String[] columns = {
                TableColumns.EmoticonColumns.EVENT_TYPE,
                TableColumns.EmoticonColumns.TAG,
                TableColumns.EmoticonColumns.NAME,
                TableColumns.EmoticonColumns.ICON_URI
            };

            // 使用 query 方法避免 SQL 注入
            Cursor cursor = db.query(
                TABLE_NAME_EMOTICONS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            );

            // 解析資料
            while (cursor.moveToNext()) {
                long eventType = cursor.getLong(cursor.getColumnIndexOrThrow(TableColumns.EmoticonColumns.EVENT_TYPE));
                String tag = cursor.getString(cursor.getColumnIndexOrThrow(TableColumns.EmoticonColumns.TAG));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(TableColumns.EmoticonColumns.NAME));
                String iconUri = cursor.getString(cursor.getColumnIndexOrThrow(TableColumns.EmoticonColumns.ICON_URI));
                EmoticonBean bean = new EmoticonBean(eventType, iconUri, tag, name);
                beanList.add(bean);
            }
            cursor.close();
        } catch (Exception e) {
            CELog.e("EmoticonDBHelper.queryEmoticonBeanList error", e);
        }
        return beanList;
    }

    public synchronized ArrayList<EmoticonBean> queryAllEmoticonBeans() {
        String sql = "SELECT * FROM " + TABLE_NAME_EMOTICONS;
        return queryEmoticonBeanList(sql, null);
    }

    public synchronized void cleanup() {
        mOpenDbHelper.close();
    }

    private static class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        private void createEmoticonsTable(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME_EMOTICONS + " ( " +
                TableColumns.EmoticonColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TableColumns.EmoticonColumns.EVENT_TYPE + " INTEGER, " +
                TableColumns.EmoticonColumns.TAG + " TEXT NOT NULL UNIQUE, " +
                TableColumns.EmoticonColumns.NAME + " TEXT, " +
                TableColumns.EmoticonColumns.ICON_URI + " TEXT NOT NULL, " +
                TableColumns.EmoticonColumns.MSG_URI + " TEXT, " +
                TableColumns.EmoticonColumns.EMOTICON_SET_NAME + " TEXT NOT NULL);");


            db.execSQL(new StringBuilder().append("CREATE TABLE ").append(TABLE_NAME_EMOTICONSET).append(" ( ").append(TableColumns.EmoticonSetColumns._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ").append(TableColumns.EmoticonSetColumns.NAME).append(" TEXT NOT NULL UNIQUE, ").append(TableColumns.EmoticonSetColumns.LINE).append(" INTEGER, ").append(TableColumns.EmoticonSetColumns.ROW).append(" INTEGER, ").append(TableColumns.EmoticonSetColumns.ICON_URI).append(" TEXT, ").append(TableColumns.EmoticonSetColumns.IS_SHOW_DEL_BTN).append(" BOOLEAN, ").append(TableColumns.EmoticonSetColumns.IS_SHOWN_NAME).append(" BOOLEAN, ").append(TableColumns.EmoticonSetColumns.ITEM_PADDING).append(" INTEGER, ").append(TableColumns.EmoticonSetColumns.HORIZONTAL_SPACING).append(" INTEGER, ").append(TableColumns.EmoticonSetColumns.VERTICAL_SPACING).append(" TEXT);").toString());
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            createEmoticonsTable(sqLiteDatabase);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int currentVersion) {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EMOTICONS);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EMOTICONSET);
            onCreate(sqLiteDatabase);
        }
    }
}
