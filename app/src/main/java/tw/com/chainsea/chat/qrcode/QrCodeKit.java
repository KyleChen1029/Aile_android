package tw.com.chainsea.chat.qrcode;

import static tw.com.chainsea.chat.qrcode.Config.CODE;
import static tw.com.chainsea.chat.qrcode.Config.JOIN_TRANS_TENANT;
import static tw.com.chainsea.chat.qrcode.Config.SERVER_PATH;
import static tw.com.chainsea.chat.qrcode.Config.USER_INFO;

import tw.com.chainsea.android.common.hash.AESHelper;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.chat.qrcode.module.TransTenantInfo;
import tw.com.chainsea.chat.qrcode.module.UserInfo;

public class QrCodeKit {
    public String getTransTenantQrCode(String tenantId, String creator){
        return SERVER_PATH + JOIN_TRANS_TENANT + CODE + AESHelper.encryptBase64(JsonHelper.getInstance().toJson(new TransTenantInfo(tenantId, creator)));
    }

    public String getUserInfoQrCode(String tenantCode, String accountId, String tenantName, String userId, String nickName) {
        return SERVER_PATH + USER_INFO + CODE + AESHelper.encryptBase64(JsonHelper.getInstance().toJson(new UserInfo(tenantCode, accountId, tenantName, userId, nickName)));
    }
}
