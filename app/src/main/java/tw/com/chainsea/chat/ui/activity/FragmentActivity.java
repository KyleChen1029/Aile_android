package tw.com.chainsea.chat.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ActivityFragmentLayoutBinding;

public abstract class FragmentActivity extends ParentActivity {
    ActivityFragmentLayoutBinding binding;
    private LeftAction leftAction = LeftAction.FINISH;

    public enum LeftAction {
        BACK, FINISH, CLOSE_APP
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = (ActivityFragmentLayoutBinding) viewDataBinding;
        addFragment();
        setTitleBar();
        binding.include.leftAction.setOnClickListener(this::doLeftAction);
    }

    protected void addFragment() {
        Fragment mFragment = createFragment();
        String TAG = getTAG();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!mFragment.isAdded()) {
            transaction.add(R.id.contentFL, mFragment, TAG);
        }
        transaction.show(mFragment).commit();
    }

    protected abstract String getTAG();

    protected abstract Fragment createFragment();

    protected void setTitleBar() {
        binding.include.title.setText(getTitleText());

        View view = rightView();
        if (view != null) {
            binding.include.rightAction.removeAllViews();
            binding.include.rightAction.setVisibility(View.VISIBLE);
            binding.include.rightAction.addView(view);
            view.setOnClickListener(v -> rightAction());
        } else {
            binding.include.rightAction.setVisibility(View.INVISIBLE);
        }
    }

    protected abstract View rightView();

    protected abstract String getTitleText();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected int createView() {
        return R.layout.activity_fragment_layout;
    }

    @Override
    protected void findView() {

    }

    public void doLeftAction(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),
                    0);
        }

        switch (this.leftAction) {
            case FINISH:
            case BACK:
                finish();
                break;
            case CLOSE_APP:
                leftAction = LeftAction.FINISH;
                ActivityCompat.finishAffinity(this);
                break;
        }
    }

    public void setLeftAction(LeftAction leftAction) {
        this.leftAction = leftAction;
    }

    protected void rightAction() {
    }

    public View getTitleBar() {
        return binding.include.getRoot();
    }

    public void setTitleText(String title) {
        binding.include.title.setText(title);
    }

}
