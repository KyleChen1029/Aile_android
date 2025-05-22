package tw.com.chainsea.chat.ui.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ActivityRecordBinding;
import tw.com.chainsea.chat.ui.fragment.RecordFragment;
import tw.com.chainsea.chat.view.BaseActivity;

/**
 * Created by jerry.yang on 2017/12/3.
 * desc:
 */
public class RecordActivity extends BaseActivity {
    public final String TAG = "RecordActivity";
    private RecordFragment mFragment;
    private ActivityRecordBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_record);
        addFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.leftAction.setOnClickListener(v -> finish());
    }

    private void addFragment() {
        mFragment = new RecordFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (!mFragment.isAdded()) {
            transaction.add(R.id.contentFL, mFragment, TAG);
        }
        transaction.show(mFragment).commit();
    }
}
