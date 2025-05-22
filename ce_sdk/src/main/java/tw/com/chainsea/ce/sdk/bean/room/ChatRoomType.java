package tw.com.chainsea.ce.sdk.bean.room;

/**
 * current by evan on 2019-11-12
 */

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.ToJson;

import java.util.Set;

/**
 * AccountType
 * Created by 90Chris on 2016/4/21.
 * person
 */
public enum ChatRoomType {
    @SerializedName("self") self(1, "self"),  // Personal chat room (current user’s own multi-vehicle chat room)
    @SerializedName("strange") strange(2, "strange"),  // Non-friend single chat room
    @SerializedName("friend") friend(3, "friend"),  // Friends chat room
    @SerializedName("broast") broast(4, "broast"),  // Broadcast chat room
    @SerializedName("group") group(5, "group"),  // Group chat room
    @SerializedName("services") services(6, "services"),  // Service account chat room Yellow theme ffbc42, fffceb
    @SerializedName("subscribe") subscribe(7, "subscribe"),  // Subscription service number chat room Joc
    @SerializedName("discuss") discuss(8, "discuss"),  // Multi-person discussion group
    @SerializedName("service") service(9, "service"),  // 概念上的, 目前用不到, linton於2022/4/13解釋
    @SerializedName("undef") undef(10, "undef"), // unknown
    @SerializedName("vistor") vistor(11, "vistor"),
    @SerializedName("system") system(12, "system"),  //  System chat room
    @SerializedName("business") business(13, "business"),  // Business chat room
    @SerializedName("broadcast") broadcast(14, "broadcast"), // Broadcast chat room
    @SerializedName("person") person(15, "person"), // Personal chat room
    @SerializedName("serviceMember") serviceMember(16, "serviceMember"), // service Member room

    @SerializedName("provisional") provisional(17, "provisional"), // 前端設定的臨時成員 type
    @SerializedName("bossOwner") bossOwner(18, "bossOwner"), // 前端設定的商務號擁有者聊天室 type
    @SerializedName("bossSecretary") bossSecretary(19, "bossSecretary"), // 前端設定的商務號秘書聊天室 type
    @SerializedName("bossServiceNumber") bossServiceNumber(20, "bossServiceNumber"), // 前端設定的商務號服務號聊天室 type
    @SerializedName("serviceINumberAsker") serviceINumberAsker(21, "serviceINumberAsker"), // 前端設定的內部服務號進線者聊天室 type
    @SerializedName("serviceINumberStaff") serviceINumberStaff(22, "serviceINumberStaff"), // 前端設定的內部服務號服務人員聊天室 type
    @SerializedName("serviceONumberAsker") serviceONumberAsker(23, "serviceONumberAsker"), // 前端設定的外部服務號進線者聊天室 type
    @SerializedName("serviceONumberStaff") serviceONumberStaff(24, "serviceONumberStaff"), // 前端設定的萬部服務號服務人員聊天室 type
    @SerializedName("consultAi") consultAi(25, "consultAi"), //前端設定的 諮詢 AI 聊天室type
    @SerializedName("bossOwnerWithSecretary") bossOwnerWithSecretary(26, "bossOwnerWithSecretary"); // 前端設定的商務號擁有者的秘書聊天室 type



    /**
     * 給 moshi 用的 Adapter
     * 因為 api 回的是小寫，無法轉換成 enum
     * */
   public static class ChatRoomTypeAdapter {
        @ToJson
        String toJson(ChatRoomType type) {
            return type.name;
        }

        @FromJson
        ChatRoomType fromJson(String type) {
            return ChatRoomType.of(type);
        }
    }

    private int type;
    private String name;

    ChatRoomType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static ChatRoomType of(int name) {
        for (ChatRoomType c : values()) {
            if (c.type == name) {
                return c;
            }
        }
        return undef;
    }

    public static ChatRoomType of(String name) {
        //為了讓 api 取得的資料跟原本 local db 存的資料一致
        name = name.replace("_", "").toLowerCase();
        for (ChatRoomType c : values()) {
            if (c.name.toLowerCase().equals(name) || c.name().toLowerCase().equals(name)) {
                return c;
            }
        }
        return undef;
    }

    public static Set<ChatRoomType> SELF_or_SYSTEM = Sets.newHashSet(self, system);

    public static Set<ChatRoomType> SYSTEM_or_SUBSCRIBE = Sets.newHashSet(system, services);

    public static Set<ChatRoomType> SERVICES_or_SUBSCRIBE = Sets.newHashSet(services, subscribe);

    public static Set<ChatRoomType> GROUP_or_DISCUSS = Sets.newHashSet(group, discuss);

    public static Set<ChatRoomType> GROUP_or_DISCUSS_or_SERVICE_MEMBER = Sets.newHashSet(group, discuss, serviceMember);

    public static Set<ChatRoomType> FRIEND_or_GROUP_or_DISCUS = Sets.newHashSet(friend, group, discuss);

    public static Set<ChatRoomType> FRIEND_or_GROUP_or_DISCUS_or_SERVICE_MEMBER = Sets.newHashSet(friend, group, discuss, serviceMember);

    public static Set<ChatRoomType> FRIEND_or_DISCUSS = Sets.newHashSet(friend, discuss);

    public static Set<ChatRoomType> FRIEND_or_STRANGE = Sets.newHashSet(friend, strange);

    public static Set<ChatRoomType> SERVICES_or_SUBSCRIBE_or_SYSTEM_or_PERSON_or_SERVICE_MEMBER = Sets.newHashSet(services, subscribe, system, person, serviceMember);

    public static Set<ChatRoomType> STRANGE_or_FRIEND_or_SELF = Sets.newHashSet(strange, friend, self);

    public static Set<ChatRoomType> FRIEND_or_SUBSCRIBE = Sets.newHashSet(friend, subscribe);

    public static Set<ChatRoomType> PERSON_or_SYSTEM = Sets.newHashSet(person, system);
    public static Set<ChatRoomType> PERSON_or_SYSTEM_or_FRIEND = Sets.newHashSet(person, system, friend);
    public static Set<ChatRoomType> PERSON_or_DISCUSS = Sets.newHashSet(person, discuss);

    public static Set<ChatRoomType> PERSON_or_SERVICES_or_SUBSCRIBE = Sets.newHashSet(person, services, subscribe);

    public static Set<ChatRoomType> GROUP_or_DISCUSS_or_BUSINESS = Sets.newHashSet(group, discuss, business);

    public static Set<ChatRoomType> MAIN_CHAT_ROOM_TYPES_2 = Sets.newHashSet(discuss, group, friend, subscribe, services, undef, broadcast);

    public static Set<ChatRoomType> SERVICES_CHAT_ROOM_TYPES = Sets.newHashSet(services);

    public static Set<ChatRoomType> ALL_CHAT_ROOM_TYPES_2 = Sets.newHashSet(undef, broadcast);
}


