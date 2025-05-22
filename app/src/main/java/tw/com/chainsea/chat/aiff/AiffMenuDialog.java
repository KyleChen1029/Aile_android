package tw.com.chainsea.chat.aiff;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.aiff.database.entity.AiffInfo;
import tw.com.chainsea.chat.databinding.DialogAiffMenuBinding;
import tw.com.chainsea.chat.ui.adapter.AiffMenuAdapter;

public class AiffMenuDialog extends DialogFragment {
    private Context context;
    private DialogAiffMenuBinding binding;
    private AiffMenuAdapter adapter;
    private List<AiffInfo> aiffInfoList;
    private OnAiffMenuCallback callback;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public AiffMenuDialog(Context context, List<AiffInfo> aiffInfoList) {
        this.context = context;
        this.aiffInfoList = aiffInfoList;
        setCancelable(false);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).setBackgroundDrawableResource(R.color.transparent);
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_aiff_menu, null, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new AiffMenuAdapter(aiffInfoList);
        binding.rvAiffMenu.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        binding.rvAiffMenu.setAdapter(adapter);
        binding.rvAiffMenu.setHasFixedSize(true);
        binding.rvAiffMenu.setItemAnimator(new DefaultItemAnimator());
        binding.rvAiffMenu.setNestedScrollingEnabled(false);

        initListener();
    }

    private void initListener() {
        binding.tvCancel.setOnClickListener(v -> dismiss());
        adapter.setOnItemClickListener((adapter, view, position) -> {
            AiffInfo info = (AiffInfo) adapter.getData().get(position);
            if (callback != null) {
                callback.onCallBack(info);
                this.dismiss();
            }
        });
    }


    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    public void setCallback(OnAiffMenuCallback callback) {
        this.callback = callback;
    }

}
