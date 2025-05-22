package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.bean.statistics.StatisticsEntity;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.model.User;

/**
 * current by evan on 2020-08-19
 *
 * @author Evan Wang
 * date 2020-08-19
 */
public class ServiceNumberReference extends AbsReference {

    public static List<ServiceNumberEntity> findSelfServiceNumber(String userId) {
        try {
            List<ServiceNumberEntity> list = Lists.newArrayList();
//            String sql = "SELECT * FROM " + DBContract.ServiceNumEntry.TABLE_NAME
//                    + " WHERE " + DBContract.ServiceNumEntry.COLUMN_STATUS + " = '"+ User.Status.ENABLE + "' "
//                    + " AND (" + DBContract.ServiceNumEntry.COLUMN_OWNER_ID + " != '" + userId + "'"
//                    + " OR " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " != '" + ServiceNumberType.BOSS.getType() + "')";
//            Cursor cursor = DBManager.getInstance().openDatabase().rawQuery(sql, null);
            Cursor cursor = DBManager.getInstance().openDatabase().query(
                DBContract.ServiceNumEntry.TABLE_NAME,
                null,
                DBContract.ServiceNumEntry.COLUMN_STATUS + " !=? "
                    + " AND (" + DBContract.ServiceNumEntry.COLUMN_OWNER_ID + " != ?"
                    + " OR " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " != ?)"
                    + " AND " + DBContract.ServiceNumEntry.COLUMN_STATUS + " != ?" ,
                new String[]{User.Status.DISABLE, userId, ServiceNumberType.BOSS.getType(), "New"},
                null,
                null,
                null
            );
            if (cursor == null) {
                return Lists.newArrayList();
            }

            Map<String, Integer> index = DBContract.ServiceNumEntry.getIndex(cursor);
            while (cursor.moveToNext()) {
                ServiceNumberEntity entity = ServiceNumberEntity.formatByCursor(index, cursor).build();
                list.add(entity);
            }
            cursor.close();
            return list;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }


    public static ChatRoomEntity findServiceMemberRoomIdById(String serviceNumberId) {
        try {
            SQLiteDatabase sqLiteDatabase = DBManager.getInstance().openDatabase();
//            String sql = "SELECT " + DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID + " FROM " + DBContract.ServiceNumEntry.TABLE_NAME
//                    + " WHERE " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=? "
//                    + " AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "!=''";
//            Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{serviceNumberId});
            Cursor cursor = sqLiteDatabase.query(
                DBContract.ServiceNumEntry.TABLE_NAME,
                new String[]{DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID},
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=? "
                    + " AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "!=''",
                new String[]{serviceNumberId},
                null,
                null,
                null
            );
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                String serviceMemberRoomId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID);
                cursor.close();

                boolean hasData = ChatRoomReference.getInstance().hasLocalData(serviceMemberRoomId);
                if (hasData) {
                    return ChatRoomReference.getInstance().findById2("", serviceMemberRoomId, false, false, false, false, false);
                } else {
                    return ChatRoomEntity.Build().id(serviceMemberRoomId).type(ChatRoomType.serviceMember).build();
                }
            }
            return null;
//            return serviceMemberRoomId;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }


    public static ServiceNumberEntity findSelfBossServiceNumber() {
        try {
//            String sql = "SELECT * FROM " + DBContract.ServiceNumEntry.TABLE_NAME
//                    + " WHERE " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + "== 'Boss' "
//                    + " AND " + DBContract.ServiceNumEntry.COLUMN_IS_OWNER + "== 'true' "
//                    + " AND " + DBContract.ServiceNumEntry.COLUMN_STATUS + "== 'Enable'";
//            Cursor cursor = DBManager.getInstance().openDatabase().rawQuery(sql, new String[]{});
            Cursor cursor = DBManager.getInstance().openDatabase().query(
                DBContract.ServiceNumEntry.TABLE_NAME,
                null,
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + "=? "
                    + " AND " + DBContract.ServiceNumEntry.COLUMN_IS_OWNER + "=? "
                    + " AND " + DBContract.ServiceNumEntry.COLUMN_STATUS + "=?",
                new String[]{"Boss", "true", "Enable"},
                null,
                null,
                null
            );
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                ServiceNumberEntity entity = ServiceNumberEntity.formatByCursor(cursor).build();
                cursor.close();
                return entity;
            }
            return null;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    public static ServiceNumberEntity findManageServiceNumber() {
        try {
//            String sql = "SELECT * FROM " + DBContract.ServiceNumEntry.TABLE_NAME
//                    + " WHERE " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + "== '"+ ServiceNumberType.MANAGER.getType() +"' ";
//            Cursor cursor = DBManager.getInstance().openDatabase().rawQuery(sql, new String[]{});
            Cursor cursor = DBManager.getInstance().openDatabase().query(
                DBContract.ServiceNumEntry.TABLE_NAME,
                null,
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + "=?",
                new String[]{ServiceNumberType.MANAGER.getType()},
                null,
                null,
                null
            );
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                ServiceNumberEntity entity = ServiceNumberEntity.formatByCursor(cursor).build();
                cursor.close();
                return entity;
            }
            return null;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    public static ServiceNumberEntity findBroadcastRoomByIdAndServiceNumberId(SQLiteDatabase db, String broadcastRoomId, String serviceNumberId) {
        try {
//            String sql = "SELECT * FROM " + DBContract.ServiceNumEntry.TABLE_NAME
//                    + " WHERE " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?"
//                    + " AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "=?";
//            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).rawQuery(sql, new String[]{serviceNumberId, broadcastRoomId});
            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).query(
                DBContract.ServiceNumEntry.TABLE_NAME,
                null,
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?"
                    + " AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "=?",
                new String[]{serviceNumberId, broadcastRoomId},
                null,
                null,
                null
            );
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                ServiceNumberEntity entity = ServiceNumberEntity.formatByCursor(cursor).build();
                cursor.close();
                return entity;
            } else
                return null;

        } catch (Exception e) {
            return null;
        }
    }

    public static String findUserIdByOpenId(String openId) {
//        String sql = "SELECT " + DBContract.UserProfileEntry.COLUMN_ID + " FROM " + DBContract.UserProfileEntry.TABLE_NAME
//                + " where " + DBContract.UserProfileEntry.COLUMN_OPEN_ID + " == '" + openId + "'";
//        Cursor cursor = DBManager.getInstance().openDatabase().rawQuery(sql, new String[]{});
        Cursor cursor = DBManager.getInstance().openDatabase().query(
            DBContract.UserProfileEntry.TABLE_NAME,
            new String[]{DBContract.UserProfileEntry.COLUMN_ID},
            DBContract.UserProfileEntry.COLUMN_OPEN_ID + " = ?",
            new String[]{openId},
            null,
            null,
            null
        );
        if (cursor == null) {
            return "";
        }
        if (cursor.moveToFirst()) {
            String ownerId = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ID);
            cursor.close();
            return ownerId;
        }
        return "";
    }

