package tw.com.chainsea.chat.view.login;

import static android.app.Activity.RESULT_OK;

import static androidx.navigation.Navigation.findNavController;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import java.text.MessageFormat;
import java.util.Objects;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
import tw.com.chainsea.ce.sdk.http.cp.model.Configuration;
import tw.com.chainsea.ce.sdk.http.cp.respone.AccountLoginDeviceCheckResponse;
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.FragmentLoginRegisterBinding;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.ui.activity.TermsActivity;
import tw.com.chainsea.chat.util.NoDoubleClickListener;

public class LoginRegisterFragment extends Fragment{
    public static final String TAG = LoginRegisterFragment.class.getSimpleName();
    private Context context;
    private FragmentLoginRegisterBinding binding;
//    private LoginViewModel viewModel;
    private NavController navController;
    private CountDownTimer timer;
    private ActivityResultLauncher<Intent> termsBySubmitARL = null;
    private ActivityResultLauncher<Intent> termsByClickTermsARL = null;
    private static boolean isAgreementTerms = false;

    private String countryCode = "";
    private String phoneNumber = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_register, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getBundleArguments();
        navController = NavHostFragment.findNavController(this);
        termsBySubmitARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    boolean isAgreeForTerms = bundle.getBoolean("isAgree");
                    if(isAgreeForTerms) {
                        isAgreementTerms = true;
                        register();
                    }
                }
            }
        });
        termsByClickTermsARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    boolean isAgreeForTerms = bundle.getBoolean("isAgree");
                    if(isAgreeForTerms)
                        isAgreementTerms = true;
                }
            }
        });
        binding.txtCountryCode.setText(countryCode);
        binding.txtPhone.setText(phoneNumber);
        binding.btnClose.setOnClickListener(clickListener);
        binding.btnSend.setOnClickListener(clickListener);
        binding.btnGetSmsCode.setOnClickListener(clickListener);
        binding.txtTerms.setOnClickListener(clickListener);
        //binding.txtTerms.setMovementMethod(LinkMovementMethod.getInstance());
        binding.btnSend.setEnabled(false);
        binding.edtSmsCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                binding.btnSend.setBackground(s.length()>0 && binding.edtAccount.length()>0 ? ContextCompat.getDrawable(requireActivity(), R.drawable.btn_blue_full) : ContextCompat.getDrawable(requireActivity(), R.drawable.btn_input_error));
                binding.btnSend.setEnabled(s.length() > 0 && binding.edtAccount.length()>0);
            }
        });
        binding.edtAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 1) {
                    binding.scopeName.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.radius_rectangle_bg));
                    binding.tvMsgError.setVisibility(View.GONE);
                }
            }
        });
        timer = new CountDownTimer(90000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                binding.btnGetSmsCode.setText(MessageFormat.format("{0}秒", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                binding.btnGetSmsCode.setText(getString(R.string.get_sms_code));
                binding.btnGetSmsCode.setClickable(true);
            }
        };

    }

    private void getBundleArguments() {
        if (getArguments() != null) {
            countryCode = getArguments().getString("countryCode");
            phoneNumber = getArguments().getString("phoneNumber");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(timer != null) timer.cancel();
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if(v.equals(binding.btnClose)){
                requireActivity().onBackPressed();
            }
            else if(v.equals(binding.btnSend)){
                if(!isAgreementTerms)
                    termsBySubmitARL.launch(new Intent(requireActivity(), TermsActivity.class));
                else
                    register();
            }
            else if(v.equals(binding.btnGetSmsCode)){
                obtainOtp();
            }else if(v.equals(binding.txtTerms))
                navigateToTermsPage();
        }
    };

    private void navigateToTermsPage() {
        Intent intent = new Intent(requireActivity(), TermsActivity.class);
        intent.putExtra("PhoneNumber", phoneNumber);
        termsByClickTermsARL.launch(intent);
    }

    private void obtainOtp() {
        timer.start();
        binding.btnGetSmsCode.setClickable(false);
        CpApiManager.getInstance().sendCheckCode(requireContext(), countryCode, phoneNumber, new CpApiListener<>() {

            @Override
            public void onSuccess(String s) {
                if (!TextUtils.isEmpty(s)) {
                    Toast.makeText(requireContext(), "驗證碼已送出", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void register() {
        if(binding.edtAccount.length() < 2 || binding.edtAccount.length() > 8) {
            binding.tvMsgError.setVisibility(View.VISIBLE);
            binding.scopeName.setBackground(ContextCompat.getDrawable(requireActivity(), R.drawable.radius_rectangle_bg_error));
            return;
        }
        CpApiManager.getInstance().register(context,
                phoneNumber,
                countryCode,
                Objects.requireNonNull(binding.edtSmsCode.getText()).toString(),
                Objects.requireNonNull(binding.edtAccount.getText()).toString(),
                new CpApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                AccountLoginDeviceCheckResponse response = JsonHelper.getInstance().from(s, AccountLoginDeviceCheckResponse.class);
                TokenPref.getInstance(context).setCpName(response.getName());
                TokenPref.getInstance(context).setCpAccountId(response.getAccountId());
                TokenPref.getInstance(context).setAuthToken(response.getAuthToken());
                Configuration configuration = response.getConfiguration();
                TokenPref.getInstance(context)
                        .setCpRefreshTokenId(response.getRefreshTokenId())
                        .setCpTokenId(response.getTokenId())
                        .setCpSocketName(response.getName())
                        .setCpSocketDeviceId(response.getDeviceId())
                        .setCpSocketUrl(configuration.getSocketIoUrl())
                        .setCpSocketNameSpace(configuration.getSocketIoNamespace());

                CpSocket.getInstance().connect(
                        TokenPref.getInstance(context).getCpSocketUrl(),
                        TokenPref.getInstance(context).getCpSocketNameSpace(),
                        TokenPref.getInstance(context).getCpSocketName(),
                        TokenPref.getInstance(context).getCpSocketDeviceId()
                );
                navController.navigate(R.id.action_loginRegisterFragment_to_loginCreateOrJoinFragment);
            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                CELog.d("TAG", errorCode + errorMessage);
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(context, errorMessage));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isAgreementTerms = false;
    }
}