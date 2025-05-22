package tw.com.chainsea.chat.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import tw.com.chainsea.chat.R;

/**
 * Created by sunhui on 2017/5/12.
 */

public class DoubleTextView extends RelativeLayout {

    private int tv_title_tv_size;
    private String mTitle;
    private int mTitleColor;
    private String mContent;
    private boolean isShowIcon;
    private TextView mTv_title;
    private TextView mTv_content;
    private ImageView mRightIcon;

    private OnEditClickListenner mOnEditClickListenner;

    public DoubleTextView(Context context) {
        super(context);
    }

    public DoubleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DoubleTextView);
        mTitle = a.getString(R.styleable.DoubleTextView_tv_title);
//        tv_title_tv_size = a.getDimensionPixelSize(attrs, sp2px(context, 18));
        mContent = a.getString(R.styleable.DoubleTextView_tv_content);
        isShowIcon = a.getBoolean(R.styleable.DoubleTextView_show_right_icon, false);
        Drawable drawable = a.getDrawable(R.styleable.DoubleTextView_right_icon);
        View view = View.inflate(context, R.layout.double_text_view, null);

        mTv_title = (TextView) view.findViewById(R.id.tv_title);
        mTv_content = (TextView) view.findViewById(R.id.tv_content);
        mRightIcon = (ImageView) view.findViewById(R.id.right_icon);

        mRightIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnEditClickListenner.onEdit(DoubleTextView.this);
            }
        });

        mTv_title.setText(mTitle);
        mTv_content.setText(mContent);
        mRightIcon.setImageDrawable(drawable);
        showRightIcon();
        addView(view);
        a.recycle();
    }

    private void showRightIcon() {
        if (isShowIcon) {
            mRightIcon.setVisibility(VISIBLE);
        } else {
            mRightIcon.setVisibility(GONE);
        }
    }

    public void setRightIcon(Drawable icon) {
        mRightIcon.setImageDrawable(icon);
    }

    public void setRightIcon(int resId) {
        mRightIcon.setImageResource(resId);
    }


    public DoubleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
        mTv_title.setText(mTitle);
    }

    public void setTitleTVColor(int titleTVColorolor) {
        mTitleColor = titleTVColorolor;
        mTv_title.setTextColor(titleTVColorolor);
    }

    public void setTitleTvSize(int tv_title_tv_size) {
        this.tv_title_tv_size = tv_title_tv_size;
        mTv_title.setTextSize(tv_title_tv_size);
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
        mTv_content.setText(mContent);
    }


    public boolean isShowIcon() {
        return isShowIcon;
    }

    public void setShowIcon(boolean showIcon) {
        isShowIcon = showIcon;
        showRightIcon();
    }

    public void setOnEditClickListenner(OnEditClickListenner onEditClickListenner) {
        mOnEditClickListenner = onEditClickListenner;
    }

    public interface OnEditClickListenner {
        void onEdit(View view);
    }

    private static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
}
