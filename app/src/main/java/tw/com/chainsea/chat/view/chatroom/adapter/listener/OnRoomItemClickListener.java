package tw.com.chainsea.chat.view.chatroom.adapter.listener;

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;

/**
 * current by evan on 2020-04-08
 *
 * @author Evan Wang
 * date 2020-04-08
 */
public interface OnRoomItemClickListener<CR extends ChatRoomEntity> {
    void onItemClick(CR cr);

    void onComponentItemClick(CR cr);
}
