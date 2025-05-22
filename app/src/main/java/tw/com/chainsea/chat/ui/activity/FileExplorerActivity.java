package tw.com.chainsea.chat.ui.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.messagekit.lib.FileUtil;
import tw.com.chainsea.chat.messagekit.lib.MToolBox;
import tw.com.chainsea.chat.ui.fragment.FileExplorerFragment;


public class FileExplorerActivity extends FragmentActivity {
    public final String TAG = "FileExplorerActivity";

    private FileExplorerFragment mFragment;


    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int themeColor = getIntent().getIntExtra(BundleKey.THEME_COLOR.key(), 0);
        if (themeColor != 0) {
            getWindow().setStatusBarColor(themeColor);
            binding.include.clToolBar.setBackgroundColor(themeColor);
        }
    }

    @Override
    protected Fragment createFragment() {
        int limit = getIntent().getIntExtra(BundleKey.LIMIT_MAX.key(), 5);
        mFragment = FileExplorerFragment.newInstance(limit);
        return mFragment;
    }

    @Override
    protected View rightView() {
        return null;
    }

    @Override
    protected String getTitleText() {
        return "";
    }

    private void dealBack() {
        if (FileUtil.getCurPath().equals(MToolBox.getSdcardPath())) {
            finish();
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        } else {
            String l_Path = FileUtil.getParentPath();
            mFragment.updateFileList(l_Path);
        }
    }

//    @Override
//    public void onBackPressed() {
//        dealBack();
//        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
//    }

    @Override
    public void doLeftAction(View view) {
//        super.doLeftAction(view);
        dealBack();
//        dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
//        public void callKeyBack(long delayMillis) {
//            if (this.activityWeak.get() != null && delayMillis < 0) {
//                this.activityWeak.get().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
//            } else if (this.activityWeak.get() != null && delayMillis > 0) {
//                new Handler().postDelayed(() -> this.activityWeak.get().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK)), delayMillis);
//            }
//        }
    }
}
