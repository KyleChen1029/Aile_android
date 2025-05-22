package tw.com.chainsea.custom.view.layout.refresh.view;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * current by evan on 2019-12-26
 */
public class TextRefreshView extends AppCompatTextView implements IRefreshView {

    public TextRefreshView(Context context) {
        this(context, "Pull on Refresh");
    }

    public TextRefreshView(Context context, String pullMessage) {
        super(context);
        setText(pullMessage);
        setTextSize(12.0f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onPull(int offset, int total, int overPull) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void doRefresh() {

    }

}