package tw.com.chainsea.chat.view.account;

import androidx.annotation.DrawableRes;

import com.google.common.base.Strings;
import com.google.gson.annotations.SerializedName;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.text.StringHelper;
import tw.com.chainsea.chat.R;

/**
 * current by evan on 2020-11-13
 *
 * @author Evan Wang
 * date 2020-11-13
 */
public class BackgroundCanvasTransformer {

    public enum Res {
        @SerializedName("0") RES_0("0", R.drawable.back00),
        @SerializedName("1") RES_1("1", R.drawable.back01),
        @SerializedName("2") RES_2("2", R.drawable.back02),
        @SerializedName("3") RES_3("3", R.drawable.back03),
        @SerializedName("4") RES_4("4", R.drawable.back04),
        @SerializedName("5") RES_5("5", R.drawable.back05),
        @SerializedName("6") RES_6("6", R.drawable.back06),
        @SerializedName("7") RES_7("7", R.drawable.back07),
        @SerializedName("8") RES_8("8", R.drawable.back08),
        @SerializedName("9") RES_9("9", R.drawable.back09),
        @SerializedName("A") RES_A("A", R.drawable.back_a),
        @SerializedName("B") RES_B("B", R.drawable.back_b),
        @SerializedName("C") RES_C("C", R.drawable.back_c),
        @SerializedName("D") RES_D("D", R.drawable.back_d),
        @SerializedName("E") RES_E("E", R.drawable.back_e),
        @SerializedName("F") RES_F("F", R.drawable.back_f);

        private final String key;

        @DrawableRes
        private final int resId;

        Res(String key, int resId) {
            this.key = key;
            this.resId = resId;
        }

        public String getKey() {
            return key;
        }

        public int getResId() {
            return resId;
        }
    }


    public static Res getBackgroundCanvas(String userId) {
        String end = StringHelper.getEnd(userId, "", 1);
        if (Strings.isNullOrEmpty(end)) {
            return Res.RES_0;
        }
        return JsonHelper.getInstance().from(end, Res.class);
    }

}
