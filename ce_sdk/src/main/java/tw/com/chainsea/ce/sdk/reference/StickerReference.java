package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.common.EnableType;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.bean.sticker.EmoticonType;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerPackageEntity;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-09-30
 *
 * @author Evan Wang
 * date 2020-09-30
 */
public class StickerReference extends AbsReference {

    public static boolean hasEmojiData(SQLiteDatabase db) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            String packageId = "";
            int itemCount = -1;
            boolean emojiPackage = false;
            boolean emojiItem = false;
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.StickerPackageEntry.TABLE_NAME,
                    null,
                    DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + " =?",
                    new String[]{EmoticonType.EMOJI.name()},
                    null,
                    null,
                    null
            );


            if (cursor.getCount() > 0) {
                emojiPackage = true;
                cursor.moveToFirst();
                packageId = Tools.getDbString(cursor, DBContract.StickerPackageEntry._ID);
                itemCount = Tools.getDbInt(cursor, DBContract.StickerPackageEntry.COLUMN_ITEM_COUNT);
            }

            if (db == null) db = DBManager.getInstance().openDatabase();
            cursor = db.query(
                    DBContract.StickerItemEntry.TABLE_NAME,
                    null,
                    DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + " =?",
                    new String[]{packageId},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() >= itemCount && itemCount > 0) {
                emojiItem = true;
            }
            cursor.close();
            return emojiPackage && emojiItem;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static boolean emojiSave(SQLiteDatabase db, StickerPackageEntity entity) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            ContentValues values = StickerPackageEntity.getContentValues(entity, System.currentTimeMillis(), EmoticonType.EMOJI);
            long _id = db.replace(DBContract.StickerPackageEntry.TABLE_NAME, null, values);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }


    public static boolean packageSave(SQLiteDatabase db, List<StickerPackageEntity> entities) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {
            boolean result = true;
            long updateTime = System.currentTimeMillis();
            for (StickerPackageEntity entity : entities) {
                ContentValues values = StickerPackageEntity.getContentValues(entity, updateTime, EmoticonType.STICKER);
                long _id = db.replace(DBContract.StickerPackageEntry.TABLE_NAME, null, values);
                result = result && _id > 0;
            }
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static boolean updateDisablePackageByIds(SQLiteDatabase db, Set<String> ids) {
        try {
            boolean status = true;
            for (String id : ids) {
                final String whereClause = DBContract.StickerPackageEntry._ID + " = ?";
                final String[] whereArgs = new String[]{id};
                ContentValues values = new ContentValues();
                values.put(DBContract.StickerPackageEntry.COLUMN_IS_ENABLE, EnableType.N.name());
                int _id = (db == null ? DBManager.getInstance().openDatabase() : db).update(DBContract.StickerPackageEntry.TABLE_NAME, values, whereClause, whereArgs);
                status = status ? _id > 0 : status;
            }
            return status;
        } catch (Exception e) {
            return false;
        }
    }

    public static Set<String> packageFindAllIds(SQLiteDatabase db) {
        try {
            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.StickerPackageEntry.TABLE_NAME,
                    new String[]{DBContract.StickerPackageEntry._ID},
                    DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + " =?",
                    new String[]{EmoticonType.STICKER.name()},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                return Sets.newHashSet();
            }

            Set<String> ids = Sets.newHashSet();
            while (cursor.moveToNext()) {
                String id = Tools.getDbString(cursor, DBContract.StickerPackageEntry._ID);
                ids.add(id);
            }
            cursor.close();
            return ids;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Sets.newHashSet();
        }
    }

    public static List<StickerPackageEntity> packageFindByIds(SQLiteDatabase db, Context context, Set<String> ids) {
        try {
            List<StickerPackageEntity> list = Lists.newArrayList();

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.StickerPackageEntry.TABLE_NAME,
                    null,
                    DBContract.StickerPackageEntry._ID + " IN("+ concatStrings("'", ",", ids.toArray()) +") AND " + DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + " =?",
                    new String[]{EnableType.Y.name()},
                    null,
                    null,
                    null
            );

            while (cursor.moveToNext()) {
                StickerPackageEntity entity = StickerPackageEntity.formatByCursor(cursor, Lists.newArrayList()).build();
                List<StickerItemEntity> items = itemFindAll(db, entity.getId(), context);
                if (!items.isEmpty()) {
                    Collections.sort(items);
                    entity.setStickerItems(items);
                }
                list.add(entity);
            }
            cursor.close();
            return list;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public static List<StickerPackageEntity> packageFindAll(SQLiteDatabase db, Context context) {
        try {
            List<StickerPackageEntity> list = Lists.newArrayList();

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.StickerPackageEntry.TABLE_NAME,
                    null,
                    DBContract.StickerPackageEntry.COLUMN_IS_ENABLE + " =?",
                    new String[]{EnableType.Y.name()},
                    null,
                    null,
                    null
            );

            while (cursor.moveToNext()) {
                StickerPackageEntity entity = StickerPackageEntity.formatByCursor(cursor, Lists.newArrayList()).build();
                List<StickerItemEntity> items = itemFindAll(db, entity.getId(), context);
                if (!items.isEmpty()) {
                    Collections.sort(items);
                    entity.setStickerItems(items);
                }
                list.add(entity);
            }
            cursor.close();
            return list;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }


    public static List<StickerItemEntity> itemFindAll(SQLiteDatabase db, String stickerPackageId, Context context) {
        try {
            List<StickerItemEntity> list = Lists.newArrayList();

            if (db == null) db = DBManager.getInstance().openDatabase();
            Cursor cursor = db.query(
                    DBContract.StickerItemEntry.TABLE_NAME,
                    null,
                    DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + " =?",
                    new String[]{stickerPackageId},
                    null,
                    null,
                    null
            );

            while (cursor.moveToNext()) {
                list.add(StickerItemEntity.formatByCursor(cursor));
            }
            cursor.close();
            return list;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public static boolean itemSave(SQLiteDatabase db, List<StickerItemEntity> entities) {
        if (db == null) {
            db = DBManager.getInstance().openDatabase();
        }
        try {

            boolean result = true;
            long updateTime = System.currentTimeMillis();
            for (StickerItemEntity entity : entities) {
                ContentValues values = StickerItemEntity.getContentValues(entity, updateTime);
                long _id = db.replace(DBContract.StickerItemEntry.TABLE_NAME, null, values);
                result = result && _id > 0;
            }
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

}
