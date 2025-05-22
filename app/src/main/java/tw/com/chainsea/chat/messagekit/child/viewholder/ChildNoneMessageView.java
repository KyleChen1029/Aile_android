package tw.com.chainsea.chat.messagekit.child.viewholder;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemMsgTipBinding;


/**
 * current by evan on 2019-12-02
 */
public class ChildNoneMessageView extends ChildMessageViewBase {
    ItemMsgTipBinding msgTipBinding;

    public ChildNoneMessageView(@NonNull ItemMsgTipBinding binding) {
        super(binding);
        msgTipBinding = binding;
    }

    @Override
    protected void findViews(View itemView) {

    }

    @Override
    public void refresh(MessageEntity item, boolean isCache) {
        msgTipBinding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_bg);
        msgTipBinding.msgkitTipContent.setTextColor(Color.WHITE);
        msgTipBinding.msgkitTipContent.setText("暫不支持此消息類型");
    }
}
