package tw.com.chainsea.chat.ui.adapter.entity;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import tw.com.chainsea.chat.R;

public class RichMenuInfo implements Comparable<RichMenuInfo> {
    public enum MenuType {
        FIXED(0),
        AIFF(1);

        private int type;

        MenuType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public enum FixedMenuId {
        NEW_FRIEND("0"),
        NEW_GROUP("1"),
        NEW_TAG("2"),
        AMPLIFICATION("3"),
        MUTE("4"),
        SEARCH("5"),
        SERVICE("7"),
        UPGRADE("8"),
        DISMISS_CROWD("9"),
        EXIT_CROWD("10"),
        EXIT_DISCUSS("11"),
        NEW_MEMBER("12"),
        MAIN_PAGE("13"),
        TODO("13"),
        FILTER("14");

        private String id;

        FixedMenuId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public enum MenuInfo {
        NEW_FRIEND(FixedMenuId.NEW_FRIEND, R.drawable.new_friend, R.string.base_top_rich_menu_add_friend),
        NEW_GROUP(FixedMenuId.NEW_GROUP, R.drawable.new_group, R.string.base_top_rich_menu_create_crowd),
        NEW_TAG(FixedMenuId.NEW_TAG, R.drawable.new_tag, R.string.base_top_rich_menu_add_label),
        AMPLIFICATION(FixedMenuId.AMPLIFICATION, R.drawable.amplification, R.string.base_top_rich_menu_cancel_mute),
        MUTE(FixedMenuId.MUTE, R.drawable.not_remind, R.string.base_top_rich_menu_open_mute),
        SEARCH(FixedMenuId.SEARCH, R.drawable.search_icon, R.string.base_top_rich_menu_search),
        UPGRADE(FixedMenuId.UPGRADE, R.drawable.new_group, R.string.text_transfer_to_crowd),
        DISMISS_CROWD(FixedMenuId.DISMISS_CROWD, R.drawable.ic_quit, R.string.base_top_rich_menu_dismiss_crowd),
        EXIT_CROWD(FixedMenuId.EXIT_CROWD, R.drawable.ic_quit, R.string.base_top_rich_menu_exit_crowd),
        EXIT_DISCUSS(FixedMenuId.EXIT_DISCUSS, R.drawable.ic_quit, R.string.base_top_rich_menu_exit_discuss),
        NEW_MEMBER(FixedMenuId.NEW_MEMBER, R.drawable.ic_provisional_member, R.string.text_provisional_member),
        MAIN_PAGE(FixedMenuId.MAIN_PAGE, R.drawable.ic_home_page, R.string.text_home_page),

        TODO(FixedMenuId.TODO, R.drawable.ic_check_list_22dp, R.string.rich_menu_text_todo),
        FILTER(FixedMenuId.FILTER, R.drawable.icon_filter, R.string.rich_menu_text_filter);

        @DrawableRes
        private int imageRes;
        @StringRes
        private int nameRes;
        private FixedMenuId id;

        MenuInfo(FixedMenuId id, int imageRes, int nameRes) {
            this.id = id;
            this.imageRes = imageRes;
            this.nameRes = nameRes;
        }

        public int getImageRes() {
            return imageRes;
        }

        public void setImageRes(int imageRes) {
            this.imageRes = imageRes;
        }

        public int getNameRes() {
            return nameRes;
        }

        public void setNameRes(int nameRes) {
            this.nameRes = nameRes;
        }

        public FixedMenuId getId() {
            return id;
        }

        public void setId(FixedMenuId id) {
            this.id = id;
        }
    }

    private int type;
    private FixedMenuId menuId;
    private String id;
    private int imageRes;
    private int nameRes;
    private boolean isEnable;
    private String name;
    private String image;
    private String title;
    private long pinTimestamp;

    private long useTimestamp;


    public static RichMenuInfo of(int type, FixedMenuId id, int imageRes, int nameRes, boolean isEnable) {
        return new RichMenuInfo(type, id, imageRes, nameRes, isEnable);
    }

    public static RichMenuInfo of(int type, String id, String image, String title, String name, long pinTimestamp, long useTimestamp) {
        return new RichMenuInfo(type, id, image, title, name, pinTimestamp, useTimestamp);
    }

    public static List<RichMenuInfo> getBaseTopRichMenus() {
        return Lists.newArrayList(
            RichMenuInfo.of(MenuType.FIXED.getType(), MenuInfo.NEW_GROUP.getId(), MenuInfo.NEW_GROUP.getImageRes(), MenuInfo.NEW_GROUP.getNameRes(), true)
        );
    }

    public static List<RichMenuInfo> getServiceNumberChatRoomTopRichMenus(boolean isMute, boolean isServiceNumberChatRoom, boolean isEmployee) {
        ArrayList<RichMenuInfo> menu = Lists.newArrayList();
        if (!isEmployee) {
            menu.add(RichMenuInfo.of(
                MenuType.FIXED.getType(), MenuInfo.NEW_MEMBER.getId(), MenuInfo.NEW_MEMBER.getImageRes(), MenuInfo.NEW_MEMBER.getNameRes(), true));

        }
        // 搜索
        menu.add(RichMenuInfo.of(MenuType.FIXED.getType(), MenuInfo.SEARCH.getId(), MenuInfo.SEARCH.getImageRes(), MenuInfo.SEARCH.getNameRes(), true));

        if (!isServiceNumberChatRoom) {
            if (isMute) {
                // 開啟通知
                menu.add(RichMenuInfo.of(MenuType.FIXED.getType(), MenuInfo.AMPLIFICATION.getId(),
                    MenuInfo.AMPLIFICATION.getImageRes(), MenuInfo.AMPLIFICATION.getNameRes(),
                    true));
            } else {
                // 關閉通知
                menu.add(RichMenuInfo.of(MenuType.FIXED.getType(), MenuInfo.MUTE.getId(),
                    MenuInfo.MUTE.getImageRes(), MenuInfo.MUTE.getNameRes(), true));
            }
        }

        //篩選
        menu.add(RichMenuInfo.of(MenuType.FIXED.getType(), MenuInfo.FILTER.getId(), MenuInfo.FILTER.getImageRes(), MenuInfo.FILTER.getNameRes(), true));

        //記事
        menu.add(RichMenuInfo.of(MenuType.FIXED.getType(), MenuInfo.TODO.getId(), MenuInfo.TODO.getImageRes(), MenuInfo.TODO.getNameRes(), true));

        return menu;
    }

    public static RichMenuInfo getDiscussCrowdChatRoomMainPageRichMenus() {
        return RichMenuInfo.of(MenuType.FIXED.getType(), MenuInfo.MAIN_PAGE.getId(), MenuInfo.MAIN_PAGE.getImageRes(), MenuInfo.MAIN_PAGE.getNameRes(), true);
    }

    @Override
    public int compareTo(RichMenuInfo o) {
        return ComparisonChain.start()
            .compare(o.getWeights(), this.getWeights())
            .compare(o.getPinTimestamp(), this.getPinTimestamp())
            .compare(o.getUseTimestamp(), this.getUseTimestamp())
            .result();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RichMenuInfo that = (RichMenuInfo) o;
        return Objects.equals(id, that.id);
    }

    private Double getWeights() {
        double weight = 0.0;
        if (pinTimestamp != 0L) {
            return 16.0;
        }
        return 0.0;
    }

    public RichMenuInfo(int type, FixedMenuId id, int imageRes, int nameRes, boolean isEnable) {
        this.type = type;
        this.menuId = id;
        this.imageRes = imageRes;
        this.nameRes = nameRes;
        this.isEnable = isEnable;
    }

    public RichMenuInfo(int type, String id, String image, String title, String name, long pinTimestamp, long useTimestamp) {
        this.type = type;
        this.id = id;
        this.image = image;
        this.title = title;
        this.name = name;
        this.pinTimestamp = pinTimestamp;
        this.useTimestamp = useTimestamp;
    }

    public FixedMenuId getMenuId() {
        return menuId;
    }

    public void setMenuId(FixedMenuId menuId) {
        this.menuId = menuId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getImageRes() {
        return imageRes;
    }

    public int getNameRes() {
        return nameRes;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPinTimestamp() {
        return pinTimestamp;
    }

    public void setPinTimestamp(long pinTimestamp) {
        this.pinTimestamp = pinTimestamp;
    }

    public long getUseTimestamp() {
        return useTimestamp;
    }

    public void setUseTimestamp(long useTimestamp) {
        this.useTimestamp = useTimestamp;
    }
}
