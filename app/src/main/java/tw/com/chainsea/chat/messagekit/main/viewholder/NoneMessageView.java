package tw.com.chainsea.chat.messagekit.main.viewholder;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemMsgTipBinding;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageViewBase;


/**
 * current by evan on 2019-12-02
 */
public class NoneMessageView extends MessageViewBase {

    private ItemMsgTipBinding binding;

    public NoneMessageView(@NonNull ItemMsgTipBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        getView(binding.getRoot());
    }

    @Override
    protected void findViews(View itemView) {
    }

    @Override
    public void refresh(MessageEntity item, boolean isCache) {
        binding.msgkitTipContent.setBackgroundResource(R.drawable.sys_msg_bg);
        binding.msgkitTipContent.setTextColor(Color.WHITE);
        binding.msgkitTipContent.setText("暫不支持此消息類型");
    }
}
