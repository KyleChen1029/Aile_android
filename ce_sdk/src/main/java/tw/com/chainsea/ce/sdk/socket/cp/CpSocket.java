package tw.com.chainsea.ce.sdk.socket.cp;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.net.URI;
import java.util.Objects;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.Polling;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.WebSocket;
import tw.com.chainsea.android.common.client.ClientsManager;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.SdkLib;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.event.SocketEvent;
import tw.com.chainsea.ce.sdk.event.SocketEventEnum;
import tw.com.chainsea.ce.sdk.http.common.model.DeviceData;
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;
import tw.com.chainsea.ce.sdk.socket.ce.bean.AckBean;
import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeName;
import tw.com.chainsea.ce.sdk.socket.cp.model.CpMessage;
import tw.com.chainsea.ce.sdk.socket.cp.model.Data;
import tw.com.chainsea.ce.sdk.socket.cp.model.DeviceLoginContent;
import tw.com.chainsea.ce.sdk.socket.cp.model.GuarantorJoinAgreeContent;
import tw.com.chainsea.ce.sdk.socket.cp.model.GuarantorJoinContent;
import tw.com.chainsea.ce.sdk.socket.cp.model.SqueezedOutContent;
import tw.com.chainsea.ce.sdk.socket.cp.model.TenantDeleteMember;
import tw.com.chainsea.ce.sdk.socket.cp.model.TransTenantJoinContent;

public class CpSocket {
    public final static String DEV_SERVER_SOCKET = "https://cp.dev.aile.cloud/cp";
    public final static String QA_SERVER_SOCKET = "https://cp.qa.aile.cloud/cp";
    public final static String UAT_SERVER_SOCKET = "https://cp.uat.aile.cloud/cp";
    public final static String FORMAL_SERVER = "https://cp.aile.cloud:16922/cp"; //正式區
    public static String BASE_URL = FORMAL_SERVER; //正式區
    private static CpSocket instance;
    private final IO.Options options;
    //    private final SocketLogger logger;
//    private final Gson gson;
    private Socket socket = null;

    private CpSocket() {
        options = new IO.Options();
        options.reconnection = true;
        options.reconnectionDelay = 1000L;
        options.reconnectionDelayMax = 5000L;
        options.reconnectionAttempts = Integer.MAX_VALUE;
        options.transports = new String[]{Polling.NAME};
        options.secure = true;
        options.timeout = 30 * 1000L;
        options.timestampRequests = true;
//        logger = new SocketLogger();

        OkHttpClient client = ClientsManager.initClient()
            .threadPoolNumber(1)
            .giveLog(true)
//                .logger(logger)
            .single(true)
            .newSocketBuild();

        IO.setDefaultOkHttpWebSocketFactory((WebSocket.Factory) client);
        IO.setDefaultOkHttpCallFactory((Call.Factory) client);
        options.callFactory = (Call.Factory) client;
        options.webSocketFactory = (WebSocket.Factory) client;
    }

    public static synchronized CpSocket getInstance() {
        if (instance == null) {
            instance = new CpSocket();
        }
        return instance;
    }

    public void connect(String url, String namespace, String name, String deviceId) {
        CELog.d("[CP Socket] start() " + System.currentTimeMillis());
        if (socket != null) {
            socket.close();
            socket = null;
        }

        try {
            options.forceNew = true;
            options.query = "deviceId=" + deviceId
                + "&" + "name=" + name
                + "&" + "osType=" + "android"
                + "&" + "enableAck=" + "1" //暫時不用ack
                + "&" + "serviceUrl=" + BASE_URL //固定位置
                + "&" + "deviceData=" + JsonHelper.getInstance().toJson(new DeviceData());
//            options.query = "deviceId=" + "61822f578f0818f244e15eca"
//                    + "&" + "name=" + "qq"
//                    + "&" + "osType=" + "android"
//                    + "&" + "enableAck=" + "1"
//                    + "&" + "serviceUrl=" + "https://csaile-qaaiwow.qbicloud.com:16622/cp"
//                    + "&" + "deviceData=" + "{\"deviceName\":\"client\",\"uniqueID\":\"ffffffff-bae3-ef32-ffff-ffffef05ac4a\",\"osType\":\"android\",\"bundleId\":\"tw.com.chainsea.chat\"}";

            CELog.d("options.query = " + options.query);
            Manager manager = new Manager(new URI(url), options);
            socket = manager.socket(namespace);
//            boolean ackEnable = TokenPref.getInstance(context).isSocketAckEnable();
//            onEvent(socket, ackEnable);
            setSocket();
            socket.connect();
        } catch (Exception e) {
            CELog.e("[CP Socket] " + e.getMessage());
        }
    }

