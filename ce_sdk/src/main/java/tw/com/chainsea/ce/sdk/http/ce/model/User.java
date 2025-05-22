package tw.com.chainsea.ce.sdk.http.ce.model;

public @interface User {
    @interface Status {
        String ENABLE = "Enable"; //開啟
        String DISABLE = "Disable"; //關閉
        String DELETED = "Deleted";  //刪除
    }
    @interface Type {
        String VISITOR = "visitor"; //訪客
        String CONTACT = "contact"; //客戶
        String EMPLOYEE = "employee";  //夥伴
    }
}
