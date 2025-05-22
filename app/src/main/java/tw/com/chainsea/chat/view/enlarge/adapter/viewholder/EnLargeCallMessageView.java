package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import androidx.annotation.NonNull;
import android.view.View;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.CallContent;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * @date 2020-04-15
 */
public class EnLargeCallMessageView extends EnLargeMessageBaseView<CallContent> {

    public EnLargeCallMessageView(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void onBind(MessageEntity entity, CallContent callContent, int position) {

    }
}
