package tw.com.chainsea.ce.sdk.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.bean.CrowdEntity;
import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
import tw.com.chainsea.ce.sdk.bean.GroupEntity;
import tw.com.chainsea.ce.sdk.bean.SearchBean;
import tw.com.chainsea.ce.sdk.bean.ServiceNum;
import tw.com.chainsea.ce.sdk.bean.account.AccountType;
import tw.com.chainsea.ce.sdk.bean.account.Gender;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.label.Label;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.model.Member;
import tw.com.chainsea.ce.sdk.http.ce.model.ServiceNumber;
import tw.com.chainsea.ce.sdk.http.ce.model.User;
import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference;

/**
 * DBManager
 * Created by 90Chris on 2015/7/5.
 */
public class DBManager {
    private static DBManager INSTANCE;
    // instanceDataBaseName will store the database name specific to this instance
    private String instanceDataBaseName; 
    private String selfId; // Retained: Stores the UserID for this instance's context
    private DBHelper instanceHelper; // Instance-specific DBHelper
    public boolean isChangingTenant = false; // Retained: Global flag checked by SystemKit

    public SQLiteDatabase openDatabase() {
        if (isChangingTenant) {
            CELog.e("TenantSwitch", "DBManager: Attempted to open database while tenant switch is in progress (isChangingTenant is true). Returning null.");
            return null;
        }
        if (this.instanceHelper == null) {
            // This case should ideally not be reached if constructor ensures initDB.
            // If it is, it means the instance is not properly initialized.
            CELog.e("TenantSwitch", "DBManager.openDatabase: instanceHelper is null for this DBManager instance. Database not initialized correctly. selfId: " + this.selfId);
            // Attempt to re-initialize for this instance. This is a recovery attempt.
            // initDB(); // Be cautious with re-calling initDB here, it might have side effects if not designed for it.
            // For now, let's assume initDB in constructor was sufficient or failed definitively.
            return null; 
        }
        try {
            SQLiteDatabase db = this.instanceHelper.getWritableDatabase();
            if (db != null) {
                db.enableWriteAheadLogging(); // Ensure this is set on each open, if required.
                return db;
            } else {
                CELog.e("TenantSwitch", "DBManager.openDatabase: instanceHelper.getWritableDatabase() returned null. Database: " + this.instanceDataBaseName);
                return null;
            }
        } catch (Exception e) {
            CELog.e("TenantSwitch", "DBManager.openDatabase: Exception getting writable database for " + this.instanceDataBaseName, e);
            return null;
        }
    }

    public static DBManager getInstance() {
        if (INSTANCE == null) {
            // Crucial: isChangingTenant is checked BEFORE creating a new instance.
            // If a switch is in progress, SystemKit should prevent calls to getInstance()
            // until the switch is complete and isChangingTenant is false.
            // If getInstance is called while isChangingTenant is true, it might lead to issues.
            // However, the DBManager constructor itself also checks isChangingTenant.
            INSTANCE = new DBManager();
        }
        return INSTANCE;
    }

    private DBManager() {
        // Constructor now directly calls initDB().
        // The isChangingTenant flag is checked within initDB() as the first step.
        initDB();
    }

    public void initDB() {
        // This method now initializes instance fields.
        // It's called by the constructor.
        if (isChangingTenant) {
            CELog.w("TenantSwitch", "DBManager.initDB: Attempted to initialize DBManager instance while isChangingTenant is true. Aborting initDB. selfId at this point: " + TokenPref.getInstance(SdkLib.getAppContext()).getUserId());
            // instanceHelper will remain null, and instanceDataBaseName will be uninitialized.
            // openDatabase() for this instance will subsequently fail.
            return;
        }

        CELog.d("TenantSwitch", "DBManager.initDB: Initializing new DBManager instance.");
        this.selfId = TokenPref.getInstance(SdkLib.getAppContext()).getUserId();
        CELog.d("TenantSwitch", "DBManager.initDB: Instance context set to selfId (UserID): " + this.selfId);

        if (Strings.isNullOrEmpty(this.selfId)) {
            CELog.e("TenantSwitch", "DBManager.initDB: selfId is null or empty. Cannot initialize database. This indicates a critical issue with tenant context setup PRIOR to DBManager instantiation.");
            this.instanceDataBaseName = null;
            this.instanceHelper = null;
            return;
        }

        this.instanceDataBaseName = this.selfId + ".db";
        CELog.d("TenantSwitch", "DBManager.initDB: Instance database name set to: " + this.instanceDataBaseName);

        try {
            // Initialize the instance-specific DBHelper
            // No more SdkLib.dbHelper
            this.instanceHelper = new DBHelper(this.instanceDataBaseName);
            this.instanceHelper.setWriteAheadLoggingEnabled(false); // Or true, based on previous default
            CELog.d("TenantSwitch", "DBManager.initDB: Instance helper initialized for: " + this.instanceDataBaseName);
        } catch (Exception e) {
            CELog.e("TenantSwitch", "DBManager.initDB: Failed to initialize DBHelper for " + this.instanceDataBaseName, e);
            this.instanceHelper = null; // Ensure it's null if initialization fails
        }
    }

    public UserProfileEntity querySelfAccount(String userId) {
        return queryFriend(userId);
    }