    public static String findRoomIdByUserIdAndServiceNumberId(String userId, String serviceNumberId) {
//        String sql = "SELECT " + DBContract.ChatRoomEntry._ID + " FROM " + DBContract.ChatRoomEntry.TABLE_NAME
//                + " where " + DBContract.ChatRoomEntry.COLUMN_OWNER_ID + " == '" + userId + "'" +
//                " and " + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " == '" + serviceNumberId + "'";
//        Cursor cursor = DBManager.getInstance().openDatabase().rawQuery(sql, new String[]{});
        Cursor cursor = DBManager.getInstance().openDatabase().query(
            DBContract.ChatRoomEntry.TABLE_NAME,
            new String[]{DBContract.ChatRoomEntry._ID},
            DBContract.ChatRoomEntry.COLUMN_OWNER_ID + " =?" +
                " AND " + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =?",
            new String[]{userId, serviceNumberId},
            null,
            null,
            null
        );
        if (cursor == null) {
            return "";
        }
        if (cursor.moveToFirst()) {
            String roomId = Tools.getDbString(cursor, DBContract.ChatRoomEntry._ID);
            cursor.close();
            return roomId;
        }
        return "";
    }

    public static ServiceNumberEntity findSubscribeNumberById(SQLiteDatabase db, String subscribeNumberId) {
        try {
//            String sql = "SELECT * FROM " + DBContract.ServiceNumEntry.TABLE_NAME
//                    + " WHERE " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?"
//                    + " AND " + DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE + "=?";
//            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).rawQuery(sql, new String[]{subscribeNumberId, "true"});
            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).query(
                DBContract.ServiceNumEntry.TABLE_NAME,
                null,
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?"
                    + " AND " + DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE + "=?",
                new String[]{subscribeNumberId, "true"},
                null,
                null,
                null
            );
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                ServiceNumberEntity entity = ServiceNumberEntity.formatByCursor(cursor).build();
                cursor.close();
                return entity;
            }
            return null;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    public static ServiceNumberEntity findServiceNumberById(String serviceNumberId) {
        try {
//            String sql = "SELECT * FROM " + DBContract.ServiceNumEntry.TABLE_NAME
//                    + " WHERE " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?";
//            Cursor cursor = DBManager.getInstance().openDatabase().rawQuery(sql, new String[]{serviceNumberId});
            Cursor cursor = DBManager.getInstance().openDatabase().query(
                DBContract.ServiceNumEntry.TABLE_NAME,
                null,
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?",
                new String[]{serviceNumberId},
                null,
                null,
                null
            );
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                ServiceNumberEntity entity = ServiceNumberEntity.formatByCursor(cursor).build();
                cursor.close();
                return entity;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static ServiceNumberEntity findBroadcastServiceNumberById(SQLiteDatabase db, String serviceNumberId) {
        try {
//            String sql = "SELECT * FROM " + DBContract.ServiceNumEntry.TABLE_NAME
//                    + " WHERE " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?"
//                    + " AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "!=?";
//            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).rawQuery(sql, new String[]{serviceNumberId, ""});
            Cursor cursor = (db == null ? DBManager.getInstance().openDatabase() : db).query(
                DBContract.ServiceNumEntry.TABLE_NAME,
                null,
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + "=?"
                    + " AND " + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + "!=''",
                new String[]{serviceNumberId},
                null,
                null,
                null
            );
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                ServiceNumberEntity entity = ServiceNumberEntity.formatByCursor(cursor).build();
                cursor.close();
                return entity;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 儲存服務號
     */
    public static boolean save(SQLiteDatabase db, ServiceNumberEntity entity) {
        try {
            ContentValues values = ServiceNumberEntity.getContentValues(entity);
            if (!Strings.isNullOrEmpty(entity.getBroadcastRoomId()) && !entity.memberIds().isEmpty()) {

                ServiceNumberAgentRelReference.saveAgentsRelByServiceNumber(null, entity);
//                改 Service Agent Rel Table
//                AccountRoomRelReference.saveByMemberIds(db, Lists.newArrayList(entity.memberIds()), entity.getBroadcastRoomId());
            }
            List<StatisticsEntity> entities = entity.getStatisticsEntities();
            Set<Long> endTimeSet = Sets.newHashSet();
            for (StatisticsEntity stat : entities) {
                endTimeSet.add(stat.getEndTime());
            }
            StatisticsReference.replace(db, entity.getServiceNumberId(), entities, endTimeSet);

            long _id = (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.ServiceNumEntry.TABLE_NAME, null, values);
            return _id > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean updateWelcomeData(String serviceNumberId, String serviceWelcomeMessage, String serviceIdleMessage, String everyContactMessage, int idleTime) {
        try {
            ContentValues values = new ContentValues();
            values.put(DBContract.ServiceNumEntry.COLUMN_FIRST_WELCOME_MESSAGE, serviceWelcomeMessage);
            values.put(DBContract.ServiceNumEntry.COLUMN_EACH_WELCOME_MESSAGE, everyContactMessage);
            values.put(DBContract.ServiceNumEntry.COLUMN_INTERVAL_WELCOME_MESSAGE, serviceIdleMessage);
            values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_IDEL_TIME, idleTime);
            long id = DBManager.getInstance().openDatabase().update(DBContract.ServiceNumEntry.TABLE_NAME, values, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?", new String[]{serviceNumberId});
            return id > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean updateTimeOutTime(String serviceNumberId, int timeoutTime) {
        try {
            ContentValues values = new ContentValues();
            values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_TIMEOUT_TIME, timeoutTime);
            long id = DBManager.getInstance().openDatabase().update(DBContract.ServiceNumEntry.TABLE_NAME, values, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " =?", new String[]{serviceNumberId});
            return id > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized static void updateSubscribeServiceNumberTime(String serviceNumberId) {
        SQLiteDatabase db = DBManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME, System.currentTimeMillis());
            db.update(
                DBContract.ServiceNumEntry.TABLE_NAME,
                values,
                DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?",
                new String[]{serviceNumberId}
            );
            db.setTransactionSuccessful();
        } catch (Exception ignored) {

        } finally {
            db.endTransaction();
        }
    }
}
