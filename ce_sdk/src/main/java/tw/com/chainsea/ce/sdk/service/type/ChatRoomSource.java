package tw.com.chainsea.ce.sdk.service.type;

import com.google.common.base.Strings;

import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
/**
 * current by evan on 2020-03-18
 *
 * @author Evan Wang
 * date 2020-03-18
 */

public enum ChatRoomSource {
    ALL(ChatRoomType.ALL_CHAT_ROOM_TYPES_2),
    MAIN(ChatRoomType.MAIN_CHAT_ROOM_TYPES_2), //Main chatRoom tab
    SERVICE(ChatRoomType.SERVICES_CHAT_ROOM_TYPES); //Service chatRoom tab

    public static ChatRoomSource of(String code) {
        if (Strings.isNullOrEmpty(code)) {
            return ALL;
        }
        for (ChatRoomSource s : values()) {
            if (s.name().equals(code)) {
                return s;
            }
        }
        return ALL;
    }

    ChatRoomSource(Set<ChatRoomType> chatRoomTypes) {
    }
}
