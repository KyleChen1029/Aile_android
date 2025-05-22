package tw.com.chainsea.ce.sdk.bean.statistics;

import androidx.annotation.ColorInt;

import com.google.common.collect.Sets;

import java.util.Set;
/**
 * current by evan on 2020-09-17
 *
 * @author Evan Wang
 * date 2020-09-17
 */
public enum ServiceNumberStatType {
    LAST_DAY_MEMBER(0, "昨日", 0xFF96F0FF, 0xFFDCC9FB, 0, 0xFF4a90e2, "我處理", "所有"), // 自己
    LAST_30_DAY_MEMBER(1, "近30日", 0xFF96F0FF, 0xFFDCC9FB, -30, 0xFF4a90e2, "我處理", "所有"), // 自己
    LAST_DAY(2, "昨日", 0xFFfad961, 0xFFf76b1c, 0, 0xFF846532, "服務號", "所有"),
    LAST_30_DAY(3, "近30日", 0xFFfad961, 0xFFf76b1c, -30, 0xFF846532, "服務號", "所有"),
    UNDEF(4, "-- / --", 0xFFfad961, 0xFFf76b1c, 0, 0xFF4a90e2, "" , ""); // 失敗

    private int index;
    private String name;
    @ColorInt
    int reachedColor;
    @ColorInt
    int unReachedColor;
    int plusDay;
    @ColorInt
    int textColor;
    String categoryLeft;
    String categoryRight;


    public static Set<ServiceNumberStatType> STAT_ME = Sets.newHashSet(LAST_DAY_MEMBER, LAST_30_DAY_MEMBER);
    public static Set<String> STAT_ME_BY_MANE = Sets.newHashSet(LAST_DAY_MEMBER.name(), LAST_30_DAY_MEMBER.name(), UNDEF.name());
    public static Set<String> STAT_ALL_BY_MANE = Sets.newHashSet(LAST_DAY.name(), LAST_30_DAY.name(), UNDEF.name());

    ServiceNumberStatType(int index, String name, int reachedColor, int unReachedColor, int plusDay, int textColor, String categoryLeft, String categoryRight) {
        this.index = index;
        this.name = name;
        this.reachedColor = reachedColor;
        this.unReachedColor = unReachedColor;
        this.plusDay = plusDay;
        this.textColor = textColor;
        this.categoryLeft = categoryLeft;
        this.categoryRight = categoryRight;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getReachedColor() {
        return reachedColor;
    }

    public int getUnReachedColor() {
        return unReachedColor;
    }

    public int getPlusDay() {
        return plusDay;
    }

    public int getTextColor() {
        return textColor;
    }

    public String getCategoryLeft() {
        return categoryLeft;
    }

    public String getCategoryRight() {
        return categoryRight;
    }
}
