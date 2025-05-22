package tw.com.chainsea.chat.messagekit.main.viewholder;

import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import java.util.List;
import java.util.Map;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
import tw.com.chainsea.ce.sdk.bean.msg.content.MentionContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.MsgkitAtBinding;
import tw.com.chainsea.chat.lib.AtMatcherHelper;
import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageBubbleView;
import tw.com.chainsea.chat.util.VibratorKit;

/**
 * current by evan on 2019-11-28
 */
public class AtMessageView extends MessageBubbleView<AtContent> {
    private static final String TAG = AtMessageView.class.getSimpleName();


    private final MsgkitAtBinding binding;

    public AtMessageView(@NonNull ViewBinding binding) {
        super(binding);
        this.binding = MsgkitAtBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
        getView(this.binding.getRoot());
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_at;
    }

    @Override
    protected View getChildView() {
        return binding.getRoot();
    }


    @Override
    protected void bindContentView(AtContent atContent) {
        MessageEntity message = getMessage();
        Map<String, String> membersTable = getMembersTable();
        if (isAnonymous) {
            membersTable = getAnonymousMembersTable();
        }

        if (message.content() instanceof AtContent) {
            List<MentionContent> ceMentions = atContent.getMentionContents();
            binding.contentCTV.setText(AtMatcherHelper.matcherAtUsers("@", ceMentions, membersTable, id -> {
                if (checkBox.getVisibility() == View.VISIBLE) {
                    boolean isCheck = checkBox.isChecked();
                    checkBox.setChecked(!isCheck);
                } else {
                    if (!getUserId().equals(id)) {
                        if (this.onMessageControlEventListener != null) {
                            this.onMessageControlEventListener.onAtSpanClick(id);
                        }
                    }
                }
            }));

            binding.contentCTV.setMovementMethod(new AtClickLinkMovementMethod<>(getMessage()) {
                @Override
                public void click(MessageEntity m) {
                    if (checkBox.getVisibility() == View.VISIBLE) {
                        boolean isCheck = checkBox.isChecked();
                        checkBox.setChecked(!isCheck);
                    }
                }

                @Override
                public void doubleClick(MessageEntity m) {
                    Log.i(TAG, "");
                }

                @Override
                public void longClick(MessageEntity m) {
                    onLongClick(null, 0f, 0f, m);
                }
            });
            return;
        }
        binding.contentCTV.setText(new SpannableStringBuilder("[標註訊息]"));
    }


    @Override
    protected boolean showName() {
        return !isRightMessage();
    }


    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
        if (checkBox.getVisibility() == View.VISIBLE) {
            boolean isCheck = checkBox.isChecked();
            checkBox.setChecked(!isCheck);
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {
    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onLongClick(getMessage(), 0, 0);
        }
    }


    public interface OnAtClickSpanClickListener<T> extends SpanClickListener<T> {
        void atClick(T t);
    }

    public interface SpanClickListener<T> {
        void atClick(T t);
    }

    public abstract static class AtClickLinkMovementMethod<T> extends LinkMovementMethod {
        private final T t;
        private int clickCount; // 点击次数
        private float downX;
        private float downY;

        private long lastDownTime;
        private long firstClick;

        private boolean isDoubleClick = false;

        private final Handler eventHandler = new Handler();
        TextView widget;
        ClickableSpan link;

        // Handle long press
        private final Runnable longPressTask = new Runnable() {
            @Override
            public void run() {
                clickCount = 0;
                VibratorKit.longClick();
                longClick(t);
            }
        };

        // Handle click
        private final Runnable singleClickTask = new Runnable() {
            @Override
            public void run() {
                clickCount = 0;
                if (link != null) {
                    link.onClick(widget);
                    widget = null;
                    link = null;
                } else {
                    click(t);
                }
            }
        };

        // Click event
        public abstract void click(T t);

        // Double Click event
        public abstract void doubleClick(T t);

        // Long press event
        public abstract void longClick(T t);

        public AtClickLinkMovementMethod(T t) {
            this.t = t;
        }


        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            int MAX_LONG_PRESS_TIME = 500;// Long press / double tap the longest waiting time
            int MAX_SINGLE_CLICK_TIME = 200;// Click the longest wait time
            int MAX_MOVE_FOR_CLICK = 20;// The longest change distance, more than it is considered as moving
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
                    } else if (clickCount == 2) {
                        long secondClick = System.currentTimeMillis();
                        if (secondClick - firstClick <= MAX_LONG_PRESS_TIME) {
                            VibratorKit.doubleClick();
                            doubleClick(this.t);
                            isDoubleClick = true;
                            clickCount = 0;
                            firstClick = 0;
                            eventHandler.removeCallbacks(singleClickTask);
                            eventHandler.removeCallbacks(longPressTask);
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    eventHandler.removeCallbacks(singleClickTask);
                    eventHandler.removeCallbacks(longPressTask);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveY = event.getY();
                    float absMy = Math.abs(moveY - downY);
                    if (absMy > MAX_MOVE_FOR_CLICK) {
                        eventHandler.removeCallbacks(longPressTask);
                        eventHandler.removeCallbacks(singleClickTask);
                        isDoubleClick = false;
                        clickCount = 0;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    long lastUpTime = System.currentTimeMillis();
                    float upY = event.getY();
                    float my = Math.abs(upY - downY);
                    if (my <= MAX_MOVE_FOR_CLICK) {
                        if ((lastUpTime - lastDownTime) <= MAX_LONG_PRESS_TIME) {
                            eventHandler.removeCallbacks(longPressTask);
                            if (!isDoubleClick)
                                eventHandler.postDelayed(singleClickTask, MAX_SINGLE_CLICK_TIME);
                        } else {
                            clickCount = 0;
                        }
                    } else {
                        eventHandler.removeCallbacks(longPressTask);
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
