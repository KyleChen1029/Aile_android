package tw.com.chainsea.ce.sdk.database;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * DBContract
 * Created by 90Chris on 2015/7/5.
 */
public class DBContract {

    public static final String CONTENT_AUTHORITY = "tw.com.chainsea.chat";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class ChatRoomEntry implements BaseColumns {
        public static final String TABLE_NAME = "chat_room";
        public static final String COLUMN_TITLE = "name";
        public static final String COLUMN_TYPE = "chat_room_type";
        public static final String COLUMN_AVATAR_ID = "avatar_id";
        public static final String COLUMN_OWNER_ID = "owner_id";
        public static final String COLUMN_UNREAD_NUMBER = "unread_num";
        public static final String COLUMN_IS_TOP = "is_top";
        public static final String COLUMN_TOP_TIME = "top_time";
        public static final String COLUMN_IS_CUSTOM_NAME = "is_custom_name";
        public static final String COLUMN_IS_MUTE = "is_mute";
        public static final String COLUMN_SERVICE_NUMBER_ID = "service_number_id";
        public static final String COLUMN_SERVICE_NUMBER_NAME = "service_number_name";
        public static final String COLUMN_SERVICE_NUMBER_AVATAR_ID = "service_number_avatar_id";
        public static final String COLUMN_SERVICE_NUMBER_TYPE = "service_number_type";
        public static final String COLUMN_SERVICE_NUMBER_AGENT_ID = "service_number_agent_id";
        public static final String COLUMN_SERVICE_NUMBER_OWNER_ID = "is_service_number_owner_id";
        public static final String COLUMN_SERVICE_NUMBER_STATUS = "service_number_status";
        public static final String COLUMN_BUSINESS_ID = "business_id";
        public static final String COLUMN_BUSINESS_EXECUTOR_ID = "business_executor_id";
        public static final String COLUMN_BUSINESS_NAME = "business_name";
        public static final String COLUMN_BUSINESS_CODE = "business_code";
        public static final String COLUMN_LAST_MESSAGE_ENTITY = "last_message_entity";
        public static final String COLUMN_UPDATE_TIME = "update_time";
        public static final String COLUMN_UNFINISHED_EDITED = "unfinished_edited";
        public static final String COLUMN_UNFINISHED_EDITED_TIME = "unfinished_edited_time";
        public static final String COLUMN_LIST_CLASSIFY = "list_classify";
        public static final String COLUMN_SORT_WEIGHTS = "sort_weights";
        public static final String COLUMN_CONSULT_ROOM_ID = "consult_room_id";
        public static final String COLUMN_SERVICE_NUMBER_OPEN_TYPES = "service_number_open_types";
        public static final String COLUMN_AI_SERVICE_WARNED = "warned";

        public static final String COLUMN_CHAT_ROOM_DELETED = "deleted";
        public static final String COLUMN_PROVISIONAL_IDS = "provisionalIds";

        public static final String COLUMN_SERVICE_NUMBER_OWNER_STOP = "serviceNumberOwnerStop";
        public static final String COLUMN_OWNER_USER_TYPE = "ownerUserType";

        public static final String COLUMN_CHAT_ROOM_MEMBER = "chatRoomMember";
        public static final String COLUMN_MEMBER_IDS = "memberIds";
        public static final String COLUMN_LAST_END_SERVICE_TIME = "lastEndServiceTime";
        public static final String COLUMN_INTERACTION_TIME = "interactionTime";
        public static final String COLUMN_TRANSFER_FLAG = "transferFlag";
        public static final String COLUMN_TRANSFER_REASON = "transferReason";
        public static final String COLUMN_DFR_TIME = "dfrTime";
        public static final String COLUMN_IS_AT_ME = "isAtMe";
        public static final String COLUMN_LAST_SEQUENCE = "lastSequence";

        public static Map<String, Integer> getIndex(Cursor cursor) {
            Map<String, Integer> index = Maps.newHashMap();
            index.put(_ID, cursor.getColumnIndex(_ID));
            index.put(COLUMN_TYPE, cursor.getColumnIndex(COLUMN_TYPE));
            index.put(COLUMN_AVATAR_ID, cursor.getColumnIndex(COLUMN_AVATAR_ID));
            index.put(COLUMN_SERVICE_NUMBER_AVATAR_ID, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_AVATAR_ID));
            index.put(COLUMN_TITLE, cursor.getColumnIndex(COLUMN_TITLE));
            index.put(COLUMN_UPDATE_TIME, cursor.getColumnIndex(COLUMN_UPDATE_TIME));
            index.put(COLUMN_UNREAD_NUMBER, cursor.getColumnIndex(COLUMN_UNREAD_NUMBER));
            index.put(COLUMN_IS_TOP, cursor.getColumnIndex(COLUMN_IS_TOP));
            index.put(COLUMN_TOP_TIME, cursor.getColumnIndex(COLUMN_TOP_TIME));
            index.put(COLUMN_IS_CUSTOM_NAME, cursor.getColumnIndex(COLUMN_IS_CUSTOM_NAME));
            index.put(COLUMN_OWNER_ID, cursor.getColumnIndex(COLUMN_OWNER_ID));
            index.put(COLUMN_SERVICE_NUMBER_NAME, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_NAME));
            index.put(COLUMN_SERVICE_NUMBER_ID, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_ID));
            index.put(COLUMN_BUSINESS_ID, cursor.getColumnIndex(COLUMN_BUSINESS_ID));
            index.put(COLUMN_BUSINESS_EXECUTOR_ID, cursor.getColumnIndex(COLUMN_BUSINESS_EXECUTOR_ID));
            index.put(COLUMN_BUSINESS_NAME, cursor.getColumnIndex(COLUMN_BUSINESS_NAME));
            index.put(COLUMN_BUSINESS_CODE, cursor.getColumnIndex(COLUMN_BUSINESS_CODE));
            index.put(COLUMN_UNFINISHED_EDITED, cursor.getColumnIndex(COLUMN_UNFINISHED_EDITED));
            index.put(COLUMN_UNFINISHED_EDITED_TIME, cursor.getColumnIndex(COLUMN_UNFINISHED_EDITED_TIME));
            index.put(COLUMN_SERVICE_NUMBER_TYPE, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_TYPE));
            index.put(COLUMN_SERVICE_NUMBER_AGENT_ID, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_AGENT_ID));
            index.put(COLUMN_SERVICE_NUMBER_OWNER_ID, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_OWNER_ID));
            index.put(COLUMN_IS_MUTE, cursor.getColumnIndex(COLUMN_IS_MUTE));
            index.put(COLUMN_SERVICE_NUMBER_STATUS, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_STATUS));
            index.put(COLUMN_LIST_CLASSIFY, cursor.getColumnIndex(COLUMN_LIST_CLASSIFY));
            index.put(COLUMN_LAST_MESSAGE_ENTITY, cursor.getColumnIndex(COLUMN_LAST_MESSAGE_ENTITY));
            index.put(COLUMN_SORT_WEIGHTS, cursor.getColumnIndex(COLUMN_SORT_WEIGHTS));
            index.put(COLUMN_CONSULT_ROOM_ID, cursor.getColumnIndex(COLUMN_CONSULT_ROOM_ID));
            index.put(COLUMN_SERVICE_NUMBER_OPEN_TYPES, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_OPEN_TYPES));
            index.put(COLUMN_AI_SERVICE_WARNED, cursor.getColumnIndex(COLUMN_AI_SERVICE_WARNED));
            index.put(COLUMN_CHAT_ROOM_DELETED, cursor.getColumnIndex(COLUMN_CHAT_ROOM_DELETED));
            index.put(COLUMN_PROVISIONAL_IDS, cursor.getColumnIndex(COLUMN_PROVISIONAL_IDS));
            index.put(COLUMN_SERVICE_NUMBER_OWNER_STOP, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_OWNER_STOP));
            index.put(COLUMN_OWNER_USER_TYPE, cursor.getColumnIndex(COLUMN_OWNER_USER_TYPE));
            index.put(COLUMN_MEMBER_IDS, cursor.getColumnIndex(COLUMN_MEMBER_IDS));
            index.put(COLUMN_LAST_END_SERVICE_TIME, cursor.getColumnIndex(COLUMN_LAST_END_SERVICE_TIME));
            index.put(COLUMN_INTERACTION_TIME, cursor.getColumnIndex(COLUMN_INTERACTION_TIME));
            index.put(COLUMN_TRANSFER_FLAG, cursor.getColumnIndex(COLUMN_TRANSFER_FLAG));
            index.put(COLUMN_TRANSFER_REASON, cursor.getColumnIndex(COLUMN_TRANSFER_REASON));
            index.put(COLUMN_CHAT_ROOM_MEMBER, cursor.getColumnIndex(COLUMN_CHAT_ROOM_MEMBER));
            index.put(COLUMN_DFR_TIME, cursor.getColumnIndex(COLUMN_DFR_TIME));
            index.put(COLUMN_IS_AT_ME, cursor.getColumnIndex(COLUMN_IS_AT_ME));
            index.put(COLUMN_LAST_SEQUENCE, cursor.getColumnIndex(COLUMN_LAST_SEQUENCE));
            return index;
        }

        public static Map<String, Integer> getIndex() {
            Map<String, Integer> index = Maps.newHashMap();
            index.put(_ID, 0);
            index.put(COLUMN_TITLE, 1);
            index.put(COLUMN_TYPE, 2);
            index.put(COLUMN_AVATAR_ID, 3);
            index.put(COLUMN_OWNER_ID, 4);
            index.put(COLUMN_UNREAD_NUMBER, 5);
            index.put(COLUMN_IS_TOP, 6);
            index.put(COLUMN_TOP_TIME, 7);
            index.put(COLUMN_IS_CUSTOM_NAME, 8);
            index.put(COLUMN_IS_MUTE, 9);
            index.put(COLUMN_SERVICE_NUMBER_ID, 10);
            index.put(COLUMN_SERVICE_NUMBER_NAME, 11);
            index.put(COLUMN_SERVICE_NUMBER_AVATAR_ID, 12);
            index.put(COLUMN_SERVICE_NUMBER_TYPE, 13);
            index.put(COLUMN_SERVICE_NUMBER_AGENT_ID, 14);
            index.put(COLUMN_SERVICE_NUMBER_OWNER_ID, 15);
            index.put(COLUMN_SERVICE_NUMBER_STATUS, 16);
            index.put(COLUMN_BUSINESS_ID, 17);
            index.put(COLUMN_BUSINESS_EXECUTOR_ID, 18);
            index.put(COLUMN_BUSINESS_NAME, 19);
            index.put(COLUMN_BUSINESS_CODE, 20);
            index.put(COLUMN_LAST_MESSAGE_ENTITY, 21);
            index.put(COLUMN_UPDATE_TIME, 22);
            index.put(COLUMN_UNFINISHED_EDITED, 23);
            index.put(COLUMN_UNFINISHED_EDITED_TIME, 24);
            index.put(COLUMN_LIST_CLASSIFY, 25);
            index.put(COLUMN_SORT_WEIGHTS, 26);
            index.put(COLUMN_CONSULT_ROOM_ID, 27);
            index.put(COLUMN_SERVICE_NUMBER_OPEN_TYPES, 28);
            index.put(COLUMN_AI_SERVICE_WARNED, 29);
            index.put(COLUMN_CHAT_ROOM_DELETED, 30);
            index.put(COLUMN_PROVISIONAL_IDS, 31);
            index.put(COLUMN_SERVICE_NUMBER_OWNER_STOP, 32);
            index.put(COLUMN_OWNER_USER_TYPE, 33);
            index.put(COLUMN_MEMBER_IDS, 34);
            index.put(COLUMN_LAST_END_SERVICE_TIME, 35);
            index.put(COLUMN_INTERACTION_TIME, 36);
            index.put(COLUMN_TRANSFER_FLAG, 37);
            index.put(COLUMN_TRANSFER_REASON, 38);
            index.put(COLUMN_CHAT_ROOM_MEMBER, 39);
            index.put(COLUMN_DFR_TIME, 40);
            index.put(COLUMN_IS_AT_ME, 41);
            index.put(COLUMN_LAST_SEQUENCE, 42);
            return index;
        }
    }

    public static final class ChatRoomEntryIndex {
        public static final String IDX_COLUMN = "idx_chat_room";
    }

    public static final String PATH_MESSAGE = "chat_message";

    public static final class MessageEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGE).build();
        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
        // table
        public static final String TABLE_NAME = "chat_message";
        public static final String COLUMN_ROOM_ID = "room_id";
        // v.50
        public static final String COLUMN_PREVIOUS_MESSAGE_ID = "previous_message_id";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_SEND_TIME = "send_time";
        public static final String COLUMN_SENDER_ID = "sender_id";
        public static final String COLUMN_SENDER_NAME = "sender_name";
        public static final String COLUMN_AVATAR_ID = "avatar_id";
        public static final String COLUMN_FLAG = "flag";
        public static final String COLUMN_FROM = "channel";
        public static final String COLUMN_SEND_NUM = "send_num";
        public static final String COLUMN_RECEIVED_NUM = "received_num";
        public static final String COLUMN_READED_NUM = "readed_num";
        public static final String COLUMN_SOURCE_TYPE = "sourceType";
        // message sequence
        public static final String COLUMN_SEQUENCE = "sequence";
        // device
        public static final String COLUMN_OS_TYPE = "os_type";
        public static final String COLUMN_DEVICE_TYPE = "device_type";
        // theme
        public static final String COLUMN_THEME_ID = "theme_id";
        public static final String COLUMN_NEAR_MESSAGE_ID = "near_message_id";
        public static final String COLUMN_NEAR_MESSAGE_SEND_ID = "near_message_send_id";
        public static final String COLUMN_NEAR_MESSAGE_SEND_NAME = "near_message_send_name";
        public static final String COLUMN_NEAR_MESSAGE_AVATAR_ID = "near_message_avatar_id";
        public static final String COLUMN_NEAR_MESSAGE_CONTENT = "near_message_content";
        public static final String COLUMN_NEAR_MESSAGE_TYPE = "near_message_type";
        // local
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_IS_ENABLE = "is_enable";
        // Broadcast
        public static final String COLUMN_CREATE_USER_ID = "create_user_id";
        public static final String COLUMN_UPDATE_USER_ID = "update_user_id";
        public static final String COLUMN_BROADCAST_TIME = "broadcast_time";
        public static final String COLUMN_BROADCAST_FLAG = "broadcast_flag";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_IS_FACEBOOK_PRIVATE_REPLIED = "isFacebookPrivateReplied";
        public static final String COLUMN_FACEBOOK_COMMENT_STATUS = "facebookCommentStatus";
        public static final String COLUMN_FACEBOOK_POST_STATUS = "facebookPostStatus";

        public static Map<String, Integer> getIndex(Cursor cursor) {
            Map<String, Integer> index = Maps.newHashMap();
            index.put(_ID, cursor.getColumnIndex(_ID));
            index.put(COLUMN_ROOM_ID, cursor.getColumnIndex(COLUMN_ROOM_ID));
            index.put(COLUMN_PREVIOUS_MESSAGE_ID, cursor.getColumnIndex(COLUMN_PREVIOUS_MESSAGE_ID));
            index.put(COLUMN_TYPE, cursor.getColumnIndex(COLUMN_TYPE));
            index.put(COLUMN_CONTENT, cursor.getColumnIndex(COLUMN_CONTENT));
            index.put(COLUMN_SEND_TIME, cursor.getColumnIndex(COLUMN_SEND_TIME));
            index.put(COLUMN_SENDER_ID, cursor.getColumnIndex(COLUMN_SENDER_ID));
            index.put(COLUMN_SENDER_NAME, cursor.getColumnIndex(COLUMN_SENDER_NAME));
            index.put(COLUMN_AVATAR_ID, cursor.getColumnIndex(COLUMN_AVATAR_ID));
            index.put(COLUMN_FLAG, cursor.getColumnIndex(COLUMN_FLAG));
            index.put(COLUMN_FROM, cursor.getColumnIndex(COLUMN_FROM));
            index.put(COLUMN_SEND_NUM, cursor.getColumnIndex(COLUMN_SEND_NUM));
            index.put(COLUMN_RECEIVED_NUM, cursor.getColumnIndex(COLUMN_RECEIVED_NUM));
            index.put(COLUMN_READED_NUM, cursor.getColumnIndex(COLUMN_READED_NUM));
            index.put(COLUMN_SOURCE_TYPE, cursor.getColumnIndex(COLUMN_SOURCE_TYPE));
            index.put(COLUMN_SEQUENCE, cursor.getColumnIndex(COLUMN_SEQUENCE));
            index.put(COLUMN_OS_TYPE, cursor.getColumnIndex(COLUMN_OS_TYPE));
            index.put(COLUMN_DEVICE_TYPE, cursor.getColumnIndex(COLUMN_DEVICE_TYPE));
            index.put(COLUMN_THEME_ID, cursor.getColumnIndex(COLUMN_THEME_ID));
            index.put(COLUMN_NEAR_MESSAGE_ID, cursor.getColumnIndex(COLUMN_NEAR_MESSAGE_ID));
            index.put(COLUMN_NEAR_MESSAGE_SEND_ID, cursor.getColumnIndex(COLUMN_NEAR_MESSAGE_SEND_ID));
            index.put(COLUMN_NEAR_MESSAGE_SEND_NAME, cursor.getColumnIndex(COLUMN_NEAR_MESSAGE_SEND_NAME));
            index.put(COLUMN_NEAR_MESSAGE_AVATAR_ID, cursor.getColumnIndex(COLUMN_NEAR_MESSAGE_AVATAR_ID));
            index.put(COLUMN_NEAR_MESSAGE_CONTENT, cursor.getColumnIndex(COLUMN_NEAR_MESSAGE_CONTENT));
            index.put(COLUMN_NEAR_MESSAGE_TYPE, cursor.getColumnIndex(COLUMN_NEAR_MESSAGE_TYPE));
            index.put(COLUMN_STATUS, cursor.getColumnIndex(COLUMN_STATUS));
            index.put(COLUMN_IS_ENABLE, cursor.getColumnIndex(COLUMN_IS_ENABLE));
            index.put(COLUMN_CREATE_USER_ID, cursor.getColumnIndex(COLUMN_CREATE_USER_ID));
            index.put(COLUMN_UPDATE_USER_ID, cursor.getColumnIndex(COLUMN_UPDATE_USER_ID));
            index.put(COLUMN_BROADCAST_TIME, cursor.getColumnIndex(COLUMN_BROADCAST_TIME));
            index.put(COLUMN_BROADCAST_FLAG, cursor.getColumnIndex(COLUMN_BROADCAST_FLAG));
            index.put(COLUMN_TAG, cursor.getColumnIndex(COLUMN_TAG));
            index.put(COLUMN_IS_FACEBOOK_PRIVATE_REPLIED, cursor.getColumnIndex(COLUMN_IS_FACEBOOK_PRIVATE_REPLIED));
            index.put(COLUMN_FACEBOOK_COMMENT_STATUS, cursor.getColumnIndex(COLUMN_FACEBOOK_COMMENT_STATUS));
            index.put(COLUMN_FACEBOOK_POST_STATUS, cursor.getColumnIndex(COLUMN_FACEBOOK_POST_STATUS));
            // 額外
            index.put(DBContract.UserProfileEntry.COLUMN_ALIAS, cursor.getColumnIndex(DBContract.UserProfileEntry.COLUMN_ALIAS));
            return index;
        }
    }

    public static final class LastMessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "chat_room_last_message";
        public static final String COLUMN_ROOM_ID = "roomId";
        public static final String COLUMN_DEVICE_TYPE= "deviceType";
        public static final String COLUMN_FLAG = "flag";
        public static final String COLUMN_RECEIVE_NUM = "receiveNum";
        public static final String COLUMN_CHAT_ID = "chatId";
        public static final String COLUMN_MSG_SRC = "msgSrc";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_SEND_TIME = "sendTime";
        public static final String COLUMN_SEQUENCE = "sequence";
        public static final String COLUMN_SENDER_ID = "senderId";
        public static final String COLUMN_SENDER_NAME = "senderName";
        public static final String COLUMN_SOURCE_TYPE = "sourceType";
        public static final String COLUMN_OS_TYPE = "osType";
        public static final String COLUMN_FROM = "channel";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_TAG = "tag";
        public static final String COLUMN_SEND_NUM = "sendNum";
        public static final String COLUMN_READED_NUM = "readedNum";
        public static Map<String, Integer> getIndex(Cursor cursor) {
            Map<String, Integer> index = Maps.newHashMap();
            index.put(_ID, cursor.getColumnIndex(_ID));
            index.put(COLUMN_ROOM_ID, cursor.getColumnIndex(COLUMN_ROOM_ID));
            index.put(COLUMN_DEVICE_TYPE, cursor.getColumnIndex(COLUMN_DEVICE_TYPE));
            index.put(COLUMN_FLAG, cursor.getColumnIndex(COLUMN_FLAG));
            index.put(COLUMN_RECEIVE_NUM, cursor.getColumnIndex(COLUMN_RECEIVE_NUM));
            index.put(COLUMN_CHAT_ID, cursor.getColumnIndex(COLUMN_CHAT_ID));
            index.put(COLUMN_MSG_SRC, cursor.getColumnIndex(COLUMN_MSG_SRC));
            index.put(COLUMN_TYPE, cursor.getColumnIndex(COLUMN_TYPE));
            index.put(COLUMN_CONTENT, cursor.getColumnIndex(COLUMN_CONTENT));
            index.put(COLUMN_SEND_TIME, cursor.getColumnIndex(COLUMN_SEND_TIME));
            index.put(COLUMN_SEQUENCE, cursor.getColumnIndex(COLUMN_SEQUENCE));
            index.put(COLUMN_SENDER_ID, cursor.getColumnIndex(COLUMN_SENDER_ID));
            index.put(COLUMN_SENDER_NAME, cursor.getColumnIndex(COLUMN_SENDER_NAME));
            index.put(COLUMN_SOURCE_TYPE, cursor.getColumnIndex(COLUMN_SOURCE_TYPE));
            index.put(COLUMN_OS_TYPE, cursor.getColumnIndex(COLUMN_OS_TYPE));
            index.put(COLUMN_FROM, cursor.getColumnIndex(COLUMN_FROM));
            index.put(COLUMN_ID, cursor.getColumnIndex(COLUMN_ID));
            index.put(COLUMN_TAG, cursor.getColumnIndex(COLUMN_TAG));
            index.put(COLUMN_SEND_NUM, cursor.getColumnIndex(COLUMN_SEND_NUM));
            index.put(COLUMN_READED_NUM, cursor.getColumnIndex(COLUMN_READED_NUM));
            return index;
        }
    }

    public static final class ChatMemberEntry implements BaseColumns {
        public static final String TABLE_NAME = "chat_room_members";
        public static final String COLUMN_ROOM_ID = "roomId";
        public static final String COLUMN_FIRST_SEQUENCE= "firstSequence";
        public static final String COLUMN_DELETED = "deleted";
        public static final String COLUMN_SOURCE_TYPE = "sourceType";
        public static final String COLUMN_LAST_READ_SEQUENCE = "lastReadSequence";
        public static final String COLUMN_JOIN_TIME = "joinTime";
        public static final String COLUMN_LAST_RECEIVED_SEQUENCE = "lastReceivedSequence";
        public static final String COLUMN_UPDATE_TIME = "updateTime";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_MEMBER_ID = "memberId";
    }

    public static final class ChatRoomMemberIdsEntry implements BaseColumns {
        public static final String TABLE_NAME = "chat_room_memberIds";
        public static final String COLUMN_ROOM_ID = "roomId";
        public static final String COLUMN_MEMBER_ID = "memberId";
    }
    public static final class MessageEntryIndex {
        public static final String IDX_COLUMN = "idx_message";
    }

    public static final class AccountRoomRel implements BaseColumns {
        public static final String TABLE_NAME = "account_room_rel";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ACCOUNT_ID = "account_id";
        public static final String COLUMN_ROOM_ID = "room_id";

        public static Map<String, Integer> getIndex(Cursor cursor) {
            Map<String, Integer> index = Maps.newHashMap();
            index.put(COLUMN_ID, cursor.getColumnIndex(COLUMN_ID));
            index.put(COLUMN_ACCOUNT_ID, cursor.getColumnIndex(COLUMN_ACCOUNT_ID));
            index.put(COLUMN_ROOM_ID, cursor.getColumnIndex(COLUMN_ROOM_ID));
            return index;
        }
    }

    public static final class AccountRoomRelIndex {
        public static final String IDX_COLUMN = "idx_account_room_rel";
    }


    public static final class FriendsLabelRel implements BaseColumns {
        public static final String TABLE_NAME = "friends_label";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_ACCOUNT_ID = "friend_id";
        public static final String COLUMN_LABEL_ID = "label_id";
    }

    public static final class SEARCH_HISTORY implements BaseColumns {
        public static final String TABLE_NAME = "search";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TIME = "time";
    }

    public static final class UserProfileEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NICKNAME = "nickname";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AVATAR_URL = "avatar_url";
        public static final String COLUMN_USER_TYPE = "user_type";
        public static final String COLUMN_EXTENSION = "extension"; //
        public static final String COLUMN_DUTY = "duty";
        public static final String COLUMN_DEPARTMENT = "department";
        public static final String COLUMN_OPEN_ID = "openId";
        public static final String COLUMN_MOOD = "mood"; //
        public static final String COLUMN_SERVICE_NUMBER_IDS = "serviceNumberIds"; //
        public static final String COLUMN_SCOPE_ARRAY = "scopeArray"; //
        public static final String COLUMN_STATUS = "status"; //

        public static final String COLUMN_CUSTOMER_DESCRIPTION = "customerDescription";
        public static final String COLUMN_CUSTOMER_NAME = "customerName";
        public static final String COLUMN_CUSTOMER_BUSINESS_CARD_URL = "customerBusinessCardUrl";
        public static final String COLUMN_ALIAS = "alias";
        public static final String COLUMN_SIGNATURE = "signature";
        public static final String COLUMN_ROOM_ID = "roomId";
        public static final String COLUMN_RELATION = "relation";
        public static final String COLUMN_BLOCK = "block";
        public static final String COLUMN_COLLECTION = "collection";
        public static final String COLUMN_OTHER_PHONE = "other_phone";

        public static Map<String, Integer> getIndex(Cursor cursor) {
            Map<String, Integer> index = Maps.newHashMap();
            index.put(COLUMN_ID, cursor.getColumnIndex(COLUMN_ID));
            index.put(COLUMN_NICKNAME, cursor.getColumnIndex(COLUMN_NICKNAME));
            index.put(COLUMN_NAME, cursor.getColumnIndex(COLUMN_NAME));
            index.put(COLUMN_AVATAR_URL, cursor.getColumnIndex(COLUMN_AVATAR_URL));
            index.put(COLUMN_USER_TYPE, cursor.getColumnIndex(COLUMN_USER_TYPE));
            index.put(COLUMN_EXTENSION, cursor.getColumnIndex(COLUMN_EXTENSION));
            index.put(COLUMN_DUTY, cursor.getColumnIndex(COLUMN_DUTY));
            index.put(COLUMN_DEPARTMENT, cursor.getColumnIndex(COLUMN_DEPARTMENT));
            index.put(COLUMN_OPEN_ID, cursor.getColumnIndex(COLUMN_OPEN_ID));
            index.put(COLUMN_MOOD, cursor.getColumnIndex(COLUMN_MOOD));
            index.put(COLUMN_SERVICE_NUMBER_IDS, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_IDS));
            index.put(COLUMN_SCOPE_ARRAY, cursor.getColumnIndex(COLUMN_SCOPE_ARRAY));
            index.put(COLUMN_STATUS, cursor.getColumnIndex(COLUMN_STATUS));

            index.put(COLUMN_CUSTOMER_DESCRIPTION, cursor.getColumnIndex(COLUMN_CUSTOMER_DESCRIPTION));
            index.put(COLUMN_CUSTOMER_NAME, cursor.getColumnIndex(COLUMN_CUSTOMER_NAME));
            index.put(COLUMN_CUSTOMER_BUSINESS_CARD_URL, cursor.getColumnIndex(COLUMN_CUSTOMER_BUSINESS_CARD_URL));
            index.put(COLUMN_ALIAS, cursor.getColumnIndex(COLUMN_ALIAS));
            index.put(COLUMN_SIGNATURE, cursor.getColumnIndex(COLUMN_SIGNATURE));
            index.put(COLUMN_ROOM_ID, cursor.getColumnIndex(COLUMN_ROOM_ID));
            index.put(COLUMN_RELATION, cursor.getColumnIndex(COLUMN_RELATION));
            index.put(COLUMN_BLOCK, cursor.getColumnIndex(COLUMN_BLOCK));
            index.put(COLUMN_COLLECTION, cursor.getColumnIndex(COLUMN_COLLECTION));
            index.put(COLUMN_OTHER_PHONE, cursor.getColumnIndex(COLUMN_OTHER_PHONE));
            return index;
        }
    }

    public static final class USER_INFO implements BaseColumns {
        public static final String TABLE_NAME = "user_info";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_OTHER_PHONE = "other_phone";
        public static final String COLUMN_MOBILE = "mobile";
        public static final String COLUMN_MOOD = "mood";
        public static final String COLUMN_LOGIN_NAME = "login_name";
        public static final String COLUMN_BIRTHDAY = "birthday";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_OPEN_ID = "open_id";
    }

    public static final class API_INFO implements BaseColumns {
        public static final String TABLE_NAME = "api_info";
        public static final String COLUMN_SOURCE = "source";
        public static final String COLUMN_LAST_REFRESH_TIME = "last_Refresh_Time";
        public static final String COLUMN_USER_ID = "user_id";
    }

    public @interface REFRESH_TIME_SOURCE{
        String FIRST_LOAD = "first_load";
        String SELF_ALL = "member/self/all";
        String BUSINESS_LIST_ME = "business/list/me";
        String TODO_LIST = "todo/list";
        String EMPLOYEE_LIST = "employee/list";
        String ADDRESS_BOOK_SYNC = "addressbook/sync";
        String BOSSSERVICENUMBER_CONTACT_LIST = "bossservicenumber/contact/list";
        String ROOM_RECENT_V2 = "room/recent/v2";
        String SYNC_EMPLOYEE = "sync/employee";
        String SYNC_LABEL = "sync/label";
        String SYNC_TODO = "sync/todo";
        String SYNC_SERVICENUMBER = "sync/servicenumber";
        String SYNC_CONTACT = "sync/contact";
        String CHAT_ROOM_ROBOT_SERVICE_LIST = "chat/room/robot/service/list";
        String SYNC_ROOM_UNREAD = "sync/room/unread";
        String SYNC_ROOM_UNREAD_ONCE = "sync/room/unread_ONCE";

        String SYNC_SERVICE_NUMBER_ACTIVE_LIST = "/servicenumber/active/list";
        String SYNC_ROOM_NORMAL = "sync/room/normal";

        String CHAT_ROBOT_SERVICE_LIST = "chat/room/robotservice/list";

        String SYNC_ROOM_SERVICE_NUMBER= "sync/room/servicenumber";
        String SYNC_GROUP= "sync/group";
    }

    /**
     * 2023/05/03 建立用於機器人服務中的表單
     */
    public interface ChatRobotRoomEntry extends BaseColumns {
        String TABLE_NAME = "chat_robot_room_list";
        String avatarId = "avatarId";
        String serviceNumberId = "serviceNumberId";
        String updateTime = "updateTime";
        String ownerId = "ownerId";
        String serviceNumberOwnerId = "serviceNumberOwnerId";
        String type = "type";
        String deleted = "deleted";
        String unReadNum = "unReadNum";
        String roomMemberIdentity = "roomMemberIdentity";
        String id = "id";
        String memberIds = "memberIds";
        String serviceNumberType = "serviceNumberType";
        String serviceNumberStatus = "serviceNumberStatus";
        String serviceNumberAvatarId = "serviceNumberAvatarId";
        String name = "name";
        String lastEndServiceTime = "lastEndServiceTime";
        String serviceNumberOpenType = "serviceNumberOpenType";
        String lastMessage = "lastMessage";
        String isMute = "isMute";
        String serviceNumberName = "serviceNumberName";
    }

    public interface BossServiceNumberContactEntry extends BaseColumns{
        String TABLE_NAME = "contact_boss_service_number_customer";
        String ID = "id";
        String NAME = "name";
        String NICKNAME = "nickName";
        String CUSTOMER_NAME = "customerName";
        String CUSTOMER_DESCRIPTION = "customerDescription";
        String CUSTOMER_BUSINESS_CARD_URL = "customerBusinessCardUrl";
        String AVATAR_ID = "avatarId";
        String IS_ADDRESS_BOOK = "isAddressBook";
        String IS_MOBILE = "isMobile";
        String USER_TYPE = "userType";
        String ROOM_ID = "roomId";
        String OPEN_ID = "openId";
        String SCOPE_INFOS = "scopeInfos";
        String SERVICE_NUMBER_IDS = "serviceNumberIds";
        String SCOPE_ARRAY = "scopeArray";
        String UPDATE_TIME = "updateTime";
        String MOBILE_VISIBLE = "mobileVisible";
        String STATUS = "status";
    }

    public static final String PATH_GROUP = "crowd";

    public static final class GroupEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUP).build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "crowd";
        public static final String COLUMN_CUSTOM_NAME = "custom_name";
        public static final String COLUMN_KIND = "kind";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_OWNER_ID = "owner_id";
        public static final String COLUMN_AVATAR_URL = "avatar_id";
        public static final String COLUMN_MEMBER_IDS = "member_ids";
    }

    public static final String PATH_SERVICE_NUM = "service_num";


    public static final class ServiceNumEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SERVICE_NUM).build();

        public static final String TABLE_NAME = "service_num";
        public static final String COLUMN_SERVICE_NUMBER_ID = "service_num_id";
        public static final String COLUMN_SERVICE_NUMBER_TYPE = "service_num_type";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_SUBSCRIBE = "subscribe";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_ROOM_ID = "roomId";
        public static final String COLUMN_AVATAR_URL = "avatar";
        public static final String COLUMN_BROADCAST_ROOM_ID = "broadcast_room_id";
        public static final String COLUMN_OWNER_ID = "owner_id";
        public static final String COLUMN_FIRST_WELCOME_MESSAGE = "first_welcome_message";
        public static final String COLUMN_EACH_WELCOME_MESSAGE = "each_welcome_message";
        public static final String COLUMN_INTERVAL_WELCOME_MESSAGE = "interval_welcome_message";
        public static final String COLUMN_IS_ENABLE = "is_enable";
        public static final String COLUMN_IS_IN_SITE_SERVICE = "is_in_site_service";
        public static final String COLUMN_IS_OUT_SITE_SERVICE = "is_out_site_service";
        public static final String COLUMN_IS_MANAGER = "is_manager";
        public static final String COLUMN_IS_OWNER = "is_owner";
        public static final String COLUMN_IS_COMMON = "isCommon"; //是否是成員
        public static final String COLUMN_INTERNAL_SUBSCRIBE_COUNT = "internal_subscribe_count";
        public static final String COLUMN_EXTERNAL_SUBSCRIBE_COUNT = "external_subscribe_count";
        public static final String COLUMN_SERVICE_MEMBER_ROOM_ID ="service_member_room_id";
        public static final String COLUMN_SERVICE_IDEL_TIME ="serviceIdleTime";
        public static final String COLUMN_SERVICE_TIMEOUT_TIME ="serviceTimeoutTime";
        public static final String COLUMN_STATUS ="status";
        public static final String COLUMN_SERVICE_OPEN_TYPE ="serviceOpenType";
        public static final String COLUMN_ROBOT_SERVICE_FLAG ="robotServiceFlag";
        public static final String COLUMN_ROBOT_ID ="robotId";
        public static final String COLUMN_ROBOT_NAME ="robotName";
        public static final String COLUMN_MEMBER_ITEMS ="memberItems";
        public static final String COLUMN_UPDATE_TIME ="updateTime";

        public static Map<String, Integer> getIndex(Cursor cursor) {
            Map<String, Integer> index = Maps.newHashMap();
            index.put(COLUMN_SERVICE_NUMBER_ID, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_ID));
            index.put(COLUMN_SERVICE_NUMBER_TYPE, cursor.getColumnIndex(COLUMN_SERVICE_NUMBER_TYPE));
            index.put(COLUMN_DESCRIPTION, cursor.getColumnIndex(COLUMN_DESCRIPTION));
            index.put(COLUMN_SUBSCRIBE, cursor.getColumnIndex(COLUMN_SUBSCRIBE));
            index.put(COLUMN_NAME, cursor.getColumnIndex(COLUMN_NAME));
            index.put(COLUMN_ROOM_ID, cursor.getColumnIndex(COLUMN_ROOM_ID));
            index.put(COLUMN_OWNER_ID, cursor.getColumnIndex(COLUMN_OWNER_ID));
            index.put(COLUMN_AVATAR_URL, cursor.getColumnIndex(COLUMN_AVATAR_URL));
            index.put(COLUMN_BROADCAST_ROOM_ID, cursor.getColumnIndex(COLUMN_BROADCAST_ROOM_ID));
            index.put(COLUMN_FIRST_WELCOME_MESSAGE, cursor.getColumnIndex(COLUMN_FIRST_WELCOME_MESSAGE));
            index.put(COLUMN_EACH_WELCOME_MESSAGE, cursor.getColumnIndex(COLUMN_EACH_WELCOME_MESSAGE));
            index.put(COLUMN_INTERVAL_WELCOME_MESSAGE, cursor.getColumnIndex(COLUMN_INTERVAL_WELCOME_MESSAGE));
            index.put(COLUMN_IS_ENABLE, cursor.getColumnIndex(COLUMN_IS_ENABLE));
            index.put(COLUMN_IS_IN_SITE_SERVICE, cursor.getColumnIndex(COLUMN_IS_IN_SITE_SERVICE));
            index.put(COLUMN_IS_OUT_SITE_SERVICE, cursor.getColumnIndex(COLUMN_IS_OUT_SITE_SERVICE));
            index.put(COLUMN_IS_MANAGER, cursor.getColumnIndex(COLUMN_IS_MANAGER));
            index.put(COLUMN_IS_OWNER, cursor.getColumnIndex(COLUMN_IS_OWNER));
            index.put(COLUMN_IS_COMMON, cursor.getColumnIndex(COLUMN_IS_COMMON));
            index.put(COLUMN_INTERNAL_SUBSCRIBE_COUNT, cursor.getColumnIndex(COLUMN_INTERNAL_SUBSCRIBE_COUNT));
            index.put(COLUMN_EXTERNAL_SUBSCRIBE_COUNT, cursor.getColumnIndex(COLUMN_EXTERNAL_SUBSCRIBE_COUNT));
            index.put(COLUMN_SERVICE_MEMBER_ROOM_ID , cursor.getColumnIndex(COLUMN_SERVICE_MEMBER_ROOM_ID));
            index.put(COLUMN_SERVICE_IDEL_TIME , cursor.getColumnIndex(COLUMN_SERVICE_IDEL_TIME));
            index.put(COLUMN_SERVICE_TIMEOUT_TIME , cursor.getColumnIndex(COLUMN_SERVICE_TIMEOUT_TIME));
            index.put(COLUMN_STATUS , cursor.getColumnIndex(COLUMN_STATUS));
            index.put(COLUMN_SERVICE_OPEN_TYPE , cursor.getColumnIndex(COLUMN_SERVICE_OPEN_TYPE));
            index.put(COLUMN_ROBOT_SERVICE_FLAG , cursor.getColumnIndex(COLUMN_ROBOT_SERVICE_FLAG));
            index.put(COLUMN_ROBOT_ID , cursor.getColumnIndex(COLUMN_ROBOT_ID));
            index.put(COLUMN_ROBOT_NAME , cursor.getColumnIndex(COLUMN_ROBOT_NAME));
            index.put(COLUMN_MEMBER_ITEMS , cursor.getColumnIndex(COLUMN_MEMBER_ITEMS));
            index.put(COLUMN_UPDATE_TIME , cursor.getColumnIndex(COLUMN_UPDATE_TIME));
            return index;
        }
    }

    public static final class ServiceNumberEntryIndex {
        public static final String IDX_COLUMN = "idx_service_number";
    }

    public static final String PATH_GROUP_MEMBER = "group_member";

    public static final class GroupMemberEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUP_MEMBER).build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String COLUMN_ACCOUNT_ID = "account_id";
        public static final String TABLE_NAME = "group_member";
        public static final String COLUMN_NICKNAME = "nickname";
        public static final String COLUMN_ALIAS = "alias";
        public static final String COLUMN_AVATAR = "avatar";
        public static final String COLUMN_SIGNTURE = "signture";
    }

    public static final String PATH_SEARCH_RECORDS = "search_records";

    public static final class SearchRecordEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_RECORDS).build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "records";
        public static final String COLUMN_NAME = "name";
    }

    public static final String PATH_LABEL = "label";

    public static final class LabelEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LABEL).build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "label";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_USER_IDS = "user_ids";
        public static final String COLUMN_USERS = "users";
        public static final String COLUMN_CREATE_TIME = "createTime";
        public static final String COLUMN_OWNER_ID = "ownerId";
        public static final String COLUMN_READ_ONLY = "read_only";
        public static final String COLUMN_DELETED = "deleted";
    }

    public static final String PATH_RECOMMEND = "recommend";

    public static final class RecommendEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RECOMMEND).build();

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "recommend";
        public static final String COLUMN_NAME = "_name";
        public static final String COLUMN_URL = "_url";
    }

    public static final class SearchLabelHistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "search_history";
        public static final String COLUMN_NAME = "name";
    }


    public static final class TodoEntry implements BaseColumns {

        public static final String TABLE_NAME = "aile_todo";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OPEN_CLOCK = "open_clock";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_REMIND_TIME = "remindTime";
        public static final String COLUMN_CREATE_TIME = "create_time";
        public static final String COLUMN_UPDATE_TIME = "update_time";
        public static final String COLUMN_TODO_STATUS = "todo_status";

        public static final String COLUMN_ROOM_ID = "room_id";
        public static final String COLUMN_MESSAGE_ID = "message_id";
        public static final String COLUMN_PROCESS_STATUS = "process_status";
        public static final String COLUMN_PUBLIC_TYPE = "publicType";
    }

    public static final class BroadcastTopicEntry implements BaseColumns {
        public static final String TABLE_NAME = "broadcast_topic";

        public static final String COLUMN_AVATAR_ID = "avatar_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
    }


    public static final class EntityTopicRel implements BaseColumns {
        public static final String TABLE_NAME = "entity_topic_rel";

        public static final String COLUMN_RELATION_ID = "rel_id";
        public static final String COLUMN_RELATION_TYPE = "rel_type";
        public static final String COLUMN_TOPIC_ID = "top_id";
    }

    public static final class EntityTopicRelIndex {
        public static final String IDX_COLUMN = "idx_entity_topic_rel";
    }


    public static final class BusinessEntry implements BaseColumns {
        public static final String TABLE_NAME = "ecp_business";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_END_TIME = "end_time";
        public static final String COLUMN_END_TIMESTAMP = "end_timestamp";
        public static final String COLUMN_BUSINESS_CODE = "business_code";
        public static final String COLUMN_PRIMARY_ID = "primary_id";
        public static final String COLUMN_PRIMARY_NAME = "primary_name";
        public static final String COLUMN_MANAGER_ID = "manager_id";
        public static final String COLUMN_MANAGER_AVATAR_ID = "manager_avatar_id";
        public static final String COLUMN_MANAGER_NAME = "manager_name";
        public static final String COLUMN_EXECUTOR_ID = "executor_id";
        public static final String COLUMN_EXECUTOR_AVATAR_ID = "executor_avatar_id";
        public static final String COLUMN_EXECUTOR_NAME = "executor_name";
        public static final String COLUMN_CUSTOMER_ID = "customer_id";
        public static final String COLUMN_CUSTOMER_NAME = "customer_name";
        public static final String COLUMN_CUSTOMER_AVATAR_ID = "customer_avatar_id";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_IS_ENABLE = "is_enable";

    }


    /**
     * Service Statistics
     * <p>
     * _id serial number
     * * rel_id association ID
     * * total row total
     * * score scoring
     * * ascription attribution (source), "professional service number, member, time of use, general service number"
     * * original_content original data
     * * stat_type statistical type
     * * update_time update time
     * </p>
     */
    public static final class StatisticsEntry implements BaseColumns {
        public static final String TABLE_NAME = "ecp_stat";

        public static final String COLUMN_RELATION_ID = "rel_id";
        public static final String COLUMN_ASCRIPTION = "ascription";
        public static final String COLUMN_ORIGINAL_CONTENT = "original_content";
        public static final String COLUMN_TOTAL_ROW = "total_row";
        public static final String COLUMN_ROW_COUNT = "row_count";
        public static final String COLUMN_STAT_TYPE = "stat_type";
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_END_TIME = "end_time";
        public static final String COLUMN_UPDATE_TIME = "update_time";
    }

    public static final class StatisticsEntryIndex {
        public static final String IDX_COLUMN = "idx_ecp_stat";
    }


    public static final class StickerPackageEntry implements BaseColumns {
        public static final String TABLE_NAME = "ecp_sticker_package";

        public static final String COLUMN_PACKAGE_NAME = "package_name";
        public static final String COLUMN_ICON_ID = "icon_id";
        public static final String COLUMN_ICON_URL = "icon_url";
        public static final String COLUMN_JOIN_TIME = "join_time";

        // local  control
        public static final String COLUMN_EMOTICON_TYPE = "emoticon_type";
        public static final String COLUMN_ITEM_COUNT = "item_count";
        public static final String COLUMN_UPDATE_TIME = "update_time";
        public static final String COLUMN_ITEM_LINE = "item_line";
        public static final String COLUMN_ITEM_ROW = "item_row";
        public static final String COLUMN_SELECT_ACTION = "select_action";
        public static final String COLUMN_IS_ENABLE = "is_enable";
    }


    public static final class StickerItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "ecp_sticker_item";

