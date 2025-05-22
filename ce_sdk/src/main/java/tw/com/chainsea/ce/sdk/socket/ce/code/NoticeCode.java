package tw.com.chainsea.ce.sdk.socket.ce.code;

import com.google.gson.annotations.SerializedName;

/**
 * current by evan on 2020-09-30
 *
 * @author Evan Wang
 * date 2020-09-30
 */
public enum NoticeCode {
    @SerializedName("Ce.Notice.ReadAndReceived") READ_RECEIVED("Ce.Notice.ReadAndReceived"),  // 1.1 Message has been read notification
    @SerializedName("Ce.Notice.DeleteMessage") DELETE_MESSAGE("Ce.Notice.DeleteMessage"),  // 1.2 Message deletion notification
    @SerializedName("Ce.Notice.RetractMessage") RETRACT_MESSAGE("Ce.Notice.RetractMessage"),  // 1.3 Notification of message withdrawal
    @SerializedName("Ce.Notice.CreateRoom") CREATE_ROOM("Ce.Notice.CreateRoom"),  // 2.1 Create a multi-person chat room notification
    @SerializedName("Ce.Notice.UpgradeRoom") UPGRADE_ROOM("Ce.Notice.UpgradeRoom"),  // Upgrade chat room notification
    @SerializedName("Ce.Notice.AddRoomMember") ADD_ROOM_MEMBER("Ce.Notice.AddRoomMember"),  // 2.2 Notice of new membership (notify existing members)
    @SerializedName("Ce.Notice.ExitRoom") USER_EXIT("Ce.Notice.ExitRoom"),  // 2.3 Notice of member withdrawal
    @SerializedName("Ce.Notice.DeleteRoomMember") DELETE_ROOM_MEMBER("Ce.Notice.DeleteRoomMember"),  // 2.4 Delete chat room member notification
    @SerializedName("Ce.Notice.DismissRoom") DISMISS_ROOM("Ce.Notice.DismissRoom"),  // 2.5 Notification of chat room dissolution
    @SerializedName("Ce.Notice.UpdateRoomName") UPDATE_ROOM_NAME("Ce.Notice.UpdateRoomName"),  // 2.6 Update chat room name notification
    @SerializedName("Ce.Notice.TransferOwner") TRANSFER_OWNER("Ce.Notice.TransferOwner"),  // 2.7 Notification of transfer of management authority
    @SerializedName("Ce.Notice.UpdateGroupAvatar") UPDATE_GROUP_AVATAR("Ce.Notice.UpdateGroupAvatar"),  // 2.8 Notification of Group Avatar Change
    @SerializedName("Ce.Notice.InviteAddRoom") INVITE_ADD_ROOM("Ce.Notice.InviteAddRoom"),  // 2.9 Notification of new members (notify new members)
    @SerializedName("Ce.Notice.UpdateDiscussAvatar") UPDATE_DISCUSS_AVATAR("Ce.Notice.UpdateDiscussAvatar"),  // 2.10 Update group chat (discuss) avatar notification (triggered by personal avatar change)
    @SerializedName("Ce.Notice.AddAddressBook") ADD_ADDRESS_BOOK("Ce.Notice.AddAddressBook"),  // 3.1 Add friend notification
    @SerializedName("Ce.Notice.DeleteAddressBook") DELETE_FRIEND("Ce.Notice.DeleteAddressBook"),  // 3.2 Delete friend notification
    //@SerializedName("Ce.Notice.SqueezedOut") SQUEEZED_OUT("Ce.Notice.SqueezedOut"),  // 4.1 Notification of being squeezed out
    @SerializedName("Ce.Notice.OtherDeviceLogin") OTHER_DEVICE_LOGIN("Ce.Notice.OtherDeviceLogin"),  // 4.2 Other vehicles login notification
    @SerializedName("Ce.Notice.PublishMood") PUBLISH_MOOD("Ce.Notice.PublishMood"),  // 4.3 Post mood
    @SerializedName("Ce.Notice.Block") BLOCK("Ce.Notice.Block"),  // 4.4 Block user notifications
    @SerializedName("Ce.Notice.unBlock") UNBLOCK("Ce.Notice.unBlock"),  // 4.5 Unblock notification
    @SerializedName("Ce.Notice.UpdateProfile") UPDATE_PROFILE("Ce.Notice.UpdateProfile"),// 4.6 Notification of Personal Information Update
    @SerializedName("Ce.Notice.UpdateCustomerProfile") UPDATE_CUSTOMER_PROFILE("Ce.Notice.UpdateCustomerProfile"),
    @SerializedName("Ce.Notice.UpdateUserAvatar") UPDATE_USER_AVATAR("Ce.Notice.UpdateUserAvatar"),  // 4.7 Notification of updating profile picture
    @SerializedName("Ce.Notice.AddServiceNumberMember") ADD_SERVICE_NUMBER_MEMBER("Ce.Notice.AddServiceNumberMember"),  // 5.1 Service account new member notification (need to rethink)
    @SerializedName("Ce.Notice.DeleteServiceNumberMember") DELETE_SERVICE_NUMBER_MEMBER("Ce.Notice.DeleteServiceNumberMember"),  //5.2 Service account delete member notification (need to rethink)
    @SerializedName("Ce.Notice.ServiceNumberSubscribe") SERVICE_NUMBER_SUBSCRIBE("Ce.Notice.ServiceNumberSubscribe"),  //5.3 Service account subscription notification
    @SerializedName("Ce.Notice.ServiceNumberUnSubscribe") SERVICE_NUMBER_UNSUBSCRIBE("Ce.Notice.ServiceNumberUnSubscribe"),  //5.4 Service account unsubscribe notification
    @SerializedName("Ce.Notice.CreateLabel") CREATE_LABEL("Ce.Notice.CreateLabel"),  // 6.1 New label notification
    @SerializedName("Ce.Notice.UpdateLabelName") UPDATE_LABEL_NAME("Ce.Notice.UpdateLabelName"),  //6.2 Update label name notification
    @SerializedName("Ce.Notice.UpdateLabelMember") UPDATE_LABEL_MEMBER("Ce.Notice.UpdateLabelMember"),  //6.3 Change label member notification
    @SerializedName("Ce.Notice.DeleteLabel") DELETE_LABEL("Ce.Notice.DeleteLabel"),  //6.4 Remove label notification
    @SerializedName("Ce.Notice.AddVistorAddressBook") ADD_VISTOR_ADDRESSBOOK("Ce.Notice.AddVistorAddressBook"),  //7.1 GW turns to manual (personnel) to create visitor friend notification
    @SerializedName("Ce.Notice.SyncReadFlag") SYNC_READ("Ce.Notice.SyncReadFlag"),  //
    @SerializedName("Ce.Notice.ServiceNumberRegisterAgent") SERVICE_REGISTER_AGENT("Ce.Notice.ServiceNumberRegisterAgent"),  // Service account members start service
    @SerializedName("Ce.Notice.ServiceNumberReleaseAgent") SERVICE_RELEASE_AGENT("Ce.Notice.ServiceNumberReleaseAgent"),  // Service account member ends service
    @SerializedName("Ce.Notice.AppointOffline") SERVICE_APPOINT_OFFLINE("Ce.Notice.AppointOffline"),  // The channel is offline
    @SerializedName("Ce.Notice.AppointOnline") SERVICE_APPOINT_ONLINE("Ce.Notice.AppointOnline"),  // The channel is online
    @SerializedName("Ce.Notice.BusinessBindingRoom") BUSINESS_BINDING_ROOM("Ce.Notice.BusinessBindingRoom"),  // When the chat room is bound with business, a multi-person chat room is now
    @SerializedName("Ce.Notice.BusinessUnBindingRoom") BUSINESS_UNBINDING_ROOM("Ce.Notice.BusinessUnBindingRoom"),  // Send this notification when a chat room is disassociated from an business
    @SerializedName("Ce.Notice.DismissBusinessObjectRoom") CE_NOTICE_DISMISS_BUSINESS_OBJECT_ROOM("Ce.Notice.DismissBusinessObjectRoom"),  // The chat room of an object is disassociated from the task by the owner.
    @SerializedName("Ce.Notice.CancelTopRoom") NOTICE_CANCEL_TOP_ROOM("Ce.Notice.CancelTopRoom"),  // Clear sticky
    @SerializedName("Ce.Notice.TopRoom") NOTICE_TOP_ROOM("Ce.Notice.TopRoom"),  // to Top
    @SerializedName("Ce.Notice.Todo") NOTICE_TODO("Ce.Notice.Todo"),  // To-do event
    @SerializedName("Ce.Notice.MuteRoom") NOTICE_MUTE_ROOM("Ce.Notice.MuteRoom"),  // Mute chat room
    @SerializedName("Ce.Notice.User.Mute") NOTICE_MUTE_USER("Ce.Notice.User.Mute"),  // Global mute
    @SerializedName("Ce.Notice.Broadcast") NOTICE_BROADCAST_EVENT("Ce.Notice.Broadcast"),  // Broadcast message notification
    @SerializedName("Ce.Notice.ServiceNumberMember") NOTICE_SERVICE_NUMBER_MEMBER("Ce.Notice.ServiceNumberMember"),  // Service change notification
    @SerializedName("Ce.Notice.CancelMuteRoom") NOTICE_CANCEL_MUTE_ROOM("Ce.Notice.CancelMuteRoom"),  // Unmute
    @SerializedName("Ce.Notice.ServiceNumber") NOTICE_SERVICE_NUMBER("Ce.Notice.ServiceNumber"),  // Service account related operational events

