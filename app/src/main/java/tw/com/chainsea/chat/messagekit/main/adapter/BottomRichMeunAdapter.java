package tw.com.chainsea.chat.messagekit.main.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.Collections;
import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.chat.databinding.ItemLongMsgClickLayoutBinding;
import tw.com.chainsea.chat.messagekit.enums.RichMenuBottom;
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo;

//訊息長按顯示下方功能列表
public class BottomRichMeunAdapter extends RecyclerView.Adapter<BottomRichMeunAdapter.ViewHolder> {
    private Context cxt;
    //    private List<Integer> datas = Lists.newArrayList();
    private List<RichMenuBottom> datas = Lists.newArrayList();
    private List<RichMenuInfo> aiffList = Lists.newArrayList();
    private OnItemClickListener onItemClickListener;
    private OnAiffItemClickListener onAiffItemClickListener;

    private MessageEntity msg;

    public BottomRichMeunAdapter() {
    }

    public void setData(MessageEntity msg) {
        this.msg = msg;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDatas(List<RichMenuBottom> datas) {
        this.datas.clear();
        this.datas.addAll(datas);
        Collections.sort(this.datas, (o1, o2) -> Ints.compare(o1.getSortIndex(), o2.getSortIndex()));
        notifyDataSetChanged();
    }

    public void setAiffData(List<RichMenuInfo> aiffList) {
        this.aiffList.clear();
        this.aiffList.addAll(aiffList);
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.cxt = parent.getContext();
        ItemLongMsgClickLayoutBinding binding = ItemLongMsgClickLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
//        int res = this.datas.get(position);

        if (position < this.datas.size() - this.aiffList.size()) {
            RichMenuBottom menu = this.datas.get(position);
            String name = this.cxt.getString(menu.getStrRes());
            holder.binding.memberName.setText(name);
            holder.itemView.setOnClickListener(v -> {
                if (this.onItemClickListener != null) {
                    this.onItemClickListener.onClick(this.msg, menu, position);
//                this.onItemClickListener.onLongItemClick(this.msg, name, res, position);
                }
            });
            //CELog.d("Kyle2 111position="+position+", size="+this.datas.size()+", name="+name);
        } else {
            int p = position - (datas.size() - aiffList.size());
            //CELog.d("Kyle2 222position="+position+", size="+this.aiffList.size()+", name="+aiffList.get(p).getName());
            holder.binding.memberName.setText(aiffList.get(p).getName());
            holder.itemView.setOnClickListener(v -> {
                if (this.onAiffItemClickListener != null) {
                    this.onAiffItemClickListener.onClick(this.msg, aiffList.get(p).getId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return this.datas == null ? 0 : this.datas.size();
    }

    public interface OnItemClickListener {
//        void onLongItemClick(MessageEntity msg, String name, int res, int position);

        void onClick(MessageEntity msg, RichMenuBottom menu, int position);

        void onCancle();
    }

    public interface OnAiffItemClickListener {
        void onClick(MessageEntity msg, String aiffId);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnAiffItemClickListener(OnAiffItemClickListener onAiffItemClickListener) {
        this.onAiffItemClickListener = onAiffItemClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ItemLongMsgClickLayoutBinding binding;

        public ViewHolder(ItemLongMsgClickLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
