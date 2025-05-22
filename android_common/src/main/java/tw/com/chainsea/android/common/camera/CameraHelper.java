package tw.com.chainsea.android.common.camera;

import android.hardware.Camera;

import com.google.android.gms.common.images.Size;

import java.util.List;

/**
 * current by evan on 12/1/20
 *
 * @author Evan Wang
 * @date 12/1/20
 */
public class CameraHelper {
    /**
     * Get the preview size closest to the aspect ratio by comparison (if the same size is available, the preferred choice)
     *
     * @param isPortrait    Whether the screen is vertical
     * @param surfaceWidth  Original width to be compared
     * @param surfaceHeight Original height to be compared
     * @param preSizeList   List of preview sizes to be compared
     * @return Get the size closest to the original width and height ratio
     */
    public static Camera.Size getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
        int reqTmpWidth;
        int reqTmpHeight;
        // When the screen is vertical, you need to change the width and height values ​​to ensure that the width is greater than the height
        if (isPortrait) {
            reqTmpWidth = surfaceHeight;
            reqTmpHeight = surfaceWidth;
        } else {
            reqTmpWidth = surfaceWidth;
            reqTmpHeight = surfaceHeight;
        }
        // First query whether there is a size with the same width and height as the surfaceview in the preview
        for (Camera.Size size : preSizeList) {
            if ((size.width == reqTmpWidth) && (size.height == reqTmpHeight)) {
                return size;
            }
        }

        // Get the size closest to the incoming aspect ratio
        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    public static Size getCloselyPreSize2(boolean isPortrait, int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
        int reqTmpWidth;
        int reqTmpHeight;
        // When the screen is vertical, you need to change the width and height values ​​to ensure that the width is greater than the height
        if (isPortrait) {
            reqTmpWidth = surfaceHeight;
            reqTmpHeight = surfaceWidth;
        } else {
            reqTmpWidth = surfaceWidth;
            reqTmpHeight = surfaceHeight;
        }
        // First query whether there is a size with the same width and height as the surfaceview in the preview
        for (Camera.Size size : preSizeList) {
            if ((size.width == reqTmpWidth) && (size.height == reqTmpHeight)) {
                return new com.google.android.gms.common.images.Size(size.width, size.height);
            }
        }

        // Get the size closest to the incoming aspect ratio
        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return new com.google.android.gms.common.images.Size(retSize.width, retSize.height);
//        return retSize;
    }

}


