package tw.com.chainsea.ce.sdk.reference;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.bean.msg.BroadcastFlag;
import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus;
import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus;
import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;

/**
 * current by evan on 2020-02-05
 */
public class MessageReference extends AbsReference {
    private static final String TAG = MessageReference.class.getSimpleName();

    public enum Sort {
        ASC, DESC
    }

    public static boolean hasLocalData(SQLiteDatabase db, String roomId, String messageId) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                new String[]{DBContract.MessageEntry._ID},
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =? AND " + DBContract.MessageEntry._ID + " =?",
                new String[]{roomId, messageId},
                null,
                null,
                null
            );

            boolean status = cursor.getCount() > 0;
            cursor.close();
            return status;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static MessageEntity findById(String id) {
        if (Strings.isNullOrEmpty(id)) {
            return null;
        }
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry._ID + " =?",
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
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            MessageEntity messageEntityBuilder = MessageEntity.formatByCursor(index, cursor);
            cursor.close();
            return messageEntityBuilder;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }


    /**
     * find The first unread message ID
     *
     * @param db
     * @param roomId
     * @return
     */
    public static String findUnreadFirstMessageIdByRoomId(SQLiteDatabase db, String roomId) {
        try {
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =? AND " + DBContract.MessageEntry.COLUMN_FLAG + " IN(0,1)",
                new String[]{roomId},
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " ASC",
                "1"
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return "";
            }
            cursor.moveToFirst();
            String unreadFirstMessageId = Tools.getDbString(cursor, DBContract.MessageEntry._ID);
            return StringHelper.getString(unreadFirstMessageId, "").toString();
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return "";
        }
    }

    /**
     * 找該聊天室最舊的訊息
     *
     * @param db
     * @param roomId
     * @return
     */
    public static String findTopMessageIdByRoomId(SQLiteDatabase db, String roomId) {
        try {

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =? AND " + DBContract.MessageEntry.COLUMN_FLAG + " !=?",
                new String[]{roomId, String.valueOf(MessageFlag.DELETED.getFlag())},
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " ASC",
                "1"
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return "";
            }
            cursor.moveToFirst();
            String topId = Tools.getDbString(cursor, DBContract.MessageEntry._ID);
            cursor.close();
            return topId;
        } catch (Exception e) {
            return "";
        }
    }

    public static String findLastMessageIdByRoomId(SQLiteDatabase db, String roomId) {
        try {

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.LastMessageEntry.TABLE_NAME,
                null,
                DBContract.LastMessageEntry.COLUMN_ROOM_ID + " =? AND " + DBContract.LastMessageEntry.COLUMN_FLAG + " !=?",
                new String[]{roomId, String.valueOf(MessageFlag.DELETED.getFlag())},
                null,
                null,
                null,
                null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return "";
            }
            cursor.moveToFirst();
            String topId = Tools.getDbString(cursor, DBContract.LastMessageEntry.COLUMN_ID);
            cursor.close();
            return topId;
        } catch (Exception e) {
            return "";
        }
    }

    public static List<MessageEntity> findByIdsAndRoomId(SQLiteDatabase db, String[] messageIds, String roomId) {
        if (messageIds == null || messageIds.length == 0) {
            return Lists.newArrayList();
        }
        try {
            List<MessageEntity> entities = Lists.newArrayList();

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =? AND " + DBContract.MessageEntry._ID + " IN(" + concatStrings("'", ",", messageIds) + ")",
                new String[]{roomId},
                null,
                null,
                null
            );


            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }

            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            while (cursor.moveToNext()) {
                MessageEntity builder = MessageEntity.formatByCursor(index, cursor);
                entities.add(builder);
            }
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    /**
     * 查詢單筆資訊
     *
     * @param db
     * @param messageId
     * @param roomId
     * @return
     */
    public static MessageEntity findByIdAndRoomId(SQLiteDatabase db, String messageId, String roomId) {
        if (Strings.isNullOrEmpty(messageId)) {
            return null;
        }

        try {
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry._ID + " =? AND " + DBContract.MessageEntry.COLUMN_ROOM_ID + " =?",
                new String[]{messageId, roomId},
                null,
                null,
                null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            MessageEntity entity = MessageEntity.formatByCursor(index, cursor);
            if (MessageType.BROADCAST.equals(entity.getType())) {
                List<TopicEntity> topicEntities = TopicReference.findTopicRelByIdAndType(db, entity.getId(), TopicReference.TopicRelType.MESSAGE);
                if (!topicEntities.isEmpty()) {
                    entity.setTopicArray(topicEntities);
                }
            }
            cursor.close();
            return entity;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    /**
     * 依照 類型查詢 message entity
     *
     * @return
     * @version 1.9.1
     */
    public static List<MessageEntity> findByType(SQLiteDatabase db, List<MessageType> types) {

        try {
            List<MessageEntity> messages = Lists.newArrayList();

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                "UPPER(" + DBContract.MessageEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size()) + ") AND " + DBContract.MessageEntry.COLUMN_FLAG + " != ?",
                new String[]{String.valueOf(MessageFlag.DELETED.getFlag())},
                null,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " ASC "
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);

            while (cursor.moveToNext()) {
                MessageEntity msg = MessageEntity.formatByCursor(index, cursor);
                messages.add(msg);
            }
            cursor.close();
            return messages;
        } catch (Exception e) {
            CELog.e("DBManager.dimQueryMessages() Error", e);
            return Lists.newArrayList();
        }
    }

    public static List<MessageEntity> findAllMessagesByTypeAndKeyWord(List<MessageType> types, String keyword) {
        long dateTime = System.currentTimeMillis();
        try {
            List<MessageEntity> messages = Lists.newArrayList();
            Cursor cursor = DBManager.getInstance().openDatabase().query(
                DBContract.ChatRoomEntry.TABLE_NAME + " AS c INNER JOIN " + DBContract.MessageEntry.TABLE_NAME + " AS m ON c." + DBContract.ChatRoomEntry._ID + " = m." + DBContract.MessageEntry.COLUMN_ROOM_ID +
                    " AND (c." + DBContract.ChatRoomEntry.COLUMN_TYPE + " != 'services' OR c." + DBContract.ChatRoomEntry.COLUMN_TYPE + " != 'subscrible')",
                new String[]{"c." + DBContract.ChatRoomEntry._ID, "c." + DBContract.ChatRoomEntry.COLUMN_TYPE, "m.*"},
                "UPPER(m." + DBContract.MessageEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size()) +
                    ") AND m." + DBContract.MessageEntry.COLUMN_FLAG + "!= " + MessageFlag.DELETED.getFlag() +
                    " AND ((m." + DBContract.MessageEntry.COLUMN_CONTENT + " NOT LIKE '%\"text\":\"%'" +
                    " AND m." + DBContract.MessageEntry.COLUMN_CONTENT + " LIKE '%" + keyword + "%')" +
                    " OR m." + DBContract.MessageEntry.COLUMN_CONTENT + " LIKE '%\"text\":\"%" + keyword + "%\"%')" +
                    " AND m." + DBContract.MessageEntry.COLUMN_SOURCE_TYPE + " NOT LIKE '" + SourceType.SYSTEM.name() + "'" +
                    " AND m." + DBContract.MessageEntry.COLUMN_SOURCE_TYPE + " NOT LIKE '" + SourceType.LOGIN.name() + "'",
                toUpperCase(types),
                null,
                null,
                "m." + DBContract.MessageEntry.COLUMN_ROOM_ID + " ASC"
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);

            while (cursor.moveToNext()) {
                MessageEntity msg = MessageEntity.formatByCursor(index, cursor);
                messages.add(msg);
            }
            cursor.close();
            Log.d("Kyle116", String.format("findAllMessagesByTypeAndKeyWord count->%s, use time->%s/秒  ", messages.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return messages;
        } catch (Exception e) {
            CELog.e("DBManager.findAllMessagesByTypeAndKeyWord() Error", e);
            return Lists.newArrayList();
        }
    }

    public static List<MessageEntity> findAllMessagesByTypeAndKeyWordForServiceNumber(List<MessageType> types, String keyword) {
        long dateTime = System.currentTimeMillis();
        try {
            List<MessageEntity> messages = Lists.newArrayList();
            Cursor cursor = DBManager.getInstance().openDatabase().query(
                DBContract.ChatRoomEntry.TABLE_NAME + " AS c INNER JOIN " + DBContract.MessageEntry.TABLE_NAME + " AS m ON c." + DBContract.ChatRoomEntry._ID + " = m." + DBContract.MessageEntry.COLUMN_ROOM_ID +
                    " AND (c." + DBContract.ChatRoomEntry.COLUMN_TYPE + " = 'services' OR c." + DBContract.ChatRoomEntry.COLUMN_TYPE + " = 'subscrible' OR c." + DBContract.ChatRoomEntry.COLUMN_TYPE + " = 'serviceMember')",
                new String[]{"c." + DBContract.ChatRoomEntry._ID, "c." + DBContract.ChatRoomEntry.COLUMN_TYPE, "m.*"},
                "UPPER(m." + DBContract.MessageEntry.COLUMN_TYPE + ") IN (" + generatePlaceholdersForIn(types.size()) +
                    ") AND m." + DBContract.MessageEntry.COLUMN_FLAG + "!= " + MessageFlag.DELETED.getFlag() +
                    " AND ((m." + DBContract.MessageEntry.COLUMN_CONTENT + " NOT LIKE '%\"text\":\"%'" +
                    " AND m." + DBContract.MessageEntry.COLUMN_CONTENT + " LIKE '%" + keyword + "%')" +
                    " OR m." + DBContract.MessageEntry.COLUMN_CONTENT + " LIKE '%\"text\":\"%" + keyword + "%\"%')" +
                    " AND m." + DBContract.MessageEntry.COLUMN_SOURCE_TYPE + " NOT LIKE '" + SourceType.SYSTEM.name() + "'" +
                    " AND m." + DBContract.MessageEntry.COLUMN_SOURCE_TYPE + " NOT LIKE '" + SourceType.LOGIN.name() + "'",
                toUpperCase(types),
                null,
                null,
                "m." + DBContract.MessageEntry.COLUMN_ROOM_ID + " ASC"
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);

            while (cursor.moveToNext()) {
                MessageEntity msg = MessageEntity.formatByCursor(index, cursor);
                messages.add(msg);
            }
            cursor.close();
            Log.d("Kyle116", String.format("findAllMessagesByTypeAndKeyWord count->%s, use time->%s/秒  ", messages.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return messages;
        } catch (Exception e) {
            CELog.e("DBManager.findAllMessagesByTypeAndKeyWord() Error", e);
            return Lists.newArrayList();
        }
    }

    private static String[] toUpperCase(List<MessageType> types) {
        String[] result = new String[types.size()];
        for (int i = 0; i < types.size(); i++) {
            result[i] = types.get(i).name().toUpperCase();
        }
        return result;
    }

    private static String generatePlaceholdersForIn(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append("UPPER(?), ");
        }
        // Remove the last comma and space
        builder.setLength(builder.length() - 2);
        return builder.toString();
    }

    /**
     * 刪除 message by ids
     *
     * @param messageIds
     * @return
     * @version 1.9.1
     */
    public static synchronized boolean deleteByIds(String[] messageIds) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            final String whereClause = String.format(DBContract.MessageEntry._ID + " IN (%s)", new Object[]{TextUtils.join(", ", Collections.nCopies(messageIds.length, "?"))});
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_FLAG, MessageFlag.DELETED.getFlag());
            int number = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, messageIds);
            return number == messageIds.length;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static synchronized boolean deleteById(String messageId) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            final String whereClause = DBContract.MessageEntry._ID + " = ?";
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_FLAG, MessageFlag.DELETED.getFlag());
            int number = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, new String[]{messageId});
            return number > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    /**
     * 刪除 message by roomId and messageIds
     *
     * @param roomId
     * @param messageIds
     */
    public static synchronized void deleteByRoomIdAndMessageIds(String roomId, String[] messageIds) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            final String whereClause = String.format(DBContract.MessageEntry.COLUMN_ROOM_ID + "=? AND " + DBContract.MessageEntry._ID + " IN (%s)", new Object[]{TextUtils.join(", ", Collections.nCopies(messageIds.length, "?"))});
            String[] whereArgs = new String[messageIds.length + 1];
            System.arraycopy(messageIds, 0, whereArgs, 1, messageIds.length);
            whereArgs[0] = roomId;
            db.delete(DBContract.MessageEntry.TABLE_NAME, whereClause, whereArgs);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    /**
     * 廣播訊息刪除標籤
     *
     * @return
     */
    public static synchronized boolean updateBroadcastFlag(SQLiteDatabase db, String broadcastRoomId, String messageId, BroadcastFlag flag) {
        try {
            final String whereClause = DBContract.MessageEntry.COLUMN_ROOM_ID + " =?"
                + " AND " + DBContract.MessageEntry._ID + " =?";
            final String[] whereArgs = new String[]{broadcastRoomId, messageId};
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_BROADCAST_FLAG, flag.name());
            int _id = (db == null ? DBManager.getInstance().openDatabase() : db).update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }


    /**
     * 依照聊天室ID 刪除全部訊息
     *
     * @param roomId
     * @return
     */
    public static synchronized boolean deleteByRoomId(String roomId) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String whereClause = DBContract.MessageEntry.COLUMN_ROOM_ID + " = ?";
            String[] args = new String[]{roomId};
            return db.delete(DBContract.MessageEntry.TABLE_NAME, whereClause, args) >= 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }


    public static synchronized void updateFacebookPrivateReplyStatus(String roomId, String messageId, boolean status) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED, status ? 1 : 0);
            long _id = db.update(DBContract.MessageEntry.TABLE_NAME, values, DBContract.MessageEntry.COLUMN_ROOM_ID + " = ? AND " + DBContract.MessageEntry._ID + " = ?", new String[]{roomId, messageId});
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static synchronized void updateFacebookCommentStatus(String roomId, String messageId, FacebookCommentStatus facebookCommentStatus) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_FACEBOOK_COMMENT_STATUS, facebookCommentStatus.name());
            long _id = db.update(DBContract.MessageEntry.TABLE_NAME, values, DBContract.MessageEntry.COLUMN_ROOM_ID + " = ? AND " + DBContract.MessageEntry._ID + " = ?", new String[]{roomId, messageId});
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static synchronized void updateFacebookPostStatus(String roomId, String messageId, FacebookPostStatus facebookPostStatus) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_FACEBOOK_POST_STATUS, facebookPostStatus.name());
            long _id = db.update(DBContract.MessageEntry.TABLE_NAME, values, DBContract.MessageEntry.COLUMN_ROOM_ID + " = ? AND " + DBContract.MessageEntry._ID + " = ?", new String[]{roomId, messageId});
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static synchronized boolean save(String roomId, MessageEntity entity) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = entity.getContentValues(roomId);

            if (MessageType.BROADCAST.equals(entity.getType()) && !entity.getTopicArray().isEmpty()) {
                TopicReference.saveByRelIdAndTopicIdsAndType(db, entity.getId(), entity.getTopicIds(), TopicReference.TopicRelType.MESSAGE);
            }

            boolean isMessageDeleted = checkIsMessageDeleted(entity.getId());
            if (isMessageDeleted) return true;
            long _id = db.update(DBContract.MessageEntry.TABLE_NAME, values, DBContract.MessageEntry._ID + " = ?", new String[]{entity.getId()});
            if (_id > 0) {
                return true;
            } else {
                _id = db.insert(DBContract.MessageEntry.TABLE_NAME, null, values);
                return _id > 0;
            }
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static boolean checkIsMessageDeleted(String messageId) {
        Cursor cursor = null;
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                new String[]{DBContract.MessageEntry.COLUMN_FLAG},
                DBContract.MessageEntry._ID + " =?",
                new String[]{messageId},
                null,
                null,
                null
            );
            cursor.moveToFirst();
            if (cursor.getCount() <= 0) {
                return false;
            }

            return cursor.getInt(0) == MessageFlag.DELETED.getFlag();
        } catch (Exception e) {
            CELog.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public static synchronized boolean updateNearMessage(MessageEntity message) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_THEME_ID, message.getThemeId());
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID, message.getNearMessageId());
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE, message.getNearMessageType().getValue());
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID, message.getNearMessageAvatarId());
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT, message.getNearMessageContent());
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID, message.getNearMessageSenderId());
            values.put(DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME, message.getNearMessageSenderName());
            long _id = db.update(DBContract.MessageEntry.TABLE_NAME, values, DBContract.MessageEntry._ID + " = ?", new String[]{message.getId()});
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static synchronized boolean updateFacebookMessageContent(MessageEntity message) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_CONTENT, message.getContent());
            values.put(DBContract.MessageEntry.COLUMN_TAG, message.getTag());
            long _id = db.update(DBContract.MessageEntry.TABLE_NAME, values, DBContract.MessageEntry._ID + " = ?", new String[]{message.getId()});
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }


    public static synchronized boolean save(SQLiteDatabase db, ChatRoomEntity entity) {
        try {
            if (entity == null) {
                return false;
            }
            if (entity.getLastMessage() == null) {
                return false;
            }
            ContentValues values = entity.getLastMessage().getContentValues(entity.getId());
            (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.MessageEntry.TABLE_NAME, null, values);

            if (MessageType.BROADCAST.equals(entity.getType()) && !entity.getLastMessage().getTopicArray().isEmpty()) {
                TopicReference.saveByRelIdAndTopicIdsAndType(db, entity.getId(), entity.getLastMessage().getTopicIds(), TopicReference.TopicRelType.MESSAGE);
            }
            ContentValues lastMessageValues = ChatRoomEntity.getLastMessageContentValues(entity.getLastMessage());
            long _id = (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.LastMessageEntry.TABLE_NAME, null, lastMessageValues);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }


    /**
     * 存入多筆信息
     *
     * @param roomId
     * @param entities
     * @return
     * @version 1.9.1
     */
    @SuppressLint("Range")
    public static synchronized boolean saveByRoomId(String roomId, List<MessageEntity> entities) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                new String[]{DBContract.MessageEntry._ID},
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =? AND " + DBContract.MessageEntry.COLUMN_FLAG + " =?",
                new String[]{roomId, String.valueOf(MessageFlag.DELETED.getFlag())},
                null,
                null,
                null
            );

            String[] ids = null;
            if (cursor.getCount() != 0) {
                ids = new String[cursor.getCount()];
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    ids[i] = cursor.getString(cursor.getColumnIndex(DBContract.MessageEntry._ID));
                    cursor.moveToNext();
                }
                cursor.close();
            }

            int completeCount = 0;
            if (ids == null) {
                for (MessageEntity message : entities) {
                    ContentValues values = message.getContentValues(roomId);
                    if (MessageType.BROADCAST.equals(message.getType()) && !message.getTopicArray().isEmpty()) {
                        TopicReference.saveByRelIdAndTopicIdsAndType(db, message.getId(), message.getTopicIds(), TopicReference.TopicRelType.MESSAGE);
                    }
                    long _id = db.replace(DBContract.MessageEntry.TABLE_NAME, null, values);
                    if (_id > 0) {
                        completeCount++;
                    }
                }
            } else {
                save:
                {
                    for (MessageEntity message : entities) {
                        ContentValues values = message.getContentValues(roomId);
                        if (ids != null && ids.length > 0) {
                            for (String mId : ids) {
                                if (message.getId().equals(mId)) {
                                    break save;
                                }
                            }
                        }
                        if (MessageType.BROADCAST.equals(message.getType()) && !message.getTopicArray().isEmpty()) {
                            TopicReference.saveByRelIdAndTopicIdsAndType(db, message.getId(), message.getTopicIds(), TopicReference.TopicRelType.MESSAGE);
                        }
                        long _id = db.replace(DBContract.MessageEntry.TABLE_NAME, null, values);
                        if (_id > 0) {
                            completeCount++;
                        }
                    }
                }

            }

            return completeCount == entities.size();
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }


    /**
     * 查詢有無AT我的未讀訊息
     *
     * @param db
     * @param userId
     * @param roomId
     * @return
     */
    public static boolean findUnreadAtMessagesByRoomId(SQLiteDatabase db, String userId, String roomId) {
        try {
            long dateTime = System.currentTimeMillis();
            if (db == null) db = DBManager.getInstance().openDatabase();
            boolean hasUnReadAtMe = false;

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                new String[]{DBContract.MessageEntry._ID},
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " IN(0,1)" +
                    " AND " + DBContract.MessageEntry.COLUMN_TYPE + " =? " +
                    " AND (" + DBContract.MessageEntry.COLUMN_CONTENT + " LIKE ?" +
                    " OR " + DBContract.MessageEntry.COLUMN_CONTENT + " LIKE ?" +
                    " OR " + DBContract.MessageEntry.COLUMN_CONTENT + " LIKE ?)",
                new String[]{
                    roomId,
                    MessageType.AT.getValue(),
                    "'%" + userId + "%'" + " COLLATE NOCASE ESCAPE '/'",
                    "'%" + "\"objectType\":\"All\"" + "%'" + " COLLATE NOCASE ESCAPE '/'",
                    "'%" + "\"objectType\": \"All\"" + "%'" + " COLLATE NOCASE ESCAPE '/'"
                },
                null,
                null,
                null
            );

            if (cursor.getCount() > 0) {
                hasUnReadAtMe = true;
            }
            cursor.close();
            CELog.w(String.format("room find has At Me  by %s, isAtMe->%s, use time->%s/秒  ", roomId, hasUnReadAtMe, (System.currentTimeMillis() - dateTime) / 1000.d));
            return hasUnReadAtMe;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }

    }


    /**
     * 依照RoomId 跟 Message MessageType
     *
     * @param roomId
     * @param messageType
     * @return
     */
    public static List<MessageEntity> findMessageByRoomIdAndMessageType(String roomId, @Nullable MessageType messageType) {

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            List<MessageEntity> messages = Lists.newArrayList();

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_TYPE + " IN('" + messageType.getValue() + "')",
                new String[]{roomId},
                null,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " ASC"
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            while (cursor.moveToNext()) {
                MessageEntity msg = MessageEntity.formatByCursor(index, cursor);
                messages.add(msg);
            }
            cursor.close();
            return messages;
        } catch (Exception e) {
            CELog.e("DBManager.dimQueryMessages() Error", e);
            return Lists.newArrayList();
        }
    }

    public static List<MessageEntity> findAllMediaMessageByRoomId(String roomId) {

        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            List<MessageEntity> messages = Lists.newArrayList();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_TYPE + " IN (?, ?)" +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " NOT IN (?, ?)",
                new String[]{
                    roomId,
                    MessageType.IMAGE.getValue(),
                    MessageType.VIDEO.getValue(),
                    String.valueOf(MessageFlag.RETRACT.getFlag()),
                    String.valueOf(MessageFlag.DELETED.getFlag())
                },
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " ASC"
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            while (cursor.moveToNext()) {
                MessageEntity msg = MessageEntity.formatByCursor(index, cursor);
                messages.add(msg);
            }
            cursor.close();
            return messages;
        } catch (Exception e) {
            CELog.e("DBManager.dimQueryMessages() Error", e);
            return Lists.newArrayList();
        }
    }

    /**
     * update message read flag by room id
     *
     * @param roomId
     * @return
     */
    public static synchronized boolean updateReadFlagByRoomId(String roomId) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            final String whereClause = DBContract.MessageEntry.COLUMN_ROOM_ID + " = ? AND " + DBContract.MessageEntry.COLUMN_FLAG + " IN (0,1)";
            final String[] whereArgs = new String[]{roomId};
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_FLAG, MessageFlag.READ.getFlag());
            int _id = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * find messages by room id
     *
     * @param roomId
     * @return
     * @version 1.9.1
     */
    public static List<MessageEntity> findByRoomId(String roomId) {
        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            List<MessageEntity> messages = Lists.newArrayList();

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?",
                new String[]{roomId},
                null,
                null,
                null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);

            while (cursor.moveToNext()) {
                MessageEntity entity = MessageEntity.formatByCursor(index, cursor);
                if (MessageType.BROADCAST.equals(entity.getType())) {
                    List<TopicEntity> topicEntities = TopicReference.findTopicRelByIdAndType(db, entity.getId(), TopicReference.TopicRelType.MESSAGE);
                    if (!topicEntities.isEmpty()) {
                        entity.setTopicArray(topicEntities);
                    }
                }
                messages.add(entity);
            }
            cursor.close();
            return messages;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return Lists.newArrayList();
        }
    }

    /**
     * 尋找該聊天室的圖片、影片訊息
     *
     * @param roomId 需要查找的聊天室 id
     */
    public static List<MessageEntity> filterMediaMessageByRoomId(String roomId, String sort) {
        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            List<MessageEntity> messages = Lists.newArrayList();

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_TYPE + " IN('" + MessageType.IMAGE.getValue() + "', '" + MessageType.VIDEO.getValue() + "')" +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " != ?" +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " != ?",
                new String[]{
                    roomId,
                    String.valueOf(MessageFlag.RETRACT.getFlag()),
                    String.valueOf(MessageFlag.DELETED.getFlag())
                },
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " " + sort
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);

            while (cursor.moveToNext()) {
                MessageEntity entity = MessageEntity.formatByCursor(index, cursor);
                if (MessageType.BROADCAST.equals(entity.getType())) {
                    List<TopicEntity> topicEntities = TopicReference.findTopicRelByIdAndType(db, entity.getId(), TopicReference.TopicRelType.MESSAGE);
                    if (!topicEntities.isEmpty()) {
                        entity.setTopicArray(topicEntities);
                    }
                }
                messages.add(entity);
            }
            cursor.close();
            return messages;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return Lists.newArrayList();
        }
    }

    public static List<MessageEntity> filterMessageByRoomId(String roomId, String sortType, MessageType... messageType) {
        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            List<MessageEntity> messages = Lists.newArrayList();

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                        " AND " + DBContract.MessageEntry.COLUMN_TYPE + " IN (" + makePlaceholders(messageType.length) + ")" +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " != ?" +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " != ?",
                convertToParams(roomId, messageType),
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " " + sortType
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);

            while (cursor.moveToNext()) {
                MessageEntity entity = MessageEntity.formatByCursor(index, cursor);
                if (MessageType.BROADCAST.equals(entity.getType())) {
                    List<TopicEntity> topicEntities = TopicReference.findTopicRelByIdAndType(db, entity.getId(), TopicReference.TopicRelType.MESSAGE);
                    if (!topicEntities.isEmpty()) {
                        entity.setTopicArray(topicEntities);
                    }
                }
                messages.add(entity);
            }
            cursor.close();
            return messages;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return Lists.newArrayList();
        }
    }

    private static String makePlaceholders(int count) {
        if (count <= 0) return "";
        return new String(new char[count]).replace("\0", "?, ").replaceAll(", $", "");
    }

    public static String[] convertToParams(String roomId, MessageType... messageTypes) {
        List<String> params = new ArrayList<>();
        params.add(roomId);

        for (MessageType type : messageTypes) {
            params.add(type.getType());
        }

        params.add(String.valueOf(MessageFlag.RETRACT.getFlag()));
        params.add(String.valueOf(MessageFlag.DELETED.getFlag()));

        return params.toArray(new String[0]);
    }

    public static MessageEntity findRoomLastMessage(String roomId) {
        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " != ?",
                new String[]{
                    roomId,
                    String.valueOf(MessageFlag.DELETED.getFlag())
                },
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " DESC"
            );
            cursor.moveToFirst();
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            return MessageEntity.formatByCursor(index, cursor);
        } catch (Exception e) {
            CELog.e("findRoomLastMessage Error", e);
            return null;
        }
    }

    public static List<MessageEntity> findByBroadcastRoomId(SQLiteDatabase db, String broadcastRoomId) {
        try {
            List<MessageEntity> list = Lists.newArrayList();

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " != ?" +
                    " AND " + DBContract.MessageEntry.COLUMN_TYPE + " IN('" + MessageType.BROADCAST.getValue() + "','" + MessageType.TEXT.getValue() + "')",
                new String[]{broadcastRoomId},
                null,
                null,
                null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);

            while (cursor.moveToNext()) {
                MessageEntity entity = MessageEntity.formatByCursor(index, cursor);
                if (MessageType.BROADCAST.equals(entity.getType())) {
                    List<TopicEntity> topicEntities = TopicReference.findTopicRelByIdAndType(db, entity.getId(), TopicReference.TopicRelType.MESSAGE);
                    if (!topicEntities.isEmpty()) {
                        entity.setTopicArray(topicEntities);
                    }
                }
                list.add(entity);
            }
            cursor.close();
            return list;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return Lists.newArrayList();
        }
    }

    public static MessageEntity findByIdAndBroadcastRoomId(SQLiteDatabase db, String id, String broadcastRoomId) {
        try {

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_ROOM_ID + " = ?" +
                    " AND " + DBContract.MessageEntry._ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_TYPE + " IN('" + MessageType.BROADCAST.getValue() + "','" + MessageType.TEXT.getValue() + "')",
                new String[]{
                    broadcastRoomId,
                    id
                },
                null,
                null,
                null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();


            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            MessageEntity entity = MessageEntity.formatByCursor(index, cursor);
            if (entity != null) {
                if (MessageType.BROADCAST.equals(entity.getType())) {
                    List<TopicEntity> topicEntities = TopicReference.findTopicRelByIdAndType(db, entity.getId(), TopicReference.TopicRelType.MESSAGE);
                    if (!topicEntities.isEmpty()) {
                        entity.setTopicArray(topicEntities);
                    }
                }

            }
            cursor.close();
            return entity;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return null;
        }
    }


    public static List<String> findUnreadByRoomIdAndSendTime(SQLiteDatabase db, String roomId, long lastTime) {

        try {
            Set<String> messageIdSet = Sets.newHashSet();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_ROOM_ID + " = ?" +
                    " AND " + DBContract.MessageEntry.COLUMN_SEND_TIME + " <=?" +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " < ?",
                new String[]{
                    roomId,
                    String.valueOf(lastTime),
                    "2"
                },
                null,
                null,
                null
            );
            while (cursor.moveToNext()) {
                String id = Tools.getDbString(cursor, DBContract.MessageEntry._ID);
                messageIdSet.add(id);
            }
            return Lists.newArrayList(messageIdSet);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    /**
     * 主要取得 Local 已儲存在 SQLite 內的訊息
     *
     * @param roomId
     * @param time
     * @param chatRoomType
     * @param sort
     * @param limit
     * @return
     */
    public static List<MessageEntity> findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName(String roomId, long time, ChatRoomType chatRoomType, Sort sort, int limit) {
        long useTime = System.currentTimeMillis();
        try {

            SQLiteDatabase db = DBManager.getInstance().openDatabase();
            String[] args;
            if (time > 0) {
                if (limit > 0) {
                    args = new String[]{roomId, String.valueOf(time + 1), String.valueOf(limit)};
                } else {
                    args = new String[]{roomId, String.valueOf(time + 1)};
                }
            } else {
                if (limit > 0) {
                    args = new String[]{roomId, String.valueOf(limit)};
                } else {
                    args = new String[]{roomId};
                }
            }

            String sql = "";
            if (time > 0) {
                sql = " AND " + DBContract.MessageEntry.COLUMN_SEND_TIME;
                if (Sort.DESC.equals(sort)) {
                    sql += " <? ";
                } else {
                    sql += " >? ";
                }
            }

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    sql +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " != ?"
                , args,
                null,
                null,
                null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }

            List<MessageEntity> entities = Lists.newArrayList();
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            while (cursor.moveToNext()) {
                MessageEntity builder = MessageEntity.formatByCursor(index, cursor);
                entities.add(builder);
            }
            cursor.close();
            CELog.w(String.format("MessageReference:: findByRoomIdAndTypeAndSortAndLimitAndInnerJoniAliasName roomId -->%s, count-->%s, use time->%s /s", roomId, entities.size(), ((System.currentTimeMillis() - useTime) / 1000.0d)));
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public static List<MessageEntity> findRoomMessageList(String roomId, int sequence, Sort sort, int limit) {
        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry.COLUMN_SEQUENCE + " <= ?",
                new String[]{roomId, String.valueOf(sequence)},
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " " + sort.name(),
                String.valueOf(limit)
            );


            if (cursor.getCount() <= 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            List<MessageEntity> entities = Lists.newArrayList();
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            while (cursor.moveToNext()) {
                MessageEntity builder = MessageEntity.formatByCursor(index, cursor);
                entities.add(builder);
            }
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }


    /**
     * Find the first message according to room id and MessageStatus[] and Sort
     *
     * @param roomId
     * @param status
     * @param sort
     * @return
     * @version 1.9.1
     */
    public static MessageEntity findMessageByRoomIdAndStatusAndLimitOne(SQLiteDatabase db, String roomId, Integer[] status, Sort sort) {
        try {
            long dateTime = System.currentTimeMillis();
            String statusIn = String.format(DBContract.MessageEntry.COLUMN_STATUS + " IN (%s)", concatStrings("", ",", status));
            String sql = "";
            if (status.length > 0) {
                sql = " AND " + statusIn;
            }

            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                null,
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    sql +
                    " AND " + DBContract.MessageEntry.COLUMN_FLAG + " != ?",
                new String[]{roomId, String.valueOf(MessageFlag.DELETED.getFlag())},
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " " + sort.name(),
                String.valueOf(1)
            );
            if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            Map<String, Integer> index = DBContract.MessageEntry.getIndex(cursor);
            MessageEntity message = MessageEntity.formatByCursor(index, cursor);
            cursor.close();
            CELog.w(String.format("room find last or failed Message  by %s, entity->%s, use time->%s/秒  ", roomId, message != null, (System.currentTimeMillis() - dateTime) / 1000.d));
            return message;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }


    public static String findIdByRoomIdAndSotFormLimitOne(SQLiteDatabase db, String roomId, Sort sort) {
        try {
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                new String[]{DBContract.MessageEntry._ID},
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?",
                new String[]{roomId},
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " " + sort.name(),
                "1"
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                return "";
            }

            cursor.moveToFirst();
            String id = Tools.getDbString(cursor, DBContract.MessageEntry._ID);
            cursor.close();
            return id;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return "";
        }
    }

    public static long findSendTimeByRoomIdAndSotFormLimitOne(SQLiteDatabase db, String roomId, Sort sort) {
        try {
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                new String[]{DBContract.MessageEntry.COLUMN_SEND_TIME},
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?",
                new String[]{roomId},
                null,
                null,
                DBContract.MessageEntry.COLUMN_SEND_TIME + " " + sort.name(),
                "1"
            );
            if (cursor.getCount() == 0) {
                cursor.close();
                return -1;
            }

            cursor.moveToFirst();
            long sendTime = Tools.getDbLong(cursor, DBContract.MessageEntry.COLUMN_SEND_TIME);
            cursor.close();
            return sendTime;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return -1;
        }
    }


    /**
     * 依照 roomId 更新 message send name
     *
     * @param roomId
     * @param name
     * @return
     */
    public static synchronized boolean updateSenderName(String roomId, String name) {
        SQLiteDatabase db = DBManager.getInstance().openDatabase();
        try {
            db.beginTransaction();
            final String whereClause = DBContract.MessageEntry.COLUMN_ROOM_ID + " = ?";
            final String[] args = new String[]{roomId};
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_ROOM_ID, roomId);
            values.put(DBContract.MessageEntry.COLUMN_SENDER_NAME, name);
            boolean isUpdated = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, args) > 0;
            db.setTransactionSuccessful();
            return isUpdated;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public static synchronized boolean updateSendNameBySenderId(String senderId, String name) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            final String whereClause = DBContract.MessageEntry.COLUMN_SENDER_ID + " = ?";
            final String[] args = new String[]{senderId};
            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry.COLUMN_SENDER_ID, senderId);
            values.put(DBContract.MessageEntry.COLUMN_SENDER_NAME, name);
            return db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, args) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    /**
     * 依照 roomId 刪除相關 message
     *
     * @param roomId
     * @return
     */
    public static synchronized boolean deleteMessageByRoomId(String roomId) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            final String messageSelection = DBContract.MessageEntry.COLUMN_ROOM_ID + " = ?";
            final String[] selectionArgs = new String[]{roomId};
            int l = db.delete(DBContract.MessageEntry.TABLE_NAME, messageSelection, selectionArgs);
            if (l > 0) {
            }
            return l > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }


    public static synchronized boolean updateMessageFormat(String messageId, String formatName, String formatContent) {
        try {
            final SQLiteDatabase db = DBManager.getInstance().openDatabase();
            final String whereClause = DBContract.MessageEntry._ID + " = ?";
            final String[] whereArgs = new String[]{messageId};

            ContentValues values = new ContentValues();
            values.put(DBContract.MessageEntry._ID, messageId);
//        values.put(DBContract.MessageEntry.COLUMN_FORMAT, formatName);
            values.put(DBContract.MessageEntry.COLUMN_CONTENT, formatContent);
            int _id = db.update(DBContract.MessageEntry.TABLE_NAME, values, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }


    public static Set<String> findDoesNotExistIdsByIds(SQLiteDatabase db, String roomId, Set<String> messageIds) {
        try {
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.MessageEntry.TABLE_NAME,
                new String[]{DBContract.MessageEntry._ID},
                DBContract.MessageEntry.COLUMN_ROOM_ID + " =?" +
                    " AND " + DBContract.MessageEntry._ID + " IN(" + concatStrings("'", ",", messageIds.toArray()) + ")",
                new String[]{roomId},
                null,
                null,
                null
            );
            if (cursor.getCount() == 0) {
                cursor.close();
                return messageIds;
            }

            while (cursor.moveToNext()) {
                String id = Tools.getDbString(cursor, DBContract.MessageEntry._ID);
                messageIds.remove(id);
            }
            cursor.close();
            return messageIds;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return messageIds;
        }
    }
}
