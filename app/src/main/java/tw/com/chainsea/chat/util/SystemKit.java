package tw.com.chainsea.chat.util;

import static tw.com.chainsea.ce.sdk.config.CpConfig.TRANS_MEMBER_STATUS.ALREADY_JOIN;
import static tw.com.chainsea.ce.sdk.config.CpConfig.TRANS_MEMBER_STATUS.OWNER;
import static tw.com.chainsea.ce.sdk.config.CpConfig.TRANS_MEMBER_STATUS.WAIT_CONFIRM;
import static tw.com.chainsea.ce.sdk.database.DBContract.REFRESH_TIME_SOURCE.FIRST_LOAD;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.widget.Toast;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import tw.com.chainsea.android.common.client.ClientsManager;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.response.AileTokenApply;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TenantPref;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.request.TokenApplyRequest;
import tw.com.chainsea.ce.sdk.http.cp.model.TransMember;
import tw.com.chainsea.ce.sdk.http.cp.model.TransTenantInfo;
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
import tw.com.chainsea.ce.sdk.service.AvatarService;
import tw.com.chainsea.ce.sdk.service.ChatRoomService;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.ce.sdk.socket.ce.SocketManager;
import tw.com.chainsea.ce.sdk.socket.cp.CpSocket;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.aiff.database.AiffDB;
import tw.com.chainsea.chat.aiff.database.dao.AiffInfoDao;
import tw.com.chainsea.chat.aiff.database.entity.AiffInfo;
import tw.com.chainsea.chat.aiff.database.entity.AiffInfoUpdate;
import tw.com.chainsea.chat.config.AiffEmbedLocation;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.config.Const;
import tw.com.chainsea.chat.lib.ActivityManager;
import tw.com.chainsea.chat.lib.ChatService;
import tw.com.chainsea.chat.lib.NetworkUtils;
import tw.com.chainsea.chat.lib.ToastUtils;
import tw.com.chainsea.chat.refactor.loginPage.LoginCpActivity;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.ui.dialog.IosProgressDialog;
import tw.com.chainsea.chat.view.base.HomeActivity;
import tw.com.chainsea.chat.view.base.PreloadStepEnum;
import tw.com.chainsea.chat.view.group.GroupCreateActivity;
import tw.com.chainsea.chat.view.group.GroupWaitActivity;
import tw.com.chainsea.chat.view.group.GroupWaitConfirmActivity;

