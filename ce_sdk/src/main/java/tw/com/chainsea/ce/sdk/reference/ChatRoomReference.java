package tw.com.chainsea.ce.sdk.reference;

import static tw.com.chainsea.ce.sdk.database.DBContract.AccountRoomRel;
import static tw.com.chainsea.ce.sdk.database.DBContract.ChatRoomEntry;
import static tw.com.chainsea.ce.sdk.database.DBContract.MessageEntry;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.bean.BadgeDataModel;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
import tw.com.chainsea.ce.sdk.bean.servicenumber.type.GroupPrivilegeEnum;
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse;
import tw.com.chainsea.ce.sdk.network.model.response.SyncRoomNormalResponse;
import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;

/**
 * current by evan on 2020-02-05
 */
public class ChatRoomReference extends AbsReference {

    private static ChatRoomReference INSTANCE;

    public static ChatRoomReference getInstance() {
        if (INSTANCE == null) {
            synchronized (ChatRoomReference.class) {
                INSTANCE = new ChatRoomReference();
            }
        }
        return INSTANCE;
    }

    // private SQLiteDatabase getDb() { // This method will be removed after refactoring all its usages.
    //     return DBManager.getInstance().openDatabase();
    // }


    public synchronized boolean save(List<ChatRoomEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return true;
        }
        try {
            long dateTime = System.currentTimeMillis();
            boolean result = true;
            Multimap<String, String> multimap = ArrayListMultimap.create();
            // Note: This method calls save(ChatRoomEntity entity) in a loop.
            // The 'save(entity)' call itself will be refactored to use a consistent DBManager instance for its own scope.
            // For this method, direct DB operations like AccountRoomRelReference are the main concern for now.
            DBManager currentDbManager = DBManager.getInstance();
            SQLiteDatabase db = currentDbManager.openDatabase();
            if (db == null) {
                CELog.e("ChatRoomReference.save(List)", "Failed to obtain database. Tenant switch may be in progress or DBManager not initialized.");
                return false;
            }

            for (ChatRoomEntity entity : entities) {
                // Each call to save(entity) will manage its own DBManager instance and transaction context if needed.
                // Alternatively, save(entity) could be refactored to accept 'db' as a parameter if part of a larger transaction.
                // For this iteration, assuming save(entity) is self-contained or handles its DB instance correctly.
                boolean isSuccess = save(entity); // This will be refactored separately.
                result = result && isSuccess;
                // If save(entity) populates multimap correctly based on its own DB context, this is fine.
                // However, multimap should ideally be populated by the save(entity) call itself if it does DB work.
                // For now, assuming multimap is related to the entities list directly, not DB state from this method.
            }
            AccountRoomRelReference.batchSaveByRoomIdsAndAccountIds(db, multimap); // Use the db instance from this method
            CELog.d(String.format("save room entities and batch replace Account Room Rel, count->%s, use time->%s/second  ", entities.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    //for first sync method
    public synchronized boolean syncSave(List<ChatRoomEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return true;
        }
        try {
            long dateTime = System.currentTimeMillis();
            boolean result = true;
            Multimap<String, String> multimap = ArrayListMultimap.create();
            DBManager currentDbManager = DBManager.getInstance();
            SQLiteDatabase db = currentDbManager.openDatabase();
            if (db == null) {
                CELog.e("ChatRoomReference.syncSave(List)", "Failed to obtain database. Tenant switch may be in progress or DBManager not initialized.");
                return false;
            }

            for (ChatRoomEntity entity : entities) {
                // Similar to save(List), syncSave(entity) will be refactored separately.
                boolean isSuccess = syncSave(entity); // This will be refactored separately.
                result = result && isSuccess;
            }
            AccountRoomRelReference.batchSaveByRoomIdsAndAccountIds(db, multimap); // Use the db instance from this method
            CELog.d(String.format("save room entities and batch replace Account Room Rel, count->%s, use time->%s/second  ", entities.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public synchronized boolean save(ChatRoomEntity entity) {
        DBManager currentDbManager = DBManager.getInstance();
        SQLiteDatabase db = currentDbManager.openDatabase();
        if (db == null) {
            CELog.e("ChatRoomReference.save(ChatRoomEntity)", "Failed to obtain database. Tenant switch may be in progress or DBManager not initialized. RoomId: " + entity.getId());
            return false;
        }

        try {
            // It's important that getUnReadNumberById is also refactored to use a consistent DB instance.
            // For this change, we assume getUnReadNumberById will be refactored or its current behavior is acceptable for now.
            // If getUnReadNumberById internally calls DBManager.getInstance().openDatabase(), it might get a different context
            // if a tenant switch happens exactly during this method's execution.
            // A more robust solution would be to pass 'db' to getUnReadNumberById or make it use currentDbManager.
            if (entity.getUnReadNum() == 0) {
                 // Temporarily, let getUnReadNumberById handle its own DB access.
                 // This specific call might still be problematic if a switch occurs mid-save.
                if (getUnReadNumberById(entity.getId(), currentDbManager) == -1) {
                    entity.setUnReadNum(-1);
                }
            }
            if (entity.getLastMessage() == null && entity.getUnReadNum() <= 0)
                entity.setInitUpdateTime(0L);

            ContentValues lastMessageValues;
            if (entity.getLastMessage() != null) {
                lastMessageValues = ChatRoomEntity.getLastMessageContentValues(entity.getLastMessage());
                db.replace(DBContract.LastMessageEntry.TABLE_NAME, null, lastMessageValues);
            } else {
                lastMessageValues = ChatRoomEntity.getLastMessageContentValues(entity.getId());
                db.update(DBContract.LastMessageEntry.TABLE_NAME, lastMessageValues, DBContract.LastMessageEntry.COLUMN_ROOM_ID + " =? ", new String[]{entity.getId()});
            }

            Multimap<String, String> multimap = ArrayListMultimap.create(); // Keep multimap local to this save operation
            multimap.putAll(entity.getId(), Sets.newHashSet(entity.getMemberIds()));

            if (ChatRoomSource.ALL.equals(entity.getListClassify())) {
                String selfUserId = TokenPref.getInstance(SdkLib.getAppContext()).getUserId();
                setChatType(entity, selfUserId);
            }
            ContentValues values = ChatRoomEntity.getContentValues(entity);
            long _id = db.replace(ChatRoomEntry.TABLE_NAME, null, values);

            if (entity.getUnReadNum() == 0) {
                if (getUnReadNumberById(entity.getId(), currentDbManager) == -1) { // Pass currentDbManager
                    entity.setUnReadNum(-1);
                }
            }
            AccountRoomRelReference.batchSaveByRoomIdsAndAccountIds(db, multimap);
            return _id > 0;
        } catch (Exception e) {
            CELog.e("ChatRoomReference.save(ChatRoomEntity)", "Exception during save for RoomId: " + entity.getId() + " - " + e.getMessage(), e);
            return false;
        }
        // Note: db is not closed here as it's managed by DBManager instance lifecycle or higher-level transaction.
    }

    //for first sync method
    public synchronized boolean syncSave(ChatRoomEntity entity) {
        DBManager currentDbManager = DBManager.getInstance();
        SQLiteDatabase db = currentDbManager.openDatabase();
        if (db == null) {
            CELog.e("ChatRoomReference.syncSave(ChatRoomEntity)", "Failed to obtain database. Tenant switch may be in progress or DBManager not initialized. RoomId: " + entity.getId());
            return false;
        }

        try {
            Multimap<String, String> multimap = ArrayListMultimap.create();
            if (entity.getUnReadNum() == 0) {
                if (getUnReadNumberById(entity.getId(), currentDbManager) == -1) { // Pass currentDbManager
                    entity.setUnReadNum(-1);
                }
            }
            if (entity.getLastMessage() == null && entity.getUnReadNum() <= 0)
                entity.setInitUpdateTime(0L);
            else if (entity.getLastMessage() != null) {
                entity.setInitUpdateTime(entity.getLastMessage().getSendTime());
            }
            for (String id : entity.getMemberIds()) {
                ContentValues contentValue = ChatRoomEntity.getMemberId(entity.getId(), id);
                db.replace(DBContract.ChatRoomMemberIdsEntry.TABLE_NAME, null, contentValue);
            }
            if (entity.getLastMessage() != null) {
                ContentValues lastMessageValues = ChatRoomEntity.getLastMessageContentValues(entity.getLastMessage());
                db.replace(DBContract.LastMessageEntry.TABLE_NAME, null, lastMessageValues);
            }
            multimap.putAll(entity.getId(), Sets.newHashSet(entity.getMemberIds()));
            if (ChatRoomSource.ALL.equals(entity.getListClassify())) {
                String selfUserId = TokenPref.getInstance(SdkLib.getAppContext()).getUserId();
                setChatType(entity, selfUserId);
            }
            ContentValues values = ChatRoomEntity.getContentValues(entity);
            long _id = db.replace(ChatRoomEntry.TABLE_NAME, null, values);

            if (entity.getUnReadNum() == 0) {
                if (getUnReadNumberById(entity.getId(), currentDbManager) == -1) { // Pass currentDbManager
                    entity.setUnReadNum(-1);
                }
            }
            AccountRoomRelReference.batchSaveByRoomIdsAndAccountIds(db, multimap);
            return _id > 0;
        } catch (Exception e) {
            Log.e("ChatRoomReference", "syncSave error=" + e.getMessage());
            return false;
        }
    }

    public synchronized boolean saveChatRoomFromSync(ChatRoomEntity entity) {
        DBManager currentDbManager = DBManager.getInstance();
        SQLiteDatabase db = currentDbManager.openDatabase();
        if (db == null) {
            CELog.e("ChatRoomReference.saveChatRoomFromSync", "Failed to obtain database. Tenant switch may be in progress or DBManager not initialized. RoomId: " + entity.getId());
            return false;
        }
        try {
            Multimap<String, String> multimap = ArrayListMultimap.create();
            if (entity.getUnReadNum() == 0) {
                if (getUnReadNumberById(entity.getId(), currentDbManager) == -1) { // Pass currentDbManager
                    entity.setUnReadNum(-1);
                }
            }
            if (entity.getLastMessage() == null && entity.getUnReadNum() <= 0)
                entity.setInitUpdateTime(0L);
            else if (entity.getLastMessage() != null) {
                entity.setInitUpdateTime(entity.getLastMessage().getSendTime());
            }
            for (String id : entity.getMemberIds()) {
                ContentValues contentValue = ChatRoomEntity.getMemberId(entity.getId(), id);
                db.replace(DBContract.ChatRoomMemberIdsEntry.TABLE_NAME, null, contentValue);
            }
            if (entity.getLastMessage() != null) {
                ContentValues lastMessageValues = ChatRoomEntity.getLastMessageContentValues(entity.getLastMessage());
                db.replace(DBContract.LastMessageEntry.TABLE_NAME, null, lastMessageValues);
            }

            multimap.putAll(entity.getId(), Sets.newHashSet(entity.getMemberIds()));
            if (ChatRoomSource.ALL.equals(entity.getListClassify())) {
                String selfUserId = TokenPref.getInstance(SdkLib.getAppContext()).getUserId();
                setChatType(entity, selfUserId);
            }
            ContentValues values = ChatRoomEntity.getContentValues(entity);
            long _id = db.replace(ChatRoomEntry.TABLE_NAME, null, values);

            if (entity.getUnReadNum() == 0) {
                if (getUnReadNumberById(entity.getId(), currentDbManager) == -1) { // Pass currentDbManager
                    entity.setUnReadNum(-1);
                }
            }
            AccountRoomRelReference.batchSaveByRoomIdsAndAccountIds(db, multimap);
            return _id > 0;
        } catch (Exception e) {
            CELog.e("ChatRoomReference.saveChatRoomFromSync", "Exception during save for RoomId: " + entity.getId() + " - " + e.getMessage(), e);
            return false;
        }
    }

    // Overload getUnReadNumberById to accept a DBManager instance or SQLiteDatabase
    // This is a helper, ensure it's used by the main save methods.
    private int getUnReadNumberById(String roomId, DBManager dbManager) {
        SQLiteDatabase db = dbManager.openDatabase();
        if (db == null) {
            CELog.e("ChatRoomReference.getUnReadNumberById", "Failed to obtain database for roomId: " + roomId);
            return 0; // Or some other default/error indicator
        }
        try {
            Cursor cursor = db.query(
                ChatRoomEntry.TABLE_NAME,
                new String[]{ChatRoomEntry.COLUMN_UNREAD_NUMBER},
                ChatRoomEntry._ID + "=?",
                new String[]{roomId},
                null,
                null,
                null
            );
            if (cursor.getCount() == 0) {
                cursor.close();
                return 0;
            }
            cursor.moveToFirst();
            int unreadNumber = cursor.getInt(0);
            cursor.close();
            return unreadNumber;
        } catch (Exception e) {
            CELog.e("ChatRoomReference.getUnReadNumberById", "Exception for roomId: " + roomId + " - " + e.getMessage(), e);
            return 0;
        }
    }


    //聊天室&服務號分類
    private void setChatType(ChatRoomEntity chatRoomEntity, String userId) {
        //聊天室或服務號分類
        if (ChatRoomType.serviceMember.equals(chatRoomEntity.getType())) {
            if (ServiceNumberType.BOSS.equals(chatRoomEntity.getServiceNumberType()) && userId.equals(chatRoomEntity.getServiceNumberOwnerId())) { 
                chatRoomEntity.setListClassify(ChatRoomSource.MAIN);
            } else {
                chatRoomEntity.setListClassify(ChatRoomSource.SERVICE);
            }
        } else if (ChatRoomType.services.equals(chatRoomEntity.getType())) {
            if (userId.equals(chatRoomEntity.getOwnerId())) { // Use userId.equals pattern
                chatRoomEntity.setType(ChatRoomType.subscribe);
                chatRoomEntity.setListClassify(ChatRoomSource.MAIN);
            } else if (ServiceNumberType.BOSS.equals(chatRoomEntity.getServiceNumberType()) && userId.equals(chatRoomEntity.getServiceNumberOwnerId())) { 
                chatRoomEntity.setListClassify(ChatRoomSource.MAIN);
            } else if (chatRoomEntity.getProvisionalIds().contains(userId)) {
                chatRoomEntity.setListClassify(ChatRoomSource.MAIN); 
            } else {
                chatRoomEntity.setListClassify(ChatRoomSource.SERVICE);
            }
        } else {
            chatRoomEntity.setListClassify(ChatRoomSource.MAIN);
        }
    }

    // This is the original getUnReadNumberById. It will be replaced by the overloaded one
    // or refactored if still needed by non-critical paths.
    // For now, calls from save/syncSave are updated to use the new overloaded version.
    // public int getUnReadNumberById(String roomId) { 
    //     DBManager currentDbManager = DBManager.getInstance();
    //     SQLiteDatabase db = currentDbManager.openDatabase();
    //     if (db == null) {
    //         CELog.e("ChatRoomReference.getUnReadNumberById (original)", "Failed to obtain database for roomId: " + roomId);
    //         return 0;
    //     }
    //     try {
    //         Cursor cursor = db.query(
    //             ChatRoomEntry.TABLE_NAME,
    //             new String[]{ChatRoomEntry.COLUMN_UNREAD_NUMBER},
    //             ChatRoomEntry._ID + "=?",
    //             new String[]{roomId},
    //             null,
    //             null,
    //             null
    //         );
    //         if (cursor.getCount() == 0) {
    //             cursor.close();
    //             return 0;
    //         }
    //         cursor.moveToFirst();
    //         int unreadNumber = cursor.getInt(0);
    //         cursor.close();
    //         return unreadNumber;
    //     } catch (Exception e) {
    //         CELog.e("ChatRoomReference.getUnReadNumberById (original)", "Exception for roomId: " + roomId + " - " + e.getMessage(), e);
    //         return 0;
    //     }
    // }

    /**
     * checking If There Is Information
     */
    public boolean hasLocalData(String roomId) {
        if (Strings.isNullOrEmpty(roomId)) {
            return false;
        }
        try {
//            String sql = "SELECT r._id FROM " + ChatRoomEntry.TABLE_NAME + " AS r "
//                    + " WHERE r." + ChatRoomEntry._ID + "=?";
//            Cursor cursor = getDb().rawQuery(sql, new String[]{roomId});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME + " AS r ",
                new String[]{"r._id"},
                "r." + ChatRoomEntry._ID + "=?",
                new String[]{roomId},
                null,
                null,
                null
            );
            int count = cursor.getCount();
            cursor.close();
            return count > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public ChatRoomEntity findSelfRoom(String ownerId) {
        try {
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_OWNER_ID + " =? " +
                    " AND " + ChatRoomEntry.COLUMN_TYPE + " =?",
                new String[]{ownerId, ChatRoomType.person.name()},
                null,
                null,
                null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }

            cursor.moveToFirst();
//            List<MessageEntity> unreadMessages = MessageReference.findUnreadMessagesByRoomId(getDb(), roomId);
            Map<String, Integer> index = ChatRoomEntry.getIndex(cursor);
            ChatRoomEntity entity = ChatRoomEntity.formatByCursor(index, cursor, false).build();
//            ChatRoomEntity entity = assemblyDetail(getDb(), retCursor, userId, true, true, true, true, false, false);
            cursor.close();
            return entity;
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }

        return null;
    }

    public ChatRoomEntity findById(String roomId) {
        if (roomId == null) {
            return null;
        }
        try {
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME +
//                    " WHERE " + ChatRoomEntry._ID + "=?";
//            Cursor retCursor = getDb().rawQuery(sql, new String[]{roomId});
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry._ID + "=?",
                new String[]{roomId},
                null,
                null,
                null
            );
            if (retCursor.getCount() <= 0) {
                retCursor.close();
                return null;
            }

            retCursor.moveToFirst();
            List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(getDb(), roomId);

            List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(roomId);
            MessageEntity lastMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), roomId, MessageStatus.getValidStatus(), MessageReference.Sort.DESC);
            MessageEntity failedMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), roomId, MessageStatus.getFailedErrorStatus(), MessageReference.Sort.DESC);
//            List<MessageEntity> unreadMessages = MessageReference.findUnreadMessagesByRoomId(getDb(), roomId);
            Map<String, Integer> index = ChatRoomEntry.getIndex(retCursor);
            ChatRoomEntity entity = ChatRoomEntity.formatByCursor(index, retCursor, false, memberIds, members, lastMessage, failedMessage).build();
//            ChatRoomEntity entity = assemblyDetail(getDb(), retCursor, userId, true, true, true, true, false, false);
            retCursor.close();
            return entity;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return null;
        }
    }

    public ChatRoomEntity findById2(String userId, String roomId, boolean needLastMessage, boolean needFailedMessage, boolean needMembersProfile, boolean needCheckFavourite, boolean needCheckUnreadAtMessage) {
        if (roomId == null) {
            return null;
        }

        try {
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME +
//                    " WHERE " + ChatRoomEntry._ID + "=?";
//            Cursor cursor = getDb().rawQuery(sql, new String[]{roomId});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry._ID + "=?",
                new String[]{roomId},
                null,
                null,
                null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            Map<String, Integer> index = ChatRoomEntry.getIndex();
            return assemblyDetail(index, cursor, userId, needLastMessage, needFailedMessage, needMembersProfile, needCheckFavourite, needCheckUnreadAtMessage, false);
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return null;
        }
    }

    public ChatRoomEntity findSelfRoomBySelfId(String selfId) {
        try {
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry.COLUMN_OWNER_ID + "=?"
//                    + " AND " + ChatRoomEntry.COLUMN_TYPE + " =?";
//            Cursor cursor = getDb().rawQuery(sql, new String[]{selfId, ChatRoomType.person.name()});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_OWNER_ID + "=? AND " + ChatRoomEntry.COLUMN_TYPE + " =?",
                new String[]{selfId, ChatRoomType.person.name()},
                null,
                null,
                null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            Map<String, Integer> index = ChatRoomEntry.getIndex(cursor);
            return assemblyDetail(index, cursor, selfId, true, true, true, true, true, false);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }

    }

    public ChatRoomEntity findByRoomIdAndServiceNumberId(String roomId, String serviceNumberId) {
        if (roomId == null) {
            return null;
        }
        try {
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry._ID + "=?"
//                    + " AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + "=?";
//            Cursor retCursor = getDb().rawQuery(sql, new String[]{roomId, serviceNumberId});
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry._ID + "=? AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + "=?",
                new String[]{roomId, serviceNumberId},
                null,
                null,
                null
            );
            if (retCursor.getCount() <= 0) {
                retCursor.close();
                return null;
            }

            retCursor.moveToFirst();
            List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(getDb(), roomId);

            List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(roomId);
            MessageEntity lastMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), roomId, MessageStatus.getValidStatus(), MessageReference.Sort.DESC);
            MessageEntity failedMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), roomId, MessageStatus.getFailedErrorStatus(), MessageReference.Sort.DESC);
