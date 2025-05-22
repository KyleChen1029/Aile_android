package tw.com.chainsea.ce.sdk.http.cp;

public @interface CpApiPath {
    String DEV_SERVER = "https://cp.dev.aile.cloud/cp/openapi/";
    String QA_SERVER = "https://cp.qa.aile.cloud/cp/openapi/";
    String UAT_SERVER = "https://cp.uat.aile.cloud/cp/openapi/";
    String FORMAL_SERVER = "https://cp.aile.cloud:16922/cp/openapi/"; //正式區
    //Login
    String LOGIN = "account/login";
    String LOGIN_DEVICE_CHECK = "account/login/device/check";
    String ACCOUNT_LOGIN_CHECK_CODE_VALIDATE = "account/login/checkcode/validate";
    String ACCOUNT_LOGIN_DEVICE_SCAN = "account/login/device/scan";
    String ACCOUNT_LOGIN_DEVICE_AGREE = "account/login/device/agree";
    String ACCOUNT_LOGIN_DEVICE_REJECT = "account/login/device/reject";
    String ACCOUNT_DEVICE_RECORD_REMEMBER_ADD = "account/device/record/rememberme/add";
    String ACCOUNT_DEVICE_RECORD_REMEMBER_REMOVE = "account/device/record/rememberme/remove";
    //Logout
    String LOGOUT = "account/logout";
    //Register
    String REGISTER = "account/register";
    //Token
    String TOKEN_ANEW = "token/anew"; //延長使用期
    String TOKEN_REFRESH = "token/refresh"; //換一個新的
    //CheckCode
    String ACCOUNT_CHECK_CODE_SEND = "account/checkcode/send";
    //Trans Tenant
    String TENANT_TRANS_CREATE = "tenant/trans/create";
    String TENANT_TRANS_DISMISS = "tenant/trans/dismiss";
    String TENANT_TRANS_JOIN = "tenant/trans/join";
    String TENANT_TRANS_ACTIVE = "tenant/trans/active";
    String TENANT_TRANS_MEMBER_JOIN_AGREE = "tenant/trans/member/join/agree";
    String TENANT_TRANS_MEMBER_JOIN_REJECT = "tenant/trans/member/join/reject";
    String TENANT_TRANS_MEMBER_REMOVE = "tenant/trans/member/remove";
    String TENANT_TRANS_MEMBER_EXIT = "tenant/trans/member/exit";
    //Tenant
    String TENANT_RELATION_LIST = "tenant/relation/list";
    String TENANT_GUARANTOR_ADD = "tenant/guarantor/add";
    String TENANT_GUARANTOR_AGREE = "tenant/guarantor/agree";
    String TENANT_GUARANTOR_REJECT = "tenant/guarantor/reject";
    String TENANT_GUARANTOR_CANCEL = "tenant/guarantor/cancel";
    String TENANT_DICTIONARY_INDUSTRY = "tenant/dictionary/industry";
    String TENANT_DICTIONARY_SCALE = "tenant/dictionary/scale";
    String TENANT_SUPPORT_FILE_UPLOAD = "tenant/supportfile/upload";
    String TENANT_UPDATE = "/openapi/tenant/update";
    String TENANT_AVATAR_URL = "base/avatar/view";
    String TENANT_ITEM = "tenant/item";
    String TENANT_UPGRADE = "tenant/upgrade";
    //Delete account
    String DELETE = "account/delete";
    //Repair
    String BASE_REPAIR = "base/repair";
    //Check version
    String BASE_VERSION_CHECK = "base/version/check";
    String JOIN_TENANT_INVITE_CODE = "tenant/invitation/code";
    String TENANT_JOIN = "tenant/join";
    String  DEVICE_RECORD_LIST= "account/device/record/list"; //载具列表
    String DEVICE_RECORD_DELETE= "account/device/record/delete"; //删除载具
    String DEVICE_RECORD_REMEMBER_ME_ADD= "account/device/record/rememberme/add"; //设置自动登录(记住我)
    String DEVICE_RECORD_REMEMBER_ME_REMOVE= "account/device/record/rememberme/remove"; //取消自动登录(记住我)
    String DEVICE_LOGOUT_FORCE = "account/logout/force";
}
