package tw.com.chainsea.chat.view.contact;

import androidx.annotation.StringRes;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * current by evan on 2020-03-23
 *
 * @author Evan Wang
 * date 2020-03-23
 */
public enum ContactPersonType {
    SELF(0, 11, "個人資料", 0),
//    RECOMMEND(0, 12, "你可能認識的人", 0),
    COLLECTION(0, 13, "我的收藏", 0),
    LABEL(0, 14, "標籤", 0),
//    SERVICE_NUMBER(0, 15, "我的服務號", 0),
    SERVICE(0, 16, "訂閱服務號", 0), //目前只能由後台設定, 代表我可以進線要求服務的
    GROUP(0, 17, "社團", 0),
    DISCUSS(0, 18, "多人", 0),
    EMPLOYEE(0, 19, "夥伴", 0),
    CUSTOMER(0, 20, "客戶", 0),
    BLOCK(0, 21, "封鎖", 0),
    AIFF(0, 22,"應用", 0),
    UNDEF(0, 99, "未知", 0),
    HEADER(0,98,"",0);

    private int headerViewType;
    private int viewType;
    private String name;
    private @StringRes
    int nameResId;

    public static ContactPersonType of(int viewType) {
        for (ContactPersonType t : ContactPersonType.values()) {
            if (t.getViewType() == viewType) {
                return t;

            }
        }
        return UNDEF;
    }

    public static Set<ContactPersonType> LABEL_or_GROUP_or_EMPLOYEE = Sets.newHashSet(LABEL, GROUP, EMPLOYEE);

    ContactPersonType(int headerViewType, int viewType, String name, int nameResId) {
        this.headerViewType = headerViewType;
        this.viewType = viewType;
        this.name = name;
        this.nameResId = nameResId;
    }

    public int getHeaderViewType() {
        return headerViewType;
    }

    public void setHeaderViewType(int headerViewType) {
        this.headerViewType = headerViewType;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNameResId() {
        return nameResId;
    }

    public void setNameResId(int nameResId) {
        this.nameResId = nameResId;
    }
}
