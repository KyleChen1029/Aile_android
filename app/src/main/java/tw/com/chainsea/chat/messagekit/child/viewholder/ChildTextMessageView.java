package tw.com.chainsea.chat.messagekit.child.viewholder;

import android.os.Handler;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.android.common.text.KeyWordHelper;
import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.MsgkitTextBinding;
import tw.com.chainsea.chat.util.VibratorKit;


/**
 * text message view
 * Created by 90Chris on 2016/4/20.
 */
public class ChildTextMessageView extends ChildMessageBubbleView<TextContent> {

    private MsgkitTextBinding binding;
    public ChildTextMessageView(@NonNull ViewBinding binding) {
        super(binding);
    }

    @Override
    protected int getContentResId() {
        return R.layout.msgkit_text;
    }

    @Override
    protected void bindView(View itemView) {
        binding = MsgkitTextBinding.inflate(LayoutInflater.from(itemView.getContext()), null, false);
    }

    @Override
    protected void bindContentView(TextContent textContent) {
        String content = textContent.getText();
        if ("%{}%".equals(content)) {
            content = "{}";
        }

        binding.contentCTV.setText(KeyWordHelper.matcherSearchBackground(0xFFFFF039, content, getKeyword()), TextView.BufferType.NORMAL);

        binding.contentCTV.setMovementMethod(new TextClickLinkMovementMethod<>(getMessage()) {
            @Override
            public void click(MessageEntity m) {
                onClick(null, m);
            }

            @Override
            public void doubleClick(MessageEntity m) {
                onDoubleClick(null, m);
            }

            @Override
            public void longClick(MessageEntity m) {
                onLongClick(null, 0f, 0f, m);
            }
        });
    }

    @Override
    protected boolean showName() {
        return !isRightMessage();
    }

    @Override
    public void onClick(View v, MessageEntity message) {
        super.onClick(v, message);
        if (this.onMessageControlEventListener != null) {
            if (itemMsgBubbleBinding.checkBox.getVisibility() == View.VISIBLE) {
                boolean isCheck = itemMsgBubbleBinding.checkBox.isChecked();
                itemMsgBubbleBinding.checkBox.setChecked(!isCheck);
            } else {
                if (MessageType.BUSINESS_TEXT.equals(getMessage().getType())) {
                    this.onMessageControlEventListener.onImageClick(getMessage());
                }
            }
        }
    }

    @Override
    public void onDoubleClick(View v, MessageEntity message) {
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.enLarge(getMessage());
        }
    }

    @Override
    public void onLongClick(View v, float x, float y, MessageEntity message) {
        if (this.onMessageControlEventListener != null) {
            this.onMessageControlEventListener.onLongClick(getMessage(), 0, 0);
        }
    }

    public abstract class TextClickLinkMovementMethod<T> extends LinkMovementMethod {
        private final T t;

        private int clickCount;
        private float downX;
        private float downY;
        private long lastDownTime;
        private long firstClick;

        private boolean isDoubleClick = false;

        private final Handler eventHandler = new Handler();
        TextView widget;
        ClickableSpan link;

        private final Runnable longPressTask = new Runnable() {
            @Override
            public void run() {
                clickCount = 0;
                VibratorKit.longClick();
                longClick(t);
            }
        };

        private final Runnable singleClickTask = new Runnable() {
            @Override
            public void run() {
                clickCount = 0;
                if (link != null) {
                    if (itemMsgBubbleBinding.checkBox.getVisibility() == View.VISIBLE) {
                        boolean isCheck = itemMsgBubbleBinding.checkBox.isChecked();
                        itemMsgBubbleBinding.checkBox.setChecked(!isCheck);
                    } else {
                        link.onClick(widget);
                    }
                    widget = null;
                    link = null;
                } else {
                    click(t);
                }
            }
        };

        public abstract void click(T t);

        public abstract void doubleClick(T t);

        public abstract void longClick(T t);

        public TextClickLinkMovementMethod(T t) {
            this.t = t;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            this.widget = widget;
            int MAX_LONG_PRESS_TIME = 500;
            int MAX_SINGLE_CLICK_TIME = 200;
            int MAX_MOVE_FOR_CLICK = 20;
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
                    eventHandler.removeCallbacks(singleClickTask);
                    if (!isDoubleClick)
                        eventHandler.postDelayed(longPressTask, MAX_LONG_PRESS_TIME);
                    if (1 == clickCount) {
                        firstClick = System.currentTimeMillis();
                    } else if (clickCount == 2) {
                        long secondClick = System.currentTimeMillis();
                        if (secondClick - firstClick <= MAX_LONG_PRESS_TIME) {
                            //處理雙擊
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
