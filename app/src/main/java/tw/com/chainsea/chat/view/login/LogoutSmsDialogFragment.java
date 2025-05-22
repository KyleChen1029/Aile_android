package tw.com.chainsea.chat.view.login;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.DialogLogoutSmsBinding;
import tw.com.chainsea.chat.util.SystemKit;

public class LogoutSmsDialogFragment extends DialogFragment implements LifecycleObserver {
    public static final String TAG = LogoutSmsDialogFragment.class.getSimpleName();
    private Context context;
    private DialogLogoutSmsBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        getLifecycle().addObserver(this);
        LayoutInflater inflater = getLayoutInflater();
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_logout_sms, null, false);
        Dialog dialog = new AlertDialog.Builder(context).setView(binding.getRoot()).create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        binding.btnConfirm.setOnClickListener(v -> logout());
        binding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        return dialog;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void logout() {
        CpApiManager.getInstance().logout(context, new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                SystemKit.logoutToLoginPage();
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d("TAG", errorCode + errorMessage);
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
