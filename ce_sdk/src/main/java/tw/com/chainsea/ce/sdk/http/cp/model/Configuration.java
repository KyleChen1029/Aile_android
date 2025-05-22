package tw.com.chainsea.ce.sdk.http.cp.model;

public class Configuration {
    private String socketIoNamespace;
    private int tokenValidSeconds;
    private String socketIoUrl;
    private String socketIoPassword;
    private boolean enableAck;

    public String getSocketIoNamespace() {
        return socketIoNamespace;
    }

    public int getTokenValidSeconds() {
        return tokenValidSeconds;
    }

    public String getSocketIoUrl() {
        return socketIoUrl;
    }

    public String getSocketIoPassword() {
        return socketIoPassword;
    }

    public boolean isEnableAck() {
        return enableAck;
    }
}
