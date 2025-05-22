//package tw.com.chainsea.custom.view.alert;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.webkit.URLUtil;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.RequestOptions;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import tw.com.chainsea.custom.view.R;
//import tw.com.chainsea.custom.view.image.CircleImageView;
//
/// **
// * current by evan on 2020-04-09
// *
// * @author Evan Wang
// * @date 2020-04-09
// */
//public class BusinessAlertViewAdapter extends BaseAdapter {
//    private List<AlertView.Business> datas = new ArrayList<AlertView.Business>();
//
//    BusinessAlertViewAdapter(List<AlertView.Business> datas) {
//        this.datas = datas;
//    }
//
//    @Override
//    public int getCount() {
//        return this.datas.size();
//    }
//
//    @Override
//    public Object getItem(int position) {
//        return this.datas.get(position);
//    }
//
//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        AlertView.Business data = this.datas.get(position);
//
//        BusinessAlertViewAdapter.Holder holder = null;
//        View view = convertView;
//        if (view == null) {
//            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
////            item_imagetext_alertbutton
//            view = inflater.inflate(R.layout.item_bussiness_alertbutton, null);
//            holder = creatHolder(view);
//            view.setTag(holder);
//        } else {
//            holder = (BusinessAlertViewAdapter.Holder) view.getTag();
//        }
//        holder.updateUI(parent.getContext(), data, position);
//        return view;
//    }
//
//
//    public BusinessAlertViewAdapter.Holder creatHolder(View view) {
//        return new BusinessAlertViewAdapter.Holder(view);
//    }
//
//
//    class Holder {
//        private CircleImageView civ_icon;
//        private TextView tv_business_name;
//
//        private TextView tv_end_time;
//        private TextView tv_manager_name;
//        private TextView tv_executor_name;
//
//
//        public Holder(View view) {
//            civ_icon = (CircleImageView) view.findViewById(R.id.civ_icon);
//            tv_business_name = (TextView) view.findViewById(R.id.tv_business_name);
//            tv_end_time = (TextView) view.findViewById(R.id.tv_end_time);
//            tv_manager_name = (TextView) view.findViewById(R.id.tv_manager_name);
//            tv_executor_name = (TextView) view.findViewById(R.id.tv_executor_name);
//
//        }
//
//        public void updateUI(Context context, AlertView.Business data, int position) {
//            tv_business_name.setText(data.businessName);
//            tv_end_time.setText(data.businessEndTime);
//            tv_manager_name.setText(data.businessManagerName);
//            tv_executor_name.setText(data.businessExecutorName);
//            if (URLUtil.isValidUrl(data.avatarUrl) ){
//                Glide.with(context)
//                        .load(data.avatarUrl)
//                        .apply(new RequestOptions()
//                                .placeholder(R.drawable.default_avatar)
//                                .error(R.drawable.default_avatar)
//                                .fitCenter())
//                        .into(civ_icon);
//            }else {
//                civ_icon.setImageResource(R.drawable.default_avatar);
//            }
//
//
//
////            tvAlert.setTextColor(ContextCompat.getColor(context.getApplicationContext(), 
//R.color.textColor_alert_edit));
////            ivIcon.setImageResource(data.res);
//        }
//    }
//}
