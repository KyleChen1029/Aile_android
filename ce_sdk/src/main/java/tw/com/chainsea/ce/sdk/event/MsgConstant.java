package tw.com.chainsea.ce.sdk.event;

public class MsgConstant {


    public final static int REFRESH_FILTER = 1;  // filter
    //    public final static int BASE_LAUNCHED_FILTER = 2;  // Basic startup filter
    public final static int CLEAN_MSGS_FILTER = 3;  // Clean the information filter,
    public final static int ADD_GROUP_FILTER = 4;  // Add a group filter,
    public final static int REMOVE_GROUP_FILTER = 5;  // Delete the group filter,
    public final static int ACCOUNT_REFRESH_FILTER = 6;  // Account refresh filter,
    public final static int REMOVE_FRIEND_FILTER = 7;  // Delete friend filter,
    public final static int REMOVE_LOVE_ACCOUNT_FILTER = 8;  // Delete the collect account filter,
    public final static int GROUP_REFRESH_FILTER = 10;  // Group refresh filter
    public final static int GROUP_UPGRADE_FILTER = 11; // Group upgrade filter,
    public final static int SESSION_UPDATE_CALLING_FILTER = 12; // Session update call filter,
    public final static int INTERNET_STSTE_FILTER = 13; // Internet filter,
    public final static int CHAT_TITLE_FILTER = 14; // Chat title filter
    public final static int SESSION_REFRESH_FILTER = 15;  // Session refresh filter,
    public final static int ADD_LOVE_ACCOUNT_FILTER = 16; // Add a lover account filter,
    public final static int SELF_REFRESH_FILTER = 17; // Self-refresh filter
    public final static int ADD_FRIEND_FILTER = 18; // Add a friend filter,
    public final static int RECOMMEND_REFRESH_FILTER = 19; // Recommend refresh filter,
    public final static int CANCEL_FILTER = 20; // Cancel filter
    public final static int SESSION_REMOVE_FILTER = 23; // Session delete filter,
    public final static int BADGE_UPDATE_FILTER = 24; // Badge update filter,
    public final static int INIT_SESSIONS = 25; // Initialize session
    public final static int SESSION_UPDATE_FILTER = 26;  // Session update filter,
    //    public final static int IN_COMMING_FILTER = 27;  // In the filter,
    public final static int UPDATE_PROFILE = 28;  // Update personal information,
    public final static int MSG_RECEIVED_FILTER = 29;  // Message acceptance filter,
    public final static int MSG_STATUS_FILTER = 30;  // Message status filter
    public final static int MSG_NOTICE_FILTER = 31;  // Message notification filter
    public final static int USER_EXIT = 32;  // User exit,
    //public final static int SQUEEZED_OUT_FILTER = 33;  // Extrusion filter,
    public final static int REMOVE_SERVICE_NUM_FILTER = 34;  // Delete the service tag filter,
    public final static int ADD_SERVICE_NUM_FILTER = 35;  // Add a service tag filter,
    public final static int TOKEN_INVALID_FILTER = 36;  // Token invalid filter,
    public final static int REFRESH_CONTACT_FILTER = 37;  // Refresh the contact filter,
    public final static int TRANSFER_SEND_SUCCESS_FILTER = 38;  // Transfer sent successfully filter,
    public final static int SEND_NOTIFICATION_FILTER = 39;  // Send notification filter
    public final static int SEND_UPDATE_AVATAR = 40;  // Send updated avatar,
    public final static int SYNC_READ = 41;  // Synchronous read

    // EVAN_FLAG 2019-11-21 (1.8.0) personal Connection Service Event Notification
    public final static int SERVICE_NUMBER_PERSONAL_START = 42;  // Have a dedicated connection to start
    public final static int SERVICE_NUMBER_PERSONAL_STOP = 43;  // The end of the dedicated connection
    public final static int APPOINT_STATUS_CHECKING = 44;  // Channel status update


    public final static int REFRESH_ROOM_LIST_BY_ENTITY = 45;  // Update chat room list (ChatRoomEntity)
    public final static int SERVICE_NUMBER_TRANSFER_STATUS = 46;  // 服務號換手


    public final static int UPDATE_MESSAGE_STATUS = 48;  //Update message status
    //    public final static int TRIGGER_READ_ALL = 49; // Trigger all read
    public final static int CHANGE_LAST_MESSAGE = 50; // Update last Message


//    public final static int REOLACE_CHAT_ROOM_LIST_BY_LOCAL_OR_REMOTE = 51; //Update chat room information, local or remote


    public final static int CHAT_ROOM_DATA_LOADING_MAX_EVENT = 52;  // Give the data loading progress value, max and progress
    public final static int CHAT_ROOM_DATA_LOADING_PROGRESS_EVENT = 53;  // Give the data loading progress value, max and progress


    public final static int UPDATE_ALL_BADGE_NUMBER_EVENT = 54; // Update the number of All corner labels
    public final static int UPDATE_MAIN_BADGE_NUMBER_EVENT = 55; // Update the number of Main corner labels
    public final static int UPDATE_SERVICE_BADGE_NUMBER_EVENT = 56; // Update the number of Service corner labels

