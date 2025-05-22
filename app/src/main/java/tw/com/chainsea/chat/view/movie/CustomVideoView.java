package tw.com.chainsea.chat.view.movie;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * current by evan on 2020-01-14
 */
public class CustomVideoView extends VideoView {
    /**
     *     声明屏幕的大小
     */

    int width = 1920;
    int height = 1080;
    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //设置宽高
        int defaultWidth = getDefaultSize(width,widthMeasureSpec);
        int defaultHeight = getDefaultSize(height,heightMeasureSpec);
        setMeasuredDimension(defaultWidth,defaultHeight);
    }
}
