package tw.com.chainsea.chat.view.login;

import android.content.Context;
import android.content.Intent;
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
import tw.com.chainsea.chat.databinding.FragmentCreateGuideBinding;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.view.group.GroupCreateActivity;
import tw.com.chainsea.chat.view.login.viewmodel.LoginViewModel;

public class LoginCreateGuideFragment extends Fragment{
    public static final String TAG = LoginCreateGuideFragment.class.getSimpleName();
    private Context context;
    private FragmentCreateGuideBinding binding;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_guide, container, false);
        navController = NavHostFragment.findNavController(this);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);

        binding.btnClose.setOnClickListener(clickListener);
        binding.btnCreateTeam.setOnClickListener(clickListener);
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if(v.equals(binding.btnClose)){
                requireActivity().onBackPressed();
            }
            else if(v.equals(binding.btnCreateTeam)){
                startActivity(new Intent(context, GroupCreateActivity.class));
            }
        }
    };
}
