package tw.com.chainsea.chat.view.login;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.FragmentCreateOrJoinBinding;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.util.SystemKit;
import tw.com.chainsea.chat.view.login.viewmodel.LoginViewModel;

public class LoginCreateOrJoinFragment extends Fragment{
    public static final String TAG = LoginCreateOrJoinFragment.class.getSimpleName();
    private Context context;
    private FragmentCreateOrJoinBinding binding;
    private LoginViewModel viewModel;
    private NavController navController;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_or_join, container, false);
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        binding.btnCreateTeam.setOnClickListener(clickListener);
        binding.btnJoinTeam.setOnClickListener(clickListener);
        binding.btnClose.setOnClickListener(clickListener);

        //CP創建團隊回復
        SystemKit.recoverTransTenant(requireActivity());
        //CP創建團隊回復-end
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if(v.equals(binding.btnClose))
                navController.popBackStack(R.id.loginCpFragment, false);
            else if(v.equals(binding.btnCreateTeam))
                navController.navigate(R.id.action_loginCreateOrJoinFragment_to_loginCreateGuideFragment);
            else if(v.equals(binding.btnJoinTeam))
                navController.navigate(R.id.action_loginCreateOrJoinFragment_to_loginJoinGuideFragment);
        }
    };
}