//        public static final String COLUMN_RELATION_PACKAGE_ID = "rel_package_id"; //

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DISPLAY_NAME = "display_name";
        public static final String COLUMN_SORT_INDEX = "sort_index";
        public static final String COLUMN_KEYWORDS = "keywords";
        public static final String COLUMN_PICTURE_ID = "picture_id";
        public static final String COLUMN_PICTURE_URL = "picture_url";
        public static final String COLUMN_THUMBNAIL_PICTURE_ID = "thumbnail_picture_id";
        public static final String COLUMN_THUMBNAIL_PICTURE_URL = "thumbnail_picture_url";
        public static final String COLUMN_STICKER_PACKAGE_ID = "sticker_package_id";
        //
        public static final String COLUMN_UPDATE_TIME = "update_time";
        public static final String COLUMN_IS_ENABLE = "is_enable";
    }


    public static final class StickerItemEntryIndex {
        public static final String IDX_COLUMN = "idx_ecp_sticker_item";
    }

    /**
     * Used in Service Number Agents Relation
     * _id = serviceNumbetId + broadcastRoomId
     */
    public static final class ServiceNumberAgentRel implements BaseColumns {
        public static final String TABLE_NAME = "service_number_agent_rel";
        public static final String COLUMN_SERVICE_NUMBER_ID = "service_number_id";
        public static final String COLUMN_BROADCAST_ROOM_ID = "broadcast_room_id";
        public static final String COLUMN_AGENT_ID = "agent_id";
        public static final String COLUMN_AGENT_PRIVILEGE = "agent_privilege";

    }

    public static final class ServiceNumberAgentRelIndex {
        public static final String IDX_COLUMN = "idx_service_number_agent_rel";
    }

    public static final class SyncGroupEntry implements BaseColumns {
        public static final String TABLE_NAME = "group_info";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_AVATAR_URL = "avatar_id";
        public static final String COLUMN_IS_CUSTOM_NAME = "isCustomName";
        public static final String COLUMN_DELETED = "deleted";
        public static final String COLUMN_MEMBER_DELETED = "member_deleted";
        public static final String COLUMN_UPDATE_TIME = "updateTime";
        public static final String COLUMN_TOP_TIME = "topTime";
        public static final String COLUMN_IS_TOP = "isTop";
        public static final String COLUMN_DFR_TIME = "dfrTime";
        public static final String COLUMN_MEMBER_IDS = "member_ids";
        public static final String COLUMN_LAST_READ_SEQUENCE = "lastReadSequence";
        public static final String COLUMN_LAST_RECEIVED_SEQUENCE = "lastReceivedSequence";
    }
}