    private void setSocket() {
        socket
            .on(Socket.EVENT_CONNECT, args -> CELog.d("[CP Socket] connect"))
            .on(Socket.EVENT_DISCONNECT, args -> CELog.d("[CP Socket] disconnect"))
            .on(Socket.EVENT_MESSAGE, args -> {
                CELog.d("[CP Socket] receive");
                try {
                    JSONObject jsonObject = (JSONObject) args[0];
                    CELog.d("[CP Socket] " + jsonObject.toString());
                    CpMessage messages = JsonHelper.getInstance().from(jsonObject.toString(), CpMessage.class);
                    if (messages.getName().equals(CpSocketType.NAME.NOTICE)) {
                        Data data = messages.getData();
                        SocketEventEnum enumType = SocketEventEnum.valueOf(data.getEvent());
                        JsonObject content = JsonHelper.getInstance().toJsonTree(data.getContent()).getAsJsonObject();
                        switch (enumType) {
                            case TenantDeleteMember: //退出團隊
                                TenantDeleteMember tenantDeleteMember = JsonHelper.getInstance().from(content, TenantDeleteMember.class);
                                EventBus.getDefault().post(new SocketEvent(enumType, tenantDeleteMember));
                                break;
                            case TransTenantExit:
                            case TransTenantJoinReject: //被拒絕加入
                            case GuarantorJoinReject: //拒絕加入團隊
                            case ForceLogout:
                            case TenantUnReadNum:
                                EventBus.getDefault().post((new SocketEvent(enumType)));
                                break;
                            case SqueezedOut:
                                SqueezedOutContent squeezedOutContent = JsonHelper.getInstance().from(content, SqueezedOutContent.class);
                                EventBus.getDefault().post((new SocketEvent(enumType, squeezedOutContent)));
                                break;
                            case DeviceLogin:
                                DeviceLoginContent deviceLoginContent = JsonHelper.getInstance().from(content, DeviceLoginContent.class);
                                EventBus.getDefault().post((new SocketEvent(enumType, deviceLoginContent)));
                                break;
                            case GuarantorJoin:
                                GuarantorJoinContent guarantorJoinContent = JsonHelper.getInstance().from(content, GuarantorJoinContent.class);
                                EventBus.getDefault().post((new SocketEvent(enumType, guarantorJoinContent)));
                                break;
                            case TenantJoinAgree:
                            case GuarantorJoinAgree: // 同意入團隊
                                GuarantorJoinAgreeContent guarantorJoinAgreeContent = JsonHelper.getInstance().from(content, GuarantorJoinAgreeContent.class);
                                EventBus.getDefault().post((new SocketEvent(enumType, guarantorJoinAgreeContent)));
                                break;
                            case TransTenantJoinAgree:
                                TransTenantInfo transTenantInfo = JsonHelper.getInstance().from(content, TransTenantInfo.class);
                                EventBus.getDefault().post((new SocketEvent(enumType, transTenantInfo)));
                                break;
                            case TransTenantJoin:
                            case TransTenantMemberAdd:
                            case TransTenantDismiss:
                                TransTenantJoinContent tenantJoinContent = JsonHelper.getInstance().from(content, TransTenantJoinContent.class);
                                EventBus.getDefault().post(new SocketEvent(enumType, tenantJoinContent));
                                break;
                            case TransTenantActive:
                                RelationTenant tenantInfo = JsonHelper.getInstance().from(content, RelationTenant.class);
                                EventBus.getDefault().post(new SocketEvent(enumType, tenantInfo));
                                break;
                            case LoginSuccess:
                                EventBus.getDefault().post(new EventMsg<>(MsgConstant.DESKTOP_LOGIN_SUCCESS));
                                break;
                        }
                    } else {
                        CELog.d("[CP Socket] new type message");
                    }
                    sendAck(args);
                } catch (Exception e) {
                    CELog.e("[CP Socket] Error : " + e.getMessage());
                }
            });
    }

    public void sendAck(Object... object) {
        if (object != null) {
            Ack ack = (Ack) object[object.length - 1];
            String deviceName = TokenPref.getInstance(SdkLib.getAppContext()).getDeviceName();
            AckBean.AckBeanBuilder builder = AckBean.Build().ack(true)
                    .event(CpSocketType.NAME.NOTICE);
            builder.deviceName(deviceName);
            try {
                JSONObject jsonObject = builder.build().toJsonObject();
                ack.call(jsonObject);
            } catch (Exception e) {
                CELog.e("[CE Socket] " + e.getMessage());
            }
        }
    }

    public void disconnect() {
        if (socket != null) {
            CELog.d("[CP Socket] disconnect() " + System.currentTimeMillis());
            socket.disconnect();
            socket.off();
            socket.close();
        }
    }

//    public void emitAck(Ack ack, AckBean.AckBeanBuilder builder) {
//        if (ack != null) {
//            try {
//                JSONObject jsonObject = builder.build().toJsonObject();
//                ack.call(jsonObject);
//            } catch (Exception e) {
//                logger.e(e.getMessage());
//            }
//        }
//    }
}
