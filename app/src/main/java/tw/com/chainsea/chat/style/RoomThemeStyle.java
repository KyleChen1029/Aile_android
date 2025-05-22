package tw.com.chainsea.chat.style;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import java.util.Set;

import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.util.ThemeHelper;

/**
 * Used for chat room theme style
 *
 * <p>
 * mainColor
 * auxiliaryColor
 *
 * @version 1.10.0
 * DrawableRes int floatingTimeBox = Floating time Toast
 *
 *
 * </p>
 * current by evan on 2020-03-03
 */
public enum RoomThemeStyle {
    @SerializedName("self") SELF("self", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),  // Personal chat room (current user’s own multi-vehicle chat room)
    @SerializedName("strange") STRANGE("strange", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),   // Non-friend single chat room
    @SerializedName("friend") FRIEND("friend", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),   // Friends chat room
    @SerializedName("broast") BROAST("broast", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),   // Broadcast chat room
    @SerializedName("group") GROUP("group", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),   // Group chat room
    //    @SerializedName("services") SERVICES("services", 0xFFFFBC42, 0xFFFFFFFF, 0xFFFFBC42, R.drawable.bg_float_time_service),  // Service account chat room yellow theme
    @SerializedName("services") SERVICES("services", 0XFF6BC2BA, 0xFFFFFFFF, 0XFF6BC2BA, R.drawable.bg_float_time_business), // Business chat room
    //    @SerializedName("subscribe") SUBSCRIBE("subscribe", 0xFFFFBC42, 0xFFFFFFFF, 0xFFFFBC42, R.drawable.bg_float_time_service),  // Subscription service number chat room Joc
    @SerializedName("subscribe") SUBSCRIBE("subscribe", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),   // Friends chat room
    @SerializedName("discuss") DISCUSS("discuss", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),   // Multi-person discussion group
    @SerializedName("service") SERVICE("service", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),   // Service account dedicated chat room (gw to manual use)
    @SerializedName("vistor") VISTOR("vistor", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),
    @SerializedName("system") SYSTEM("system", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult),   // System chat room
    @SerializedName("business") BUSINESS("business", 0XFF6BC2BA, 0xFFFFFFFF, 0XFF6BC2BA, R.drawable.bg_float_time_business), // Business chat room
    @SerializedName("service_Member") SERVICE_MEMBER("service_Member", 0XFF6BC2BA, 0xFFFFFFFF, 0XFF6BC2BA, R.drawable.bg_float_time_business), // Business chat room
    //    @SerializedName("broadcast") BROADCAST("broadcast", 0xFFFFBC42, 0xFFFFFFFF, 0xFFFFBC42, R.drawable.bg_float_time_service),  // Service account broadcastedit
    @SerializedName("broadcast") BROADCAST("broadcast", 0XFF6BC2BA, 0xFFFFFFFF, 0XFF6BC2BA, R.drawable.bg_float_time_business), // Business chat room
    @SerializedName("undef") UNDEF("undef", 0xFF6B93C2, 0xFFFFFFFF, 0xFF6B93C2, R.drawable.bg_float_time_defult);  // UNDEF

    private String name;

    @ColorInt
    int mainColor;

    @ColorInt
    int auxiliaryColor;

    @ColorInt
    int keyboardColor;

    @DrawableRes
    int floatingTimeBox;

    public static RoomThemeStyle of(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return UNDEF;
        }
        for (RoomThemeStyle style : RoomThemeStyle.values()) {
            if (style.name.toUpperCase().equals(name.toUpperCase())) {
                return style;
            }
        }
        return UNDEF;
    }

    public int getTodoIconResId() {
        if (SERVICES_or_BUSINESS_or_BROADCAST_or_SERVICE_MEMBER.contains(this)) {
            return R.drawable.res_check_list_circle_business;
        }
        return R.drawable.res_check_list_circle_def;
    }

    public int getConsultIconResId() {
        if (SERVICES_or_BUSINESS_or_BROADCAST_or_SERVICE_MEMBER.contains(this)) {
            return R.drawable.icon_service_consult_business;
        }
        return R.drawable.icon_service_consult;
    }

    public int getServiceMemberIconResId() {
        if (SERVICES_or_BUSINESS_or_BROADCAST_or_SERVICE_MEMBER.contains(this) || ThemeHelper.INSTANCE.isGreenTheme() || ThemeHelper.INSTANCE.isServiceRoomTheme()) {
            return R.drawable.ic_service_member_business;
        }
        return R.drawable.ic_service_member;
    }

    public static Set<RoomThemeStyle> SERVICES_or_SUBSCRIBE = Sets.newHashSet(SERVICES, SUBSCRIBE);

    public static Set<RoomThemeStyle> SERVICES_or_BUSINESS_or_BROADCAST_or_SERVICE_MEMBER = Sets.newHashSet(SERVICES, BUSINESS, BROADCAST, SERVICE_MEMBER);

    RoomThemeStyle(String name, int mainColor, int auxiliaryColor, int keyboardColor, int floatingTimeBox) {
        this.name = name;
        this.mainColor = mainColor;
        this.auxiliaryColor = auxiliaryColor;
        this.keyboardColor = keyboardColor;
        this.floatingTimeBox = floatingTimeBox;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMainColor() {
        return mainColor;
    }

    public void setMainColor(int mainColor) {
        this.mainColor = mainColor;
    }

    public int getAuxiliaryColor() {
        return auxiliaryColor;
    }

    public void setAuxiliaryColor(int auxiliaryColor) {
        this.auxiliaryColor = auxiliaryColor;
    }

    public int getKeyboardColor() {
        return keyboardColor;
    }

    public void setKeyboardColor(int keyboardColor) {
        this.keyboardColor = keyboardColor;
    }

    public int getFloatingTimeBox() {
        return floatingTimeBox;
    }

    public void setFloatingTimeBox(int floatingTimeBox) {
        this.floatingTimeBox = floatingTimeBox;
    }
}
