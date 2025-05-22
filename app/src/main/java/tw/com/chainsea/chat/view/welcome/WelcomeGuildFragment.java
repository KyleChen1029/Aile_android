package tw.com.chainsea.chat.view.welcome;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.FragmentWelcomeGuildBinding;

public class WelcomeGuildFragment extends Fragment {
    private static final String TIP = "TIP";
    private static final String TITLE = "TITLE";
    private static final String IMAGE = "IMAGE";

    private FragmentWelcomeGuildBinding binding;
    private int tip;
    private int title;
    private int image;

    public WelcomeGuildFragment() {
    }

    public static WelcomeGuildFragment newInstance(int tip, int title, int image) {
        WelcomeGuildFragment fragment = new WelcomeGuildFragment();
        Bundle args = new Bundle();
        args.putInt(TIP, tip);
        args.putInt(TITLE, title);
        args.putInt(IMAGE, image);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tip = getArguments().getInt(TIP);
            title = getArguments().getInt(TITLE);
            image = getArguments().getInt(IMAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome_guild, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(getString(tip).isEmpty()) binding.txtTip.setVisibility(View.INVISIBLE);
        else binding.txtTip.setText(tip);
        binding.txtTitle.setText(title);
        binding.img.setImageResource(image);
    }
}