package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;

/**
 * current by evan on 2020-03-10
 */
public class AccountRoomRelReference extends AbsReference {

//    public static void saveByMemberIds(SQLiteDatabase db, List<String> memberIds, String roomId) {
//        if (memberIds != null) {
//            for (String accountId : memberIds) {
//                saveByAccountIdAndRoomId(db, accountId, roomId);
//            }
//        }
//    }
//
//
    public static void saveProfiles(SQLiteDatabase db, List<UserProfileEntity> userProfiles, String roomId) {
        if (userProfiles != null && !userProfiles.isEmpty()) {
            for (UserProfileEntity profile : userProfiles) {
                saveByAccountIdAndRoomId(db, profile.getId(), roomId);
            }
        }
    }
//
//
    public static boolean saveByAccountIdAndRoomId(SQLiteDatabase db, String accountId, String roomId) {
        try {
            ContentValues values = new ContentValues();
            values.put(DBContract.AccountRoomRel.COLUMN_ID, roomId + accountId);
            values.put(DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID, accountId);
            values.put(DBContract.AccountRoomRel.COLUMN_ROOM_ID, roomId);
            return (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.AccountRoomRel.TABLE_NAME, null, values) > 0L;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }



    public synchronized static boolean batchSaveByRoomIdsAndAccountIds(SQLiteDatabase db, Multimap<String, String> multimap) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            long useTime = System.currentTimeMillis();

            db.beginTransaction();
            String roomIdsIn = String.format(DBContract.AccountRoomRel.COLUMN_ROOM_ID + " IN (%s)", concatStrings("'", ",", multimap.keySet().toArray()));
            db.delete(DBContract.AccountRoomRel.TABLE_NAME, roomIdsIn, null);
            StringBuffer sbSQL = new StringBuffer();
            Iterator<Map.Entry<String, String>> iterator = multimap.entries().iterator();
            boolean first = true;
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String roomId = entry.getKey();
                String accountId = entry.getValue();
                if (!first) {
                    sbSQL.delete(0, sbSQL.length());
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(DBContract.AccountRoomRel.COLUMN_ID, roomId+accountId);
                contentValues.put(DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID, accountId);
                contentValues.put(DBContract.AccountRoomRel.COLUMN_ROOM_ID, roomId);

                db.replace(
                        DBContract.AccountRoomRel.TABLE_NAME,
                        null,
                        contentValues
                );


//                sbSQL.append(" REPLACE INTO '")
//                        .append(DBContract.AccountRoomRel.TABLE_NAME)
//                        .append("' (")
//                        .append(DBContract.AccountRoomRel.COLUMN_ID)
//                        .append(", ")
//                        .append(DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID)
//                        .append(", ")
//                        .append(DBContract.AccountRoomRel.COLUMN_ROOM_ID)
//                        .append(") VALUES");
//
//                sbSQL.append(" ('")
//                        .append(roomId)
//                        .append(accountId)
//                        .append("', '")
//                        .append(accountId)
//                        .append("', '")
//                        .append(roomId)
//                        .append("');");
//                db.execSQL(sbSQL.toString());
                first = false;
            }

            CELog.d(String.format("AccountRoomRelReference:: batchSaveByAccountIdsAndRoomIds roomId count-->%s, count-->%s, useTime-->%s /s", multimap.keySet().size(), multimap.values().size(), ((System.currentTimeMillis() - useTime) / 1000.0d)));
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            Log.e("AccountRoomRelReference", "batchSaveByRoomIdsAndAccountIds error="+e.getMessage());
            return false;
        }finally {
            db.endTransaction();
        }
    }

    
    public synchronized static boolean batchSaveByAccountIdsAndRoomId(SQLiteDatabase db, String roomId, List<String> accountIds) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            long useTime = System.currentTimeMillis();

            db.beginTransaction();

            StringBuffer sbSQL = new StringBuffer();
            for (int i = 0; i < accountIds.size(); i++) {
                String accountId = accountIds.get(i);

                if (i == 0) {
                    // 根据当前用户id和圈子id删除圈子成员
                    String whereClause = DBContract.AccountRoomRel.COLUMN_ROOM_ID + " = ?";
                    String[] whereArgs = new String[]{roomId};
                    db.delete(DBContract.AccountRoomRel.TABLE_NAME, whereClause, whereArgs);
                    // 第一次新增的时候删除历史数据
                }

                if (i != 0) {
                    sbSQL.delete(0, sbSQL.length());
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(DBContract.AccountRoomRel.COLUMN_ID, roomId+accountId);
                contentValues.put(DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID, accountId);
                contentValues.put(DBContract.AccountRoomRel.COLUMN_ROOM_ID, roomId);

                db.replace(
                        DBContract.AccountRoomRel.TABLE_NAME,
                        null,
                        contentValues
                );
//                sbSQL.append(" REPLACE INTO '")
//                        .append(DBContract.AccountRoomRel.TABLE_NAME)
//                        .append("' (")
//                        .append(DBContract.AccountRoomRel.COLUMN_ID)
//                        .append(", ")
//                        .append(DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID)
//                        .append(", ")
//                        .append(DBContract.AccountRoomRel.COLUMN_ROOM_ID)
//                        .append(") VALUES");
//
//                sbSQL.append(" ('")
//                        .append(roomId)
//                        .append(accountId)
//                        .append("', '")
//                        .append(accountId)
//                        .append("', '")
//                        .append(roomId)
//                        .append("');");
//                db.execSQL(sbSQL.toString());
            }

            db.setTransactionSuccessful();

            CELog.d(String.format("AccountRoomRelReference:: batchSaveByAccountIdsAndRoomId roomId-->%s, count-->%s, useTime-->%s /s", roomId, accountIds.size(), ((System.currentTimeMillis() - useTime) / 1000.0d)));
//            ChatMemberCacheService.clearCache(roomId);
            ChatMemberCacheService.refresh(roomId);

            return true;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }finally {
            db.endTransaction();
        }

    }

    //删除聊天室成员
    public static boolean deleteRelByRoomId(SQLiteDatabase db, String roomId) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }
            String whereClause = DBContract.AccountRoomRel.COLUMN_ROOM_ID + " = ?";
            String[] whereArgs = new String[]{roomId};
            return db.delete(DBContract.AccountRoomRel.TABLE_NAME, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    //删除聊天室单个成员
    public static void deleteRelByRoomIdAndAccountId(SQLiteDatabase db, String roomId, String accountId) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }
            String whereClause = DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID + " = ?"
                    + " AND " + DBContract.AccountRoomRel.COLUMN_ROOM_ID + " = ?";
            String[] whereArgs = new String[]{accountId, roomId};
//        db.beginTransaction();
            db.delete(DBContract.AccountRoomRel.TABLE_NAME, whereClause, whereArgs);
//        db.setTransactionSuccessful();
//        db.endTransaction();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }

    }

    public static List<String> findMemberIdsByRoomId(SQLiteDatabase db, String roomId) {
        try {
            Set<String> memberIds = Sets.newHashSet();
            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).query(
                    DBContract.AccountRoomRel.TABLE_NAME,
                    null,
                    DBContract.AccountRoomRel.COLUMN_ROOM_ID + " =? ",
                    new String[]{roomId},
                    DBContract.AccountRoomRel.COLUMN_ID,
                    null,
                    null
            );
            if (cursor.getCount() != 0) {
                Map<String, Integer> index = DBContract.AccountRoomRel.getIndex(cursor);
                while (cursor.moveToNext()) {
                    memberIds.add(cursor.getString(index.get(DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID)));
                }
            }
            cursor.close();
            return Lists.newArrayList(memberIds);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public static List<String> findMemberIdsByRoomId(String roomId) {
        return findMemberIdsByRoomId(null, roomId);
    }
}
