package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.ce.sdk.bean.account.AccountType;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.msg.Tools;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;

/**
 * current by evan on 2020-03-30
 *
 * @author Evan Wang
 * @date 2020-03-30
 */
public class UserProfileReference extends AbsReference {


    public static boolean hasLocalData(SQLiteDatabase db, String userId) {


        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }
            Cursor cursor = db.query(
                    DBContract.UserProfileEntry.TABLE_NAME,
                    new String[]{DBContract.UserProfileEntry.COLUMN_ID},
                    DBContract.UserProfileEntry.COLUMN_ID + "=?",
                    new String[]{userId},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                return false;
            }
            boolean status = cursor.getCount() > 0;
            cursor.close();
            return status;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }


    public static String findAvatarIdByUserId(SQLiteDatabase db, String userId) {
        if (Strings.isNullOrEmpty(userId)) {
            return "";
        }
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }
            Cursor cursor = db.query(
                    DBContract.UserProfileEntry.TABLE_NAME,
                    new String[]{DBContract.UserProfileEntry.COLUMN_AVATAR_URL},
                    DBContract.UserProfileEntry.COLUMN_ID + "=?",
                    new String[]{userId},
                    null,
                    null,
                    null
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                return "";
            }
            cursor.moveToFirst();
            String avatarId = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_AVATAR_URL);
            cursor.close();
            return avatarId;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return "";
        }
    }


    public static boolean saveUserProfile(SQLiteDatabase db, UserProfileEntity account) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }
            String id = account.getId();
            ContentValues friendValues = getFrinedValues(account);

            final String friendSelection = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
            final String[] friendSelectionArgs = new String[]{id};
            long _id;
            Cursor friendCursor = db.query(DBContract.UserProfileEntry.TABLE_NAME, null, friendSelection, friendSelectionArgs, null, null, null);
            if (friendCursor.getCount() <= 0) {
                friendValues.put(DBContract.UserProfileEntry.COLUMN_ID, id);
                _id = db.insert(DBContract.UserProfileEntry.TABLE_NAME, null, friendValues);
            } else {
                _id = db.update(DBContract.UserProfileEntry.TABLE_NAME, friendValues, friendSelection, friendSelectionArgs);
            }
            friendCursor.close();
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static boolean saveUserProfiles(SQLiteDatabase db, Set<UserProfileEntity> profileEntities) {
        long useTime = System.currentTimeMillis();
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }

            final String friendSelection = DBContract.UserProfileEntry.COLUMN_ID + " = ?";

            boolean result = true;
            Cursor cursor = null;
            long _id;
            for (UserProfileEntity entity : profileEntities) {
                String id = entity.getId();
                cursor = db.query(
                        DBContract.UserProfileEntry.TABLE_NAME,
                        null,
                        DBContract.UserProfileEntry.COLUMN_ID + "=?",
                        new String[]{id},
                        null,
                        null,
                        null
                );

                final String[] selectionArgs = new String[]{id};
                if (cursor.getCount() <= 0) {
                    _id = db.insert(DBContract.UserProfileEntry.TABLE_NAME, null, getFrinedValues(entity));
                } else {
                    _id = db.update(DBContract.UserProfileEntry.TABLE_NAME, getFrinedValues(entity), friendSelection, selectionArgs);
                }
                result = result && _id > 0;
//                long _id = db.replace(DBContract.UserProfileEntry.COLUMN_ID, null, getFrinedValues(entity));
//                result = !result ? false : _id > 0;
            }
            if (cursor != null) {
                cursor.close();
            }

            CELog.w(String.format("UserProfileReference:: saveUserProfiles use time::: %s /s", ((System.currentTimeMillis() - useTime) / 1000.0d)));
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            CELog.w(String.format("UserProfileReference:: saveUserProfiles use time::: %s /s", ((System.currentTimeMillis() - useTime) / 1000.0d)));
            return false;
        }
    }

    @NonNull
    private static ContentValues getFrinedValues(UserProfileEntity account) {
        String id = account.getId();
        String avatarUrl = account.getAvatarId();
        String nickName = account.getNickName();
        String name = account.getName();
        String customerName = account.getCustomerName();
        String customerDescription = account.getCustomerDescription();
        String customerBusinessCardUrl = account.getCustomerBusinessCardUrl();
        String mood = account.getMood();
        AccountType accountType = account.getType();
//        boolean isBlock = account.isBlock();
        String alias = account.getAlias();
        String roomId = account.getRoomId();
        boolean collection = account.isCollection();
        UserType userType = account.getUserType();
        String department = account.getDepartment();
        String duty = account.getDuty();

        ContentValues friendValues = new ContentValues();

        friendValues.put(DBContract.UserProfileEntry.COLUMN_ID, id);
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
            friendValues.put(DBContract.UserProfileEntry.COLUMN_SIGNATURE, mood);
        }
        if (alias != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_ALIAS, alias);
        }
        if (roomId != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_ROOM_ID, roomId);
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
//        friendValues.put(DBContract.UserProfileEntry.COLUMN_BLOCK, isBlock ? 1 : 0);
        friendValues.put(DBContract.UserProfileEntry.COLUMN_COLLECTION, collection ? "true" : "false");
        if (accountType != null) {
            friendValues.put(DBContract.UserProfileEntry.COLUMN_RELATION, accountType.getValue());
        } else {
            CELog.w("no accountType");
        }
        return friendValues;
    }

    /**
     * 依照類型與關聯找尋userIds
     *
     */
    public static Set<String> findByUserIdsTypeAndRelation(SQLiteDatabase db, String userType, int relation) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }
            String relationCondition = "";
            String[] selectionArgs = new String[]{userType};
            if (relation != -1) {
                relationCondition = " AND " + DBContract.UserProfileEntry.COLUMN_RELATION + " =? ";
                selectionArgs = new String[]{userType, String.valueOf(relation)};
            }

            Cursor cursor = db.query(
                    DBContract.UserProfileEntry.TABLE_NAME,
                    null,
                    DBContract.UserProfileEntry.COLUMN_USER_TYPE + " =?" + relationCondition,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor.getCount() <= 0) {
                cursor.close();
                return Sets.newHashSet();
            }

            Set<String> ids = Sets.newHashSet();

            while (cursor.moveToNext()) {
                String userId = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ID);
                ids.add(userId);
            }
            cursor.close();

            return ids;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Sets.newHashSet();
        }

    }



    public static String findRoomIdByAccountId(SQLiteDatabase db, String accountId) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }

            Cursor cursor = db.query(
                    DBContract.UserProfileEntry.TABLE_NAME,
                    new String[]{DBContract.UserProfileEntry.COLUMN_ROOM_ID},
                    DBContract.UserProfileEntry.COLUMN_ID + " =?",
                    new String[]{accountId},
                    null,
                    null,
                    null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            String roomId = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ROOM_ID);
            cursor.close();
            return roomId;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    public static UserProfileEntity findCustomerInfoByAccountId(String accountId) {
        try {
            SQLiteDatabase db = DBManager.getInstance().openDatabase();

            Cursor cursor = db.query(
                DBContract.UserProfileEntry.TABLE_NAME,
                null,
                DBContract.UserProfileEntry.COLUMN_ID + " =?",
                new String[]{accountId},
                null,
                null,
                null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            if (cursor.moveToFirst()) {
                UserProfileEntity accountCE = UserProfileEntity.getEntity(cursor);
                cursor.close();
                return accountCE;
            }
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
        return null;
    }

    public static String findAccountName(SQLiteDatabase db, String accountId) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }

            Cursor cursor = db.query(
                    DBContract.UserProfileEntry.TABLE_NAME,
                    new String[]{DBContract.UserProfileEntry.COLUMN_ALIAS, DBContract.UserProfileEntry.COLUMN_NICKNAME},
                    DBContract.UserProfileEntry.COLUMN_ID + " =?",
                    new String[]{accountId},
                    null,
                    null,
                    null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            String alias = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ALIAS);
            String nickname = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_NICKNAME);
            cursor.close();
            return StringHelper.getString(alias, nickname).toString();
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    public static String findAccountAvatarId(SQLiteDatabase db, String accountId) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }

            Cursor cursor = db.query(
                    DBContract.UserProfileEntry.TABLE_NAME,
                    new String[]{DBContract.UserProfileEntry.COLUMN_AVATAR_URL},
                    DBContract.UserProfileEntry.COLUMN_ID + " =?",
                    new String[]{accountId},
                    null,
                    null,
                    null
            );
            if (cursor.getCount() <= 0) {
                cursor.close();
                return null;
            }
            cursor.moveToFirst();
            String avatarId = Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_AVATAR_URL);
            cursor.close();
            return avatarId;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    public static UserProfileEntity findById(SQLiteDatabase db, String id) {
        try {
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }

            Cursor cursor = db.query(
                    DBContract.UserProfileEntry.TABLE_NAME,
                    null,
                    DBContract.UserProfileEntry.COLUMN_ID + " =?",
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
            UserProfileEntity entity = UserProfileEntity.Build()
                    .id(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ID))
                    .avatarId(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_AVATAR_URL))
                    .nickName(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_NICKNAME))
                    .name(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_NAME))
                    .customerName(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_CUSTOMER_NAME))
                    .customerDescription(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION))
                    .customerBusinessCardUrl(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL))
                    .mood(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_SIGNATURE))
                    .type(AccountType.of(Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_RELATION)))
                    .alias(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ALIAS))
                    .roomId(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_ROOM_ID))
                    .isBlock(1 == Tools.getDbInt(cursor, DBContract.UserProfileEntry.COLUMN_BLOCK))
                    .isCollection("true".equals(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_COLLECTION)))
                    .otherPhone(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_OTHER_PHONE))
                    .userType(UserType.of(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_USER_TYPE)))
                    .department(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_DEPARTMENT))
                    .duty(Tools.getDbString(cursor, DBContract.UserProfileEntry.COLUMN_DUTY))
                    .build();

            cursor.close();
            return entity;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return null;
        }
    }

    public static String getDiscussTitle(SQLiteDatabase db, String roomId, String selfId, int limit, int originalQuantity) {
        try {
            long dateTime = System.currentTimeMillis();
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }

            Cursor cursor = db.query(
                    true,
                    DBContract.AccountRoomRel.TABLE_NAME + " AS r " +
                            "INNER JOIN " + DBContract.UserProfileEntry.TABLE_NAME +
                            " AS f ON f." + DBContract.UserProfileEntry.COLUMN_ID +
                            "= r." + DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID,
                    new String[] {"f." + DBContract.UserProfileEntry.COLUMN_ID, "f." + DBContract.UserProfileEntry.COLUMN_NICKNAME, "f." + DBContract.UserProfileEntry.COLUMN_ALIAS},
                    "r." + DBContract.AccountRoomRel.COLUMN_ROOM_ID + " =? AND r." + DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID + " =?",
                    new String[]{roomId, selfId},
                    null,
                    null,
                    "f." + DBContract.UserProfileEntry.COLUMN_ID + " ASC ",
                    String.valueOf(limit)
            );

            if (cursor.getCount() == 0) {
                cursor.close();
                return "";
            }
            Map<String, Integer> index = DBContract.UserProfileEntry.getIndex(cursor);
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()) {
                Integer nicknameIndex = index.get(DBContract.UserProfileEntry.COLUMN_NICKNAME);
                Integer aliasIndex = index.get(DBContract.UserProfileEntry.COLUMN_ALIAS);

                String nickname = nicknameIndex != null ? cursor.getString(nicknameIndex) : "";
                String alias = aliasIndex != null ? cursor.getString(aliasIndex) : "";

                builder.append(!Strings.isNullOrEmpty(alias) ? alias : nickname);
                builder.append(", ");

//                String nickname = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_NICKNAME))
//                    .map(cursor::getString)
//                    .orElse("");
//                String alias = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_ALIAS))
//                    .map(cursor::getString)
//                    .orElse("");
//
//                builder.append(!Strings.isNullOrEmpty(alias) ? alias : nickname);
//                builder.append(", ");
            }
            cursor.close();

            if (builder.length() > 2) {
                builder.replace(builder.length() - 2, builder.length(), "");
            }

            if (originalQuantity > limit) {
                builder.append("...");
            }

            CELog.w(String.format("room find chat member avatar data by %s, count->%s, use time->%s/秒  ", roomId, builder, (System.currentTimeMillis() - dateTime) / 1000.d));
            return builder.toString();
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return "";
        }
    }


    public static Map<String, String> getMemberAvatarData(SQLiteDatabase db, String roomId, String selfId, int limit) {

        try {

            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }

            Cursor cursor = db.query(
                    true,
                    DBContract.AccountRoomRel.TABLE_NAME + " AS r " +
                            "INNER JOIN " + DBContract.UserProfileEntry.TABLE_NAME +
                            " AS f ON f." + DBContract.UserProfileEntry.COLUMN_ID +
                            "= r." + DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID,
                    new String[] {"f." + DBContract.UserProfileEntry.COLUMN_ID, "f." + DBContract.UserProfileEntry.COLUMN_AVATAR_URL},
                    "r." + DBContract.AccountRoomRel.COLUMN_ROOM_ID + " =? AND r." + DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID + " =?",
                    new String[]{roomId, selfId},
                    null,
                    null,
                    "f." + DBContract.UserProfileEntry.COLUMN_ID + " ASC ",
                    String.valueOf(limit)
            );


            if (cursor.getCount() == 0) {
                cursor.close();
                return Maps.newLinkedHashMap();
            }
            Map<String, Integer> index = DBContract.UserProfileEntry.getIndex(cursor);
            Map<String, String> data = Maps.newLinkedHashMap();

            while (cursor.moveToNext()) {
                String userId = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_ID))
                    .map(cursor::getString)
                    .orElse("");
                String avatarUrl = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_AVATAR_URL))
                    .map(cursor::getString)
                    .orElse("");
                data.put(userId, avatarUrl);
            }
            cursor.close();
