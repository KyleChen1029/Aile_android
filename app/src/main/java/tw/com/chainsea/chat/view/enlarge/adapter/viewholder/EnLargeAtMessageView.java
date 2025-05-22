package tw.com.chainsea.chat.view.enlarge.adapter.viewholder;

import android.content.Context;
import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.MentionContent;
import tw.com.chainsea.chat.databinding.ItemEnLargeAtMessageBinding;
import tw.com.chainsea.chat.lib.AtMatcherHelper;
import tw.com.chainsea.chat.util.VibratorKit;
import tw.com.chainsea.chat.view.enlarge.adapter.viewholder.base.EnLargeMessageBaseView;

/**
 * current by evan on 2020-04-15
 *
 * @author Evan Wang
 * @date 2020-04-15
 */
public class EnLargeAtMessageView extends EnLargeMessageBaseView<AtContent> {
    ItemEnLargeAtMessageBinding enLargeAtMessageBinding;

    public EnLargeAtMessageView(@NonNull ItemEnLargeAtMessageBinding binding) {
        super(binding.getRoot());
        enLargeAtMessageBinding = binding;
    }

    @Override
    public void onBind(MessageEntity entity, AtContent atContent, int position) {

        Map<String, String> membersTable = getMembersTable();
        List<MentionContent> ceMentions = atContent.getMentionContents();
        enLargeAtMessageBinding.tvText.setText(AtMatcherHelper.matcherAtUsers("@", ceMentions, membersTable, id -> {
//                if (checkBox.getVisibility() == View.VISIBLE) {
//                    boolean isCheck = checkBox.isChecked();
//                    checkBox.setChecked(!isCheck);
//                } else {
//                    if (!getUserId().equals(id)) {
//                        if (this.onMessageControlEventListener!= null){
//                            this.onMessageControlEventListener.onAtSpanClick(id);
//                        }
//                    }
//                }
        }));

        enLargeAtMessageBinding.tvText.setMovementMethod(new AtClickLinkMovementMethod<MessageEntity>(context, true, entity) {
            @Override
            public void click(MessageEntity m) {
//                    if (checkBox.getVisibility() == View.VISIBLE) {
//                        boolean isCheck = checkBox.isChecked();
//                        checkBox.setChecked(!isCheck);
//                    }
            }

            @Override
            public void doubleClick(MessageEntity m) {
//                    Log.i(TAG, "");
            }

            @Override
            public void longClick(MessageEntity m) {
//                    onLongClick(null, 0f, 0f, m);
            }
        });


    }


    public abstract static class AtClickLinkMovementMethod<T> extends LinkMovementMethod {
        private T t;
        private Context ctx;

        private boolean isFeedback = false;

        private int clickCount; // 点击次数
        private float downX;
        private float downY;
        private float moveX;
        private float moveY;
        private float upX;
        private float upY;

        private long lastDownTime;
        private long lastUpTime;
        private long firstClick;
        private long secondClick;

        private boolean isDoubleClick = false;
        private int MAX_LONG_PRESS_TIME = 500;// 长按/双击最长等待时间
        private int MAX_SINGLE_CLICK_TIME = 200;// 单击最长等待时间
        private int MAX_MOVE_FOR_CLICK = 20;// 最长改变距离,超过则算移动

        private Handler eventHandler = new Handler();
        TextView widget;
        ClickableSpan link;

        // 處理長按
        private Runnable longPressTask = new Runnable() {
            @Override
            public void run() {
                clickCount = 0;
                VibratorKit.longClick();
                longClick(t);
            }
        };

        // 處理單擊
        private Runnable singleClickTask = new Runnable() {
            @Override
            public void run() {
                clickCount = 0;
//            triggerSensoryFeedback(new long[]{10, 100});
                if (link != null) {
                    link.onClick(widget);
                    widget = null;
                    link = null;
                } else {
                    click(t);
                }
            }
        };

        // 單擊事件
        public abstract void click(T t);

        // 雙點事件
        public abstract void doubleClick(T t);

        // 長按事件
        public abstract void longClick(T t);

        public AtClickLinkMovementMethod(Context ctx, boolean isFeedback, T t) {
            this.ctx = ctx;
            this.isFeedback = isFeedback;
            this.t = t;
        }


        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            this.widget = widget;
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);
                if (links.length != 0) {
                    link = links[0];
                }
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    eventHandler.removeCallbacks(longPressTask);
                    lastDownTime = System.currentTimeMillis();
//                closeOpenMenu();
                    downX = event.getX();
                    downY = event.getY();
                    clickCount++;
                    if (singleClickTask != null) {
                        eventHandler.removeCallbacks(singleClickTask);
                    }
                    if (!isDoubleClick)
                        eventHandler.postDelayed(longPressTask, MAX_LONG_PRESS_TIME);
                    if (1 == clickCount) {
                        firstClick = System.currentTimeMillis();
                    } else if (clickCount == 2) { // 雙擊
                        secondClick = System.currentTimeMillis();
                        if (secondClick - firstClick <= MAX_LONG_PRESS_TIME) {
                            //處理雙擊
                            VibratorKit.doubleClick();
                            doubleClick(this.t);
                            isDoubleClick = true;
                            clickCount = 0;
                            firstClick = 0;
                            secondClick = 0;
                            eventHandler.removeCallbacks(singleClickTask);
                            eventHandler.removeCallbacks(longPressTask);
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    // 如果是 Scroll View 在滑動時
                    eventHandler.removeCallbacks(singleClickTask);
                    eventHandler.removeCallbacks(longPressTask);
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveX = event.getX();
                    moveY = event.getY();
                    float absMx = Math.abs(moveX - downX);
                    float absMy = Math.abs(moveY - downY);
                    if (absMy > MAX_MOVE_FOR_CLICK) {
                        eventHandler.removeCallbacks(longPressTask);
                        eventHandler.removeCallbacks(singleClickTask);
                        isDoubleClick = false;
                        clickCount = 0; // 移動了
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    lastUpTime = System.currentTimeMillis();
                    upX = event.getX();
                    upY = event.getY();


                    float mx = Math.abs(upX - downX);
                    float my = Math.abs(upY - downY);
                    if (my <= MAX_MOVE_FOR_CLICK) {
                        if ((lastUpTime - lastDownTime) <= MAX_LONG_PRESS_TIME) {
                            eventHandler.removeCallbacks(longPressTask);
                            if (!isDoubleClick)
                                eventHandler.postDelayed(singleClickTask, MAX_SINGLE_CLICK_TIME);
                        } else {
                            // 超出雙擊時間區間
                            clickCount = 0;
                        }
                    } else {
                        eventHandler.removeCallbacks(longPressTask);
                        // 移動了
                        clickCount = 0;
                        downX = 0.0f;
                        return true;
                    }
                    if (isDoubleClick) isDoubleClick = false;
                    break;
            }
            return true;
        }
    }
}
