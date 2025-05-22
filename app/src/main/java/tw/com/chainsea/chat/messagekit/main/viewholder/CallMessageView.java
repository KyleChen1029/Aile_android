package tw.com.chainsea.chat.messagekit.main.viewholder;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import org.json.JSONObject;

import java.util.Objects;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.msg.content.CallContent;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.CallStatus;
import tw.com.chainsea.chat.databinding.MsgkitCallBinding;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;
import tw.com.chainsea.chat.util.TimeUtil;


/**
 * Created by sunhui on 2017/10/25.
 */

public class CallMessageView extends MessageBubbleView<CallContent> {


    private final MsgkitCallBinding binding;

    public CallMessageView(@NonNull ViewBinding viewBinding) {
        super(viewBinding);
        this.binding = MsgkitCallBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
        getView(this.binding.getRoot());
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
    protected View getChildView() {
        return binding.getRoot();
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

        if(!item.getContent().isEmpty()) {
            try {
                JSONObject jsonObject = JsonHelper.getInstance().toJsonObject(item.getContent());
                String status = jsonObject.getString("status");
                if(status.equals(CallStatus.CallCancel.name())){
                    binding.scopePhoneCallDone.setVisibility(View.GONE);
                    binding.tvPhoneCancel.setVisibility(View.VISIBLE);
                    binding.tvPhoneCancel.setText(App.getContext().getString(R.string.text_phone_call_cancel));
                    binding.ivPhone.setImageResource(R.drawable.ic_phone_cancel);
                } else if (status.equals(CallStatus.CallEnd.name())) {
                    binding.scopePhoneCallDone.setVisibility(View.VISIBLE);
                    binding.msgkitCallText.setText(App.getContext().getString(R.string.text_phone_call_end_already));
                    int callTime = jsonObject.getInt("callTime");
                    binding.tvTime.setText(App.getContext().getString(R.string.text_time_format, callTime/60, callTime%60));
                    binding.tvPhoneCancel.setVisibility(View.GONE);
                    binding.ivPhone.setImageResource(R.drawable.ic_phone_done);
                } else if (status.equals(CallStatus.CallBusy.name())) {
                    binding.scopePhoneCallDone.setVisibility(View.GONE);
                    binding.tvPhoneCancel.setVisibility(View.VISIBLE);
                    binding.msgkitCallText.setText(Objects.equals(item.getSenderId(), userId) ? App.getContext().getString(R.string.text_phone_call_busy) : App.getContext().getString(R.string.text_phone_call_busy_myself));
                    binding.ivPhone.setImageResource(Objects.equals(item.getSenderId(), userId) ? R.drawable.ic_phone_busying : R.drawable.ic_phone_busying2);
                }
            } catch (Exception e){
                CELog.e("CallMessageView setSingleStatus error="+e.getMessage());
            }
        }
//        CELog.d("Kyle1 content = "+item.getContent()+", senderId="+item.getSenderId()+", name="+item.getSenderName());
    }

    @Override
    protected void bindContentView(CallContent callContent) {
//        CallMsgFormat format = (CallMsgFormat) getMessage().getFormat();
//        tvContent.setText(KeyWordHelper.matcherSearchBackground(0xFFFFF039, callContent.getContent(), getKeyword()), TextView.BufferType.NORMAL);
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
