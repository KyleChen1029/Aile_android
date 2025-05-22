package tw.com.chainsea.android.common.color;

import android.content.Context;
import android.util.TypedValue;

public class ColorHelper {

    public static int getAttrColor(Context context, int attrRes){
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue.data;
    }
}