public class SystemKit {
    /**
     * 登入CE該存儲的資訊
     */
    public static void saveCEInfo(AileTokenApply.Resp resp) {
        ArrayList<AileTokenApply.Resp.AiffInfo> aiffInfo = resp.getAiffInfo();
        if (aiffInfo != null) {
            AiffDB db = AiffDB.getInstance(App.getContext());
            AiffInfoDao aiffInfoDao = db.getAiffInfoDao();
            List<AiffInfo> aiffInfoList = aiffInfoDao.getAll();
            if (aiffInfoList.isEmpty()) {
                for (AileTokenApply.Resp.AiffInfo i : aiffInfo) {
                    if (i.getEmbedLocation().equals(AiffEmbedLocation.ChatRoomMenu.name()) ||
                        i.getEmbedLocation().equals(AiffEmbedLocation.MessageMenu.name()) ||
                        i.getEmbedLocation().equals(AiffEmbedLocation.ContactHome.name()) &&
                            i.getSupportDevice().contains(Const.APP)
                    ) {
                        AiffInfo info = new AiffInfo(i.getId(), i.getTenantId(), i.getIndex(), i.getDisplayLocation(),
                            i.getPictureId(), i.getUrl(), i.getUserType(), i.getEmbedLocation(), i.getTitle(), i.getDisplayType(),
                            i.getName(), i.getAiffURL(), i.getApplyType(), i.getCloseInAiff(), i.getTenantId$(), i.getIncomingAiff()
                            , JsonHelper.getInstance().toJson(i.getServiceNumberIds()), i.getUserType_APP()
                            , JsonHelper.getInstance().toJson(i.getServiceNumberNames()), i.getStatus());
                        try {
                            aiffInfoDao.upsert(info);
                        } catch (SQLiteConstraintException exception) {
                            CELog.e("SQLiteConstraintException = " + exception);
                        } catch (Throwable throwable) {
                            CELog.e("Error = " + throwable.getMessage());
                        }
                        //CELog.d("Kyle2 add name="+i.getName()+", displayLocation="+i.getDisplayLocation()+", embedLocation="+i.getEmbedLocation()+", supportDevice="+i.getSupportDevice());
                    }
                }
            } else {
                for (AileTokenApply.Resp.AiffInfo i : aiffInfo) {
                    if (i.getEmbedLocation().equals(AiffEmbedLocation.ChatRoomMenu.name()) ||
                        i.getEmbedLocation().equals(AiffEmbedLocation.MessageMenu.name()) ||
                        i.getEmbedLocation().equals(AiffEmbedLocation.ContactHome.name()) &&
                            i.getSupportDevice().contains(Const.APP)
                    ) {
                        AiffInfoUpdate info = new AiffInfoUpdate(i.getId(), i.getTenantId(), i.getIndex(), i.getDisplayLocation(),
                            i.getPictureId(), i.getUrl(), i.getUserType(), i.getEmbedLocation(), i.getTitle(), i.getDisplayType(),
                            i.getName(), i.getAiffURL(), i.getApplyType(), i.getCloseInAiff(), i.getTenantId$(), i.getIncomingAiff()
                            , JsonHelper.getInstance().toJson(i.getServiceNumberIds()), i.getUserType_APP()
                            , JsonHelper.getInstance().toJson(i.getServiceNumberNames()), i.getStatus());
                        try {
                            aiffInfoDao.updateAiffInfo(info);
                        } catch (SQLiteConstraintException exception) {
                            CELog.e("SQLiteConstraintException = " + exception);
                        } catch (Throwable throwable) {
                            CELog.e("Error = " + throwable.getMessage());
                        }
                    }
                }
            }
        }

        AileTokenApply.Resp.User user = resp.getUser();
        if (user != null) {
            TokenPref.getInstance(App.getContext()).setUserId(user.getId()).setUserResp(user);
            if (user.getUserType() != null) UserPref.getInstance(App.getContext()).saveUserType(user.getUserType().getUserType());
            DBManager.getInstance().insertUserAndFriends(new UserProfileEntity(user));
        }
        AileTokenApply.Resp.TenantInfo tenantInfo = resp.getTenantInfo();
        if (tenantInfo != null) {
            TokenPref.getInstance(App.getContext()).setTenantInfo(tenantInfo);
        }

        if (resp.getAuthToken() != null && !resp.getAuthToken().isEmpty()) {
            TokenPref.getInstance(App.getContext()).setCEAuthToken(resp.getAuthToken());
        }

    }

    /**
     * 登出CE不呼叫API, 只釋放資源和清除暫存資料
     */
    public static void cleanCE() {
        CELog.d("cleanCE start");
        ClientsManager.cancelAll();
        SocketManager.close();
        ChatService.getInstance().release();
        UserPref.getInstance(App.getContext())
            .clearByKey(UserPref.PreferencesKey.USER_TYPE)
            .clearByKey(UserPref.PreferencesKey.LOVE_LABEL_ID)
            .setBrand(0);

        TokenPref.getInstance(App.getContext())
            .clearByKey(TokenPref.PreferencesKey.USER_ID)
            .clearByKey(TokenPref.PreferencesKey.TOKEN_ID)
            .clearByKey(TokenPref.PreferencesKey.DEVICE_LOGIN_TIME)
            .clearByKey(TokenPref.PreferencesKey.IS_ENABLE_CALL);

        TenantPref.getInstance(App.getContext()).delete();

        AvatarService.clearAllCache();
        ChatMemberCacheService.clearAllCache();
        App.getInstance().clearMeetingId();
        DBManager.getInstance().close();
        AiffDB.getInstance(App.getContext()).getAiffInfoDao().delAll();
        CELog.d("cleanCE end");
    }

