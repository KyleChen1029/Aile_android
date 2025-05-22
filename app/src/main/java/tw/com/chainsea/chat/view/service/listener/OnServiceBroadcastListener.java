package tw.com.chainsea.chat.view.service.listener;

import tw.com.chainsea.chat.keyboard.listener.OnNewChatRoomToolbarListener;
import tw.com.chainsea.chat.keyboard.listener.OnNewKeyboardListener;
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageScrollStatusListener;

/**
 * current by evan on 2020-07-28
 *
 * @author Evan Wang
 * @date 2020-07-28
 */
public interface OnServiceBroadcastListener<T> extends OnNewChatRoomToolbarListener<T>, OnNewKeyboardListener, OnMainMessageScrollStatusListener {
}
