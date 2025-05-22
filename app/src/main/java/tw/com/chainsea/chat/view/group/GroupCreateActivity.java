package tw.com.chainsea.chat.view.group;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.List;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ActivityGroupCreateBinding;
import tw.com.chainsea.chat.util.ThemeHelper;

public class GroupCreateActivity extends AppCompatActivity {
    private ActivityGroupCreateBinding binding;
    private static final int REQUEST_CARD = 1999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.INSTANCE.setTheme(GroupCreateActivity.this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_create);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof NavHostFragment) {
                for (Fragment childFragment : fragment.getChildFragmentManager().getFragments()) {
                    if (childFragment instanceof GroupCreateInfoFragment) {
                        ((GroupCreateInfoFragment) childFragment).showPicDialog();
                    }
                }
            }
        }
    }
}
