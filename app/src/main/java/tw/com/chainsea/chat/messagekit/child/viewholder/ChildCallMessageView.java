package tw.com.chainsea.chat.messagekit.child.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.bean.msg.content.CallContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.util.TimeUtil;


/**
 * Created by sunhui on 2017/10/25.
 */

public class ChildCallMessageView extends ChildMessageBubbleView<CallContent> {

//    @Nullable
//    @BindView(R.id.msgkit_call_text)
//    TextView tvContent;

    private tw.com.chainsea.chat.databinding.MsgkitCallBinding binding;
    public ChildCallMessageView(@NonNull ViewBinding binding) {
        super(binding);
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_call;
    }

    @Override
    protected void bindView(View itemView) {
        binding = tw.com.chainsea.chat.databinding.MsgkitCallBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
//        ButterKnife.bind(this, itemView);
//        tvContent = findView(R.id.msgkit_call_text);
    }

    @Override
    public void setGroupStatus() {
        super.setGroupStatus();
        if (super.getMessage().content() instanceof CallContent) {
            super.setTimeFrom(TimeUtil.INSTANCE.getHHmm(super.getMessage().getSendTime()));
//            timeTV.setText(TimeUtil.getHHmm(super.getMessage().getTime()));
        }
    }

    @Override
    public void setSingleStatus(MessageEntity item) {
        super.setSingleStatus(item);
        if (super.getMessage().content() instanceof CallContent) {
            super.setTimeFrom(TimeUtil.INSTANCE.getHHmm(super.getMessage().getSendTime()));
//            timeTV.setText(TimeUtil.getHHmm(super.getMessage().getTime()));
        }
    }

    @Override
    protected void bindContentView(CallContent callContent) {
//        CallMsgFormat format = (CallMsgFormat) getMessage().getFormat();
        binding.msgkitCallText.setText(KeyWordHelper.matcherSearchBackground(0xFFFFF039, callContent.getContent(), getKeyword()), TextView.BufferType.NORMAL);
//        tvContent.setText(format.getContent());
    }

//    @Override
//    protected boolean onBubbleLongClicked(float pressX, float pressY) {
//        showPopup((int)pressX, (int)pressY);
//        return false;
//    }

//    @Override
//    protected void onBubbleClicked() {
//        super.onBubbleClicked();
//        onImgClickListener.onClick(super.getMessage());
//    }


    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onImageClick(super.getMessage());
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {

    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
        showPopup((int) x, (int) y);
    }


    private void showPopup(int pressX, int pressY) {
        final MessageEntity msg = getMessage();
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onLongClick(msg, pressX, pressY);
        }
        /*
        final List<String> popupMenuItemList = new ArrayList<>();
        popupMenuItemList.clear();
        *//*if (message.getStatus() == MessageStatus.FAILED) {
            popupMenuItemList.add(getContext().getString(R.string.retry));
        }*//*
        popupMenuItemList.add(getContext().getString(R.string.item_del));
        PopupList popupList = new PopupList(view.getContext());
        popupList.showPopupListWindow(view, pressX, pressY, popupMenuItemList, new PopupList.PopupListListener() {
            @Override
            public boolean showPopupList(View adapterView, View contextView) {
                return true;
            }

            @Override
            public void onPopupListClick(View contextView, int position) {
                String s = popupMenuItemList.get(position);
                switch (s) {
                    case "刪除":
                        onListDilogItemClickListener.delete(message);
                        break;
                }
            }
        });*/
    }
}
