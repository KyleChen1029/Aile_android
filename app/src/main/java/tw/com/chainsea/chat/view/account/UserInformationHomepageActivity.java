package tw.com.chainsea.chat.view.account;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.chat.R;

/**
 * current by evan on 2020-11-10
 *
 * @author Evan Wang
 * date 2020-11-10
 */
public abstract class UserInformationHomepageActivity extends AppCompatActivity {

    private int statusHeight = 0;

    protected UserProfileEntity profile;

    protected PageMode pageMode = PageMode.MAIN;

    public enum TabType {
        ROOM_LIST(0, R.string.home_page_tab_chats),
        PASTE(1, R.string.home_page_tab_article),
        SETTING(2, R.string.home_page_tab_setting),
        SELF_PROFILE(3, R.string.home_page_tab_self_profile),
        SYSTEM_SETTING(3, R.string.home_page_tab_system_setting),
        CHANGE_TENANT(3, R.string.home_page_tab_switch_tenant);

        private final int index;

        @StringRes
        private final int nameResId;

        TabType(int index, int nameResId) {
            this.index = index;
            this.nameResId = nameResId;
        }

        public int getIndex() {
            return index;
        }

        public int getNameResId() {
            return nameResId;
        }
    }

    public enum PageMode {
        MAIN,
        SETTING
    }

    @Override
    @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS); // 確認取消半透明設置。
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN // 全螢幕顯示，status bar 不隱藏，activity 上方 layout 會被 status bar 覆蓋。
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); // 配合其他 flag 使用，防止 system bar 改變後 layout 的變動。
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); // 跟系統表示要渲染 system bar 背景。
        window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.WHITE);

        int resourceId = getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            this.statusHeight = getApplicationContext().getResources().getDimensionPixelSize(resourceId);
        } else {
            this.statusHeight = UiHelper.dip2px(this, 24);
        }

    }

    protected int getStatusHeight() {
        return this.statusHeight;
    }

    protected abstract void switchMode(PageMode pageMode);
}
