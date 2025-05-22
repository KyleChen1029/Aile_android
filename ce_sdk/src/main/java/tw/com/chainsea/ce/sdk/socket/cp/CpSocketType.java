package tw.com.chainsea.ce.sdk.socket.cp;

public @interface CpSocketType {
    @interface NAME {
        String NOTICE = "CP.Notice";
    }

    @interface CODE {
        String TENANT = "CP.Tenant";
        String LOGIN = "CP.Login";
    }
}