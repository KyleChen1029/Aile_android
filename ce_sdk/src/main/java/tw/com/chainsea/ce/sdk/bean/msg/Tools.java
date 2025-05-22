package tw.com.chainsea.ce.sdk.bean.msg;

import android.database.Cursor;
import android.text.TextUtils;

/**
 * Tools for system
 * Created by 90Chris on 2014/11/12.
 */
public class Tools {
    public static String generateMessageId() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static String generateTimeMessageId(long time) {
        return String.valueOf(time);
    }

    public static String getDbString(Cursor cursor, String columnName) {
        String dbString = cursor.getString(checkIndex(cursor, columnName));
        if (TextUtils.isEmpty(dbString)) {
            dbString = "";
        }
        return dbString;
    }

    public static int getDbInt(Cursor cursor, String columnName) {
        return cursor.getInt(checkIndex(cursor, columnName));
    }

    public static long getDbLong(Cursor cursor, String columnName) {
        return cursor.getLong(checkIndex(cursor, columnName));
    }

    public static String getDbString(Cursor cursor, int index) {
        return cursor.getString(index);
    }

    public static int getDbInt(Cursor cursor, int index) {
        return cursor.getInt(index);
    }

    public static long getDbLong(Cursor cursor, int index) {
        return cursor.getLong(index);
    }

    public static int checkIndex(Cursor cursor, String columnName) throws RuntimeException {
        return cursor.getColumnIndex(columnName);
    }
}
