//package tw.com.chainsea.chat.view.login;
//
//import static android.app.Activity.RESULT_OK;
//import static tw.com.chainsea.ce.sdk.lib.ErrCode.MOBILE_NOT_EXIST;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.content.ContextCompat;
//import androidx.databinding.DataBindingUtil;
//import androidx.fragment.app.Fragment;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.navigation.NavController;
//import androidx.navigation.fragment.NavHostFragment;
//
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//import tw.com.chainsea.android.common.version.VersionHelper;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.http.cp.CpApiManager;
//import tw.com.chainsea.ce.sdk.http.cp.CpApiPath;
//import tw.com.chainsea.ce.sdk.http.cp.base.CpApiListener;
//import tw.com.chainsea.ce.sdk.http.cp.base.CpNewRequestBase;
//import tw.com.chainsea.ce.sdk.http.cp.respone.LoginResponse;
//import tw.com.chainsea.ce.sdk.socket.cp.CpSocket;
//import tw.com.chainsea.chat.App;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.config.SystemConfig;
//import tw.com.chainsea.chat.databinding.FragmentLoginPhoneBinding;
//import tw.com.chainsea.chat.lib.ToastUtils;
//import tw.com.chainsea.chat.lib.Util;
//import tw.com.chainsea.chat.ui.dialog.IosProgressDialog;
//import tw.com.chainsea.chat.ui.utils.countrycode.CountryActivity;
//import tw.com.chainsea.chat.util.NoDoubleClickListener;
//import tw.com.chainsea.chat.view.login.viewmodel.LoginViewModel;
//@Deprecated
//public class LoginCpFragment extends Fragment {
//    public static final String TAG = LoginCpFragment.class.getSimpleName();
//    private Context context;
//    private FragmentLoginPhoneBinding binding;
//    private NavController navController;
//    //private static final int REQUEST_COUNTRY_CODE = 12;
//    private LoginViewModel viewModel;
//
//    private ActivityResultLauncher<Intent> ARL = null;
//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        this.context = context;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login_phone, container, false);
//        navController = NavHostFragment.findNavController(this);
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        viewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
//
//        ARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//
//            if (result.getResultCode() == RESULT_OK) {
//                if (result.getData() != null) {
//                    Bundle bundle = result.getData().getExtras();
//                    String countryNumber = bundle.getString("countryNumber");
//                    binding.txtCountryCode.setText(countryNumber);
//                    TokenPref.getInstance(context).setCountryCode(countryNumber);
//                    init(countryNumber);
//                }
//            }
//        });
//
//        binding.btnJoin.setOnClickListener(clickListener);
//        binding.txtChangeToAccount.setOnClickListener(clickListener);
//        binding.txtChangeToPhone.setOnClickListener(clickListener);
//        binding.txtCountryCode.setOnClickListener(clickListener);
//
//        String countryCode = TokenPref.getInstance(context).getCountryCode();
//        if (!TextUtils.isEmpty(countryCode)) {
//            binding.txtCountryCode.setText(countryCode);
//        }
//        binding.cbRememberMe.setChecked(TokenPref.getInstance(getContext()).isRememberMe());
//        if (binding.cbRememberMe.isChecked()) {
//            String number = TokenPref.getInstance(getContext()).getAccountNumber();
//            binding.edtAccount.setText(number);
//            binding.btnJoin.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.yellow));
//            binding.btnJoin.setClickable(true);
//        }else {
//            binding.btnJoin.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.color_FAD391));
//            binding.btnJoin.setClickable(false);
//        }
//        checkUpdate();
//        init(countryCode);
//    }
//
//    private void init(String countryCode) {
//        binding.edtAccount.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) { }
//            @Override
//            public void afterTextChanged(Editable s) {
//                binding.btnJoin.setBackgroundColor(Util.checkMobile(countryCode + s) ? ContextCompat.getColor(requireActivity(), R.color.yellow) : ContextCompat.getColor(requireActivity(), R.color.color_FAD391));
//                binding.btnJoin.setClickable(Util.checkMobile(countryCode + s));
//            }
//        });
//    }
//    private void checkUpdate(){
//        try {
//            VersionUpdateUtil.getInstance().checkVersion(context, false);
//
//            ThreadExecutorHelper.MyHandler.newInstance().execute(() -> {
//                //判斷現在版本是否大於server版本, 才可以使用切換server功能
//                if (Integer.parseInt(VersionHelper.getVersionCode(context)) > TokenPref.getInstance(context).getCurrentAppVersionFromServer()) {
//                    binding.tvChangeServer.setOnLongClickListener(v -> {
//                        changeCpServer();
//                        return false;
//                    });
//                }
//                App.getInstance().isCheckVersionUpdate = true;
//
//            }, 2000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
//        @Override
//        protected void onNoDoubleClick(View v) {
//            if(v.equals(binding.txtCountryCode)){
//                ARL.launch(new Intent(getActivity(), CountryActivity.class));
//            }
//            else if(v.equals(binding.btnJoin)){
//                loginFlow();
//            }
//            else if(v.equals(binding.txtChangeToAccount)){
//                changeToAccount();
//            }
//            else if(v.equals(binding.txtChangeToPhone)){
//                changeToPhone();
//            }
//        }
//    };
//
////    @Override
////    public void onActivityResult(int requestCode, int resultCode, Intent data) {
////        super.onActivityResult(requestCode, resultCode, data);
////        if (resultCode == RESULT_OK) {
////            if (requestCode == REQUEST_COUNTRY_CODE) {
////                Bundle bundle = data.getExtras();
////                String countryNumber = bundle.getString("countryNumber");
////                binding.txtCountryCode.setText(countryNumber);
////                TokenPref.getInstance(context).setCountryCode(countryNumber);
////            }
////        }
////    }
////    private void ClearDBWhenChangeCpServer(int which) {
////        String[] serversCode = {"F", "T", "N", "C", "U"};
////        String currentCode = TokenPref.getInstance(context).getCurrentServer();
////        if(!currentCode.equals(serversCode[which])) {
//            //Clear DB
//            //CELog.d("Kyle1 clear DB");
//            //TokenPref.getInstance(requireActivity()).clearByKey(TokenPref.PreferencesKey.CP_RELATION_TENANT);
//
////        }
////    }
//    private void changeCpServer(){
//        String[] servers = {"正式服", "QA測試服", "南京測試服", "秋雨本地測試服", "UAT測試服"};
//        new AlertDialog.Builder(getActivity()).setItems(servers, (dialog, which) -> {
////            ClearDBWhenChangeCpServer(which);
//            switch (which){
//                case 0:
//                    CpNewRequestBase.BASE_URL = CpApiPath.FORMAL_SERVER;
//                    CpSocket.BASE_URL = CpSocket.FORMAL_SERVER;
//                    binding.tvChangeServer.setText("");
//                    TokenPref.getInstance(context).setCurrentServer("F");
//                    break;
//                case 1:
//                    CpNewRequestBase.BASE_URL = CpApiPath.TEST_SERVER;
//                    CpSocket.BASE_URL = CpSocket.TEST_SERVER;
//                    binding.tvChangeServer.setText("Q");
//                    TokenPref.getInstance(context).setCurrentServer("T");
//                    break;
//                case 2:
//                    CpNewRequestBase.BASE_URL = CpApiPath.NJ_TEST_SERVER;
//                    CpSocket.BASE_URL = CpSocket.NJ_TEST_SERVER;
//                    binding.tvChangeServer.setText("N");
//                    TokenPref.getInstance(context).setCurrentServer("N");
//                    break;
//                case 4:
//                    CpNewRequestBase.BASE_URL = CpApiPath.UAT_SERVER;
//                    CpSocket.BASE_URL = CpSocket.UAT_SERVER;
//                    binding.tvChangeServer.setText("U");
//                    TokenPref.getInstance(context).setCurrentServer("U");
//                    break;
//            }
//        }).show();
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if(SystemConfig.isCpMode) {
//            switch (TokenPref.getInstance(context).getCurrentServer()) {
//                case "F":
//                    CpNewRequestBase.BASE_URL = CpApiPath.FORMAL_SERVER;
//                    CpSocket.BASE_URL = CpSocket.FORMAL_SERVER;
//                    binding.tvChangeServer.setText("");
//                    break;
//                case "T":
//                    CpNewRequestBase.BASE_URL = CpApiPath.TEST_SERVER;
//                    CpSocket.BASE_URL = CpSocket.TEST_SERVER;
//                    binding.tvChangeServer.setText("Q");
//                    break;
//                case "N":
//                    CpNewRequestBase.BASE_URL = CpApiPath.NJ_TEST_SERVER;
//                    CpSocket.BASE_URL = CpSocket.NJ_TEST_SERVER;
//                    binding.tvChangeServer.setText("N");
//                    break;
//                case "U":
//                    CpNewRequestBase.BASE_URL = CpApiPath.UAT_SERVER;
//                    CpSocket.BASE_URL = CpSocket.UAT_SERVER;
//                    binding.tvChangeServer.setText("U");
//                    break;
//            }
//        }
//    }
//
//    private void changeToAccount(){
//        binding.txtChangeToAccount.setVisibility(View.GONE);
//        binding.txtChangeToPhone.setVisibility(View.VISIBLE);
//        binding.txtForgetPassword.setVisibility(View.VISIBLE);
//        binding.edtPassword.setVisibility(View.VISIBLE);
//    }
//    private void changeToPhone(){
//        binding.txtChangeToAccount.setVisibility(View.VISIBLE);
//        binding.txtChangeToPhone.setVisibility(View.GONE);
//        binding.txtForgetPassword.setVisibility(View.GONE);
//        binding.edtPassword.setVisibility(View.GONE);
//    }
//    private void loginFlow(){
//        Editable account = binding.edtAccount.getText();
////        if(account == null || account.toString().isEmpty()){
////            Toast.makeText(context, R.string.wording_please_enter_accout, Toast.LENGTH_SHORT).show();
////            return;
////        }
//        CharSequence countryCode = binding.txtCountryCode.getText();
////        if (!Util.checkMobile(countryCode.toString() + account)) {
////            Toast.makeText(context, R.string.wording_please_enter_correct_phone_number, Toast.LENGTH_SHORT).show();
////            return;
////        }
//
//        TokenPref.getInstance(context).setCountryCode(countryCode.toString());
//        TokenPref.getInstance(context).setAccountNumber(account!=null ? account.toString() : "");
//        viewModel.countryCode = countryCode.toString();
//        viewModel.phone = account!=null ? account.toString() : "";
//
//        IosProgressDialog iosProgressDialog = new IosProgressDialog(requireActivity());
//        iosProgressDialog.show();
//        CpApiManager.getInstance().login(context, countryCode.toString(), account!=null ? account.toString() : "", new CpApiListener<String>() {
//            @Override
//            public void onSuccess(String s) {
//                iosProgressDialog.dismiss();
//                TokenPref.getInstance(getContext()).setRememberMe(binding.cbRememberMe.isChecked());
//                LoginResponse response = JsonHelper.getInstance().from(s, LoginResponse.class);
//                viewModel.onceToken = response.getOnceToken();
//
//                NavHostFragment.findNavController(requireParentFragment()).navigateUp();
//                NavHostFragment.findNavController(requireParentFragment()).navigate(R.id.action_loginCpFragment_to_loginSmsFragment);
//            }
//
//            @Override
//            public void onFailed(String errorCode, String errorMessage) {
//                iosProgressDialog.dismiss();
//                Log.d("TAG", errorCode + errorMessage);
//                if(MOBILE_NOT_EXIST.getValue().equals(errorCode)){
//                    NavHostFragment.findNavController(requireParentFragment()).navigate(R.id.action_loginCpFragment_to_loginRegisterFragment);
//                }else{
//                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showToast(context, errorMessage));
//                }
//            }
//        });
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        ARL.unregister();
//    }
//}
