package tw.com.chainsea.chat.aiff;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.aiff.database.AiffDB;
import tw.com.chainsea.chat.aiff.database.dao.AiffInfoDao;
import tw.com.chainsea.chat.aiff.database.entity.AiffInfo;
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo;
import tw.com.chainsea.chat.util.IntentUtil;

public class AiffManager {

    private Context context;
    private AiffInfoDao dao;
    private String chatRoomId;

    public AiffManager() {
    }

    public AiffManager(Context context, String chatRoomId) {
        this.context = context;
        AiffDB db = AiffDB.getInstance(context);
        dao = db.getAiffInfoDao();
        this.chatRoomId = chatRoomId;
    }

    public void showAiffList(List<RichMenuInfo> aiffInfoList) {
        Intent intent = new Intent(context, AiffDialog.class);
        intent.putExtra(AiffKey.TITLE, "更多應用");
        intent.putExtra(AiffKey.DISPLAY_TYPE, AiffKey.ALL);
        intent.putExtra(AiffKey.AIFF_INFO_LIST, JsonHelper.getInstance().toJson(aiffInfoList));
        intent.putExtra(AiffKey.ROOM_ID, chatRoomId);
        IntentUtil.INSTANCE.start(context, intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.ios_dialog_enter, R.anim.ios_dialog_exit);
        }
    }

    public void showAiffViewByInfo(AiffInfo info) {
        addAiffView(info);
    }

    public void showAiffView(String serviceKey) {
        AiffInfo info = dao.getAiffInfo("ServiceRoom", serviceKey);
        addAiffView(info);
    }

    public void showAiffById(String id) {
        AiffInfo info = dao.getAiffInfo(id);
        addAiffView(info);
    }

    private void addAiffView(AiffInfo info) {
        Intent intent = new Intent(context, AiffDialog.class);
        intent.putExtra(AiffKey.TITLE, info.getTitle());
        intent.putExtra(AiffKey.URL, info.getUrl());
        intent.putExtra(AiffKey.DISPLAY_TYPE, info.getDisplayType());
        intent.putExtra(AiffKey.ROOM_ID, chatRoomId);
        context.startActivity(intent);
//        IntentUtil.INSTANCE.start(context, intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.ios_dialog_enter, R.anim.ios_dialog_exit);
        }
    }

    /**
     * open Aiff webview without AiffInfo, by only url.
     *
     * @param urlString
     */
    public void addAiffWebView(String urlString){
        Intent intent = new Intent(context, AiffDialog.class);
        intent.putExtra(AiffKey.URL, urlString);
        intent.putExtra(AiffKey.DISPLAY_TYPE, AiffKey.ALL);
        intent.putExtra(AiffKey.ROOM_ID, chatRoomId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        IntentUtil.INSTANCE.start(context, intent);
    }

}
