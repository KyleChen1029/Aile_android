package tw.com.chainsea.ce.sdk.config;

public @interface CpConfig {
    @interface TRANS_MEMBER_STATUS{
        String OWNER = "-1";
        String WAIT_CONFIRM = "0";
        String ALREADY_JOIN = "1";
    }
}