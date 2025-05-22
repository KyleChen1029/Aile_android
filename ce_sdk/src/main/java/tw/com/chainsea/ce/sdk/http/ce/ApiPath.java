package tw.com.chainsea.ce.sdk.http.ce;

/**
 * current by evan on 2020-09-15
 *
 * @author Evan Wang
 * date 2020-09-15
 */
public @interface ApiPath {

    String ROUTE = "";
    String tokenApply = "openapi/token/apply";
    String chatRoomList = "openapi/chat/room/list";
    String chatRoomItem = "openapi/chat/room/item";
    String chatRoomHomepage = "openapi/chat/room/homepage";
    String chatRoomRecentDelete = "openapi/chat/room/recent/delete";
    String chatRoomTopCancel = "openapi/chat/room/top/cancel";
    String chatRoomTop = "openapi/chat/room/top";
    String chatRoomCreate = "openapi/chat/room/create";
    String chatRoomUpdate = "openapi/chat/room/update";
    String chatRoomComplaint = "/openapi/chat/room/complaint";
    String chatRoomUpgrade = "openapi/chat/room/upgrade";
    String chatRoomDismiss = "openapi/chat/room/dismiss";
    String chatRoomMute = "openapi/chat/room/mute";
    String chatRoomMuteCancel = "openapi/chat/room/mute/cancel";
    String tenantDismiss = "/openapi/tenant/dismiss";
    String tenantEmployeeDelete = "/openapi/tenant/employee/delete";
    String tenantEmployeeExit = "openapi/tenant/employee/exit";
    String tenantServiceNumberList = "/openapi/tenant/servicenumber/list";


    String chatMemberExit = "openapi/chat/member/exit";
    String chatMemberDelete = "openapi/chat/member/delete";
    String chatMemberAdd = "/openapi/chat/member/add";

    String chatRoomHomepagepicsUpdate = "/openapi/chat/room/homepagepics/update";


    // 訊息
    String messageList = "openapi/chat/message/list";
    String messageItem = "openapi/chat/message/item";
    String messageSend = "openapi/chat/message/send";
    String messageReply = "/openapi/chat/message/reply";
    String messageDelete = "openapi/chat/message/delete";
    String messageClean = "openapi/chat/message/clean";
    String messageForward = "/openapi/chat/message/forward";
    String messageReceived = "/openapi/chat/message/received";
    String messageRead = "openapi/chat/message/read";
    String messageReadv2 = "openapi/chat/message/v2/read";
    String messageRetract = "/openapi/chat/message/retract";
    String messageReadingstate = "/openapi/chat/message/readingstate";
    // 服務號
    String serviceNumberContactList = "openapi/bossservicenumber/contact/list"; //取的客戶列表
    String serviceNumberList = "/openapi/servicenumber/list"; //取得服務號列表
    String serviceRoomItem = "openapi/chat/serviceroom/item"; //查询用户的订阅服务号聊天室
    String serviceNumberItem = "openapi/servicenumber/item";
    String serviceNumberUpdate = "openapi/servicenumber/update";
    String serviceNumberTransfer = "/openapi/servicenumber/transfer";
    String serviceNumberTransferCancel = "/openapi/servicenumber/transfer/cancel";
    String serviceNumberTransferComplete = "/openapi/servicenumber/transfer/complete";
    String serviceNumberTransferSnatch = "openapi/servicenumber/transfer/snatch";

    // 取得諮詢 AI 訊息
    String serviceNumberConsultAIMessage= "openapi/chat/message/assist/list";

    // 發送訊息給諮詢AI
    String serviceNumberConsultAISendMessage = "openapi/chat/message/assist/send";

    String serviceNumberBroadcastDelete = "/openapi/servicenumber/broadcast/delete";
    String serviceNumberBroadcastSend = "/openapi/servicenumber/broadcast/send";
    String serviceNumberBroadcastUpdate = "/openapi/servicenumber/broadcast/update";

