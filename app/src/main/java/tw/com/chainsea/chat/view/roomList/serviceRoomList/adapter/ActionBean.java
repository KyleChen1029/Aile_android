package tw.com.chainsea.chat.view.roomList.serviceRoomList.adapter;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import tw.com.chainsea.chat.R;

/**
 * current by evan on 12/3/20
 *
 * @author Evan Wang
 * date 12/3/20
 */

public enum ActionBean {
    CHAT(R.string.service_room_sectioned_action_chat, R.drawable.icon_sectioned_action_chat_gray_21dp, 0),
    WAIT_TRANSFER(R.string.service_room_sectioned_action_waittransfer, R.drawable.icon_sectioned_action_wait_transfer, 1),
    BROADCAST(R.string.service_room_sectioned_action_broadcast, R.drawable.icon_sectioned_action_broadcast_gray_21dp, 2),
    WELCOME_MESSAGE(R.string.service_room_sectioned_action_welcome, R.drawable.icon_sectioned_action_welcome_message_gray_21dp, 3),
    MEMBERS(R.string.service_room_sectioned_action_members, R.drawable.icon_sectioned_action_members_gray_21dp, 4),
    HOME(R.string.service_room_sectioned_action_home, R.drawable.icon_sectioned_action_home_gray_21dp, 5);

    @StringRes
    private final int nameResId;

    @DrawableRes
    private final int resId;

    private final int index;

    ActionBean(int nameResId, int resId, int index) {
        this.nameResId = nameResId;
        this.resId = resId;
        this.index = index;
    }

    public int getNameResId() {
        return nameResId;
    }

    public int getResId() {
        return resId;
    }

    public int getIndex() {
        return index;
    }
}
