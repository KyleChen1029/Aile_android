package tw.com.chainsea.ce.sdk.service;

import android.content.Context;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Map;

import tw.com.chainsea.android.common.client.callback.impl.EntityCallBack;
import tw.com.chainsea.android.common.client.callback.impl.NativeCallback;
import tw.com.chainsea.android.common.client.helper.ClientsHelper;
import tw.com.chainsea.android.common.client.type.Media;
import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.http.ce.ApiPath;
import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
import tw.com.chainsea.ce.sdk.service.listener.AServiceCallBack;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;

/**
 * current by evan on 2020-08-20
 *
 * @author Evan Wang
 * date 2020-08-20
 */
public class FileService {

    public static void uploadFile(Context context, boolean mainThreadEnable, String tokenId, Media media, String filePath, AServiceCallBack<UploadManager.FileEntity, RefreshSource> callBack) {
        String url = TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.baseFileUpload;
//        String url = NetConfig.getInstance().getUrl() + ApiPath.baseFileUpload;
        Map<String, String> formDataPart = Maps.newHashMap(ImmutableMap.of("args", new Args(tokenId).toJson()));
        File file = new File(filePath);
        ClientsHelper.post(true).execute(url, media.get(), Maps.newHashMap(), formDataPart, "file", file, new EntityCallBack<UploadManager.FileEntity>(mainThreadEnable) {
            @Override
            public void onSuccess(UploadManager.FileEntity fileEntity) {
                if (callBack != null) {
                    callBack.complete(fileEntity, RefreshSource.REMOTE);
                }
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                if (callBack != null) {
                    callBack.error(errorMsg);
                }
            }

            @Override
            public void onProgress(float progress, long total) {
                super.onProgress(progress, total);
                if (callBack != null) {
                    callBack.onProgress(progress, total);
                }
            }
        });
    }


    /**
     * Upload avatar
     */
    public static void uploadAvatar(Context context, boolean mainThreadEnable, String tokenId, Media media, int size, String filePath, String fileName, ServiceCallBack<String, RefreshSource> callBack) {
        String url = TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.baseAvatarUpload;
        String args = new Args(tokenId)
                .x(0)
                .y(0)
                .size(size)
                .toJson();
        Map<String, String> formDataPart = Maps.newHashMap(ImmutableMap.of("args", args));
        File file = new File(filePath);
        ClientsHelper.post(true).execute(url, media.get(), Maps.newHashMap(), formDataPart, "file", file, fileName, new NativeCallback(mainThreadEnable) {
            @Override
            public void onSuccess(String resp) {
                if (callBack != null) {
                    callBack.complete(resp , RefreshSource.REMOTE);
                }
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                if (callBack != null) {
                    callBack.error(errorMsg);
                }
            }
        });
    }

    public static void uploadServiceNumberAvatar(Context context, String serviceNumberId, String tokenId, Media media, int size, String filePath, String fileName, ServiceCallBack<String, RefreshSource> callBack) {
        String url = TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + "/" + ApiPath.serviceNumberUpdate;
        String args = getArgs(serviceNumberId, tokenId, size);
        Map<String, String> formDataPart = Maps.newHashMap(ImmutableMap.of("args", args));
        File file = new File(filePath);
        ClientsHelper.post(true).execute(url, media.get(), Maps.newHashMap(), formDataPart, "file", file, fileName, new NativeCallback(false) {
            @Override
            public void onSuccess(String resp) {
                if (callBack != null) {
                    callBack.complete(resp , RefreshSource.REMOTE);
                }
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                if (callBack != null) {
                    callBack.error(errorMsg);
                }
            }
        });
    }

    private static String getArgs(String tokenId, String serviceNumberId, int size) {
        return new Args(tokenId).x(0).y(0).id(serviceNumberId).size(size).toJson();
    }


