package tw.com.chainsea.chat.keyboard;

import android.graphics.Color;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.style.RoomThemeStyle;
import tw.com.chainsea.chat.keyboard.view.HadEditText;
import tw.com.chainsea.chat.widget.GridItemDecoration;
import tw.com.chainsea.custom.view.recyclerview.MaxHeightRecyclerView;

/**
 * current by evan on 2020-07-08
 *
 * @author Evan Wang
 * @date 2020-07-08
 */
public class NewKeyboardView {

    View root;
//    MaxHeightRecyclerView mrvRichMenu;

    ConstraintLayout clArea;

    LinearLayout llLeftFunBox;
    ImageView ivCamera;
    ImageView ivPic;
    ImageView ivVideo;
    ImageView ivMedia;
    ImageView ivFunExpand;

    HadEditText etInput;
    ImageView ivFacial;


    LinearLayout llRightFunBox;
    ImageView ivSend;
    ImageView ivInputExpand;


    MaxHeightRecyclerView mhrvRichMenu;


    private NewKeyboardView(View root) {
        this.root = root;
//        this.mrvRichMenu = root.findViewById(R.id.mrv_rich_menu);

        this.clArea = root.findViewById(R.id.cl_area);

        this.llLeftFunBox = root.findViewById(R.id.ll_left_fun_box);
        this.ivCamera = root.findViewById(R.id.iv_camera);
        this.ivPic = root.findViewById(R.id.iv_pic);
        this.ivVideo = root.findViewById(R.id.iv_video);
        this.ivMedia = root.findViewById(R.id.iv_media);
        this.ivFunExpand = root.findViewById(R.id.iv_fun_expand);

        this.etInput = root.findViewById(R.id.et_input);
        this.ivFacial = root.findViewById(R.id.iv_facial);
        this.ivFacial.setTag(null);

        this.llRightFunBox = root.findViewById(R.id.ll_right_fun_box);
        this.ivSend = root.findViewById(R.id.iv_send);
        this.ivInputExpand = root.findViewById(R.id.iv_input_expand);
        this.mhrvRichMenu = root.findViewById(R.id.mrv_rich_menu);


        this.mhrvRichMenu.setBackgroundColor(RoomThemeStyle.UNDEF.getMainColor());
        this.mhrvRichMenu.setMaxHeight(0);
        this.mhrvRichMenu.setLayoutManager(new GridLayoutManager(root.getContext(), 5));
        this.mhrvRichMenu.addItemDecoration(new GridItemDecoration(Color.WHITE));
        this.mhrvRichMenu.setItemAnimator(new DefaultItemAnimator());
        this.mhrvRichMenu.setHasFixedSize(true);
//        bottomRichMeunAdapter = new BottomRichMeunAdapter();
//        bottomRichMenuRV.setAdapter(bottomRichMeunAdapter);
        this.mhrvRichMenu.measure(0, 0);


    }


    public static NewKeyboardView bindView(View root) {
        return new NewKeyboardView(root);
    }
}
