package tw.com.chainsea.chat.config;

public enum BundleKey {
    SERVICE_NUMBER_ID("SERVICE_NUMBER_ID"),
    SUBSCRIBE_NUMBER_ID("SUBSCRIBE_NUMBER_ID"),
    SUBSCRIBE_AGENT_ID("SUBSCRIBE_AGENT_ID"),
    IS_FIRST_LOADING("IS_FIRST_LOADING"),
    IS_SUBSCRIBE("IS_SUBSCRIBE"),
    USER_ID("USER_ID"),
    USER_NICKNAME("USER_NICKNAME"),
    EXTRA_TITLE("EXTRA_TITLE"),
    EXTRA_SESSION_ID("EXTRA_SESSION_ID"),
    EXTRA_SESSION_LIST("EXTRA_SESSION_LIST"),
    SERVICE_NUMBER_NAME("SERVICE_NUMBER_NAME"),
    TRANSFER_ACTION("TRANSFER_ACTION"),
    ACCOUNT_ID("ACCOUNT_ID"),
    IS_INVITE("IS_INVITE"),
    IS_VIPCN_FROM_CHAT("IS_VIPCN_FROM_CHAT"),
    INVITE("INVITE"),
    ACCOUNT_TYPE("ACCOUNT_TYPE"),
    RESULT_PIC_URI("RESULT_PIC_URI"),
    RESULT_PIC_SMALL_URI("RESULT_PIC_SMALL_URI"),
    ACCOUNT_IDS("account_ids"),
    TRANSEND_MSG("transend_msg"),
    FROM_ROOM_ID("from_room_id"),
    TRANSEND_MSG_IDS("transend_msg_ids"),
    FROM_ROOM_IDS("FROM_ROOM_IDS"),
    EXTRA_ROOM_ENTITY("EXTRA_ROOM_ENTITY"),
    EXTRA_SESSION("EXTRA_SESSION"),
    EXTRA_MESSAGE("EXTRA_MESSAGE"),
    ROOM_IDS("ROOM_IDS"),
    ROOM_ID("ROOM_ID"),
    LABEL_ID("LABEL_ID"),
    BROADCAST_ROOM_ID("BROADCAST_ROOM_ID"),
    UNREAD_MESSAGE_ID("UNREAD_MESSAGE_ID"),
    BUSINESS_ID("BUSINESS_ID"),
    SEARCH_KEY("SEARCH_KEY"),
    UNREAD_TYPE("UNREAD_TYPE"),
    UNREAD_ROOM_IDS("UNREAD_ROOM_IDS"),
    WHERE_COME("WHERE_COME"),
    PHOTO_GALLERY_MESSAGE("PHOTO_GALLERY_MESSAGE"),
    PHOTO_GALLERY_URL("PHOTO_GALLERY_URL"),
    PHOTO_GALLERY_THUMBNAIL_URL("PHOTO_GALLERY_THUMBNAIL_URL"),
    INTENT_TO_TODO("INTENT_TO_TODO"),

    // 播放器使用
    VIDEO_PATH("VIDEO_PATH"),
    VIDEO_URL("VIDEO_URL"),
    VIDEO_WIDTH("VIDEO_WIDTH"),
    VIDEO_HEIGHT("VIDEO_HEIGHT"),
    VIDEO_POSITION("VIDEO_POSITION"),

//    QR_USER_ID("QR_USER_ID"),
//    QR_SERVICE_NUMBER_ID("QR_SERVICE_NUMBER_ID"),

    TITLE("TITLE"),
    LIMIT_MAX("LIMIT_MAX"),
    FILTER_TYPE("FILTER_TYPE"),
    ROOM_TYPE("ROOM_TYPE"),
    ACTION("ACTION"),
    SOURCE("SOURCE"),
    BUSINESS_ITEM("BUSINESS_ITEM"),
    MESSAGE_ID("MESSAGE_ID"),
    MESSAGE_IDS("MESSAGE_IDS"),
    ROOM_INSTANCE("ROOM_INSTANCE"),
    FILE_PATH("FILE_PATH"),
    IS_SEND_VIDEO("IS_SEND_VIDEO"),
    IS_ORIGINAL("IS_ORIGINAL"),
    MAX_COUNT("MAX_COUNT"),
    TYPE("TYPE"),
    VISION_TYPE("VISION_TYPE"),
    CURRENT("CURRENT"),
    DATA("DATA"),

    TOPIC_IDS("TOPIC_IDS"),
    TOPIC_SELECT_IDS("TOPIC_SELECT_IDS"),
    FILE_PATH_LIST("FILE_PATH_LIST"),
    OTP_VERIFY_CODE("OTP_VERIFY_CODE"),
    PHONE_NUMBER("PHONE_NUMBER"),
    SCAN_RESULT("SCAN_RESULT"),
    IS_CP_SCAN_RESULT("IS_CP_SCAN_RESULT"),
    DEVICE_NAME("DEVICE_NAME"),
    ONCE_TOKEN("ONCE_TOKEN"),
    BLACK_LIST("BLACK_LIST"),
    TENANT_GUARANTOR_ADD("TENANT_GUARANTOR_ADD"),
    URL("URL"),
    IS_BIND_AILE("IS_BIND_AILE"),
    BIND_URL("BIND_URL"),
    IS_COLLECT_INFO("IS_COLLECT_INFO"),
    SCANNER_TYPE("SCANNER_TYPE"),
    PROVISIONAL_MEMBER_IDS("PROVISIONAL_MEMBER_IDS"),
    MEMBERS_LIST("MEMBERS_LIST"),

    RE_SCAN_GUARANTOR("MEMBERS_LIST"),
    NEED_PRELOAD("NEED_PRELOAD"),
    AUTO_LOGIN("AUTO_LOGIN"),
    IS_NEED_SAVE_TENANT("IS_NEED_SAVE_TENANT"),
    CHAT_ROOM_NAME("CHAT_ROOM_NAME"),
    BOTTOM_TAB("BOTTOM_TAB"),
    TO_CREATE_OR_JOIN_TENANT("TO_CREATE_OR_JOIN_TENANT"),
    CONSULT_AI_ID("CONSULT_AI_ID"),
    CONSULT_AI_QUOTE_STRING("CONSULT_AI_QUOTE_STRING"),
    CONSULT_AI_QUOTE_TYPE("CONSULT_AI_QUOTE_TYPE"),
    CHAT_ROOM_STYLE("CHAT_ROOM_STYLE"),
    IS_FROM_FILTER("IS_FROM_FILTER"),
    MESSAGE_SORT("MESSAGE_SORT"),
    TARGET_QR_CODE_POSITION("TARGET_QR_CODE_POSITION"),
    THEME_COLOR("THEME_COLOR");


    private final String key;

    BundleKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
