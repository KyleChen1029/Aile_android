package tw.com.chainsea.ce.sdk.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.R;
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
import tw.com.chainsea.ce.sdk.http.ce.request.UserItemRequest;
import tw.com.chainsea.ce.sdk.http.ce.request.UserProfileRequest;
import tw.com.chainsea.ce.sdk.lib.ErrCode;
import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
import tw.com.chainsea.ce.sdk.service.listener.AServiceCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;

/**
 * current by evan on 2020-03-23
 *
 * @author Evan Wang
 * date 2020-03-23
 */
public class UserProfileService {

    static long useTime = System.currentTimeMillis();

    public static void getProfileFromRemote(Context context, String id, @Nullable ServiceCallBack<UserProfileEntity, RefreshSource> callBack) {
        ApiManager.doUserItem(context, id, new UserItemRequest.Listener() {
            @Override
            public void onSuccess(UserProfileEntity entity) {
                if (callBack != null && entity != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        callBack.complete(entity, RefreshSource.REMOTE);
                    });
                }
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e(errorMessage);
            }
        });
    }

    public static void getProfile(Context context, String id, @Nullable ServiceCallBack<UserProfileEntity, RefreshSource> callBack) {
        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
            UserProfileEntity profile = UserProfileReference.findById(null, id);
            if (callBack != null) {
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    if (profile != null) {
                        callBack.complete(profile, RefreshSource.LOCAL);
                    } else {
                        getProfileFromRemote(context, id, callBack);
                    }
                });
            }
        });
    }

    public static void getProfile(Context context, RefreshSource source, String id, @Nullable ServiceCallBack<UserProfileEntity, RefreshSource> callBack) {
        if (RefreshSource.ALL_or_LOCAL.contains(source)) {
            ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
                UserProfileEntity profile = UserProfileReference.findById(null, id);
                if (callBack != null) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        if (profile != null) {
                            callBack.complete(profile, RefreshSource.LOCAL);
                        } else {
                            getProfile(context, RefreshSource.REMOTE, id, callBack);
                        }
                    });
                }
            });
        }

        if (RefreshSource.ALL_or_REMOTE.contains(source)) {
            ApiManager.doUserItem(context, id, new UserItemRequest.Listener() {
                @Override
                public void onSuccess(UserProfileEntity entity) {
                    if (callBack != null) {
                        if (entity != null) {
                            ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                                callBack.complete(entity, RefreshSource.REMOTE);
                            });
                        }
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    CELog.e(errorMessage);
                }
            });

        }
    }

    /**
     * persona lInformation
     */
    public static void getSelfProfile(Context context, RefreshSource source, @Nullable ServiceCallBack<UserProfileEntity, RefreshSource> callBack) {
        useTime = System.currentTimeMillis();
        String selfId = TokenPref.getInstance(context).getUserId();
        if (RefreshSource.LOCAL.equals(source)) {
            UserProfileEntity profile = DBManager.getInstance().queryUser(selfId);
            if (profile != null) {
                callBack.complete(profile, source);
            } else {
                callBack.error(context.getString(R.string.find_not_found_by_local_database));
            }
            CELog.w(String.format("ContactPerson2Fragment:: getSelfProfile use time::: %s /s", ((System.currentTimeMillis() - useTime) / 1000.0d)));
        } else {
            useTime = System.currentTimeMillis();
            ApiManager.doUserProfile(context, new UserProfileRequest.Listener() {
                @Override
                public void onProfileSuccess(UserProfileEntity profile) {
                    if (profile != null && profile.getUserType() != null) {
                        UserPref.getInstance(context).saveUserType(profile.getUserType().getUserType());
                    }
                    DBManager.getInstance().insertUserAndFriends(profile);
                    UserProfileEntity selfProfile = DBManager.getInstance().queryUser(selfId);
                    selfProfile.setHomePagePics(profile.getHomePagePics());
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.complete(selfProfile, RefreshSource.REMOTE));
                    CELog.d(String.format("ContactPerson2Fragment:: getSelfProfile use time::: %s /s", ((System.currentTimeMillis() - useTime) / 1000.0d)));
                }

                @Override
                public void onInvalidAccPw() {

                }

                @Override
                public void onProfileFailed(String errorMessage) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.error(errorMessage));
                }

                @Override
                public void onThridLoginUnBind() {

                }
            });
        }
    }

    /**
     * Obtain employee information
     */
    public static void getEmployeeProfile(Context context, RefreshSource source, @Nullable ServiceCallBack<List<UserProfileEntity>, RefreshSource> callBack) {
        if (RefreshSource.LOCAL.equals(source)) {
            ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
                List<UserProfileEntity> profiles = DBManager.getInstance().queryFriends();
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    if (callBack != null) callBack.complete(profiles, source);
                });
            });

        } else {
            ApiManager.doSyncEmployee(context, new ApiListener<List<UserProfileEntity>>() {
                @Override
                public void onSuccess(List<UserProfileEntity> contactList) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        if (callBack != null) callBack.complete(contactList, RefreshSource.REMOTE);
                    });
                }

                @Override
                public void onFailed(String errorMessage) {
                    Log.d("TAG", errorMessage);
                }
            });
        }
    }

    /**
     * Obtain block employee list
     */
    public static void getBlockEmployeeProfile(Context context, RefreshSource source, @Nullable ServiceCallBack<List<UserProfileEntity>, RefreshSource> callBack) {
        if (RefreshSource.LOCAL.equals(source)) {
            ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
                List<UserProfileEntity> profiles = DBManager.getInstance().queryBlockFriends();
                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                    if (callBack != null) callBack.complete(profiles, source);
                });
            });

        } else {
            ApiManager.doSyncEmployee(context, new ApiListener<List<UserProfileEntity>>() {
                @Override
                public void onSuccess(List<UserProfileEntity> contactList) {
                    ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                        if (callBack != null) {
                            List<UserProfileEntity> blocksList = new ArrayList<>();
                            for (UserProfileEntity entity : contactList) {
                                if (entity.isBlock()) blocksList.add(entity);
                            }
                            callBack.complete(blocksList, RefreshSource.REMOTE);
                        }
                    });
                }

                @Override
                public void onFailed(String errorMessage) {
                    Log.d("TAG", errorMessage);
                }
            });
        }
    }

    /**
     * Update profile picture
     */
    public static void uploadSelfAvatar(Context context, int size, String filePath, String fileName) {
        String tokenId = TokenPref.getInstance(context).getTokenId();
        String selfId = TokenPref.getInstance(context).getUserId();
        FileService.uploadAvatar(context, false, tokenId, Media.findByFileType(filePath), size, filePath, fileName, new ServiceCallBack<String, RefreshSource>() {
            @Override
            public void complete(String resp, RefreshSource source) {
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    if (JsonHelper.getInstance().has(jsonObject, "_header_")) {
                        JSONObject header = jsonObject.getJSONObject("_header_");
                        if (JsonHelper.getInstance().has(jsonObject, "id") && header.getBoolean("success")) {
                            String id = jsonObject.getString("id");
                            UserPref.getInstance(context).setUserAvatarId(id);
                            UserProfileReference.updateByCursorNameAndValues(null, selfId, DBContract.UserProfileEntry.COLUMN_AVATAR_URL, id);
                            EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_REFRESH_HOMEPAGE_AVATAR));
                        } else {
                            String errorCode = header.getString("errorCode");
                            ErrCode code = ErrCode.of(errorCode);
                            if (ErrCode.TOKEN_INVALID_or_TOKEN_REQUIRED.contains(code)) {
                                throw new RuntimeException();
                            }
                        }
                    } else {
                        throw new RuntimeException();
                    }
                } catch (Exception e) {
                    error(e.getMessage());
                }
            }

            @Override
            public void error(String message) {

            }
        });
    }

    /**
     * Update personal homepage background image
     */
    public static void uploadSelfHomepagePics(Context context, String filePath, String fileName) {
        String tokenId = TokenPref.getInstance(context).getTokenId();
        FileService.uploadPicture(context, false, tokenId, Media.findByFileType(filePath), filePath, fileName, new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
            @Override
            public void complete(UploadManager.FileEntity fileEntity, RefreshSource source) {
                if (fileEntity == null) {
                    error("");
                    return;
                }
                String picUrl = fileEntity.getUrl();
                if (Strings.isNullOrEmpty(picUrl)) {
                    error("url is null ");
                    return;
                }

                ApiManager.doUserHomePagePicsUpdate(context, Lists.newArrayList(picUrl), new ApiListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        UserPref.getInstance(context).setUserHomePageBackgroundUrl(picUrl);
                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_REFRESH_HOMEPAGE_BACKGROUND_PICS));
                    }

                    @Override
                    public void onFailed(String errorMessage) {
                        CELog.e(errorMessage);
                    }
                });
            }

            @Override
            public void onProgress(float progress, long total) {
            }

            @Override
            public void error(String message) {
                super.error(message);
            }
        });
    }

    public static void getProfileIsEmployee(Context context, String ownerId, @Nullable ServiceCallBack<UserType, RefreshSource> callBack) {
        UserProfileEntity profile = UserProfileReference.findById(null, ownerId);
        if (profile != null) {
            if (callBack != null) {
                callBack.complete(profile.getUserType(), RefreshSource.LOCAL);
            }
        } else {
            ApiManager.doUserItem(context, ownerId, new UserItemRequest.Listener() {
                @Override
                public void onSuccess(UserProfileEntity entity) {
                    if (callBack != null && entity != null) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
                            callBack.complete(entity.getUserType(), RefreshSource.REMOTE);
                        });
                    }

                    if (entity == null) {
                        onFailed(" Profile is null");
                    }
                }

                @Override
                public void onFailed(String errorMessage) {
                    if (callBack != null) {
                        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> callBack.error(errorMessage));
                    }
                }
            });
        }
    }

    public static void removeCustomerBusinessCard(Context context, String userId) {
        ApiManager.removeCustomerBusinessCard(context, userId, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                CELog.d(s);
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e(errorMessage);
            }
        });
    }

    public static void updateCustomerProfile(Context context, String userId, String customerName, String desc) {
        ApiManager.doUserHomePageUpdateProfileCustomer(context, userId, customerName, desc, new ApiListener<String>() {
            @Override
            public void onSuccess(String s) {
                CELog.d(s);
            }

            @Override
            public void onFailed(String errorMessage) {
                CELog.e(errorMessage);
            }
        });
    }
}
