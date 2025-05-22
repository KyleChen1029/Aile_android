package tw.com.chainsea.chat.view.fulltextsearch.adapter;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemNoSwipeViewHolder;

/**
 * Create by evan on 3/9/21
 *
 * @author Evan Wang
 * @date 3/9/21
 */
abstract public class SectionedCellAdapter<T, VH extends ItemNoSwipeViewHolder<T>> extends RecyclerView.Adapter<VH> {

    private List<T> metadata = Lists.newArrayList();
    private List<T> list = Lists.newArrayList();

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return getItemView(parent, viewType);
    }

    abstract public VH getItemView(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull VH vh, int i) {
        T t = this.list.get(i);
        vh.onBind(t, 0, i);
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public SectionedCellAdapter<T, VH> setData(List<T> list) {
        this.metadata.clear();
        this.metadata.addAll(list);
        return this;
    }

    private void sort() {

    }

    private void filter() {

    }

    private void component() {
        this.list.clear();
        this.list.addAll(this.metadata);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void refresh() {
        sort();
        filter();
        component();
        notifyDataSetChanged();
    }
}
