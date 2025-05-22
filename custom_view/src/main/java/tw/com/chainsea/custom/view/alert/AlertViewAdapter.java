package tw.com.chainsea.custom.view.alert;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import java.util.List;

import tw.com.chainsea.custom.view.R;

/**
 * Created by Sai on 15/8/9.
 */
public class AlertViewAdapter extends BaseAdapter {
    private List<String> mDatas;
    private List<String> mDestructive;

    private @ColorInt
    int textColor = 0xFF007AFF;
    private @ColorInt
    int destructiveTextColor = 0xFFFF3b30;

    public AlertViewAdapter(List<String> datas, List<String> destructive) {
        this.mDatas = datas;
        this.mDestructive = destructive;
    }


    public AlertViewAdapter(List<String> datas, List<String> destructive, int destructiveColor) {
        this.mDatas = datas;
        this.mDestructive = destructive;
    }

    public AlertViewAdapter setTextColor(@ColorInt int textColor) {
        this.textColor = textColor;
        return this;
    }

    public AlertViewAdapter setDestructiveTextColor(@ColorInt int destructiveTextColor) {
        this.destructiveTextColor = destructiveTextColor;
        return this;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String data = mDatas.get(position);
        Holder holder = null;
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            view = inflater.inflate(R.layout.item_alertbutton, null);
            holder = creatHolder(view);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }
        holder.UpdateUI(parent.getContext(), data, position);
        return view;
    }

    public Holder creatHolder(View view) {
        return new Holder(view);
    }

    class Holder {
        private TextView tvAlert;

        public Holder(View view) {
            tvAlert = (TextView) view.findViewById(R.id.tvAlert);
//            tvAlert.setTextColor(textColor);
        }

        public void UpdateUI(Context context, String data, int position) {
            tvAlert.setText(data);
            if (mDestructive != null && mDestructive.contains(data)) {
                tvAlert.setTextColor(destructiveTextColor);
//                tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(), 
//                R.color.textColor_alert_button_destructive));
            } else {
                tvAlert.setTextColor(textColor);
            }
        }
    }
}