    public static void uploadPicture(Context context, boolean mainThreadEnable, String tokenId, Media media, String filePath, String fileName, AServiceCallBack<UploadManager.FileEntity, RefreshSource> callBack) {
        String url = TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.basePictureUpload;
        Map<String, String> formDataPart = Maps.newHashMap(ImmutableMap.of("args", new Args(tokenId).toJson()));
        File file = new File(filePath);

        ClientsHelper.post(true).execute(url, media.get(), Maps.newHashMap(), formDataPart, "file", file, fileName, new EntityCallBack<UploadManager.FileEntity>(mainThreadEnable) {
            @Override
            public void onSuccess(UploadManager.FileEntity fileEntity) {
                if (callBack != null) {
                    callBack.complete(fileEntity, RefreshSource.REMOTE);
                }
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                if (callBack != null) {
                    callBack.error(errorMsg);
                }
            }

            @Override
            public void onProgress(float progress, long total) {
                super.onProgress(progress, total);
                if (callBack != null) {
                    callBack.onProgress(progress, total);
                }
            }
        });
    }


    public static void uploadFile(Context context, boolean mainThreadEnable, String tokenId, Media media, String filePath, String fileName, AServiceCallBack<UploadManager.FileEntity, RefreshSource> callBack) {
        String url = TokenPref.getInstance(context).getCurrentTenantUrl() + ApiPath.ROUTE + ApiPath.baseFileUpload;
//        String url = NetConfig.getInstance().getUrl() + ApiPath.baseFileUpload;
        Map<String, String> formDataPart = Maps.newHashMap(ImmutableMap.of("args", new Args(tokenId).toJson()));
        File file = new File(filePath);
        ClientsHelper.post(true).execute(url, media.get(), Maps.newHashMap(), formDataPart, "file", file, fileName, new EntityCallBack<UploadManager.FileEntity>(mainThreadEnable) {
            @Override
            public void onSuccess(UploadManager.FileEntity fileEntity) {
                if (callBack != null) {
                    callBack.complete(fileEntity, RefreshSource.REMOTE);
                }
            }

            @Override
            public void onFailure(Exception e, String errorMsg) {
                if (callBack != null) {
                    callBack.error(errorMsg);
                }
            }

            @Override
            public void onProgress(float progress, long total) {
                super.onProgress(progress, total);
                if (callBack != null) {
                    callBack.onProgress(progress, total);
                }
            }
        });
    }

    public static class RepairArgs {
        Header _header_;
        String type;
        String name;
        String version;
        String content;
        String osType;

        public RepairArgs(String tokenId) {
            this._header_ = new Header(tokenId);
        }

        public RepairArgs type(String type) {
            this.type = type;
            return this;
        }

        public RepairArgs name(String name) {
            this.name = name;
            return this;
        }

        public RepairArgs version(String version) {
            this.version = version;
            return this;
        }

        public RepairArgs content(String content) {
            this.content = content;
            return this;
        }

        public RepairArgs osType(String osType) {
            this.osType = osType;
            return this;
        }

        public String toJson() {
            return JsonHelper.getInstance().toJson(this);
        }
    }

    public static class FacebookImageArgs {
        Header _header_;
        String postId;
        String commentId;
        String operation = "Add";
        String type = "Image";

        public FacebookImageArgs(String tokenId) {
            this._header_ = new Header(tokenId);
        }

        public FacebookImageArgs postId(String postId) {
            this.postId = postId;
            return this;
        }

        public FacebookImageArgs commentId(String commentId) {
            this.commentId = commentId;
            return this;
        }

        public FacebookImageArgs operation(String operation) {
            this.operation = operation;
            return this;
        }

        public FacebookImageArgs type(String type) {
            this.type = type;
            return this;
        }

        public String toJson() {
            return JsonHelper.getInstance().toJson(this);
        }
    }

    public static class Args {
        Header _header_;
        //
        int x;
        int y;
        int size;
        String postId;
        String commentId;
        String id;

       public Args(String tokenId) {
            this._header_ = new Header(tokenId);
        }

        public Args x(int x) {
            this.x = x;
            return this;
        }

        public Args y(int y) {
            this.y = y;
            return this;
        }

        public Args size(int size) {
            this.size = size;
            return this;
        }

        public Args postId(String postId) {
           this.postId = postId;
           return this;
        }

        public Args commentId(String commentId) {
           this.commentId = commentId;
           return this;
        }

        public Args id(String serviceNumberId) {
           this.id = serviceNumberId;
           return this;
        }

        public String toJson() {
            return JsonHelper.getInstance().toJson(this);
        }
    }

    static class Header {
        String tokenId;

        Header(String tokenId) {
            this.tokenId = tokenId;
        }
    }
}