    public final static int BUSINESS_BINDING_ROOM_EVENT = 57; // When a multiplayer chat room is bound by an object

    public final static int CHANGE_TOP_ROOM = 58; // Change when the chat room is on top
    public final static int DELETE_ROOM = 59; // When the chat room is deleted


    public final static int OUTCROP_MENTION_UNREAD_ROOM = 60; // The notification list has found unread chat rooms that have been At

    public final static int NOTICE_EXECUTION_BUSINESS_CREATE_ACTION = 63; // Perform object creation

    public final static int NOTICE_DISCUSS_ROOM_TITLE_UPDATE = 64; // Multiplayer chat room was renamed


    public final static int MESSAGE_SEND_FAIL = 65; // Failed to send message


    public final static int NOTICE_FINISH_ACTIVITY = 66; // Notification to close Activity
    public final static int REFRESH_ROOM_BY_LOCAL = 67; // Refresh chat room data﹍local data

    public final static int CHANGE_MUTE_ROOM = 68; // When the chat room mute status changes

    public final static int SEND_PHOTO_MEDIA_SELECTOR = 69; // Send the selected picture


    public final static int NOTICE_UPDATE_AVATARS = 70;  // Bulk notification update avatar
    public final static int CHANGE_MUTE_USER = 71; // When the chat room mute status changes


    public final static int NOTICE_TODO_CREATE = 72;
    public final static int NOTICE_TODO_UPDATE = 73;
    public final static int NOTICE_TODO_DELETE = 74;
    public final static int NOTICE_TODO_COMPLETE = 75;

    public final static int NOTICE_APPEND_MESSAGE = 76; // Notification additional message entity
    public final static int NOTICE_TOPIC_SELECTOR = 77; // Select topic


    public final static int NOTICE_BROADCAST_MESSAGE_DELETE = 78; // Delete broadcast message
    public final static int NOTICE_BROADCAST_MESSAGE_UPDATE = 79; // Delete broadcast message
    public final static int NOTICE_BROADCAST_FLAG_STATUS = 80; // Broadcasting status
    public final static int REFRESH_FCM_TOKEN_ID = 81; // refresh google fire base FCM token Id

    public final static int NOTICE_ACCOUNT_REFRESH_FILTER = 82;  // Account refresh filter,
    public final static int NOTICE_ACCOUNT_DELETE_REFRESH_FILTER = 83;  // Account deleted
    public final static int NOTICE_APPEND_NEW_MESSAGE_IDS = 84; // Notification additional message entity
    public final static int NOTICE_APPEND_OFFLINE_MESSAGE_IDS = 85; //Notification additional message entity


    public final static int NOTICE_PLAY_NOTIFY_TONE = 86;

    public final static int NOTICE_NEW_MESSAGE_CHECKING_SEVICENUMBER_SERVICED = 87; // Notification additional message entity
    public final static int NOTICE_APPEND_RECENT_MAIN_ROOMS = 88;
    public final static int NOTICE_APPEND_RECENT_SERVICE_ROOM = 89;


    //    public final static int UPDATE_BUSINESS_EXPIRED_COUNT_EVENT = 90;
    public final static int UPDATE_TODO_EXPIRED_COUNT_EVENT = 90;

    public final static int NAVIGATE_TO_CHAT_ROOM = 91;

    public final static int SCROLL_TO_TARGET_MESSAGE_POSITION = 92;

    public final static int NOTICE_SERVICE_NUMBER_CONSULT_EVENT = 93;

    public final static int NOTICE_REFRESH_HOMEPAGE_BACKGROUND_PICS = 94;
    public final static int NOTICE_REFRESH_HOMEPAGE_AVATAR = 95;

    public final static int NOTICE_APPEND_CONSULTATION_NEW_MESSAGE_IDS = 96;

    public final static int NOTICE_TODO_UNBIND_ROOM = 97;
    public final static int NOTICE_SELF_EXIT_ROOM = 98;

    public final static int NOTICE_REFRESH_MENTION_DATA = 99;

    public final static int UPDATE_ROBOT_SERVICE_LIST = 100;

    public final static int DELETE_SERVICE_NUMBER_MEMBER = 101;

    public final static int DELETE_SERVICE_NUMBER_OTHER_MEMBER = 108;

    public final static int NOTICE_CLOSE_OLD_ROOM = 102;

    public final static int NOTICE_ROBOT_SERVICE_WARNED = 103;
    public final static int NOTICE_SERVICE_NUMBER_REFRESH_BY_DB = 104;


    public final static int NOTICE_PROVISIONAL_MEMBER_REMOVED = 105;

    public final static int NOTICE_PROVISIONAL_MEMBER_ADDED = 106;
    public final static int NOTICE_DISABLE_SERVICE_NUMBER = 107;

    public final static int NOTICE_GUARANTOR_JOIN = 109;
    public final static int NOTICE_GUARANTOR_JOIN_AGREE = 110;
    public final static int NOTICE_GUARANTOR_JOIN_REJECT = 111;

