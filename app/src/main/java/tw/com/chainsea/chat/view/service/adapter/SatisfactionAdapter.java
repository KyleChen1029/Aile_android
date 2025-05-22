package tw.com.chainsea.chat.view.service.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.ItemSatisfactionStarBinding;

/**
 * current by evan on 2020-04-17
 *
 * @author Evan Wang
 * @date 2020-04-17
 */
public class SatisfactionAdapter extends RecyclerView.Adapter<SatisfactionAdapter.ViewHolder> {

    List<Boolean> list = Lists.newArrayList();

    public SatisfactionAdapter(int amount) {
        for (int i = 1; i <= 5; i++) {
            list.add(i <= amount);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ItemSatisfactionStarBinding binding = ItemSatisfactionStarBinding.inflate(LayoutInflater.from(viewGroup.getContext()), viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.binding.ivStar.setImageResource(list.get(i) ? R.drawable.ic_star_yellow : R.drawable.star);
        viewHolder.binding.ivStar.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {


        private ItemSatisfactionStarBinding binding;

        public ViewHolder(@NonNull ItemSatisfactionStarBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
