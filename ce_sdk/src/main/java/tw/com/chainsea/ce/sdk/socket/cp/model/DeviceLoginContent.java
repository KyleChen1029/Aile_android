package tw.com.chainsea.ce.sdk.socket.cp.model;

public class DeviceLoginContent {
    private String deviceName;
    private String onceToken;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOnceToken() {
        return onceToken;
    }

    public void setOnceToken(String onceToken) {
        this.onceToken = onceToken;
    }
}
