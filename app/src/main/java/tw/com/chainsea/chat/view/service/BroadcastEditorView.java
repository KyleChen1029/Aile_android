package tw.com.chainsea.chat.view.service;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.ui.adapter.WrapContentLinearLayoutManager;
import tw.com.chainsea.custom.view.picker.DateTimePickerLayout;

/**
 * current by evan on 2020-07-29
 *
 * @author Evan Wang
 * @date 2020-07-29
 */
public class BroadcastEditorView {

//    BroadcastEditorLayout root;

    ImageView ivExpansion;
    ImageView ivCancel;

    // 訂閱列表
    RecyclerView rvTopics;

    // 動態調整訊息內容
//    NestedScrollView svMessageContent;
//    LinearLayout llMessageContent;
//    FrameLayout flMessageContent;
//    ViewStub  vsMessageContent;

    RecyclerView rvMessageContent;
//
//    Space space;

    // 最後修改完成的成員名稱
    TextView tvLastEditorName;

    // 日期選擇器
    DateTimePickerLayout dateTimePick;


    // 底部控件
    ConstraintLayout clControlElement;
    ImageView ivCalendar;

    TextView tvDatetime;

    Button btnDateTimeClear;
    Button btnDelete;
    Button btnEditor;
    Button btnSend;

    private BroadcastEditorView(View root) {
//        this.root = root;
        this.ivExpansion = root.findViewById(R.id.iv_expansion);
        this.ivCancel = root.findViewById(R.id.iv_cancel);
        this.rvTopics = root.findViewById(R.id.rv_topics);
        this.rvTopics.setLayoutManager(new GridLayoutManager(root.getContext(), 1,GridLayoutManager.HORIZONTAL, false));
//        this.flMessageContent = root.findViewById(R.id.fl_message_content);
//        this.vsMessageContent = root.findViewById(R.id.vs_message_content);
//        this.space = root.findViewById(R.id.space);
        this.rvMessageContent = root.findViewById(R.id.rv_message_content);
        this.rvMessageContent.setLayoutManager(new WrapContentLinearLayoutManager(root.getContext()));
//        this.rvMessageContent.setLayoutManager(new LinearLayoutManager(root.getContext(),LinearLayoutManager.VERTICAL, false));
//        this.svMessageContent = root.findViewById(R.id.sv_message_content);
//        this.llMessageContent = root.findViewById(R.id.ll_message_content);

        this.tvLastEditorName = root.findViewById(R.id.tv_last_editor_name);
        this.dateTimePick = root.findViewById(R.id.date_time_pick);

        this.clControlElement = root.findViewById(R.id.cl_control_element);
        this.ivCalendar = root.findViewById(R.id.iv_calendar);
        this.tvDatetime = root.findViewById(R.id.tv_datetime);

        this.btnDateTimeClear = root.findViewById(R.id.btn_date_time_clear);
        this.btnDelete = root.findViewById(R.id.btn_delete);
        this.btnEditor = root.findViewById(R.id.btn_editor);
        this.btnSend = root.findViewById(R.id.btn_send);

        init();
    }


    private void init() {
        this.dateTimePick.setControl(true);
        this.dateTimePick.setDateTimeFormat("yyyy/MM/dd HH:mm");
    }

    public static BroadcastEditorView bindView(View root) {
        return new BroadcastEditorView(root);
    }
}
