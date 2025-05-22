package com.bigkoo.pickerview.lib;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

final class LoopViewGestureListener extends android.view.GestureDetector.SimpleOnGestureListener {

    final WheelView loopView;

    LoopViewGestureListener(WheelView loopview) {
        loopView = loopview;
    }

    @Override
    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        loopView.scrollBy(velocityY);
        return true;
    }
}