    /**
     * 清除CP暫存資料
     */
    public static void cleanCP() {
        CELog.d("cleanCP start");
        CpSocket.getInstance().disconnect();
        TokenPref.getInstance(App.getContext())
            .clearByKey(TokenPref.PreferencesKey.CP_NAME)
            .clearByKey(TokenPref.PreferencesKey.CP_ACCOUNT_ID)
            .clearByKey(TokenPref.PreferencesKey.CP_SOCKET_DEVICE_ID)
            .clearByKey(TokenPref.PreferencesKey.CP_SOCKET_NAME)
            .clearByKey(TokenPref.PreferencesKey.CP_SOCKET_NAME_SPACE)
            .clearByKey(TokenPref.PreferencesKey.CP_SOCKET_URL)
            .clearByKey(TokenPref.PreferencesKey.CP_TRANS_TENANT_ID)
            .clearByKey(TokenPref.PreferencesKey.CP_TRANS_TENANT_INFO)
            .clearByKey(TokenPref.PreferencesKey.TENANT_CODE)
            .clearByKey(TokenPref.PreferencesKey.CP_CURRENT_TENANT)
            .clearByKey(TokenPref.PreferencesKey.CURRENT_TENANT_ID)
            .clearByKey(TokenPref.PreferencesKey.CURRENT_TENANT_CODE)
            .clearByKey(TokenPref.PreferencesKey.CURRENT_TENANT_URL)
            .clearByKey(TokenPref.PreferencesKey.AUTH_TOKEN)
            .clearByKey(TokenPref.PreferencesKey.CP_RELATION_TENANT);
        AiffDB.getInstance(App.getContext()).getAiffInfoDao().delAll();
        CELog.d("cleanCP end");
    }

