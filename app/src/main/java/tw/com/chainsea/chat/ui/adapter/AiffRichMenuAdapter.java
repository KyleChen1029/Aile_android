package tw.com.chainsea.chat.ui.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder;
import com.google.common.base.Strings;

import java.util.List;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemAiffRichMenuBinding;
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo;
import tw.com.chainsea.chat.util.AvatarKit;
import tw.com.chainsea.chat.util.NameKit;

public class AiffRichMenuAdapter extends BaseQuickAdapter<RichMenuInfo, BaseDataBindingHolder<ItemAiffRichMenuBinding>> {
    private final AvatarKit avatarKit = new AvatarKit();
    NameKit nameKit = new NameKit();
    public AiffRichMenuAdapter(List<RichMenuInfo> infoList) {
        super(R.layout.item_aiff_rich_menu, infoList);
        addChildClickViewIds(R.id.cl_pin);
        addChildClickViewIds(R.id.cl_content_view);
    }

    @Override
    protected void convert(@NonNull BaseDataBindingHolder<ItemAiffRichMenuBinding> holder, RichMenuInfo info) {
        ItemAiffRichMenuBinding binding = holder.getDataBinding();
        String shortName = nameKit.getAvatarName(info.getName());
        if (binding != null) {
            binding.tvMenuTitle.setText(info.getTitle());
            binding.tvMenuName.setText(info.getName());
            if (info.getPinTimestamp() != 0) {
                binding.ivMenuPin.setVisibility(View.VISIBLE);
                binding.ivPin.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.icon_un_pin));
            } else {
                binding.ivMenuPin.setVisibility(View.GONE);
                binding.ivPin.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.icon_pin));
            }
            if(Strings.isNullOrEmpty(info.getImage())){
                binding.ivAiffIcon.setVisibility(View.GONE);
                binding.tvAvatar.setVisibility(View.VISIBLE);
                binding.tvAvatar.setText(shortName);
                GradientDrawable gradientDrawable = (GradientDrawable) binding.tvAvatar.getBackground();
                gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(shortName)));
            }else
                avatarKit.loadCEAvatar(info.getImage(), binding.ivAiffIcon, binding.tvAvatar, info.getName());
        }

    }
}
