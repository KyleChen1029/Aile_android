package tw.com.chainsea.chat.messagekit.main.viewholder;

import static tw.com.chainsea.chat.messagekit.main.viewholder.Constant.ActionType;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewbinding.ViewBinding;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.msg.TemplateContent;
import tw.com.chainsea.ce.sdk.bean.msg.TemplateElementAction;
import tw.com.chainsea.ce.sdk.bean.msg.content.Action;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.aiff.AiffManager;
import tw.com.chainsea.chat.databinding.ItemMsgBubbleBinding;
import tw.com.chainsea.chat.messagekit.main.adapter.TemplateMultiAdapter;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;

public class TemplateMessageView extends MessageBubbleView<TemplateContent> {

    private ItemMsgBubbleBinding binding;

    public TemplateMessageView(@NonNull ViewBinding binding) {
        super(binding);
        this.binding = (ItemMsgBubbleBinding) binding;
//        this.binding = MsgkitTemplateBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
//        getView(this.binding.getRoot());
    }

    @Override
    protected boolean showName() {
        return false;
    }

    public TemplateMessageView(ViewBinding binding, boolean isReply) {
        super(binding);
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_template;
    }

    @Override
    protected View getChildView() {
        return binding.getRoot();
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {

    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {

    }

    @Override
    protected void bindContentView(TemplateContent templateContent) {
        if (templateContent == null) return;
        binding.rvMultiTemplateList.setVisibility(View.VISIBLE);
        binding.rvMultiTemplateList.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvMultiTemplateList.setAdapter(new TemplateMultiAdapter(templateContent, action -> {
            if (action instanceof TemplateElementAction) {
                new ActionClick((TemplateElementAction) action, binding.getRoot().getContext()).onClick(binding.rvMultiTemplateList);
            } else if (action instanceof Action) {
                new ActionClick((Action) action, binding.getRoot().getContext()).onClick(binding.rvMultiTemplateList);
            }
            return null;
        }, templateElement -> {
            if (onMessageControlEventListener != null) {
                onMessageControlEventListener.onLongClick(getMessage(), 0, 0);
            }
            return null;
        }));
//        if (Objects.equals(templateContent.getTemplateType(), "carousel")) {
//            binding.llSingleTemplate.setVisibility(View.GONE);
//            binding.rvMultiTemplateList.setVisibility(View.VISIBLE);
//            binding.rvMultiTemplateList.setLayoutManager(new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false));
//            binding.rvMultiTemplateList.setAdapter(new TemplateMultiAdapter(
//                    templateContent.getElements(), templateElementAction -> {
//                        new ActionClick(templateElementAction, binding.getRoot().getContext()).onClick(binding.rvMultiTemplateList);
//                        return null;
//                    }, templateElement -> {
//                        onLongClick(null, 0f, 0f,null);
//                        return null;
//            }));
//        } else {
//            binding.llSingleTemplate.setVisibility(View.VISIBLE);
//            binding.rvMultiTemplateList.setVisibility(View.GONE);
//            Glide.with(binding.img)
//                    .load(templateContent.getImageUrl())
//                    .apply(new RequestOptions().centerCrop())
//                    .into(binding.img);
//            binding.txtTitle.setText(templateContent.getTitle());
//            binding.txtContent.setText(templateContent.getText());
//
//            Action defaultAction = templateContent.getDefaultAction();
//            if (defaultAction != null) {
//                binding.img.setOnClickListener(new ActionClick(defaultAction, binding.getRoot().getContext()));
//                binding.txtContent.setOnClickListener(new ActionClick(defaultAction, binding.getRoot().getContext()));
//            }
//
//            List<Action> actions = templateContent.getActions();
//            if (actions != null && !actions.isEmpty()) {
//                binding.lineTop.setVisibility(View.VISIBLE);
//                String orientation = templateContent.getOrientation();
//                if (Orientation.VERTICAL.equals(orientation)) {
//                    binding.layoutButtonV.setVisibility(View.VISIBLE);
//                    binding.layoutButtonH.setVisibility(View.GONE);
//                    binding.line0V.setVisibility(View.GONE);
//                    binding.line1V.setVisibility(View.GONE);
//                    binding.btn0V.setVisibility(View.GONE);
//                    binding.btn1V.setVisibility(View.GONE);
//                    binding.btn2V.setVisibility(View.GONE);
//                    switch (actions.size()) {
//                        case 3:
//                            binding.line1V.setVisibility(View.VISIBLE);
//                            binding.btn2V.setVisibility(View.VISIBLE);
//                            binding.btn2V.setText(actions.get(2).getLabel());
//                            binding.btn2V.setOnClickListener(
//                                    new ActionClick(actions.get(2), binding.getRoot().getContext()));
//                        case 2:
//                            binding.line0V.setVisibility(View.VISIBLE);
//                            binding.btn1V.setVisibility(View.VISIBLE);
//                            binding.btn1V.setText(actions.get(1).getLabel());
//                            binding.btn1V.setOnClickListener(
//                                    new ActionClick(actions.get(1), binding.getRoot().getContext()));
//                        case 1:
//                            binding.btn0V.setVisibility(View.VISIBLE);
//                            binding.btn0V.setText(actions.get(0).getLabel());
//                            binding.btn0V.setOnClickListener(
//                                    new ActionClick(actions.get(0), binding.getRoot().getContext()));
//                    }
//                } else {
//                    binding.layoutButtonV.setVisibility(View.VISIBLE);
//                    binding.layoutButtonH.setVisibility(View.GONE);
//                    binding.line0H.setVisibility(View.GONE);
//                    binding.line1H.setVisibility(View.GONE);
//                    binding.btn0H.setVisibility(View.GONE);
//                    binding.btn1H.setVisibility(View.GONE);
//                    binding.btn2H.setVisibility(View.GONE);
//                    switch (actions.size()) {
//                        case 3:
//                            binding.line1H.setVisibility(View.VISIBLE);
//                            binding.btn2H.setVisibility(View.VISIBLE);
//                            binding.btn2H.setText(actions.get(2).getLabel());
//                            binding.btn2H.setOnClickListener(
//                                    new ActionClick(actions.get(2), binding.getRoot().getContext()));
//                        case 2:
//                            binding.line0H.setVisibility(View.VISIBLE);
//                            binding.btn1H.setVisibility(View.VISIBLE);
//                            binding.btn1H.setText(actions.get(1).getLabel());
//                            binding.btn1H.setOnClickListener(
//                                    new ActionClick(actions.get(1), binding.getRoot().getContext()));
//                        case 1:
//                            binding.btn0H.setVisibility(View.VISIBLE);
//                            binding.btn0H.setText(actions.get(0).getLabel());
//                            binding.btn0H.setOnClickListener(
//                                    new ActionClick(actions.get(0), binding.getRoot().getContext()));
//                    }
//                }
//            } else {
//                binding.layoutButtonH.setVisibility(View.GONE);
//                binding.layoutButtonV.setVisibility(View.GONE);
//                binding.lineTop.setVisibility(View.GONE);
//            }
//        }
    }

    public class ActionClick implements View.OnClickListener {
        private final Action action;
        private final TemplateElementAction mTemplateElementAction;
        private final WeakReference<Context> context;

        public ActionClick(Action action, Context context) {
            this.action = action;
            this.mTemplateElementAction = null;
            this.context = new WeakReference<>(context);
        }

        public ActionClick(TemplateElementAction templateElementAction, Context context) {
            this.action = null;
            this.mTemplateElementAction = templateElementAction;
            this.context = new WeakReference<>(context);
        }

        @Override
        public void onClick(View v) {
            String type = "";
            if (action == null) {
                type = mTemplateElementAction.getType();
            } else {
                type = action.getType();
            }


            switch (type) {
                case ActionType.AIFF:
                    if (!action.getUrl().contains("aiff.aile.com")) {
//                       if(false){
//                           getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(action.getUrl())));
//                           EventBusUtils.sendEvent(new EventMsg(MsgConstant.SHOW_AIFF_WEBVIEW, action.getUrl()));
                        // 如果取得的 Url 是 liff, 直接使用 aiffManager 打開連結
                        String roomId = getMessage().getRoomId();
                        AiffManager aiffManager = new AiffManager(context.get(), roomId);
                        aiffManager.addAiffWebView(action.getUrl());
                        break;
                    }
                    try {
                        URI uri = new URI(action.getUrl());
                        String[] segments = uri.getPath().split("/");
                        String id = segments[segments.length - 1];
                        if (id.contains("?")) {
                            String str1 = id.substring(0, id.indexOf("?"));
                            id = id.substring(str1.length() + 1);
                        }
                        String roomId = UserPref.getInstance(context.get()).getCurrentRoomId();
                        AiffManager aiffManager = new AiffManager(context.get(), roomId);
                        aiffManager.showAiffById(id);
                    } catch (URISyntaxException ignored) {
                    }
                    break;
                case ActionType.LINK: //取得服務機器人連結
//                    if(onRobotChatMessageClickListener != null) {
//                        onRobotChatMessageClickListener.onLinkClickListener(action.getLink());
//                    }
                    break;
                case ActionType.POSTBACK:
                    if(mOnTemplateClickListener != null)
                        mOnTemplateClickListener.onTemplateClick(JsonHelper.getInstance().toJson(mTemplateElementAction));
                    break;
                default:
                    String url = "";
                    if (action == null) {
                        url = mTemplateElementAction.getUrl();
                    } else {
                        url = action.getUrl();
                    }
                    context.get().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        }
    }

//    @Override
//    protected boolean showName() {
//        return !isRightMessage();
//    }
//
//    @Override
//    public void onClick(View v, MessageEntity message) {
//        super.onClick(v, message);
//        if (this.onMessageControlEventListener != null) {
//
//        }
//    }
//
//    @Override
//    public void onDoubleClick(View v, MessageEntity message) {
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.enLarge(getMessage());
//        }
//    }
//
//    @Override
//    public void onLongClick(View v, float x, float y, MessageEntity message) {
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.onLongClick(getMessage(), 0, 0);
//        }
//    }


}
