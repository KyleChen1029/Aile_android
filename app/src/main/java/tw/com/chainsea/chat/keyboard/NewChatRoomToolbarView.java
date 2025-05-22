package tw.com.chainsea.chat.keyboard;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.custom.view.image.CircleImageView;

/**
 * current by evan on 2020-07-09
 *
 * @author Evan Wang
 * date 2020-07-09
 */
public class NewChatRoomToolbarView {

    View root;
    ImageView ivBack;
    TextView tvUnreadNum;
    LinearLayout llTitleBox;
    CircleImageView civTitleIcon;
    TextView tvTitle;
    TextView tvMemberSize;
    TextView tvSTitle;
    LinearLayout llFunBox;
    ImageView ivBusiness;
    ImageView ivPen;
    ImageView ivChannel;
    ImageView ivSearch;
    ImageView ivInvite;
    ImageView ivCall;
    public ImageView ivDropDown;
    ConstraintLayout clSearchBox;
    SearchView svMessage;
    TextView tvSearchCancel;

    private NewChatRoomToolbarView(View root) {
        this.root = root;
        this.ivBack = root.findViewById(R.id.iv_back);
        this.tvUnreadNum = root.findViewById(R.id.tv_unread_num);
        this.llTitleBox = root.findViewById(R.id.ll_title_box);
        this.civTitleIcon = root.findViewById(R.id.civ_title_icon);
        this.tvTitle = root.findViewById(R.id.tv_title);
        this.tvMemberSize = root.findViewById(R.id.tv_member_size);
        this.tvSTitle = root.findViewById(R.id.tv_s_title);
        this.llFunBox = root.findViewById(R.id.ll_fun_box);
        this.ivBusiness = root.findViewById(R.id.iv_business);
        this.ivPen = root.findViewById(R.id.iv_pen);
        this.ivChannel = root.findViewById(R.id.iv_channel);
        this.ivSearch = root.findViewById(R.id.iv_search);
        this.ivInvite = root.findViewById(R.id.iv_invite);
        this.ivCall = root.findViewById(R.id.iv_call);
        this.ivDropDown = root.findViewById(R.id.iv_drop_down);
        this.clSearchBox = root.findViewById(R.id.cl_search_box);
        this.svMessage = root.findViewById(R.id.sv_message);
        this.tvSearchCancel = root.findViewById(R.id.tv_search_cancel);
    }

    public static NewChatRoomToolbarView bindView(View root) {
        return new NewChatRoomToolbarView(root);
    }

    public void setListener(View.OnClickListener listener) {
        this.root.setOnClickListener(listener);
        this.ivBack.setOnClickListener(listener);
        this.llTitleBox.setOnClickListener(listener);
        this.tvTitle.setOnClickListener(listener);
        this.tvSTitle.setOnClickListener(listener);

        this.ivBusiness.setOnClickListener(listener);
        this.ivPen.setOnClickListener(listener);
        this.ivChannel.setOnClickListener(listener);
        this.ivSearch.setOnClickListener(listener);
        this.ivInvite.setOnClickListener(listener);
        this.ivCall.setOnClickListener(listener);
        this.ivDropDown.setOnClickListener(listener);

        this.tvSearchCancel.setOnClickListener(listener);
    }
}