//            CELog.w(String.format("room find chat member avatar data by %s, count->%s, use time->%s/秒  ", roomId, data.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return data;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Maps.newLinkedHashMap();
        }
    }

    public static List<UserProfileEntity> findUserProfilesByRoomId(SQLiteDatabase db, String roomId) {
        try {
            long dateTime = System.currentTimeMillis();
            if (db == null) {
                db = DBManager.getInstance().openDatabase();
            }

            Cursor cursor = db.query(
                    true,
                    DBContract.AccountRoomRel.TABLE_NAME + " AS r " +
                            "INNER JOIN " + DBContract.UserProfileEntry.TABLE_NAME +
                            " AS f ON f." + DBContract.UserProfileEntry.COLUMN_ID +
                            "= r." + DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID,
                    null,
                    "r." + DBContract.AccountRoomRel.COLUMN_ROOM_ID + " =? ",
                    new String[]{roomId},
                    null,
                    null,
                    null,
                    null
            );
            if (cursor.getCount() == 0) {
                cursor.close();
                return Lists.newArrayList();
            }

            Set<UserProfileEntity> profiles = Sets.newHashSet();
            Map<String, Integer> index = DBContract.UserProfileEntry.getIndex(cursor);
            while (cursor.moveToNext()) {
                String userId = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_ID))
                    .map(cursor::getString)
                    .orElse("");
                String avatarId = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_AVATAR_URL))
                    .map(cursor::getString)
                    .orElse("");
                String nickName = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_NICKNAME))
                    .map(cursor::getString)
                    .orElse("");
                String name = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_NAME))
                    .map(cursor::getString)
                    .orElse("");
                String customerName = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_CUSTOMER_NAME))
                    .map(cursor::getString)
                    .orElse("");
                String customerDescription = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION))
                    .map(cursor::getString)
                    .orElse("");
                String customerBusinessCardUrl = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL))
                    .map(cursor::getString)
                    .orElse("");
                String mood = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_SIGNATURE))
                    .map(cursor::getString)
                    .orElse("");
                int type = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_RELATION))
                    .map(cursor::getInt)
                    .orElse(-1);
                String alias = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_ALIAS))
                    .map(cursor::getString)
                    .orElse("");
                String roomId_ = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_ROOM_ID))
                    .map(cursor::getString)
                    .orElse("");
                int isBlock = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_BLOCK))
                    .map(cursor::getInt)
                    .orElse(-1);
                String isCollection = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_COLLECTION))
                    .map(cursor::getString)
                    .orElse("");
                String otherPhone = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_OTHER_PHONE))
                    .map(cursor::getString)
                    .orElse("");
                String userType = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_USER_TYPE))
                    .map(cursor::getString)
                    .orElse("");
                String department = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_DEPARTMENT))
                    .map(cursor::getString)
                    .orElse("");
                String duty = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_DUTY))
                    .map(cursor::getString)
                    .orElse("");
                String openId = Optional.ofNullable(index.get(DBContract.UserProfileEntry.COLUMN_OPEN_ID))
                    .map(cursor::getString)
                    .orElse("");
                UserProfileEntity entity = UserProfileEntity.Build()
                        .id(userId)
                        .avatarId(avatarId)
                        .nickName(nickName)
                        .name(name)
                        .customerName(customerName)
                        .customerDescription(customerDescription)
                        .customerBusinessCardUrl(customerBusinessCardUrl)
                        .mood(mood)
                        .type(AccountType.of(type))
                        .alias(alias)
                        .roomId(roomId_)
                        .isBlock(1 == isBlock)
                        .isCollection("true".equals(isCollection))
                        .otherPhone(otherPhone)
                        .userType(UserType.of(userType))
                        .department(department)
                        .duty(duty)
                        .openId(openId)
                        .build();
                profiles.add(entity);
            }
            cursor.close();
            CELog.w(String.format("room find chat member by %s, count->%s, use time->%s/秒  ", roomId, profiles.size(), (System.currentTimeMillis() - dateTime) / 1000.d));
            return Lists.newArrayList(profiles);
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return Lists.newArrayList();
        }
    }

    public static boolean updateUserAvatar(String userId, String avatarId) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBContract.UserProfileEntry.COLUMN_AVATAR_URL, avatarId);
            String whereClause = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
            String[] whereArgs = new String[]{userId};
            int _id = DBManager.getInstance().openDatabase().update(DBContract.UserProfileEntry.TABLE_NAME, contentValues, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static boolean updateCustomerAlias(String userId, String alias) {
        try {
            ContentValues contentvalues = new ContentValues();
            contentvalues.put(DBContract.UserProfileEntry.COLUMN_ALIAS, alias);

            String whereClause = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
            String[] whereArgs = new String[]{userId};
            int _id = DBManager.getInstance().openDatabase().update(DBContract.UserProfileEntry.TABLE_NAME, contentvalues, whereClause, whereArgs);
            return _id > 0;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }

    public static void updateByCursorNameAndValues(SQLiteDatabase db, String id, String cursorName, String values) {
        try {
            ContentValues contentvalues = new ContentValues();
            contentvalues.put(cursorName, values);
            String whereClause = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
            String[] whereArgs = new String[]{id};
            (db == null ? DBManager.getInstance().openDatabase() : db).update(DBContract.UserProfileEntry.TABLE_NAME, contentvalues, whereClause, whereArgs);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }


    public static void updateByCursorNameAndValuesMap(SQLiteDatabase db, String id, Map<String, Object> cursorData) {
        try {
            ContentValues values = new ContentValues();
            for (Map.Entry<String, Object> c : cursorData.entrySet()) {
                if (c.getValue() instanceof String) {
                    values.put(c.getKey(), (String) c.getValue());
                } else if (c.getValue() instanceof Boolean) {
                    values.put(c.getKey(), (Boolean) c.getValue());
                } else if (c.getValue() instanceof Long) {
                    values.put(c.getKey(), (Long) c.getValue());
                } else if (c.getValue() instanceof Double) {
                    values.put(c.getKey(), (Double) c.getValue());
                } else if (c.getValue() instanceof Integer) {
                    values.put(c.getKey(), (Integer) c.getValue());
                } else if (c.getValue() instanceof Float) {
                    values.put(c.getKey(), (Float) c.getValue());
                } else if (c.getValue() instanceof Byte) {
                    values.put(c.getKey(), (Byte) c.getValue());
                } else if (c.getValue() instanceof byte[]) {
                    values.put(c.getKey(), (byte[]) c.getValue());
                } else if (c.getValue() instanceof Short) {
                    values.put(c.getKey(), (Short) c.getValue());
                }
            }

            String whereClause = DBContract.UserProfileEntry.COLUMN_ID + " = ?";
            String[] whereArgs = new String[]{id};
            (db == null ? DBManager.getInstance().openDatabase() : db).update(DBContract.UserProfileEntry.TABLE_NAME, values, whereClause, whereArgs);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    public static void updateUserRoomId(String userId, String roomId) {
        ContentValues values = new ContentValues();
        values.put(DBContract.UserProfileEntry.COLUMN_ROOM_ID, roomId);
        DBManager.getInstance().openDatabase().update(DBContract.UserProfileEntry.TABLE_NAME, values, DBContract.UserProfileEntry.COLUMN_ID + " = ?", new String[]{userId});
    }

    public static boolean updateNickNameById(String userId, String nickName) {
        ContentValues values = new ContentValues();
        values.put(DBContract.UserProfileEntry.COLUMN_NICKNAME, nickName);
        return DBManager.getInstance().openDatabase().update(DBContract.UserProfileEntry.TABLE_NAME, values, DBContract.UserProfileEntry.COLUMN_ID + " = ?", new String[]{userId}) > 0;
    }
}