    String serviceNumberStartService = "/openapi/servicenumber/startService";
    String serviceNumberStopService = "/openapi/servicenumber/stopService";
    String serviceNumberChatRoomAgentServiced = "openapi/servicenumber/chatroom/agent/serviced";
    String serviceNumberIdentityList = "openapi/service/identity/list"; //服務身份列表
    String serviceNumberIdentityBind =  "openapi/member/identity/bind"; //更換服務身份
    String becomeOwner = "openapi/chat/member/becomeOwner"; //成為社團聊天室擁有者

    String serviceNumberSubscribe = "/openapi/servicenumber/subscribe";
    String serviceNumberSearch = "/openapi/servicenumber/search";
    String serviceNumberConsult = "openapi/servicenumber/consult";
    String serviceNumberConsultList = "openapi/servicenumber/consult/list";
    String serviceNumberBusinessCardUrl = "openapi/servicenumber/getbusinesscard/url";
    String serviceNumberConsultAiStart = "openapi/servicenumber/consultAI/start";


    // 渠道
    String fromAppoint = "openapi/from/appoint";
    String fromSwitch = "/openapi/from/switch";

    // 服務號 代理人
    String agentStopService = "/openapi/agent/stopService";

    // 訂閱列表
    String topicList = "/openapi/topic/list";

    // 待辦事項
    String todoItem = "/openapi/todo/item";
    String todoComplete = "/openapi/todo/complete";
    String todoCreate = "/openapi/todo/create";
    String todoUpdate = "/openapi/todo/update";

    // 貼圖
    String stickerPackageList = "/openapi/sticker/package/list";
    String stickerList = "/openapi/sticker/list";
    // user
    String userUpdateProfile = "openapi/user/updateprofile";
    String userItem = "openapi/user/item";
    String userLogout = "/openapi/user/logout";
    String userList = "/openapi/user/list";
    String userProfile = "openapi/user/profile";
    String userMute = "/openapi/user/mute";
    String userMuteCancel = "/openapi/user/mute/cancel";
    String userBlock = "/openapi/user/block";
    //個人主頁
    String userHomePageUpdateCustomerProfileCustomer = "/openapi/user/updateprofile/customer";
    String userHomePagePicsUpdate = "/openapi/user/homepagepics/update";
    String addressBookAdd = "openapi/addressbook/add";
    String addressBookDelete = "/openapi/addressbook/delete";
    String addressBookCustomFriendInfo = "openapi/addressbook/customfriendinfo";
    String addressBookSync = "openapi/addressbook/sync";
    String updateMood = "openapi/user/mood/create";

    String aiffList = "openapi/aiff/list";


    // label
    String labelMemberDelete = "/openapi/label/member/delete";
    String labelMemberAdd = "/openapi/label/member/add";
    String labelCreate = "/openapi/label/create";
    String labelDelete = "openapi/label/delete";
    String labelItem = "openapi/label/item";
    String labelUpdate = "/openapi/label/update";

    // base
    String avatarView = "/openapi/base/avatar/view";
    String baseStickerDownload = "/openapi/base/sticker/download";
    String basePictureUpload = "/openapi/base/picture/upload";
    String baseAvatarUpload = "/openapi/base/avatar/upload";
    String baseRoomAvatarUpload = "/openapi/base/roomavatar/upload";
    String baseFileUpload = "/openapi/base/file/upload";


    // member
    String memberList = "openapi/chat/member/list";

    String deviceTokenUpdate = "/openapi/device/token/update";

    //差異化同步API
    String syncLabel = "openapi/base/sync/label"; //分页同步与当前用户同一租户下的标签资料列表
    String syncEmployee = "openapi/base/sync/employee"; //分页同步与当前用户同一租户下的员工资料列表
    String syncSubscribeServicenumber = "openapi/base/sync/subscribe/servicenumber"; //一次性抓取同步与当前用户已订阅的服务号资料列表
    String syncTodo = "openapi/base/sync/todo"; //分页同步与当前用户同一租户下的记事资料列表
    String syncServiceNumber = "openapi/base/sync/servicenumber"; //分页同步与当前用户同一租户下的服务号资料列表
    String syncContact = "openapi/base/sync/contact"; //分页同步与当前用户同一租户下的，且与当前租户有共同聊天室的客户资料列表

