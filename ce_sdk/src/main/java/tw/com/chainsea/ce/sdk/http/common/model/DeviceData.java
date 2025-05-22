package tw.com.chainsea.ce.sdk.http.common.model;

import static tw.com.chainsea.android.common.system.DeviceHelper.getUUID;
import android.os.Build;
import tw.com.chainsea.android.common.CommonLib;
import tw.com.chainsea.ce.sdk.config.AppConfig;

public class DeviceData {
    private final String deviceName;
    private final String osType;
    private final String bundleId;
    private final String uniqueID;
    private final String version;

    public DeviceData() {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        this.deviceName = (bluetoothAdapter != null && bluetoothAdapter.getName() != null) ? bluetoothAdapter.getName() : Build.DEVICE;
        this.deviceName = Build.DEVICE;
        this.osType = AppConfig.osType;
        this.bundleId = CommonLib.packageName;
        this.uniqueID = getUUID();
        this.version = CommonLib.getInstant().getVersionName();
    }
}