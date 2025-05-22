package tw.com.chainsea.custom.view.layout.refresh.view;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * current by evan on 2019-12-26
 */
public class NoneRefreshView extends AppCompatTextView implements IRefreshView {
    public NoneRefreshView(Context context) {
        super(context);
    }

    @Override
    public void stop() {

    }

    @Override
    public void doRefresh() {

    }

    @Override
    public void onPull(int offset, int total, int overPull) {

    }
}