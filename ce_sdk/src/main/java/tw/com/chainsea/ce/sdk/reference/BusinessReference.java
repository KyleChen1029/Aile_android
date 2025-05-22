package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.business.BusinessEntity;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;

/**
 * current by evan on 2020-09-03
 *
 * @author Evan Wang
 * @date 2020-09-03
 */
public class BusinessReference extends AbsReference {


    // select count(*) from ecp_business
    // where (end_timestamp>0 and end_timestamp < date('now'))
    // or  (end_time <  strftime('%Y-%m-%d', 'now') or end_time < strftime('%Y/%m/%d', 'now'))
    public static int getExpiredCount(SQLiteDatabase db) {
        int count;
        long now = System.currentTimeMillis();
        SQLiteDatabase database = (db == null ? DBManager.getInstance().openDatabase() : db);

        Cursor cursor = database.query(
                DBContract.BusinessEntry.TABLE_NAME, // 表名稱
                new String[]{DBContract.BusinessEntry._ID}, // 查詢欄位：我們只需要 _id
                DBContract.BusinessEntry.COLUMN_END_TIMESTAMP + ">0 AND "
                        + DBContract.BusinessEntry.COLUMN_END_TIMESTAMP + "<?", // 查詢條件
                new String[]{String.valueOf(now)}, // 查詢參數
                null, // 無分組
                null, // 無排序
                null);  // 無排序

            count = cursor.getCount(); // 計算符合條件的行數
        cursor.close();
        return count;
    }

    public static long maxInteractionTime(SQLiteDatabase db, String businessId) {
        try {

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.ChatRoomEntry.TABLE_NAME + " AS cr INNER JOIN " +
                            DBContract.MessageEntry.TABLE_NAME + " AS cm ON cr." +
                            DBContract.ChatRoomEntry._ID + " = cm." + DBContract.MessageEntry.COLUMN_ROOM_ID,
                    new String[]{"MAX(cr." + DBContract.ChatRoomEntry.COLUMN_UPDATE_TIME + ", cm." + DBContract.MessageEntry.COLUMN_SEND_TIME + ") AS update_time"},
                    "cr." + DBContract.ChatRoomEntry.COLUMN_BUSINESS_ID + " =?",
                    new String[]{businessId},
                    null,
                    null,
                    "update_time",
                    "1"
            );


            if (cursor.getCount() <= 0) {
                cursor.close();
                return -1L;
            }
            long max = -1L;
            while (cursor.moveToNext()) {
                max = Tools.getDbLong(cursor, "update_time");
            }
            cursor.close();
            return max;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return -1L;
        }
    }

    public static BusinessEntity find(SQLiteDatabase db, String businessId) {
        try {

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.BusinessEntry.TABLE_NAME,
                    null,
                    DBContract.BusinessEntry._ID + " =?",
                    new String[]{businessId},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();

            String id = Tools.getDbString(cursor, DBContract.BusinessEntry._ID);
            long max = maxInteractionTime(db, id);
            BusinessEntity entity = BusinessEntity.formatByCursor(cursor, max);
            cursor.close();
            return entity;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }


    public static Map<String, String> findExecutorIdMappingByIds(SQLiteDatabase db, Set<String> businessIds) {
        try {
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.BusinessEntry.TABLE_NAME,
                    null,
                    DBContract.BusinessEntry._ID + " IN(" + concatStrings("'", ",", businessIds.toArray()) +")",
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return Maps.newHashMap();
            }

            Map<String, String> data = Maps.newHashMap();
            while (cursor.moveToNext()) {
                String id = Tools.getDbString(cursor, DBContract.BusinessEntry._ID);
                String executorId = Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_EXECUTOR_ID);
                data.put(id, executorId);
            }
            return data;

        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Maps.newHashMap();
        }

    }


    public static BusinessEntity findExecutorAvatarAndNameById(SQLiteDatabase db, String executorId) {
        try {

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.BusinessEntry.TABLE_NAME,
                    new String[]{DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID, DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME},
                    DBContract.BusinessEntry.COLUMN_EXECUTOR_ID + " =?",
                    new String[]{executorId},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            BusinessEntity entity = BusinessEntity.Build()
                    .executorId(executorId)
                    .executorAvatarId(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID))
                    .executorName(Tools.getDbString(cursor, DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME))
                    .build();
            cursor.close();
            return entity;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    public static List<BusinessEntity> findAll(SQLiteDatabase db) {
        try {
            List<BusinessEntity> entities = Lists.newArrayList();
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.BusinessEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }

            while (cursor.moveToNext()) {
                String id = Tools.getDbString(cursor, DBContract.BusinessEntry._ID);
                long max = maxInteractionTime(db, id);
                entities.add(BusinessEntity.formatByCursor(cursor, max));
            }
            cursor.close();

            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public static boolean save(SQLiteDatabase db, List<BusinessEntity> entities) {
        try {
            boolean result = true;
            for (BusinessEntity entity : entities) {
                ContentValues values = BusinessEntity.getContentValues(entity);
                long _id = (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.BusinessEntry.TABLE_NAME, null, values);
                result = result && _id > 0;
            }
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static void updateEndTimestampById(SQLiteDatabase db, String id, long endTimestamp) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBContract.BusinessEntry.COLUMN_END_TIMESTAMP, endTimestamp);
            String whereClause = DBContract.BusinessEntry._ID + " = ?";
            String[] whereArgs = new String[]{id};
            (db == null ? DBManager.getInstance().openDatabase() : db).update(DBContract.BusinessEntry.TABLE_NAME, contentValues, whereClause, whereArgs);

        } catch (Exception ignored) {
        }
    }

}