    /**
     * 登出並導到登入頁
     */
    public static void logoutToLoginPage() {
        SystemKit.cleanCE();
        SystemKit.cleanCP();
        TokenPref.getInstance(App.getInstance()).setPreloadStep(PreloadStepEnum.SYNC_LABEL.ordinal());
        TokenPref.getInstance(App.getInstance()).setCpTokenId("");
        TokenPref.getInstance(App.getInstance()).setBossServiceNumberId("");
        Intent intent = new Intent(App.getInstance().currentActivity(), LoginCpActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        App.getInstance().currentActivity().startActivity(new Intent(intent));
        ActivityManager.finishBaseActivity();
        ActivityManager.finishAll();
        App.getInstance().currentActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    public static void quitTenant(Activity activity) {
        List<RelationTenant> tenantList = TokenPref.getInstance(activity)
            .getCpRelationTenantList();
        String currentTenantCode = TokenPref.getInstance(activity).getCurrentTenantCode();
        List<RelationTenant> otherTenant = tenantList.stream().filter(tenant -> !tenant.getTenantCode().equals(currentTenantCode)).collect(Collectors.toList());
        TokenPref.getInstance(activity).setCpRelationTenantList(otherTenant);
        if (!otherTenant.isEmpty()) {
            changeTenant(activity, otherTenant.get(0), false);
        } else {
            ChatRoomReference.getInstance().deleteServiceNumber();
            logoutToLoginPage();
        }
    }

    /**
     * CP創建團隊中斷時回復
     */
    public static void recoverTransTenant(Activity activity) {
        if (activity == null) return;
        String accountId = TokenPref.getInstance(activity).getCpAccountId();
        TransTenantInfo transTenantInfo = TokenPref.getInstance(activity).getCpTransTenantInfo();
        if (transTenantInfo != null) {
            List<TransMember> transMemberArray = transTenantInfo.getTransMembers();
            if (transMemberArray != null && !transMemberArray.isEmpty()) {
                for (TransMember member : transMemberArray) {
                    if (member.getAccountId().equals(accountId)) {
                        if (Objects.equals(member.getStatus(), OWNER)) {
                            activity.startActivity(new Intent(activity, GroupCreateActivity.class));
                        } else if (Objects.equals(member.getStatus(), WAIT_CONFIRM)) {
                            activity.startActivity(new Intent(activity, GroupWaitConfirmActivity.class));
                        } else if (Objects.equals(member.getStatus(), ALREADY_JOIN)) {
                            activity.startActivity(new Intent(activity, GroupWaitActivity.class));
                        }
                        break;
                    }
                }
            }
        }
    }

    public static void changeTenant(Activity activity, RelationTenant tenant, Boolean isReload) {
        if (NetworkUtils.isNetworkAvailable(activity.getApplicationContext()))
            changeTenant(activity, tenant, isReload, "");
        else
            ToastUtils.showCenterToast(activity, activity.getString(R.string.text_no_internet_alert));
    }


    /**
     * 切換團隊
     */
    public static void changeTenant(Activity activity, RelationTenant tenant, Boolean isReload, String scannerType) {
        CELog.e("changeTenant start"); //temp for bug
        RelationTenant currentTenant = TokenPref.getInstance(activity).getCpCurrentTenant();
        if (currentTenant != null && currentTenant.getTenantName().equals(tenant.getTenantName()) && !isReload) {
            Toast.makeText(activity, "已在該團隊", Toast.LENGTH_SHORT).show();
            CELog.w("已在該團隊"); //temp for bug
        } else {
            DBManager.getInstance().isChangingTenant = true;
            DBManager.getInstance().close();
            IosProgressDialog iosProgressDialog = new IosProgressDialog(activity);
            iosProgressDialog.show(isReload ? "系統過載，重新加載中..." : "切換團隊中...");
//            String oriTenantUrl = TokenPref.getInstance(activity).getCurrentTenantUrl();
            TokenPref.getInstance(activity).setCurrentTenantUrl(tenant.getServiceUrl());
            ApiManager.getInstance().setCEAccount(activity, TokenPref.getInstance(activity).getCountryCode(), TokenPref.getInstance(activity).getAccountNumber(), tenant.getTenantCode(), TokenPref.getInstance(activity).getAuthToken());

            CELog.w("changeTenant : token apply start"); //temp for bug
            ApiManager.doTokenApply(activity, false, new TokenApplyRequest.Listener() {
                @Override
                public void allCallBack(boolean isRefresh, boolean status) {
                }

                @Override
                public void onSuccess(boolean isRefresh, AileTokenApply.Resp resp) {
                    CELog.d("changeTenant : token apply success"); //temp for bug
                    //取得新CEToken後, 清空舊CE的資料
                    SystemKit.cleanCE();
                    ClientsManager.cancelAll(() -> {
                        TokenPref.getInstance(activity)
                            .setCpCurrentTenant(tenant)
                            .setCurrentTenantId(tenant.getTenantName())
                            .setCurrentTenantCode(tenant.getTenantCode())
                            .setCurrentTenantUrl(tenant.getServiceUrl())
                            .setTokenId(resp.getTokenId());
                        if (resp.getUser() != null) CELog.startLogSave(activity, resp.getUser().getId());
                        DBManager.getInstance().isChangingTenant = false;
                        saveCEInfo(resp);
                        syncApiData();
                        SocketManager.reconnect();
                        CpSocket.getInstance().connect(
                            TokenPref.getInstance(activity).getCpSocketUrl(),
                            TokenPref.getInstance(activity).getCpSocketNameSpace(),
                            TokenPref.getInstance(activity).getCpSocketName(),
                            TokenPref.getInstance(activity).getCpSocketDeviceId()
                        );
//                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        long lastRefreshTime = DBManager.getInstance().getLastRefreshTime(FIRST_LOAD);
                        if (lastRefreshTime == 0) {
                            // 重置火箭頁的進行階段
                            TokenPref.getInstance(activity).setPreloadStep(PreloadStepEnum.SYNC_LABEL.ordinal());
                        }
                        Intent intent = new Intent();
                        intent.setClass(activity, HomeActivity.class);
                        intent.putExtra(BundleKey.NEED_PRELOAD.key(), lastRefreshTime == 0);
                        intent.putExtra(BundleKey.IS_BIND_AILE.key(), resp.getUser().getIsBindAile());
                        intent.putExtra(BundleKey.BIND_URL.key(), resp.getUser().getBindUrl());
                        intent.putExtra(BundleKey.IS_COLLECT_INFO.key(), resp.getUser().getIsCollectInfo());
                        intent.putExtra(BundleKey.IS_NEED_SAVE_TENANT.key(), true);
                        if (!Strings.isNullOrEmpty(scannerType)) {
                            intent.putExtra(BundleKey.SCANNER_TYPE.key(), scannerType);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(intent);
                        activity.finish();
                        ActivityManager.finishBaseActivity();
                        ActivityManager.finishAll();
                        iosProgressDialog.dismiss();
//                        });
                    });
                }

                @Override
                public void onFailed(ErrCode errorCode, String errorMessage) {
                    CELog.d("changeTenant fail: token apply fail"); //temp for bug
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        DBManager.getInstance().isChangingTenant = false;
//                        if (currentTenant != null) {
//                            ApiManager.getInstance().setCEAccount(activity, TokenPref.getInstance(activity).getCountryCode(), TokenPref.getInstance(activity).getAccountNumber(), currentTenant.getTenantCode(), TokenPref.getInstance(activity).getAuthToken());
//                        }
                        Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
//                        TokenPref.getInstance(activity).setCurrentTenantUrl(oriTenantUrl);
                        iosProgressDialog.dismiss();
                    });
                }

                @Override
                public void onCallData(String roomId, String meetingId, String callKey) {
                }
            });
        }
    }

    /**
     * 打開服務號群發頁
     */
    public static void openServiceNumberBroadcast(Context context, String name, String broadcastRoomId, String serviceNumberId) {
        ChatRoomEntity entity = ChatRoomReference.getInstance().findByRoomIdAndServiceNumberId(broadcastRoomId, serviceNumberId);
        if (entity == null) {
            ChatRoomService.getInstance().getChatRoomItem(context, "", broadcastRoomId, RefreshSource.REMOTE, new ServiceCallBack<ChatRoomEntity, RefreshSource>() {
                @Override
                public void complete(ChatRoomEntity entity, RefreshSource source) {
                    entity.setType(ChatRoomType.broadcast);
                    entity.setServiceNumberId(serviceNumberId);
                    entity.setUnReadNum(0);
                    ChatRoomReference.getInstance().save(entity);
                    ActivityTransitionsControl.navigateToServiceBroadcastEditor(context, broadcastRoomId, name, serviceNumberId, (intent, s) -> {
                        IntentUtil.INSTANCE.start(context, intent);
                    });
                }

                @Override
                public void error(String message) {

                }
            });
        } else {
            entity.setType(ChatRoomType.broadcast);
            ActivityTransitionsControl.navigateToServiceBroadcastEditor(context, broadcastRoomId, name, serviceNumberId, (intent, s) -> {
                IntentUtil.INSTANCE.start(context, intent);
            });
        }
    }

    /**
     * 背景同步資校
     */
    public static void syncApiData() {
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            StickerService.initEmoji(App.getContext(), null);
            StickerService.getStickerPackageEntities(App.getContext(), RefreshSource.REMOTE, null);
        });
    }
}
