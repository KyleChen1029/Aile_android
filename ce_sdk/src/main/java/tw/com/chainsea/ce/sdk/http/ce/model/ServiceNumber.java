package tw.com.chainsea.ce.sdk.http.ce.model;

public @interface ServiceNumber {
    @interface Type{
        String BOSS = "Boss"; // 商業服務號
        String PROFESSIONAL = "Professional"; // 專業服務號
        String NORMAL = "Normal"; // 一般服務號
        String OFFICIAL = "Official"; // 官方服務號
        String MANAGER = "Manage"; // 管理服務號
    }

    @interface OpenType{
        String OPEN = "P"; //全部開放
        String INSIDE = "I"; //對內
        String OUTSIDE = "O"; //對外
        String CONSULT = "C"; //諮詢
    }

    @interface PrivilegeType{
        String OWNER = "OWNER";
        String MANAGER = "MANAGER";
        String COMMON = "COMMON";
    }
}
