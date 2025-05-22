package tw.com.chainsea.chat.view.group;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.util.ThemeHelper;

public class GroupWaitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeHelper.INSTANCE.setTheme(this);
        setContentView(R.layout.activity_group_wait);
    }
}