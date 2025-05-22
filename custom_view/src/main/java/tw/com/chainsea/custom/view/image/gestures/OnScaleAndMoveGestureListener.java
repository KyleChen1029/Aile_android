package tw.com.chainsea.custom.view.image.gestures;

import android.view.ScaleGestureDetector;

/**
 * current by evan on 2019-11-01
 */
public interface OnScaleAndMoveGestureListener {
    void onScaleBegin(ScaleGestureDetector detector);

    void onScaleEnd(ScaleGestureDetector detector, float moveDistanceFromX, float moveDistanceFromY);

    void onScaleAndMove(ScaleGestureDetector detector, float currentScale, float moveDistanceFromX, float moveDistanceFromY);

}
