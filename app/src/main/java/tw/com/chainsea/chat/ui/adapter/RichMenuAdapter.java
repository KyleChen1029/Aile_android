package tw.com.chainsea.chat.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;

import java.util.List;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemRichMenuBinding;
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo;
import tw.com.chainsea.chat.util.AvatarKit;

public class RichMenuAdapter extends BaseQuickAdapter<RichMenuInfo, BaseDataBindingHolder<ItemRichMenuBinding>> {
    private final AvatarKit avatarKit = new AvatarKit();
    private OnBlockItemClickListener onBlockItemClickListener;

    public RichMenuAdapter(List<RichMenuInfo> infoList) {
        super(R.layout.item_rich_menu, infoList);
    }

    @Override
    protected void convert(@NonNull BaseDataBindingHolder<ItemRichMenuBinding> holder, RichMenuInfo info) {
        ItemRichMenuBinding binding = holder.getDataBinding();
        if (binding != null) {
            if (info.getType() == RichMenuInfo.MenuType.FIXED.getType()) {
                binding.tvMenuName.setText(info.getNameRes());
                binding.ivMenuPic.setImageResource(info.getImageRes());
                binding.getRoot().setEnabled(info.isEnable());
                if (!info.isEnable()) {
                    if (this.onBlockItemClickListener != null) {
                        this.onBlockItemClickListener.onUnblockWarning(binding.getRoot(), info.isEnable());
                    }
                }
            }

            if (info.getType() == RichMenuInfo.MenuType.AIFF.getType()) {
                binding.tvMenuName.setText(info.getTitle());
                avatarKit.loadCEAvatar(info.getImage(), binding.ivMenuPic, binding.tvAvatar, info.getName());
            }

        }
    }

    /**
     * 設定點擊監聽事件
     *
     * @param onBlockItemClickListener
     */
    public RichMenuAdapter setOnBlockItemClickListener(RichMenuAdapter.OnBlockItemClickListener onBlockItemClickListener) {
        this.onBlockItemClickListener = onBlockItemClickListener;
        return this;
    }

    public interface OnBlockItemClickListener {
        void onUnblockWarning(View v, boolean isEnable);
    }
}
