package tw.com.chainsea.chat.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.lib.ActivityManager;
import tw.com.chainsea.custom.view.progress.IosProgressBar;

/**
 * Created by sunhui on 2017/10/17.
 */

public abstract class ParentActivity extends AppCompatActivity {
    protected ViewDataBinding viewDataBinding;
    private IosProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, createView());
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        ActivityManager.addActivity(this);
    }


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    protected abstract int createView();

    protected abstract void findView();

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.removeActivity(this);
    }

    public void showLoadingView(int resId) {
        if (progressBar == null) {
            progressBar = IosProgressBar.show(this, resId, true, false, dialog -> { });
        }
    }


    public void hideLoadingView() {
        try{
            if (progressBar != null && progressBar.isShowing())
                progressBar.dismiss();
        }catch (Exception ignored){}
    }
}
