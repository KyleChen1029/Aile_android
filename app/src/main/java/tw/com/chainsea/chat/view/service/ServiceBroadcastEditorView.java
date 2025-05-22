package tw.com.chainsea.chat.view.service;

import java.lang.ref.WeakReference;

/**
 * current by evan on 2020-08-27
 *
 * @author Evan Wang
 * @date 2020-08-27
 */
public class ServiceBroadcastEditorView {
    WeakReference<ServiceBroadcastEditorActivity> activityWeak;

    public ServiceBroadcastEditorView(ServiceBroadcastEditorActivity activity) {
        this.activityWeak = new WeakReference<ServiceBroadcastEditorActivity>(activity);
        listener(activity);
    }

    private void listener (ServiceBroadcastEditorActivity activity) {
        activity.vb.ncrtlToolbar.setOnNewChatRoomToolbarListener(activity);
        // 鍵盤功能黑名單
//        nklInput.setBlacklist(Sets.newHashSet(FUN_RECORD));
        activity.vb.nklInput.setOnNewKeyboardListener(activity);
        activity.vb.rvMessageList.setOnMainMessageScrollStatusListener(activity);
        activity.vb.belEditor.setOnBroadcastEditorListener(activity);
        activity.vb.belEditor.setBottomToTopTargetId(activity.vb.nklInput.getId());
        activity.vb.funMedia.setOnNewKeyboardListener(activity);
        activity.vb.funMedia.setMaxCount(1);
        activity.vb.funEmoticon.setOnEmoticonSelectListener(activity);
//        activity.vb.rlRecorder.setOnRecordListener(activity);
        activity.vb.xRefreshLayout.setOnLoadMoreListener(activity);
        activity.vb.xRefreshLayout.setOnRefreshListener(activity);
    }
}
