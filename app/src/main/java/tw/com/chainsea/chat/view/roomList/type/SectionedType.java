package tw.com.chainsea.chat.view.roomList.type;


import androidx.annotation.StringRes;

import com.google.common.collect.Sets;

import java.util.Set;

import tw.com.chainsea.chat.R;

/**
 * Create by evan on 1/5/21
 *
 * @author Evan Wang
 * date 1/5/21
 */
public enum SectionedType {
    //    UNREAD(0, "未讀服務號聊天室"),
    UNREAD_NO_AGENT(1, "剛進件", R.string.service_room_sectioned_no_agent_unread),
    MY_SERVICE(2, "我服務中", R.string.service_room_sectioned_my_service),
    OTHERS_SERVICE(3, "服務中", R.string.service_room_sectioned_others_service),
    OTHER(4, "", 0),
    ROBOT_SERVICE(5, "AI 服務", R.string.service_room_sectioned_robot_service),
    MONITOR_AI_SERVICE(6, "監控AI", R.string.service_room_sectioned_monitor_ai_service);
    private int index;
    private String name;

    @StringRes
    private int resId;

    public static Set<SectionedType> CALCULATE_SECTIONED_TYPE = Sets.newHashSet(UNREAD_NO_AGENT, MY_SERVICE, OTHERS_SERVICE);

    public static Set<SectionedType> DEFAULT_SECTIONED_TYPE = Sets.newHashSet(UNREAD_NO_AGENT, MY_SERVICE, OTHERS_SERVICE);

    public static Set<SectionedType> MY_or_OTHERS_SERVICE = Sets.newHashSet(MY_SERVICE, OTHERS_SERVICE);

    SectionedType(int index, String name, int resId) {
        this.index = index;
        this.name = name;
        this.resId = resId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }
}