    public void updateServiceStatus(String broadcastRoomId, boolean enable) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ServiceNumEntry.COLUMN_STATUS, (enable) ? "Enable" : "Disable");
        openDatabase().update(DBContract.ServiceNumEntry.TABLE_NAME, values, DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + " = ?", new String[]{broadcastRoomId});
    }

    public void updateUserAvatarId(String userId, String avatarId) {
        ContentValues values = new ContentValues();
        values.put(DBContract.UserProfileEntry.COLUMN_AVATAR_URL, avatarId);
        openDatabase().update(DBContract.UserProfileEntry.TABLE_NAME, values, DBContract.UserProfileEntry.COLUMN_ID + " = ?", new String[]{userId});
    }

    public void updateFriendRoomAvatarId(String roomId, String avatarId) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomEntry.COLUMN_AVATAR_ID, avatarId);
        openDatabase().update(DBContract.ChatRoomEntry.TABLE_NAME, values, DBContract.ChatRoomEntry._ID + " = ?", new String[]{roomId});
    }

    public void setChatRoomListItemOwnerUserType(String id, String ownerUserType) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomEntry.COLUMN_OWNER_USER_TYPE, ownerUserType);
        openDatabase().update(DBContract.ChatRoomEntry.TABLE_NAME, values, DBContract.ChatRoomEntry._ID + " = ?", new String[]{id});
    }

    public void setChatRoomListItemTop(String id, boolean isTop) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomEntry.COLUMN_IS_TOP, isTop ? "Y" : "N");
        openDatabase().update(DBContract.ChatRoomEntry.TABLE_NAME, values, DBContract.ChatRoomEntry._ID + " = ?", new String[]{id});
    }

    public void setChatRoomListItemMute(String id, boolean isMute) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomEntry.COLUMN_IS_MUTE, isMute ? "Y" : "N");
        openDatabase().update(DBContract.ChatRoomEntry.TABLE_NAME, values, DBContract.ChatRoomEntry._ID + " = ?", new String[]{id});
    }

    public void setChatRoomListItemUnreadNum(String id, int num) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomEntry.COLUMN_UNREAD_NUMBER, num);
        openDatabase().update(DBContract.ChatRoomEntry.TABLE_NAME, values, DBContract.ChatRoomEntry._ID + " = ?", new String[]{id});
    }

    public void setChatRoomListItemInteractionTime(String id) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomEntry.COLUMN_UPDATE_TIME, System.currentTimeMillis());
        openDatabase().update(DBContract.ChatRoomEntry.TABLE_NAME, values, DBContract.ChatRoomEntry._ID + " = ?", new String[]{id});
    }

    public boolean deleteRoomListItem(String id) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED, "Y");
        values.put(DBContract.ChatRoomEntry.COLUMN_DFR_TIME, System.currentTimeMillis());
        return openDatabase().update(DBContract.ChatRoomEntry.TABLE_NAME, values, DBContract.ChatRoomEntry._ID + " = ?", new String[]{id}) > 0;
    }

    public void setRoomNotDeleted(String roomId) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED, "N");
        values.put(DBContract.ChatRoomEntry.COLUMN_DFR_TIME, 0);
        openDatabase().update(DBContract.ChatRoomEntry.TABLE_NAME, values, DBContract.ChatRoomEntry._ID + " = ?", new String[]{roomId});
    }

    /***
     * 服務號列表要判斷自己是否是該服務號成員，才能取得該服務號聊天室或成員聊天室的未讀數
     * SELECT chat.unread
     * FROM chat
     * JOIN service ON chat.service_id = service.id
     * WHERE service.owner = true OR service.manager = true OR service.common = true;
     *
     *一般聊天列表僅抓取未讀數
     */
    public int getChatRoomListUnReadSum(String source) {
        int sum = 0;
        SQLiteDatabase db = openDatabase();
        if (db == null)
            return 0;
        Cursor cursor = null;

        try {
            if (Objects.equals(source, "SERVICE")) {

                String selection = "l." + DBContract.ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = ? AND ("
                    + "k." + DBContract.ServiceNumEntry.COLUMN_IS_OWNER + " = ?"
                    + " OR " + "k." + DBContract.ServiceNumEntry.COLUMN_IS_MANAGER + " = ?"
                    + " OR " + "k." + DBContract.ServiceNumEntry.COLUMN_IS_COMMON + " = ?)";
                String[] selectionArgs = new String[]{"SERVICE", "true", "true", "true"};

                cursor = db.query(
                    DBContract.ChatRoomEntry.TABLE_NAME + " AS l INNER JOIN " + DBContract.ServiceNumEntry.TABLE_NAME + " AS k ON " +
                        "l." + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = k." + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID, // 表名,
                    new String[]{"SUM(abs(l." + DBContract.ChatRoomEntry.COLUMN_UNREAD_NUMBER + "))"}, // 聚合的列
                    selection, // where 條件
                    selectionArgs, // where 條件的參數
                    null, // group by
                    null, // having
                    null // order by
                );
            } else {
                // 處理非 "SERVICE" 來源的情況
                String selection = DBContract.ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = ? AND "
                    + DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = ?";
                String[] selectionArgs = new String[]{source, "N"};

                cursor = db.query(
                    DBContract.ChatRoomEntry.TABLE_NAME, // 表名,
                    new String[]{"SUM(abs(" + DBContract.ChatRoomEntry.COLUMN_UNREAD_NUMBER + "))"}, // 聚合的列
                    selection, // where 條件
                    selectionArgs, // where 條件的參數
                    null, // group by
                    null, // having
                    null // order by
                );
            }

            // 獲取聚合結果
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                sum = cursor.getInt(0); // 獲取 SUM 結果
            }

        } catch (Exception e) {
            CELog.e("Error in getChatRoomListUnReadSum", e.getMessage(), e);
            // 如果發生錯誤，返回 0
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return sum;
    }

    //Chat Room List end

    public boolean insertCustomer(CustomerEntity entity) {
        String id = entity.getId();
        ContentValues contentValues = CustomerEntity.getContentValues(entity);

        final SQLiteDatabase db = openDatabase();
        if (db == null)
            return false;
        final String selection = DBContract.BossServiceNumberContactEntry.ID + " = ?";
        final String[] selectionArgs = new String[]{id};

        long _id;
        Cursor friendCursor = db.query(DBContract.BossServiceNumberContactEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (friendCursor.getCount() <= 0) {
            contentValues.put(DBContract.BossServiceNumberContactEntry.ID, id);
            _id = db.insert(DBContract.BossServiceNumberContactEntry.TABLE_NAME, null, contentValues);
        } else {
            _id = db.update(DBContract.BossServiceNumberContactEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        }
        friendCursor.close();
        return _id > 0;
    }

    public List<CustomerEntity> queryCustomers() {
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null)
                return Lists.newArrayList();
            String selection = DBContract.BossServiceNumberContactEntry.STATUS + " = 'Enable' ";
            Cursor cursor = db.query(DBContract.BossServiceNumberContactEntry.TABLE_NAME, null, selection, null, null, null, null);
            List<CustomerEntity> entities = Lists.newArrayList();
            while (cursor.moveToNext()) {
                entities.add(new CustomerEntity(
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ID),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.NAME),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.NICKNAME),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.AVATAR_ID),
                    Tools.getDbInt(cursor, DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK) != 0,
                    Tools.getDbInt(cursor, DBContract.BossServiceNumberContactEntry.IS_MOBILE) != 0,
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.IS_MOBILE),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.USER_TYPE),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ROOM_ID),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.OPEN_ID),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.SCOPE_INFOS),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS),
                    Tools.getDbInt(cursor, DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE) != 0,
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.STATUS)

                ));
            }
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<CustomerEntity> queryCustomersByName(String keyWord) {
        try {
            SQLiteDatabase db = openDatabase();
            if (db == null)
                return Lists.newArrayList();
            String status = "Enable";
            String userType = "contact";
            String selection = DBContract.BossServiceNumberContactEntry.STATUS + " = '" + status + "'"
                + "AND " + DBContract.BossServiceNumberContactEntry.USER_TYPE + " = '" + userType + "'"
                + " AND (" + DBContract.BossServiceNumberContactEntry.NAME + " LIKE '%" + keyWord + "%'"
                + " OR " + DBContract.BossServiceNumberContactEntry.NICKNAME + " LIKE '%" + keyWord + "%'"
                + " OR " + DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME + " LIKE '%" + keyWord + "%')";
            Cursor cursor = db.query(DBContract.BossServiceNumberContactEntry.TABLE_NAME, null, selection, null, null, null, null);
            List<CustomerEntity> entities = Lists.newArrayList();
            while (cursor.moveToNext()) {
                entities.add(new CustomerEntity(
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ID),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.NAME),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.NICKNAME),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.AVATAR_ID),
                    Tools.getDbInt(cursor, DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK) != 0,
                    Tools.getDbInt(cursor, DBContract.BossServiceNumberContactEntry.IS_MOBILE) != 0,
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.IS_MOBILE),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.USER_TYPE),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ROOM_ID),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.OPEN_ID),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.SCOPE_INFOS),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS),
                    Tools.getDbInt(cursor, DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE) != 0,
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.STATUS)

                ));
            }
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public String queryUserIdByName(String name) {
        SQLiteDatabase db = DBManager.getInstance().openDatabase();
        if (db == null)
            return "";
        String[] selectionArgs = new String[]{name};
        Cursor cursor = db.query(
            DBContract.UserProfileEntry.TABLE_NAME,
            new String[]{DBContract.UserProfileEntry.COLUMN_ID},
            DBContract.UserProfileEntry.COLUMN_NICKNAME + " = ?",
            selectionArgs,
            null, null, null
        );

        try {
            if (cursor.moveToFirst()) {
                return Tools.getDbString(cursor, 0);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }


    public CustomerEntity queryCustomer(String userId) {
        SQLiteDatabase db = DBManager.getInstance().openDatabase();
        if (db == null)
            return null;
        String[] selectionArgs = new String[]{userId};
        Cursor cursor = db.query(
            DBContract.BossServiceNumberContactEntry.TABLE_NAME,
            new String[]{
                DBContract.BossServiceNumberContactEntry.ID,
                DBContract.BossServiceNumberContactEntry.NAME,
                DBContract.BossServiceNumberContactEntry.NICKNAME,
                DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME,
                DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION,
                DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL,
                DBContract.BossServiceNumberContactEntry.AVATAR_ID,
                DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK,
                DBContract.BossServiceNumberContactEntry.IS_MOBILE,
                DBContract.BossServiceNumberContactEntry.IS_MOBILE,
                DBContract.BossServiceNumberContactEntry.USER_TYPE,
                DBContract.BossServiceNumberContactEntry.ROOM_ID,
                DBContract.BossServiceNumberContactEntry.OPEN_ID,
                DBContract.BossServiceNumberContactEntry.SCOPE_INFOS,
                DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS,
                DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE,
                DBContract.BossServiceNumberContactEntry.STATUS
            },
            DBContract.BossServiceNumberContactEntry.ID + " = ?",
            selectionArgs,
            null, null, null
        );

        try {
            if (cursor.moveToFirst()) {
                return new CustomerEntity(
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ID),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.NAME),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.NICKNAME),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.AVATAR_ID),
                    Tools.getDbInt(cursor, DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK) != 0,
                    Tools.getDbInt(cursor, DBContract.BossServiceNumberContactEntry.IS_MOBILE) != 0,
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.IS_MOBILE),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.USER_TYPE),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.ROOM_ID),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.OPEN_ID),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.SCOPE_INFOS),
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS),
                    Tools.getDbInt(cursor, DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE) != 0,
                    Tools.getDbString(cursor, DBContract.BossServiceNumberContactEntry.STATUS)
                );
            }
        } catch (Exception e) {
            CELog.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    public static int queryUsersWithSelfTableSize(String selfId) {
        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            if (db == null) {
                CELog.e("Database is null");
                return 0;
            }

            String[] selectionArgs = new String[]{"Enable", selfId};
            Cursor cursor = db.query(
                DBContract.UserProfileEntry.TABLE_NAME,
                new String[]{"COUNT(*)"},
                DBContract.UserProfileEntry.COLUMN_STATUS + " = ? AND " + DBContract.UserProfileEntry.COLUMN_ID + " != ?",
                selectionArgs,
                null, null, null
            );

            try {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getInt(0);
                }
            } catch (Exception e) {
                CELog.e(e.getMessage());
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            CELog.e("Error in queryUsersWithSelfTableSize: " + e.getMessage());
        }
        return 0;
    }


    public boolean insertFriends(UserProfileEntity account) {
        String id = account.getId();
        ContentValues friendValues = UserProfileEntity.getFriendValues(account);

        final SQLiteDatabase db = openDatabase();
        if (db == null)
            return false;
        final String friendSelection = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
        final String[] friendSelectionArgs = new String[]{id};

        long _id;
        Cursor friendCursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, friendSelection, friendSelectionArgs, null, null, null);
        if (friendCursor.getCount() <= 0) {
            _id = db.insert(DBContract.UserProfileEntry.TABLE_NAME, null, friendValues);
        } else {
            _id = db.update(DBContract.UserProfileEntry.TABLE_NAME, friendValues, friendSelection, friendSelectionArgs);
        }
        friendCursor.close();
        return _id > 0;
    }

    public boolean updateFriendField(String id, String key, String values) {
        SQLiteDatabase db = openDatabase();
        if (db == null)
            return false;
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(key, values);
        String whereClause = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{id};
//        db.beginTransaction();
        int _id = db.update(DBContract.UserProfileEntry.TABLE_NAME, contentvalues, whereClause, whereArgs);
//        db.setTransactionSuccessful();
//        db.endTransaction();
        return _id > 0;
    }

    public boolean setFriendBlock(String id, boolean values) {
        SQLiteDatabase db = openDatabase();
        if (db == null)
            return false;
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(DBContract.UserProfileEntry.COLUMN_BLOCK, values ? 1 : 0);
        String whereClause = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{id};
//        db.beginTransaction();
        int _id = db.update(DBContract.UserProfileEntry.TABLE_NAME, contentvalues, whereClause, whereArgs);
//        db.setTransactionSuccessful();
//        db.endTransaction();
        return _id > 0;
    }

    public boolean updateUserField(String id, String key, String values) {
        SQLiteDatabase db = openDatabase();
        if (db == null)
            return false;
        ContentValues contentvalues = new ContentValues();
        contentvalues.put(key, values);
        String whereClause = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{id};
        int _id = db.update(DBContract.UserProfileEntry.TABLE_NAME, contentvalues, whereClause, whereArgs);
        return _id > 0;
    }

    public synchronized boolean updateOrInsertApiInfoField(String key, Long values) {
        SQLiteDatabase db = openDatabase();
        if (db == null) return false;
        ContentValues initialValues = new ContentValues();
        initialValues.put(DBContract.API_INFO.COLUMN_SOURCE, key);
        initialValues.put(DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME, values);
        initialValues.put(DBContract.API_INFO.COLUMN_USER_ID, TokenPref.getInstance(SdkLib.getAppContext()).getUserId());
        String whereClause = DBContract.API_INFO.COLUMN_SOURCE + " = ?";
        int _id = db.update(DBContract.API_INFO.TABLE_NAME, initialValues, whereClause, new String[]{key});
        if (_id == 0) {
            db.insertWithOnConflict(DBContract.API_INFO.TABLE_NAME, null, initialValues, SQLiteDatabase.CONFLICT_REPLACE);
        }
        return _id > 0;
    }

    @SuppressLint("Range")
    public Long getLastRefreshTime(String source) {
        SQLiteDatabase db = openDatabase();
        if (db == null) return 0L;
        String[] selectionArgs = new String[]{
            TokenPref.getInstance(SdkLib.getAppContext()).getUserId(), source
        };
        Cursor cursor = db.query(
            DBContract.API_INFO.TABLE_NAME,
            new String[]{DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME},
            DBContract.API_INFO.COLUMN_USER_ID + " = ? AND " + DBContract.API_INFO.COLUMN_SOURCE + " = ?",
            selectionArgs,
            null, null, null
        );

        try {
            if (cursor.getCount() == 0) {
                return 0L;
            }
            cursor.moveToFirst();
            return cursor.getLong(cursor.getColumnIndex(DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME));
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return 0L;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean insertGroup(CrowdEntity crowdEntity) {
        String id = crowdEntity.getId();
        String kind = crowdEntity.getKind();
        String name = crowdEntity.getName();
        String ownerId = crowdEntity.getOwnerId();
        String avatarUrl = crowdEntity.getAvatarUrl();
        boolean isCustomName = crowdEntity.isCustomName();
        ContentValues values = new ContentValues();
        if (kind != null) {
            values.put(DBContract.GroupEntry.COLUMN_KIND, kind);
        }
        if (name != null) {
            values.put(DBContract.GroupEntry.COLUMN_NAME, name);
        }
        if (ownerId != null) {
            values.put(DBContract.GroupEntry.COLUMN_OWNER_ID, ownerId);
        }
        if (isCustomName) {
            values.put(DBContract.GroupEntry.COLUMN_CUSTOM_NAME, 1);
        } else {
            values.put(DBContract.GroupEntry.COLUMN_CUSTOM_NAME, 0);
        }
        if (avatarUrl != null) {
            values.put(DBContract.GroupEntry.COLUMN_AVATAR_URL, avatarUrl);
        } else {
            values.put(DBContract.GroupEntry.COLUMN_AVATAR_URL, crowdEntity.getAvatarId());
        }

        final SQLiteDatabase db = openDatabase();
        if (db == null)
            return false;
        final String selection = DBContract.GroupEntry._ID + " = ?";
        final String[] selectionArgs = new String[]{id};

        long _id;
//        db.beginTransaction();
        Cursor retCursor = db.query(DBContract.GroupEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        if (retCursor.getCount() <= 0) {
            //if not exist, insert
            values.put(DBContract.GroupEntry._ID, id);
            _id = db.insert(DBContract.GroupEntry.TABLE_NAME, null, values);
        } else {
            _id = db.update(DBContract.GroupEntry.TABLE_NAME, values, selection, selectionArgs);
        }
        retCursor.close();

        List<UserProfileEntity> users = crowdEntity.getMemberArray();

        if (users != null && !users.isEmpty()) {
            AccountRoomRelReference.saveProfiles(db, crowdEntity.getMemberArray(), crowdEntity.getId());
//            String sql = "INSERT OR REPLACE INTO " + DBContract.AccountRoomRel.TABLE_NAME + " VALUES(? , ?)";
//            String sql = "INSERT OR REPLACE INTO " + DBContract.AccountRoomRel.TABLE_NAME + " VALUES(? , ? , ?)";
            String memberSelection = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
            for (UserProfileEntity user : users) {

//                db.execSQL(sql, new String[]{user.getId(), crowdEntity.getId()});
//                db.execSQL(sql, new String[]{user.getId() + crowdEntity.getId(), user.getId(), crowdEntity.getId()});
                String[] memberArgs = new String[]{user.getId()};
                Cursor cursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, memberSelection, memberArgs, null, null, null);
                if (cursor.getCount() > 0) {
                    db.update(DBContract.UserProfileEntry.TABLE_NAME, UserProfileEntity.getFriendValues(user), memberSelection, memberArgs);
                } else {
                    ContentValues accountValues = UserProfileEntity.getFriendValues(user);
                    accountValues.put(DBContract.UserProfileEntry.COLUMN_ID, user.getId());
                    db.insert(DBContract.UserProfileEntry.TABLE_NAME, null, accountValues);
                }
                cursor.close();
            }
        }
//        db.setTransactionSuccessful();
//        db.endTransaction();
        return _id > 0;
    }

    public boolean updateGroupField(String roomId, String key, String valuse) {
        SQLiteDatabase db = openDatabase();
        if (db == null)
            return false;
        ContentValues values = new ContentValues();
        values.put(key, valuse);
//        db.beginTransaction();
        String wherClause = DBContract.GroupEntry._ID + " = ?";
        String[] whereArgs = new String[]{roomId};
        int _id = db.update(DBContract.GroupEntry.TABLE_NAME, values, wherClause, whereArgs);
//        db.setTransactionSuccessful();
//        db.endTransaction();
        return _id > 0;
    }

    //编辑标签页面，删除标签成员
    public void delAccount_Lable(String LableId, String accountId) {
        SQLiteDatabase db = openDatabase();
        String whereClause = DBContract.FriendsLabelRel.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{accountId + LableId};
//        db.beginTransaction();
        db.delete(DBContract.FriendsLabelRel.TABLE_NAME, whereClause, whereArgs);
//        db.setTransactionSuccessful();
//        db.endTransaction();
    }

    public void delAccount_Lables(String LableId) {
        SQLiteDatabase db = openDatabase();
        String whereClause = DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " = ?";
        String[] whereArgs = new String[]{LableId};
//        db.beginTransaction();
        db.delete(DBContract.FriendsLabelRel.TABLE_NAME, whereClause, whereArgs);
//        db.setTransactionSuccessful();
//        db.endTransaction();
    }

    public boolean insertServiceNum(ServiceNum serviceNum) {
        ContentValues values = new ContentValues();
        values.put(DBContract.ServiceNumEntry.COLUMN_ROOM_ID, serviceNum.roomId);
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID, serviceNum.serviceNumberId);
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE, serviceNum.getServiceNumberType());
        values.put(DBContract.ServiceNumEntry.COLUMN_DESCRIPTION, serviceNum.description);
        values.put(DBContract.ServiceNumEntry.COLUMN_ROBOT_ID, serviceNum.getRobotId());
        values.put(DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE, String.valueOf(serviceNum.isSubscribe));
        values.put(DBContract.ServiceNumEntry.COLUMN_NAME, serviceNum.name);
        values.put(DBContract.ServiceNumEntry.COLUMN_AVATAR_URL, serviceNum.serviceNumberAvatarId);
        values.put(DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID, serviceNum.getBroadcastRoomId());
        values.put(DBContract.ServiceNumEntry.COLUMN_OWNER_ID, serviceNum.getOwnerId());
        values.put(DBContract.ServiceNumEntry.COLUMN_FIRST_WELCOME_MESSAGE, serviceNum.getServiceWelcomeMessage());
        values.put(DBContract.ServiceNumEntry.COLUMN_EACH_WELCOME_MESSAGE, serviceNum.getEveryContactMessage());
        values.put(DBContract.ServiceNumEntry.COLUMN_INTERVAL_WELCOME_MESSAGE, serviceNum.getServiceIdleMessage());
        values.put(DBContract.ServiceNumEntry.COLUMN_IS_OWNER, serviceNum.getOwnerId() != null && serviceNum.getOwnerId().equals(selfId) ? "true" : "false");
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_IDEL_TIME, serviceNum.getServiceIdleTime());
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_TIMEOUT_TIME, serviceNum.getServiceTimeoutTime());
        values.put(DBContract.ServiceNumEntry.COLUMN_STATUS, serviceNum.getStatus());
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE, JsonHelper.getInstance().toJson(serviceNum.getServiceOpenType(), new TypeToken<List<String>>() {
        }.getType()));
        values.put(DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID, serviceNum.getServiceMemberRoomId());
        values.put(DBContract.ServiceNumEntry.COLUMN_ROBOT_SERVICE_FLAG, String.valueOf(serviceNum.isRobotServiceFlag()));
        values.put(DBContract.ServiceNumEntry.COLUMN_ROBOT_NAME, serviceNum.getRobotName());
        values.put(DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS, JsonHelper.getInstance().toJson(serviceNum.getMemberItems(), new TypeToken<List<Member>>() {
        }.getType()));

        for (Member member : serviceNum.getMemberItems()) {
            if (selfId.equals(member.getId())) {
                if (ServiceNumber.PrivilegeType.OWNER.equals(member.getPrivilege())) {
                    values.put(DBContract.ServiceNumEntry.COLUMN_IS_OWNER, String.valueOf(true));
                } else if (ServiceNumber.PrivilegeType.MANAGER.equals(member.getPrivilege().name())) {
                    values.put(DBContract.ServiceNumEntry.COLUMN_IS_MANAGER, String.valueOf(true));
                } else if (ServiceNumber.PrivilegeType.COMMON.equals(member.getPrivilege().name())) {
                    values.put(DBContract.ServiceNumEntry.COLUMN_IS_COMMON, String.valueOf(true));
                }
            }
        }

        SQLiteDatabase db = openDatabase();
        long id = db.update(DBContract.ServiceNumEntry.TABLE_NAME, values, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?", new String[]{serviceNum.serviceNumberId});
        if (id == 0) {
            db.insertWithOnConflict(DBContract.ServiceNumEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        }
        return id > 0;
    }

    public List<String> queryAllServiceNumberId() {
        List<String> serviceNumIdList = new ArrayList<>();
        SQLiteDatabase db = openDatabase();
        String[] selectionArgs = new String[]{"Enable"};
        Cursor cursor = db.query(
            DBContract.ServiceNumEntry.TABLE_NAME,
            new String[]{DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID},
            DBContract.ServiceNumEntry.COLUMN_STATUS + " = ?",
            selectionArgs,
            null, null, null
        );

        try {
            while (cursor.moveToNext()) {
                String serviceNumberId = Tools.getDbString(cursor, 0);
                serviceNumIdList.add(serviceNumberId);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return serviceNumIdList;
    }


    public boolean queryFriendIsBlock(String memberId) {
        final SQLiteDatabase db = openDatabase();
        final String selection = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
        final String[] selectionArgs = new String[]{memberId};
        String[] columns = {DBContract.UserProfileEntry.COLUMN_BLOCK};
        Cursor retCursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        if (retCursor.getCount() <= 0) {
            retCursor.close();
            return false;
        }
        retCursor.moveToFirst();
        int isBlock = Tools.getDbInt(retCursor, DBContract.UserProfileEntry.COLUMN_BLOCK);
        retCursor.close();
        return 1 == isBlock;
    }

    public UserProfileEntity queryFriend(String id) {
        if (!Strings.isNullOrEmpty(id)) {
            try {
                final SQLiteDatabase db = openDatabase();
                final String selection = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
                final String[] selectionArgs = new String[]{id};
                Cursor retCursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
                if (retCursor.moveToFirst()) {
                    UserProfileEntity accountCE = UserProfileEntity.getEntity(retCursor);
                    retCursor.close();
                    return accountCE;
                }
                return null;
            } catch (Exception e) {
                CELog.e("DBManager.queryFriend Error ", e.getMessage());
                return null;
            }
        } else
            return null;
    }

    public UserProfileEntity queryFriendByOpenId(String openId) {
        try {
            final SQLiteDatabase db = openDatabase();
            final String selection = DBContract.UserProfileEntry.COLUMN_OPEN_ID + " = ?";
            final String[] selectionArgs = new String[]{openId};
            Cursor retCursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
            int count = retCursor.getCount();
            if (count <= 0) {
                retCursor.close();
                return null;
            }
            retCursor.moveToFirst();
            UserProfileEntity accountCE = UserProfileEntity.getEntity(retCursor);
            retCursor.close();
            return accountCE;
        } catch (Exception e) {
            CELog.e("DBManager.queryFriend Error ", e.getMessage(), e);
            return null;
        }
    }

    public CrowdEntity queryGroup(String id) {
        SQLiteDatabase db = openDatabase();
        String selection = DBContract.GroupEntry._ID + " = ?";
        String[] selectionArgs = new String[]{id};
        Cursor retCursor = db.query(DBContract.GroupEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (retCursor.getCount() <= 0) {
            retCursor.close();
            return null;
        }
        retCursor.moveToFirst();
        CrowdEntity crowdEntity = new CrowdEntity();
        crowdEntity.setCustomName("true".equals(Tools.getDbString(retCursor, DBContract.GroupEntry.COLUMN_CUSTOM_NAME)));
        crowdEntity.setKind(Tools.getDbString(retCursor, DBContract.GroupEntry.COLUMN_KIND));
        crowdEntity.setName(Tools.getDbString(retCursor, DBContract.GroupEntry.COLUMN_NAME));
        crowdEntity.setOwnerId(Tools.getDbString(retCursor, DBContract.GroupEntry.COLUMN_OWNER_ID));
        crowdEntity.setAvatarUrl(Tools.getDbString(retCursor, DBContract.GroupEntry.COLUMN_AVATAR_URL));
        crowdEntity.setId(id);
        retCursor.close();
        return crowdEntity;
    }

    /**
     * 取得所有多人聊天室
     */
    public List<CrowdEntity> findAllCrowds() {
        try {
            List<CrowdEntity> crowdEntities = Lists.newArrayList();
            final SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(DBContract.GroupEntry.TABLE_NAME, null, null, null, null, null, null);
            if (cursor.getCount() <= 0) {
                cursor.close();
                return crowdEntities;
            }

            while (cursor.moveToNext()) {
                String id = Tools.getDbString(cursor, DBContract.GroupEntry._ID);
                List<UserProfileEntity> users = ChatMemberCacheService.getChatMember(id);
                crowdEntities.add(CrowdEntity.Build()
                    .id(Tools.getDbString(cursor, DBContract.GroupEntry._ID))
                    .users(users)
                    .isCustomName("true".equals(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_CUSTOM_NAME)))
                    .kind(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_KIND))
                    .name(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_NAME))
                    .ownerId(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_OWNER_ID))
                    .avatarUrl(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_AVATAR_URL))
                    .build());

//                crowdEntities.add(crowdEntity);
//
//                CrowdEntity crowdEntity = new CrowdEntity();
//                crowdEntity.setId(Tools.getDbString(cursor, DBContract.GroupEntry._ID));
//
//                crowdEntity.setCustomName("true".equals(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_CUSTOM_NAME)));
//                crowdEntity.setKind(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_KIND));
//                crowdEntity.setName(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_NAME));
//                crowdEntity.setOwnerId(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_OWNER_ID));
//                crowdEntity.setAvatarUrl(Tools.getDbString(cursor, DBContract.GroupEntry.COLUMN_AVATAR_URL));
//                crowdEntities.add(crowdEntity);
            }
            cursor.close();
            return crowdEntities;
        } catch (Exception e) {
            CELog.e("DBManager.findAllCrowds Error ", e.getMessage(), e);
            return Lists.newArrayList();
        }
    }

    public boolean saveGroupInfo(GroupEntity entity) {
        try {
            final SQLiteDatabase db = openDatabase();
            ContentValues values = GroupEntity.getContentValues(entity);
            long _id = (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.SyncGroupEntry.TABLE_NAME, null, values);
            return _id > 0;
        } catch (Exception e) {
            Log.e("saveGroupInfo", "error=" + e.getMessage());
            return false;
        }
    }

    public GroupEntity queryGroupInfo(String id) {
        SQLiteDatabase db = openDatabase();
        String[] selectionArgs = new String[]{id, "N", "N"};
        Cursor retCursor = db.query(
            DBContract.SyncGroupEntry.TABLE_NAME,
            null,
            DBContract.SyncGroupEntry._ID + " = ? AND " +
                DBContract.SyncGroupEntry.COLUMN_DELETED + " = ? AND " +
                DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED + " = ?",
            selectionArgs,
            null, null, null
        );

        try {
            if (retCursor.getCount() <= 0) {
                return null;
            }
            retCursor.moveToFirst();
            return GroupEntity.getEntity(retCursor);
        } catch (Exception e) {
            Log.e("queryGroupInfo", "error=" + e.getMessage());
            return null;
        } finally {
            if (retCursor != null) {
                retCursor.close();
            }
        }
    }


    /**
     * 取得所有Group
     */
    public List<GroupEntity> findAllGroups() {
        List<GroupEntity> groupEntities = new ArrayList<>();
        try {
            SQLiteDatabase db = openDatabase();

            if (db == null)
                return groupEntities;

            String[] selectionArgs = new String[]{"N", "N"};

            Cursor cursor = db.query(
                DBContract.SyncGroupEntry.TABLE_NAME,   // Table name
                null,                                   // Retrieve all columns
                DBContract.SyncGroupEntry.COLUMN_DELETED + " = ? AND " +
                    DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED + " = ?", // WHERE clause
                selectionArgs,                          // Selection arguments
                null,                                   // Group by
                null,                                   // Having
                null                                    // Order by
            );
            while (cursor.moveToNext()) {
                GroupEntity groupEntity = GroupEntity.getEntity(cursor);
                groupEntities.add(groupEntity);
            }

            cursor.close();
        } catch (Exception e) {
            CELog.e("findAllGroups Error", e.getMessage(), e);
        }

        return groupEntities;
    }

    public void deleteGroupInfo(String roomId) {
        final SQLiteDatabase db = openDatabase();
        final String messageSelection = DBContract.SyncGroupEntry._ID + " = ?";
        final String[] selectionArgs = new String[]{roomId};
        db.delete(DBContract.SyncGroupEntry.TABLE_NAME, messageSelection, selectionArgs);
    }


    public List<GroupEntity> findAllGroupsByName(String keyWord) {
        List<GroupEntity> groupEntities = new ArrayList<>();
        SQLiteDatabase db = openDatabase();

        String[] selectionArgs = new String[]{"N", "N", "%" + keyWord + "%"};

        Cursor cursor = db.query(
            DBContract.SyncGroupEntry.TABLE_NAME,
            null,
            DBContract.SyncGroupEntry.COLUMN_DELETED + " = ? AND " +
                DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED + " = ? AND " +
                DBContract.SyncGroupEntry.COLUMN_NAME + " LIKE ?",
            selectionArgs,
            null,
            null,
            null
        );

        try {
            while (cursor.moveToNext()) {
                GroupEntity groupEntity = GroupEntity.getEntity(cursor);
                groupEntities.add(groupEntity);
            }
        } catch (Exception e) {
            CELog.e("findAllGroupsByName Error", e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return groupEntities;
    }

    public List<Label> queryAllLabels() {
        List<Label> labels = new ArrayList<>();
        SQLiteDatabase db = openDatabase();

        try {
            Cursor cursor = db.query(
                DBContract.LabelEntry.TABLE_NAME,
                null,
                DBContract.LabelEntry.COLUMN_DELETED + " != ?",
                new String[]{"true"},
                null, null, null
            );
            while (cursor.moveToNext()) {
                Label label = new Label(
                    Tools.getDbString(cursor, DBContract.LabelEntry._ID),
                    Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_NAME),
                    JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_USER_IDS), new TypeToken<List<String>>() {
                    }.getType()),
                    Tools.getDbLong(cursor, DBContract.LabelEntry.COLUMN_CREATE_TIME),
                    Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_OWNER_ID),
                    "true".equals(Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_READ_ONLY)),
                    "true".equals(Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_DELETED))
                );

                Cursor userCursor = db.query(
                    DBContract.FriendsLabelRel.TABLE_NAME + " AS l INNER JOIN " + DBContract.UserProfileEntry.TABLE_NAME + " AS f ON f." + DBContract.UserProfileEntry.COLUMN_ID + " = l." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID,
                    null,
                    "l." + DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " = ?",
                    new String[]{label.getId()},
                    null, null, null
                );

                List<UserProfileEntity> friends = new ArrayList<>();
                while (userCursor.moveToNext()) {
                    UserProfileEntity friend = UserProfileEntity.getEntity(userCursor);
                    if (!friend.isBlock() && !User.Status.DISABLE.equals(friend.getStatus())) {
                        friends.add(friend);
                    }
                }
                userCursor.close();
                label.setUsers(friends);
                labels.add(label);
            }
            cursor.close();
        } catch (Exception e) {
            CELog.e("DBManager.queryAllLabels", e.getMessage(), e);
        }

        return labels;
    }


    public ServiceNumberEntity queryOfficialServiceNumber() {
        SQLiteDatabase db = openDatabase();
        String selection = DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " = ?";
        String[] selectionArgs = new String[]{ServiceNumberType.OFFICIAL.getType()};

        Cursor cursor = db.query(DBContract.ServiceNumEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return ServiceNumberEntity.formatByCursor(cursor).build();
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        } finally {
            cursor.close();
        }
    }


    public ServiceNum queryServiceNumberById(String serviceNumberId) {
        SQLiteDatabase db = openDatabase();
        String selection = DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?";
        String[] selectionArgs = new String[]{serviceNumberId};

        Cursor cursor = db.query(DBContract.ServiceNumEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToNext();
            ServiceNum serviceNum = new ServiceNum();
            serviceNum.description = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_DESCRIPTION);
            serviceNum.isSubscribe = Boolean.parseBoolean(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE));
            serviceNum.name = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_NAME);
            serviceNum.roomId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_ROOM_ID);
            serviceNum.serviceNumberAvatarId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_AVATAR_URL);
            serviceNum.serviceNumberId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID);
            serviceNum.memberItems = JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS), new TypeToken<List<Member>>() {
            }.getType());
            return serviceNum;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return null;
        } finally {
            cursor.close();
        }
    }


    public ServiceNum queryServiceNumberByConsultRoomId(String consultRoomId) {
        SQLiteDatabase db = openDatabase();
        String selection = DBContract.ServiceNumEntry.COLUMN_ROOM_ID + " = ?";
        String[] selectionArgs = new String[]{consultRoomId};

        Cursor cursor = db.query(DBContract.ServiceNumEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToNext();
            ServiceNum serviceNum = new ServiceNum();
            serviceNum.description = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_DESCRIPTION);
            serviceNum.isSubscribe = Boolean.parseBoolean(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE));
            serviceNum.name = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_NAME);
            serviceNum.roomId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_ROOM_ID);
            serviceNum.serviceNumberAvatarId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_AVATAR_URL);
            serviceNum.serviceNumberId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID);
            serviceNum.memberItems = JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS), new TypeToken<List<Member>>() {
            }.getType());
            return serviceNum;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return null;
        } finally {
            cursor.close();
        }
    }


    public void deleteBannedServiceNum(String serviceId) {
        final SQLiteDatabase db = openDatabase();
        final String messageSelection = DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?";
        final String[] selectionArgs = new String[]{serviceId};
        db.delete(DBContract.ServiceNumEntry.TABLE_NAME, messageSelection, selectionArgs);
    }

    public List<ServiceNum> querySubscribeServiceNumber() {
        try {
            List<ServiceNum> serviceNumList = Lists.newArrayList();
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.ServiceNumEntry.TABLE_NAME,
                null,
                DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE + " = ? AND " + DBContract.ServiceNumEntry.COLUMN_STATUS + " = ? AND " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " !=?",
                new String[]{"true", "Enable", ServiceNumberType.BOSS.getType()},
                null,
                null,
                DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME + " DESC"
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ServiceNum serviceNum = new ServiceNum();
                    serviceNum.description = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_DESCRIPTION);
                    serviceNum.isSubscribe = Boolean.parseBoolean(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE));
                    serviceNum.name = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_NAME);
                    serviceNum.roomId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_ROOM_ID);
                    serviceNum.serviceNumberAvatarId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_AVATAR_URL);
                    serviceNum.serviceNumberId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID);
                    String openType = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE);
                    serviceNum.setServiceOpenType(JsonHelper.getInstance().from(openType, new TypeToken<List<String>>() {
                    }.getType()));
                    serviceNum.updateTime = Tools.getDbLong(cursor, DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME);
                    serviceNumList.add(serviceNum);
                }
                cursor.close();
                return serviceNumList;
            } else
                return Lists.newArrayList();
        } catch (Exception e) {
            return Lists.newArrayList();
        }
    }

    public List<ServiceNum> querySubscribeServiceNumbersByName(String keyWord) {
        List<ServiceNum> serviceNumList = Lists.newArrayList();
        SQLiteDatabase db = openDatabase();
        String selection = DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE + " = ? AND " + DBContract.ServiceNumEntry.COLUMN_STATUS + " = ? AND " + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " !=? AND " + DBContract.ServiceNumEntry.COLUMN_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"true", "Enable", ServiceNumberType.BOSS.getType(), "%" + keyWord + "%"};

        Cursor cursor = db.query(DBContract.ServiceNumEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

        try {
            if (cursor.getCount() <= 0) {
                return serviceNumList;
            }

            while (cursor.moveToNext()) {
                ServiceNum serviceNum = new ServiceNum();
                serviceNum.description = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_DESCRIPTION);
                serviceNum.isSubscribe = Boolean.parseBoolean(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE));
                serviceNum.name = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_NAME);
                serviceNum.roomId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_ROOM_ID);
                serviceNum.serviceNumberAvatarId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_AVATAR_URL);
                serviceNum.serviceNumberId = Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID);
                serviceNum.serviceOpenType = JsonHelper.getInstance().from(Tools.getDbString(cursor, DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS), new TypeToken<List<Member>>() {
                }.getType());
                serviceNumList.add(serviceNum);
            }
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
        } finally {
            cursor.close();
        }
        return serviceNumList;
    }


    public boolean updateMessageStatus(String messageId, MessageStatus status) {
        final SQLiteDatabase db = openDatabase();
        final String whereClause = DBContract.MessageEntry._ID + " = ?";
        final String[] whereArgs = new String[]{messageId};

        ContentValues values = new ContentValues();
        values.put(DBContract.MessageEntry._ID, messageId);
        values.put(DBContract.MessageEntry.COLUMN_STATUS, status.getValue());

        int _id = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, whereArgs);

        return _id > 0;
    }

    public boolean updateReceivedNum(String messageId, int ReceivedNum) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.MessageEntry.COLUMN_RECEIVED_NUM, ReceivedNum);
        String whereClause = DBContract.MessageEntry._ID + "= ?";
        String[] whereArgs = new String[]{messageId};
        int id = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, whereArgs);
        return id > 0;
    }

    public boolean updateReadNum(String messageId, int ReadNum) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.MessageEntry.COLUMN_READED_NUM, ReadNum);
        String whereClause = DBContract.MessageEntry._ID + "= ?";
        String[] whereArgs = new String[]{messageId};
        int id = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, whereArgs);
        return id > 0;
    }

    public boolean updateSendTime(String messageId, long sendTime) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.MessageEntry.COLUMN_SEND_TIME, sendTime);
        String whereClause = DBContract.MessageEntry._ID + "= ?";
        String[] whereArgs = new String[]{messageId};
        int id = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, whereArgs);
        return id > 0;
    }

    public boolean updateSendNum(String messageId, int sendNum) {
        SQLiteDatabase db = openDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.MessageEntry.COLUMN_SEND_NUM, sendNum);
        String whereClause = DBContract.MessageEntry._ID + "= ?";
        String[] whereArgs = new String[]{messageId};
        int id = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, whereArgs);
        return id > 0;
    }

    public boolean deleteGroup(String groupId) {
        final SQLiteDatabase db = openDatabase();
        final String messageSelection = DBContract.GroupEntry._ID + " = ?";
        final String[] selectionArgs = new String[]{groupId};

//        String whereClause = DBContract.AccountRoomRel.COLUMN_ROOM_ID + " = ?";
//        String[] whereArgs = new String[]{groupId};

//        db.beginTransaction();
//        int delete = db.delete(DBContract.AccountRoomRel.TABLE_NAME, whereClause, whereArgs);
        boolean delete = AccountRoomRelReference.deleteRelByRoomId(db, groupId);
        int delete1 = db.delete(DBContract.GroupEntry.TABLE_NAME, messageSelection, selectionArgs);
//        db.setTransactionSuccessful();
//        db.endTransaction();
        return delete && delete1 > 0;
    }

    public boolean deleteServiceNum(String roomId) {
        final SQLiteDatabase db = openDatabase();
        final String messageSelection = DBContract.ServiceNumEntry.COLUMN_ROOM_ID + " = ?";
        final String[] selectionArgs = new String[]{roomId};
        return db.delete(DBContract.ServiceNumEntry.TABLE_NAME, messageSelection, selectionArgs) > 0;
    }

    public boolean deleteServiceNumRoomByServiceNumberId(String serviceNumberId) {
        final SQLiteDatabase db = openDatabase();
        final String messageSelection = DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = ?";
        final String[] selectionArgs = new String[]{serviceNumberId};

        final String serviceNumber = DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " = ?";
        final String[] serviceNumberArgs = new String[]{serviceNumberId};
        int serviceNumberStatus = db.delete(DBContract.ServiceNumEntry.TABLE_NAME, serviceNumber, serviceNumberArgs);
        int chatRoomStatus = db.delete(DBContract.ChatRoomEntry.TABLE_NAME, messageSelection, selectionArgs);

        return serviceNumberStatus > 0 && chatRoomStatus > 0;
    }

    public boolean deleteServiceNumber(String serviceNumberId) {
        SQLiteDatabase db = openDatabase();
        String messageSelection = DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " =?";
        String[] selectionArgs = new String[]{serviceNumberId};
        return db.delete(DBContract.ServiceNumEntry.TABLE_NAME, messageSelection, selectionArgs) > 0;
    }

    public List<MessageEntity> dimQueryLastReadMsg(String roomId) {
        SQLiteDatabase db = openDatabase();
        List<MessageEntity> messages = Lists.newArrayList();
        final String selection = DBContract.MessageEntry.COLUMN_READED_NUM + " > ?";
        final String[] selectionArgs = new String[]{"0"};
        final String order = DBContract.MessageEntry.COLUMN_SEND_TIME + " DESC";
        Cursor retCursor = db.query(DBContract.MessageEntry.TABLE_NAME, null, selection, selectionArgs, null, null, order, "1");

        if (retCursor.getCount() == 0) {
            retCursor.close();
            return Lists.newArrayList();
        }
        Map<String, Integer> index = DBContract.MessageEntry.getIndex(retCursor);
        while (retCursor.moveToNext()) {
            MessageEntity msg = MessageEntity.formatByCursor(index, retCursor);
            messages.add(msg);
        }
        retCursor.close();
        return messages;
    }

    public List<MessageEntity> queryMessagesByMsgStatus(String roomId, MessageStatus status) {
        SQLiteDatabase db = openDatabase();
        List<MessageEntity> messages = Lists.newArrayList();
        String selection = DBContract.MessageEntry.COLUMN_ROOM_ID + " = ? " +
            "AND " + DBContract.MessageEntry.COLUMN_RECEIVED_NUM + " > ? " +
            "AND " + DBContract.MessageEntry.COLUMN_READED_NUM + " = ?";
        String[] selectionArgs = new String[]{roomId, "0", "0"};

        Cursor cursor = db.query(DBContract.MessageEntry.TABLE_NAME, null, selection, selectionArgs, null, null, DBContract.MessageEntry.COLUMN_SEND_TIME + " ASC");

        try {
            if (cursor.getCount() == 0) {
                return messages;
            }

            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            while (cursor.moveToNext()) {
                MessageEntity msg = MessageEntity.formatByCursor(index, cursor);
                messages.add(msg);
            }
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
        } finally {
            cursor.close();
        }

        return messages;
    }


    public String queryLastReadMsgId(String roomId, String userId) {
        SQLiteDatabase db = openDatabase();
        String selection = DBContract.MessageEntry.COLUMN_ROOM_ID + " = ? " +
            "AND " + DBContract.MessageEntry.COLUMN_SENDER_ID + " = ? " +
            "AND " + DBContract.MessageEntry.COLUMN_READED_NUM + " > ?";
        String[] selectionArgs = new String[]{roomId, userId, "0"};

        Cursor cursor = db.query(DBContract.MessageEntry.TABLE_NAME, null, selection, selectionArgs, null, null, DBContract.MessageEntry.COLUMN_SEND_TIME + " DESC");

        try {
            if (cursor.getCount() <= 0) {
                return null;
            }
            cursor.moveToFirst();
            return Tools.getDbString(cursor, DBContract.MessageEntry._ID);
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return null;
        } finally {
            cursor.close();
        }
    }


    /***整理后的数据库增删改查方法*/
    public synchronized void insertUserAndFriends(UserProfileEntity user) {
        String id = user.getId();
        Gender gender = user.getGender();
        String birthday = user.getBirthday();
        String otherPhone = user.getOtherPhone();
        long mobile = user.getMobile();
        String email = user.getEmail();
        String loginName = user.getLoginName();
        String openId = user.getOpenId();
        String mood = user.getMood();

        ContentValues userValues = new ContentValues();
        if (id != null) {
            userValues.put(DBContract.USER_INFO._ID, id);
        }
        if (loginName != null) {
            userValues.put(DBContract.USER_INFO.COLUMN_LOGIN_NAME, loginName);
        }
        if (gender != null) {
            userValues.put(DBContract.USER_INFO.COLUMN_GENDER, gender.getValue());
        }
        if (birthday != null) {
            userValues.put(DBContract.USER_INFO.COLUMN_BIRTHDAY, birthday);
        }
        if (!TextUtils.isEmpty(otherPhone)) {
            userValues.put(DBContract.USER_INFO.COLUMN_OTHER_PHONE, otherPhone);
        }
        if (mobile != 0) {
            userValues.put(DBContract.USER_INFO.COLUMN_MOBILE, mobile);
        }
        if (mood != null) {
            userValues.put(DBContract.USER_INFO.COLUMN_MOOD, mood);
        }
        if (email != null) {
            userValues.put(DBContract.USER_INFO.COLUMN_EMAIL, email);
        }
        if (openId != null) {
            userValues.put(DBContract.USER_INFO.COLUMN_OPEN_ID, openId);
        }

        String avatarUrl = user.getAvatarId();
        String nickName = user.getNickName();
        String name = user.getName();
        String customerName = user.getCustomerName();
        String customerDescription = user.getCustomerDescription();
        String customerBusinessCardUrl = user.getCustomerBusinessCardUrl();
        mood = user.getMood();
//        int type = user.getType();
        AccountType accountType = user.getType();
        boolean isBlock = user.isBlock();
        String alias = user.getAlias();
        String roomId = user.getRoomId();
        boolean collection = user.isCollection();
        UserType userType = user.getUserType();
        String department = user.getDepartment();
        String duty = user.getDuty();

        ContentValues friendValues = new ContentValues();

        if (avatarUrl != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_AVATAR_URL, avatarUrl);
        }
        if (nickName != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_NICKNAME, nickName);
        }
        if (name != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_NAME, name);
        }
        if (customerName != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_CUSTOMER_NAME, customerName);
        }
        if (customerDescription != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION, customerDescription);
        }
        if (customerBusinessCardUrl != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL, customerBusinessCardUrl);
        }
        if (mood != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_MOOD, mood);
        }
        if (alias != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_ALIAS, alias);
        }
        if (roomId != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_ROOM_ID, roomId);
        }
        if (openId != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_OPEN_ID, openId);
        }
        friendValues.put(DBContract.UserProfileEntry.COLUMN_BLOCK, isBlock ? 1 : 0);
        friendValues.put(DBContract.UserProfileEntry.COLUMN_COLLECTION, collection ? "true" : "false");
        if (accountType != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_RELATION, accountType.getValue());
        }
        if (userType != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_USER_TYPE, userType.getUserType());
        }
        if (department != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_DEPARTMENT, department);
        }
        if (duty != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_DUTY, duty);
        }
        SQLiteDatabase db = openDatabase();
        try {
            String userSelection = DBContract.USER_INFO._ID + " = ?";
            String[] userSelectionArgs = new String[]{id};
            final String friendSelection = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
            final String[] friendSelectionArgs = new String[]{id};
            Cursor userCursor = db.query(DBContract.USER_INFO.TABLE_NAME, null, userSelection, userSelectionArgs, null, null, null);
            db.beginTransaction();
            if (userCursor.getCount() <= 0) {
                db.insert(DBContract.USER_INFO.TABLE_NAME, null, userValues);
            } else {
                db.update(DBContract.USER_INFO.TABLE_NAME, userValues, userSelection, userSelectionArgs);
            }

            Cursor friendCursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, friendSelection, friendSelectionArgs, null, null, null);
            if (friendCursor.getCount() <= 0) {
                //if not exist, insert
                friendValues.put(DBContract.UserProfileEntry.COLUMN_ID, id);
                db.insert(DBContract.UserProfileEntry.TABLE_NAME, null, friendValues);
            } else {
                db.update(DBContract.UserProfileEntry.TABLE_NAME, friendValues, friendSelection, friendSelectionArgs);
            }
            userCursor.close();
            friendCursor.close();
            db.setTransactionSuccessful();
        } catch (Exception ignored) {

        } finally {
            db.endTransaction();
        }
    }

    public List<UserProfileEntity> queryMembersFromUser(List<String> memberIds) {
        if (memberIds.isEmpty()) {
            return Collections.emptyList();
        }

        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;
        try {
            String selection = DBContract.UserProfileEntry.COLUMN_ID + " IN (" + generatePlaceholdersForIn(memberIds.size()) + ")";
            cursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, selection, toArray(memberIds), null, null, null);

            List<UserProfileEntity> users = new ArrayList<>();
            while (cursor.moveToNext()) {
                int isBlock = Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_BLOCK);
                if (isBlock != 1 && !User.Status.DISABLE.equals(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_STATUS))) {
                    users.add(UserProfileEntity.getEntity(cursor));
                }
            }
            return users;
        } catch (Exception e) {
            CELog.e("queryMembersFromUser error", e.getMessage(), e);
            throw new RuntimeException("Error querying members", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private String[] toArray(List<String> memberIds) {
        return memberIds.toArray(new String[0]);
    }

    private String generatePlaceholdersForIn(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        return String.join(", ", Collections.nCopies(length, "?"));
    }

    public UserProfileEntity queryUser(String id) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;
        try {
            String selection = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
            cursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, selection, new String[]{id}, null, null, null);
            if (cursor.moveToFirst()) {
                return UserProfileEntity.getEntity(cursor);
            }
            return null;
        } catch (Exception e) {
            CELog.e("queryUser error", e.getMessage(), e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * 查询所有fiend
     */
    public List<UserProfileEntity> queryFriends() {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, null, null, null, null, null);
            List<UserProfileEntity> friends = Lists.newArrayList();
            while (cursor.moveToNext()) {
                int isBlock = Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_BLOCK);
                AccountType accountType = AccountType.of(Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_RELATION));
                if (AccountType.FRIEND.equals(accountType) && 1 != isBlock && !User.Status.DISABLE.equals(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_STATUS))) {
                    friends.add(UserProfileEntity.getEntity(cursor));
                }
            }
            cursor.close();

            for (UserProfileEntity f : friends) {
                f.setLabels(getFriendLabels(f.getId()));
            }
            return friends;
        } catch (Exception e) {
            CELog.e("DBManager.queryFriends error", e.getMessage(), e);
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<UserProfileEntity> queryAllContactsByName(String keyWord, String userId) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;
        try {
            String selection = DBContract.UserProfileEntry.COLUMN_ID + " != ? AND ("
                + DBContract.UserProfileEntry.COLUMN_NICKNAME + " LIKE ? OR "
                + DBContract.UserProfileEntry.COLUMN_ALIAS + " LIKE ? OR "
                + DBContract.UserProfileEntry.COLUMN_NAME + " LIKE ?)";
            String[] selectionArgs = new String[]{
                userId,
                "%" + keyWord + "%",
                "%" + keyWord + "%",
                "%" + keyWord + "%"};
            cursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null);

            List<UserProfileEntity> users = new ArrayList<>();
            while (cursor.moveToNext()) {
                int isBlock = Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_BLOCK);
                if (1 != isBlock && !User.Status.DISABLE.equals(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_STATUS))) {
                    users.add(UserProfileEntity.getEntity(cursor));
                }
            }
            return users;
        } catch (Exception e) {
            CELog.e("queryAllContactsByName error", e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * 查询所有fiend
     */
    public List<UserProfileEntity> queryBlockFriends() {

        try {
            SQLiteDatabase db = openDatabase();
//            db.beginTransaction();
            Cursor cursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, null, null, null, null, null);
            List<UserProfileEntity> friends = Lists.newArrayList();
            while (cursor.moveToNext()) {
                int isBlock = Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_BLOCK);
                AccountType accountType = AccountType.of(Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_RELATION));
                if (AccountType.FRIEND.equals(accountType) && 1 == isBlock && !User.Status.DISABLE.equals(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_STATUS))) {
                    friends.add(UserProfileEntity.getEntity(cursor));
                }
            }

            cursor.close();

            for (UserProfileEntity f : friends) {
                f.setLabels(getFriendLabels(f.getId()));
            }
            return friends;
        } catch (Exception e) {
            CELog.e("DBManager.queryFriends error", e.getMessage(), e);
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<UserProfileEntity> queryEmployeeList() {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.UserProfileEntry.TABLE_NAME,
                null,
                DBContract.UserProfileEntry.COLUMN_STATUS + " = ?",
                new String[]{User.Status.ENABLE}, null, null, null);
            List<UserProfileEntity> friends = Lists.newArrayList();
            while (cursor.moveToNext()) {
                friends.add(UserProfileEntity.getEntity(cursor));
            }
            cursor.close();
            for (UserProfileEntity f : friends) {
                f.setLabels(getFriendLabels(f.getId()));
            }
            return friends;
        } catch (Exception e) {
            CELog.e("DBManager.queryFriends error", e.getMessage(), e);
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    private List<Label> getFriendLabels(String id) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = null;
        try {
            String[] selectionArgs = new String[]{id};
            cursor = db.query(DBContract.LabelEntry.TABLE_NAME + " AS f INNER JOIN " + DBContract.FriendsLabelRel.TABLE_NAME + " AS u ON f." + DBContract.LabelEntry._ID + " = u." + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID,
                new String[]{"f." + DBContract.LabelEntry._ID, "f." + DBContract.LabelEntry.COLUMN_NAME},
                "EXISTS (SELECT 1 FROM " + DBContract.FriendsLabelRel.TABLE_NAME + " WHERE "
                    + DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " = f." + DBContract.LabelEntry._ID + " AND "
                    + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + " = ?)",
                selectionArgs, null, null, null);

            List<Label> labels = new ArrayList<>();
            while (cursor.moveToNext()) {
                String labelId = Tools.getDbString(cursor, DBContract.LabelEntry._ID);
                String name = Tools.getDbString(cursor, DBContract.LabelEntry.COLUMN_NAME);
                Label label = new Label();
                label.setId(labelId);
                label.setName(name);
                labels.add(label);
            }
            return labels;
        } catch (Exception e) {
            return new ArrayList<>();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    public void insertSearchHistory(String content, String time) {
        SQLiteDatabase db = openDatabase();

        ContentValues values = new ContentValues();
        values.put(DBContract.SEARCH_HISTORY.COLUMN_TIME, time);
        values.put(DBContract.SEARCH_HISTORY.COLUMN_CONTENT, content);
//        db.beginTransaction();

        Cursor cursor = db.query(DBContract.SEARCH_HISTORY.TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            SearchBean mSearchBean = new SearchBean(
                Tools.getDbInt(cursor, DBContract.SEARCH_HISTORY.COLUMN_ID),
                Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_CONTENT),
                Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_TIME)
            );
//            SearchBean mSearchBean = new SearchBean(Tools.getDbInt(cursor, DBContract.SEARCH_HISTORY.COLUMN_ID), Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_CONTENT));
            if (content.equals(mSearchBean.getContent())) {
                deleteSearchHistoryById(String.valueOf(mSearchBean.getId()));
            }
        }

        db.insert(DBContract.SEARCH_HISTORY.TABLE_NAME, null, values);
        cursor.close();
//        db.setTransactionSuccessful();
//        db.endTransaction();
    }

    public List<SearchBean> querySearchHistory() {
        List<SearchBean> mSearchHistory = Lists.newArrayList();
        try {
            SQLiteDatabase db = openDatabase();
//            db.beginTransaction();
            final String order = DBContract.SEARCH_HISTORY.COLUMN_TIME + " DESC";
            Cursor cursor = db.query(DBContract.SEARCH_HISTORY.TABLE_NAME, null, null, null, null, null, order, "10");
            while (cursor.moveToNext()) {

                mSearchHistory.add(new SearchBean(
                    Tools.getDbInt(cursor, DBContract.SEARCH_HISTORY.COLUMN_ID),
                    Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_CONTENT),
                    Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_TIME)
                ));
//                mSearchHistory.add(new SearchBean(Tools.getDbInt(cursor, DBContract.SEARCH_HISTORY.COLUMN_ID), Tools.getDbString(cursor, DBContract.SEARCH_HISTORY.COLUMN_CONTENT)));
            }
            cursor.close();