    public final static int NOTICE_SERVICE_NUMBER_DISABLE = 112;

    public final static int NOTICE_SERVICE_NUMBER_ADD_FROM_PROVISIONAL = 113;

    public final static int NOTICE_DISCUSS_MEMBER_EXIT = 114;

    public final static int NOTICE_DISCUSS_MEMBER_ADD = 118;

    public final static int NOTICE_DISCUSS_GROUP_MEMBER_REMOVED = 115;
    public final static int NOTICE_DISCUSS_GROUP_USER_PROFILE_CHANGED = 116;

    public final static int MESSAGE_QUICK_REPLY = 117;

    public final static int NOTICE_INTELLIGENT_ASSISTANCE = 119;
    public final static int MESSAGE_AI_CONSULTATION_QUOTED_IMAGE = 120;
    public final static int MESSAGE_AI_CONSULTATION_QUOTED_VIDEO = 121;
    public final static int NOTICE_CREATE_ROOM = 122;
    public final static int NOTICE_CLEAR_CHAT_ROOM_ALL_MESSAGE = 123;
    public final static int MESSAGE_AT = 124;
    public final static int FACEBOOK_COMMENT_CREATE = 125;
    public final static int FACEBOOK_COMMENT_UPDATE = 126;
    public final static int FACEBOOK_COMMENT_DELETE = 127;
    public final static int FACEBOOK_POST_DELETE = 128;
    public final static int SERVICE_NUMBER_UPDATE = 129;
    public final static int ON_FACEBOOK_PRIVATE_REPLY = 130;
    public final static int Do_UPDATE_CONTACT_BY_LOCAL = 131;
    public final static int UPDATE_LINE_CUSTOMER_AVATAR = 132;
    public final static int UPDATE_CUSTOMER_NAME = 133;
    public final static int REFRESH_CUSTOMER_NAME = 134;
    public final static int REFRESH_SERVICE_NUMBER = 135;
    public final static int MEMBER_EXIT_DISMISS_ROOM = 136;
    public final static int DESKTOP_LOGIN_SUCCESS = 137;

    public final static int QUOTE_AND_SEND_MESSAGE = 138;
    public final static int QUOTE_MESSAGE = 139;
    public final static int QUOTE_AND_SEND_IMAGE_MESSAGE = 140;
    public final static int QUOTE_AND_SEND_TEMPLATE_MESSAGE = 141;

    public final static int MESSAGE_VIDEO_UPDATE_DOWNLOAD_PROGRESS = 142;
    public final static int MESSAGE_VIDEO_ON_DOWNLOAD_SUCCESS = 143;
    public final static int NOTICE_ROBOT_STOP = 144;
    public final static int NOTICE_DISMISS_DISCUSS_ROOM = 145;
    public final static int NOTICE_MEDIA_SELECTOR_REFRESH = 146;
    public final static int MESSAGE_VIDEO_UPDATE_DOWNLOAD_CANCEL = 147;
    public final static int NOTICE_REFRESH_CHAT_ROOM_LIST = 148;

    public final static int API_ON_FAILED_MESSAGE_EVENT = 1000; // API request failure event


    // UI handle
    public final static int UI_NOTICE_TODO_REFRESH = 4000;
//    public final static int UI_NOTICE_TODO_SHOW_ALARM  = 4001;

    public final static int UI_NOTICE_TO_TODO_ITEM = 4001;


    public final static int UI_NOTICE_TODO_UPDATE_ALARM = 4002;
    public final static int UI_NOTICE_TODO_DELETE_ALARM = 4003;


    public final static int NOTICE_KEEP_SCREEN_ON = 4004; // Notification prohibition of sleep
    public final static int NOTICE_CLEAR_KEEP_SCREEN_ON = 4005; // Notification of lifting of dormancy prohibition
    public final static int NOTICE_TODO_ALARM_DELAY = 4006;


    public final static int UI_NOTICE_BROADCAST_EDIT_TO_DEFAULT = 4007; // Broadcast editor returns to its default state

    public final static int UI_NOTICE_CLOSE_TREE_LIST = 4008; // If BusinessMe room list does not exist locally, close the tree structure list
    public final static int UI_NOTICE_UPDATE_AVATARS_ALL = 4009;

    public final static int UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT = 4010; // Update the number of work lists

    public final static int DOWNLOAD_STICKER_RESOURCES_BY_PACKAGE_ID = 4011; // Notification to download sticker resources

    public final static int SWITCH_BASE_STATUS_BAR_COLOR = 4012;  //  change the basic status bar color


    public final static int UI_NOTICE_SWIPE_MENU_CLOSE_OPEN = 4013;

    public final static int CLEAN_DB_AND_RELOAD = 4014;

    public final static int AIFF_REQUEST_PERMISSION = 4015;

    public final static int AIFF_ON_LOCATION_GET = 4016;

//    public final static int ACCOUNT_LOGOUT = 42;  // Account logout

}
