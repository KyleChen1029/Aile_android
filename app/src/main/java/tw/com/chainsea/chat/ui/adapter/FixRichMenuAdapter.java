package tw.com.chainsea.chat.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;

import java.util.List;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemFixRichMenuBinding;
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo;

public class FixRichMenuAdapter extends BaseQuickAdapter<RichMenuInfo, BaseDataBindingHolder<ItemFixRichMenuBinding>> {

    private OnBlockItemClickListener onBlockItemClickListener;

    public FixRichMenuAdapter(List<RichMenuInfo> infoList) {
        super(R.layout.item_fix_rich_menu, infoList);
    }

    @Override
    protected void convert(@NonNull BaseDataBindingHolder<ItemFixRichMenuBinding> holder, RichMenuInfo info) {
        ItemFixRichMenuBinding binding = holder.getDataBinding();
        if (binding != null) {
            if (info.getType() == RichMenuInfo.MenuType.FIXED.getType()) {
                binding.tvMenuName.setText(info.getNameRes());
                binding.ivIcon.setImageResource(info.getImageRes());
            }
        }
    }

    /**
     * 設定點擊監聽事件
     *
     * @param onBlockItemClickListener
     */
    public FixRichMenuAdapter setOnBlockItemClickListener(FixRichMenuAdapter.OnBlockItemClickListener onBlockItemClickListener) {
        this.onBlockItemClickListener = onBlockItemClickListener;
        return this;
    }

    public interface OnBlockItemClickListener {
        void onUnblockWarning(View v, boolean isEnable);
    }
}
