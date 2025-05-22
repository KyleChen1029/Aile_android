package tw.com.chainsea.chat.ui.adapter;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;

import java.util.List;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.aiff.database.entity.AiffInfo;
import tw.com.chainsea.chat.databinding.ItemAiffMenuBinding;

public class AiffMenuAdapter extends BaseQuickAdapter<AiffInfo, BaseDataBindingHolder<ItemAiffMenuBinding>> {

    public AiffMenuAdapter(List<AiffInfo> infoList) {
        super(R.layout.item_aiff_menu, infoList);
    }

    @Override
    protected void convert(@NonNull BaseDataBindingHolder<ItemAiffMenuBinding> holder, AiffInfo info) {
        ItemAiffMenuBinding binding = holder.getDataBinding();
        if (binding != null) {
            binding.tvMenuName.setText(info.getName());
        }
    }
}
