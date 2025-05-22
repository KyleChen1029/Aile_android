package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-08-20
 *
 * @author Evan Wang
 * @date 2020-08-20
 */
public class TopicReference extends AbsReference {

    public enum TopicRelType {
        MESSAGE,
        ROOM,
        UNDEF
    }


    /**
     * 依照關聯Id 與關聯類型查詢
     *
     * @param db
     * @param relId
     * @param type
     * @return
     */
    public static List<TopicEntity> findTopicRelByIdAndType(SQLiteDatabase db, String relId, TopicRelType type) {
        try {

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.EntityTopicRel.TABLE_NAME,
                    null,
                    DBContract.EntityTopicRel.COLUMN_RELATION_ID + " =?" +
                            " AND " + DBContract.EntityTopicRel.COLUMN_RELATION_TYPE + " =?",
                    new String[]{relId, type.name()},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            List<TopicEntity> list = Lists.newArrayList();
            Set<String> idSet = Sets.newHashSet();
            while (cursor.moveToNext()) {
                String topic = Tools.getDbString(cursor, DBContract.EntityTopicRel.COLUMN_TOPIC_ID);
                idSet.add(topic);
            }
            cursor.close();
            if (!idSet.isEmpty()) {
                String[] topicIds = idSet.toArray(new String[idSet.size()]);
                list = findByIds(db, topicIds);
            }
            return list;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }


    /**
     * 儲存 topic 關聯資訊
     *
     * @param db
     * @param relId
     * @param topicIds
     * @param type
     * @return
     */
    public static boolean saveByRelIdAndTopicIdsAndType(SQLiteDatabase db, String relId, String[] topicIds, TopicRelType type) {

        try {
            boolean result = true;
            String whereClause = DBContract.EntityTopicRel.COLUMN_RELATION_ID + " = ?";
            String[] whereArgs = new String[]{relId};
            boolean status = (db == null ? DBManager.getInstance().openDatabase() : db).delete(DBContract.EntityTopicRel.TABLE_NAME, whereClause, whereArgs) > 0;

            for (String topicId : topicIds) {
                ContentValues values = new ContentValues();
                values.put(DBContract.EntityTopicRel._ID, relId + topicId);
                values.put(DBContract.EntityTopicRel.COLUMN_RELATION_ID, relId);
                values.put(DBContract.EntityTopicRel.COLUMN_TOPIC_ID, topicId);
                values.put(DBContract.EntityTopicRel.COLUMN_RELATION_TYPE, type.name());
                long _id = (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.EntityTopicRel.TABLE_NAME, null, values);
                if (result) {
                    result = _id > 0;
                }
            }
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static boolean deleteTopicRelByRelIdAndType(SQLiteDatabase db, String relId, TopicRelType type) {
        try {
            String whereClause = DBContract.EntityTopicRel.COLUMN_RELATION_ID + " = ?"
                    + " AND " + DBContract.EntityTopicRel.COLUMN_RELATION_TYPE + " =?";
            String[] whereArgs = new String[]{relId, type.name()};
            return (db == null ? DBManager.getInstance().openDatabase() : db).delete(DBContract.EntityTopicRel.TABLE_NAME, whereClause, whereArgs) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 依照 topic ids 查詢
     *
     * @param db
     * @param topicIds
     * @return
     */
    public static List<TopicEntity> findByIds(SQLiteDatabase db, String[] topicIds) {

        try {
            Set<TopicEntity> set = Sets.newHashSet();

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.BroadcastTopicEntry.TABLE_NAME,
                    null,
                    DBContract.BroadcastTopicEntry._ID + " IN(" + concatStrings("'", ",", topicIds) +")",
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                return Lists.newArrayList();
            }
            while (cursor.moveToNext()) {
                TopicEntity entity = TopicEntity.formatByCursor(cursor).build();
                set.add(entity);
            }
            cursor.close();
            return Lists.newArrayList(set);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }


    /**
     * 批量儲存
     *
     * @param db
     * @param entities
     * @return
     */
    public static boolean save(SQLiteDatabase db, List<TopicEntity> entities) {
        try {
            boolean result = true;
            for (TopicEntity entity : entities) {
                if (!entity.isHardCode()) {
                    if (result) {
                        result = save((db == null ? DBManager.getInstance().openDatabase() : db), entity);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    /**
     * 儲存實體
     *
     * @param db
     * @param entity
     * @return
     */
    public static boolean save(SQLiteDatabase db, TopicEntity entity) {
        try {
            ContentValues values = TopicEntity.getContentValues(entity);
            long _id = (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.BroadcastTopicEntry.TABLE_NAME, null, values);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }


    /**
     * 查詢全部
     *
     * @param db
     * @return
     */
    public static List<TopicEntity> findAll(SQLiteDatabase db) {
        try {
            List<TopicEntity> list = Lists.newArrayList();

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.BroadcastTopicEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                return list;
            }
            while (cursor.moveToNext()) {
                TopicEntity entity = TopicEntity.formatByCursor(cursor).build();
                list.add(entity);
            }
            cursor.close();
            return list;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

}
