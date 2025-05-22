package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-04-27
 *
 * @author Evan Wang
 * @date 2020-04-27
 */
public class FriendsLabelRelReference {


    public synchronized static boolean saveByLabel(SQLiteDatabase db, String labelId, Set<String> userIds) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            db.beginTransaction();
            boolean status = true;
            for (String userId : userIds) {
                ContentValues values = new ContentValues();
                values.put(DBContract.FriendsLabelRel.COLUMN_ID, userId + labelId);
                values.put(DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID, userId);
                values.put(DBContract.FriendsLabelRel.COLUMN_LABEL_ID, labelId);
                status = status && db.replace(DBContract.FriendsLabelRel.TABLE_NAME, null, values) > 0L;
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }finally {
            db.endTransaction();
        }
    }

    public synchronized static boolean saveByLabel(SQLiteDatabase db, Label label) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            db.beginTransaction();
            boolean status = true;
            if (label != null && label.getUserIds() != null && !label.getUserIds().isEmpty()) {
                for (String userId : label.getUserIds()) {
                    ContentValues values = new ContentValues();
                    values.put(DBContract.FriendsLabelRel.COLUMN_ID, userId + label.getId());
                    values.put(DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID, userId);
                    values.put(DBContract.FriendsLabelRel.COLUMN_LABEL_ID, label.getId());
                    status = status && db.replace(DBContract.FriendsLabelRel.TABLE_NAME, null, values) > 0L;
                }
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }finally {
            db.endTransaction();
        }
    }


    public synchronized static void deleteByAccountId(SQLiteDatabase db, String accountId) {
        db = db == null ? DBManager.getInstance().openDatabase() : db;
        try {
            db.beginTransaction();
            String whereClause = DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + " =?";
            boolean isDeleted = db.delete(DBContract.FriendsLabelRel.TABLE_NAME, whereClause, new String[]{accountId}) > 0;
            db.setTransactionSuccessful();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }finally {
            db.endTransaction();
        }
    }


    /**
     * 依照 label id 刪除關聯資料
     *
     * @param db
     * @param labelId
     * @return
     */
    public synchronized static boolean deleteByLabelId(SQLiteDatabase db, String labelId) {
        try {
            db.beginTransaction();
            String whereClause = DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " = ?";
            String[] whereArgs = new String[]{labelId};
            boolean isDeleted = (db == null ? DBManager.getInstance().openDatabase() : db).delete(DBContract.FriendsLabelRel.TABLE_NAME, whereClause, whereArgs) > 0;
            db.setTransactionSuccessful();
            return isDeleted;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }finally {
            db.endTransaction();
        }
    }


    /**
     * 依照 label id 刪除關聯資料
     * 如果關聯表已經空連動刪除 label
     *
     * @param db
     * @param userId
     * @return
     */
    public synchronized static boolean deleteByLabelIdAndUserId(SQLiteDatabase db, String labelId, String userId) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }
            boolean status = true;
//            String labelId = null;
//            String sql = " SELECT l.* FROM " + DBContract.LabelEntry.TABLE_NAME + " AS l " +
//                    " INNER JOIN " + DBContract.FriendsLabelRel.TABLE_NAME + " AS f " +
//                    " ON l." + DBContract.LabelEntry._ID + "= f." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID +
//                    " WHERE l." + DBContract.LabelEntry.COLUMN_READ_ONLY + "=? " +
//                    " AND f." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + "=?";


//            Cursor cursor = db.rawQuery(sql, new String[]{"true", userId});
            Cursor cursor = db.query(
                    DBContract.LabelEntry.TABLE_NAME + " AS l " +
                            " INNER JOIN " + DBContract.FriendsLabelRel.TABLE_NAME + " AS f " +
                            " ON l." + DBContract.LabelEntry._ID + "= f." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID,
                    new String[]{"l.*"},
                    "l." + DBContract.LabelEntry.COLUMN_READ_ONLY + "=? " +
                            " AND f." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + "=?",
                    new String[]{"true", userId},
                    null,
                    null,
                    null
            );
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                labelId = Tools.getDbString(cursor, DBContract.LabelEntry._ID);

                String whereClause = DBContract.FriendsLabelRel.COLUMN_ID + " =?";
                String[] whereArgs = new String[]{userId + labelId};
                status = db.delete(DBContract.FriendsLabelRel.TABLE_NAME, whereClause, whereArgs) > 0;
            }
            cursor.close();
            if (!Strings.isNullOrEmpty(labelId)) {
//                sql = "SELECT * FROM " + DBContract.FriendsLabelRel.TABLE_NAME +
//                        " WHERE " + DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " =?";
//                cursor = db.rawQuery(sql, new String[]{labelId});
                Cursor retCursor = db.query(
                        DBContract.FriendsLabelRel.TABLE_NAME,
                        null,
                        DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " =?",
                        new String[]{labelId},
                        null,
                        null,
                        null
                );
                if (retCursor.getCount() == 0) {
                    String whereClause = DBContract.LabelEntry._ID + " =?";
                    status = status && db.delete(DBContract.LabelEntry.TABLE_NAME, whereClause, new String[]{labelId}) > 0;
                }
                retCursor.close();
            }

            return status;
        } catch (Exception e) {
            return false;
        }
    }
    public static List<Label> findRelLabelByAccountId(SQLiteDatabase db, String accountId) {
        try {
//            String sql = "SELECT * FROM " + DBContract.FriendsLabelRel.TABLE_NAME + " AS flr "
//                    + " INNER JOIN " + DBContract.LabelEntry.TABLE_NAME + " AS l"
//                    + " ON l." + DBContract.LabelEntry._ID + "=flr." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID
//                    + " WHERE flr." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + " =?";
//            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).rawQuery(sql, new String[]{accountId});
            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).query(
                    DBContract.FriendsLabelRel.TABLE_NAME + " AS flr "
                            + " INNER JOIN " + DBContract.LabelEntry.TABLE_NAME + " AS l"
                            + " ON l." + DBContract.LabelEntry._ID + "=flr." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID,
                    null,
                    "flr." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + " =?",
                    new String[]{accountId},
                    null,
                    null,
                    null
            );
            if (cursor.getCount() == 0) {
                cursor.close();
                return Lists.newArrayList();
            }

            List<Label> labels = Lists.newArrayList();
            while (cursor.moveToNext()) {
                labels.add(
                        new Label(Tools.getDbString(cursor, DBContract.LabelEntry._ID),
                                Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_NAME),
                                JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_USER_IDS), new TypeToken<List<String>>(){}.getType()),
                                Tools.getDbLong(cursor, DBContract.LabelEntry.COLUMN_CREATE_TIME),
                                Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_OWNER_ID),
                                "true".equals(Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_READ_ONLY)),
                                "true".equals(Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_DELETED))
                ));
            }
            cursor.close();
            return labels;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }
}