//            db.setTransactionSuccessful();
//            db.endTransaction();
        } catch (Exception e) {
            CELog.e("DBManager.querySearchHistory error ", e.getMessage(), e);
        }

        return mSearchHistory;
    }

    public void deleteSearchHistoryById(String id) {
        SQLiteDatabase db = openDatabase();
//        db.beginTransaction();
        String whereClause = DBContract.SEARCH_HISTORY.COLUMN_ID + " = ?";
        String[] whereArgs = new String[]{id};
        db.delete(DBContract.SEARCH_HISTORY.TABLE_NAME, whereClause, whereArgs);
//        db.setTransactionSuccessful();
//        db.endTransaction();
    }

    public void close() {
        // This static method is called by SystemKit.cleanCE() and SystemKit.changeTenant()
        // Its role is to tear down the *current singleton instance* and its resources.
        CELog.d("TenantSwitch", "DBManager.close (static): Called.");
        if (INSTANCE != null) {
            CELog.d("TenantSwitch", "DBManager.close (static): Current INSTANCE found. Closing its instanceHelper for DB: " + INSTANCE.instanceDataBaseName);
            if (INSTANCE.instanceHelper != null) {
                try {
                    INSTANCE.instanceHelper.close();
                } catch (Exception e) {
                    CELog.e("TenantSwitch", "DBManager.close (static): Exception closing instanceHelper for " + INSTANCE.instanceDataBaseName, e);
                }
                INSTANCE.instanceHelper = null;
            }
            INSTANCE.instanceDataBaseName = null; // Clear instance-specific fields
            INSTANCE.selfId = null;
            INSTANCE = null; // Nullify the static singleton holder
            CELog.d("TenantSwitch", "DBManager.close (static): INSTANCE and its helper closed and nulled.");
        } else {
            CELog.d("TenantSwitch", "DBManager.close (static): No current INSTANCE to close.");
        }
        // No more SdkLib.dbHelper to manage here.
    }

    public String queryCustomBossServiceId(String customerId) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = db.query(
            DBContract.ChatRoomEntry.TABLE_NAME,
            null,
            DBContract.ChatRoomEntry.COLUMN_OWNER_ID + " = ? AND " +
                DBContract.ChatRoomEntry.COLUMN_TYPE + " = ? AND " +
                DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + " =? ",
            new String[]{customerId, "services", "BOSS"},
            null,
            null,
            null
        );
        if (cursor.getCount() <= 0) {
            cursor.close();
            return "";
        }
        cursor.moveToFirst();
        String roomId = Tools.getDbString(cursor, DBContract.ChatRoomEntry._ID);
        cursor.close();
        return roomId;
    }

    public ChatRoomEntity queryCustomBossServiceRoom(String customerId) {
        SQLiteDatabase db = openDatabase();
        Cursor cursor = db.query(
            DBContract.ChatRoomEntry.TABLE_NAME,
            null,
            DBContract.ChatRoomEntry.COLUMN_OWNER_ID + " = ? AND " +
                DBContract.ChatRoomEntry.COLUMN_TYPE + " = ? AND " +
                DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + " =? ",
            new String[]{customerId, "services", "BOSS"},
            null,
            null,
            null
        );
        if (cursor.getCount() <= 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();
        Map<String, Integer> index = DBContract.ChatRoomEntry.getIndex(cursor);
        ChatRoomEntity entity = ChatRoomEntity.formatByCursor(index, cursor, false).build();
        cursor.close();
        return entity;
    }

    public String querySourceTypeFromLastMessage(String roomId) {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.LastMessageEntry.TABLE_NAME,
                new String[]{DBContract.LastMessageEntry.COLUMN_SOURCE_TYPE},
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            String sourceType = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_SOURCE_TYPE);
            cursor.close();
            return sourceType;
        } catch (Exception ignored) {
            return "";
        }
    }

    public String queryTypeFromLastMessage(String roomId) {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.LastMessageEntry.TABLE_NAME,
                new String[]{DBContract.LastMessageEntry.COLUMN_TYPE},
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            String type = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_TYPE);
            cursor.close();
            return type;
        } catch (Exception ignored) {
            return "";
        }
    }

    public String querySenderIdFromLastMessage(String roomId) {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.LastMessageEntry.TABLE_NAME,
                new String[]{DBContract.LastMessageEntry.COLUMN_SENDER_ID},
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            String senderId = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_SENDER_ID);
            cursor.close();
            return senderId;
        } catch (Exception ignored) {
            return "";
        }
    }

    public String queryContentFromLastMessage(String roomId) {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.LastMessageEntry.TABLE_NAME,
                new String[]{DBContract.LastMessageEntry.COLUMN_CONTENT},
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            String content = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_CONTENT);
            cursor.close();
            return content;
        } catch (Exception ignored) {
            return "";
        }
    }

    public long querySendTimeFromLastMessage(String roomId) {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.LastMessageEntry.TABLE_NAME,
                new String[]{DBContract.LastMessageEntry.COLUMN_SEND_TIME},
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            long sendTime = Tools.getDbLong(cursor, DBContract.LastMessageEntry.COLUMN_SEND_TIME);
            cursor.close();
            return sendTime;
        } catch (Exception ignored) {
            return 0L;
        }
    }

    public int queryFlagFromLastMessage(String roomId) {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.LastMessageEntry.TABLE_NAME,
                new String[]{DBContract.LastMessageEntry.COLUMN_FLAG},
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            int flag = Tools.getDbInt(cursor, DBContract.LastMessageEntry.COLUMN_FLAG);
            cursor.close();
            return flag;
        } catch (Exception e) {
            return -99;
        }
    }

    public String querySenderNameFromLastMessage(String roomId) {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.LastMessageEntry.TABLE_NAME,
                new String[]{DBContract.LastMessageEntry.COLUMN_SENDER_NAME},
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                new String[]{roomId},
                null,
                null,
                null
            );

            cursor.moveToFirst();
            String senderName = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_SENDER_NAME);
            cursor.close();
            return senderName;
        } catch (Exception ignored) {
            return "";
        }
    }

    public int querySequenceFromLastMessage(String roomId) {
        try {
            SQLiteDatabase db = openDatabase();
            Cursor cursor = db.query(
                DBContract.LastMessageEntry.TABLE_NAME,
                new String[]{DBContract.LastMessageEntry.COLUMN_SEQUENCE},
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " = ?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            int sequence = Tools.getDbInt(cursor, DBContract.LastMessageEntry.COLUMN_SEQUENCE);
            cursor.close();
            return sequence;
        } catch (Exception ignored) {
            return -1;
        }
    }

    public List<UserProfileEntity> queryMembersFromRoomId(String roomId) {
        SQLiteDatabase db = openDatabase();
        List<UserProfileEntity> members = Lists.newArrayList();
        Cursor cursor = db.query(
            DBContract.ChatRoomMemberIdsEntry.TABLE_NAME,
            null,
            DBContract.ChatRoomMemberIdsEntry.COLUMN_ROOM_ID + " = ?",
            new String[]{roomId},
            null,
            null,
            null
        );

        while (cursor.moveToNext()) {
            String memberId = Tools.getDbString(cursor, DBContract.ChatRoomMemberIdsEntry.COLUMN_MEMBER_ID);
            members.add(queryUser(memberId));
        }
        cursor.close();
        return members;
    }

    public List<MessageEntity> getMessageBetweenSequence(String roomId, int firstSequence, int lastSequence) {
        SQLiteDatabase db = openDatabase();
        List<MessageEntity> messageEntities = Lists.newArrayList();
        Cursor cursor = db.query(
            DBContract.MessageEntry.TABLE_NAME,
            null,
            DBContract.MessageEntry.COLUMN_ROOM_ID + " =? AND " + DBContract.MessageEntry.COLUMN_SEQUENCE + " BETWEEN ? AND ?",
            new String[]{roomId, String.valueOf(firstSequence), String.valueOf(lastSequence)},
            null,
            null,
            DBContract.MessageEntry.COLUMN_SEND_TIME + " ASC"
        );
        Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
        while (cursor.moveToNext()) {
            MessageEntity msg = MessageEntity.formatByCursor(index, cursor);
            messageEntities.add(msg);
        }

        return messageEntities;
    }
}
