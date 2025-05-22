package tw.com.chainsea.ce.sdk.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.android.common.log.CELog;

/**
 * current by evan on 2020-05-04
 *
 * @author Evan Wang
 * date 2020-05-04
 */
public abstract class DataBaseVersionHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 84;

    public DataBaseVersionHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void create(SQLiteDatabase db) {
        try {
            // Create chat room entity
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.ChatRoomEntry.TABLE_NAME + "( "
                + DBContract.ChatRoomEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.ChatRoomEntry.COLUMN_TITLE + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_TYPE + " TEXT, "
                + DBContract.ChatRoomEntry.COLUMN_AVATAR_ID + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_OWNER_ID + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_UNREAD_NUMBER + " INTEGER DEFAULT 0, "

                + DBContract.ChatRoomEntry.COLUMN_IS_TOP + " TEXT DEFAULT 'N', "
                + DBContract.ChatRoomEntry.COLUMN_TOP_TIME + " LONG, "
                + DBContract.ChatRoomEntry.COLUMN_IS_CUSTOM_NAME + " TEXT DEFAULT 'N', "
                + DBContract.ChatRoomEntry.COLUMN_IS_MUTE + " TEXT DEFAULT 'N', "

                + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_NAME + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_AVATAR_ID + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + " TEXT DEFAULT 'NONE', "
                + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " TEXT DEFAULT 'OFF_LINE', "

                + DBContract.ChatRoomEntry.COLUMN_BUSINESS_ID + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_BUSINESS_NAME + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_BUSINESS_CODE + " TEXT DEFAULT 'UNDEF', "

                + DBContract.ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY + " TEXT DEFAULT '{}', "

                + DBContract.ChatRoomEntry.COLUMN_UPDATE_TIME + " LONG, "
                + DBContract.ChatRoomEntry.COLUMN_UNFINISHED_EDITED + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_UNFINISHED_EDITED_TIME + " LONG , "
                + DBContract.ChatRoomEntry.COLUMN_LIST_CLASSIFY + " TEXT DEFAULT 'ALL', "
                + DBContract.ChatRoomEntry.COLUMN_SORT_WEIGHTS + " INTEGER DEFAULT 0 , "
                + DBContract.ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OPEN_TYPES + " TEXT DEFAULT '[]',"
                + DBContract.ChatRoomEntry.COLUMN_AI_SERVICE_WARNED + " TEXT DEFAULT 'N',"
                + DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " TEXT DEFAULT 'N',"
                + DBContract.ChatRoomEntry.COLUMN_PROVISIONAL_IDS + " TEXT DEFAULT '[]',"
                + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_STOP + " INTEGER DEFAULT 0,"
                + DBContract.ChatRoomEntry.COLUMN_OWNER_USER_TYPE + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_MEMBER_IDS + " TEXT DEFAULT '[]', "
                + DBContract.ChatRoomEntry.COLUMN_LAST_END_SERVICE_TIME + " LONG DEFAULT 0,"
                + DBContract.ChatRoomEntry.COLUMN_INTERACTION_TIME + " LONG DEFAULT 0,"
                + DBContract.ChatRoomEntry.COLUMN_TRANSFER_FLAG + " INTEGER DEFAULT 0, "
                + DBContract.ChatRoomEntry.COLUMN_TRANSFER_REASON + " TEXT DEFAULT '', "
                + DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER + " TEXT DEFAULT '',"
                + DBContract.ChatRoomEntry.COLUMN_DFR_TIME + " LONG,"
                + DBContract.ChatRoomEntry.COLUMN_IS_AT_ME + " INTEGER DEFAULT 0 , "
                + DBContract.ChatRoomEntry.COLUMN_LAST_SEQUENCE + " INTEGER DEFAULT 0)"
            );


            // Create a chat room entity reference
            db.execSQL("CREATE INDEX " + DBContract.ChatRoomEntryIndex.IDX_COLUMN
                + " ON " + DBContract.ChatRoomEntry.TABLE_NAME + "( "
                + DBContract.ChatRoomEntry.COLUMN_TITLE + ","
                + DBContract.ChatRoomEntry.COLUMN_OWNER_ID + ", "
                + DBContract.ChatRoomEntry.COLUMN_TYPE
                + " );"
            );


            // Create chat room message entity
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.MessageEntry.TABLE_NAME + "("
                + DBContract.MessageEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.MessageEntry.COLUMN_ROOM_ID + " TEXT, "
                + DBContract.MessageEntry.COLUMN_CONTENT + " TEXT, "
                + DBContract.MessageEntry.COLUMN_STATUS + " INTEGER, "
                + DBContract.MessageEntry.COLUMN_SEND_NUM + " INTEGER, "
                + DBContract.MessageEntry.COLUMN_RECEIVED_NUM + " INTEGER, "
                + DBContract.MessageEntry.COLUMN_FLAG + " INTEGER, "
                + DBContract.MessageEntry.COLUMN_READED_NUM + " INTEGER, "
                + DBContract.MessageEntry.COLUMN_SENDER_ID + " TEXT, "
                + DBContract.MessageEntry.COLUMN_FROM + " TEXT, "
                + DBContract.MessageEntry.COLUMN_SENDER_NAME + " TEXT, "
                + DBContract.MessageEntry.COLUMN_SOURCE_TYPE + " TEXT, "
                + DBContract.MessageEntry.COLUMN_SEQUENCE + " INTEGER, "
                + DBContract.MessageEntry.COLUMN_AVATAR_ID + " TEXT, "
                + DBContract.MessageEntry.COLUMN_TYPE + " TEXT, "
                + DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_CONTENT + " TEXT, "
                + DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_TYPE + " TEXT, "
                + DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_ID + " TEXT, "
                + DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_ID + " TEXT, "
                + DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_SEND_NAME + " TEXT, "
                + DBContract.MessageEntry.COLUMN_NEAR_MESSAGE_AVATAR_ID + " TEXT, "
                + DBContract.MessageEntry.COLUMN_THEME_ID + " TEXT, "
                + DBContract.MessageEntry.COLUMN_OS_TYPE + " TEXT, "
                + DBContract.MessageEntry.COLUMN_DEVICE_TYPE + " TEXT, "

                // 1.13.0 Add broadcast message information
                + DBContract.MessageEntry.COLUMN_CREATE_USER_ID + " TEXT DEFAULT '' , "
                + DBContract.MessageEntry.COLUMN_UPDATE_USER_ID + " TEXT DEFAULT '' , "
                + DBContract.MessageEntry.COLUMN_BROADCAST_TIME + " INTEGER , "
                + DBContract.MessageEntry.COLUMN_BROADCAST_FLAG + " TEXT DEFAULT 'BOOKING' , "


                + DBContract.MessageEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' , "
                + DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID + " TEXT DEFAULT '' , "
                + DBContract.MessageEntry.COLUMN_SEND_TIME + " INTEGER , "
                + DBContract.MessageEntry.COLUMN_TAG + " TEXT DEFAULT '', "
                + DBContract.MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED + " INTEGER DEFAULT 0, "
                + DBContract.MessageEntry.COLUMN_FACEBOOK_COMMENT_STATUS + " TEXT DEFAULT '', "
                + DBContract.MessageEntry.COLUMN_FACEBOOK_POST_STATUS + " TEXT DEFAULT '');"
            );


            // Create an entity reference for chat room messages
            db.execSQL("CREATE INDEX " + DBContract.MessageEntryIndex.IDX_COLUMN
                + " ON " + DBContract.MessageEntry.TABLE_NAME + "( "
                + DBContract.MessageEntry.COLUMN_TYPE + ","
                + DBContract.MessageEntry.COLUMN_ROOM_ID + ", "
                + DBContract.MessageEntry.COLUMN_SEND_TIME
                + " );"
            );


            // Establish account chat room connection
            db.execSQL("CREATE TABLE IF NOT EXISTS " + DBContract.AccountRoomRel.TABLE_NAME + "("
                + DBContract.AccountRoomRel.COLUMN_ID + " TEXT PRIMARY KEY, "
                + DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID + " TEXT, "
                + DBContract.AccountRoomRel.COLUMN_ROOM_ID + " TEXT);"
            );

            // Create an account and chat room link reference
            db.execSQL("CREATE INDEX " + DBContract.AccountRoomRelIndex.IDX_COLUMN
                + " ON " + DBContract.AccountRoomRel.TABLE_NAME + "( "
                + DBContract.AccountRoomRel.COLUMN_ACCOUNT_ID + ","
                + DBContract.AccountRoomRel.COLUMN_ROOM_ID
                + " );"
            );

            final String SQL_CREATE_GROUP_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.GroupEntry.TABLE_NAME + "("
                + DBContract.GroupEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.GroupEntry.COLUMN_CUSTOM_NAME + " INTEGER, "
                + DBContract.GroupEntry.COLUMN_KIND + " TEXT, "
                + DBContract.GroupEntry.COLUMN_NAME + " TEXT, "
                + DBContract.GroupEntry.COLUMN_AVATAR_URL + " TEXT, "
                + DBContract.GroupEntry.COLUMN_MEMBER_IDS + " TEXT, "
                + DBContract.GroupEntry.COLUMN_OWNER_ID + " TEXT);";
            db.execSQL(SQL_CREATE_GROUP_TABLE);

            createServiceNumberTable(db);

            final String SQL_CREATE_GROUP_MEMBER_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.GroupMemberEntry.TABLE_NAME + "("
                + DBContract.GroupMemberEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.GroupMemberEntry.COLUMN_ACCOUNT_ID + " TEXT, "
                + DBContract.GroupMemberEntry.COLUMN_NICKNAME + " TEXT, "
                + DBContract.GroupMemberEntry.COLUMN_ALIAS + " TEXT, "
                + DBContract.GroupMemberEntry.COLUMN_AVATAR + " TEXT, "
                + DBContract.GroupMemberEntry.COLUMN_SIGNTURE + " TEXT);";
            db.execSQL(SQL_CREATE_GROUP_MEMBER_TABLE);

            final String SQL_CREATE_SEARCH_RECORDS_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.SearchRecordEntry.TABLE_NAME + "("
                + DBContract.SearchRecordEntry.COLUMN_NAME + " TEXT PRIMARY KEY);";
            db.execSQL(SQL_CREATE_SEARCH_RECORDS_TABLE);

            final String SQL_CREATE_SEARCH_LABEL_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.SearchLabelHistoryEntry.TABLE_NAME + "("
                + DBContract.SearchLabelHistoryEntry.COLUMN_NAME + " TEXT PRIMARY KEY);";
            db.execSQL(SQL_CREATE_SEARCH_LABEL_TABLE);

            final String SQL_CREATE_LABEL_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.LabelEntry.TABLE_NAME + "("
                + DBContract.LabelEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.LabelEntry.COLUMN_NAME + " TEXT, "
                + DBContract.LabelEntry.COLUMN_USER_IDS + " TEXT, "
                + DBContract.LabelEntry.COLUMN_USERS + " TEXT, "
                + DBContract.LabelEntry.COLUMN_CREATE_TIME + " TEXT, "
                + DBContract.LabelEntry.COLUMN_OWNER_ID + " TEXT, "
                + DBContract.LabelEntry.COLUMN_READ_ONLY + " TEXT, "
                + DBContract.LabelEntry.COLUMN_DELETED + " TEXT);";
            db.execSQL(SQL_CREATE_LABEL_TABLE);

            final String SQL_CREATE_RECOMMEND_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.RecommendEntry.TABLE_NAME + "("
                + DBContract.RecommendEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.RecommendEntry.COLUMN_NAME + " TEXT, "
                + DBContract.RecommendEntry.COLUMN_URL + " TEXT);";
            db.execSQL(SQL_CREATE_RECOMMEND_TABLE);


            // New account table
            final String SQL_CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.USER_INFO.TABLE_NAME + "( "
                + DBContract.USER_INFO._ID + " TEXT PRIMARY KEY, "
                + DBContract.USER_INFO.COLUMN_GENDER + " INTEGER, "
                + DBContract.USER_INFO.COLUMN_OTHER_PHONE + " TEXT, "
                + DBContract.USER_INFO.COLUMN_MOBILE + " INTEGER, "
                + DBContract.USER_INFO.COLUMN_LOGIN_NAME + " TEXT, "
                + DBContract.USER_INFO.COLUMN_BIRTHDAY + " TEXT, "
                + DBContract.USER_INFO.COLUMN_EMAIL + " TEXT, "
                + DBContract.USER_INFO.COLUMN_MOOD + " TEXT, "
                + DBContract.USER_INFO.COLUMN_OPEN_ID + " TEXT );";
            db.execSQL(SQL_CREATE_USER_TABLE);

            // New friend list
            createUsersTable(db);

            // New friend-tag table
            final String SQL_CREATE_ACCOUNT_LABEL_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.FriendsLabelRel.TABLE_NAME + "("
                + DBContract.FriendsLabelRel.COLUMN_ID + " TEXT PRIMARY KEY, "
                + DBContract.FriendsLabelRel.COLUMN_ACCOUNT_ID + " TEXT, "
                + DBContract.FriendsLabelRel.COLUMN_LABEL_ID + " TEXT);";
            db.execSQL(SQL_CREATE_ACCOUNT_LABEL_TABLE);

            final String SQL_CREATE_SEARCH_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.SEARCH_HISTORY.TABLE_NAME + "("
                + DBContract.SEARCH_HISTORY.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DBContract.SEARCH_HISTORY.COLUMN_CONTENT + " TEXT,"
                + DBContract.SEARCH_HISTORY.COLUMN_TIME + " TEXT);";
            db.execSQL(SQL_CREATE_SEARCH_HISTORY_TABLE);

            // Create
            createTodoTable(db);

            // topic  entity
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.BroadcastTopicEntry.TABLE_NAME + "("
                + DBContract.BroadcastTopicEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.BroadcastTopicEntry.COLUMN_AVATAR_ID + " TEXT DEFAULT '', "
                + DBContract.BroadcastTopicEntry.COLUMN_NAME + " TEXT DEFAULT '', "
                + DBContract.BroadcastTopicEntry.COLUMN_DESCRIPTION + " TEXT DEFAULT '');"
            );

            // entity topic rel
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.EntityTopicRel.TABLE_NAME + "("
                + DBContract.EntityTopicRel._ID + " TEXT PRIMARY KEY, "
                + DBContract.EntityTopicRel.COLUMN_RELATION_ID + " TEXT, "
                + DBContract.EntityTopicRel.COLUMN_RELATION_TYPE + " TEXT DEFAULT 'UNDEF', "
                + DBContract.EntityTopicRel.COLUMN_TOPIC_ID + " TEXT );"
            );

            // entity topic rel index
            db.execSQL("CREATE INDEX " + DBContract.EntityTopicRelIndex.IDX_COLUMN
                + " ON " + DBContract.EntityTopicRel.TABLE_NAME + "( "
                + DBContract.EntityTopicRel.COLUMN_RELATION_ID + ","
                + DBContract.EntityTopicRel.COLUMN_RELATION_TYPE
                + " );"
            );

            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.BusinessEntry.TABLE_NAME + "("
                + DBContract.BusinessEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.BusinessEntry.COLUMN_BUSINESS_CODE + " TEXT DEFAULT 'Ecp.Task', "
                + DBContract.BusinessEntry.COLUMN_NAME + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_END_TIME + " TEXT, "
                + DBContract.BusinessEntry.COLUMN_END_TIMESTAMP + " INTEGER, "
                + DBContract.BusinessEntry.COLUMN_PRIMARY_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_PRIMARY_NAME + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_MANAGER_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_MANAGER_NAME + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_MANAGER_AVATAR_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_EXECUTOR_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_CUSTOMER_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_CUSTOMER_NAME + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_CUSTOMER_AVATAR_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y', "
                + DBContract.BusinessEntry.COLUMN_DESCRIPTION + " TEXT DEFAULT '' );"
            );


            // statistical data
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.StatisticsEntry.TABLE_NAME + "("
                + DBContract.StatisticsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + DBContract.StatisticsEntry.COLUMN_RELATION_ID + " TEXT, "
                + DBContract.StatisticsEntry.COLUMN_ASCRIPTION + " TEXT DEFAULT '', "
                + DBContract.StatisticsEntry.COLUMN_ORIGINAL_CONTENT + " TEXT , "
                + DBContract.StatisticsEntry.COLUMN_TOTAL_ROW + " INTEGER, "
                + DBContract.StatisticsEntry.COLUMN_ROW_COUNT + " INTEGER, "
                + DBContract.StatisticsEntry.COLUMN_STAT_TYPE + " TEXT, "
                + DBContract.StatisticsEntry.COLUMN_START_TIME + " INTEGER, "
                + DBContract.StatisticsEntry.COLUMN_END_TIME + " INTEGER, "
                + DBContract.StatisticsEntry.COLUMN_UPDATE_TIME + " INTEGER );"
            );

            // statistical data index
            db.execSQL("CREATE INDEX " + DBContract.StatisticsEntryIndex.IDX_COLUMN
                + " ON " + DBContract.StatisticsEntry.TABLE_NAME + "( "
                + DBContract.StatisticsEntry.COLUMN_RELATION_ID + ","
                + DBContract.StatisticsEntry.COLUMN_ASCRIPTION + ","
                + DBContract.StatisticsEntry.COLUMN_ROW_COUNT + ","
                + DBContract.StatisticsEntry.COLUMN_STAT_TYPE
                + " );"
            );

            // Sticker pack information
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.StickerPackageEntry.TABLE_NAME + "("
                + DBContract.StickerPackageEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.StickerPackageEntry.COLUMN_PACKAGE_NAME + " TEXT, "
                + DBContract.StickerPackageEntry.COLUMN_ICON_ID + " TEXT, "
                + DBContract.StickerPackageEntry.COLUMN_ICON_URL + " TEXT, "
                + DBContract.StickerPackageEntry.COLUMN_JOIN_TIME + " INTEGER, "
                + DBContract.StickerPackageEntry.COLUMN_ITEM_COUNT + " INTEGER, "
                + DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + " TEXT DEFAULT 'STICKER', "
                + DBContract.StickerPackageEntry.COLUMN_ITEM_LINE + " INTEGER, "
                + DBContract.StickerPackageEntry.COLUMN_ITEM_ROW + " INTEGER, "
                + DBContract.StickerPackageEntry.COLUMN_SELECT_ACTION + " TEXT DEFAULT 'UNDEF', "
                + DBContract.StickerPackageEntry.COLUMN_UPDATE_TIME + " INTEGER, "

                + DBContract.StickerPackageEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' );"
            );

            // Sticker Information
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.StickerItemEntry.TABLE_NAME + "("
                + DBContract.StickerItemEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.StickerItemEntry.COLUMN_NAME + " TEXT, "
                + DBContract.StickerItemEntry.COLUMN_DISPLAY_NAME + " TEXT, "
                + DBContract.StickerItemEntry.COLUMN_SORT_INDEX + " INTEGER, "
                + DBContract.StickerItemEntry.COLUMN_KEYWORDS + " TEXT, "
                + DBContract.StickerItemEntry.COLUMN_PICTURE_ID + " TEXT, "
                + DBContract.StickerItemEntry.COLUMN_PICTURE_URL + " TEXT, "
                + DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_ID + " TEXT, "
                + DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_URL + " TEXT, "
                + DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + " TEXT, "
                + DBContract.StickerItemEntry.COLUMN_UPDATE_TIME + " INTEGER, "
                + DBContract.StickerItemEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' );"
            );

            // Sticker Information index
            db.execSQL("CREATE INDEX " + DBContract.StickerItemEntryIndex.IDX_COLUMN
                + " ON " + DBContract.StickerItemEntry.TABLE_NAME + "( "
                + DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + ","
                + DBContract.StickerItemEntry.COLUMN_KEYWORDS
                + " );"
            );

            // entity service number agent rel table
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.ServiceNumberAgentRel.TABLE_NAME + "("
                + DBContract.ServiceNumberAgentRel._ID + " TEXT PRIMARY KEY, "
                + DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + " TEXT, "
                + DBContract.ServiceNumberAgentRel.COLUMN_BROADCAST_ROOM_ID + " TEXT, "
                + DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID + " TEXT, "
                + DBContract.ServiceNumberAgentRel.COLUMN_AGENT_PRIVILEGE + " TEXT DEFAULT 'UNDEF');"
            );

            // entity service number agent rel index
            db.execSQL("CREATE INDEX " + DBContract.ServiceNumberAgentRelIndex.IDX_COLUMN
                + " ON " + DBContract.ServiceNumberAgentRel.TABLE_NAME + "( "
                + DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + ","
                + DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID
                + " );"
            );

            createApiInfo(db);
            createBossServiceNumberContactTable(db);

            createGroupTable(db);
            createLastMessageTable(db);
            createChatMemberTable(db);
            createChatRoomMemberIdsTable(db);

        } catch (SQLException e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v49_to_v50(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.MessageEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.MessageEntry.COLUMN_PREVIOUS_MESSAGE_ID + " TEXT  DEFAULT '' ");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v50_to_v51(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_IS_MUTE + " TEXT  DEFAULT 'false' ");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v52_to_v53(SQLiteDatabase db) {
        try {
            // topic  entity
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.BroadcastTopicEntry.TABLE_NAME + "("
                + DBContract.BroadcastTopicEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.BroadcastTopicEntry.COLUMN_AVATAR_ID + " TEXT DEFAULT '', "
                + DBContract.BroadcastTopicEntry.COLUMN_NAME + " TEXT DEFAULT '', "
                + DBContract.BroadcastTopicEntry.COLUMN_DESCRIPTION + " TEXT DEFAULT '');"
            );

            // entity topic rel
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.EntityTopicRel.TABLE_NAME + "("
                + DBContract.EntityTopicRel._ID + " TEXT PRIMARY KEY, "
                + DBContract.EntityTopicRel.COLUMN_RELATION_ID + " TEXT, "
                + DBContract.EntityTopicRel.COLUMN_RELATION_TYPE + " TEXT DEFAULT 'UNDEF', "
                + DBContract.EntityTopicRel.COLUMN_TOPIC_ID + " TEXT );"
            );

            // entity topic rel index
            db.execSQL("CREATE INDEX " + DBContract.EntityTopicRelIndex.IDX_COLUMN
                + " ON " + DBContract.EntityTopicRel.TABLE_NAME + "( "
                + DBContract.EntityTopicRel.COLUMN_RELATION_ID + ","
                + DBContract.EntityTopicRel.COLUMN_RELATION_TYPE
                + " );"
            );

            // 1.13.0 Add broadcast message information
            List<String> columnNames = Lists.newArrayList(
                DBContract.MessageEntry.COLUMN_CREATE_USER_ID + " TEXT DEFAULT '' ",
                DBContract.MessageEntry.COLUMN_UPDATE_USER_ID + " TEXT DEFAULT '' ",
                DBContract.MessageEntry.COLUMN_BROADCAST_TIME + " INTEGER ",
                DBContract.MessageEntry.COLUMN_BROADCAST_FLAG + " TEXT DEFAULT 'BOOKING' "
            );

            for (String column : columnNames) {
                db.execSQL("ALTER TABLE " + DBContract.MessageEntry.TABLE_NAME
                    + " ADD COLUMN " + column);
            }

        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v53_to_v54(SQLiteDatabase db) {
        try {
            db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.BusinessEntry.TABLE_NAME + "("
                + DBContract.BusinessEntry._ID + " TEXT PRIMARY KEY, "
                + DBContract.BusinessEntry.COLUMN_BUSINESS_CODE + " TEXT DEFAULT 'Ecp.Task', "

                + DBContract.BusinessEntry.COLUMN_NAME + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_END_TIME + " TEXT, "
                + DBContract.BusinessEntry.COLUMN_END_TIMESTAMP + " INTEGER, "

                + DBContract.BusinessEntry.COLUMN_PRIMARY_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_PRIMARY_NAME + " TEXT DEFAULT '', "

                + DBContract.BusinessEntry.COLUMN_MANAGER_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_MANAGER_NAME + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_MANAGER_AVATAR_ID + " TEXT DEFAULT '', "

                + DBContract.BusinessEntry.COLUMN_EXECUTOR_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_EXECUTOR_NAME + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_EXECUTOR_AVATAR_ID + " TEXT DEFAULT '', "

                + DBContract.BusinessEntry.COLUMN_CUSTOMER_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_CUSTOMER_NAME + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_CUSTOMER_AVATAR_ID + " TEXT DEFAULT '', "
                + DBContract.BusinessEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y', "
                + DBContract.BusinessEntry.COLUMN_DESCRIPTION + " TEXT DEFAULT '' );"
            );
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v54_to_v55(SQLiteDatabase db) {

        // statistical data
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.StatisticsEntry.TABLE_NAME + "("
            + DBContract.StatisticsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
            + DBContract.StatisticsEntry.COLUMN_RELATION_ID + " TEXT, "
            + DBContract.StatisticsEntry.COLUMN_ASCRIPTION + " TEXT DEFAULT '', "
            + DBContract.StatisticsEntry.COLUMN_ORIGINAL_CONTENT + " TEXT , "
            + DBContract.StatisticsEntry.COLUMN_TOTAL_ROW + " INTEGER, "
            + DBContract.StatisticsEntry.COLUMN_ROW_COUNT + " INTEGER, "
            + DBContract.StatisticsEntry.COLUMN_STAT_TYPE + " TEXT, "
            + DBContract.StatisticsEntry.COLUMN_START_TIME + " INTEGER, "
            + DBContract.StatisticsEntry.COLUMN_END_TIME + " INTEGER, "
            + DBContract.StatisticsEntry.COLUMN_UPDATE_TIME + " INTEGER );"
        );

        // statistical data index
        db.execSQL("CREATE INDEX " + DBContract.StatisticsEntryIndex.IDX_COLUMN
            + " ON " + DBContract.StatisticsEntry.TABLE_NAME + "( "
            + DBContract.StatisticsEntry.COLUMN_RELATION_ID + ","
            + DBContract.StatisticsEntry.COLUMN_ASCRIPTION + ","
            + DBContract.StatisticsEntry.COLUMN_ROW_COUNT + ","
            + DBContract.StatisticsEntry.COLUMN_STAT_TYPE
            + " );"
        );

        // Sticker pack information
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.StickerPackageEntry.TABLE_NAME + "("
            + DBContract.StickerPackageEntry._ID + " TEXT PRIMARY KEY, "
            + DBContract.StickerPackageEntry.COLUMN_PACKAGE_NAME + " TEXT, "
            + DBContract.StickerPackageEntry.COLUMN_ICON_ID + " TEXT, "
            + DBContract.StickerPackageEntry.COLUMN_ICON_URL + " TEXT, "
            + DBContract.StickerPackageEntry.COLUMN_JOIN_TIME + " INTEGER, "
            + DBContract.StickerPackageEntry.COLUMN_ITEM_COUNT + " INTEGER, "
            + DBContract.StickerPackageEntry.COLUMN_EMOTICON_TYPE + " TEXT DEFAULT 'STICKER', "
            + DBContract.StickerPackageEntry.COLUMN_ITEM_LINE + " INTEGER, "
            + DBContract.StickerPackageEntry.COLUMN_ITEM_ROW + " INTEGER, "
            + DBContract.StickerPackageEntry.COLUMN_SELECT_ACTION + " TEXT DEFAULT 'UNDEF', "
            + DBContract.StickerPackageEntry.COLUMN_UPDATE_TIME + " INTEGER, "
            + DBContract.StickerPackageEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' );"
        );

        // Sticker Information
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.StickerItemEntry.TABLE_NAME + "("
            + DBContract.StickerItemEntry._ID + " TEXT PRIMARY KEY, "
            + DBContract.StickerItemEntry.COLUMN_NAME + " TEXT, "
            + DBContract.StickerItemEntry.COLUMN_DISPLAY_NAME + " TEXT, "
            + DBContract.StickerItemEntry.COLUMN_SORT_INDEX + " INTEGER, "
            + DBContract.StickerItemEntry.COLUMN_KEYWORDS + " TEXT, "
            + DBContract.StickerItemEntry.COLUMN_PICTURE_ID + " TEXT, "
            + DBContract.StickerItemEntry.COLUMN_PICTURE_URL + " TEXT, "
            + DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_ID + " TEXT, "
            + DBContract.StickerItemEntry.COLUMN_THUMBNAIL_PICTURE_URL + " TEXT, "
            + DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + " TEXT, "
            + DBContract.StickerItemEntry.COLUMN_UPDATE_TIME + " INTEGER, "
            + DBContract.StickerItemEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y' );"
        );

        // Sticker Information index
        db.execSQL("CREATE INDEX " + DBContract.StickerItemEntryIndex.IDX_COLUMN
            + " ON " + DBContract.StickerItemEntry.TABLE_NAME + "( "
            + DBContract.StickerItemEntry.COLUMN_STICKER_PACKAGE_ID + ","
            + DBContract.StickerItemEntry.COLUMN_KEYWORDS
            + " );"
        );
    }

    protected void v55_to_v56(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
            + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID + " TEXT DEFAULT '' ");

        db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
            + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + " TEXT DEFAULT '' ");
    }

    protected void v56_to_v57(SQLiteDatabase db) {
        // entity service number agent rel table
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.ServiceNumberAgentRel.TABLE_NAME + "("
            + DBContract.ServiceNumberAgentRel._ID + " TEXT PRIMARY KEY, "
            + DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + " TEXT, "
            + DBContract.ServiceNumberAgentRel.COLUMN_BROADCAST_ROOM_ID + " TEXT, "
            + DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID + " TEXT, "
            + DBContract.ServiceNumberAgentRel.COLUMN_AGENT_PRIVILEGE + " TEXT DEFAULT 'UNDEF');"
        );

        // entity service number agent rel index
        db.execSQL("CREATE INDEX " + DBContract.ServiceNumberAgentRelIndex.IDX_COLUMN
            + " ON " + DBContract.ServiceNumberAgentRel.TABLE_NAME + "( "
            + DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + ","
            + DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID
            + " );"
        );
    }

    protected void v57_to_v58(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.ChatRoomEntry.TABLE_NAME);
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.ChatRoomEntry.TABLE_NAME + "( "
            + DBContract.ChatRoomEntry._ID + " TEXT PRIMARY KEY, "
            + DBContract.ChatRoomEntry.COLUMN_TITLE + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_TYPE + " TEXT, "
            + DBContract.ChatRoomEntry.COLUMN_AVATAR_ID + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_OWNER_ID + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_UNREAD_NUMBER + " INTEGER DEFAULT 0, "

            + DBContract.ChatRoomEntry.COLUMN_IS_TOP + " TEXT DEFAULT 'N', "
            + DBContract.ChatRoomEntry.COLUMN_TOP_TIME + " LONG, "
            + DBContract.ChatRoomEntry.COLUMN_IS_CUSTOM_NAME + " TEXT DEFAULT 'N', "
            + DBContract.ChatRoomEntry.COLUMN_IS_MUTE + " TEXT DEFAULT 'N', "

            + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_ID + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_NAME + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_AVATAR_ID + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_TYPE + " TEXT DEFAULT 'NONE', "
            + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_AGENT_ID + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_ID + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_STATUS + " TEXT DEFAULT 'OFF_LINE', "

            + DBContract.ChatRoomEntry.COLUMN_BUSINESS_ID + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_BUSINESS_EXECUTOR_ID + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_BUSINESS_NAME + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_BUSINESS_CODE + " TEXT DEFAULT 'UNDEF', "

            + DBContract.ChatRoomEntry.COLUMN_LAST_MESSAGE_ENTITY + " TEXT DEFAULT '{}', "

            + DBContract.ChatRoomEntry.COLUMN_UPDATE_TIME + " LONG, "
            + DBContract.ChatRoomEntry.COLUMN_UNFINISHED_EDITED + " TEXT DEFAULT '', "
            + DBContract.ChatRoomEntry.COLUMN_UNFINISHED_EDITED_TIME + " LONG , "
            + DBContract.ChatRoomEntry.COLUMN_LIST_CLASSIFY + " TEXT DEFAULT 'ALL', "
            + DBContract.ChatRoomEntry.COLUMN_SORT_WEIGHTS + " INTEGER DEFAULT 0 ); "
        );


        db.execSQL("CREATE INDEX " + DBContract.ChatRoomEntryIndex.IDX_COLUMN
            + " ON " + DBContract.ChatRoomEntry.TABLE_NAME + "( "
            + DBContract.ChatRoomEntry.COLUMN_TITLE + ","
            + DBContract.ChatRoomEntry.COLUMN_OWNER_ID + ", "
            + DBContract.ChatRoomEntry.COLUMN_TYPE
            + " );"
        );
    }

    protected void v59_to_v60(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
            + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_CONSULT_ROOM_ID + " TEXT DEFAULT '' ");
    }

    protected void v60_to_v61(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
            + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OPEN_TYPES + " TEXT DEFAULT '[]' ");
    }

    protected void v65_to_v66(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + DBContract.USER_INFO.TABLE_NAME, null)) {
            if (cursor.getColumnIndex(DBContract.USER_INFO.COLUMN_OPEN_ID) < 0) {
                db.execSQL("ALTER TABLE " + DBContract.USER_INFO.TABLE_NAME
                    + " ADD COLUMN " + DBContract.USER_INFO.COLUMN_OPEN_ID + " TEXT DEFAULT '' ");
            }
        }
    }

    protected void v66_to_v67(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + DBContract.USER_INFO.TABLE_NAME, null)) {
            if (cursor.getColumnIndex(DBContract.USER_INFO.COLUMN_MOOD) < 0) {
                db.execSQL("ALTER TABLE " + DBContract.USER_INFO.TABLE_NAME
                    + " ADD COLUMN " + DBContract.USER_INFO.COLUMN_MOOD + " TEXT DEFAULT '' ");
            }
        }
    }

    protected void v69_to_v70(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT * FROM " + DBContract.LabelEntry.TABLE_NAME, null)) {
            if (cursor.getColumnIndex(DBContract.LabelEntry.COLUMN_USER_IDS) < 0) {
                db.execSQL("ALTER TABLE " + DBContract.LabelEntry.TABLE_NAME
                    + " ADD COLUMN " + DBContract.LabelEntry.COLUMN_USER_IDS + " TEXT DEFAULT '' ");
            }
            if (cursor.getColumnIndex(DBContract.LabelEntry.COLUMN_CREATE_TIME) < 0) {
                db.execSQL("ALTER TABLE " + DBContract.LabelEntry.TABLE_NAME
                    + " ADD COLUMN " + DBContract.LabelEntry.COLUMN_CREATE_TIME + " TEXT DEFAULT '' ");
            }
            if (cursor.getColumnIndex(DBContract.LabelEntry.COLUMN_OWNER_ID) < 0) {
                db.execSQL("ALTER TABLE " + DBContract.LabelEntry.TABLE_NAME
                    + " ADD COLUMN " + DBContract.LabelEntry.COLUMN_OWNER_ID + " TEXT DEFAULT '' ");
            }
            if (cursor.getColumnIndex(DBContract.LabelEntry.COLUMN_DELETED) < 0) {
                db.execSQL("ALTER TABLE " + DBContract.LabelEntry.TABLE_NAME
                    + " ADD COLUMN " + DBContract.LabelEntry.COLUMN_DELETED + " TEXT DEFAULT '' ");
            }
        }
    }

    protected void v70_to_v71(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.API_INFO.TABLE_NAME);
        createApiInfo(db);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.TodoEntry.TABLE_NAME);
        createTodoTable(db);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.ServiceNumEntry.TABLE_NAME);
        createServiceNumberTable(db);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.UserProfileEntry.TABLE_NAME);
        createUsersTable(db);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.BossServiceNumberContactEntry.TABLE_NAME);
        createBossServiceNumberContactTable(db);
    }

    protected void v71_to_v73(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.API_INFO.TABLE_NAME
                + " ADD COLUMN " + DBContract.API_INFO.COLUMN_USER_ID + " TEXT  DEFAULT '' ");
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_AI_SERVICE_WARNED + " TEXT  DEFAULT 'N' ");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v73_to_v75(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_DELETED + " TEXT  DEFAULT 'N' ");
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_PROVISIONAL_IDS + " TEXT  DEFAULT '[]' ");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v75_to_v76(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_OWNER_USER_TYPE + " TEXT DEFAULT '' ");
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_SERVICE_NUMBER_OWNER_STOP + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_MEMBER_IDS + " TEXT  DEFAULT '[]' ");
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_TRANSFER_FLAG + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_TRANSFER_REASON + " TEXT DEFAULT '' ");
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_LAST_END_SERVICE_TIME + " LONG DEFAULT 0");
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_INTERACTION_TIME + " LONG DEFAULT 0");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v76_to_v77(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_CHAT_ROOM_MEMBER + " TEXT DEFAULT '' ");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v77_to_v78(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.BossServiceNumberContactEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + DBContract.BossServiceNumberContactEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.BossServiceNumberContactEntry.STATUS + " TEXT DEFAULT '' ");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v78_to_v79(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_DFR_TIME + " LONG DEFAULT 0");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v79_to_v80(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_IS_AT_ME + " INTEGER DEFAULT 0");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v80_to_v81(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.MessageEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.MessageEntry.COLUMN_TAG + " TEXT DEFAULT ''");
            db.execSQL("ALTER TABLE " + DBContract.MessageEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.MessageEntry.COLUMN_IS_FACEBOOK_PRIVATE_REPLIED + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + DBContract.MessageEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.MessageEntry.COLUMN_FACEBOOK_COMMENT_STATUS + " TEXT DEFAULT ''");
            db.execSQL("ALTER TABLE " + DBContract.MessageEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.MessageEntry.COLUMN_FACEBOOK_POST_STATUS + " TEXT DEFAULT ''");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v81_to_v82(SQLiteDatabase db) {
        try {
            createLastMessageTable(db);
            createChatMemberTable(db);
            createChatRoomMemberIdsTable(db);
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v82_to_v83(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.ChatRoomEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ChatRoomEntry.COLUMN_LAST_SEQUENCE + " INTEGER DEFAULT 0 ");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    protected void v83_to_v84(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + DBContract.ServiceNumEntry.TABLE_NAME
                + " ADD COLUMN " + DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME + " LONG DEFAULT 0 ");
        } catch (Exception e) {
            CELog.e(e.getMessage());
        }
    }

    private void createApiInfo(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBContract.API_INFO.TABLE_NAME + "( "
            + DBContract.API_INFO.COLUMN_SOURCE + " TEXT  PRIMARY KEY, "
            + DBContract.API_INFO.COLUMN_USER_ID + " TEXT, "
            + DBContract.API_INFO.COLUMN_LAST_REFRESH_TIME + " LONG DEFAULT 0 ); "
        );
    }

    private void createBossServiceNumberContactTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBContract.BossServiceNumberContactEntry.TABLE_NAME + "("
            + DBContract.BossServiceNumberContactEntry.ID + " TEXT PRIMARY KEY, "
            + DBContract.BossServiceNumberContactEntry.NAME + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.NICKNAME + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.CUSTOMER_NAME + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.CUSTOMER_DESCRIPTION + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.CUSTOMER_BUSINESS_CARD_URL + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.AVATAR_ID + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.IS_ADDRESS_BOOK + " INTEGER, "
            + DBContract.BossServiceNumberContactEntry.IS_MOBILE + " INTEGER, "
            + DBContract.BossServiceNumberContactEntry.USER_TYPE + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.ROOM_ID + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.OPEN_ID + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.UPDATE_TIME + " LONG, "
            + DBContract.BossServiceNumberContactEntry.SCOPE_ARRAY + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.SERVICE_NUMBER_IDS + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.SCOPE_INFOS + " TEXT, "
            + DBContract.BossServiceNumberContactEntry.MOBILE_VISIBLE + " INTEGER, "
            + DBContract.BossServiceNumberContactEntry.STATUS + " TEXT );");
    }

    private void createServiceNumberTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBContract.ServiceNumEntry.TABLE_NAME + "("
            + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_ID + " TEXT PRIMARY KEY,"
            + DBContract.ServiceNumEntry.COLUMN_SERVICE_NUMBER_TYPE + " TEXT,"
            + DBContract.ServiceNumEntry.COLUMN_DESCRIPTION + " INTEGER ,"
            + DBContract.ServiceNumEntry.COLUMN_ROOM_ID + " TEXT, "
            + DBContract.ServiceNumEntry.COLUMN_SUBSCRIBE + " TEXT, "
            + DBContract.ServiceNumEntry.COLUMN_NAME + " TEXT, "
            + DBContract.ServiceNumEntry.COLUMN_AVATAR_URL + " TEXT, "
            + DBContract.ServiceNumEntry.COLUMN_BROADCAST_ROOM_ID + " TEXT DEFAULT '',"
            + DBContract.ServiceNumEntry.COLUMN_OWNER_ID + " TEXT DEFAULT '',"
            + DBContract.ServiceNumEntry.COLUMN_FIRST_WELCOME_MESSAGE + " TEXT DEFAULT '',"
            + DBContract.ServiceNumEntry.COLUMN_EACH_WELCOME_MESSAGE + " TEXT DEFAULT '',"
            + DBContract.ServiceNumEntry.COLUMN_INTERVAL_WELCOME_MESSAGE + " TEXT DEFAULT '',"
            + DBContract.ServiceNumEntry.COLUMN_IS_ENABLE + " TEXT DEFAULT 'Y',"
            + DBContract.ServiceNumEntry.COLUMN_IS_OWNER + " TEXT DEFAULT 'false',"
            + DBContract.ServiceNumEntry.COLUMN_IS_MANAGER + " TEXT DEFAULT 'false',"
            + DBContract.ServiceNumEntry.COLUMN_IS_COMMON + " TEXT DEFAULT 'false',"
            + DBContract.ServiceNumEntry.COLUMN_IS_IN_SITE_SERVICE + " TEXT DEFAULT 'N',"
            + DBContract.ServiceNumEntry.COLUMN_IS_OUT_SITE_SERVICE + " TEXT DEFAULT 'N',"
            + DBContract.ServiceNumEntry.COLUMN_INTERNAL_SUBSCRIBE_COUNT + " INTEGER,"
            + DBContract.ServiceNumEntry.COLUMN_EXTERNAL_SUBSCRIBE_COUNT + " INTEGER,"
            + DBContract.ServiceNumEntry.COLUMN_SERVICE_IDEL_TIME + " INTEGER,"
            + DBContract.ServiceNumEntry.COLUMN_SERVICE_TIMEOUT_TIME + " INTEGER,"
            + DBContract.ServiceNumEntry.COLUMN_STATUS + " TEXT,"
            + DBContract.ServiceNumEntry.COLUMN_SERVICE_OPEN_TYPE + " TEXT,"
            + DBContract.ServiceNumEntry.COLUMN_ROBOT_SERVICE_FLAG + " TEXT,"
            + DBContract.ServiceNumEntry.COLUMN_ROBOT_ID + " TEXT,"
            + DBContract.ServiceNumEntry.COLUMN_ROBOT_NAME + " TEXT,"
            + DBContract.ServiceNumEntry.COLUMN_MEMBER_ITEMS + " TEXT,"
            + DBContract.ServiceNumEntry.COLUMN_SERVICE_MEMBER_ROOM_ID + " TEXT DEFAULT '',"
            + DBContract.ServiceNumEntry.COLUMN_UPDATE_TIME + " LONG DEFAULT 0 );");
    }

    private void createTodoTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBContract.TodoEntry.TABLE_NAME + "("
            + DBContract.TodoEntry._ID + " TEXT PRIMARY KEY, "
            + DBContract.TodoEntry.COLUMN_TITLE + " TEXT DEFAULT '', "
            + DBContract.TodoEntry.COLUMN_ROOM_ID + " TEXT DEFAULT '', "
            + DBContract.TodoEntry.COLUMN_MESSAGE_ID + " TEXT DEFAULT '', "
            + DBContract.TodoEntry.COLUMN_USER_ID + " TEXT DEFAULT '', "
            + DBContract.TodoEntry.COLUMN_TODO_STATUS + " TEXT, "
            + DBContract.TodoEntry.COLUMN_PUBLIC_TYPE + " TEXT, "
            + DBContract.TodoEntry.COLUMN_REMIND_TIME + " INTEGER, "
            + DBContract.TodoEntry.COLUMN_CREATE_TIME + " INTEGER, "
            + DBContract.TodoEntry.COLUMN_UPDATE_TIME + " INTEGER, "
            + DBContract.TodoEntry.COLUMN_PROCESS_STATUS + " TEXT DEFAULT 'UNDEF', "
            + DBContract.TodoEntry.COLUMN_OPEN_CLOCK + " TEXT DEFAULT 'N');"
        );
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBContract.UserProfileEntry.TABLE_NAME + "("
            + DBContract.UserProfileEntry.COLUMN_ID + " TEXT PRIMARY KEY , "
            + DBContract.UserProfileEntry.COLUMN_NICKNAME + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_NAME + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_AVATAR_URL + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_USER_TYPE + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_EXTENSION + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_DUTY + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_DEPARTMENT + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_OPEN_ID + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_MOOD + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_SERVICE_NUMBER_IDS + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_SCOPE_ARRAY + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_STATUS + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_CUSTOMER_NAME + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_CUSTOMER_DESCRIPTION + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_CUSTOMER_BUSINESS_CARD_URL + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_ALIAS + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_ROOM_ID + " TEXT, "
            + DBContract.UserProfileEntry.COLUMN_BLOCK + " INTEGER, "
            + DBContract.UserProfileEntry.COLUMN_COLLECTION + " INTEGER, "
            + DBContract.UserProfileEntry.COLUMN_RELATION + " INTEGER, "
            + DBContract.UserProfileEntry.COLUMN_OTHER_PHONE + " TEXT DEFAULT '', "
            + DBContract.UserProfileEntry.COLUMN_SIGNATURE + " TEXT );"
        );
    }

    private void createGroupTable(SQLiteDatabase db) {
        final String SQL_CREATE_GROUP_TABLE = "CREATE TABLE IF NOT EXISTS " + DBContract.SyncGroupEntry.TABLE_NAME + "("
            + DBContract.SyncGroupEntry._ID + " TEXT PRIMARY KEY, "
            + DBContract.SyncGroupEntry.COLUMN_LAST_READ_SEQUENCE + " INTEGER, "
            + DBContract.SyncGroupEntry.COLUMN_LAST_RECEIVED_SEQUENCE + " INTEGER, "
            + DBContract.SyncGroupEntry.COLUMN_IS_CUSTOM_NAME + " TEXT DEFAULT 'N', "
            + DBContract.SyncGroupEntry.COLUMN_MEMBER_DELETED + " TEXT DEFAULT 'N', "
            + DBContract.SyncGroupEntry.COLUMN_UPDATE_TIME + " LONG, "
            + DBContract.SyncGroupEntry.COLUMN_TYPE + " TEXT DEFAULT '', "
            + DBContract.SyncGroupEntry.COLUMN_TOP_TIME + " LONG, "
            + DBContract.SyncGroupEntry.COLUMN_DELETED + " TEXT DEFAULT 'N', "
            + DBContract.SyncGroupEntry.COLUMN_AVATAR_URL + " TEXT DEFAULT '', "
            + DBContract.SyncGroupEntry.COLUMN_IS_TOP + " TEXT DEFAULT 'N', "
            + DBContract.SyncGroupEntry.COLUMN_DFR_TIME + " LONG, "
            + DBContract.SyncGroupEntry.COLUMN_NAME + " TEXT, "
            + DBContract.SyncGroupEntry.COLUMN_MEMBER_IDS + " TEXT);";
        db.execSQL(SQL_CREATE_GROUP_TABLE);
    }

    private void createLastMessageTable(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.LastMessageEntry.TABLE_NAME + "("
            + DBContract.LastMessageEntry.COLUMN_ROOM_ID + " TEXT DEFAULT '' PRIMARY KEY, "
            + DBContract.LastMessageEntry.COLUMN_ID + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_DEVICE_TYPE + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_FLAG + " INTEGER DEFAULT 0, "
            + DBContract.LastMessageEntry.COLUMN_RECEIVE_NUM + " INTEGER Default 0, "
            + DBContract.LastMessageEntry.COLUMN_CHAT_ID + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_MSG_SRC + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_TYPE + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_CONTENT + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_SEND_TIME + " LONG DEFAULT 0, "
            + DBContract.LastMessageEntry.COLUMN_SEQUENCE + " INTEGER DEFAULT 0, "
            + DBContract.LastMessageEntry.COLUMN_SENDER_ID + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_SENDER_NAME + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_SOURCE_TYPE + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_OS_TYPE + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_FROM + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_TAG + " TEXT DEFAULT '', "
            + DBContract.LastMessageEntry.COLUMN_SEND_NUM + " INTEGER DEFAULT 0, "
            + DBContract.LastMessageEntry.COLUMN_READED_NUM + " INTEGER DEFAULT 0);"
        );
    }

    private void createChatMemberTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + DBContract.ChatMemberEntry.TABLE_NAME + " ("
            + DBContract.ChatMemberEntry.COLUMN_ROOM_ID + " TEXT DEFAULT '', "
            + DBContract.ChatMemberEntry.COLUMN_FIRST_SEQUENCE + " INTEGER DEFAULT 0, "
            + DBContract.ChatMemberEntry.COLUMN_DELETED + " TEXT DEFAULT 'N', "
            + DBContract.ChatMemberEntry.COLUMN_SOURCE_TYPE + " TEXT DEFAULT '', "
            + DBContract.ChatMemberEntry.COLUMN_LAST_READ_SEQUENCE + " INTEGER DEFAULT 0, "
            + DBContract.ChatMemberEntry.COLUMN_JOIN_TIME + " LONG DEFAULT 0, "
            + DBContract.ChatMemberEntry.COLUMN_LAST_RECEIVED_SEQUENCE + " INTEGER DEFAULT 0, "
            + DBContract.ChatMemberEntry.COLUMN_UPDATE_TIME + " LONG DEFAULT 0, "
            + DBContract.ChatMemberEntry.COLUMN_TYPE + " TEXT DEFAULT '', "
            + DBContract.ChatMemberEntry.COLUMN_MEMBER_ID + " TEXT DEFAULT '', "
            + "UNIQUE(" + DBContract.ChatMemberEntry.COLUMN_ROOM_ID + ", "
            + DBContract.ChatMemberEntry.COLUMN_MEMBER_ID + ")"
            + ");"
        );
    }

    private void createChatRoomMemberIdsTable(SQLiteDatabase db) {
        db.execSQL(" CREATE TABLE IF NOT EXISTS " + DBContract.ChatRoomMemberIdsEntry.TABLE_NAME + "("
            + DBContract.ChatMemberEntry.COLUMN_ROOM_ID + " TEXT DEFAULT '', "
            + DBContract.ChatMemberEntry.COLUMN_MEMBER_ID + " TEXT DEFAULT '', "
            + "UNIQUE(" + DBContract.ChatMemberEntry.COLUMN_ROOM_ID + ", "
            + DBContract.ChatMemberEntry.COLUMN_MEMBER_ID + ")"
            + ");"
        );
    }
}
