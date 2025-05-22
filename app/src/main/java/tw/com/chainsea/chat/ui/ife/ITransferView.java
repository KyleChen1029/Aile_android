package tw.com.chainsea.chat.ui.ife;

import java.util.List;

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.chat.base.IBaseView;


/**
 * IMultiChoiceView
 * Created by Fleming on 2016/9/13.
 */
public interface ITransferView extends IBaseView {
    void displayContact(List<ChatRoomEntity> sessions);

    void finishActivity();

    String getRoomId();
}
