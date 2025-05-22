package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.ProcessStatus;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntityKt;
import tw.com.chainsea.ce.sdk.bean.todo.TodoStatus;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;

/**
 * current by evan on 2020-07-14
 *
 * @author Evan Wang
 * @date 2020-07-14
 */
public class TodoReference {

    public static int getExpiredCount(SQLiteDatabase db) {
        try {
            long now = System.currentTimeMillis();
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.TodoEntry.TABLE_NAME,
                new String[]{DBContract.TodoEntry._ID},
                DBContract.TodoEntry.COLUMN_TODO_STATUS + " = ? AND "
                    + DBContract.TodoEntry.COLUMN_REMIND_TIME + " > 0 AND "
                    + DBContract.TodoEntry.COLUMN_REMIND_TIME + " < ? ",
                new String[]{TodoStatus.PROGRESS.name(), String.valueOf(now)},
                null,
                null,
                null
            );


            int count = cursor.getCount();
            cursor.close();
            return count;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return 0;
        }
    }


    public static TodoEntity findById(SQLiteDatabase db, String id) {
        try {

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.TodoEntry.TABLE_NAME,
                null,
                DBContract.TodoEntry._ID + " = ?",
                new String[]{id},
                null,
                null,
                null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                throw new RuntimeException("query not found ");
            }
            cursor.moveToFirst();
            String roomId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID);
            String messageId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID);
            ChatRoomEntity roomEntity = ChatRoomReference.getInstance().findById2("", roomId, false, false, true, false, false);
            MessageEntity messageEntity = MessageReference.findByIdAndRoomId((db == null ? DBManager.getInstance().openDatabase() : db), messageId, roomId);
            TodoEntity entity = TodoEntity.formatByCursor(cursor, messageEntity, roomEntity);

            return entity;
        } catch (Exception e) {
            return null;
        }
    }


    public static List<TodoEntity> findByRoomId(SQLiteDatabase db, String roomId) {

        try {

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.TodoEntry.TABLE_NAME,
                null,
                DBContract.TodoEntry.COLUMN_ROOM_ID + " = ? AND " +
                    DBContract.TodoEntry.COLUMN_TODO_STATUS + " != ?",
                new String[]{roomId, TodoStatus.DELETED.name()},
                null,
                null,
                null
            );

            List<TodoEntity> entities = Lists.newArrayList();
            if (cursor.getCount() <= 0) {
                cursor.close();
                return entities;
            }

            while (cursor.moveToNext()) {
                String messageId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID);
                ChatRoomEntity roomEntity = ChatRoomReference.getInstance().findById2("", roomId, false, false, true, false, false);
                MessageEntity messageEntity = MessageReference.findByIdAndRoomId((db == null ? DBManager.getInstance().openDatabase() : db), messageId, roomId);
                entities.add(TodoEntity.formatByCursor(cursor, messageEntity, roomEntity));
            }
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public static List<TodoEntity> findBySelf(SQLiteDatabase db, String roomId) {

        try {
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.TodoEntry.TABLE_NAME,
                null,
                DBContract.TodoEntry.COLUMN_TODO_STATUS + " !=? " +
                    " AND " + DBContract.TodoEntry.COLUMN_ROOM_ID + " =?",
                new String[]{TodoStatus.DELETED.name(), roomId},
                null,
                null,
                null
            );

            List<TodoEntity> entities = Lists.newArrayList();
            if (cursor.getCount() <= 0) {
                cursor.close();
                return entities;
            }

            while (cursor.moveToNext()) {
                ChatRoomEntity roomEntity = null;
                MessageEntity messageEntity = null;
                String targetRoomId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID);
                if (!Strings.isNullOrEmpty(targetRoomId)) {
                    roomEntity = ChatRoomReference.getInstance().findById2("", roomId, false, false, true, false, false);
                    String messageId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID);
                    if (!Strings.isNullOrEmpty(messageId)) {
                        messageEntity = MessageReference.findByIdAndRoomId((db == null ? DBManager.getInstance().openDatabase() : db), messageId, roomId);
                    }
                }
                entities.add(TodoEntity.formatByCursor(cursor, messageEntity, roomEntity));
            }
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }


    public static List<TodoEntity> findAll(SQLiteDatabase db) {
        try {
            List<TodoEntity> entities = Lists.newArrayList();
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.TodoEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return entities;
            }
            while (cursor.moveToNext()) {
                String roomId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID);
                String messageId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID);
                ChatRoomEntity roomEntity = ChatRoomReference.getInstance().findById2("", roomId, false, false, true, false, false);
                MessageEntity messageEntity = MessageReference.findByIdAndRoomId((db == null ? DBManager.getInstance().openDatabase() : db), messageId, roomId);
                entities.add(TodoEntity.formatByCursor(cursor, messageEntity, roomEntity));
            }
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }

    }


    public static List<TodoEntity> findByStatus(SQLiteDatabase db, TodoStatus status) {
        try {
            List<TodoEntity> entities = Lists.newArrayList();


            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.TodoEntry.TABLE_NAME,
                null,
                DBContract.TodoEntry.COLUMN_TODO_STATUS + " =?",
                new String[]{status.name()},
                null,
                null,
                null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return entities;
            }
            while (cursor.moveToNext()) {
//                String roomId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID);
//                String messageId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID);
//                List<UserProfileEntity> members = UserProfileReference.findUserProfilesByRoomId(db, roomId);
//                MessageEntity messageEntity = MessageReference.findByIdAndRoomId(messageId, roomId);
                entities.add(TodoEntity.formatByCursor(cursor));
            }
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }


    /**
     * 取出全部未同步資料
     *
     * @param db
     * @return
     */
    public static List<TodoEntity> findNotProcessStatus(SQLiteDatabase db, ProcessStatus status) {
        try {
            List<TodoEntity> entities = Lists.newArrayList();

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                DBContract.TodoEntry.TABLE_NAME,
                null,
                DBContract.TodoEntry.COLUMN_PROCESS_STATUS + " !=?",
                new String[]{status.name()},
                null,
                null,
                null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return entities;
            }
            while (cursor.moveToNext()) {
//                String roomId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID);
//                String messageId = Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID);
//                List<UserProfileEntity> members = UserProfileReference.findUserProfilesByRoomId(db, roomId);
//                MessageEntity messageEntity = MessageReference.findByIdAndRoomId(messageId, roomId);
                entities.add(TodoEntity.formatByCursor(cursor));
            }
            cursor.close();
            return entities;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public static boolean save(SQLiteDatabase db, List<TodoEntity> entities) {
        try {
            boolean result = true;
            for (TodoEntity entity : entities) {
                ContentValues values = TodoEntityKt.getContentValues(entity);
                long _id = (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.TodoEntry.TABLE_NAME, null, values);
                result = result && _id > 0;
            }
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static boolean save(SQLiteDatabase db, TodoEntity entity) {
        try {
            ContentValues values = TodoEntityKt.getContentValues(entity);
            long _id = (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.TodoEntry.TABLE_NAME, null, values);
            return _id > 0;
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean update(SQLiteDatabase db, TodoEntity entity) {
        try {
            final String whereClause = DBContract.TodoEntry._ID + " = ?";
            final String[] whereArgs = new String[]{entity.getId()};
            ContentValues values = TodoEntityKt.getUpdateContentValues(entity);
//            values.put(DBContract.TodoEntry.COLUMN_PROCESS_STATUS, status.name());
            return (db == null ? DBManager.getInstance().openDatabase() : db).update(DBContract.TodoEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public static boolean updateRoomId(SQLiteDatabase db, String roomId) {
        try {
            final String whereClause = DBContract.TodoEntry.COLUMN_ROOM_ID + " = ?";
            final String[] whereArgs = new String[]{roomId};
            ContentValues values = new ContentValues();
            values.put(DBContract.TodoEntry.COLUMN_ROOM_ID, "");
            values.put(DBContract.TodoEntry.COLUMN_MESSAGE_ID, "");
            return (db == null ? DBManager.getInstance().openDatabase() : db).update(DBContract.TodoEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public static boolean delete(SQLiteDatabase db, String id) {
        try {
            final String whereClause = DBContract.TodoEntry._ID + " = ?";
            return (db == null ? DBManager.getInstance().openDatabase() : db).delete(DBContract.TodoEntry.TABLE_NAME, whereClause, new String[]{id}) >= 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public static boolean updateProcessStatus(SQLiteDatabase db, String id, ProcessStatus status, long createTime, long updateTime) {
        try {
            final String whereClause = DBContract.TodoEntry._ID + " = ?";
            final String[] whereArgs = new String[]{id};
            ContentValues values = new ContentValues();
            values.put(DBContract.TodoEntry.COLUMN_PROCESS_STATUS, status.name());
            if (createTime > 0) {
                values.put(DBContract.TodoEntry.COLUMN_CREATE_TIME, createTime);
            }
            if (updateTime > 0) {
                values.put(DBContract.TodoEntry.COLUMN_UPDATE_TIME, updateTime);
            }
            return (db == null ? DBManager.getInstance().openDatabase() : db).update(DBContract.TodoEntry.TABLE_NAME, values, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage(), e);
            return false;
        }
    }

    public static boolean save(tw.com.chainsea.ce.sdk.network.model.response.TodoEntity todoEntity) {
        try {
            ContentValues contentValues = todoEntity.getContentValues();
            long _id = DBManager.getInstance().openDatabase().replace(DBContract.TodoEntry.TABLE_NAME, null, contentValues);
            return _id > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
