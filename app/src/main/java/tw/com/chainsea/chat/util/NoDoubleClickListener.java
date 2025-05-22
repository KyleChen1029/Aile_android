package tw.com.chainsea.chat.util;


import android.view.View;

import java.util.Calendar;

public abstract class NoDoubleClickListener implements View.OnClickListener {
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

    protected abstract void onNoDoubleClick(View v);
    @Override
    public void onClick(View v) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if(currentTime - lastClickTime > MIN_CLICK_DELAY_TIME){
            lastClickTime = currentTime;
            onNoDoubleClick(v);
        }
    }
}
