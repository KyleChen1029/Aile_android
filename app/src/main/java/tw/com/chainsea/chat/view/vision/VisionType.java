package tw.com.chainsea.chat.view.vision;

import androidx.annotation.StringRes;

import com.google.gson.annotations.SerializedName;

import tw.com.chainsea.chat.R;

/**
 * current by evan on 11/26/20
 *
 * @author Evan Wang
 * date 11/26/20
 */
// JoinTenantSchemeReceiver

public enum VisionType {
    @SerializedName("/joinTenant") JOIN_TENANT("/joinTenant", R.string.barcode_detector_scan_title),
    @SerializedName("/barcodeScan") SCAN_BAR_CODE("/barcodeScan", R.string.barcode_detector_scan_title);

    private final String path;

    @StringRes
    private int titleResId;


    VisionType(String path, int titleResId) {
        this.path = path;
        this.titleResId = titleResId;
    }

    public String getPath() {
        return path;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public static int getScanBarCodeRequestCode() {
        return SCAN_BAR_CODE_REQUEST_CODE;
    }

    public static VisionType of(String path) {
        for (VisionType t : values()) {
            if (t.getPath().equals(path)) {
                return t;
            }
        }
        return SCAN_BAR_CODE;
    }


    public static final int SCAN_BAR_CODE_REQUEST_CODE = 1009;
}