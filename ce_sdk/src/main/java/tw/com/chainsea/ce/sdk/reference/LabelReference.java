package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.collect.Sets;
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
 * current by evan on 2020-04-21
 *
 * @author Evan Wang
 * @date 2020-04-21
 */
public class LabelReference {


    public static Label findById(SQLiteDatabase db, String id) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }
//            String sql = "SELECT * FROM " + DBContract.LabelEntry.TABLE_NAME +
//                    " WHERE " + DBContract.LabelEntry._ID + " = ?";

//            Cursor cursor = db.rawQuery(sql, new String[]{id});
            Cursor cursor = db.query(
                    DBContract.LabelEntry.TABLE_NAME,
                    null,
                    DBContract.LabelEntry._ID + " = ?",
                    new String[]{id},
                    null,
                    null,
                    null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            Label label = new Label(Tools.getDbString(cursor, DBContract.LabelEntry._ID),
                    Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_NAME),
                    JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_USER_IDS), new TypeToken<List<String>>(){}.getType()),
                    Tools.getDbLong(cursor, DBContract.LabelEntry.COLUMN_CREATE_TIME),
                    Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_OWNER_ID),
                    "true".equals(Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_READ_ONLY)),
                    "true".equals(Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_DELETED))
            );
            cursor.close();
            return label;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }


    /**
     * 儲存標籤
     */
    public synchronized static boolean save(SQLiteDatabase db, Label label) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DBContract.LabelEntry._ID, label.getId());
            values.put(DBContract.LabelEntry.COLUMN_NAME, label.getName());
            values.put(DBContract.LabelEntry.COLUMN_USER_IDS, JsonHelper.getInstance().toJson(label.getUserIds()));
            values.put(DBContract.LabelEntry.COLUMN_CREATE_TIME, label.getCreateTime());
            values.put(DBContract.LabelEntry.COLUMN_OWNER_ID, label.getOwnerId());
            values.put(DBContract.LabelEntry.COLUMN_READ_ONLY, label.isReadOnly() ? "true" : "false");
            values.put(DBContract.LabelEntry.COLUMN_DELETED, label.isDeleted() ? "true" : "false");
            boolean status = db.replace(DBContract.LabelEntry.TABLE_NAME, null, values) > 0L;
            status = status && FriendsLabelRelReference.saveByLabel(db, label);
            db.setTransactionSuccessful();
            return status;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }finally {
            db.endTransaction();
        }
    }

    /**
     * 儲存標籤
     */
    public synchronized static boolean save(SQLiteDatabase db, Label label, Set<String> userIds) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DBContract.LabelEntry._ID, label.getId());
            values.put(DBContract.LabelEntry.COLUMN_NAME, label.getName());
            values.put(DBContract.LabelEntry.COLUMN_USER_IDS, JsonHelper.getInstance().toJson(label.getUserIds()));
            values.put(DBContract.LabelEntry.COLUMN_CREATE_TIME, label.getCreateTime());
            values.put(DBContract.LabelEntry.COLUMN_OWNER_ID, label.getOwnerId());
            values.put(DBContract.LabelEntry.COLUMN_READ_ONLY, label.isReadOnly() ? "true" : "false");
            values.put(DBContract.LabelEntry.COLUMN_DELETED, label.isDeleted() ? "true" : "false");
            boolean status = db.replace(DBContract.LabelEntry.TABLE_NAME, null, values) > 0L;
            status = status && FriendsLabelRelReference.saveByLabel(db, label.getId(), userIds);
            db.setTransactionSuccessful();
            return status;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }finally {
            db.endTransaction();
        }
    }

    /**
     * 刪除標籤，連同刪除關聯表
     *
     * @param db
     * @param id
     * @return
     */
    public synchronized static boolean delete(SQLiteDatabase db, String id) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            db.beginTransaction();
            String whereClause = DBContract.LabelEntry._ID + " = ?";
            String[] whereArgs = new String[]{id};
            boolean status = db.delete(DBContract.LabelEntry.TABLE_NAME, whereClause, whereArgs) > 0;
            status = status && FriendsLabelRelReference.deleteByLabelId(db, id);
            db.setTransactionSuccessful();
            return status;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }

    }

    public static Set<String> findFavouriteLabels(SQLiteDatabase db) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }
            Set<String> favouriteIds = Sets.newHashSet();
//            String sql = " SELECT f.* FROM " + DBContract.FriendsLabelRel.TABLE_NAME + " AS f " +
//                    " INNER JOIN " + DBContract.LabelEntry.TABLE_NAME + " AS l " +
//                    " ON l." + DBContract.LabelEntry._ID + "= f." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID +
//                    " WHERE l." + DBContract.LabelEntry.COLUMN_READ_ONLY + "=? ";

//            Cursor cursor = db.rawQuery(sql, new String[]{"true"});
            Cursor cursor = db.query(
                    DBContract.FriendsLabelRel.TABLE_NAME + " AS f " +
                            " INNER JOIN " + DBContract.LabelEntry.TABLE_NAME + " AS l " +
                            " ON l." + DBContract.LabelEntry._ID + "= f." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID,
                    new String[]{"f.*"},
                    "l." + DBContract.LabelEntry.COLUMN_READ_ONLY + "=?",
                    new String[]{"true"},
                    null,
                    null,
                    null
            );
            while (cursor.moveToNext()) {
                favouriteIds.add(Tools.getDbString(cursor, DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID));
            }
            cursor.close();
            return favouriteIds;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return Sets.newHashSet();
        }
    }


    public static boolean findIsFavouriteById(SQLiteDatabase db, String id) {
        try {
            boolean status;
//            String sql = " SELECT f.* FROM " + DBContract.FriendsLabelRel.TABLE_NAME + " AS f " +
//                    " INNER JOIN " + DBContract.LabelEntry.TABLE_NAME + " AS l " +
//                    " ON l." + DBContract.LabelEntry._ID + "= f." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID +
//                    " WHERE l." + DBContract.LabelEntry.COLUMN_READ_ONLY + "=? " +
//                    " AND f." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + "=?";
//            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).rawQuery(sql, new String[]{"true", id});
            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).query(
                    DBContract.FriendsLabelRel.TABLE_NAME + " AS f " +
                            " INNER JOIN " + DBContract.LabelEntry.TABLE_NAME + " AS l " +
                            " ON l." + DBContract.LabelEntry._ID + "= f." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID,
                    new String[]{"f.*"},
                    "l." + DBContract.LabelEntry.COLUMN_READ_ONLY + "=? " +
                            " AND f." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + "=?",
                    new String[]{"true", id},
                    null,
                    null,
                    null
            );
            status = cursor.getCount() > 0;
            cursor.close();
            return status;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }
}
