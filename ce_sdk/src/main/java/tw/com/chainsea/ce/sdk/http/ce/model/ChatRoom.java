package tw.com.chainsea.ce.sdk.http.ce.model;

public @interface ChatRoom {
    @interface Type {
        String SYSTEM = "system"; //系統
        String PERSON = "person"; //個人
        String FRIEND = "friend";  //好友
        String GROUP = "group"; //社團
        String DISCUSS = "discuss"; //群療
        String SERVICES = "services"; //服務號
        String SERVICE_MEMBER = "serviceMember"; //服務號成員聊天室
    }
}
