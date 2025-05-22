package tw.com.chainsea.chat.aiff;

import static tw.com.chainsea.chat.aiff.AiffKey.OPEN_CONTACT_CHAT;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.gson.JsonObject;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.PicSize;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.customview.AvatarIcon;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.reference.ServiceNumberReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.lib.ChatService;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.util.IntentUtil;

public class AiffSupport {
    private final String TAG = AiffSupport.class.getSimpleName();
    private final Context context;
    //    private final Gson gson = new Gson();
    private OnBridgeOpenRoom onBridgeOpenRoom;
    private String chatRoomId;

    public String status;

    public String lastFrom;

    public JSONArray otherFroms;

    private String imgBase64 = "";

    private String channel;
    private String methodData;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            String grantedData = getLocationPermissionGrantedData(channel, methodData, location);
            EventBus.getDefault().post(new EventMsg<>(MsgConstant.AIFF_ON_LOCATION_GET, grantedData));
        }
    };

    public AiffSupport(Context context, String chatRoomId) {
        this.context = context;
        this.chatRoomId = chatRoomId;
    }

    public void removeLocationListener() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
    }

    public JsonObject defaultJsonObj() {
        Map<String, String> content = new HashMap<>();
        content.put(AiffKey.CODE, "Aiff.OK");
        return generateJson(content);
    }

    public JsonObject generateJson(Map<String, String> map) {
        return JsonHelper.getInstance().toJsonTree(map).getAsJsonObject();
    }

    public String callJsFunction(String channel, String methodData, JSONObject obj) throws JSONException {
        return generateFuncString(channel, methodData, obj);
    }

    private String generateFuncString(String channel, String methodData, JSONObject obj) throws JSONException {
        String url;
        String functionStr = "";
        String openId;
        String roomId;
        String key;
        String serviceNumberId;
        String type = "";
        String msg = "";
        JsonObject defaultRequest = defaultJsonObj();
        Map<String, String> dataJson = new HashMap<>();

        UserProfileEntity userProfileEntity;
        ChatRoomEntity chatRoomEntity = ChatRoomReference.getInstance().findById(chatRoomId);
        switch (channel) {
            case AiffKey.INIT_LAPP:
                dataJson = getInitData(chatRoomEntity);
                defaultRequest.add(AiffKey.DATA, generateJson(dataJson));
                Map<String, String> info = new HashMap<>();
                JsonObject infoJson = generateJson(info);
                if (chatRoomEntity != null) {
                    infoJson.addProperty(AiffKey.ID, chatRoomEntity.getId());
                    infoJson.addProperty(AiffKey.OPENER_SOURCE, "contactHome");
                    infoJson.addProperty(AiffKey.OPENER_TYPE, OpenerType.returnType(chatRoomEntity.getType().getName(), chatRoomEntity.getOwnerId().equals(TokenPref.getInstance(context).getUserId())));
                    infoJson.addProperty(AiffKey.ROOM_ID, chatRoomEntity.getId());
                    Map<String, String> roomInfo = getOpenerInfo(chatRoomEntity);
                    infoJson.add(AiffKey.ROOM_INFO, generateJson(roomInfo));
                    Map<String, String> userInfo = generateUserInfo(chatRoomEntity);
                    infoJson.add(AiffKey.USER_INFO, generateJson(userInfo));
                }
                defaultRequest.add(AiffKey.INFO, infoJson);
                String adjustDefaultRequest = defaultRequest.toString().replace("\\", "")
                    .replace("\"[", "[")
                    .replace("]\"", "]");
                functionStr = generateJsFunStr(channel, methodData, adjustDefaultRequest);
                break;
            case AiffKey.GET_AVATAR:
                if (!Objects.equals(imgBase64, ""))
                    dataJson.put(AiffKey.IMAGE, AiffKey.PNG_BASE64_HEADER + imgBase64);
                defaultRequest.addProperty(AiffKey.CODE, "Aiff.OK");
                defaultRequest.add(AiffKey.DATA, generateJson(dataJson));
                functionStr = generateJsFunStr(channel, methodData, defaultRequest.toString());
                break;
            case AiffKey.GET_PROFILE:
                userProfileEntity = DBManager.getInstance().queryFriend(TokenPref.getInstance(context).getUserId());
                defaultRequest.addProperty(AiffKey.ID, TokenPref.getInstance(context).getUserId());
                defaultRequest.addProperty(AiffKey.NICKNAME, userProfileEntity.getNickName());
                if (!Objects.equals(imgBase64, ""))
                    defaultRequest.addProperty(AiffKey.IMG_SRC, imgBase64);
                defaultRequest.addProperty(AiffKey.USER_TYPE, userProfileEntity.getUserType().getUserType());
                defaultRequest.addProperty(AiffKey.DEPARTMENT, userProfileEntity.getDepartment());
                defaultRequest.addProperty(AiffKey.AUTH_TOKEN, TokenPref.getInstance(context).getCEAuthToken());
                defaultRequest.addProperty(AiffKey.OPEN_ID, userProfileEntity.getOpenId());
                defaultRequest.addProperty(AiffKey.TENANT_CODE, TokenPref.getInstance(context).getTenantCode());
                functionStr = generateJsFunStr(channel, methodData, defaultRequest.toString());
                break;
            case AiffKey.GET_OPENER:
                functionStr = generateJsFunStr(channel, methodData, defaultRequest.toString());
                break;
//            case AiffKey.AIFF_EVENT:
//                break;
//            case AiffKey.CLOSE_AIFF:
//                break;
            case AiffKey.OPEN_ROOM:
                roomId = obj.getString(AiffKey.ROOM_ID);
                onBridgeOpenRoom.open(roomId);
                // open a room by roomId
                functionStr = generateJsFunStr(channel, methodData, defaultRequest.toString());
                break;
            case AiffKey.OPEN_USER_ROOM:
                openId = obj.getString(AiffKey.OPEN_ID);
                userProfileEntity = DBManager.getInstance().queryFriend(TokenPref.getInstance(context).getUserId());
                if (userProfileEntity.getOpenId().equals(openId)) {
                    onBridgeOpenRoom.close();
                    ActivityTransitionsControl.navigateToChat(context, userProfileEntity.getPersonRoomId(), (intent, s) -> IntentUtil.INSTANCE.start(context, intent));
                } else {
                    userProfileEntity = DBManager.getInstance().queryFriendByOpenId(openId);
                    if (userProfileEntity != null) {
                        if (userProfileEntity.getRoomId() != null && !userProfileEntity.getRoomId().isEmpty()) {
                            onBridgeOpenRoom.close();
                            ActivityTransitionsControl.navigateToChat(context, userProfileEntity.getRoomId(), (intent, s) -> IntentUtil.INSTANCE.start(context, intent));
                        } else {
                            UserProfileEntity finalUserProfileEntity = userProfileEntity;
                            ChatService.getInstance().addContact(new ApiListener<String>() {
                                @Override
                                public void onSuccess(final String roomId) {
                                    onBridgeOpenRoom.close();
                                    ActivityTransitionsControl.navigateToChat(context, roomId, (intent, s) -> IntentUtil.INSTANCE.start(context, intent));
                                    finalUserProfileEntity.setRoomId(roomId);
                                }

                                @Override
                                public void onFailed(String errorMessage) {
                                    onBridgeOpenRoom.close();
                                    ActivityTransitionsControl.navigationToVirtualChat(context, finalUserProfileEntity.getNickName(), finalUserProfileEntity.getId(), (intent, s) -> IntentUtil.INSTANCE.start(context, intent));
                                }
                            }, userProfileEntity.getId(), userProfileEntity.getName());
                        }
                    } else
                        ToastUtils.showToast(context, context.getString(R.string.text_room_not_found));
                }
                functionStr = generateJsFunStr(channel, methodData, defaultRequest.toString());
                break;
//            case AiffKey.GET_USER_ROOM:
//                functionStr = "AileClientAPI.callBack('"+channel+"', '" + data + "')";
//                break;
            case AiffKey.GET_USER:
                openId = obj.getString(AiffKey.OPEN_ID);
                userProfileEntity = DBManager.getInstance().queryFriend(openId);
                dataJson.put(AiffKey.OPEN_ID, userProfileEntity.getOpenId());
                dataJson.put(AiffKey.NICKNAME, userProfileEntity.getNickName());
                dataJson.put(AiffKey.ALIAS, userProfileEntity.getAlias());
                dataJson.put(AiffKey.USER_TYPE, userProfileEntity.getUserType().getUserType());
                dataJson.put(AiffKey.IMAGE, AiffKey.PNG_BASE64_HEADER + imgBase64);
                defaultRequest.add(AiffKey.DATA, generateJson(dataJson));
                functionStr = generateJsFunStr(channel + userProfileEntity.getId(), methodData, defaultRequest.toString());
                break;
//            case AiffKey.GET_ALL_ROOM:
//                functionStr = "AileClientAPI.callBack('"+channel+"(key:"+ key +")"+"', '" + data + "')";
//                break;
            case AiffKey.SEND_MESSAGE: //直接引用並發送消息至聊天室
                type = obj.getString(AiffKey.TYPE);
                msg = obj.getString(AiffKey.MSG);
                onBridgeOpenRoom.quoteAndSend(type, msg);
                break;
            case AiffKey.SELECT_MESSAGE: //引用消息至聊天室輸入框
                msg = obj.getString(AiffKey.MSG);
                onBridgeOpenRoom.quote(msg);
                break;
            case OPEN_CONTACT_CHAT:
                openId = obj.getString(AiffKey.OPEN_ID);
                String userId = ServiceNumberReference.findUserIdByOpenId(openId);
                serviceNumberId = obj.getString(AiffKey.SERVICE_NUMBER_ID);
                roomId = ServiceNumberReference.findRoomIdByUserIdAndServiceNumberId(userId, serviceNumberId);
                if (roomId != null && !roomId.isEmpty()) {
                    onBridgeOpenRoom.close();
                    ActivityTransitionsControl.navigateToChat(context, roomId, (intent, s) -> IntentUtil.INSTANCE.start(context, intent));
                } else {
                    ApiManager.doServiceRoomItem(context, serviceNumberId, userId, 2, openId, UserType.CONTACT.name().toLowerCase(), new ApiListener<ChatRoomEntity>() {
                        @Override
                        public void onSuccess(ChatRoomEntity entity) {
                            onBridgeOpenRoom.close();
                            ActivityTransitionsControl.navigateToChat(context, entity.getId(), (intent, s) -> IntentUtil.INSTANCE.start(context, intent));
                        }

                        @Override
                        public void onFailed(String errorMessage) {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                functionStr = generateJsFunStr(channel, methodData, defaultRequest.toString());
                break;
            case AiffKey.OPEN_BOSS_CONTACT_CHAT:
                break;
            case AiffKey.GET_ROOM_STATUS:
                dataJson.put("status", status);
                dataJson.put("channel", lastFrom);
                dataJson.put("channelList", String.valueOf(otherFroms));
                defaultRequest.add(AiffKey.DATA, generateJson(dataJson));
                String adjustJson = defaultRequest.toString();
                String adjustJson_ = adjustJson.replace("\\", "")
                    .replace("\"[", "[")
                    .replace("]\"", "]");
                functionStr = generateJsFunStr(channel, methodData, adjustJson_);
                break;
            case AiffKey.OPEN_WINDOW:
                String args = obj.getString(AiffKey.ARGS);
                JSONObject jsonObject = JsonHelper.getInstance().toJsonObject(args);
                String url_ = jsonObject.getString(AiffKey.URL);
                boolean external = jsonObject.getBoolean(AiffKey.EXTERNAL);
                if (external)
                    onBridgeOpenRoom.openExternalWindow(url_);
                else
                    onBridgeOpenRoom.openInternalAiffWindow(url_);
                break;
            case AiffKey.SHARE_TARGET_PICKER:
                break;
            case AiffKey.SEND_STATE:
                break;
            case AiffKey.SEND_NUMBER:
                break;
            case AiffKey.GET_AIFF_ITEM:
                break;
            case AiffKey.GET_CURRENT_LOCATION:
                checkLocationPermission(channel, methodData);
                break;
            case AiffKey.SUBSCRIBE_CURRENT_POSITION:
                checkLocationPermission(AiffKey.CURRENT_POSITION_CHANGED, methodData);
                break;
        }
//        Log.d(TAG, "function string = " + functionStr);
        return functionStr;
    }

    private void checkLocationPermission(String channel, String methodData) {
        // 需要精確的位置資訊
        boolean accessFineLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!accessFineLocationPermission) {
            EventBus.getDefault().post(new EventMsg<>(MsgConstant.AIFF_REQUEST_PERMISSION));
            return;
        }
        getLocation(channel, methodData);
    }

    private void getLocation(String channel, String methodData) {
        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String deniedData = getLocationPermissionDeniedData(channel, methodData);
                EventBus.getDefault().post(new EventMsg<>(MsgConstant.AIFF_ON_LOCATION_GET, deniedData));
                return;
            }
            List<String> provides = locationManager.getProviders(true);
            for (String provide : provides) {
                Location location = locationManager.getLastKnownLocation(provide);
                if (location == null) continue;
                String grantedData = getLocationPermissionGrantedData(channel, methodData, location);
                EventBus.getDefault().post(new EventMsg<>(MsgConstant.AIFF_ON_LOCATION_GET, grantedData));
                if (AiffKey.CURRENT_POSITION_CHANGED.equals(channel)) {
                    this.channel = channel;
                    this.methodData = methodData;
                    locationManager.requestLocationUpdates(provide, 0, 0, locationListener);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public String generateJsFunStr(String channel, String methodData, String request) {
        return "AileClientAPI.callBack('" + channel + methodData + "', '" + request + "')";
    }

    private Map<String, String> getInitData(ChatRoomEntity entity) {
        Map<String, String> data = new HashMap<>();
        UserProfileEntity userProfileEntity = DBManager.getInstance().queryFriend(TokenPref.getInstance(context).getUserId());
        data.put(AiffKey.ID, TokenPref.getInstance(context).getUserId());
        data.put(AiffKey.NICKNAME, userProfileEntity.getNickName());
        data.put(AiffKey.USER_TYPE, userProfileEntity.getUserType().getUserType());
        data.put(AiffKey.OPEN_ID, userProfileEntity.getOpenId());
        data.put(AiffKey.AUTH_TOKEN, TokenPref.getInstance(context).getCEAuthToken());
        data.put(AiffKey.TENANT_CODE, TokenPref.getInstance(context).getTenantCode());
        data.put(AiffKey.ACCOUNT_ID, TokenPref.getInstance(context).getCpAccountId());
        data.put(AiffKey.APP_TYPE, "android");
        data.put(AiffKey.CLIENT_TYPE, "sdk");
        if (entity != null && entity.getAvatarId() != null) {
            imgBase64 = encodeToBase64(getBitmap(AvatarService.getAvatarUrl(context, entity.getAvatarId(), PicSize.LARGE)));
        } else {
            if (userProfileEntity.getAvatarId() != null && !userProfileEntity.getAvatarId().isEmpty()) {
                imgBase64 = encodeToBase64(getBitmap(AvatarService.getAvatarUrl(context, userProfileEntity.getAvatarId(), PicSize.LARGE)));
            } else {
                String uniqueId = userProfileEntity.getId();
                String userName = userProfileEntity.getNickName();
                String avatarPic = context.getApplicationContext().getFilesDir().getAbsoluteFile() + "/avatars/" + uniqueId + userName + ".jpg";
                Bitmap avatarBitmap = null;
                File file = new File(avatarPic);
                if (file.exists()) {
                    avatarBitmap = BitmapFactory.decodeFile(avatarPic);
                } else {
                    try {
                        avatarBitmap = new AvatarIcon(context).getTextAvatarFromJava(userProfileEntity.getNickName()).get();
                    } catch (Exception e) {
                        CELog.e(e.getMessage());
                    }
                }
                if (avatarBitmap != null) {
                    imgBase64 = encodeToBase64(avatarBitmap);
                }
            }
            //imgBase64 = encodeToBase64(createBitmapOfName(userProfileEntity.getNickName()));
        }
        data.put(AiffKey.IMAGE, AiffKey.PNG_BASE64_HEADER + imgBase64);
        data.put("department", userProfileEntity.getDepartment());
        return data;
    }

    private Map<String, String> generateUserInfo(ChatRoomEntity entity) {
        Map<String, String> info = new HashMap<>();
        UserProfileEntity userProfileEntity = DBManager.getInstance().queryFriend(entity.getOwnerId());
        if (userProfileEntity != null) {
            info.put(AiffKey.CUSTOMER_ALIAS, userProfileEntity.getAlias());
            info.put(AiffKey.CUSTOMER_NICK_NAME, userProfileEntity.getOriginName());
            info.put(AiffKey.CUSTOMER_OPEN_ID, userProfileEntity.getOpenId());
        }
        return info;
    }

    private Map<String, String> getOpenerInfo(ChatRoomEntity entity) {
        Map<String, String> info = new HashMap<>();
        List<UserProfileEntity> members = entity.getMembers();
        ArrayList<String> roomMembersId = new ArrayList<>();
        for (UserProfileEntity member : members) {
            roomMembersId.add(member.getOpenId());
        }

//        Gson gson = new Gson().newBuilder().create();
        info.put(AiffKey.ROOM_NAME, entity.getName());
        info.put(AiffKey.ROOM_MEMBERS, JsonHelper.getInstance().toJson(roomMembersId));

        for (int i = 0; i < members.size(); i++) {
            if (entity.getOwnerId().equals(members.get(i).getId())) {
                info.put(AiffKey.ROOM_OWNER_ID, members.get(i).getOpenId());
            }
        }
        info.put(AiffKey.SERVICE_ID, entity.getServiceNumberId());
        if (!Objects.equals(imgBase64, ""))
            info.put(AiffKey.ROOM_AVATAR, AiffKey.PNG_BASE64_HEADER + imgBase64);
        return info;
    }

    public void setBridgeOpenRoom(OnBridgeOpenRoom onBridgeOpenRoom) {
        this.onBridgeOpenRoom = onBridgeOpenRoom;
    }

    public String getLocationPermissionDeniedData(String channel, String methodData) {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject location = new JSONObject();
            location.put("latitude", JSONObject.NULL);
            location.put("longitude", JSONObject.NULL);
            location.put("status", false);
            jsonObject.put(AiffKey.CODE, "Aiff.OK");
            jsonObject.put(AiffKey.DATA, location);
            return generateJsFunStr(channel, methodData, jsonObject.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public String getLocationPermissionGrantedData(String channel, String methodData, Location gpsLocation) {
        if (gpsLocation == null) return null;
        try {
            JSONObject jsonObject = new JSONObject();
            JSONObject location = new JSONObject();
            location.put("latitude", gpsLocation.getLatitude());
            location.put("longitude", gpsLocation.getLongitude());
            location.put("status", true);
            Geocoder geocoder = new Geocoder(context);
            try {
                List<Address> address = geocoder.getFromLocation(gpsLocation.getLatitude(), gpsLocation.getLongitude(), 1);
                if (address != null && !address.isEmpty()) {
                    location.put("address", address.get(0).getAddressLine(0));
                }
            } catch (Exception e) {

            }
            jsonObject.put(AiffKey.CODE, "Aiff.OK");
            jsonObject.put(AiffKey.DATA, location);
            return generateJsFunStr(channel, methodData, jsonObject.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public Bitmap getBitmap(String urlPath) {
        Bitmap map = null;
        try {
            URL url = new URL(urlPath);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream in;
            in = conn.getInputStream();
            map = BitmapFactory.decodeStream(in);
            // TODO Auto-generated catch block
        } catch (IOException ignored) {
        }
        return map;
    }

    public static String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.NO_WRAP);
    }

}
