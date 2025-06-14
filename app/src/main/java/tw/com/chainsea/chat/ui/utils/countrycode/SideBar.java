/*
 * Copyright 2010 Beijing Xinwei, Inc. All rights reserved.
 *
 * History:
 * ------------------------------------------------------------------------------
 * Date    	|  Who  		|  What
 * 2015��3��18��	| duanbokan 	| 	create the file
 */

package tw.com.chainsea.chat.ui.utils.countrycode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import tw.com.chainsea.chat.R;

public class SideBar extends View {
    // 字母变化监听事件
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;

    // 字母数组
    public static String[] b = {"#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
        "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    // 选中
    private int choose = -1;

    private Paint paint = new Paint();

    // 点击后提示当前选中字母
    private TextView mTextDialog;

    public void setTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }

    public SideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SideBar(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        // 获取屏幕高度和宽度
        int height = getHeight();
        int width = getWidth();

        // 设置字母高度
        float letterHeight = (height * 1f) / b.length;
        for (int i = 0; i < b.length; i++) {
            //未选中字母颜色
            paint.setColor(Color.rgb(23, 122, 126));
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            // 抗锯齿
            paint.setAntiAlias(true);
            paint.setTextSize(20);

            if (i == choose) {
                //未选中字母颜色
                paint.setColor(Color.BLUE);
                // 设置为加粗字体
                paint.setFakeBoldText(true);
            }
            // x坐标等于中间-字符串宽度的一半.
            float xPos = width / 2f - paint.measureText(b[i]) / 2;
            float yPos = letterHeight * i + letterHeight;
            canvas.drawText(b[i], xPos, yPos, paint);
            // 重置画笔
            paint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float touch_y = event.getY();
        final int oldChoose = choose;

        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        // 点击y坐标所占总高度的比例*b数组的长度就等于点击b中的个数.
        final int c = (int) (touch_y / getHeight() * b.length);

        if (action == MotionEvent.ACTION_UP) {
            setBackgroundColor(Color.WHITE);
            choose = -1;
            invalidate();
            if (mTextDialog != null) {
                mTextDialog.setVisibility(View.INVISIBLE);
            }
        } else {//字母条顏色
            setBackgroundResource(R.drawable.sidebar_background);
            if (oldChoose != c) {
                if (c >= 0 && c < b.length) {
                    if (listener != null) {
                        listener.onTouchingLetterChanged(b[c]);
                    }
                    if (mTextDialog != null) {
                        mTextDialog.setText(b[c]);
                        mTextDialog.setVisibility(View.VISIBLE);
                    }
                    choose = c;
                    invalidate();
                }
            }
        }

        return true;
    }

    /**
     * 向外公开的方法
     *
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(
        OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    /**
     * 接口
     *
     * @author coder
     */
    public interface OnTouchingLetterChangedListener {
        void onTouchingLetterChanged(String s);
    }

}
