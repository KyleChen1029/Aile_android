package tw.com.chainsea.chat.view.roomList.mainRoomList.listener;

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.chat.view.chatroom.adapter.listener.OnRoomItemClickListener;

/**
 * current by evan on 2020-11-06
 *
 * @author Evan Wang
 * @date 2020-11-06
 */
public interface OnRoomItem3ClickListener<CR extends ChatRoomEntity> extends OnRoomItemClickListener<CR> {

    void onChildItemClick(CR cr, String key);
}
