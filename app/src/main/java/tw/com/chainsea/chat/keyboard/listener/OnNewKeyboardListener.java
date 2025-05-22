package tw.com.chainsea.chat.keyboard.listener;

import android.view.View;

import java.util.List;
import java.util.TreeMap;

import tw.com.chainsea.android.common.multimedia.AMediaBean;
import tw.com.chainsea.android.common.multimedia.MultimediaHelper;
import tw.com.chainsea.chat.keyboard.NewKeyboardLayout;
import tw.com.chainsea.chat.keyboard.view.HadEditText;

/**
 * current by evan on 2020-07-08
 *
 * @author Evan Wang
 * date 2020-07-08
 */
public interface OnNewKeyboardListener  {

    void onFocusChange(View v, boolean hasFocus);

    void onOpenCameraFun();

    void onOpenPhotoSelectorFun(boolean isChange);

    void onOpenVideoFun();

    void onOpenMediaSelectorFun();

    void onOpenRecordFun();

    void onOpenFacialFun();

    void onCloseFun(NewKeyboardLayout.NewKeyboardFun type);

    void onSendAction(HadEditText.SendData sendData, boolean enableSend);

    void toMediaSelectorPreview(boolean isOriginal, String type, String current, TreeMap<String, String> data, int maxCount);

    void onMediaSelector(MultimediaHelper.Type type, List<AMediaBean> list, boolean isOriginal);

//    public final static int SINGLE = 1;
//    public final static int MULTIPLE = 2;
    void onOpenGallery(int mode);


    void onOpenFileFolder(int limit);

}
