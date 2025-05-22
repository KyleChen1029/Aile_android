package tw.com.chainsea.custom.view.text;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

/**
 * current by evan on 2019-11-06
 */
public class ResHelper {
    public static float getAttrFloatValue(Context context, int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue.getFloat();
    }

    public static int getAttrColor(Context context, int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue.data;
    }

    public static ColorStateList getAttrColorStateList(Context context, int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return ContextCompat.getColorStateList(context, typedValue.resourceId);
    }

//    public static int getAttrDimen(Context context, int attrRes) {
//        TypedValue typedValue = new TypedValue();
//        context.getTheme().resolveAttribute(attrRes, typedValue, true);
//        return TypedValue.complexToDimensionPixelSize(typedValue.data, DisplayHelper.getDisplayMetrics(context));
//    }

//    public static void assignTextViewWithAttr(TextView textView, int attrRes) {
//        TypedArray a = textView.getContext().obtainStyledAttributes(null, R.styleable.QMUITextCommonStyleDef, attrRes, 0);
//        int count = a.getIndexCount();
//        int paddingLeft = textView.getPaddingLeft(), paddingRight = textView.getPaddingRight(),
//                paddingTop = textView.getPaddingTop(), paddingBottom = textView.getPaddingBottom();
//        for (int i = 0; i < count; i++) {
//            int attr = a.getIndex(i);
//            if (attr == R.styleable.QMUITextCommonStyleDef_android_gravity) {
//                textView.setGravity(a.getInt(attr, -1));
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_textColor) {
//                textView.setTextColor(a.getColorStateList(attr));
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_textSize) {
//                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimensionPixelSize(attr, 0));
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_paddingLeft) {
//                paddingLeft = a.getDimensionPixelSize(attr, 0);
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_paddingRight) {
//                paddingRight = a.getDimensionPixelSize(attr, 0);
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_paddingTop) {
//                paddingTop = a.getDimensionPixelSize(attr, 0);
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_paddingBottom) {
//                paddingBottom = a.getDimensionPixelSize(attr, 0);
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_singleLine) {
//                textView.setSingleLine(a.getBoolean(attr, false));
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_ellipsize) {
//                int ellipsize = a.getInt(attr, 3);
//                switch (ellipsize) {
//                    case 1:
//                        textView.setEllipsize(TextUtils.TruncateAt.START);
//                        break;
//                    case 2:
//                        textView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
//                        break;
//                    case 3:
//                        textView.setEllipsize(TextUtils.TruncateAt.END);
//                        break;
//                    case 4:
//                        textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//                        break;
//                }
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_maxLines) {
//                textView.setMaxLines(a.getInt(attr, -1));
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_background) {
//                ViewHelper.setBackgroundKeepingPadding(textView, a.getDrawable(attr));
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_lineSpacingExtra) {
//                textView.setLineSpacing(a.getDimensionPixelSize(attr, 0), 1f);
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_drawablePadding) {
//                textView.setCompoundDrawablePadding(a.getDimensionPixelSize(attr, 0));
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_textColorHint) {
//                textView.setHintTextColor(a.getColor(attr, 0));
//            } else if (attr == R.styleable.QMUITextCommonStyleDef_android_textStyle) {
//                int styleIndex = a.getInt(attr, -1);
//                textView.setTypeface(null, styleIndex);
//            }
//        }
//        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
//        a.recycle();
//    }
}
