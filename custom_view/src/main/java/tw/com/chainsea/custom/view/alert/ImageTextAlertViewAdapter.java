package tw.com.chainsea.custom.view.alert;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import tw.com.chainsea.custom.view.R;

/**
 * current by evan on 2020-03-02
 */
public class ImageTextAlertViewAdapter extends BaseAdapter {
    private List<AlertView.ImageText> datas = new ArrayList<AlertView.ImageText>();
    boolean isShowImage = false;

    ImageTextAlertViewAdapter(List<AlertView.ImageText> datas, boolean isShowImage) {
        this.datas = datas;
        this.isShowImage = isShowImage;
    }

    @Override
    public int getCount() {
        return this.datas.size();
    }

    @Override
    public Object getItem(int position) {
        return this.datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        AlertView.ImageText data = this.datas.get(position);

        Holder holder = null;
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//            item_imagetext_alertbutton
            view = inflater.inflate(R.layout.item_imagetext_alertbutton, null);
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
        private ImageView ivIcon;

        public Holder(View view) {
            tvAlert = (TextView) view.findViewById(R.id.tvAlert);
            ivIcon = (ImageView) view.findViewById(R.id.ivIcon);
        }

        public void UpdateUI(Context context, AlertView.ImageText data, int position) {
            tvAlert.setText(data.text);
            tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(),
                R.color.textColor_alert_edit));
//            tvAlert.setCompoundDrawables(context.getDrawable(data.res),null,null,null);
//            tvAlert.setCompoundDrawablesWithIntrinsicBounds(context.getDrawable(data.res),null,null,null);
            if (!isShowImage || data.res == 0) {
                ivIcon.setVisibility(View.GONE);
            } else {
                ivIcon.setVisibility(View.VISIBLE);
                ivIcon.setImageResource(data.res);
            }

//            if (mDestructive != null && mDestructive.contains(data)) {
//                tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(), 
//            R.color.textColor_alert_button_destructive));
//            } else {
//                tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(), 
//            R.color.textColor_alert_edit));
//            }
        }
    }

}
