package tw.com.chainsea.chat.config;

import tw.com.chainsea.chat.BuildConfig;

public interface SystemConfig {
    boolean isCpMode = BuildConfig.isCpMode;
    int smsCountdownTime = 60;
    boolean enableBroadcast = false;
}
