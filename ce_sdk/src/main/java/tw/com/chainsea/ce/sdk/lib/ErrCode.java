package tw.com.chainsea.ce.sdk.lib;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by 90Chris on 2016/7/22.
 */
public enum ErrCode {
    UNDEF("undef"),
    INVALID_LOGIN_NAME_OR_PASSWORD("Qs.Auth.InvalidLoginNameOrPassword"),
    JSON_PARSE_FAILED("JsonParseFailed"),
    TOKEN_INVALID("Qs.Token.Invalid"),
    TOKEN_REQUIRED("Qs.Token.Required"),
    THRID_LOGIN_UNBIND("Ce.User.ThridLoginUnBind"),
    USER_SQUEEZED_OUT("Ce.User.SqueezedOut"),
    CE_SERVICE_NUMBER_BROADCAST_CHAT_MESSAGE_NOT_EXIST("Ce.ServiceNumber.Broadcast.ChatMessageNotExist"),
    RESPONSE_PARAMETER_NOT_FOUND("Response parameter not found"),
    REQUEST_PARAMETER_NOT_FOUND("Request parameter not found"),
    USER_ALREADY_EXISTS("Ce.User.UserAlreadyExists"),
    //CP
    CP_REFRESH_TOKEN_NOT_EXIST("CP.Login.RefreshToken.NotExist"),
    CP_REFRESH_TOKEN_EXPIRED("CP.Login.RefreshToken.Expired"),
    CP_SQUEEZED_OUT("CP.Login.Device.SqueezedOut"),
    TENANT_ALREADY_READY("CP.Tenant.Trans.AlreadyHad"),
    MOBILE_NOT_EXIST("CP.Account.Mobile.NotExist"),
    TENANT_USER_IS_ALREADY("CP.Account.TenantUser.IsAlready"),
    TENANT_USER_NOT_EXIST("CP.Account.TenantUser.NotExist"),
    GUARANTOR_NOT_JOIN("CP.Tenant.Guarantor.NotJoin"),
    GUARANTOR_ALREADY_AGREE("CP.Tenant.Guarantor.AlreadyAgree");


    private String value;

    ErrCode(String value) {
        this.value = value;
    }

    public final String getValue() {
        return value;
    }

    public static ErrCode of(String value) {
        for (ErrCode code : values()) {
            if (code.getValue().equals(value)) {
                return code;
            }
        }
        return UNDEF;
    }


    public static Set<ErrCode> TOKEN_INVALID_or_TOKEN_REQUIRED = Sets.newHashSet(TOKEN_INVALID, TOKEN_REQUIRED);
}
