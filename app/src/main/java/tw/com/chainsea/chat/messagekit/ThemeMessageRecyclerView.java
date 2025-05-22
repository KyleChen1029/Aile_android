package tw.com.chainsea.chat.messagekit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.base.Strings;

import java.util.List;

import tw.com.aile.sdk.bean.message.MessageEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.chat.messagekit.listener.OnMessageControlEventListener;
import tw.com.chainsea.chat.messagekit.theme.adapter.ThemeMessageAdapter;
import tw.com.chainsea.chat.ui.adapter.WrapContentLinearLayoutManager;
import tw.com.chainsea.custom.view.recyclerview.MaxHeightRecyclerView;

/**
 * MsgPanel
 * Created by 90Chris on 2016/6/3.
 */

public class ThemeMessageRecyclerView extends MaxHeightRecyclerView {
    private ThemeMessageAdapter adapter;
    private ChatRoomEntity chatRoomEntity;

    public ThemeMessageRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewInit(context);
    }

    public ThemeMessageRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        viewInit(context);
    }

    public ThemeMessageRecyclerView(Context context) {
        super(context);
        viewInit(context);
    }

    private void viewInit(Context context) {
        setLayoutManager(new WrapContentLinearLayoutManager(context));

    }

    public void setContainer(ChatRoomEntity session) {
        this.adapter = new ThemeMessageAdapter(getContext(), session);
        this.chatRoomEntity = session;
        setAdapter(this.adapter);
    }

    public void refreshToBottom() {
        refreshData();
        scrollToPosition(this.adapter.getItemCount() - 1);
    }

    public void refreshData() {
        this.adapter.refreshData();
    }

    public void refreshToPosition(int position) {
        this.adapter.notifyItemRangeInserted(0, position);
        ((LinearLayoutManager) getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    public void setOnMessageControlEventListener(OnMessageControlEventListener listener) {
        this.adapter.setOnMessageControlEventListener(listener);
    }

    /**
     * 給主題訊息ID
     *
     * @param themeId
     * @return
     */
    public ThemeMessageRecyclerView setThemeId(String themeId) {
        this.adapter.setThemeId(themeId);
        return this;
    }

    /**
     * 取得主題訊息
     *
     * @return
     */
    public MessageEntity getThemeData() {
        checkAdapter();
        return this.adapter.getThemeData();
    }

    /**
     * 取得上一筆訊息
     *
     * @return
     */
    public MessageEntity getNearData() {
        checkAdapter();
        return this.adapter.getNearData();
    }

    private void checkAdapter() {
        if (adapter == null) {
            adapter = new ThemeMessageAdapter(getContext(), chatRoomEntity);
            setAdapter(adapter);
        }
    }


    /**
     * 新增資料
     *
     * @param message
     * @return
     */
    @SuppressLint("NotifyDataSetChanged")
    public ThemeMessageRecyclerView setData(MessageEntity message) {
        this.adapter.setData(message).notifyDataSetChanged();
        return this;
    }

    /**
     * 如果已經開啟主題聊天室，且主題ID相同，且訊息沒有重複，且主題聊天室窗是開啟狀態
     *
     * @param themeId
     * @param message
     * @return
     * @version 1.9.1
     */
    public ThemeMessageRecyclerView setData(String themeId, MessageEntity message) {
        if (this.adapter == null || Strings.isNullOrEmpty(this.adapter.getThemeId()) || Strings.isNullOrEmpty(themeId)) {
            return this;
        }

        if (this.getVisibility() == View.GONE) {
            return this;
        }

        if (this.adapter.getThemeId().equals(themeId)) {
            if (!this.adapter.isContains(message)) {
                this.adapter.setData(message).refreshData();
            }
        }
        return this;
    }

    public void notifyChange(MessageEntity message) {
        if (adapter != null) {
            adapter.notifyMessage(message);
        }
    }

    /**
     * 清除資料
     */
    @SuppressLint("NotifyDataSetChanged")
    public void clearData() {
        if (adapter != null)
            adapter.clearData().notifyDataSetChanged();
    }

    @Override
    public ThemeMessageAdapter getAdapter() {
        return this.adapter;
    }

}

