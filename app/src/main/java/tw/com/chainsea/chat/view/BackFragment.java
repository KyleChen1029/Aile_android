package tw.com.chainsea.chat.view;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class BackFragment extends Fragment{
    public static final String TAG = BackFragment.class.getSimpleName();
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            onBackPressed();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    protected void onBackPressed(){
        //nothing to do
    }
}