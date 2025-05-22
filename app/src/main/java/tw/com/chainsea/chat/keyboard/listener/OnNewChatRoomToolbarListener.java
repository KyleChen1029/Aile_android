package tw.com.chainsea.chat.keyboard.listener;

import android.view.View;

/**
 * current by evan on 2020-07-09
 *
 * @author Evan Wang
 * date 2020-07-09
 */
public interface OnNewChatRoomToolbarListener<T> {

    void onBackClick(T t, View view);

    void onTitleBoxClick(T t ,View view);

//    void onTitleClick(T t, View view);
//
//    void onSmallTitleClick(T t, View view);

    void onPenClick(T t, View view);

    void onChannelClick(T t, View view);

    void onSearchClick(T t, View view);

    void onInviteClick(T t, View view);

    void onCallClick(T t, View view);

    void onDropDownClick(T t, View view);


}