//            List<MessageEntity> unreadMessages = MessageReference.findUnreadMessagesByRoomId(getDb(), roomId);
            Map<String, Integer> index = ChatRoomEntry.getIndex(retCursor);
            ChatRoomEntity entity = ChatRoomEntity.formatByCursor(index, retCursor, false, memberIds, members, lastMessage, failedMessage).build();
            retCursor.close();
            return entity;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return null;
        }
    }

    public List<ChatRoomMemberResponse> queryChatRoomMember(String roomId) {
//        String sql = "SELECT " + ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER + " FROM " + ChatRoomEntry.TABLE_NAME + " WHERE " + ChatRoomEntry._ID + " =?";
        try {
//            Cursor cursor = getDb().rawQuery(sql, new String[]{roomId});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                new String[]{ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER},
                ChatRoomEntry._ID + " =?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            String data = Tools.getDbString(cursor, 0);
            cursor.close();
            Type typeToken = new TypeToken<List<ChatRoomMemberResponse>>() {
            }.getType();
            return JsonHelper.getInstance().from(data, typeToken);
        } catch (Exception e) {
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomMemberResponse> queryChatMembers(String roomId) {
        try {
            Cursor cursor = getDb().query(
                DBContract.ChatMemberEntry.TABLE_NAME,
                null,
                DBContract.ChatMemberEntry.COLUMN_ROOM_ID + " =?",
                new String[]{roomId},
                null,
                null,
                null
            );

            List<ChatRoomMemberResponse> entities = Lists.newArrayList();
            while (cursor.moveToNext()) {
                // Extracting values from the cursor
                int firstSequence = Tools.getDbInt(cursor, DBContract.ChatMemberEntry.COLUMN_FIRST_SEQUENCE);
                boolean deleted = !Objects.equals(Tools.getDbString(cursor, DBContract.ChatMemberEntry.COLUMN_DELETED), "N");
                SourceType sourceType = SourceType.valueOf(Tools.getDbString(cursor, DBContract.ChatMemberEntry.COLUMN_SOURCE_TYPE));
                int lastReadSequence = Tools.getDbInt(cursor, DBContract.ChatMemberEntry.COLUMN_LAST_READ_SEQUENCE);
                long joinTime = Tools.getDbLong(cursor, DBContract.ChatMemberEntry.COLUMN_JOIN_TIME);
                int lastReceivedSequence = Tools.getDbInt(cursor, DBContract.ChatMemberEntry.COLUMN_LAST_RECEIVED_SEQUENCE);
                long updateTime = Tools.getDbLong(cursor, DBContract.ChatMemberEntry.COLUMN_UPDATE_TIME);
                String type = Tools.getDbString(cursor, DBContract.ChatMemberEntry.COLUMN_TYPE);
                String memberId = Tools.getDbString(cursor, DBContract.ChatMemberEntry.COLUMN_MEMBER_ID);

                // Creating and adding ChatRoomMemberResponse object
                ChatRoomMemberResponse memberResponse = new ChatRoomMemberResponse(
                    firstSequence,
                    deleted,
                    sourceType,
                    lastReadSequence,
                    joinTime,
                    lastReceivedSequence,
                    updateTime,
                    type,
                    memberId,
                    GroupPrivilegeEnum.Common
                );

                entities.add(memberResponse);
            }
            cursor.close();
            return entities;
        } catch (Exception ignored) {
            return Lists.newArrayList();
        }
    }

    /**
     * Open DataBase, query "chat_room" table, get ChatRoomEntities
     *
     * @param source
     * @param userId
     * @return
     */
    public List<ChatRoomEntity> findAllChatRoomSource(ChatRoomSource source, String userId) {
        try {
            long dateTime = System.currentTimeMillis();
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = ?";// + " limit " + limit;

            List<ChatRoomEntity> entities = Lists.newArrayList();
//            Cursor retCursor = getDb().rawQuery(sql, new String[]{source.name(), "N"});
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = ?",
                new String[]{source.name(), "N"},
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(retCursor, userId, true, true, false, false));
            retCursor.close();
            Log.d("Kyle111", String.format("findAllChatRoomSource count->%s, use time->%s/秒  ", entities.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    /**
     * Open DataBase, query "chat_room" table, get ChatRoomEntities
     *
     * @param offset 從第幾筆開始抓
     * @param limit  限制幾筆
     * @return
     */
    public List<ChatRoomEntity> findMainChatRoomData(int offset, int limit) {
        try {
            long dateTime = System.currentTimeMillis();
            List<ChatRoomEntity> result = Lists.newArrayList();
            //建立索引加速查詢
            getDb().execSQL("CREATE INDEX IF NOT EXISTS idx_lastmessage_roomid_sendtime " +
                "ON " + DBContract.LastMessageEntry.TABLE_NAME + "(" + DBContract.LastMessageEntry.COLUMN_ROOM_ID + ", " + DBContract.LastMessageEntry.COLUMN_SEND_TIME + " DESC)");

//            String sql = "SELECT c.*, COALESCE(l."+DBContract.LastMessageEntry.COLUMN_SEND_TIME+", 0) FROM "+
//                    ChatRoomEntry.TABLE_NAME+" c "+
//                    "INNER JOIN ("+
//                    "SELECT roomId, MAX(sendTime) as sendTime "+
//                    "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
//                    " GROUP BY roomId"+
//                    ") l ON c." + ChatRoomEntry._ID+ " = l."+DBContract.LastMessageEntry.COLUMN_ROOM_ID+
//                    " WHERE c."+ ChatRoomEntry.COLUMN_LIST_CLASSIFY+" = ? "+
//                    "AND c."+ ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED+" = ? "+
//                    "AND "+ ChatRoomEntry.COLUMN_TYPE +" != ? "+
//                    "AND (COALESCE(c."+ChatRoomEntry.COLUMN_DFR_TIME+", 0) <= 0 OR COALESCE(c."+ChatRoomEntry.COLUMN_DFR_TIME+", 0) < l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ") "+ //不顯示隱藏聊天室
//                    "ORDER BY " + ChatRoomEntry.COLUMN_SORT_WEIGHTS + " DESC, "
//                    + "COALESCE(c."+ ChatRoomEntry.COLUMN_UPDATE_TIME+",0) DESC,"
//                    + "COALESCE(l."+DBContract.LastMessageEntry.COLUMN_SEND_TIME+", 0) DESC "+
//                    "LIMIT "+limit+" OFFSET "+offset;
//            Cursor retCursor = getDb().rawQuery(sql, new String[]{"MAIN", "N", "broadcast"});
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME + " c " +
                    "LEFT JOIN (" +
                    "SELECT roomId, MAX(sendTime) as sendTime " +
                    "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
                    " GROUP BY roomId" +
                    ") l ON c." + ChatRoomEntry._ID + " = l." + DBContract.LastMessageEntry.COLUMN_ROOM_ID,
                new String[]{"c.*", "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0)"},
                "c." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = ? " +
                    "AND c." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = ? " +
                    "AND " + ChatRoomEntry.COLUMN_TYPE + " != ? " +
                    "AND (COALESCE(c." + ChatRoomEntry.COLUMN_DFR_TIME + ", 0) <= 0 OR COALESCE(c." + ChatRoomEntry.COLUMN_DFR_TIME + ", 0) < l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ") ",
                new String[]{"MAIN", "N", "broadcast"},
                null,
                null,
                ChatRoomEntry.COLUMN_SORT_WEIGHTS + " DESC, "
                    + "COALESCE(c." + ChatRoomEntry.COLUMN_UPDATE_TIME + ",0) DESC,"
                    + "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) DESC",
                limit + " OFFSET " + offset
            );
            while (retCursor.moveToNext()) {
                Map<String, Integer> index = ChatRoomEntry.getIndex(retCursor);
                ChatRoomEntity entity = ChatRoomEntity.formatByCursor(index, retCursor, false).build();
                result.add(entity);
            }
            retCursor.close();
            Log.d("Kyle116", String.format("findAllChatRoomSource offset-> %s, count->%s, use time->%s/秒  ", offset, result.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    /**
     * Open DataBase, query "chat_room" table
     *
     * @param offset 從第幾筆開始抓
     * @param limit  限制幾筆
     * @return List<ChatRoomEntity>
     */
    public List<ChatRoomEntity> queryServiceRoomByServiceNumberId(String serviceNumberId, int offset, int limit) {
        try {
            long dateTime = System.currentTimeMillis();
            List<ChatRoomEntity> result = Lists.newArrayList();
            //建立索引加速查詢
            getDb().execSQL("CREATE INDEX IF NOT EXISTS idx_lastmessage_roomid_sendtime " +
                "ON " + DBContract.LastMessageEntry.TABLE_NAME + "(" + DBContract.LastMessageEntry.COLUMN_ROOM_ID + ", " + DBContract.LastMessageEntry.COLUMN_SEND_TIME + " DESC)");

//            String sql = "SELECT c.*, COALESCE(l."+DBContract.LastMessageEntry.COLUMN_SEND_TIME+", 0) FROM "+
//                    ChatRoomEntry.TABLE_NAME+" c "+
//                    "INNER JOIN ("+
//                    "SELECT roomId, MAX(sendTime) as sendTime "+
//                    "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
//                    " GROUP BY roomId"+
//                    ") l ON c." + ChatRoomEntry._ID+ " = l."+DBContract.LastMessageEntry.COLUMN_ROOM_ID+
//                    " WHERE c."+ ChatRoomEntry.COLUMN_LIST_CLASSIFY+" = ? "+
//                    "AND c."+ ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED+" = ? "+
//                    "AND "+ ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID +" = ?"+
//                    "AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " != '" + ServiceNumberStatus.ON_LINE.getStatus() +"' " +
//                    "ORDER BY COALESCE(l."+DBContract.LastMessageEntry.COLUMN_SEND_TIME+", 0) DESC " +
//                    "LIMIT "+limit+" OFFSET "+offset;
//            Cursor retCursor = getDb().rawQuery(sql, new String[]{"SERVICE", "N", serviceNumberId});

            String limitSql = "";
            if (limit > 0) {
                limitSql += limit + " OFFSET " + offset;
            }
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME + " c " +
                    "LEFT JOIN (" +
                    "SELECT roomId, MAX(sendTime) as sendTime " +
                    "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
                    " GROUP BY roomId" +
                    ") l ON c." + ChatRoomEntry._ID + " = l." + DBContract.LastMessageEntry.COLUMN_ROOM_ID,
                new String[]{"c.*", "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0)"},
                "(c." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = ? " +
                    "AND c." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = ? " +
                    "AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = ?" +
                    "AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " != '" + ServiceNumberStatus.ON_LINE.getStatus() + "'" +
                    ") OR (c." + ChatRoomEntry.COLUMN_TYPE + " =? AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =? )",
                new String[]{"SERVICE", "N", serviceNumberId, ChatRoomType.serviceMember.name()},
                null,
                null,
                "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) DESC ",
                limitSql
            );
            while (retCursor.moveToNext()) {
                Map<String, Integer> index = ChatRoomEntry.getIndex(retCursor);
                ChatRoomEntity entity = ChatRoomEntity.formatByCursor(index, retCursor, false).build();
                result.add(entity);
            }
            retCursor.close();
            Log.d("Kyle117", String.format("queryServiceRoomByServiceNumberId id -> %s, offset-> %s, count->%s, use time->%s/秒  ", serviceNumberId, offset, result.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    /**
     * 取得除了服務號分組以外的聊天室
     *
     * @return List<ChatRoomEntity>
     */
    public List<ChatRoomEntity> queryOnlineServiceRoom(String selfUserId) {
        try {
            long dateTime = System.currentTimeMillis();
            List<ChatRoomEntity> result = Lists.newArrayList();
            //建立索引加速查詢
            getDb().execSQL("CREATE INDEX IF NOT EXISTS idx_roomid " +
                "ON " + DBContract.ChatRoomEntry.TABLE_NAME + "(" + ChatRoomEntry._ID + ")");
            String query = QueryServiceNumberChatRoom.INSTANCE.buildFormalQuery(selfUserId);
//            Cursor retCursor = getDb().rawQuery(query, null);
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                query,
                null,
                null,
                null,
                null
            );
            while (retCursor.moveToNext()) {
                Map<String, Integer> index = ChatRoomEntry.getIndex(retCursor);
                ChatRoomEntity entity = ChatRoomEntity.formatByCursor(index, retCursor, false).build();
                Log.d("Kyle117", "queryOnlineServiceRoom name=" + entity.getName() + ", status=" + entity.getServiceNumberStatus().name() + ", type = " + entity.getListClassify());
                result.add(entity);
            }
            retCursor.close();
            Log.d("Kyle117", String.format("queryOnlineServiceRoom count->%s, use time->%s/秒  ", result.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomEntity> queryOnlineServiceRoomByTime(int offset, int limit) {
        try {
            long dateTime = System.currentTimeMillis();
            List<ChatRoomEntity> result = Lists.newArrayList();
            //建立索引加速查詢
            getDb().execSQL("CREATE INDEX IF NOT EXISTS idx_lastmessage_roomid_sendtime " +
                "ON " + DBContract.LastMessageEntry.TABLE_NAME + "(" + DBContract.LastMessageEntry.COLUMN_ROOM_ID + ", " + DBContract.LastMessageEntry.COLUMN_SEND_TIME + " DESC)");

//            String sql = "SELECT c.*, COALESCE(l."+DBContract.LastMessageEntry.COLUMN_SEND_TIME+", 0) FROM "+
//                    ChatRoomEntry.TABLE_NAME+" c "+
//                    "INNER JOIN ("+
//                    "SELECT roomId, MAX(sendTime) as sendTime "+
//                    "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
//                    " GROUP BY roomId "+
//                    ") l ON c." + ChatRoomEntry._ID+ " = l."+DBContract.LastMessageEntry.COLUMN_ROOM_ID+
//                    " WHERE c."+ ChatRoomEntry.COLUMN_LIST_CLASSIFY+" = ? "+
//                    "AND c."+ ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED+" = ? "+
//                    "AND (c."+ ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " != '" + ServiceNumberStatus.ON_LINE.name()+ "' " +
//                    "AND  c." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID +" = '')" +
//                    "ORDER BY COALESCE(l."+DBContract.LastMessageEntry.COLUMN_SEND_TIME+", 0) DESC " +
//                    "LIMIT " + limit + " OFFSET " + offset;
//            Cursor retCursor = getDb().rawQuery(sql, new String[]{"SERVICE", "N"});
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME + " c " +
                    "INNER JOIN (" +
                    "SELECT roomId, MAX(sendTime) as sendTime " +
                    "FROM " + DBContract.LastMessageEntry.TABLE_NAME +
                    " GROUP BY roomId " +
                    ") l ON c." + ChatRoomEntry._ID + " = l." + DBContract.LastMessageEntry.COLUMN_ROOM_ID,
                new String[]{"c.*", "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0)"},
                "c." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = ? " +
                    "AND c." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = ? " +
                    "AND (c." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " != '" + ServiceNumberStatus.ON_LINE.name() + "' " +
                    "AND  c." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID + " = '')",
                new String[]{"SERVICE", "N"},
                null,
                null,
                "COALESCE(l." + DBContract.LastMessageEntry.COLUMN_SEND_TIME + ", 0) DESC ",
                limit + " OFFSET " + offset
            );
            while (retCursor.moveToNext()) {
                Map<String, Integer> index = ChatRoomEntry.getIndex(retCursor);
                ChatRoomEntity entity = ChatRoomEntity.formatByCursor(index, retCursor, false).build();
                Log.d("Kyle117", "queryOnlineServiceRoomByTime name=" + entity.getName() + ", status=" + entity.getServiceNumberStatus().name() + ", type = " + entity.getListClassify());
                result.add(entity);
            }
            retCursor.close();
            Log.d("Kyle117", String.format("queryOnlineServiceRoomByTime offset=%s, limit=%s, count->%s, use time->%s/秒  ", offset, limit, result.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    /**
     * findRoomByChatRoomSource 使用案例：
     * 當內部服務號成員進線內部服務號並觸發AI服務時，
     * 其他成員接手服務後，應該解除AI服務狀態，從ROBOT SERVICE 轉至 ONLINE
     * 但進線者因為 list_classify = MAIN 以致於撈資料失誤，
     * 故改採以下方法取得正確的資料
     */
    public List<ChatRoomEntity> findRoomByChatRoomSource(ChatRoomSource source, String userId) {
        try {
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE (" + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? OR (" + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = '"+ChatRoomSource.MAIN+"'"
//                    + " AND " + ChatRoomEntry.COLUMN_TYPE + " = ?))"
//                    +" AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = ?";// + " limit " + limit;

            List<ChatRoomEntity> entities = Lists.newArrayList();
//            Cursor retCursor = getDb().rawQuery(sql, new String[]{source.name(), "subscribe", "N"});
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                "(" + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? OR (" + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " = '" + ChatRoomSource.MAIN + "'"
                    + " AND " + ChatRoomEntry.COLUMN_TYPE + " = ?)) AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = ?",
                new String[]{source.name(), "subscribe", "N"},
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(retCursor, userId, true, true, false, false));
            retCursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomMemberResponse> getChatMember(String roomId) {
        try {
//            String sql = "SELECT " + ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER + " FROM " + ChatRoomEntry.TABLE_NAME + " WHERE " + ChatRoomEntry._ID + " = ?";
//            Cursor cursor = getDb().rawQuery(sql, new String[]{roomId});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                new String[]{ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER},
                ChatRoomEntry._ID + " = ?",
                new String[]{roomId},
                null,
                null,
                null
            );
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String data = Tools.getDbString(cursor, 0);
                cursor.close();
                Type typeToken = new TypeToken<List<ChatRoomMemberResponse>>() {
                }.getType();
                return JsonHelper.getInstance().from(data, typeToken);
            } else {
                // Handle the case where the cursor is empty.
                cursor.close();
                return Lists.newArrayList();
            }
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public void deleteServiceNumber() {
        try {
            String deleteSql = ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =?";
            final String[] selectionArgs = new String[]{ChatRoomSource.SERVICE.name()};
            getDb().delete(ChatRoomEntry.TABLE_NAME, deleteSql, selectionArgs);
        } catch (Exception ignored) {
        }
    }

    public List<ChatRoomEntity> findAllByChatRoomsAndExcludeType(String userId, ChatRoomType type, int page, int limit, boolean needLastMessage, boolean needFailedMessage, boolean needMembersProfile) {
        try {
            long dateTime = System.currentTimeMillis();
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry.COLUMN_TYPE + " !=? "
//                    + " ORDER BY " + ChatRoomEntry.COLUMN_UPDATE_TIME + " DESC "
//                    + " LIMIT ?, ? ";

            List<ChatRoomEntity> entities = Lists.newArrayList();
//            Cursor retCursor = getDb().rawQuery(sql, new String[]{type.name(), String.valueOf((page * (limit - 1))), String.valueOf(limit)});
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_TYPE + " !=? ",
                new String[]{type.name(), String.valueOf((page * (limit - 1))), String.valueOf(limit)},
                null,
                null,
                ChatRoomEntry.COLUMN_UPDATE_TIME + " DESC ",
                "?, ?"
            );
            entities.addAll(assemblyDetails(retCursor, userId, needLastMessage, needFailedMessage, needMembersProfile, true));
            retCursor.close();
            CELog.d(String.format("room find all limit by %s, page->%s, count->%s, use time->%s/秒  ", "All", "all page", entities.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomEntity> findAllChatRoomsByType(SQLiteDatabase db, String userId, List<ChatRoomType> types, boolean needLastMessage, boolean needFailedMessage, boolean needMembersProfile) {
        try {
            long dateTime = System.currentTimeMillis();
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE UPPER(" + ChatRoomEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size()) + ") AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " == 'N'";

            List<ChatRoomEntity> entities = Lists.newArrayList();
//            Cursor retCursor = (db == null ? DBManager.getInstance().openDatabase() : db).rawQuery(sql, toUpperCase(types));
            Cursor retCursor = (db == null ? DBManager.getInstance().openDatabase() : db).query(
                ChatRoomEntry.TABLE_NAME,
                null,
                "UPPER(" + ChatRoomEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size()) + ") AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N'",
                toUpperCase(types),
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(retCursor, userId, needLastMessage, needFailedMessage, needMembersProfile, true));
            retCursor.close();
            CELog.d(String.format("room find all limit by %s, page->%s, count->%s, use time->%s/秒  ", "All", "all page", entities.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomEntity> findAllChatRoomsByTypeAndName(SQLiteDatabase db, String userId, List<ChatRoomType> types, String keyWord, boolean needLastMessage, boolean needFailedMessage, boolean needMembersProfile) {
        try {
            long dateTime = System.currentTimeMillis();
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE UPPER(" + ChatRoomEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size())
//                    + ") AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " == 'N'"
//                    + "AND (" + ChatRoomEntry.COLUMN_TITLE +" LIKE '%" + keyWord +"%'"
//                    + " OR " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_NAME + " LIKE '%" + keyWord +"%')";

            List<ChatRoomEntity> entities = Lists.newArrayList();
//            Cursor retCursor = (db == null ? DBManager.getInstance().openDatabase() : db).rawQuery(sql, toUpperCase(types));
            Cursor retCursor = (db == null ? DBManager.getInstance().openDatabase() : db).query(
                ChatRoomEntry.TABLE_NAME,
                null,
                "UPPER(" + ChatRoomEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size())
                    + ") AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N'"
                    + "AND (" + ChatRoomEntry.COLUMN_TITLE + " LIKE '%" + keyWord + "%'"
                    + " OR " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_NAME + " LIKE '%" + keyWord + "%')",
                toUpperCase(types),
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(retCursor, userId, needLastMessage, needFailedMessage, needMembersProfile, true));
            retCursor.close();
            CELog.d(String.format("room find all limit by %s, page->%s, count->%s, use time->%s/秒  ", "All", "all page", entities.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomEntity> findAllChatRoomsByKeyword(String userId, String keyword) {
        try {
            long dateTime = System.currentTimeMillis();
            List<ChatRoomEntity> result = Lists.newArrayList();
            //建立索引加速查詢
            getDb().execSQL("CREATE INDEX IF NOT EXISTS idx_members_roomid " +
                "ON " + DBContract.ChatRoomMemberIdsEntry.TABLE_NAME + "(" + DBContract.ChatRoomMemberIdsEntry.COLUMN_ROOM_ID + ")");
            getDb().execSQL("CREATE INDEX IF NOT EXISTS idx_members_memberid " +
                "ON " + DBContract.ChatRoomMemberIdsEntry.TABLE_NAME + "(" + DBContract.ChatRoomMemberIdsEntry.COLUMN_MEMBER_ID + ")");
            getDb().execSQL("CREATE INDEX IF NOT EXISTS idx_users_nickName " +
                "ON " + DBContract.UserProfileEntry.TABLE_NAME + "(" + DBContract.UserProfileEntry.COLUMN_NICKNAME + ")");
//            String sql = "SELECT DISTINCT cr.* FROM "+
//                    ChatRoomEntry.TABLE_NAME+" cr "+
//                    "INNER JOIN " + DBContract.ChatRoomMemberIdsEntry.TABLE_NAME +" m ON cr." + ChatRoomEntry._ID+ " = m." + DBContract.ChatRoomMemberIdsEntry.COLUMN_ROOM_ID +
//                    " INNER JOIN " + DBContract.UserProfileEntry.TABLE_NAME +" u ON m." + DBContract.ChatRoomMemberIdsEntry.COLUMN_MEMBER_ID+ " = u." + DBContract.UserProfileEntry.COLUMN_ID +
//                    " WHERE ((u."+ DBContract.UserProfileEntry.COLUMN_NICKNAME +" LIKE ? " +
//                    "OR u."+ DBContract.UserProfileEntry.COLUMN_ALIAS +" LIKE ?) " +
//                    "AND cr." + ChatRoomEntry.COLUMN_TYPE + " IN ('" +
//                    ChatRoomType.group.name() + "', '" +
//                    ChatRoomType.discuss.name() + "', '" +
//                    ChatRoomType.person.name() + "')) " +
//                    "OR (cr." +ChatRoomEntry.COLUMN_TITLE + " LIKE ? " +
//                    "AND cr." + ChatRoomEntry.COLUMN_TYPE + " IN ('" +
//                    ChatRoomType.friend.name() + "', '" +
//                    ChatRoomType.system.name() + "', '" +
//                    ChatRoomType.group.name() + "')) " +
//                    "AND cr."+ ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED+" = 'N' ";
            String[] selectionArgs = new String[]{"%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%"};
//            Cursor retCursor = getDb().rawQuery(sql, selectionArgs);
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME + " cr " +
                    "INNER JOIN " + DBContract.ChatRoomMemberIdsEntry.TABLE_NAME + " m ON cr." + ChatRoomEntry._ID + " = m." + DBContract.ChatRoomMemberIdsEntry.COLUMN_ROOM_ID +
                    " INNER JOIN " + DBContract.UserProfileEntry.TABLE_NAME + " u ON m." + DBContract.ChatRoomMemberIdsEntry.COLUMN_MEMBER_ID + " = u." + DBContract.UserProfileEntry.COLUMN_ID,
                new String[]{"DISTINCT cr.*"},
                "((u." + DBContract.UserProfileEntry.COLUMN_NICKNAME + " LIKE ? " +
                    "OR u." + DBContract.UserProfileEntry.COLUMN_ALIAS + " LIKE ?) " +
                    "AND cr." + ChatRoomEntry.COLUMN_TYPE + " IN ('" +
                    ChatRoomType.group.name() + "', '" +
                    ChatRoomType.discuss.name() + "', '" +
                    ChatRoomType.person.name() + "')) " +
                    "OR (cr." + ChatRoomEntry.COLUMN_TITLE + " LIKE ? " +
                    "AND cr." + ChatRoomEntry.COLUMN_TYPE + " IN ('" +
                    ChatRoomType.friend.name() + "', '" +
                    ChatRoomType.system.name() + "', '" +
                    ChatRoomType.group.name() + "'))" +
                    " AND cr." + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " = 'N'",
                selectionArgs,
                null,
                null,
                null
            );
            result.addAll(assemblyDetails(retCursor, userId, true, false, true, true));
            retCursor.close();
            Log.d("Kyle116", String.format("findAllChatRoomsByKeyword count->%s, use time->%s/秒  ", result.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    private String[] toUpperCase(List<ChatRoomType> types) {
        String[] result = new String[types.size()];
        for (int i = 0; i < types.size(); i++) {
            result[i] = types.get(i).name().toUpperCase();
        }
        return result;
    }

    private String generatePlaceholdersForIn(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append("UPPER(?), ");
        }
        // Remove the last comma and space
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }


    public List<ChatRoomEntity> findAllChatRoomsByBossServiceNumberId(String userId, String serviceNumberId) {
        try {
            long dateTime = System.currentTimeMillis();
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =? ";

            List<ChatRoomEntity> entities = Lists.newArrayList();
//            Cursor retCursor = DBManager.getInstance().openDatabase().rawQuery(sql, new String[]{serviceNumberId});
            Cursor retCursor = DBManager.getInstance().openDatabase().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =? ",
                new String[]{serviceNumberId},
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(retCursor, userId, true, false, true, true));
            retCursor.close();
            CELog.d(String.format("room find all limit by %s, page->%s, count->%s, use time->%s/秒  ", "All", "all page", entities.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomEntity> findAllChatRoomsByType(SQLiteDatabase db, String userId, ChatRoomType type, boolean needFindIsCustomName) {
        try {
            long dateTime = System.currentTimeMillis();
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE UPPER(" + ChatRoomEntry.COLUMN_TYPE + ") IN (UPPER(?))";
            String selection = "UPPER(" + ChatRoomEntry.COLUMN_TYPE + ") IN (UPPER(?))";
            if (!needFindIsCustomName) {
                selection += " AND " + ChatRoomEntry.COLUMN_IS_CUSTOM_NAME + " = 'N'";
            }
            List<ChatRoomEntity> entities = Lists.newArrayList();
//            Cursor retCursor = (db == null ? DBManager.getInstance().openDatabase() : db).rawQuery(sql, new String[]{type.name().toUpperCase()});
            Cursor retCursor = (db == null ? DBManager.getInstance().openDatabase() : db).query(
                ChatRoomEntry.TABLE_NAME,
                null,
                selection,
                new String[]{type.name().toUpperCase()},
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(retCursor, userId, false, false, false, true));
            retCursor.close();
            CELog.d(String.format("room find all limit by %s, page->%s, count->%s, use time->%s/秒  ", "All", "all page", entities.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomEntity> findByIdsAndChatRoomSource2(ChatRoomSource source, String userId, Set<String> roomIds, boolean needLastMessage, boolean needFailedMessage, boolean needMembersProfile) {
        long dateTime = System.currentTimeMillis();
        try {
            List<ChatRoomEntity> entities = Lists.newArrayList();
            String roomIdsIn = String.format(ChatRoomEntry._ID + " IN (%s)", concatStrings("'", ",", roomIds.toArray()));

//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=?"
//                    + " AND " + roomIdsIn + " AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " != 'N'";
//            Cursor retCursor = getDb().rawQuery(sql, new String[]{source.name()});
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? AND ? AND " + ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " != 'N'",
                new String[]{source.name(), roomIdsIn},
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(retCursor, userId, needLastMessage, needFailedMessage, needMembersProfile, false));
            retCursor.close();
            CELog.d(String.format("room find all by ids and %s , count->%s, use time->%s/second  ", source.name(), entities.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomEntity> findByIds(String userId, List<String> roomIds, boolean needLastMessage, boolean needFailedMessage, boolean needMembersProfile) {
        List<ChatRoomEntity> entities = Lists.newArrayList();
        try {
            String roomIdsIn = String.format(ChatRoomEntry._ID + " IN (%s)", concatStrings("'", ",", roomIds.toArray()));
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME +
//                    " WHERE " + roomIdsIn;
//            Cursor retCursor = getDb().rawQuery(sql, null);
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                roomIdsIn,
                null,
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(retCursor, userId, needLastMessage, needFailedMessage, needMembersProfile, false));
            retCursor.close();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
        return entities;
    }

    public boolean delete(ChatRoomEntity entity) {
        try {
            return deleteById(entity.getId());
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteById(String roomId) {
        try {
            final String sessionSelection = ChatRoomEntry._ID + " = ?";
            final String messageSelection = MessageEntry.COLUMN_ROOM_ID + " = ?";

            final String[] selectionArgs = new String[]{roomId};
            int a = getDb().delete(ChatRoomEntry.TABLE_NAME, sessionSelection, selectionArgs);
            int b = getDb().delete(MessageEntry.TABLE_NAME, messageSelection, selectionArgs);
            return a > 0 && b >= 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteByServiceNumberId(String serviceNumberId) {
        try {
            final String sessionSelection = ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = ?";
            final String[] selectionArgs = new String[]{serviceNumberId};
            int a = getDb().delete(ChatRoomEntry.TABLE_NAME, sessionSelection, selectionArgs);
            return a > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public List<ChatRoomEntity> findFriendRoomEntitysByRelAccountIdAndContainTypes(String userId, String accountId, Set<ChatRoomType> types, boolean needLastMessage, boolean needFailedMessage, boolean needMembersProfile) {
        Set<ChatRoomEntity> entities = Sets.newHashSet();
        try {
            String selection = String.format(" AND " + ChatRoomEntry.COLUMN_TYPE + " IN (%s)", concatStrings("'", ",", types.toArray()));
//            String sql = "SELECT r.* FROM " + ChatRoomEntry.TABLE_NAME + " AS r " +
//                    " INNER JOIN " + AccountRoomRel.TABLE_NAME + " AS a " +
//                    " ON a.room_id = r._id " +
//                    " WHERE a.account_id = ? " +
//                    selection;
//            Cursor cursor = getDb().rawQuery(sql, new String[]{accountId});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME + " AS r INNER JOIN " + AccountRoomRel.TABLE_NAME + " AS a  ON a.room_id = r._id ",
                new String[]{"r.*"},
                "a.account_id = ? ?",
                new String[]{accountId, selection},
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(cursor, userId, needLastMessage, needFailedMessage, needMembersProfile, false));
            cursor.close();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }

        return Lists.newArrayList(entities);
    }

    public List<ChatRoomEntity> findAllBusinessRoom(String userId) {
        try {
            List<ChatRoomEntity> entities = Lists.newArrayList();
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry.COLUMN_BUSINESS_ID + " NOTNULL"
//                    + " AND " + ChatRoomEntry.COLUMN_BUSINESS_ID + " !='' ";

//            Cursor cursor = getDb().rawQuery(sql, null);
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_BUSINESS_ID + " NOTNULL AND " + ChatRoomEntry.COLUMN_BUSINESS_ID + " !='' ",
                null,
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(cursor, userId, false, false, true, false));
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public List<ChatRoomEntity> findAllBusinessRoomByBusinessId(String userId, String businessId) {
        try {
            List<ChatRoomEntity> entities = Lists.newArrayList();
//            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry.COLUMN_BUSINESS_ID + "=?";

//            Cursor cursor = getDb().rawQuery(sql, new String[]{businessId});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_BUSINESS_ID + "=?",
                new String[]{businessId},
                null,
                null,
                null
            );
            entities.addAll(assemblyDetails(cursor, userId, true, true, true, false));
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public int getAllServiceNumberChatRoomUnreadNumber() {
//        String sql = "SELECT SUM(" + ChatRoomEntry.COLUMN_UNREAD_NUMBER +") FROM " + ChatRoomEntry.TABLE_NAME
//                + " WHERE " +  ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =? "
//                + " AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " !=?";
//        Cursor cursor = getDb().rawQuery(sql, new String[]{ChatRoomSource.SERVICE.name(), ServiceNumberStatus.ON_LINE.getStatus()});
        Cursor cursor = getDb().query(
            ChatRoomEntry.TABLE_NAME,
            new String[]{"SUM(" + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ")"},
            ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =? AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " !=?",
            new String[]{ChatRoomSource.SERVICE.name(), ServiceNumberStatus.ON_LINE.getStatus()},
            null,
            null,
            null
        );
        cursor.moveToFirst();
        int unread = cursor.getInt(0);
        cursor.close();
        return unread;
    }

    public int getServiceNumberChatRoomUnreadNumber(String serviceNumberId) {
//        String sql = "SELECT SUM(" + ChatRoomEntry.COLUMN_UNREAD_NUMBER +") FROM " + ChatRoomEntry.TABLE_NAME
//                + " WHERE " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =? "
//                + " AND " + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =? "
//                + " AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " !=?";
//        Cursor cursor = getDb().rawQuery(sql, new String[]{serviceNumberId, ChatRoomSource.SERVICE.name(), ServiceNumberStatus.ON_LINE.getStatus()});
        Cursor cursor = getDb().query(
            ChatRoomEntry.TABLE_NAME,
            new String[]{"SUM(" + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ")"},
            ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =? "
                + " AND " + ChatRoomEntry.COLUMN_LIST_CLASSIFY + " =? "
                + " AND " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " !=?",
            new String[]{serviceNumberId, ChatRoomSource.SERVICE.name(), ServiceNumberStatus.ON_LINE.getStatus()},
            null,
            null,
            null
        );
        if (cursor == null) {
            return 0;
        }
        cursor.moveToFirst();
        int unread = cursor.getInt(0);
        cursor.close();
        return unread;
    }

    public Map<ChatRoomSource, BadgeDataModel> getRoomUnreadNumber(String selfId) {
        Map<ChatRoomSource, BadgeDataModel> badgeData = Maps.newHashMap(ImmutableMap.of(ChatRoomSource.ALL, new BadgeDataModel(), ChatRoomSource.MAIN, new BadgeDataModel(), ChatRoomSource.SERVICE, new BadgeDataModel()));
        try {
//            String sql = "SELECT "
//                    + "SUM(CASE WHEN (r." + ChatRoomEntry.COLUMN_TYPE + "=? AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "!=?) OR (r." + ChatRoomEntry.COLUMN_TYPE + "=? AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "=? AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + "!=?) " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_service_count, "
//                    + "SUM(CASE WHEN (r." + ChatRoomEntry.COLUMN_TYPE + "!=? AND r." + ChatRoomEntry.COLUMN_TYPE + "!=?) OR (r." + ChatRoomEntry.COLUMN_TYPE + "=? AND " + "r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "=? AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + "=?" + ") THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_main_count, "
//                    + "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "=?" + " AND r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_s_service_count, "
//                    + "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "=?" + " AND r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_m_service_count, "
//                    + "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + "!=''  THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_consult_count "
////                    + "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "='MAIN'" + " AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OPEN_TYPES + " LIKE '%C%' " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_subscribe_consult "
//                    + " FROM " + ChatRoomEntry.TABLE_NAME + " AS r "
//                    + " WHERE ( r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " > 0 " + " OR r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " == -1 )"
//                    + " AND r." + ChatRoomEntry.COLUMN_TYPE + "!='BROADCAST'";

            String[] selectionArgs = new String[]{"services", "BOSS", "services", "Boss", selfId,
                "services", "serviceMember", "services", "Boss", selfId,
                "serviceMember", "SERVICE",
                "serviceMember", "MAIN"};
//            Cursor cursor = getDb().rawQuery(sql, selectionArgs);
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME + " AS r ",
                new String[]{
                    "SUM(CASE WHEN (r." + ChatRoomEntry.COLUMN_TYPE + "=? AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "!=?) OR (r." + ChatRoomEntry.COLUMN_TYPE + "=? AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "=? AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + "!=?) " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_service_count",
                    "SUM(CASE WHEN (r." + ChatRoomEntry.COLUMN_TYPE + "!=? AND r." + ChatRoomEntry.COLUMN_TYPE + "!=?) OR (r." + ChatRoomEntry.COLUMN_TYPE + "=? AND " + "r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + "=? AND r." + ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + "=?" + ") THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_main_count",
                    "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "=?" + " AND r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_s_service_count",
                    "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "=?" + " AND r." + ChatRoomEntry.COLUMN_LIST_CLASSIFY + "=? " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_m_service_count",
                    "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + "!=''  THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS unread_consult_count "
                },
                "( r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " > 0 " + " OR r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " == -1 )"
                    + " AND r." + ChatRoomEntry.COLUMN_TYPE + "!='BROADCAST'",
                selectionArgs,
                null,
                null,
                null
            );

            while (cursor.moveToNext()) {
                int unreadServiceCount = Tools.getDbInt(cursor, "unread_service_count");
                int unreadMainCount = Tools.getDbInt(cursor, "unread_main_count");

                int unreadServiceMemberCountByService = Tools.getDbInt(cursor, "unread_s_service_count");
                int unreadServiceMemberCountByMain = Tools.getDbInt(cursor, "unread_m_service_count");

//                int unreadSubscribeConsult = Tools.getDbInt(cursor, "unread_subscribe_consult");
//                unreadMainCount = unreadMainCount - unreadSubscribeConsult;
                unreadServiceCount = unreadServiceCount + unreadServiceMemberCountByService;
                unreadMainCount = unreadMainCount + unreadServiceMemberCountByMain;

                int unreadConsultCount = Tools.getDbInt(cursor, "unread_consult_count");

                CELog.d("Kyle1 unreadMainCount=" + unreadMainCount + ", unreadServiceCount=" + unreadServiceCount + ", unreadConsultCount=" + unreadConsultCount + ".");
                badgeData.get(ChatRoomSource.ALL).setUnReadNumber(unreadMainCount + unreadServiceCount);
                badgeData.get(ChatRoomSource.MAIN).setUnReadNumber(unreadMainCount);
                badgeData.get(ChatRoomSource.SERVICE).setUnReadNumber(unreadServiceCount);
            }
            return badgeData;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return badgeData;
        }
    }

    public int getConsultsUnreadNumber(String roomId) {
        try {
            int consultsUnreadNumber;
//            String sql = "SELECT "
//                    + "SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "= ? " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS consults_unread_number "
//                    + " FROM " + ChatRoomEntry.TABLE_NAME + " AS r "
//                    + " WHERE ( r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " > 0 " + " OR r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " == -1 )"
//                    + " AND r." + ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + "= ?";

//            Cursor cursor = getDb().rawQuery(sql, new String[]{"SUBSCRIBE", roomId});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME + " AS r ",
                new String[]{"SUM(CASE WHEN r." + ChatRoomEntry.COLUMN_TYPE + "= ? " + " THEN ABS(r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + ") ELSE 0 END) AS consults_unread_number "},
                "( r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " > 0 " + " OR r." + ChatRoomEntry.COLUMN_UNREAD_NUMBER + " == -1 )"
                    + " AND r." + ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + "= ?",
                new String[]{"SUBSCRIBE", roomId},
                null,
                null,
                null
            );
            if (cursor.getCount() == 0) {
                cursor.close();
                return 0;
            }
            cursor.moveToFirst();
            do {
                consultsUnreadNumber = Tools.getDbInt(cursor, "consults_unread_number");
            } while (cursor.moveToNext());
            cursor.close();
            if (consultsUnreadNumber > 0) {
                CELog.e("");
            }
            return consultsUnreadNumber;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return 0;
        }
    }

    public List<ChatRoomEntity> findServiceBusinessByServiceNumberIdAndOwnerIdAndNotRoomId(String serviceNumberId, String ownerId, String roomId, ChatRoomType type) {
        try {
            List<ChatRoomEntity> entities = Lists.newArrayList();
            String sql = "SELECT * FROM " + ChatRoomEntry.TABLE_NAME
                + " WHERE " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =?"
                + " AND " + ChatRoomEntry.COLUMN_OWNER_ID + " =?"
                + " AND " + ChatRoomEntry.COLUMN_BUSINESS_ID + " !=?"
                + " AND " + ChatRoomEntry._ID + " !=?"
                + " AND " + ChatRoomEntry.COLUMN_TYPE + " =?";
            String[] args = new String[]{serviceNumberId, ownerId, "", roomId, type.name()};
//            Cursor retCursor = getDb().rawQuery(sql, args);
            Cursor retCursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                null,
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " =?"
                    + " AND " + ChatRoomEntry.COLUMN_OWNER_ID + " =?"
                    + " AND " + ChatRoomEntry.COLUMN_BUSINESS_ID + " !=?"
                    + " AND " + ChatRoomEntry._ID + " !=?"
                    + " AND " + ChatRoomEntry.COLUMN_TYPE + " =?",
                args,
                null,
                null,
                null
            );
            if (retCursor.getCount() <= 0) {
                retCursor.close();
                return entities;
            }
            while (retCursor.moveToNext()) {
                String id = Tools.getDbString(retCursor, ChatRoomEntry._ID);
                List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(getDb(), roomId);

//                sql = "SELECT * FROM " + AccountRoomRel.TABLE_NAME +
//                        " WHERE " + AccountRoomRel.COLUMN_ROOM_ID + " =? ";
//                Cursor cursor = getDb().rawQuery(sql, new String[]{id});
//                if (cursor.getCount() <= 0) {
//                    cursor.close();
//                } else {
//                    while (cursor.moveToNext()) {
//                        String accountId = Tools.getDbString(cursor, AccountRoomRel.COLUMN_ACCOUNT_ID);
//                        memberIds.add(accountId);
//                    }
//                    cursor.close();
//                }

                MessageEntity lastMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), id, MessageStatus.getValidStatus(), MessageReference.Sort.DESC);
                MessageEntity failedMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), id, MessageStatus.getFailedErrorStatus(), MessageReference.Sort.DESC);
//                List<MessageEntity> unreadMessages = MessageReference.findUnreadMessagesByRoomId(getDb(), id);
                List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(roomId);
                Map<String, Integer> index = ChatRoomEntry.getIndex(retCursor);
                ChatRoomEntity entity = ChatRoomEntity.formatByCursor(index, retCursor, false, memberIds, members, lastMessage, failedMessage).build();

                if (ChatRoomType.friend.equals(entity.getType())) {
                    if (memberIds.size() > 1) {
                        DBManager.getInstance();
                        String userId = TokenPref.getInstance(SdkLib.getAppContext()).getUserId();
                        boolean friendIsBlock = DBManager.getInstance().queryFriendIsBlock(memberIds.get(0).equals(userId) ? memberIds.get(1) : memberIds.get(0));
                        if (!friendIsBlock) {
                            entities.add(entity);
                        }
                    }
                } else {
                    entities.add(entity);
                }
            }
            retCursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return Lists.newArrayList();
        }
    }

    /**
     * assembly Chat Room Entity Other Details
     */
    private Set<ChatRoomEntity> assemblyDetails(Cursor cursor, String userId, boolean needLastMessage, boolean needFailedMessage, boolean needMembersProfile, boolean isJoin) {

        Set<ChatRoomEntity> entities = Sets.newHashSet();
        if (cursor.getCount() <= 0) {
            cursor.close();
            return entities;
        }

        Map<String, Integer> index = ChatRoomEntry.getIndex();
        while (cursor.moveToNext()) {
            String roomId = Tools.getDbString(cursor, ChatRoomEntry._ID);
            List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(getDb(), roomId);
            ChatRoomEntity entity;

            MessageEntity lastMessage = null;
//            if (needLastMessage) {
//                lastMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), roomId, MessageStatus.getValidStatus(), MessageReference.Sort.DESC);
//            }

            MessageEntity failedMessage = null;
            if (needFailedMessage) {
                failedMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), roomId, MessageStatus.getFailedErrorStatus(), MessageReference.Sort.DESC);
            }

            List<UserProfileEntity> members = Lists.newArrayList();
            if (needMembersProfile) {
                members = ChatMemberCacheService.getChatMember(roomId);
            }

//            entity = ChatRoomEntity.formatByCursor(index, cursor, isJoin, memberIds, members, lastMessage, failedMessage).build();
            entity = ChatRoomEntity.formatByCursor(index, cursor, isJoin, memberIds, members, lastMessage, failedMessage).build();


            String lastMessageStr = entity.getLastMessageStr();
            if (!"{}".equals(lastMessageStr)) {
                entity.setLastMessage(JsonHelper.getInstance().from(entity.getLastMessageStr(), MessageEntity.class));
            }

            if (entity.getLastMessage() != null && MessageType.AT.equals(entity.getLastMessage().getType())) {
                entity.setMembers(ChatMemberCacheService.getChatMember(entity.getId()));
            }

            if (ChatRoomType.discuss.equals(entity.getType()) || (ChatRoomType.group.equals(entity.getType()) && Strings.isNullOrEmpty(entity.getAvatarId()))) {
                entity.setMemberAvatarData(UserProfileReference.getMemberAvatarData(null, entity.getId(), userId, 4));
            }

            // EVAN_FLAG 2020-04-21 (1.10.0) My collection is compared and judged, if it is a friend,
            //  and there is no object content, and whether the user is included after the intersection member

            if (ChatRoomType.friend.equals(entity.getType()) && Strings.isNullOrEmpty(entity.getBusinessId())) {
                Set<String> favouriteUserIds = LabelReference.findFavouriteLabels(getDb());
                Set<String> retainFavouriteUserIds = Sets.newHashSet(favouriteUserIds);
                retainFavouriteUserIds.retainAll(memberIds);
                entity.setFavourite(!retainFavouriteUserIds.isEmpty());
            }
//
//            // EVAN_FLAG 2020-04-21 (1.10.0) If there is an unread message, check locally whether there is an unread message of Atme or All.
            //  ＊＊＊＊＊＊ Need to match Jocket Message.New & Judge during the supplementary message process ＊＊＊＊＊＊
//            if (entity.getUnReadNum() > 0) {
//                boolean hasUnReadAtMe = MessageReference.findUnreadAtMessagesByRoomId(getDb(), userId, roomId);
//                if(!hasUnReadAtMe) {
//                    if(entity.getLastMessage() != null) {
//                        //判斷是否為@我
//                        boolean hasAtMe = MessageType.AT.equals(entity.getLastMessage().getType()) && (entity.getLastMessage().getContent().contains(userId) || entity.getLastMessage().getContent().contains("\"objectType\": \"All\""));
//                        entity.setAtMe(hasAtMe);
//                    }else
//                        entity.setAtMe(false);
//                } else
//                    entity.setAtMe(true);
//            }
//            if (entity.getUnReadNum() > 0) {
//                boolean hasUnReadAtMe = MessageReference.findUnreadAtMessagesByRoomId(getDb(), userId, roomId);
//                boolean hasAtMe = false;
//                if (!hasUnReadAtMe && entity.getLastMessage() != null) {
//                    hasAtMe = MessageType.AT.equals(entity.getLastMessage().getType()) &&
//                            (entity.getLastMessage().getContent().contains(userId) ||
//                                    entity.getLastMessage().getContent().contains("\"objectType\": \"All\""));
//                }
//                entity.setAtMe(hasUnReadAtMe || hasAtMe);
//            }

            if (ChatRoomType.services.equals(entity.getType())) {
                int consultsUnreadNumber = getConsultsUnreadNumber(entity.getId());
                if (consultsUnreadNumber > 0) {
                    entity.setConsultSrcUnreadNumber(consultsUnreadNumber);
                }
            }

//            if (ChatRoomType.FRIEND.equals(entity.getType())) {
//                if (memberIds != null && memberIds.size() > 1) {
////                    String userId = UserPref.getInstance(DBManager.getInstance().getHelper().getContext()).getUserId();
//                    boolean friendIsBlock = DBManager.getInstance().queryFriendIsBlock(memberIds.get(0).equals(userId) ? memberIds.get(1) : memberIds.get(0));
//                    if (!friendIsBlock) {
//                        entities.add(entity);
//                    }
//                }
//            } else {
//                entities.add(entity);
//            }
            entities.add(entity);
        }

        return entities;
    }

    /**
     * assembly Chat Room Entity Other Details
     */
    private ChatRoomEntity assemblyDetail(Map<String, Integer> index, Cursor cursor, String userId, boolean needLastMessage, boolean needFailedMessage, boolean needMembersProfile, boolean needCheckFavourite, boolean needCheckUnreadAtMessage, boolean isJoin) {
        Set<String> favouriteUserIds = LabelReference.findFavouriteLabels(getDb());
        String roomId = Tools.getDbString(cursor, ChatRoomEntry._ID);
        List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(getDb(), roomId);
        ChatRoomEntity entity;
        MessageEntity lastMessage = null;
        if (needLastMessage) {
            lastMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), roomId, MessageStatus.getValidStatus(), MessageReference.Sort.DESC);
        }

        MessageEntity failedMessage = null;
        if (needFailedMessage) {
            failedMessage = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(getDb(), roomId, MessageStatus.getFailedErrorStatus(), MessageReference.Sort.DESC);
        }

        List<UserProfileEntity> members = Lists.newArrayList();
        if (needMembersProfile) {
            members = ChatMemberCacheService.getChatMember(roomId);
//            members = UserProfileReference.findUserProfilesByRoomId(getDb(), roomId);
        }
        entity = ChatRoomEntity.formatByCursor(index, cursor, isJoin, memberIds, members, lastMessage, failedMessage).build();

        // EVAN_FLAG 2020-04-21 (1.10.0) My collection is compared and judged, if it is a friend,
        //  and there is no object content, and whether the user is included after the intersection member
        if (needCheckFavourite) {
            Set<String> retainFavouriteUserIds = Sets.newHashSet(favouriteUserIds);
            retainFavouriteUserIds.retainAll(memberIds);
            entity.setFavourite(ChatRoomType.friend.equals(entity.getType()) && Strings.isNullOrEmpty(entity.getBusinessId()) && !retainFavouriteUserIds.isEmpty());
        }

        // EVAN_FLAG 2020-04-21 (1.10.0) If there is an unread message, check locally whether there is an unread message of Atme or All.
        //  ＊＊＊＊＊＊ Need to match Jocket Message.New & Judge during the supplementary message process ＊＊＊＊＊＊
        if (entity.getUnReadNum() > 0 && needCheckUnreadAtMessage) {
            boolean hasUnReadAtMe = MessageReference.findUnreadAtMessagesByRoomId(getDb(), userId, roomId);
            boolean hasAtMe = false;
            if (!hasUnReadAtMe && entity.getLastMessage() != null) {
                hasAtMe = MessageType.AT.equals(entity.getLastMessage().getType()) &&
                    (entity.getLastMessage().getContent().contains(userId) ||
                        entity.getLastMessage().getContent().contains("\"objectType\": \"All\""));
            }
            entity.setAtMe(hasUnReadAtMe || hasAtMe);
        }

        if (ChatRoomType.services.equals(entity.getType())) {
            int consultsUnreadNumber = getConsultsUnreadNumber(entity.getId());
            if (consultsUnreadNumber > 0) {
                entity.setConsultSrcUnreadNumber(consultsUnreadNumber);
            }
        }
        return entity;
    }

    /**
     * update Chat Room Interaction Time
     */
    public boolean updateInteractionTimeById(String id) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_UPDATE_TIME, System.currentTimeMillis());
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{id};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public void updateUnread(String roomId) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_UNREAD_NUMBER, 0);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{roomId};
            getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {

        }
    }

    public boolean updateChatRoomNameById(String id, String name) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_TITLE, name);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{id};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean updateTitleById(String id, String title) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_TITLE, title);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{id};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean updateLogoUtlById(String id, String logoUrl) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_AVATAR_ID, logoUrl);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{id};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean updateUnreadNumberById(String roomId, int unreadNumber) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_UNREAD_NUMBER, unreadNumber);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{roomId};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean updateOwnerIdById(String id, String ownerId) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_OWNER_ID, ownerId);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{id};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean updateUnfinishedEditedAndTimeById(String id, String text) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_UNFINISHED_EDITED, text);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{id};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public boolean updateLastMessage(String roomId, MessageEntity message) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBContract.ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY, message.toJson());
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{roomId};
            getDb().update(ChatRoomEntry.TABLE_NAME, contentValues, whereClause, whereArgs);

            ContentValues lastMessageContentValues = ChatRoomEntity.getLastMessageContentValues(message);
            String whereClause1 = DBContract.LastMessageEntry.COLUMN_ROOM_ID + "= ?";
            String[] whereArgs1 = new String[]{roomId};
            return getDb().update(DBContract.LastMessageEntry.TABLE_NAME, lastMessageContentValues, whereClause1, whereArgs1) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public String getUnfinishedEdited(String roomId) {
        try {
//            String sql = "SELECT " + ChatRoomEntry.COLUMN_UNFINISHED_EDITED + " FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry._ID + "=?";
//            Cursor cursor = getDb().rawQuery(sql, new String[]{roomId});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                new String[]{ChatRoomEntry.COLUMN_UNFINISHED_EDITED},
                ChatRoomEntry._ID + "=?",
                new String[]{roomId},
                null,
                null,
                null
            );
            if (cursor.getCount() == 0) {
                cursor.close();
                return "";
            }
            cursor.moveToFirst();
            String unfinishedEdited = Tools.getDbString(cursor, ChatRoomEntry.COLUMN_UNFINISHED_EDITED);
            cursor.close();
            return unfinishedEdited;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Update this chat room Pickup Agent Id
     */
    public boolean updateServiceNumberAgentIdById(String id, String serviceNumberAgentId) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID, serviceNumberAgentId);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{id};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public List<String> findRoomIdByServiceNumberId(String serviceNumberId) {
        List<String> roomIds = Lists.newArrayList();
        try {
//            String sql = "SELECT " + ChatRoomEntry._ID + " FROM " + ChatRoomEntry.TABLE_NAME
//                    + " WHERE " + ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + "=?";
            String[] args = new String[]{serviceNumberId};
//            Cursor cursor = getDb().rawQuery(sql, args);
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                new String[]{ChatRoomEntry._ID},
                ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + "=?",
                args,
                null,
                null,
                null
            );
            if (cursor.getCount() == 0) {
                cursor.close();
                return roomIds;
            }
            cursor.moveToFirst();
            while (cursor.moveToNext()) {
                roomIds.add(cursor.getString(0));
            }
            return roomIds;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return roomIds;
        }
    }

    public boolean updateCustomerRoomName(String serviceNumberId, String title, String ownerId) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChatRoomEntry.COLUMN_TITLE, title);
            String whereClause = ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + "= ? AND " + ChatRoomEntry.COLUMN_OWNER_ID + "=?";
            return getDb().update(ChatRoomEntry.TABLE_NAME, contentValues, whereClause, new String[]{serviceNumberId, ownerId}) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Update service number Agent to user serviced status
     */
    public boolean updateServiceNumberStatusById(String id, ServiceNumberStatus status) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS, status.getStatus());
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{id};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean updateServiceNumberOwnerStopById(String id, boolean status) {
        try {
            ContentValues values = new ContentValues();
            values.put(DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_STOP, status);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{id};
            return getDb().update(DBContract.ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    /**
     * update isTop
     */
    public boolean updateTopAndTopTimeById(String roomId, boolean isTop, long topTime) {
        try {
            final String whereClause = ChatRoomEntry._ID + " = ?";
            final String[] whereArgs = new String[]{roomId};

            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry._ID, roomId);
            values.put(ChatRoomEntry.COLUMN_IS_TOP, isTop ? "Y" : "N");
            values.put(ChatRoomEntry.COLUMN_TOP_TIME, isTop ? topTime == 0L ? System.currentTimeMillis() : topTime : 0L);

            int _id = getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }

    }

    /**
     * update isMute
     */
    public boolean updateMuteById(String roomId, boolean isMute) {
        try {
            final String whereClause = ChatRoomEntry._ID + " = ?";
            final String[] whereArgs = new String[]{roomId};

            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry._ID, roomId);
            values.put(ChatRoomEntry.COLUMN_IS_MUTE, isMute ? "Y" : "N");

            int _id = getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }

    }

    /**
     * update Chat Room Business Info
     */
    public boolean updateBusinessContent(String roomId, BusinessContent businessContent) {
        try {
            final String whereClause = ChatRoomEntry._ID + " = ?";
            final String[] whereArgs = new String[]{roomId};
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry._ID, roomId);
            values.put(ChatRoomEntry.COLUMN_BUSINESS_ID, Strings.isNullOrEmpty(businessContent.getId()) ? "" : businessContent.getId());
            values.put(ChatRoomEntry.COLUMN_BUSINESS_NAME, Strings.isNullOrEmpty(businessContent.getName()) ? "" : businessContent.getName());
            values.put(ChatRoomEntry.COLUMN_BUSINESS_CODE, Strings.isNullOrEmpty(businessContent.getCode().getCode()) ? "" : businessContent.getCode().getCode());
            int _id = getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean updateRoomAvatarByServiceNumberId(String serviceNumberId, String avatarId) {
        try {
            final String whereClause = ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " = ? AND " + ChatRoomEntry.COLUMN_TYPE + " = ?";
            final String[] whereArgs = new String[]{serviceNumberId, ChatRoomType.serviceMember.name()};
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_AVATAR_ID, avatarId);
            values.put(ChatRoomEntry.COLUMN_SERVICE_NUMBER_AVATAR_ID, avatarId);
            int _id = getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public boolean updateBusinessExecutorIdByBusinessId(String businessId, String businessExecutorId) {
        try {
            // final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            final String whereClause = ChatRoomEntry.COLUMN_BUSINESS_ID + " = ?";
            final String[] whereArgs = new String[]{businessId};
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID, businessExecutorId);
            int _id = getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public boolean updateConsultRoomIdById(String roomId, String consultRoomId) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_CONSULT_ROOM_ID, consultRoomId);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{roomId};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public void updateChatRoomMember(String roomId, List<ChatRoomMemberResponse> chatRoomMemberResponses) {
        String json = JsonHelper.getInstance().toJson(chatRoomMemberResponses);
        ContentValues values = new ContentValues();
        values.put(ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER, json);
        String whereClause = ChatRoomEntry._ID + "= ?";
        String[] whereArgs = new String[]{roomId};
        getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs);

        try {
            getDb().beginTransaction();
            for (ChatRoomMemberResponse chatMember : chatRoomMemberResponses) {
                ContentValues contentValue = ChatRoomMemberResponse.getContentValue(roomId, chatMember);
                getDb().replace(DBContract.ChatMemberEntry.TABLE_NAME, null, contentValue);

                ContentValues contentValue1 = ChatRoomEntity.getMemberId(roomId, chatMember.getMemberId());
                getDb().replace(DBContract.ChatRoomMemberIdsEntry.TABLE_NAME, null, contentValue1);
            }
            getDb().setTransactionSuccessful();
        } finally {
            getDb().endTransaction();
        }
    }

    public boolean updateChatRoomTitle(String roomId, String title) {
        ContentValues values = new ContentValues();
        values.put(ChatRoomEntry.COLUMN_TITLE, title);
        String whereClause = ChatRoomEntry._ID + " =?";
        String[] whereArgs = new String[]{roomId};
        return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
    }

    public boolean deleteChatRoomLastMsg(String roomId) {
        ContentValues values = new ContentValues();
        values.put(ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY, "{}");
        String whereClause = ChatRoomEntry._ID + "= ?";
        String[] whereArgs = new String[]{roomId};
        getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs);

        String whereClause1 = DBContract.LastMessageEntry.COLUMN_ROOM_ID + "= ?";
        String[] whereArgs1 = new String[]{roomId};
        return getDb().delete(DBContract.LastMessageEntry.TABLE_NAME, whereClause1, whereArgs1) > 0;
    }

    public boolean getIsAtMe(String roomId) {
        try {
//            String sql ="SELECT " + ChatRoomEntry.COLUMN_IS_AT_ME + " FROM " + ChatRoomEntry.TABLE_NAME + " WHERE " + ChatRoomEntry._ID + " =?";
//            Cursor cursor = getDb().rawQuery(sql, new String[]{roomId});
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                new String[]{ChatRoomEntry.COLUMN_IS_AT_ME},
                ChatRoomEntry._ID + " =?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            int isAtMe = Tools.getDbInt(cursor, ChatRoomEntry.COLUMN_IS_AT_ME);
            return isAtMe == 1;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addUnreadCount(String roomId, int count) {
        try {
            Cursor cursor = getDb().query(
                ChatRoomEntry.TABLE_NAME,
                new String[]{ChatRoomEntry.COLUMN_UNREAD_NUMBER},
                ChatRoomEntry._ID + "=?",
                new String[]{roomId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            int chatRoomUnreadCount = Tools.getDbInt(cursor, ChatRoomEntry.COLUMN_UNREAD_NUMBER);
            chatRoomUnreadCount += count;

            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_UNREAD_NUMBER, chatRoomUnreadCount);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{roomId};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean updateIsAtMeFlag(String roomId, boolean isAtMe) {
        try {
            ContentValues values = new ContentValues();
            values.put(ChatRoomEntry.COLUMN_IS_AT_ME, isAtMe ? 1 : 0);
            String whereClause = ChatRoomEntry._ID + "= ?";
            String[] whereArgs = new String[]{roomId};
            return getDb().update(ChatRoomEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized boolean save(SyncRoomNormalResponse syncRoomNormalResponse) {
        SQLiteDatabase db = getDb();
        db.beginTransaction();
        try {
            if (syncRoomNormalResponse.getUnReadNum() == 0) {
                if (getUnReadNumberById(syncRoomNormalResponse.getId()) == -1) {
                    syncRoomNormalResponse.setUnReadNum(-1);
                }
            }

            for (String id : syncRoomNormalResponse.getMemberIds()) {
                ContentValues contentValue = ChatRoomEntity.getMemberId(syncRoomNormalResponse.getId(), id);
                getDb().replace(DBContract.ChatRoomMemberIdsEntry.TABLE_NAME, null, contentValue);
            }

            if (syncRoomNormalResponse.getLastMessage() != null) {
                ContentValues lastMessageValues = ChatRoomEntity.getLastMessageContentValues(syncRoomNormalResponse.getLastMessage());
                db.replace(DBContract.LastMessageEntry.TABLE_NAME, null, lastMessageValues);
            }

            ContentValues values = syncRoomNormalResponse.getContentValues();
            long _id = db.replace(ChatRoomEntry.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
            return _id > 0;
        } catch (Exception ignored) {
        } finally {
            db.endTransaction();
        }
        return false;
    }

    public synchronized boolean deleteMemberById(String userId, String roomId) {
        SQLiteDatabase db = getDb();
        db.beginTransaction();
        try {
            String whereClause = DBContract.ChatMemberEntry.COLUMN_ROOM_ID + " = ? AND " + DBContract.ChatMemberEntry.COLUMN_MEMBER_ID + " = ?";
            String[] whereArgs = new String[]{roomId, userId};
            long _id = db.delete(DBContract.ChatMemberEntry.TABLE_NAME, whereClause, whereArgs);
            db.setTransactionSuccessful();
            return _id > 0;
        } catch (Exception ignored) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public synchronized boolean deleteMemberIdsById(String userId, String roomId) {
        SQLiteDatabase db = getDb();
        db.beginTransaction();
        try {
            String whereClause = DBContract.ChatRoomMemberIdsEntry.COLUMN_ROOM_ID + " = ? AND " + DBContract.ChatMemberEntry.COLUMN_MEMBER_ID + " = ?";
            String[] whereArgs = new String[]{roomId, userId};
            long _id = db.delete(DBContract.ChatRoomMemberIdsEntry.TABLE_NAME, whereClause, whereArgs);
            db.setTransactionSuccessful();
            return _id > 0;
        } catch (Exception ignored) {
            return false;
        } finally {
            db.endTransaction();
        }
    }
}