    String syncRoom = "openapi/base/sync/room"; //分页同步与当前用户同一租户下的聊天室资料列表
    String syncMessage = "openapi/base/sync/message"; //分页同步当前聊天室带序号的消息
    String syncRoomUnread = "openapi/base/sync/room/unread"; //分页同步与当前用户同一租户下的未读聊天室资料
    String syncChatMember = "openapi/base/sync/chatmember";//客户端在打开聊天室时，可请求该API，抓取聊天室成员
    String syncRoomNormal = "openapi/base/sync/room/normal";//分页同步与当前用户同一租户下的一般聊天室(除type=Services、Broadcast、ServiceMember)资料；
    String syncRoomServicenumber = "openapi/base/sync/room/servicenumber";//分页同步与当前用户同一租户下的服务号聊天室(包含type=Services、Broadcast、ServiceMember)资料
    String chatRoomRobotServiceList = "openapi/chat/room/robotservice/list"; // 取得機器人服務中的列表
    String chatRoomRobotServiceSnatch = "openapi/servicenumber/robot/transfer/snatch"; //强制接手机器人服务

    String syncServiceNumberActiveList = "openapi/chat/room/servicenumber/active/list"; //查询进线中和机器人服务中的聊天室记录

    String cancelAiWarning = "openapi/chat/room/warning/cancel";
    String chatRoomHomepagePics= "openapi/chat/room/homepage/pics"; //聊天室主頁背景圖片查看


    //服務號權限管理===================================================================
    String ServiceNumberModifyOwner = "openapi/servicenumber/modify/owner";
    String ServiceNumberMemberAddManager = "openapi/servicenumber/member/addManager";
    String ServiceNumberMemberDeleteManager = "openapi/servicenumber/member/deleteManager";
    String ServiceNumberMemberRemove = "openapi/servicenumber/member/remove";
    //=============================================================================

    //擔保人列表
    String TenantGuarantorList = "openapi/tenant/guarantor/list";

    //擔保人加入
    String TenantGuarantorAdd = "openapi/tenant/guarantor/add";

    String TenantReGuarantorAgree = "openapi/tenant/guarantor/agree";

    String TenantReGuarantorReject = "openapi/tenant/guarantor/reject";

    // api data count 火箭頁用
    String ApiDataCount = "openapi/base/sync/count";

    String syncGroup = "openapi/base/sync/group";

    String tokenAnew = "openapi/token/anew"; //延長使用期
    String tokenRefresh = "openapi/user/token/refresh"; //換一個新的
    String addChatRoomManager ="openapi/chat/member/addManager";
    String deleteChatRoomManager ="openapi/chat/member/deleteManager";
    // at 相關
    String chatRoomAt = "openapi/chat/room/at";
    String chatRoomAtRead= "openapi/chat/room/at/read";
    String chatRoomAtList = "openapi/chat/room/at/list";

    String bindFacebook = "openapi/servicenumber/facebook/fansPage/add";
    String unBindFacebook = "openapi/servicenumber/facebook/fansPage/remove";
    String bindInstagram = "openapi/servicenumber/instagram/fansPage/add";
    String unBindInstagram = "openapi/servicenumber/instagram/fansPage/remove";

    String sendFacebookPublicReply = "openapi/comment/send";
    String sendFacebookPrivateReply = "openapi/comment/private/reply";
    String checkCommentStatus = "openapi/comment/status";
    String sendBusinessCard = "openapi/business/card/send"; //發送電子名片
    String sendBusinessMemberCard = "openapi/send/join/card"; //發送企業會員卡片

    String offLineMessageList= "/openapi/user/offlinemessage/list"; //Socket重連後同步離線資料
    String offLineMessageClean = "/openapi/user/offlinemessage/clean"; //清除離線資料

    // 取得服務號超時 時間列表
    String getServiceTimeOutList = "openapi/servicenumber/dictionary/serviceTimeout/time";
    // 取得服務號無人問答 時間列表
    String getServiceIdleList = "openapi/servicenumber/dictionary/serviceIdle/time";
}