    @SerializedName("Ce.Notice.RobotService") NOTICE_ROBOT_SERVICE("Ce.Notice.RobotService"),
    @SerializedName("Ce.Notice.RobotLastMessage") NOTICE_ROBOT_LAST_MESSAGE("Ce.Notice.RobotLastMessage"),
    @SerializedName("Ce.Notice.RobotStop") NOTICE_ROBOT_STOP("Ce.Notice.RobotStop"),
    @SerializedName("Ce.Notice.RobotWarning") NOTICE_ROBOT_WARNING("Ce.Notice.RobotWarning"),

    @SerializedName("Ce.Notice.GuarantorJoin") NOTICE_GUARANTOR_JOIN("Ce.Notice.GuarantorJoin"),
    @SerializedName("Ce.Notice.GuarantorJoinAgree") NOTICE_GUARANTOR_JOIN_AGREE("Ce.Notice.GuarantorJoinAgree"),
    @SerializedName("Ce.Notice.GuarantorJoinReject") NOTICE_GUARANTOR_JOIN_REJECT("Ce.Notice.GuarantorJoinReject"),
    @SerializedName("Ce.Notice.IntelligentAssistance") NOTICE_INTELLIGENT_ASSISTANCE("Ce.Notice.IntelligentAssistance"),
    @SerializedName("Ce.Notice.At") NOTICE_AT("Ce.Notice.At"),
    @SerializedName("Ce.Notice.Post") NOTICE_POST("Ce.Notice.Post"),
    @SerializedName("Ce.Notice.Comment") NOTICE_COMMENT("Ce.Notice.Comment"),


    @SerializedName("") UNDEF("");

    private final String name;

    NoticeCode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
