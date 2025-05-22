package tw.com.chainsea.custom.view.text.alpha;

import android.view.View;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import tw.com.chainsea.custom.view.R;
import tw.com.chainsea.custom.view.text.ResHelper;

/**
 * current by evan on 2019-11-06
 */
public class AlphaViewHelper {
    private WeakReference<View> mTarget;

    /**
     * 设置是否要在 press 时改变透明度
     */
    private boolean mChangeAlphaWhenPress = true;

    /**
     * 设置是否要在 disabled 时改变透明度
     */
    private boolean mChangeAlphaWhenDisable = true;

    private float mNormalAlpha = 1f;
    private float mPressedAlpha = .5f;
    private float mDisabledAlpha = .5f;

    public AlphaViewHelper(@NonNull View target) {
        mTarget = new WeakReference<>(target);
        mPressedAlpha = ResHelper.getAttrFloatValue(target.getContext(), R.attr.qmui_alpha_pressed);
        mDisabledAlpha = ResHelper.getAttrFloatValue(target.getContext(), R.attr.qmui_alpha_disabled);
    }

    public AlphaViewHelper(@NonNull View target, float pressedAlpha, float disabledAlpha) {
        mTarget = new WeakReference<>(target);
        mPressedAlpha = pressedAlpha;
        mDisabledAlpha = disabledAlpha;
    }

    /**
     * @param current the view to be handled, maybe not equal to target view
     * @param pressed
     */
    public void onPressedChanged(View current, boolean pressed) {
        View target = mTarget.get();
        if (target == null) {
            return;
        }
        if (current.isEnabled()) {
            target.setAlpha(mChangeAlphaWhenPress && pressed && current.isClickable() ? mPressedAlpha : mNormalAlpha);
        } else {
            if (mChangeAlphaWhenDisable) {
                target.setAlpha(mDisabledAlpha);
            }
        }
    }

    /**
     * @param current the view to be handled, maybe not  equal to target view
     * @param enabled
     */
    public void onEnabledChanged(View current, boolean enabled) {
        View target = mTarget.get();
        if (target == null) {
            return;
        }
        float alphaToApply = determineAlpha(enabled);
        updateEnabledState(current, target, enabled);
        target.setAlpha(alphaToApply);
    }

    private float determineAlpha(boolean enabled) {
        if (!mChangeAlphaWhenDisable) {
            return mNormalAlpha;
        }
        return enabled ? mNormalAlpha : mDisabledAlpha;
    }

    private void updateEnabledState(View current, View target, boolean enabled) {
        if (current != target && target.isEnabled() != enabled) {
            target.setEnabled(enabled);
        }
    }

    /**
     * 设置是否要在 press 时改变透明度
     *
     * @param changeAlphaWhenPress 是否要在 press 时改变透明度
     */
    public void setChangeAlphaWhenPress(boolean changeAlphaWhenPress) {
        mChangeAlphaWhenPress = changeAlphaWhenPress;
    }

    /**
     * 设置是否要在 disabled 时改变透明度
     *
     * @param changeAlphaWhenDisable 是否要在 disabled 时改变透明度
     */
    public void setChangeAlphaWhenDisable(boolean changeAlphaWhenDisable) {
        mChangeAlphaWhenDisable = changeAlphaWhenDisable;
        View target = mTarget.get();
        if (target != null) {
            onEnabledChanged(target, target.isEnabled());
        }

    }

}
