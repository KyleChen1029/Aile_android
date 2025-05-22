//package tw.com.chainsea.chat.ui.fragment;
//
//import static android.Manifest.permission.CAMERA;
//import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
//import static android.app.Activity.RESULT_OK;
//import static tw.com.chainsea.ce.sdk.bean.msg.MessageStatus.RECEIVED;
//import static tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.discuss;
//import static tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.group;
//import static tw.com.chainsea.ce.sdk.bean.room.ChatRoomType.system;
//import static tw.com.chainsea.chat.messagekit.enums.OpenBottomRichMeunType.MULTIPLE_SELECTION;
//import static tw.com.chainsea.chat.messagekit.enums.OpenBottomRichMeunType.RANGE_SELECTION;
//
//import android.Manifest;
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.annotation.TargetApi;
//import android.app.Activity;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.media.CamcorderProfile;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Environment;
//import android.os.Handler;
//import android.os.Looper;
//import android.provider.MediaStore;
//import android.provider.Settings;
//import android.text.Editable;
//import android.text.SpannableStringBuilder;
//import android.text.TextUtils;
//import android.text.TextWatcher;
//import android.text.method.LinkMovementMethod;
//import android.util.Log;
//import android.util.LruCache;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.ViewTreeObserver;
//import android.view.animation.LinearInterpolator;
//import android.view.inputmethod.EditorInfo;
//import android.view.inputmethod.InputMethodManager;
//import android.webkit.URLUtil;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.PopupWindow;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.DrawableRes;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.widget.AppCompatImageView;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.core.content.ContextCompat;
//import androidx.core.content.FileProvider;
//import androidx.databinding.DataBindingUtil;
//import androidx.lifecycle.ViewModelProvider;
//import androidx.recyclerview.widget.DefaultItemAnimator;
//import androidx.recyclerview.widget.GridLayoutManager;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.RequestOptions;
//import com.google.android.material.textfield.TextInputEditText;
//import com.google.common.base.Strings;
//import com.google.common.collect.ComparisonChain;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Queues;
//import com.google.common.collect.Sets;
//import com.google.common.primitives.Longs;
//import com.google.gson.reflect.TypeToken;
//import com.hjq.permissions.OnPermissionCallback;
//import com.hjq.permissions.XXPermissions;
//import com.luck.picture.lib.basic.PictureSelector;
//import com.luck.picture.lib.config.FileSizeUnit;
//import com.luck.picture.lib.config.SelectMimeType;
//import com.luck.picture.lib.config.SelectModeConfig;
//import com.luck.picture.lib.entity.LocalMedia;
//import com.luck.picture.lib.interfaces.OnResultCallbackListener;
//import com.luck.picture.lib.style.BottomNavBarStyle;
//import com.luck.picture.lib.style.PictureSelectorStyle;
//import com.luck.picture.lib.style.SelectMainStyle;
//import com.luck.picture.lib.style.TitleBarStyle;
//
//import org.greenrobot.eventbus.Subscribe;
//import org.greenrobot.eventbus.ThreadMode;
//import org.joda.time.DateTime;
//import org.joda.time.format.DateTimeFormat;
//import org.joda.time.format.DateTimeFormatter;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.lang.reflect.Type;
//import java.text.MessageFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Queue;
//import java.util.Set;
//import java.util.TreeMap;
//import java.util.stream.Collectors;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.android.common.client.type.Media;
//import tw.com.chainsea.android.common.datetime.DateTimeHelper;
//import tw.com.chainsea.android.common.event.KeyboardHelper;
//import tw.com.chainsea.android.common.file.FileHelper;
//import tw.com.chainsea.android.common.image.BitmapHelper;
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.android.common.log.CELog;
//import tw.com.chainsea.android.common.multimedia.AMediaBean;
//import tw.com.chainsea.android.common.multimedia.ImageBean;
//import tw.com.chainsea.android.common.multimedia.MultimediaHelper;
//import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
//import tw.com.chainsea.android.common.text.KeyWordHelper;
//import tw.com.chainsea.android.common.text.StringHelper;
//import tw.com.chainsea.android.common.ui.UiHelper;
//import tw.com.chainsea.android.common.video.IVideoSize;
//import tw.com.chainsea.android.common.video.VideoSizeFromVideoFile;
//import tw.com.chainsea.ce.sdk.base.MsgBuilder;
//import tw.com.chainsea.ce.sdk.bean.BadgeDataModel;
//import tw.com.chainsea.ce.sdk.bean.FacebookTag;
//import tw.com.chainsea.ce.sdk.bean.InputLogBean;
//import tw.com.chainsea.ce.sdk.bean.MsgNoticeBean;
//import tw.com.chainsea.ce.sdk.bean.MsgStatusBean;
//import tw.com.chainsea.ce.sdk.bean.ProcessStatus;
//import tw.com.chainsea.ce.sdk.bean.UpdateAvatarBean;
//import tw.com.chainsea.ce.sdk.bean.UpdateProfileBean;
//import tw.com.chainsea.ce.sdk.bean.UserExitBean;
//import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity;
//import tw.com.chainsea.ce.sdk.bean.account.UserType;
//import tw.com.chainsea.ce.sdk.bean.business.BusinessCode;
//import tw.com.chainsea.ce.sdk.bean.common.EnableType;
//import tw.com.chainsea.ce.sdk.bean.msg.ChannelType;
//import tw.com.chainsea.ce.sdk.bean.msg.FacebookCommentStatus;
//import tw.com.chainsea.ce.sdk.bean.msg.FacebookPostStatus;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageFlag;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageStatus;
//import tw.com.chainsea.ce.sdk.bean.msg.MessageType;
//import tw.com.chainsea.ce.sdk.bean.msg.SourceType;
//import tw.com.chainsea.ce.sdk.bean.msg.Tools;
//import tw.com.chainsea.ce.sdk.bean.msg.content.AtContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.BusinessContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.FileContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.IMessageContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.ImageContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.MentionContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.TextContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.UndefContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent;
//import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent;
//import tw.com.chainsea.ce.sdk.bean.room.AiServiceWarnedSocket;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEnum;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType;
//import tw.com.chainsea.ce.sdk.bean.room.QuickReplyItem;
//import tw.com.chainsea.ce.sdk.bean.room.QuickReplySocket;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
//import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType;
//import tw.com.chainsea.ce.sdk.cache.ChatMemberCacheService;
//import tw.com.chainsea.ce.sdk.database.DBManager;
//import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
//import tw.com.chainsea.ce.sdk.database.sp.UserPref;
//import tw.com.chainsea.ce.sdk.event.EventBusUtils;
//import tw.com.chainsea.ce.sdk.event.EventMsg;
//import tw.com.chainsea.ce.sdk.event.MsgConstant;
//import tw.com.chainsea.ce.sdk.http.ce.ApiManager;
//import tw.com.chainsea.ce.sdk.http.ce.base.ApiListener;
//import tw.com.chainsea.ce.sdk.http.ce.model.User;
//import tw.com.chainsea.ce.sdk.http.ce.request.FromAppointRequest;
//import tw.com.chainsea.ce.sdk.http.ce.request.MessageSendRequest;
//import tw.com.chainsea.ce.sdk.http.ce.request.ServiceNumberChatroomAgentServicedRequest;
//import tw.com.chainsea.ce.sdk.http.ce.request.SyncContactRequest;
//import tw.com.chainsea.ce.sdk.http.ce.request.UploadManager;
//import tw.com.chainsea.ce.sdk.network.NetworkManager;
//import tw.com.chainsea.ce.sdk.network.model.common.CommonResponse;
//import tw.com.chainsea.ce.sdk.network.model.common.ServiceSwitchIdentityResponse;
//import tw.com.chainsea.ce.sdk.network.model.request.ServiceSwitchIdentityRequest;
//import tw.com.chainsea.ce.sdk.network.model.request.ServicesIdentityListRequest;
//import tw.com.chainsea.ce.sdk.network.model.response.ChatRoomMemberResponse;
//import tw.com.chainsea.ce.sdk.network.model.response.ServicesIdentityListResponse;
//import tw.com.chainsea.ce.sdk.network.services.IdentityListService;
//import tw.com.chainsea.ce.sdk.network.services.SwitchIdentityService;
//import tw.com.chainsea.ce.sdk.reference.AccountRoomRelReference;
//import tw.com.chainsea.ce.sdk.reference.ChatRoomReference;
//import tw.com.chainsea.ce.sdk.reference.MessageReference;
//import tw.com.chainsea.ce.sdk.reference.UserProfileReference;
//import tw.com.chainsea.ce.sdk.service.ChatRoomService;
//import tw.com.chainsea.ce.sdk.service.ChatServiceNumberService;
//import tw.com.chainsea.ce.sdk.service.FileService;
//import tw.com.chainsea.ce.sdk.service.UserProfileService;
//import tw.com.chainsea.ce.sdk.service.listener.AServiceCallBack;
//import tw.com.chainsea.ce.sdk.service.listener.AgentSnatchCallback;
//import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
//import tw.com.chainsea.ce.sdk.service.type.ChatRoomSource;
//import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
//import tw.com.chainsea.ce.sdk.socket.ce.bean.ProvisionalMemberAddedSocket;
//import tw.com.chainsea.ce.sdk.socket.ce.code.NoticeCode;
//import tw.com.chainsea.chat.App;
//import tw.com.chainsea.chat.R;
//import tw.com.chainsea.chat.aiff.AiffManager;
//import tw.com.chainsea.chat.aiff.database.AiffDB;
//import tw.com.chainsea.chat.aiff.database.entity.AiffInfo;
//import tw.com.chainsea.chat.base.Constant;
//import tw.com.chainsea.chat.config.AiffEmbedLocation;
//import tw.com.chainsea.chat.config.BundleKey;
//import tw.com.chainsea.chat.config.InvitationType;
//import tw.com.chainsea.chat.databinding.FragmentChatBinding;
//import tw.com.chainsea.chat.databinding.PopupProvisionMemberActionBinding;
//import tw.com.chainsea.chat.databinding.ProgressSendVideoBinding;
//import tw.com.chainsea.chat.keyboard.ChatKeyboardLayout;
//import tw.com.chainsea.chat.keyboard.MentionSelectAdapter;
//import tw.com.chainsea.chat.keyboard.media.MediaBean;
//import tw.com.chainsea.chat.keyboard.media.MediaSelectorPreviewActivity;
//import tw.com.chainsea.chat.keyboard.view.HadEditText;
//import tw.com.chainsea.chat.lib.ActivityManager;
//import tw.com.chainsea.chat.lib.AtMatcherHelper;
//import tw.com.chainsea.chat.lib.BitmapBean;
//import tw.com.chainsea.chat.lib.ChatService;
//import tw.com.chainsea.chat.lib.PictureParse;
//import tw.com.chainsea.chat.lib.ToastUtils;
//import tw.com.chainsea.chat.mediagallery.view.MediaGalleryActivity;
//import tw.com.chainsea.chat.messagekit.enums.OpenBottomRichMeunType;
//import tw.com.chainsea.chat.messagekit.enums.RichMenuBottom;
//import tw.com.chainsea.chat.messagekit.enums.RichMenuType;
//import tw.com.chainsea.chat.messagekit.lib.AudioLib;
//import tw.com.chainsea.chat.messagekit.lib.FileUtil;
//import tw.com.chainsea.chat.messagekit.lib.Global;
//import tw.com.chainsea.chat.messagekit.lib.MessageDomino;
//import tw.com.chainsea.chat.messagekit.listener.CheckFacebookCommentStatus;
//import tw.com.chainsea.chat.messagekit.listener.OnChatRoomTitleChangeListener;
//import tw.com.chainsea.chat.messagekit.listener.OnFacebookReplyClick;
//import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener;
//import tw.com.chainsea.chat.messagekit.listener.OnMainMessageScrollStatusListener;
//import tw.com.chainsea.chat.messagekit.main.adapter.BottomRichMeunAdapter;
//import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapter;
//import tw.com.chainsea.chat.messagekit.main.adapter.MessageAdapterMode;
//import tw.com.chainsea.chat.messagekit.main.viewholder.VoiceMessageView;
//import tw.com.chainsea.chat.messagekit.main.viewholder.base.MessageViewBase;
//import tw.com.chainsea.chat.network.contact.ViewModelFactory;
//import tw.com.chainsea.chat.presenter.ChatPresenter;
//import tw.com.chainsea.chat.searchfilter.view.activity.MemberInvitationActivity;
//import tw.com.chainsea.chat.service.ActivityTransitionsControl;
//import tw.com.chainsea.chat.style.RoomThemeStyle;
//import tw.com.chainsea.chat.ui.activity.ChatActivity;
//import tw.com.chainsea.chat.ui.activity.FileExplorerActivity;
//import tw.com.chainsea.chat.ui.adapter.ChatRoomMessageSearchAdapter;
//import tw.com.chainsea.chat.ui.adapter.OnMessageItemClick;
//import tw.com.chainsea.chat.ui.adapter.OnProvisionalMemberItemClick;
//import tw.com.chainsea.chat.ui.adapter.ProvisionalMemberAdapter;
//import tw.com.chainsea.chat.ui.adapter.QuickReplyAdapter;
//import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo;
//import tw.com.chainsea.chat.ui.dialog.BottomSheetDialogBuilder;
//import tw.com.chainsea.chat.ui.dialog.TransferDialogBuilder;
//import tw.com.chainsea.chat.ui.dialog.WaitTransferDialogBuilder;
//import tw.com.chainsea.chat.ui.ife.IChatView;
//import tw.com.chainsea.chat.ui.utils.CEUtils;
//import tw.com.chainsea.chat.util.BitmapKit;
//import tw.com.chainsea.chat.util.GlideEngine;
//import tw.com.chainsea.chat.util.IntentUtil;
//import tw.com.chainsea.chat.util.NoDoubleClickListener;
//import tw.com.chainsea.chat.util.TimeUtil;
//import tw.com.chainsea.chat.view.business.BusinessTaskAction;
//import tw.com.chainsea.chat.view.chat.ChatViewModel;
//import tw.com.chainsea.chat.view.chatroom.adapter.AdvisoryRoomAdapter;
//import tw.com.chainsea.chat.view.consultai.ConsultAIActivity;
//import tw.com.chainsea.chat.widget.GridItemDecoration;
//import tw.com.chainsea.chat.widget.photo.PhotoBean;
//import tw.com.chainsea.chat.widget.xrefreshlayout.XRefreshLayout;
//import tw.com.chainsea.custom.view.alert.AlertView;
//import tw.com.chainsea.custom.view.popup.SimpleTextTooltip;
//import tw.com.chainsea.custom.view.progress.IosProgressBar;
//
///**
// * chat place
// * Created by 90Chris on 2016/4/21.
// */
//public class ChatFragment extends BaseFragment implements IChatView, MediaBean.MediaListener, XRefreshLayout.OnRefreshListener, XRefreshLayout.OnBackgroundClickListener, OnProvisionalMemberItemClick, TimeUtil.OnNetworkTimeListener {
//    private static final String TAG = ChatFragment.class.getSimpleName();
//    private final int REQUEST_CAMERA = 555;
//    private final int REQUEST_VIDEO = 666;
//    private final int RESULT_PIC = 1;
//    private final int RESULT_FILE = 2;
//    //    private final int RESULT_VIDEO = 1001;
//    private final int RESULT_IMAGE_TEXT = 3;
//    public final int REQUEST_CODE = 4;
//
//    public static final int REQUEST_CONSULT_AI_CODE = 30;
//
//    private static final int SHARE_SCREENSHOTS_RESULT_CODE = 0x1389;
//    public final int MEDIA_SELECTOR_REQUEST_CODE = 0x2705;
//
//    private ChatPresenter presenter;
//    // 主訊息資料
//    private final List<MessageEntity> mainMessageData = Lists.newArrayList();
//
//    private String currentDate;
//    private ChatRoomEntity chatRoom;
//    private IosProgressBar progressBar;
//
//    private FragmentChatBinding binding;
//    private String userName;
//    private String userId;
//
//    private boolean isThemeOpen; // 主題聊天室是否開啟狀態
//    private String themeId = "";
//
//    boolean checkOnceUnreadNumber;
//    // 服務號渠道相關UI
//    private FromAppointRequest.Resp appointResp;
//
//
//    private MentionSelectAdapter mentionSelectAdapter;
//    private File photoFile;
//    private File videoFile;
//    private boolean isReply;
//
//    private OnChatRoomTitleChangeListener onChatRoomTitleChangeListener;
//    private String robotChatRecord = "";
//    private ProvisionalMemberAdapter provisionalMemberAdapter = null;
//    //紀錄手指按下 subRoom 的時間
//    long subRoomOnTouchDownTime = 0;
//    private final Map<String, String> chatRoomMemberTable = new HashMap<>();
//
//    public void setOnChatRoomTitleChangeListener(OnChatRoomTitleChangeListener onChatRoomTitleChangeListener) {
//        this.onChatRoomTitleChangeListener = onChatRoomTitleChangeListener;
//    }
//
//    public ActivityResultLauncher<String[]> launcher;
//    private ActivityResultLauncher<String> storagePermissionResult;
//    private ActivityResultLauncher<Intent> shareScreenShotResult;
//    private KeyBoardBarListener keyBoardBarListener;
//    private AiffManager aiffManager;
//    //商業號可切換身份的 list
//    private List<ServicesIdentityListResponse> identityList = new ArrayList<>();
//    private String keyWord = "";
//    private boolean isProvisionMember = false;
//
//    //點擊臨時成員的 popup window
//    private PopupWindow provisionMemberPopupWindow;
//
//    //用於判斷重複點擊相同 item 能夠正確的 dismiss
//    private boolean isProvisionMemberPopupShowing = false;
//
//    private boolean isSendSingleFile = false;
//    private int sendFileSize = 0;
//    private int addProgressValue = 0;
//
//    Runnable timeBoxTarget = new Runnable() {
//        @Override
//        public void run() {
//            binding.floatTimeBoxTV.animate()
//                    .alpha(0.0f)
//                    .setDuration(300)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//                            binding.floatTimeBoxTV.setVisibility(View.GONE);
//                        }
//                    });
//        }
//    };
//
//    RoomThemeStyle themeStyle = RoomThemeStyle.UNDEF;
//
//    // 避免重複點擊
//    private boolean IS_ACTIVITY_FOR_RESULT;
//
//    private MessageEntity messageEntity;
//    public boolean mIsBlock;
//    private int lastItemPosition;
//    private int countDown = 3;
//
//    private static final SimpleDateFormat messageTimeLineFormat = new SimpleDateFormat("MMMdd日(EEE)", Locale.TAIWAN);
//
//    private ActionStatus actionStatus = ActionStatus.SCROLL;
//
//    // Whether the unread dividing line has been crossed to determine
//    private MessageEntity undeadLineMessage;
//    private boolean undeadLineDrawable;
//    private ActivityResultLauncher<Intent> mediaPreviewARL = null;
//
//    private ChatViewModel chatViewModel;
//
//    private boolean isFacebookReplyPublic = false;
//    private boolean isFacebookReplyOverTime = false;
//    private long servicedDurationTime = 0L;
//
//    // AI 諮詢引用的回覆
//    private final ActivityResultLauncher<Intent> aiConsultQuoteTextResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//        if (result.getData() != null) {
//            Intent data = result.getData();
//            Bundle bundle = data.getExtras();
//            if (bundle != null) {
//                MessageType messageType = (MessageType)(bundle.getSerializable(BundleKey.CONSULT_AI_QUOTE_TYPE.key()));
//                if (messageType != null) {
//                    switch (messageType) {
//                        case TEXT:
//                            String quoteString = result.getData().getStringExtra(
//                                    BundleKey.CONSULT_AI_QUOTE_STRING.key());
//                            binding.chatKeyboardLayout.setUnfinishedEdited(
//                                    InputLogBean.from(quoteString));
//                            break;
//                        case IMAGE:
////                            String quotedMedia = bundle.getString(
////                                    BundleKey.CONSULT_AI_QUOTE_STRING.key());
////                            sendAiQuoteImage(quotedMedia);
//                            break;
//                    }
//                }
//            }
//        }
//    });
//
//    //服務號點選諮詢後回來 result
//    private final ActivityResultLauncher<Intent> consultSelectResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//        if (result.getData() != null) {
//            if (result.getResultCode() == REQUEST_CONSULT_AI_CODE) {
//                Bundle bundle = result.getData().getExtras();
//                if (bundle != null) {
//                    String consultAiId = bundle.getString(BundleKey.CONSULT_AI_ID.key());
//                    onAiConsultSelected(consultAiId);
//                    IntentUtil.INSTANCE.launchIntent(requireContext(), ConsultAIActivity.class,
//                            aiConsultQuoteTextResult, result.getData().getExtras());
//                }
//            } else {
//                onConsultSelected(result.getData());
//            }
//        }
//    });
//    private final AdvisoryRoomAdapter advisoryRoomAdapter = new AdvisoryRoomAdapter(aiConsultQuoteTextResult);
//
//    private ChatRoomMessageSearchAdapter adapter = null;
//
//    private ActivityResultLauncher<String> recordPermissionResult;
//    private String searchKeyWord;
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        //麥克風權限
//        recordPermissionResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//            if (isGranted) {
//                if (checkMicAvailable()) {
//                    showRecordingWindow();
//                }
//            } else {
//                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
//                IntentUtil.INSTANCE.start(requireContext(), intent);
//                ToastUtils.showToast(requireContext(), "需要麥克風權限");
//            }
//        });
//
//        //相簿權限
//        launcher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
//                result -> {
//                    if (result.get(Manifest.permission.READ_MEDIA_IMAGES) != null && result.get(Manifest.permission.READ_MEDIA_VIDEO) != null && result.get(Manifest.permission.READ_MEDIA_AUDIO) != null) {
//                        if (Objects.requireNonNull(result.get(Manifest.permission.READ_MEDIA_IMAGES)).equals(true) && Objects.requireNonNull(result.get(Manifest.permission.READ_MEDIA_VIDEO)).equals(true) && Objects.requireNonNull(result.get(Manifest.permission.READ_MEDIA_AUDIO)).equals(true)) {
//                            binding.funMedia.init(requireActivity());
//                            showFunMedia(false);
//                        } else {
//                            ToastUtils.showToast(requireContext(), getString(R.string.text_need_storage_permission));
//                        }
//                    }
//                });
//        storagePermissionResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
//            if (result) {
//                binding.funMedia.init(requireActivity());
//                showFunMedia(false);
//            } else {
//                ToastUtils.showToast(requireContext(), getString(R.string.text_need_storage_permission));
//            }
//        });
//
//        shareScreenShotResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->  {
//            if (result != null && result.getData() != null) {
//                Bundle bundle = result.getData().getExtras();
//                if (bundle != null) {
//                    Type typeToken = new TypeToken<List<String>>() {
//                    }.getType();
//                    List<String> ids = JsonHelper.getInstance().from(bundle.getString("data"), typeToken);
//                    String filePath = bundle.getString(BundleKey.FILE_PATH.key());
//                    doExecutionSendImage(ids, filePath);
//                }
//            }
//        });
//    }
//
//
//    /**
//     * 點擊臨時成員頭像
//     * @param item 該臨時成員資料
//     * @param position 該臨時成員在 recyclerView 的 position
//     * @param itemAvatarView 該臨時成員的頭像 需要當 popupWindows 的 anchor
//     * */
//    @Override
//    public void onClickEvent(@NonNull UserProfileEntity item, int position, View itemAvatarView) {
//        if (isProvisionMemberPopupShowing && (int)provisionMemberPopupWindow.getContentView().getTag() == position) {
//            isProvisionMemberPopupShowing = false;
//            return;
//        } else {
//            isProvisionMemberPopupShowing = true;
//        }
//
//        if (getContext() != null) {
//            PopupProvisionMemberActionBinding popupProvisionMemberActionBinding = PopupProvisionMemberActionBinding.inflate(LayoutInflater.from(getContext()), null, false);
//            if (provisionMemberPopupWindow == null) {
//                provisionMemberPopupWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                provisionMemberPopupWindow.setOutsideTouchable(true);
//            }
//            provisionMemberPopupWindow.setContentView(popupProvisionMemberActionBinding.getRoot());
//            popupProvisionMemberActionBinding.tvName.setText(item.getNickName());
//            popupProvisionMemberActionBinding.btnChat.setOnClickListener(v -> {
//                if (provisionMemberPopupWindow.isShowing()) provisionMemberPopupWindow.dismiss();
//                chatWithProvisionalMember(item);
//            });
//            popupProvisionMemberActionBinding.btnMainPage.setOnClickListener(v -> {
//                if (provisionMemberPopupWindow.isShowing()) provisionMemberPopupWindow.dismiss();
//                goMainPageOfProvisionalMember(item);
//            });
//            popupProvisionMemberActionBinding.btnRemoveMember.setOnClickListener(v -> {
//                if (provisionMemberPopupWindow.isShowing()) provisionMemberPopupWindow.dismiss();
//                removeProvisionalMember(item);
//            });
//
//            itemAvatarView.post(() -> {
//                if (!provisionMemberPopupWindow.isShowing()) {
//                    provisionMemberPopupWindow.getContentView().setTag(position);
//                    provisionMemberPopupWindow.showAsDropDown(itemAvatarView, (int)itemAvatarView.getLeft() * position, (int)itemAvatarView.getTop(), Gravity.CENTER);
//                }
//            });
//        }
//    }
//
//    private void chatWithProvisionalMember(UserProfileEntity entity) {
//        ChatRoomEntity entity1 = ChatRoomReference.getInstance().findById(entity.getRoomId());
//        if (entity1 == null) {
//            String userId = TokenPref.getInstance(requireActivity()).getUserId();
//            if ("".equals(entity.getRoomId())) {
//                ApiManager.getInstance().addContact(requireActivity(), new ApiListener<String>() {
//                    @Override
//                    public void onSuccess(String contactRoomId) {
//                        ApiManager.doRoomItem(requireActivity(), contactRoomId, userId, new ApiListener<ChatRoomEntity>() {
//                            @Override
//                            public void onSuccess(ChatRoomEntity entity) {
//                                boolean status = ChatRoomReference.getInstance().save(entity);
//                                if (status) {
//                                    ActivityTransitionsControl.navigateToChat(requireActivity(), entity, ChatFragment.class.getSimpleName(), (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
//                                } else {
//                                    onFailed("save room entity failed ");
//                                }
//                            }
//
//                            @Override
//                            public void onFailed(String errorMessage) {
//                                CELog.e(errorMessage);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//
//                    }
//                }, entity.getId(), entity.getAlias());
//            }
//        } else {
//            Intent intent = new Intent(requireContext(), ChatActivity.class)
//                    .putExtra(BundleKey.USER_ID.key(), entity.getId())
//                    .putExtra(BundleKey.USER_NICKNAME.key(), entity.getNickName());
//            IntentUtil.INSTANCE.start(requireContext(), intent);
//        }
//    }
//
//    private void goMainPageOfProvisionalMember(UserProfileEntity entity) {
//        ActivityTransitionsControl.navigateToEmployeeHomePage(requireActivity(), entity.getId(), entity.getUserType(), (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
//    }
//
//    private void removeProvisionalMember(UserProfileEntity entity) {
//        presenter.removeProvisionalMember(requireActivity(), chatRoom.getId(), entity.getId());
//    }
//
//    public boolean onTouchEvent(MotionEvent event) {
//        if(binding != null) {
//            if (binding.scopeRetractTip.getVisibility() == View.VISIBLE)
//                binding.scopeRetractTip.setVisibility(View.GONE);
//            return false;
//        }
//        return false;
//    }
//
//    public void onRefreshMemberList(List<String> list) {
//        if (provisionalMemberAdapter != null && provisionalMemberAdapter.getItemCount() > 0)
//            presenter.addProvisionalMember(list, provisionalMemberAdapter.getCurrentList());
//        else
//            presenter.initProvisionalMemberList(list);
//    }
//
//    public void onRemoveMember(String memberId) {
//        ThreadExecutorHelper.getIoThreadExecutor().execute(() -> {
//            if(!provisionalMemberAdapter.getCurrentList().isEmpty()) {
//                List<UserProfileEntity> newProvisionalList = provisionalMemberAdapter.getCurrentList().stream().filter(userProfile -> !userProfile.getId().equals(memberId)).collect(Collectors.toList());
//                provisionalMemberAdapter.submitList(newProvisionalList);
//            }else{
//                clearProvisionalMember();
//            }
//        });
//    }
//
//    @Override
//    public void onNetworkTime(long time) {
//        if (servicedDurationTime == 0L) {
//            servicedDurationTime = time;
//        }
//        countServiceDuration();
//    }
//
//    enum ActionStatus {
//        RICH_MENU(false),
//        SCROLL(true);
//        boolean status;
//
//        ActionStatus(boolean status) {
//            this.status = status;
//        }
//
//        /**
//         *
//         *
//         * @return
//         */
//        public boolean status() {
//            return status;
//        }
//    }
//
//    // Screenshot start and end information
//    private final List<MessageEntity> screenShotData = Lists.newArrayList();
//
//    //CreateBusinessTask
//    enum SortType {ASC, DESC}
//
//    public static ChatFragment newInstance(String userName, String userId) {
//        ChatFragment fragment = new ChatFragment();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(BundleKey.USER_NICKNAME.key(), userName);
//        bundle.putString(BundleKey.USER_ID.key(), userId);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
//
//    public static ChatFragment newInstance(MessageEntity msg, ChatRoomEntity chatRoom, String unreadMessageId, String keyWord, RoomThemeStyle themeStyle) {
//        ChatFragment fragment = new ChatFragment();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(BundleKey.EXTRA_SESSION.key(), chatRoom);
//        bundle.putSerializable(BundleKey.EXTRA_MESSAGE.key(), msg);
//        bundle.putString(BundleKey.UNREAD_MESSAGE_ID.key(), unreadMessageId);
//        bundle.putString(BundleKey.SEARCH_KEY.key(), keyWord);
//        bundle.putSerializable(BundleKey.CHAT_ROOM_STYLE.key(), themeStyle);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
//
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EventBusUtils.register(this);
//        Bundle bundle = getArguments();
//        if (bundle != null) {
//            this.chatRoom = (ChatRoomEntity) bundle.getSerializable(BundleKey.EXTRA_SESSION.key());
//            this.messageEntity = (MessageEntity) bundle.getSerializable(BundleKey.EXTRA_MESSAGE.key());
//            String unreadMessageId = bundle.getString(BundleKey.UNREAD_MESSAGE_ID.key());
//            userName = bundle.getString(BundleKey.USER_NICKNAME.key());
//            userId = bundle.getString(BundleKey.USER_ID.key());
//            searchKeyWord = bundle.getString(BundleKey.SEARCH_KEY.key());
//            themeStyle = (RoomThemeStyle) bundle.getSerializable(BundleKey.CHAT_ROOM_STYLE.key());
//            if (chatRoom != null) {
//                this.checkOnceUnreadNumber = this.chatRoom.getUnReadNum() > 0;
//            }
//            this.presenter = new ChatPresenter(chatRoom, this, unreadMessageId, requireActivity());
//        }
//        this.mainMessageData.clear();
//
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false);
//        initViewModel();
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        themeStyle();
//        if (chatRoom != null) {
//            isProvisionMember = chatRoom.getProvisionalIds().contains(getUserId()) && chatRoom.getListClassify() == ChatRoomSource.MAIN;
//            if (chatRoom.getServiceNumberStatus().equals(ServiceNumberStatus.ROBOT_SERVICE) && (!getUserId().equals(chatRoom.getOwnerId()))) { //AI服務
//                setAiServices();
//                binding.rvBottomRoomList.setVisibility(View.GONE);
//                ToastUtils.showToast(requireContext(), getString(R.string.text_robot_servicing_right_now));
//            }
//            provisionalMemberAdapter = new ProvisionalMemberAdapter(this);
//            binding.rvProvisionalMemberList.setAdapter(provisionalMemberAdapter);
//
//            if (chatRoom.getProvisionalIds() != null && !chatRoom.getProvisionalIds().isEmpty()) {
//                if (chatRoom.getProvisionalIds().contains(getUserId()))
//                    binding.scopeProvisionalMemberList.setVisibility(View.GONE);
//                else
//                    presenter.initProvisionalMemberList(chatRoom.getProvisionalIds());
//            } else
//                binding.scopeProvisionalMemberList.setVisibility(View.GONE);
//
//
//            setup();
//            init();
//            if (chatRoom.getType() == group) {
//                // 判斷是否要顯示 "沒有擁有者" 訊息
//                getMemberPrivilege();
//            }
//
//            ChatRoomType type = this.chatRoom.getType();
////            if (type.equals(ChatRoomType.subscribe) || type.equals(ChatRoomType.services)) {
////                initTitle();
////            }
//            String ownerId = this.chatRoom.getOwnerId();
//            boolean hasOwner = UserProfileReference.hasLoadlData(null, ownerId);
//            if (hasOwner) {
//
//                setInputHint(); //first time call
//                // 未編輯完成
//                String unfinishedEdited = chatRoom.getUnfinishedEdited();
//                if (!Strings.isNullOrEmpty(unfinishedEdited)) {
//                    binding.chatKeyboardLayout.setUnfinishedEdited(InputLogBean.from(unfinishedEdited));
//                }
//            } else {
//                UserProfileService.getProfile(requireActivity(), RefreshSource.REMOTE, ownerId, new ServiceCallBack<UserProfileEntity, RefreshSource>() {
//                    @Override
//                    public void complete(UserProfileEntity userProfileEntity, RefreshSource refreshSource) {
//                        setInputHint();
//                        // 未編輯完成
//                        String unfinishedEdited = chatRoom.getUnfinishedEdited();
//                        if (!Strings.isNullOrEmpty(unfinishedEdited)) {
//                            binding.chatKeyboardLayout.setUnfinishedEdited(InputLogBean.from(unfinishedEdited));
//                        }
//                    }
//
//                    @Override
//                    public void error(String message) {
//                    }
//                });
//            }
//            aiffManager = new AiffManager(requireActivity(), chatRoom.getId());
//        }
//        // EVAN_FLAG 2020-06-09 (1.11.0) 圖片選擇器，預覽後返回
//        mediaPreviewARL = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
//            if (result.getData() != null) {
//                String type = result.getData().getStringExtra(BundleKey.TYPE.key());
//                boolean isOriginal = result.getData().getBooleanExtra(BundleKey.IS_ORIGINAL.key(), false);
//                String dataJson = result.getData().getStringExtra(BundleKey.DATA.key());
//                Map<String, String> data = JsonHelper.getInstance().fromToMap(dataJson);
//                TreeMap<String, String> treeData = Maps.newTreeMap((o1, o2) -> ComparisonChain.start()
//                        .compare(o1, o2)
//                        .result());
//                treeData.putAll(data);
//                binding.funMedia.setSelectData(type, isOriginal, treeData);
//                binding.chatKeyboardLayout.doChangePicSelectorAction();
//            }
//        });
//        this.presenter.init(this.messageEntity, searchKeyWord);
//        if (chatRoom != null && chatRoom.isAtMe()) {
//            presenter.clearIsAtMeFlag(requireContext(), chatRoom.getId());
//        }
//        observeData();
//    }
//
//    private void initViewModel() {
//        ViewModelFactory viewModelFactory = new ViewModelFactory(requireActivity().getApplication());
//        chatViewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(ChatViewModel.class);
//    }
//
//    private void observeData() {
//        chatViewModel.getChatRoomEntity().observe(getViewLifecycleOwner(), chatRoomEntity -> {
//             onRefreshMemberList(chatRoomEntity.getProvisionalIds());
//        });
//
//        chatViewModel.getManagerList().observe(getViewLifecycleOwner(), this::filterToShowNoOwnerNotify);
//
//        chatViewModel.getBecomeOwnerStatus().observe(getViewLifecycleOwner(), string -> {
//            ToastUtils.showToast(requireContext(), string);
//            hideNoOwnerNotify();
//        });
//
//        chatViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
//            ToastUtils.showToast(requireContext(), errorMessage);
//        });
//
//        chatViewModel.isCancelAiWarningSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
//            hideLoadingView();
//            if (isSuccess) {
//                Toast.makeText(requireContext(), "轉回成功", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(requireContext(), requireContext().getString(R.string.text_toast_operator_failure), Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        chatViewModel.getConsultList().observe(getViewLifecycleOwner(), consultList -> {
//            if (advisoryRoomAdapter != null) {
//                if (consultList.isEmpty()) {
//                    chatRoom.setAiConsultId("");
//                }
//                advisoryRoomAdapter.submitList(consultList);
//                binding.rvBottomRoomList.setVisibility((!consultList.isEmpty()) ? View.VISIBLE : View.GONE);
//            }
//        });
//
//        chatViewModel.getConsultRoomId().observe(getViewLifecycleOwner(), consultRoomId -> {
//            if (consultRoomId.getFirst()) {
//                chatRoom.setAiConsultId(consultRoomId.getSecond());
//            } else {
//                Bundle bundle = new Bundle();
//                bundle.putString(BundleKey.EXTRA_SESSION_ID.key(), consultRoomId.getSecond());
//                IntentUtil.INSTANCE.startIntent(requireContext(), ChatActivity.class, bundle);
//            }
//        });
//
//        chatViewModel.getStartConsultError().observe(getViewLifecycleOwner(), errorData -> {
//            new AlertView.Builder()
//                    .setContext(requireContext())
//                    .setStyle(AlertView.Style.Alert)
//                    .setMessage(errorData.getErrorMessage())
//                    .setOthers(new String[]{getString(R.string.alert_confirm)})
//                    .setOnItemClickListener((o, position) -> {})
//                    .build()
//                    .setCancelable(true)
//                    .setOnDismissListener(null)
//                    .show();
//        });
//
//        chatViewModel.getOnProvisionalIdsGet().observe(getViewLifecycleOwner(), provisionalIds -> {
//            presenter.initProvisionalMemberList(provisionalIds);
//        });
//
//        chatViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
////            if (isLoading) {
////                showLoadingView();
////            } else {
////                hideLoadingView();
////            }
//        });
//        chatViewModel.getSendCloseExtraLayout().observe(getViewLifecycleOwner(), isClose -> {
//            if (isClose) {
//                binding.chatKeyboardLayout.doExtraAction(binding.chatKeyboardLayout);
//                hideLoadingView();
//            }
//        });
//        chatViewModel.getHideLoadingDialog().observe(getViewLifecycleOwner(), isHide -> {
//            if(isHide)
//                hideLoadingView();
//        });
//    }
//
//    private void hideNoOwnerNotify() {
//        binding.clNotifyNoOwner.setVisibility(View.GONE);
//    }
//
//
//    //抓取社團成員的權限
//    private void getMemberPrivilege() {
//        chatViewModel.getChatMember(chatRoom.getId());
//    }
//
//    //如果沒有管理者，則全部人都顯示
//    //有管理者，只有管理者顯示
//    private void filterToShowNoOwnerNotify(List<ChatRoomMemberResponse> groupRoomManager) {
//        if (chatRoom.getType() != group) return;
//        if (!chatRoom.getOwnerId().isEmpty()) return;
//        if (groupRoomManager == null) return;
//        if (groupRoomManager.isEmpty()) {
//            showNoOwnerNotify();
//        } else {
//            groupRoomManager.forEach(member -> {
//                if (member.getMemberId().equals(getUserId())) {
//                    showNoOwnerNotify();
//                }
//            });
//        }
//    }
//
//    private void showNoOwnerNotify() {
//        binding.clNotifyNoOwner.setVisibility(View.VISIBLE);
//        binding.tvNoOnwerText.setMovementMethod(LinkMovementMethod.getInstance());
//        binding.tvNoOnwerText.setText(KeyWordHelper.matcherKeys(0xFF0076FF, getString(R.string.group_room_no_owner), "點擊成為社團擁有者", view -> {
//            chatViewModel.becomeOwner(chatRoom.getId());
//        }));
//    }
//
//    private void showLoadingView() {
//        if (getActivity() == null) return;
//        progressBar = IosProgressBar.show(getActivity(), getString(R.string.wording_loading), true, false, dialog -> {
//        });
//    }
//
//    private void setInputHint() {
//        if (chatRoom.getServiceNumberStatus().equals(ServiceNumberStatus.ROBOT_SERVICE)) {
//            binding.chatKeyboardLayout.setInputHint(Objects.requireNonNull(requireActivity()).getString(R.string.text_robot_hand_over_immediately));
//        } else if (ChatRoomType.services.equals(chatRoom.getType())) {
//            if (getContext() == null) return;
//            binding.chatKeyboardLayout.setInputHint(
//                    getContext().getString(chatRoom.getServiceNumberOpenType().contains("I") ? R.string.text_input_hint_inside_sentences : R.string.text_input_hint_outside_sentences,
//                            chatRoom.getServiceNumberName().length() > 5 ? chatRoom.getServiceNumberName().substring(0,3)+"..." : chatRoom.getServiceNumberName(),
//                            chatRoom.getName().length() > 5 ? chatRoom.getName().substring(0,3)+"..." : chatRoom.getName()
//                    ));
//        }else
//            binding.chatKeyboardLayout.setInputHint(getString(R.string.text_hint_input_message)); //預設
//    }
//
//    private void queryMemberIsBlock() {
//        if (chatRoom == null) return;
//        List<String> memberIds = AccountRoomRelReference.findMemberIdsByRoomId(null, chatRoom.getId());
//        if (memberIds.size() > 1 && chatRoom.getType() == ChatRoomType.friend) {
//            String userId = TokenPref.getInstance(getCtx()).getUserId();
//            boolean isBlock = DBManager.getInstance().queryFriendIsBlock(memberIds.get(0).equals(userId) ? memberIds.get(1) : memberIds.get(0));
//            binding.chatKeyboardLayout.setIsBlock(isBlock);
//        }
//    }
//
//    private int setupDefaultThemeHeight(double proportion) {
//        int paramsHeight = proportion == 0.0d ? ConstraintLayout.LayoutParams.MATCH_PARENT : ConstraintLayout.LayoutParams.WRAP_CONTENT;
//        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, paramsHeight);
//        params.verticalBias = 1.0f;
//        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
//        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
//        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
//        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
//        binding.themeMRV.setLayoutParams(params);
//
//        int toolbarHeight = UiHelper.dip2px(requireContext(), 44.0f);
//        int inputHeight = UiHelper.dip2px(requireContext(), 45.0f);
//        int displayHeight = UiHelper.getDisplayHeight(requireContext());
//        int roomHeight = displayHeight - (toolbarHeight + inputHeight);
//        return (int) (roomHeight * proportion);
//    }
//
//    protected void themeStyle() {
//        if (chatRoom != null) {
////            if (!Strings.isNullOrEmpty(chatRoom.getBusinessId()) && !ChatRoomType.SERVICES_or_SUBSCRIBE.contains(chatRoom.getType())) {
////                this.themeStyle = RoomThemeStyle.BUSINESS;
////            } else {
////                if (ChatRoomType.services.equals(chatRoom.getType())
////                        && ServiceNumberType.BOSS.equals(chatRoom.getServiceNumberType())
////                        && getUserId().equals(chatRoom.getServiceNumberOwnerId())) {
////                    this.themeStyle = RoomThemeStyle.UNDEF;
////                } else {
////                    this.themeStyle = RoomThemeStyle.of(chatRoom.getType().name());
////                }
////            }
//            this.binding.chatKeyboardLayout.setChatRoomEntity(this.chatRoom, ChatRoomType.FRIEND_or_GROUP_or_DISCUS_or_SERVICE_MEMBER.contains(this.chatRoom.getType()), ChatRoomType.GROUP_or_DISCUSS_or_SERVICE_MEMBER.contains(this.chatRoom.getType()));
//        } else {
//            this.binding.chatKeyboardLayout.setChatRoomEntity(null, true, false);
//        }
//        try{
//            binding.floatTimeBoxTV.setBackgroundResource(this.themeStyle.getFloatingTimeBox());
//            binding.xrefreshLayout.setBackgroundColor(this.themeStyle.getAuxiliaryColor());
//            binding.messageRV.setBackgroundColor(this.themeStyle.getAuxiliaryColor());
//            binding.ivConsult.setImageResource(this.themeStyle.getConsultIconResId());
//            binding.chatKeyboardLayout.setThemeStyle(themeStyle);
//        } catch (Exception err) {
//            //null checked
//            Log.e(ChatFragment.TAG, err.getMessage());
//        }
//    }
//
//    private void setup() {
//        if (getContext() == null || !isAdded()) return;
//        int cellHeight = UiHelper.dip2px(requireContext(), 46f);
//        binding.mentionMHRV.setMaxHeight(cellHeight * 4);
//        binding.mentionMHRV.setBackgroundColor(Color.WHITE);
//        binding.mentionMHRV.setLayoutManager(new GridLayoutManager(requireContext(), 2));
//        binding.mentionMHRV.addItemDecoration(new GridItemDecoration(Color.WHITE));
//        binding.mentionMHRV.setItemAnimator(new DefaultItemAnimator());
//        binding.mentionMHRV.setHasFixedSize(false);
//
//        if (chatRoom != null) {
//            this.mentionSelectAdapter = new MentionSelectAdapter(requireContext())
//                    .setUserProfiles(chatRoom.getMembersLinkedList())
//                    .setKeyword("");
//        }
//
//        if (chatRoom != null && chatRoom.getMembersLinkedList().size() >= 8) {
//            ViewGroup.LayoutParams params = binding.mentionMHRV.getLayoutParams();
//            params.height = cellHeight * 4;
//        }
//
//        binding.chatKeyboardLayout.setOnMentionFeatureListener(new ChatKeyboardLayout.OnMentionFeatureListener() {
//            @Override
//            public void onShowMention(HadEditText editText, boolean isMultiSelect, String keyword) {
//                if (mentionSelectAdapter != null) {
//                    binding.mentionMHRV.setVisibility(View.VISIBLE);
//                    binding.mentionMHRV.setAdapter(mentionSelectAdapter
//                            .setKeyword(keyword)
//                            .setOnSelectItemListener((ecUserProfile, position, needCalculatePosition) -> {
//                                editText.appendMentionSelect(ecUserProfile, true, needCalculatePosition);
//                            }));
//                    if (!isMultiSelect) {
//                        mentionSelectAdapter.setKeyword(keyword).reset();
//                    } else {
//                        mentionSelectAdapter.setKeyword(keyword).refreshData();
//                    }
//                }
//                binding.mentionMHRV.measure(0, 0);
//            }
//
//            @Override
//            public void onHideMention(HadEditText editText, String keyword) {
//                if (mentionSelectAdapter != null) {
//                    mentionSelectAdapter.setKeyword(keyword);
//                }
//                binding.mentionMHRV.setVisibility(View.GONE);
//                binding.mentionMHRV.setAdapter(null);
//            }
//
//            @Override
//            public void onNotifyMentionDataChanged(LinkedList<UserProfileEntity> users) {
//                if (mentionSelectAdapter != null) {
//                    mentionSelectAdapter.setUserProfiles(users).refreshData();
//                }
//            }
//        });
//
//        binding.xrefreshLayout.setOnRefreshListener(this).setOnBackgroundClickListener(this);
//
//        int bottomDistance = UiHelper.dip2px(requireContext(), 55);
//        binding.difbDown.animate().translationY(binding.difbDown.getHeight() + bottomDistance).setInterpolator(new LinearInterpolator()).start();
//
//        // 設定滾動狀態監聽器
//        binding.messageRV.setOnMainMessageScrollStatusListener(new OnMainMessageScrollStatusListener() {
//            @Override
//            public void onStopScrolling(RecyclerView recyclerView) {
//                //获取最后一个可见view的位置
//
//                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
//                if (layoutManager instanceof LinearLayoutManager) {
//                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
//                    int lastItemPosition = linearManager.findLastVisibleItemPosition();
//                    int firstItemPosition = linearManager.findFirstVisibleItemPosition();
//                    if (mainMessageData.size() > firstItemPosition && firstItemPosition > -1) {
//                        binding.difbDown.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
//                    }
//
//                    if (!mainMessageData.isEmpty() && lastItemPosition == mainMessageData.size() - 1) {
//                        int bottomDistance = UiHelper.dip2px(requireContext(), 55);
//                        binding.difbDown.animate().translationY(binding.difbDown.getHeight() + bottomDistance).setInterpolator(new LinearInterpolator()).start();
//                        if (binding.floatingLastMessageTV.getVisibility() == View.VISIBLE) {
//                            binding.floatingLastMessageTV.setVisibility(View.GONE);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onDragScrolling(RecyclerView recyclerView) {
//                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
//                if (layoutManager instanceof LinearLayoutManager) {
//                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
//
//                    lastItemPosition = linearManager.findLastVisibleItemPosition();
//                    int index = linearManager.findFirstVisibleItemPosition();
//
//                    if (mainMessageData.size() > index && index > -1) {
//                        MessageEntity message = mainMessageData.get(index);
//                        String dateTime = TimeUtil.INSTANCE.getDateShowString(message.getSendTime(), true);
//                        binding.floatTimeBoxTV.setText(dateTime);
//                        binding.floatTimeBoxTV.setVisibility(View.VISIBLE);
//                        binding.floatTimeBoxTV.setAlpha(1.0f);
//                        binding.floatTimeBoxTV.removeCallbacks(timeBoxTarget);
//                        binding.floatTimeBoxTV.postDelayed(timeBoxTarget, 1500L);
//                    }
//
//                    if (!mainMessageData.isEmpty() && lastItemPosition == mainMessageData.size() - 1) {
//                        presenter.displayMore();
//                        if (binding.floatingLastMessageTV.getVisibility() == View.VISIBLE) {
//                            doFloatingLastMessageClickAction(binding.floatingLastMessageTV);
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onAutoScrolling(RecyclerView recyclerView) {
//            }
//        });
//
//        if (chatRoom != null) {
//            binding.themeMRV.setContainer(Lists.newArrayList(), chatRoom);
//        }
//
//        binding.chatKeyboardLayout.getRootView().getViewTreeObserver().addOnGlobalLayoutListener(
//                new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        float mRootPreHeight = (float) binding.chatKeyboardLayout.getRootView().getHeight();
//                        if (mRootPreHeight != 0) {
//                            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)binding.scopeRobotChat.getLayoutParams();
//                            layoutParams.bottomMargin = (int) mRootPreHeight;
//                            binding.scopeRobotChat.requestLayout();
//                            binding.scopeRobotChat.invalidate();
//                            binding.chatKeyboardLayout.getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                        }
//                    }
//                });
//
//        queryMemberIsBlock();
//        initBottomRoomList();
//        if (chatRoom.getType().equals(ChatRoomType.serviceMember)) {
//            initBottomMemberControl();
//        }
//
//        if (chatRoom != null) {
//
//            if (chatRoom.isService(getUserId()) || chatRoom.isBoos(getUserId())) {
////                binding.chatKeyboardLayout.setBackgroundColor(Color.parseColor("#F1F4F5"));
//                if (ServiceNumberType.BOSS.equals(chatRoom.getServiceNumberType())) {
//                    getIdentityList();
//                }
////                doServiceNumberServicedStatus(chatRoom);
//            }
//        }
//        binding.ivClose.setOnClickListener(v -> binding.scopeRobotChat.setVisibility(View.GONE));
//        binding.robotChatMessage.getSettings().setBuiltInZoomControls(true);
//        binding.robotChatMessage.getSettings().setDisplayZoomControls(false);
//        binding.robotChatMessage.getSettings().setDomStorageEnabled(true);
//        binding.robotChatMessage.getSettings().setJavaScriptEnabled(true);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            WebView.setWebContentsDebuggingEnabled(true);
//        }
//        binding.robotChatMessage.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                view.loadUrl(url);
//                return true;
//            }
//
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//                stopIosProgressBar();
//            }
//        });
//    }
//
//    @Override
//    public void initMessageList() {
//        if (chatRoom != null) {
//            UserProfileEntity owner = UserProfileReference.findById(null, chatRoom.getOwnerId());
//            binding.messageRV.setContainer(mainMessageData, chatRoom, owner);
//            binding.messageRV.setOnTouchListener((view, motionEvent) -> false);
//        }
//    }
//
//    @Override
//    public void initKeyboard() {
//        binding.chatKeyboardLayout.recorder();
//        keyBoardBarListener = new KeyBoardBarListener();
//        binding.chatKeyboardLayout.setOnKeyBoardBarListener(keyBoardBarListener);
//        binding.funMedia.setKeyBoardBarListener(keyBoardBarListener);
//        if (chatRoom != null) {
//            if (getUserId().equals(chatRoom.getOwnerId()) && chatRoom.getServiceNumberOpenType().contains("C")) {
//                binding.chatKeyboardLayout.setChatDisable("諮詢已結束，無法回覆訊息");
//            }
//            UserProfileEntity user = DBManager.getInstance().queryFriend(chatRoom.getMemberIds().size() > 1 ? chatRoom.getMemberIds().stream().filter(id -> !id.equals(getUserId())).findFirst().orElse(null) : chatRoom.getOwnerId());
//            if(user!=null && Objects.equals(user.getStatus(), User.Status.DISABLE)) {
//                binding.chatKeyboardLayout.setKeyboardDisabled(true, getString(R.string.text_forbidden_user_can_not_send_message));
//                if (getActivity() instanceof ChatActivity) {
//                    ((ChatActivity) getActivity()).disableInvite();
//                }
//            }
//        }
//    }
//
//    @Override
//    public void initListener() {
//        binding.messageRV.setOnMessageControlEventListener(new OnMainMessageControlEventListener<MessageEntity>() {
//            @Override
//            public void onItemClick(MessageEntity entity) {
//            }
//
//            /**
//             * 範圍選取
//             */
//            @Override
//            public void doRangeSelection(MessageEntity entity) {
//                buildUpRangeScreenshotData(entity);
//            }
//
//            /**
//             * 補漏訓 id to id
//             * @param current  // 比較新
//             * @param previous // 比較舊
//             */
//            @Override
//            public void makeUpMessages(MessageEntity current, MessageEntity previous) {
//            }
//
//            @Override
//            public void onItemChange(MessageEntity entity) {
//            }
//
//            @Override
//            public void onInvalidAreaClick(MessageEntity entity) {
//                binding.chatKeyboardLayout.showKeyboard();
//            }
//
//            @Override
//            public void onImageClick(MessageEntity entity) {
//                if (MessageType.BUSINESS.equals(entity.getType())) {
//                    ((ChatActivity) getActivity()).triggerToolbarClick();
//                } else if (MessageType.IMAGE.equals(entity.getType())) {
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable(BundleKey.PHOTO_GALLERY_MESSAGE.key(), entity);
//                    bundle.putString(BundleKey.ROOM_ID.key(), chatRoom.getId());
//                    bundle.putString(BundleKey.CHAT_ROOM_NAME.key(), chatRoom.getName());
//                    bundle.putString(BundleKey.ROOM_TYPE.key(), ChatRoomEnum.NORMAL_ROOM.name());
//                    IntentUtil.INSTANCE.startIntent(requireContext(), MediaGalleryActivity.class, bundle);
//                }
//            }
//
//            @Override
//            public void onLongClick(MessageEntity entity, int pressX, int pressY) {
////                if (binding.lyChildChat.getVisibility() == View.VISIBLE) {
////                    binding.lyChildChat.setVisibility(View.GONE);
////                }
//                if (binding.themeMRV.getVisibility() == View.VISIBLE) {
//                    binding.themeMRV.setVisibility(View.GONE);
//                }
//                if (binding.searchBottomBar.getVisibility() == View.VISIBLE) {
//                    return;
//                }
//                binding.funMedia.setVisibility(View.GONE);
//
//                presenter.isObserverKeboard = false;
//                actionStatus = ActionStatus.RICH_MENU;
//                binding.chatKeyboardLayout.hideKeyboard();
//                binding.chatKeyboardLayout.isOpenFuncView();
//                List<RichMenuBottom> gridMenus = Lists.newArrayList();
//
//                MessageType messageType = entity.getType();
//                if (!Strings.isNullOrEmpty(entity.getThemeId()) && !Strings.isNullOrEmpty(entity.getNearMessageContent())) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.REPLY_RICH, entity));
//                } else if (MessageType.AT.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.AT_RICH, entity));
//                } else if (MessageType.TEXT.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.TEXT_RICH, entity));
//                } else if (MessageType.VOICE.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.VOICE_RICH, entity));
//                } else if (MessageType.FILE.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.OTHER_RICH, entity));
//                } else if (MessageType.STICKER.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.STICKER_RICH, entity));
//                } else if (MessageType.IMAGE.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.IMAGE_RICH, entity));
//                } else if (MessageType.IMAGE_TEXT.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.OTHER_RICH, entity));
//                } else if (MessageType.CALL.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.CALL_RICH, entity));
//                } else if (MessageType.BUSINESS_TEXT.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.TEXT_RICH, entity));
//                } else if (MessageType.VIDEO.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.VIDEO_RICH, entity));
//                } else if (MessageType.TEMPLATE.equals(messageType)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.TEMPLATE_RICH, entity));
//                } else {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.TEXT_RICH, entity));
//                }
//
//                if (ChatRoomType.subscribe.equals(chatRoom.getType()) || !UserPref.getInstance(requireContext()).hasBusinessSystem()) {
//                    gridMenus.remove(RichMenuBottom.TASK);
//                }
//
//                //1. 如果使用者是owner(ios 只有移除諮詢服務號), 請服務號是諮詢服務號則不顯示
//                //2. 如果是 serviceNumber opentype 有O代表對外, 不顯示
//                if (getUserId().equals(chatRoom.getOwnerId()) && chatRoom.getServiceNumberOpenType().contains("C")
//                        || (chatRoom.getServiceNumberOpenType().contains("O") && !ChatRoomType.serviceMember.equals(chatRoom.getType()))
//                ) {
//                    gridMenus.remove(RichMenuBottom.REPLY);
//                }
//
//                if (ChatRoomType.services.equals(chatRoom.getType())) {
//                    gridMenus.remove(RichMenuBottom.RECOVER);
//                }
//
//                if (ChatRoomType.system.equals(chatRoom.getType())) {
//                }
//
//                binding.chatKeyboardLayout.setRichMenuGridCount(5)
//                        .setOnItemClickListener(entity, gridMenus, classifyAiffInMenu(chatRoom), new BottomRichMeunAdapter.OnItemClickListener() {
//
//                                    @Override
//                                    public void onClick(MessageEntity msg, RichMenuBottom menu, int position) {
//                                        binding.chatKeyboardLayout.showKeyboard();
//                                        presenter.isObserverKeboard = true;
//
//                                        switch (menu) {
//                                            case MULTI_COPY: // 多選複製
//                                                msg.setDelete(menu.isMulti());
//                                                openBottomRichMenu(RichMenuBottom.MULTI_COPY, MULTIPLE_SELECTION, Lists.newArrayList(RichMenuBottom.MULTI_COPY, RichMenuBottom.CANCEL));
//                                                break;
//                                            case MULTI_TRANSPOND: // 多選轉發
//                                                msg.setDelete(menu.isMulti());
//                                                openBottomRichMenu(RichMenuBottom.MULTI_TRANSPOND, MULTIPLE_SELECTION, Lists.newArrayList(RichMenuBottom.MULTI_TRANSPOND, RichMenuBottom.CANCEL));
//                                                break;
////                                    case TRANSPOND: // 轉發
////                                        executeTranspond(Lists.newArrayList(msg));
////                                        actionStatus = ActionStatus.SCROLL;
////                                        break;
////                                    case COPY: // 複製
////                                        executeCopy(Lists.newArrayList(msg));
////                                        actionStatus = ActionStatus.SCROLL;
////                                        break;
//                                            case DELETE: // 刪除
//                                                executeDelete(Lists.newArrayList(msg));
//                                                actionStatus = ActionStatus.SCROLL;
//                                                break;
//                                            case RECOVER: // 收回
//                                                binding.tvTip.setText(requireContext().getString(R.string.text_retract_tip, TokenPref.getInstance(requireActivity()).getRetractValidMinute()));
//                                                binding.scopeRetractTip.setVisibility(View.VISIBLE);
//                                                if(TokenPref.getInstance(requireContext()).getRetractRemind()) {
//                                                    binding.scopeRetractTipText.setVisibility(View.GONE);
//                                                }
//                                                binding.cbTip.setOnClickListener(v-> {
//                                                    if(binding.scopeRetractTip.getVisibility()==View.GONE) {
//                                                        binding.scopeRetractTip.setVisibility(View.VISIBLE);
//                                                    }
//                                                });
//                                                binding.btnEdit.setOnClickListener(v-> {
//                                                    //編輯
//                                                    if(binding.cbTip.isChecked())
//                                                        TokenPref.getInstance(requireContext()).setRetractRemind(true);
//                                                    executeRecover(Lists.newArrayList(msg));
//                                                    actionStatus = ActionStatus.SCROLL;
//                                                    onTipClick(msg);
//                                                });
//                                                binding.btnRetract.setOnClickListener(v -> {
//                                                    //收回
//                                                    if(binding.cbTip.isChecked())
//                                                        TokenPref.getInstance(requireContext()).setRetractRemind(true);
//                                                    executeRecover(Lists.newArrayList(msg));
//                                                    actionStatus = ActionStatus.SCROLL;
//                                                });
//                                                break;
//                                            case REPLY: // 回覆
//                                                executeReply(msg);
//                                                actionStatus = ActionStatus.SCROLL;
//                                                break;
//                                            case SHARE: // 分享
//                                                executeShare(msg);
//                                                actionStatus = ActionStatus.SCROLL;
//                                                break;
//                                            case SCREENSHOTS: // 截圖
//                                                msg.setShowSelection(true);
//                                                screenShotData.add(msg);
//                                                binding.xrefreshLayout.setBackgroundColor(0XFF525252);
//                                                binding.messageRV.setBackgroundColor(0XFF525252);
//                                                if (getActivity() instanceof ChatActivity) {
//                                                    ((ChatActivity) getActivity()).showToolBar(false);
//                                                }
//                                                binding.clBottomServicedBar.setVisibility(View.GONE);
//                                                List<RichMenuBottom> richMenuBottoms = Lists.newArrayList(RichMenuBottom.ANONYMOUS.position(0), RichMenuBottom.PREVIEW.position(1), RichMenuBottom.SHARE.position(2), RichMenuBottom.CANCEL.position(3), RichMenuBottom.SAVE.position(4));
//                                                openBottomRichMenu(RichMenuBottom.SCREENSHOTS, RANGE_SELECTION, richMenuBottoms);
//                                                break;
//                                            case TASK: // 多選任務
//                                                msg.setShowSelection(true);
//                                                screenShotData.add(msg);
//                                                binding.xrefreshLayout.setBackgroundColor(0XFF525252);
//                                                binding.messageRV.setBackgroundColor(0XFF525252);
//                                                if (getActivity() instanceof ChatActivity) {
//                                                    ((ChatActivity) getActivity()).showToolBar(false);
//                                                }
//                                                List<RichMenuBottom> taskRichMenus = Lists.newArrayList(RichMenuBottom.ANONYMOUS.position(0), RichMenuBottom.NEXT.position(1).str(R.string.alert_preview), RichMenuBottom.CANCEL.position(2), RichMenuBottom.CONFIRM.position(3));
//                                                openBottomRichMenu(RichMenuBottom.TASK, RANGE_SELECTION, taskRichMenus);
//                                                break;
//                                                executeTodo(msg);
//                                                actionStatus = ActionStatus.SCROLL;
//                                                break;
//
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancle() {
//                                        new Handler().postDelayed(() -> {
//                                            presenter.isObserverKeboard = true;
//                                            actionStatus = ActionStatus.SCROLL;
//                                        }, 1000);
//                                    }
//                                }
//                                , (msg, aiffId) -> {
//                                    AiffInfo aiffInfo = AiffDB.getInstance(requireContext()).getAiffInfoDao().getAiffInfo(aiffId);
//                                    aiffManager.showAiffViewByInfo(aiffInfo);
//                                });
//            }
//
//
//            @Override
//            public void onAtSpanClick(String userId) {
//                if (getActivity() instanceof ChatActivity) {
//                    UserProfileEntity account = DBManager.getInstance().queryFriend(userId);
//                    ((ChatActivity) getActivity()).toChatRoomByUserProfile(account);
//                }
//            }
//
//            @Override
//            public void onSendNameClick(String sendId) {
//                binding.chatKeyboardLayout.appendMentionSelectById(sendId);
//            }
//
//            @Override
//            public void onTipClick(MessageEntity entity) {
//                if (MessageType.AT_or_TEXT.contains(entity.getType())) {
//                    String input = "";
//                    IMessageContent content = entity.content();
//                    if (content instanceof TextContent) {
//                        input = content.simpleContent();
//                    } else if (content instanceof AtContent) {
//                        List<MentionContent> ceMentions = ((AtContent) content).getMentionContents();
//                        SpannableStringBuilder builder = AtMatcherHelper.matcherAtUsers("@", ceMentions, chatRoom.getMembersTable());
//                        input = builder.toString();
//                    }
//
//                    if (!Strings.isNullOrEmpty(input)) {
//                        binding.chatKeyboardLayout.clearInputArea();
//                        binding.chatKeyboardLayout.setInputHETText(input);
//                        KeyboardHelper.open(requireView());
//                    }
//                }
//            }
//
//            @Override
//            public void onSubscribeAgentAvatarClick(String senderId) {
//                if (getActivity() instanceof ChatActivity) {
//                    ((ChatActivity) getActivity()).navigateToSubscribePage();
//                }
//            }
//
//            @Override
//            public void onAvatarClick(final String senderId) {
//
//                if (ChatRoomType.system.equals(chatRoom.getType())) {
//                    return;
//                }
//                UserProfileService.getProfile(getCtx(), RefreshSource.LOCAL, senderId, new ServiceCallBack<UserProfileEntity, RefreshSource>() {
//                    @Override
//                    public void complete(UserProfileEntity profileEntity, RefreshSource source) {
//                        if (ChatRoomType.services.equals(chatRoom.getType())) {
//                            ActivityManager.addActivity((ChatActivity) getActivity());
//                            if (!((ChatActivity) requireContext()).checkClientMainPageFromAiff()) {
//                                ActivityTransitionsControl.navigateToVisitorHomePage(requireContext(), chatRoom.getOwnerId(), profileEntity.getRoomId(), UserType.VISITOR, profileEntity.getNickName(), (intent, s) -> {
//                                    IntentUtil.INSTANCE.start(requireContext(), intent.putExtra(BundleKey.WHERE_COME.key(), profileEntity.getName()));
//                                });
//                            }
//                            return;
//                        }
//
//                        if (ChatRoomType.friend.equals(chatRoom.getType())) {
//                            UserType userType = profileEntity.getUserType();
//                            if (UserType.VISITOR.equals(userType)) {
//                                Toast.makeText(requireContext(), "無訪客主頁", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//                            ActivityManager.addActivity((ChatActivity) getActivity());
//                            ActivityTransitionsControl.navigateToEmployeeHomePage(requireContext(), profileEntity.getId(), profileEntity.getUserType(), (intent, s) -> {
//                                startActivityForResult(intent, 100);
//                                IS_ACTIVITY_FOR_RESULT = true;
//                            });
//                        } else if (!Strings.isNullOrEmpty(profileEntity.getRoomId())) {
//                            ActivityTransitionsControl.navigateToChat(requireActivity(), profileEntity.getRoomId(), (intent, s) -> {
//                                IntentUtil.INSTANCE.start(requireContext(), intent);
//                                requireActivity().finish();
//                            });
//                        } else {
//                            if (!((ChatActivity) requireActivity()).checkClientMainPageFromAiff()) {
//                                ActivityTransitionsControl.navigateToVisitorHomePage(requireContext(), chatRoom.getOwnerId(), profileEntity.getRoomId(), UserType.VISITOR, profileEntity.getNickName(), (intent, s) -> {
//                                    IntentUtil.INSTANCE.start(requireContext(), intent.putExtra(BundleKey.WHERE_COME.key(), profileEntity.getName()));
//                                });
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void error(String message) {
//                        CELog.e(message);
//                    }
//                });
//            }
//
//            @Override
//            public void onAvatarLoad(ImageView iv, String senderUrl) {
//                if (senderUrl == null) {
//                    return;
//                }
//                if (URLUtil.isValidUrl(senderUrl)) {
//                    Glide.with(getCtx())
//                            .load(senderUrl)
//                            .apply(new RequestOptions()
//                                    .placeholder(R.drawable.default_avatar)
//                                    .error(R.drawable.default_avatar)
//                                    .fitCenter())
//                            .into(iv);
//                }
//            }
//
//            @Override
//            public void onContentUpdate(String msgId, String formatName, String formatContent) {
//                MessageReference.updateMessageFormat(msgId, formatName, formatContent);
//            }
//
//
//            @Override
//            public void copyText(MessageEntity entity) {
//                executeCopy(Lists.newArrayList(entity));
//            }
//
//            @Override
//            public void replyText(MessageEntity entity) {
//                executeReply(entity);
//            }
//
//            @Override
//            public void tranSend(MessageEntity entity) {
//                executeTranspond(Lists.newArrayList(entity));
//            }
//
//            @Override
//            public void retry(MessageEntity entity) {
//                executeReRetry(entity);
//            }
//
//            @Override
//            public void cellect(MessageEntity entity) {
//            }
//
//            @Override
//            public void shares(MessageEntity entity, View image) {
//                executeShare(entity);
//            }
//
//            @Override
//            public void choice() {
//                showChecked();
//            }
//
//            @Override
//            public void delete(final MessageEntity entity) {
//                executeDelete(Lists.newArrayList(entity));
//            }
//
//            @Override
//            public void enLarge(MessageEntity entity) {
//                ActivityTransitionsControl.navigateToEnLargeMessage(getCtx(), entity, (intent, s) -> {
//                    IntentUtil.INSTANCE.start(requireContext(), intent);
//                    requireActivity().overridePendingTransition(R.anim.open_enter, R.anim.open_exit);
//                });
//            }
//
//            @Override
//            public void onPlayComplete(MessageEntity msg) {
//                int index = ChatFragment.this.mainMessageData.indexOf(msg);
//                if (mainMessageData.size() - 1 > index) {
//                    for (int i = index + 1; i < mainMessageData.size(); i++) {
//                        MessageEntity _msg = mainMessageData.get(i);
//
//                        if (MessageType.VOICE.equals(msg.getType()) && msg.content() instanceof VoiceContent) {
//                            VoiceContent voiceContent = ((VoiceContent) msg.content());
//                            if (voiceContent.isRead()) {
//                                continue;
//                            } else {
//                                MessageViewBase holder = binding.messageRV.getAdapter().getHolder(i);
//                                if (holder instanceof VoiceMessageView) {
//                                    ((VoiceMessageView) holder).playLeft();
//                                }
//                                return;
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void retractMsg(final MessageEntity msg) {
//                executeRecover(Lists.newArrayList(msg));
//            }
//
//            @Override
//            public void showRePlyPanel(MessageEntity msg) {
//                showThemeView(msg.getThemeId());
//            }
//
//            @Override
//            public void findReplyMessage(String messageId) {
//                ThreadExecutorHelper.getIoThreadExecutor().execute(()-> {
//                    List<MessageEntity> filterData = mainMessageData.stream().filter(mainMessage -> mainMessage.getId().equals(messageId)).collect(Collectors.toList());
//                    if (filterData.size() > 0) {
//                        MessageEntity message = filterData.get(0);
//                        int index = mainMessageData.indexOf(message);
//                        binding.messageRV.post(() -> {
//                            message.setAnimator(true);
//                            ((LinearLayoutManager) binding.messageRV.getLayoutManager()).scrollToPositionWithOffset(index, 0);
//                            binding.messageRV.getAdapter().notifyItemChanged(index);
//                        });
//                    } else {
//                        if (getActivity() != null && !getActivity().isDestroyed()) {
//                            presenter.refreshMoreMsg(getFirstMsgId());
//                            findReplyMessage(messageId);
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onVideoClick(MessageEntity entity) {
//                Bundle bundle = new Bundle();
//                bundle.putSerializable(BundleKey.PHOTO_GALLERY_MESSAGE.key(), entity);
//                bundle.putString(BundleKey.ROOM_ID.key(), entity.getRoomId());
//                bundle.putString(BundleKey.ROOM_TYPE.key(), ChatRoomEnum.NORMAL_ROOM.name());
//                IntentUtil.INSTANCE.startIntent(binding.getRoot().getContext(), MediaGalleryActivity.class, bundle);
//            }
//
//            @Override
//            public void locationMsg(MessageEntity msg) {
//            }
//
//            @Override
//            public void onStopOtherVideoPlayback(MessageEntity msg) {
//                Iterator<MessageEntity> iterator = ChatFragment.this.mainMessageData.iterator();
//                int index = 0;
//                while (iterator.hasNext()) {
//                    MessageEntity m = iterator.next();
//                    if (MessageType.VIDEO.equals(m.getType()) && m.content() instanceof VideoContent) {
//                        if (!m.equals(msg) && ((VideoContent) m.content()).isPlaying()) { // 如果不是該視頻訊息且在播放
//                            binding.messageRV.getAdapter().notifyItemChanged(index);
//                        }
//                    }
//                    index++;
//                }
//            }
//        });
//
//        binding.messageRV.setOnRobotClickListener(
//                link -> {
//                    if (!link.isEmpty()) {
//                        binding.scopeRobotChat.setVisibility(View.VISIBLE);
//                        binding.robotChatMessage.loadUrl(link);
//                    }
//                }
//        );
//
//        binding.messageRV.setOnTemplateClickListener(
//                content -> {
//                    chatViewModel.sendQuickReplyMessage(chatRoom.getId(), "Action", content);
//                }
//        );
//
//        binding.messageRV.setOnMessageClickListener(
//                messageEntity -> {
//                    executeReply(messageEntity);
//                }
//        );
//
//        binding.messageRV.setOnFacebookPublicReplyClick(new OnFacebookReplyClick() {
//
//            @Override
//            public void onPublicReply(@NonNull MessageEntity message, @NonNull String postId, @NonNull String commentId) {
//                isFacebookReplyPublic = true;
//                setFacebookCommentStatus(message);
//                binding.tvFacebookReplyTypeText.setText(getString(R.string.facebook_public_reply));
//                binding.chatKeyboardLayout.getFacebookOverTimeView().setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onPrivateReply(MessageEntity message) {
//                // 超過7天
//                if (isFacebookReplyOverTime) {
//                    Toast.makeText(requireContext(), getString(R.string.facebook_overtime_toast), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                // 已經回覆過
//                if (message.isFacebookPrivateReplied()) {
//                    Toast.makeText(requireContext(), getString(R.string.facebook_already_private_replied), Toast.LENGTH_SHORT).show();
//                    FacebookTag facebookTag = JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
//                    presenter.moveToFacebookReplyMessage(message.getId(), facebookTag.getData().getCommentId(), mainMessageData);
//                    return;
//                }
//
//                isFacebookReplyPublic = false;
//                setFacebookCommentStatus(message);
//                binding.tvFacebookReplyTypeText.setText(getString(R.string.facebook_private_reply));
//            }
//        });
//
//        binding.messageRV.setCheckCommentStatus(new CheckFacebookCommentStatus() {
//            @Override
//            public void checkStatus(@NonNull MessageEntity message) {
//                presenter.checkCommentStatus(message);
//            }
//        });
//
//        binding.chatKeyboardLayout.setFacebookOverTimeViewClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(requireContext(), getString(R.string.facebook_overtime_toast), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void setFacebookCommentStatus(MessageEntity message) {
//        executeFacebookReply(message);
//        binding.chatKeyboardLayout.getInputHET().setFocusableInTouchMode(true);
//        binding.chatKeyboardLayout.getInputHET().requestFocus();
//        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(binding.chatKeyboardLayout.getInputHET(), InputMethodManager.SHOW_IMPLICIT);
//        binding.facebookGroup.setVisibility(View.VISIBLE);
//    }
//
//    private void initBottomMemberControl() {
//        if (chatRoom == null || App.getInstance().serviceChatRoom == null) return;
//        Set<ChatRoomEntity> list = new HashSet<>();
//        WaitTransferDialogBuilder waitTransferDialogBuilder = new WaitTransferDialogBuilder(requireContext());
//        if (ChatRoomType.serviceMember.equals(chatRoom.getType())) {
//            for (ChatRoomEntity entity : App.getInstance().serviceChatRoom) {
//                if (entity.getServiceNumberId().equals(chatRoom.getServiceNumberId())
//                        && entity.isTransferFlag() && !entity.getServiceNumberStatus().equals(ServiceNumberStatus.TIME_OUT)) {
//                    binding.clBottomMemberControl.setVisibility(View.VISIBLE);
//                    for (ChatRoomEntity data : App.getInstance().serviceChatRoom) {
//                        if (data.isTransferFlag()) {
//                            list.add(data);
//                        }
//                    }
//                    waitTransferDialogBuilder.setList(new ArrayList<>(list));
//                    binding.btnWaitTransfer.setOnClickListener(v -> {
//                        waitTransferDialogBuilder.create().show();
//                    });
//                }
//            }
//        }
//        if (list.size() > 0) {
//            binding.tvWaitTransferNumber.setText(String.valueOf(list.size()));
//            binding.tvWaitTransferNumber.setVisibility(View.VISIBLE);
//            binding.btnWaitTransfer.performClick();
//        } else {
//            binding.tvWaitTransferNumber.setVisibility(View.GONE);
//        }
//    }
//
//    private void initBottomRoomList() {
//        if (chatRoom == null) return;
//        if (binding.rvBottomRoomList.getAdapter() == null) {
//            binding.rvBottomRoomList.setAdapter(this.advisoryRoomAdapter);
//        }
//        if (ChatRoomType.services.equals(this.chatRoom.getType()) && !ChatRoomType.provisional.equals(chatRoom.getRoomType())) {
//            chatViewModel.getServiceNumberMemberRoom(chatRoom.getServiceNumberId(), themeStyle.getServiceMemberIconResId());
//        }
//
//        binding.rvBottomRoomList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
//        chatViewModel.getConsultTodoList(chatRoom.getType(), chatRoom.getId());
//    }
//
//    private void showChecked() {
//        for (MessageEntity message : mainMessageData) {
//            message.setShowChecked(true);
//        }
//        refreshListView();
//        setRightView();
//    }
//
//    private void setRightView() {
//        ChatActivity activity = (ChatActivity) getActivity();
//        activity.setRightView();
//    }
//
//    public void hideChecked() {
//        for (MessageEntity message : mainMessageData) {
//            message.setShowChecked(false);
//            message.setDelete(false);
//        }
//        refreshListView();
//    }
//
//    @Override
//    public HadEditText.SendData getInputAreaContent() {
//        return this.binding.chatKeyboardLayout.getInputHET().getTextData();
//    }
//
//    @Override
//    public void clearTypedMessage() {
//        binding.chatKeyboardLayout.clearInputArea();
//    }
//
//    @TargetApi(Build.VERSION_CODES.N)
//    @Override  //每次展示訊息
//    public void displayMainMessage(boolean isNew, boolean isSendMsgDisplay, boolean scrollToBottom, MessageEntity message, boolean isFriend) {
//        if (mainMessageData.contains(message)) {
//            binding.messageRV.notifyChange(message);
//            return;
//        }
//        boolean canScrollBottom = binding.messageRV.canScrollBottom();
//
//        String date = messageTimeLineFormat.format(message.getSendTime());
//        if (currentDate != null) {
//            if (mainMessageData.size() == 0) {
//                currentDate = messageTimeLineFormat.format(System.currentTimeMillis());
//            } else {
//                currentDate = messageTimeLineFormat.format(mainMessageData.get(mainMessageData.size() - 1).getSendTime());
//            }
//        }
//        if (!date.equals(currentDate) || mainMessageData.size() == 0) {
//            currentDate = date;
//            long dayBegin = TimeUtil.INSTANCE.getDayBegin(message.getSendTime());
//
//            MessageEntity timeMessage = new MessageEntity.Builder()
//                    .id(Tools.generateTimeMessageId(dayBegin))
//                    .sendTime(dayBegin)
//                    .roomId(chatRoom.getId())
//                    .status(MessageStatus.SUCCESS)
//                    .sourceType(SourceType.SYSTEM)
//                    .content(new UndefContent("TIME_LINE").toStringContent())
//                    .sendTime(dayBegin)
//                    .build();
//            if (!this.mainMessageData.contains(timeMessage)) {
//                mainMessageData.add(timeMessage);
//            }
//        }
//
//        boolean doesNotExist = false;
//
//        if (this.mainMessageData.contains(message)) {
//            int index = this.mainMessageData.indexOf(message);
//            mainMessageData.remove(index);
//        } else {
//            doesNotExist = true;
//        }
//        mainMessageData.remove(message);
//
//        //sync the nickname of sender when changed
//        if (!getUserId().equals(message.getSenderId()) && isNew && scrollToBottom) {
//            checkAndSyncSenderNicknameForNewMsg(message, isFriend);
//        }
//        //取得 add 前後的 size 以便 notifyItemRangeChanged
//        int originSize = mainMessageData.size();
//        mainMessageData.add(message);
//        int size = mainMessageData.size();
//        // 将顯示在聊天室的回复消息同步到回复列表中
//        if (!Strings.isNullOrEmpty(message.getThemeId()) || !Strings.isNullOrEmpty(message.getNearMessageId())) {
//            binding.themeMRV.setData(Strings.isNullOrEmpty(message.getThemeId()) ? message.getNearMessageId() : message.getThemeId(), message);
//        }
//
//        if (this.undeadLineMessage != null && !this.undeadLineDrawable) {
//            int scrollIndex = mainMessageData.indexOf(this.undeadLineMessage) - chatRoom.getUnReadNum();
//            refreshDataAndscrollToPosition(scrollIndex - 1);
//        } else {
//            if (isNew && canScrollBottom) {
//                binding.messageRV.refreshData();
//                if (!getUserId().equals(message.getSenderId())) {
//                    if (doesNotExist) {
//                        showFloatingLastMessage(message);
//                    }
//                }
//            } else {
//                if (scrollToBottom) {
//                    binding.messageRV.refreshToBottom(this.actionStatus.status(), originSize, size);
//                } else {
//                    binding.messageRV.refreshData();
//                }
//            }
//        }
//    }
//
//    private void checkAndSyncSenderNicknameForNewMsg(MessageEntity msg, boolean isFriend) {
//        //check nickName
//        if (msg != null) {
//            String senderNickNameInList = ""; //get the last one
//            if (msg.getSenderId() != null) {
//                for (int i = mainMessageData.size() - 1; i > 0; i--) {
//                    if (mainMessageData.get(i).getSenderId() != null) {
//                        if (mainMessageData.get(i).getSenderId().equals(msg.getSenderId())) {
//                            senderNickNameInList = mainMessageData.get(i).getSenderName();
//                            break;
//                        }
//                    }
//                }
//                if (msg.getSenderName() != null) {
//                    if (!senderNickNameInList.equals(msg.getSenderName())) {
//                        //update the list to correct nickname
//                        if (isFriend) {
//                            if (onChatRoomTitleChangeListener != null) {
//                                onChatRoomTitleChangeListener.onTitleChangeListener(msg.getSenderName());
//                            }
//                        }
//                        for (int i = mainMessageData.size() - 1; i > 0; i--) {
//                            if (mainMessageData.get(i).getSenderId() == null) //some senderId may be null, do not need to handle it
//                                continue;
//                            if (mainMessageData.get(i).getSenderId().equals(msg.getSenderId()))
//                                mainMessageData.get(i).setSenderName(msg.getSenderName());
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * 底部浮動信息視窗
//     */
//    private void showFloatingLastMessage(MessageEntity message) {
//        String context = "";
//        String name = message.getSenderName();
//        if (getUserId().equals(message.getSenderId())) {
//            name = "我";
//        }
//        context = message.content().simpleContent();
//        if (message.content() instanceof AtContent) {
//            try {
//                List<MentionContent> ceMentions = ((AtContent) message.content()).getMentionContents();
//                SpannableStringBuilder builder = AtMatcherHelper.matcherAtUsers("@", ceMentions, this.chatRoom.getMembersTable());
//                context = builder.toString();
//            } catch (Exception e) {
//                context = "[標註訊息]";
//            }
//        }
//        if (!Strings.isNullOrEmpty(context)) {
//            binding.floatingLastMessageTV.setVisibility(View.VISIBLE);
//            binding.floatingLastMessageTV.setText(MessageFormat.format("{0}: {1}", name, context));
//        }
//    }
//
//    @TargetApi(Build.VERSION_CODES.N)
//    @Override
//    public void onRefreshMore(List<MessageEntity> entities, MessageEntity lastMsg) {
//        if (entities == null || entities.isEmpty() || entities.size() <= 1) {
//            //Toast.makeText(requireActivity(), "已經到底了", Toast.LENGTH_SHORT).show();
//            stopRefresh();
//            return;
//        }
//        currentDate = messageTimeLineFormat.format(776188800);
//        List<MessageEntity> originList = entities;
//        entities.addAll(mainMessageData);
//        mainMessageData.clear();
//        for (MessageEntity msg : entities) {
//            if (this.mainMessageData.contains(msg) && msg.getSenderId() != null) {
//                continue;
//            }
//
//            String date = messageTimeLineFormat.format(msg.getSendTime());
//            if (!date.equals(currentDate)) {
//                currentDate = date;
//                long dayBegin = TimeUtil.INSTANCE.getDayBegin(msg.getSendTime());
//                MessageEntity timeMessage = new MessageEntity.Builder()
//                        .id(Tools.generateTimeMessageId(dayBegin))
//                        .roomId(chatRoom.getId())
//                        .status(MessageStatus.SUCCESS)
//                        .sourceType(SourceType.SYSTEM)
//                        .content(new UndefContent("TIME_LINE").toStringContent())
//                        .sendTime(dayBegin)
//                        .build();
//                timeMessage.setSendTime(dayBegin);
//                if (!this.mainMessageData.contains(timeMessage)) {
//                    mainMessageData.add(timeMessage);
//                }
//            }
//
//            if (msg.getSenderId() != null) {
//                mainMessageData.add(msg);
//                mainMessageData.indexOf(msg);
//            }
//        }
//        final int index = this.mainMessageData.indexOf(lastMsg);
//
//        if (index <= 0) {
//            stopRefresh();
//            return;
//        }
//        stopRefresh();
//        presenter.checkFacebookReplyType(originList);
//        new Handler(Looper.getMainLooper()).post(() -> {
//            binding.messageRV.refreshToPosition(index - 1);
//        });
//
//        if(binding.scopeSearch.getVisibility() == View.VISIBLE) {
//            chatViewModel.loadMoreBySearch(keyWord, mainMessageData);
//        }
//    }
//
//    @Override
//    public Context getCtx() {
//        return getContext();
//    }
//
//    @Override
//    public void showToast(int resId) {
//        Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void updateMsgStatus(String messageId, MessageStatus status) {
//        int index = this.mainMessageData.indexOf(new MessageEntity.Builder().id(messageId).build());
////        int index = this.messageIds.indexOf(messageId);
//        if (index < 0) {
//            return;
//        }
//        mainMessageData.get(index).setStatus(status);
//        refreshListView();
//    }
//
//    /**
//     * 更新進度條UI
//     */
//    @Override
//    public void updateMsgProgress(String messageId, int progress) {
//        int index = this.mainMessageData.indexOf(new MessageEntity.Builder().id(messageId).build());
//        if (index < 0) {
//            return;
//        }
//
//        MessageEntity message = mainMessageData.get(index);
//        if (MessageType.IMAGE_or_VIDEO_or_FILE.contains(message.getType())) {
//            IMessageContent content = message.content();
//            if (content instanceof ImageContent) {
//                ((ImageContent) content).setProgress(progress + "");
//            }
//            if (content instanceof FileContent) {
//                ((FileContent) content).setProgress(progress + "");
//            }
//            if (content instanceof VideoContent) {
//                ((VideoContent) content).setProgress(progress + "");
//            }
//
//            message.setContent(content.toStringContent());
//            binding.messageRV.refreshData(index, message);
//        }
//    }
//
//    @Override
//    public void updateMsgStatus(String messageId, int sendNum, long sendTime) {
////        int index = this.messageIds.indexOf(messageId);
//        int index = this.mainMessageData.indexOf(new MessageEntity.Builder().id(messageId).build());
//        if (index < 0) {
//            return;
//        }
//
//        MessageEntity iMessage = mainMessageData.get(index);
//        if (sendNum < 0) {
//            iMessage.setStatus(MessageStatus.FAILED);
//        } else {
//            iMessage.setStatus(MessageStatus.SUCCESS);
//        }
//        iMessage.setSendNum(sendNum);
//        iMessage.setSendTime(sendTime);
//        displayMainMessage(false, false, false, iMessage, false);
////        refreshListView();
//    }
//
//    @Override
//    public void updateMsgNotice(String messageId, int receivedNum, int readNum, int sendNum) {
//        int index = this.mainMessageData.indexOf(new MessageEntity.Builder().id(messageId).build());
//        if (index < 0) {
//            return;
//        }
//        MessageEntity message = mainMessageData.get(index);
//        int oldReadNum = message.getReadedNum();
//        int oldReceivedNum = message.getReceivedNum();
//        int oldSendNum = message.getSendNum();
//
//        if (receivedNum > oldReceivedNum) {
//            message.setReceivedNum(receivedNum);
//            DBManager.getInstance().updateReceivedNum(messageId, receivedNum);
//        }
//
//        if (readNum > oldReadNum) {
//            message.setReadedNum(readNum);
//            DBManager.getInstance().updateReadNum(messageId, readNum);
//        }
//        if (sendNum > oldSendNum) {
//            message.setSendNum(sendNum);
//            MessageReference.save(message.getRoomId(), message);
//        }
//        if (chatRoom.getType().equals(ChatRoomType.group) || chatRoom.getType().equals(ChatRoomType.discuss)) {
//            message.setStatus(MessageStatus.SUCCESS);
//            DBManager.getInstance().updateMessageStatus(messageId, MessageStatus.SUCCESS);
//        } else {
//            if (readNum > 0) {
//                message.setStatus(MessageStatus.READ);
//                DBManager.getInstance().updateMessageStatus(messageId, MessageStatus.READ);
//            } else if (readNum == 0 && receivedNum > 0) {
//                message.setStatus(RECEIVED);
//                DBManager.getInstance().updateMessageStatus(messageId, RECEIVED);
//            } else {
//                message.setStatus(MessageStatus.SUCCESS);
//                DBManager.getInstance().updateMessageStatus(messageId, MessageStatus.SUCCESS);
//            }
//        }
//        refreshListView();
//    }
//
//    @Override
//    public void deleteMessage(String messageId) {
//        int index = mainMessageData.indexOf(new MessageEntity.Builder().id(messageId).build());
//        if (index > -1) {
//            MessageEntity entity = mainMessageData.get(index);
//            mainMessageData.remove(index);
//            MessageEntity lastMessageEntity = MessageReference.findMessageByRoomIdAndStatusAndLimitOne(null, chatRoom.getId(), MessageStatus.getValidStatus(), MessageReference.Sort.DESC);
//            chatRoom.setLastMessage(lastMessageEntity);
//            refreshListView();
//        }
//    }
//    /*---- 主題聊天室相關 ---------------------------------------------------------------------------------------------------------------------------------------------------*/
//
//    /**
//     * According to the current incoming theme Id query the current data belongs to the theme-related message
//     */
//    public List<MessageEntity> findThemeMessageListByThemeId(String themeId) {
//        Iterator<MessageEntity> iterator = this.mainMessageData.iterator();
//        List<MessageEntity> list = Lists.newArrayList();
//        while (iterator.hasNext()) {
//            MessageEntity message = iterator.next();
//            if ((themeId.equals(message.getId()) || themeId.equals(message.getThemeId())) && !MessageFlag.RETRACT.equals(message.getFlag())) {
//                list.add(message);
//            }
//        }
//        return list;
//    }
//
//    /**
//     * 顯示主題聊天室
//     */
//    private void showThemeView(String themeId) {
//        if (binding.searchBottomBar.getVisibility() == View.VISIBLE) {
//            return;
//        }
//
//        MessageEntity themeMessage = MessageReference.findById(themeId);
//        if (themeMessage.getFrom() == ChannelType.FB && themeMessage.getType() == MessageType.TEMPLATE) return;
//
//        this.binding.expandIV.setVisibility(View.GONE);
//        this.binding.themeMRV.clearData();
//        this.binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0d / 3.0d));
//        List<MessageEntity> themeMessages = findThemeMessageListByThemeId(themeId);
//        for (MessageEntity message : themeMessages) {
//            displayThemeMessage(themeId, false, message);
//        }
//        this.binding.themeMRV.setVisibility(View.VISIBLE);
//        this.isThemeOpen = true;
//        this.themeId = themeId;
//        this.binding.themeMRV.post(() -> {
//            int maxHeight = setupDefaultThemeHeight(2.0d / 3.0d);
//            int height = binding.themeMRV.getHeight();
//            if (maxHeight == height) {
//                this.binding.expandIV.setVisibility(View.VISIBLE);
//            } else {
//                this.binding.expandIV.setVisibility(View.GONE);
//            }
//        });
//    }
//
//    /**
//     * 顯示主題訊息
//     */
//    @Override
//    public void displayThemeMessage(String themeId, boolean isSendMsgDisplay, MessageEntity message) {
//        this.binding.themeMRV.setThemeId(themeId).setData(message).refreshToBottom();
//    }
//
//    /**
//     * 取得主題訊息
//     */
//    @Override
//    public MessageEntity getThemeMessage() {
//        if (isThemeOpen()) {
//            return this.binding.themeMRV.getThemeData();
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * 如果主題訊息存在，取得最有一筆
//     */
//    @Override
//    public MessageEntity getNearMessage() {
//        if (isThemeOpen()) {
//            return this.binding.themeMRV.getNearData();
//        } else {
//            return null;
//        }
//    }
//
//    @Override
//    public boolean isThemeOpen() {
//        return this.isThemeOpen;
//    }
//
//    @Override
//    public String getChildRoomId() {
////        if (this.binding.lyChildChat.getVisibility() == View.VISIBLE) {
////            return this.binding.lyChildChat.getRoomId();
////        } else {
//        return "";
////        }
//    }
//
//    @Override
//    public boolean isChildRoomOpen() {
////        return this.binding.lyChildChat.getVisibility() == View.VISIBLE;
//        return false;
//    }
//
//    @Override
//    public void displayChildRoomMessage(String roomId, MessageEntity message) {
////        if (this.binding.lyChildChat.getVisibility() == View.VISIBLE) {
////            this.binding.lyChildChat.displayMessage(true, roomId, message);
////        }
//    }
//
//    //子聊天室 根據設定是否自動關閉
//    @Override
//    public void setThemeOpen(boolean isThemeOpen) {
//        if (this.isThemeOpen) {
//            if (TokenPref.getInstance(requireContext()).isAutoCloseSubChat()) {
//                doThemeCloseAction();
//                this.isThemeOpen = isThemeOpen;
//            } else {
//                executeReply(binding.themeMRV.getNearData());
//            }
//        }
//    }
//
//    @Override
//    public void displayLogos(List<Integer> logoBeans) {
//
//    }
//
//    @Override
//    public void onMediaClick(int i) {
//        if (i == R.drawable.plugin_file) {
//            this.IS_ACTIVITY_FOR_RESULT = true;
//            startActivityForResult(new Intent(getActivity(), FileExplorerActivity.class), RESULT_FILE);
//        }
//        getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_CODE:
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
////                    Toast.makeText(getActivity(), R.string.granted, Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(getActivity(), R.string.alert_permission_denied, Toast.LENGTH_SHORT).show();
//                }
//                break;
//        }
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 100 && resultCode == 101) {
//            mIsBlock = data.getBooleanExtra("isBlock", false);
//            if (chatRoom.getType().equals(ChatRoomType.friend)) {
//                binding.chatKeyboardLayout.setIsBlock(mIsBlock);
//            }
//        }
//        if (resultCode == RESULT_OK) {
//            switch (requestCode) {
//                case REQUEST_VIDEO:
//                    IVideoSize size = new VideoSizeFromVideoFile(videoFile.getAbsolutePath());
//                    presenter.sendVideo(size, isFacebookReplyPublic);
//                    break;
//                case REQUEST_CAMERA:
//                    int degree = BitmapKit.readPictureDegree(photoFile.getAbsolutePath());
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inJustDecodeBounds = true;
//                    options.inSampleSize = 8;           //把原圖按1/4的比例壓縮
//                    options.inJustDecodeBounds = false; // 壓縮完後便可以將inJustDecodeBounds設定為false
//                    Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
//                    Bitmap rotationBitmap = BitmapKit.rotationImage(degree, bitmap);
//                    presenter.sendImage(photoFile.getName(), photoFile.getAbsolutePath(), rotationBitmap, isFacebookReplyPublic);
//                    break;
//                case SHARE_SCREENSHOTS_RESULT_CODE:
//                    String screenshotsPath = getArguments().getString(BundleKey.FILE_PATH.key());
//                    String[] roomIds = data.getStringArrayExtra(Constant.ACTIVITY_RESULT);
//                    ChatRoomService.getInstance().checkRoomEntities(getContext(), roomIds, new ServiceCallBack<List<String>, Enum>() {
//                        @Override
//                        public void complete(List<String> strings, Enum anEnum) {
//                            presenter.sendScreenshotsImageToRooms(Lists.newArrayList(roomIds), screenshotsPath);
//                        }
//
//                        @Override
//                        public void error(String message) {
//
//                        }
//                    });
//                    break;
//                case MEDIA_SELECTOR_REQUEST_CODE:
//                    getArguments().putBundle("PREVIEW", data.getExtras());
//                    break;
////                case RESULT_VIDEO:
////                    break;
//                case RESULT_PIC:
//                    String name = data.getStringExtra(BundleKey.RESULT_PIC_URI.key());
//                    String thumbnailName = data.getStringExtra(BundleKey.RESULT_PIC_SMALL_URI.key());
////                    showSendVideoProgress("圖片發送中");
//                    presenter.sendImage(name, thumbnailName, isFacebookReplyPublic);
//                    break;
////                case RESULT_IMAGE_TEXT:
////                    String title = data.getStringExtra(Constant.RESULT_IMAGE_TEXT_TITLE);
////                    String text = data.getStringExtra(Constant.RESULT_IMAGE_TEXT_CONTENT);
////                    String coverName = data.getStringExtra(Constant.RESULT_IMAGE_TEXT_COVER_NAME);
////                    presenter.sendImageText(title, text, coverName);
////                    break;
//                case RESULT_FILE:
//                    List<String> filePaths = data.getStringArrayListExtra(BundleKey.FILE_PATH_LIST.key());
//                    if (filePaths != null) {
//                        sendFileSize = filePaths.size();
//                        isSendSingleFile = sendFileSize == 1;
//                        for (String filePath : filePaths) {
//                            int fileType = FileUtil.getFileType(filePath);
//                            switch (fileType) {
//                                case Global.FileType_Png:
//                                case Global.FileType_Jpg:
//                                case Global.FileType_jpeg:
//                                case Global.FileType_bmp:
//                                    String[] path = PictureParse.parsePath(getCtx(), filePath);
////                                    showSendVideoProgress("圖片發送中");
//                                    presenter.sendImage(path[0], path[1], isFacebookReplyPublic);
//                                    break;
//                                case Global.FileType_gif:
//                                    BitmapBean bitmapBean = PictureParse.parseGifPath(getCtx(),
//                                            filePath);
//                                    presenter.sendGifImg(bitmapBean.url, filePath, bitmapBean.width,
//                                            bitmapBean.height, isFacebookReplyPublic);
//                                    break;
//                                case Global.FileType_mov:
//                                case Global.FileType_mp4:
//                                case Global.FileType_rmvb:
//                                case Global.FileType_avi:
//                                    IVideoSize iVideoSize = new VideoSizeFromVideoFile(filePath);
//                                    if (iVideoSize.size() == 0) {
//                                        presenter.sendFile(filePath, isFacebookReplyPublic);
//                                    } else {
//                                        presenter.sendVideo(iVideoSize, isFacebookReplyPublic);
//                                    }
//                                    break;
//                                default:
//                                    presenter.sendFile(filePath, isFacebookReplyPublic);
//                                    break;
//                            }
//                        }
//                    }
//                    break;
////                case PictureConfig.CHOOSE_REQUEST:
////                    // 图片选择
////                    List<LocalMedia> mLocalMedias = PictureSelector.obtainMultipleResult(data);
////                    for (LocalMedia localMedia : mLocalMedias) {
////                        String pictureType = localMedia.getPictureType();
////                        if (Strings.isNullOrEmpty(pictureType)) {
////                            return;
////                        }
////                        if ("image/gif".equalsIgnoreCase(pictureType)) {
////                            BitmapBean bitmapBean = PictureParse.parseGifPath(getCtx(), localMedia.getPath());
////                            presenter.sendGifImg(bitmapBean.url, localMedia.getPath(), bitmapBean.width, bitmapBean.height);
////                        } else if ("video/mp4".equals(pictureType)) {
////                            IVideoSize iVideoSize = new VideoSizeFromVideoFile(localMedia.getPath());
////                            presenter.sendVideo(iVideoSize);
////                            Log.i(TAG, "");
////                        } else if ("image/png".equalsIgnoreCase(pictureType) || "image/jpeg".equalsIgnoreCase(pictureType)) {
////                            String[] path = PictureParse.parsePath(getCtx(), localMedia.getPath());
////                            presenter.sendImage(path[0], path[1]);
////                        }
////                    }
////                    break;
//            }
//        }
//    }
//
//    @Override
//    public void onRefresh() {
//        if (chatRoom != null) {
//            if (!mainMessageData.isEmpty()) {
//                MessageEntity msg = getFirstMsgId();
//                presenter.refreshMoreMsg(msg);
//            } else {
//                presenter.refreshMoreMsg(null);
//                stopRefresh();
//            }
//        }
//    }
//
//    @Nullable
//    @Override
//    public MessageEntity getFirstMsgId() {
//        MessageEntity msg = null;
//        for (MessageEntity message : mainMessageData) {
////            通过sendId区分自定义日期消息和server消息
//            if (message.getSenderId() != null) {
//                msg = message;
//                break;
//            }
//        }
//        return msg;
//    }
//
//    @Override
//    public void cancel() {
//        getActivity().finish();
//    }
//
//    @Override
//    public void onCleanMsgs() {
//        mainMessageData.clear();
////        this.messageIds.clear();
////        chatRoom.setContent("");
//        refreshListView();
//
//    }
//
//    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
//        @Override
//        protected void onNoDoubleClick(View v) {
//            if (v.equals(binding.difbDown)) {
//                doScrollToBottomAction(v);
//            } else if (v.equals(binding.ivConsult)) {
//                doConsultListAction(v);
//            } else if (v.equals(binding.expandIV)) {
//                doThemeExpandAction(v);
//            } else if (v.equals(binding.themeCloseIV)) {
//                doThemeCloseAction();
//            } else if (v.equals(binding.floatingLastMessageTV)) {
//                doFloatingLastMessageClickAction(v);
//            } else if (v.equals(binding.ivRobotRecord) || v.equals(binding.ivRobotRecord2)) {
//                showRobotChatRecord(robotChatRecord);
//            }
//        }
//    };
//
//    private void init() {
//        if (chatRoom != null) App.getInstance().chatRoomId = chatRoom.getId();
//        binding.difbDown.setOnClickListener(clickListener);
//        binding.ivConsult.setOnClickListener(clickListener);
//        binding.expandIV.setOnClickListener(clickListener);
//        binding.themeCloseIV.setOnClickListener(clickListener);
//        //點擊子聊天室時可以關閉且不會擋到滑動 recyclerview
//        binding.themeMRV.setOnTouchListener((view, motionEvent) -> {
//            switch (motionEvent.getAction()) {
//                case MotionEvent.ACTION_SCROLL:
//                case MotionEvent.ACTION_DOWN:
//                    subRoomOnTouchDownTime = System.currentTimeMillis();
//                    break;
//                case MotionEvent.ACTION_UP:
//                    long upTime = System.currentTimeMillis();
//                    if (upTime - subRoomOnTouchDownTime < 70) {
//                        doThemeCloseAction();
//                    }
//                    break;
//            }
//            return false;
//        });
//        binding.floatingLastMessageTV.setOnClickListener(clickListener);
//        binding.ivRobotRecord.setOnClickListener(clickListener);
//        binding.ivRobotRecord2.setOnClickListener(clickListener);
//    }
//
//    /**
//     * 滑動到最底部控件
//     */
//    public void doScrollToBottomAction(View view) {
//        if (binding.messageRV.getScrollState() != 0) {
//            return;
//        }
//        int bottomDistance = UiHelper.dip2px(requireActivity(), 10);
//        binding.difbDown.animate().translationY(binding.difbDown.getHeight() + bottomDistance).setInterpolator(new LinearInterpolator()).start();
//        binding.messageRV.scrollToPosition(binding.messageRV.getAdapter().getItemCount() - 1);
//        if (binding.floatingLastMessageTV.getVisibility() == View.VISIBLE) {
//            binding.floatingLastMessageTV.setVisibility(View.GONE);
//        }
//    }
//
//    private void doAppointStatus(String roomId) {
//        ChatServiceNumberService.findAppoint(requireActivity(), roomId, new ServiceCallBack<ServiceNumberChatroomAgentServicedRequest.Resp, ChatServiceNumberService.ServicedType>() {
//            @Override
//            public void complete(ServiceNumberChatroomAgentServicedRequest.Resp resp, ChatServiceNumberService.ServicedType servicedType) {
//                if (resp != null) {
//                    appointResp = new FromAppointRequest.Resp(resp.getServiceNumberStatus(), resp.getLastFrom(), resp.getOtherFroms());
//                    setupAppointStatus(appointResp);
//                } else {
//                    setupAppointStatus(null);
//                }
//            }
//
//            @Override
//            public void error(String message) {
//
//            }
//        });
//    }
//
//
//    public void doServiceNumberServicedStatus(ChatRoomEntity chatRoom) {
//        doServiceNumberServicedStatus(chatRoom, null);
//        doAppointStatus(chatRoom.getId());
//    }
//
//    /**
//     * Obtain multi-channel binding information and whether it is online
//     * Judging Room. Owner is Self, then don’t take a sharp action
//     */
//    @Override
//    public void doServiceNumberServicedStatus(ChatRoomEntity chatRoom, AgentSnatchCallback agentSnatchCallback) {
//        if (chatRoom == null || getUserId().equals(chatRoom.getOwnerId())) return;
//
//        ChatServiceNumberService.findServicedStatusAndAppoint(requireActivity(), chatRoom.getId(), new ServiceCallBack<ServiceNumberChatroomAgentServicedRequest.Resp, ChatServiceNumberService.ServicedType>() {
//            @Override
//            public void complete(ServiceNumberChatroomAgentServicedRequest.Resp resp, ChatServiceNumberService.ServicedType servicedType) {
//                switch (servicedType) {
//                    case AGENT_SERVICED:
////                        if (getActivity() instanceof ChatActivity) {
////                            ((ChatActivity) getActivity()).setChannelIconVisibility(R.drawable.ce_icon, ServiceNumberStatus.ON_LINE);
////                        }
//                        chatRoom.setServiceNumberStatus(resp.getServiceNumberStatus());
//                        chatRoom.setServiceNumberAgentId(resp.getServiceNumberAgentId());
//                        chatRoom.setIdentityId(resp.getIdentityId());
//
//                        // 如果用戶在線，且沒人接線
//                        if (ServiceNumberStatus.ON_LINE.equals(resp.getServiceNumberStatus()) && Strings.isNullOrEmpty(resp.getServiceNumberAgentId())) {
//                            doServiceNumberStart(resp.getRoomId());
//                        } else {
//                            setupBottomServicedStatus(resp.getServiceNumberStatus(), chatRoom.getServiceNumberType(), resp);
//                        }
//
//                        if (resp.getAiConsultArray() != null && resp.getAiConsultArray().size() > 0) {
//                            chatRoom.setAiConsultId(resp.getAiConsultArray().get(0).getConsultId());
//                        } else {
//                            chatRoom.setAiConsultId("");
//                        }
//
//                        setRobotChatRecord(resp);
//
//                        // 諮詢聊天室
//                        chatViewModel.setConsultRoom(resp.getAiConsultArray(), resp.getActiveServiceConsultArray(), chatRoom.getId(), chatRoom.getServiceNumberId());
//                        break;
//                    case APPOINT:
//                        if (resp != null) {
//                            appointResp = new FromAppointRequest.Resp(resp.getServiceNumberStatus(), resp.getLastFrom(), resp.getOtherFroms());
//                            setupAppointStatus(appointResp);
//                        } else {
//                            setupAppointStatus(null);
//                        }
//                        break;
//                    case ROBOT_SERVICED:
//                        if (resp != null) {
//                            binding.robotChatMessage.loadUrl(resp.getRobotChatRecordLink());
//                        }
//                        break;
//                    case ROBOT_SERVICE_STOP:
//                        if (chatRoom.getServiceNumberStatus().equals(ServiceNumberStatus.ON_LINE)) {
//                            if (getActivity() instanceof ChatActivity) {
//                                getActivity().finish();
//                            }
//                        }
//                        break;
//                }
//
//                if (agentSnatchCallback != null) agentSnatchCallback.onSnatchSuccess();
//            }
//
//            @Override
//            public void error(String message) {
//                setupAppointStatus(null);
//            }
//        });
//    }
//
//    private void setupAppointStatus(FromAppointRequest.Resp appointResp) {
//        if (!isAdded()) return;
//        if (appointResp == null) {
////            if (getActivity() instanceof ChatActivity) {
////                ((ChatActivity) getActivity()).setChannelIconVisibility(R.drawable.ce_icon, ServiceNumberStatus.ON_LINE);
////            }
////            if (ServiceNumberType.NORMAL.equals(chatRoom.getServiceNumberType())) {
//////                agentActionTV.setVisibility(!isPerson ? View.GONE : View.VISIBLE);
////            } else if (ServiceNumberType.PROFESSIONAL.equals(chatRoom.getServiceNumberType()) && !Strings.isNullOrEmpty(chatRoom.getServiceNumberAgentId()) && getUserId().equals(chatRoom.getServiceNumberAgentId())) {
//////                agentActionTV.setVisibility(!isPerson ? View.GONE : View.VISIBLE);
////            }
//            return;
//        }
//
//        ChannelType lastFrom = appointResp.getLastFrom();
//        if (appointResp.getOtherFroms() == null) {
//            appointResp.setOtherFroms(Sets.newHashSet());
//        }
//        appointResp.getOtherFroms().add(lastFrom);
//
//        if (ServiceNumberStatus.ON_LINE.equals(appointResp.getStatus())) {
//            if (ServiceNumberType.NORMAL.equals(chatRoom.getServiceNumberType())) {
//            } else {
//                if (ServiceNumberType.PROFESSIONAL.equals(chatRoom.getServiceNumberType()) && !Strings.isNullOrEmpty(chatRoom.getServiceNumberAgentId())) {
//                    getUserId();
//                }
//            }
//        } else if (ServiceNumberStatus.OFF_LINE.equals(appointResp.getStatus()) || ServiceNumberStatus.TIME_OUT.equals(appointResp.getStatus())) {
//            String selfUserId = TokenPref.getInstance(requireContext()).getUserId();
//            // 商務號擁者停止服務
//            if (ServiceNumberType.BOSS.equals(chatRoom.getServiceNumberType()) && selfUserId.equals(chatRoom.getServiceNumberOwnerId())) {
//                if (chatRoom.isServiceNumberOwnerStop()) {
//                    setApponintOfflineStatus(lastFrom);
//                }
//            } else {
//                setApponintOfflineStatus(lastFrom);
//            }
//        }
//
//        ChannelType lastChannel = appointResp.getLastFrom();
//        if (lastChannel != null) {
//            @DrawableRes int resId = 0;
//            switch (lastChannel) {
//                case LINE:
//                    resId = R.drawable.ic_line;
//                    break;
//                case FB:
//                    resId = R.drawable.ic_fb;
//                    break;
//                case AILE_WEB_CHAT:
//                case QBI:
//                    resId = R.drawable.qbi_icon;
//                    break;
//                case WEICHAT:
//                    resId = R.drawable.wechat_icon;
//                    break;
//                case GOOGLE:
//                    resId = R.drawable.ic_google_message;
//                    break;
//                case IG:
//                    resId = R.drawable.ic_ig;
//                    break;
//                default:
//                    resId = R.drawable.ce_icon;
//                    break;
//            }
//
//            if (getActivity() instanceof ChatActivity) {
//                ((ChatActivity) getActivity()).setChannelIconVisibility(resId, appointResp.getStatus());
//            }
//        }
//    }
//
//    @Override
//    public void setFacebookKeyboard() {
//        binding.chatKeyboardLayout.hideRecordVideoFeature();
//    }
//
//    @Override
//    public void updateFacebookStatus(MessageEntity message) {
//        binding.messageRV.notifyChange(message);
//        if (isThemeOpen) {
//            binding.themeMRV.notifyChange(message);
//        }
//    }
//
//    @Override
//    public void moveToFacebookReplyMessage(int position) {
//        binding.messageRV.post(() -> {
//            ((LinearLayoutManager) binding.messageRV.getLayoutManager()).scrollToPositionWithOffset(position, 0);
//            binding.messageRV.getAdapter().notifyItemChanged(position);
//        });
//    }
//
//    private void setApponintOfflineStatus(ChannelType lastFrom) {
//        binding.civServicedAgentAvatar.setVisibility(View.GONE);
//        binding.ivServicedStatus.setVisibility(View.GONE);
//        binding.tvServicedDuration.setVisibility(View.GONE);
//        clearProvisionalMember();
//        if (ChannelType.QBI.equals(lastFrom)) {
//            binding.chatKeyboardLayout.setChatDisable("qbi通路無法離線回覆");
//        }
//    }
//
//    public void doChannelChangeAction() {
//        if (appointResp != null) {
//            if (ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(appointResp.getStatus())) {
//                List<ChannelType> availableChannels = Lists.newArrayList(appointResp.getOtherFroms());
//                availableChannels.remove(appointResp.getLastFrom());
//
//                if (availableChannels.size() == 0) {
//                    Toast.makeText(requireActivity(), "無其它渠道可切換", Toast.LENGTH_SHORT).show();
//                } else {
//                    List<AlertView.ImageText> imageTests = Lists.newArrayList();
//                    Collections.sort(availableChannels, (s1, s2) -> s1.getIndex() - s2.getIndex());
//                    for (ChannelType t : availableChannels) {
//                        switch (t) {
//                            case WEICHAT:
//                                imageTests.add(new AlertView.ImageText.Builder().code(ChannelType.WEICHAT.getValue()).text("We Chat").res(R.drawable.wechat_icon).build());
//                                break;
//                            case AILE_WEB_CHAT:
//                                imageTests.add(new AlertView.ImageText.Builder().code(ChannelType.AILE_WEB_CHAT.getValue()).text("Aile Web Chat").res(R.drawable.qbi_icon).build());
//                                break;
//                            case QBI:
//                                imageTests.add(new AlertView.ImageText.Builder().code(ChannelType.QBI.getValue()).text("Qbi").res(R.drawable.qbi_icon).build());
//                                break;
//                            case FB:
//                                imageTests.add(new AlertView.ImageText.Builder().code(ChannelType.FB.getValue()).text("Message").res(R.drawable.ic_fb).build());
//                                break;
//                            case LINE:
//                                imageTests.add(new AlertView.ImageText.Builder().code(ChannelType.LINE.getValue()).text("Line").res(R.drawable.ic_line).build());
//                                break;
//                            case GOOGLE:
//                                imageTests.add(new AlertView.ImageText.Builder().code(ChannelType.GOOGLE.getValue()).text("Google").res(R.drawable.ic_google_message).build());
//                                break;
//                            case IG:
//                                imageTests.add(new AlertView.ImageText.Builder().code(ChannelType.IG.getValue()).text("IG").res(R.drawable.ic_ig).build());
//                                break;
//                            default:
//                                imageTests.add(new AlertView.ImageText.Builder().code(ChannelType.CE.getValue()).text("Aile").res(R.drawable.ce_icon).build());
//                                break;
//                        }
//                    }
//                    new AlertView.Builder()
//                            .setContext(requireActivity())
//                            .setStyle(AlertView.Style.ImageTextAlert)
//                            .setTitle("選擇回覆渠道")
//                            .setImageTexts(imageTests, true)
//                            .setCancelText("取消")
//                            .setOnItemClickListener((o, position) -> {
//                                if (position >= 0) {
//                                    AlertView.ImageText data = imageTests.get(position);
//                                    String code = data.getCode();
//                                    ChannelType msgFrom = ChannelType.of(code);
//                                    doSwitchChannel(msgFrom);
//                                }
//                            })
//                            .build()
//                            .setCancelable(true)
//                            .show();
//
//                }
//            }
//        }
//    }
//
//    boolean isAlreadyCount = false;
//    private void countServiceDuration() {
//        if (!isAlreadyCount) {
//            isAlreadyCount = true;
//            binding.tvServicedDuration.post(servicedDuration);
//        }
//    }
//
//    Runnable servicedDuration = new Runnable() {
//        @Override
//        public void run() {
//            long startTime = (long) binding.tvServicedDuration.getTag();
//            if (servicedDurationTime != 0L) {
//                long duration = servicedDurationTime - startTime;
//                binding.tvServicedDuration.setText(DateTimeHelper.convertSecondsToHMmSs(duration));
//                servicedDurationTime += 1000L;
//                binding.tvServicedDuration.postDelayed(servicedDuration, 1000L);
//            }
//        }
//    };
//
//    private Dialog transferDialog;
//
//    private Dialog createTransferDialog(TransferDialogBuilder.OnSubmitListener onSubmitListener) {
//        return new TransferDialogBuilder(getActivity())
//                .setOnSubmitListener(onSubmitListener).create();
//    }
//
//    private void disableInputBarForChannelIsOffline(ServiceNumberChatroomAgentServicedRequest.Resp resp) {
//        if (resp.getLastFrom() != null) {
//            if (resp.getLastFrom().equals(ChannelType.AILE_WEB_CHAT) &&
//                    !resp.getServiceNumberStatus().equals(ServiceNumberStatus.ON_LINE)) {
//                binding.chatKeyboardLayout.setKeyboardDisabled(true, getString(R.string.text_boss_exit_room_cannot_reply));
//            } else if (resp.getLastFrom().equals(ChannelType.FB) &&
//                    !resp.getServiceNumberStatus().equals(ServiceNumberStatus.ON_LINE) &&
//                    appointResp.isLastMessageTimeOut()) {
//                binding.chatKeyboardLayout.setKeyboardDisabled(true, getString(R.string.text_exceeded_fb_messenger_24_hour_time_limit));
//            } else
//                binding.chatKeyboardLayout.setKeyboardDisabled(false, "");
//        } else
//            binding.chatKeyboardLayout.setKeyboardDisabled(false, "");
//    }
//
//    private void setRobotChatRecord(ServiceNumberChatroomAgentServicedRequest.Resp resp) {
//        //沒有啟用機器人服務 所以不顯示按鈕
//        //ivRobotRecord 是諮詢中要顯示的 icon
//        //ivRobotRecord2 是結束服務後要顯示的 icon
//        if (resp.getRobotChatRecordLink() == null || resp.getRobotChatRecordLink().isEmpty()) {
//            binding.ivRobotRecord2.setVisibility(View.GONE);
//            binding.ivRobotRecord.setVisibility(View.GONE);
//        } else {
//            if (ServiceNumberType.BOSS.equals(chatRoom.getServiceNumberType())) {
//                if (ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(resp.getServiceNumberStatus())) {
//                    if (chatRoom.getServiceNumberOwnerId().equals(getUserId())) {
//                        if (resp.isServiceNumberOwnerStop()) {
//                            binding.ivRobotRecord2.setVisibility(View.VISIBLE);
//                            binding.ivRobotRecord.setVisibility(View.GONE);
//                        } else {
//                            binding.ivRobotRecord2.setVisibility(View.GONE);
//                            binding.ivRobotRecord.setVisibility(View.VISIBLE);
//                        }
//                    }
//                } else {
//                    binding.ivRobotRecord2.setVisibility(View.GONE);
//                    binding.ivRobotRecord.setVisibility(View.VISIBLE);
//                }
//            } else {
//                if (ServiceNumberStatus.OFF_LINE_or_TIME_OUT.contains(resp.getServiceNumberStatus())) {
//                    binding.ivRobotRecord2.setVisibility(View.VISIBLE);
//                    binding.ivRobotRecord.setVisibility(View.GONE);
//                } else if (ServiceNumberStatus.ON_LINE.equals(resp.getServiceNumberStatus())) {
//                    binding.ivRobotRecord2.setVisibility(View.GONE);
//                    binding.ivRobotRecord.setVisibility(View.VISIBLE);
//                }
//            }
//        }
//    }
//
//    //設定服務號的控制bar, 換手按鈕
//    private void setupBottomServicedStatus(ServiceNumberStatus status, ServiceNumberType type, ServiceNumberChatroomAgentServicedRequest.Resp resp) {
//        // 如果服務號 timeout 會出現 icon 重疊
//        if (status == ServiceNumberStatus.TIME_OUT) {
//            // 如果是商務號擁有者 還需要擁有者停止
//            if (!resp.isServiceNumberOwnerStop() && !type.equals(ServiceNumberType.BOSS)) {
//                return;
//            }
//        }
//
//        binding.civServicedAgentAvatar.setOnClickListener(null);
//        binding.ivTransfer.setOnClickListener(null);
//        binding.ivTransfer.setVisibility(View.GONE);
//        binding.tvServicedDuration.setTag(0L);
//        binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg);
//        if (resp == null) {
//            return;
//        }
//
//        robotChatRecord = resp.getRobotChatRecordLink();
//        disableInputBarForChannelIsOffline(resp);
//
//        String selfId = getUserId();
//        String serviceNumberAgentId = resp.getServiceNumberAgentId();
//
//        if (ServiceNumberStatus.ON_LINE.equals(status)) {
//            // 商務號擁有者
//            if ((type.equals(ServiceNumberType.BOSS)
//                    && !resp.isServiceNumberOwnerStop()
//                    && chatRoom.getServiceNumberOwnerId().equals(selfId)) || !isProvisionMember) {
//                //商務號擁有者仍保留狀態直到手動結束
//                Set<String> textTooltips = Sets.newLinkedHashSet();
//                textTooltips.add("prompt_close_serviced");
//                //正在服務中的人員才會出現咨詢功能的圖示
//                /** 2023/11/22 ID1005663
//                 *  調整只有 專業服務號 正在服務中的人員才會出現咨詢功能的圖示
//                 *  其他服務號一直顯示
//                 **/
//                if (ServiceNumberType.PROFESSIONAL.equals(chatRoom.getServiceNumberType())) {
//                    binding.ivConsult.setVisibility(selfId.equals(serviceNumberAgentId) ? View.VISIBLE : View.GONE);
//                } else {
//                    binding.ivConsult.setVisibility(View.VISIBLE);
//                }
//
//                binding.clBottomServicedControl.setVisibility(View.VISIBLE);
//                String agentName = UserProfileReference.findAccountName(null, serviceNumberAgentId);
//                if (agentName == null) agentName = "";
//                String avatarId = UserProfileReference.findAccountAvatarId(null, resp.getServiceNumberAgentId());
//                if (ServiceNumberType.PROFESSIONAL.equals(type)) {
//                    if (resp.getServiceNumberAgentId() != null)
//                        binding.civServicedAgentAvatar.loadAvatarIcon(avatarId, agentName, serviceNumberAgentId);
//                } else {
//                    binding.civServicedAgentAvatar.loadAvatarIcon(chatRoom.getServiceNumberAvatarId(), agentName, serviceNumberAgentId);
//                }
//                binding.civServicedAgentAvatar.setOnClickListener(new NoDoubleClickListener() {
//                    @Override
//                    protected void onNoDoubleClick(View v) {
//                        doAgentStopService(resp);
//                    }
//                });
//
//                if (ServiceNumberType.BOSS.equals(chatRoom.getServiceNumberType())) {
//                    binding.tvServicedDuration.setVisibility(View.GONE);
//                } else {
//                    // 如果是主管號
//                    if (resp.getStartTime() > 0) {
//                        TimeUtil.INSTANCE.getNetworkTime(this);
//                        binding.tvServicedDuration.setTag(resp.getStartTime());
//                        binding.tvServicedDuration.setVisibility(View.VISIBLE);
//                    } else {
//                        binding.tvServicedDuration.setTag(0);
//                        binding.tvServicedDuration.removeCallbacks(servicedDuration);
//                    }
//                }
//
//                if (!ServiceNumberType.PROFESSIONAL.equals(chatRoom.getServiceNumberType())) {
//                    return;
//                }
//
//                if (!Strings.isNullOrEmpty(serviceNumberAgentId) && !selfId.equals(serviceNumberAgentId)) {
//                    binding.ivTransfer.setOnClickListener(null);
//                    binding.ivTransfer.setVisibility(View.GONE);
//                }
//                //強制接手後狀態
//                if (!resp.isTransferFlag() && selfId.equals(serviceNumberAgentId)) {
//                    binding.ivTransfer.setVisibility(View.VISIBLE);
//                    textTooltips.add("prompt_post_transfer");
//                    binding.ivTransfer.setImageResource(R.drawable.icon_serviced_relay_green);
//                    binding.ivTransfer.setOnClickListener(v -> {
////                    if(transferDialog == null){
//                        transferDialog = createTransferDialog(message -> {
//                            binding.ivServicedStatus.setImageResource(R.drawable.circle_blue_bg);
//                            doServiceNumberTransfer(ChatServiceNumberService.ServicedTransferType.TRANSFER, resp, message);
//                            transferDialog.dismiss();
//                            Toast.makeText(requireActivity(), "等待接續服務", Toast.LENGTH_SHORT).show();
//                        });
////                    }
//                        if (!transferDialog.isShowing()) transferDialog.show();
//                    });
//                }
//                //發起換手後的狀態
//                if (resp.isTransferFlag() && selfId.equals(serviceNumberAgentId)) {
//                    binding.ivTransfer.setVisibility(View.VISIBLE);
//                    binding.ivServicedStatus.setImageResource(R.drawable.circle_blue_bg);
////                binding.ivConsult.setVisibility(View.GONE);
//                    binding.ivTransfer.setImageResource(R.drawable.icon_serviced_relay_cancel_green);
//                    binding.ivTransfer.setOnClickListener(v -> {
//                        binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg);
//                        doServiceNumberTransfer(ChatServiceNumberService.ServicedTransferType.TRANSFER_CANCEL, resp, "");
//                    });
//                }
//                //其他成員發起換手的狀態
//                if (resp.isTransferFlag() && !Strings.isNullOrEmpty(serviceNumberAgentId) && !selfId.equals(serviceNumberAgentId)) {
//                    binding.ivTransfer.setVisibility(View.VISIBLE);
//                    textTooltips.add("prompt_handling_transfer");
//                    binding.ivServicedStatus.setImageResource(R.drawable.circle_blue_bg);
//                    binding.ivTransfer.setImageResource(R.drawable.icon_serviced_relay_blue);
//                    binding.ivTransfer.setOnClickListener(v -> {
//                        new AlertView.Builder()
//                                .setContext(getCtx())
//                                .setStyle(AlertView.Style.Alert)
//                                .setMessage(chatRoom.getServiceNumberName() + "正在服務中，您確定要接續成為客戶專屬服務人員嗎?")
//                                .setOthers(new String[]{"取消", "確認"})
//                                .setOnItemClickListener((o, position) -> {
//                                    if (position == 1) {
//                                        binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg);
//                                        doServiceNumberTransfer(ChatServiceNumberService.ServicedTransferType.TRANSFER_COMPLETE, resp, "");
////                                    Toast.makeText(requireActivity(), "您已接替服務", Toast.LENGTH_SHORT).show();
//                                    }
//                                })
//                                .build()
//                                .setCancelable(true)
//                                .show();
//                    });
//                }
//                handleMultipleTextTooltips(Queues.newLinkedBlockingQueue(textTooltips));
//            } else {
//                binding.clBottomServicedControl.setVisibility(View.GONE);
//                binding.ivConsult.setVisibility(View.GONE);
//            }
//        } else {
//            binding.clBottomServicedControl.setVisibility(View.GONE);
//            binding.ivConsult.setVisibility(View.GONE);
//        }
//    }
//
//    private void showRobotChatRecord(String link) {
//        binding.ivIcon.setImageResource(R.drawable.ic_robot_service);
//        binding.tvTitle.setText(getString(R.string.text_robot_charting_record));
//        binding.ivClose.setVisibility(View.VISIBLE);
//        binding.robotChatMessage.loadUrl(link);
//
//        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams)binding.scopeRobotChat.getLayoutParams();
//        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
//        params.height = 0;
//        params.bottomToTop = binding.robotChatMessageGuideline.getId();
//        binding.scopeRobotChat.setLayoutParams(params);
//        binding.scopeRobotChat.invalidate();
//        binding.scopeRobotChat.requestLayout();
//        binding.scopeRobotChat.setVisibility(binding.scopeRobotChat.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
//        KeyboardHelper.hide(getView());
//    }
//
//    private void setAiServices() {
//        if (getActivity() instanceof ChatActivity)
//            ((ChatActivity) getActivity()).showTopMenu(false);
//        if (chatRoom.isWarned()) {
//            // Ai 監控中
//            binding.tvTitle.setText(getString(R.string.service_room_sectioned_monitor_ai_service));
//            binding.ivIcon.setImageResource(R.mipmap.ic_monitor_ai);
//            binding.ivTurnToAiServices.setVisibility(View.VISIBLE);
//            binding.ivTurnToAiServices.setOnClickListener(view -> {
//                UserPref.getInstance(requireActivity()).setServiceRoomIntroChannel(true,
//                        "turn_to_ai_services");
//                showTurnMonitorAiToAiServicingDialog();
//            });
//            Set<String> textTooltips = Sets.newLinkedHashSet();
//            textTooltips.add("turn_to_ai_services");
//            handleMultipleTextTooltips(Queues.newLinkedBlockingQueue(textTooltips));
//        } else {
//            binding.ivTurnToAiServices.setVisibility(View.GONE);
//            binding.tvTitle.setText(getString(R.string.service_room_sectioned_robot_service));
//            binding.ivIcon.setImageResource(R.drawable.ic_robot_service);
//        }
//        binding.scopeRobotChat.setVisibility(View.VISIBLE);
//    }
//
//    private void showTurnMonitorAiToAiServicingDialog() {
//        new AlertView.Builder()
//                .setContext(requireContext())
//                .setStyle(AlertView.Style.Alert)
//                .setMessage(getString(R.string.turn_to_ai_servicing))
//                .setOthers(new String[]{"取消", "確定"})
//                .setOnItemClickListener((o, position) -> {
//                    if (position == 1) {
//                        chatViewModel.cancelAiWarning(chatRoom.getId());
//                    }
//                })
//                .build()
//                .setCancelable(false)
//                .show();
//    }
//
//    private void doServiceNumberTransfer(ChatServiceNumberService.ServicedTransferType type, ServiceNumberChatroomAgentServicedRequest.Resp resp, String reason) {
//        ChatServiceNumberService.servicedTransferHandle(requireActivity(), type, resp.getRoomId(), reason, new ServiceCallBack<String, ChatServiceNumberService.ServicedTransferType>() {
//            @Override
//            public void complete(String s, ChatServiceNumberService.ServicedTransferType type) {
//                switch (type) {
//                    case TRANSFER_SNATCH:
//                        resp.setSnatch(true);
//                        resp.setTransferFlag(true);
//                        resp.setServiceNumberAgentId(getUserId());
//                        break;
//                    case TRANSFER:
//                        resp.setTransferFlag(true);
//                        resp.setSnatch(false);
//                        break;
//                    case TRANSFER_CANCEL:
//                        resp.setTransferFlag(false);
//                        resp.setSnatch(false);
//                        break;
//                    case TRANSFER_COMPLETE:
//                        resp.setTransferFlag(false);
//                        resp.setSnatch(false);
//                        resp.setServiceNumberAgentId(getUserId());
//                        break;
//                }
//                if (resp.istSnatch()) //多了這個判斷是為了修正強制接手後 換手圖示更新錯誤問題
//                    setup();
//                else
//                    setupBottomServicedStatus(resp.getServiceNumberStatus(), chatRoom.getServiceNumberType(), resp);
//            }
//
//            @Override
//            public void error(String message) {
//                Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    //第一次開始服務號會出現的提醒視窗
//    private void handleMultipleTextTooltips(Queue<String> textTooltipQueue) {
//        if (textTooltipQueue.isEmpty() || getActivity() == null) {
//            return;
//        }
//
//        String target = textTooltipQueue.remove();
//        if (!UserPref.getInstance(requireActivity()).isServiceRoomIntroChannel(target)) {
//            View anchorView;
//            String text = "";
//            if (target.equals("prompt_close_serviced")) {
//                anchorView = binding.civServicedAgentAvatar;
//                text = "服務中，您可按此結束服務";
//            } else if (target.equals("prompt_post_transfer")) {
//                anchorView = binding.ivTransfer;
//                text = "接續服務";
//            } else if (target.equals("turn_to_ai_services")) {
//                anchorView = binding.ivTurnToAiServices;
//                text = "按此轉回 AI 服務";
//            } else {
//                anchorView = binding.ivTransfer;
//                text = "等待中";
//            }
//
//            new SimpleTextTooltip.Builder(requireActivity())
//                    .anchorView(anchorView)
//                    .backgroundColor(Color.BLACK)
//                    .arrowColor(Color.BLACK)
//                    .textColor(Color.WHITE)
//                    .textSize(13.0f)
//                    .dismissOnInsideTouch(true)
//                    .dismissOnOutsideTouch(false)
//                    .text(text)
//                    .gravity(Gravity.TOP)
//                    .animated(false)
//                    .arrowHeight(7)
//                    .arrowWidth(10)
//                    .transparentOverlay(true)
//                    .dismissTimer(3000L) // 定時器關閉
//                    .onDismissListener(tooltip -> {
//                        if (!target.equals("turn_to_ai_services")) {
//                            UserPref.getInstance(requireActivity()).setServiceRoomIntroChannel(true,
//                                    target);
//                            handleMultipleTextTooltips(textTooltipQueue);
//                        }
//                    })
//                    .build()
//                    .show();
//        }
//    }
//
//    /**
//     * Consultation function, first navigate to the consultation list
//     */
//    void doConsultListAction(View view) {
//        binding.funMedia.setVisibility(View.GONE);
//        if (getActivity() instanceof ChatActivity) {
//            ((ChatActivity) getActivity()).doSearchCancelAction();
//        }
//        Bundle bundle = new Bundle();
//        bundle.putString(BundleKey.ROOM_ID.key(), chatRoom.getId());
//        bundle.putString(BundleKey.SERVICE_NUMBER_ID.key(), chatRoom.getServiceNumberId());
//        bundle.putString(BundleKey.ROOM_TYPE.key(), InvitationType.ServiceNUmberConsultationAI.name());
//        bundle.putString(BundleKey.CONSULT_AI_ID.key(), chatRoom.getAiConsultId());
//        //bundle.putSerializable(BundleKey.BLACK_LIST.key(), chatViewModel.getActiveServiceConsultArray());
//        IntentUtil.INSTANCE.launchIntent(requireContext(), MemberInvitationActivity.class, consultSelectResult, bundle);
//    }
//
//    // 選擇 Ai 諮詢
//    private void onAiConsultSelected(String consultId) {
//        chatViewModel.addAiConsultRoom(consultId, chatRoom.getId(), chatRoom.getServiceNumberId());
//    }
//
//    //諮詢選擇完
//    private void onConsultSelected(Intent intent) {
//        String consultRoomId = intent.getStringExtra(BundleKey.ROOM_ID.key());
//        chatViewModel.startConsult(chatRoom.getId(), consultRoomId);
//    }
//
//
//    /**
//     * EVAN_FLAG 2019-09-05 主題聊天室收合控制
//     * (1.9.0) 預設高度自動增長並設置最高為2/3
//     */
//    void doThemeExpandAction(View view) {
//        if (view.getTag() == null) { // 放大
//            view.setTag("change height");
//            this.binding.expandIV.setImageResource(R.drawable.collapse_white);
//            this.binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(0.0d));
//            this.binding.themeMRV.refreshData();
//        } else { // 縮小至最高2/3
//            view.setTag(null);
//            this.binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0d / 3.0d));
//            this.binding.themeMRV.refreshData();
//            this.binding.expandIV.setImageResource(R.drawable.expand_white);
//        }
//    }
//
//    void doThemeCloseAction() {
//        this.binding.expandIV.setTag(null);
//        this.binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0d / 3.0d));
//        this.binding.themeMRV.clearData();
//        this.binding.themeMRV.setVisibility(View.GONE);
//        binding.facebookGroup.setVisibility(View.GONE);
//        this.isThemeOpen = false;
//        this.themeId = "";
//        if (isFacebookReplyOverTime) {
//            binding.chatKeyboardLayout.getFacebookOverTimeView().setVisibility(View.VISIBLE);
//        }
//        isReply = false;
//    }
//
//    // EVAN_FLAG 2019-11-20 (1.8.0) 服務號接線或斷線
////    @OnClick(R.id.agnetActionTV)
//    void doPersonalServiceEventAction(View view) {
//        String userId = TokenPref.getInstance(requireActivity()).getUserId();
//        String serviceNumberAgnetId = this.chatRoom.getServiceNumberAgentId();
//        doServiceNumberStop(this.chatRoom.getId());
//        if (ServiceNumberType.PROFESSIONAL.equals(this.chatRoom.getServiceNumberType())) {
//            if (userId.equals(serviceNumberAgnetId)) {
//                doServiceNumberStop(this.chatRoom.getId()); // 當專人為自己
//            }
////            else {
////                doServiceNumberStart(this.chatRoom.getId()); // 當沒有專人
////            }
//        } else if (!ServiceNumberType.PROFESSIONAL.equals(this.chatRoom.getServiceNumberType())) {
//            if (Strings.isNullOrEmpty(serviceNumberAgnetId)) {
////                doServiceNumberStart(this.chatRoom.getId()); // 當沒有專人
//            } else {
//                doServiceNumberStop(this.chatRoom.getId()); // 當專人為自己
//            }
//        }
//    }
//
//    private void doServiceNumberStart(String roomId) {
//        ApiManager.doServiceNumberStartService(requireContext(), roomId, new ApiListener<Boolean>() {
//            @Override
//            public void onSuccess(Boolean result) {
//                String userId = TokenPref.getInstance(requireContext()).getUserId();
//                if (result) {
//                    boolean status = ChatRoomReference.getInstance().updateServiceNumberAgentIdById(roomId, userId);
//                    chatRoom.setServiceNumberAgentId(userId);
//                    doServiceNumberServicedStatus(chatRoom);
//                    setupAppointStatus(appointResp);
//                    setup();
//                }
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//                Log.i(TAG, errorMessage + "");
//            }
//        });
//    }
//
//    private void doServiceNumberStop(String roomId) {
//        ApiManager.doServiceNumberStopService(requireContext(), roomId, new ApiListener<Boolean>() {
//            @Override
//            public void onSuccess(Boolean result) {
//                if (!result) {
//                    Toast.makeText(requireContext(), "關閉服務失敗", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                boolean status = ChatRoomReference.getInstance().updateServiceNumberAgentIdById(roomId, "");
////                if (status) {
////                    agentActionTV.setVisibility(View.GONE);
////                }
//                chatRoom.setServiceNumberAgentId("");
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//                Log.i(TAG, errorMessage + "");
//            }
//        });
//    }
//
//
//    //商務號點擊右下角頭像可切換身份
//    private void showSwitchIdentityDialog(ServiceNumberChatroomAgentServicedRequest.Resp resp) {
//        if (getContext() != null) {
//            List<ServicesIdentityListResponse> filterSelfList = identityList.stream()
//                    .filter(identityList ->
//                            !identityList.getId().equals(chatRoom.getIdentityId())
//                    )
//                    .collect(Collectors.toList());
//            new BottomSheetDialogBuilder(getContext(), getLayoutInflater())
//                    .getSwitchIdentityDialog(filterSelfList, servicesIdentityListResponse -> {
//                        sendSwitchIdentity(servicesIdentityListResponse);
//                        return null;
//                    }, () -> {
//                        showStopServiceDialog(resp);
//                        return null;
//                    }).show();
//        }
//    }
//
//    private void getIdentityList() {
//        if (getContext() != null) {
//            NetworkManager.INSTANCE.provideRetrofit(getContext())
//                    .create(IdentityListService.class)
//                    .getIdentityList(
//                            new ServicesIdentityListRequest(
//                                    SyncContactRequest.PAGE_SIZE,
//                                    0L,
//                                    chatRoom.getServiceNumberId()
//                            )).enqueue(new Callback<CommonResponse<List<ServicesIdentityListResponse>>>() {
//                        @Override
//                        public void onResponse(Call<CommonResponse<List<ServicesIdentityListResponse>>> call, Response<CommonResponse<List<ServicesIdentityListResponse>>> response) {
//                            identityList = response.body().getItems();
//                            changeIdentity();
//                        }
//
//                        @Override
//                        public void onFailure(Call<CommonResponse<List<ServicesIdentityListResponse>>> call, Throwable t) {
//                        }
//                    });
//        }
//    }
//
//    private void showSwitchIdentityToast(ServicesIdentityListResponse servicesIdentityListResponse) {
//        String message = String.format(getString(R.string.switch_identity_toast), servicesIdentityListResponse.getText());
//        ToastUtils.showToast(requireContext(), message);
//    }
//
//    private void sendSwitchIdentity(ServicesIdentityListResponse servicesIdentityListResponse) {
//        if (getContext() != null) {
//            showLoadingView(R.string.wording_processing);
//            NetworkManager.INSTANCE.provideRetrofit(getContext())
//                    .create(SwitchIdentityService.class)
//                    .setIdentity(new ServiceSwitchIdentityRequest(
//                            SyncContactRequest.PAGE_SIZE,
//                            0L,
//                            chatRoom.getServiceNumberId(),
//                            servicesIdentityListResponse.getId()
//                    ))
//                    .enqueue(new Callback<ServiceSwitchIdentityResponse>() {
//                        @Override
//                        public void onResponse(Call<ServiceSwitchIdentityResponse> call, Response<ServiceSwitchIdentityResponse> response) {
//                            showSwitchIdentityToast(servicesIdentityListResponse);
//                            hideLoadingView();
//                            changeIdentity(servicesIdentityListResponse);
//                        }
//
//                        @Override
//                        public void onFailure(Call<ServiceSwitchIdentityResponse> call, Throwable t) {
//                            hideLoadingView();
//                            ToastUtils.showToast(requireContext(), getString(R.string.switch_identity_failed_toast));
//                            showSwitchIdentityToast(servicesIdentityListResponse);
//                        }
//                    });
//        }
//    }
//
//    //一開始進來那 identityId 去比對 list 做更換
//    private void changeIdentity() {
//        if (identityList != null && !identityList.isEmpty()) {
//            ServicesIdentityListResponse filterList = identityList.stream()
//                    .filter(servicesIdentityListResponse ->
//                            servicesIdentityListResponse.getId().equals(chatRoom.getIdentityId()))
//                    .findFirst().orElse(new ServicesIdentityListResponse());
//            changeIdentity(filterList);
//        }
//    }
//
//    //點擊列表做更換
//    private void changeIdentity(ServicesIdentityListResponse servicesIdentityListResponse) {
//        if (getContext() != null) {
//            binding.civServicedAgentAvatar.loadAvatarIcon(servicesIdentityListResponse.getAvatarId(), servicesIdentityListResponse.getNickName(), servicesIdentityListResponse.getId());
//            chatRoom.setIdentityId(servicesIdentityListResponse.getId());
//            presenter.switchIdentity(servicesIdentityListResponse);
//        }
//    }
//
//    private void showStopServiceDialog(ServiceNumberChatroomAgentServicedRequest.Resp resp) {
//        String selfId = getUserId();
//        String serviceNumberAgentId = resp.getServiceNumberAgentId();
//        if (ServiceNumberType.PROFESSIONAL.equals(this.chatRoom.getServiceNumberType()) && !selfId.equals(serviceNumberAgentId)) {
//            String agentName = UserProfileReference.findAccountName(null, serviceNumberAgentId);
//            String msg = StringHelper.getString(agentName, "有人") + "正在服務中，您確定要接續成為客戶專屬服務人員嗎？";
//
//            new AlertView.Builder()
//                    .setContext(getCtx())
//                    .setStyle(AlertView.Style.Alert)
//                    .setMessage(msg)
//                    .setOthers(new String[]{"取消", "確定"})
//                    .setOnItemClickListener((o, position) -> {
//                        if (position == 1) {
//                            binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg);
//                            doServiceNumberTransfer(ChatServiceNumberService.ServicedTransferType.TRANSFER_SNATCH, resp, "");
////                            Toast.makeText(requireActivity(), "您已接替服務", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .build()
//                    .setCancelable(true)
//                    .show();
//            return;
//        }
//
//        String message = "您正在服務中，您確認要結束該進線的服務嗎？";
//        if (ServiceNumberType.NORMAL.equals(chatRoom.getServiceNumberType())) { //一般
//            message = "您正在服務" + chatRoom.getName() + "，確認要結束此次服務嗎？";
//        } else if (ServiceNumberType.PROFESSIONAL.equals(chatRoom.getServiceNumberType())) { //專業
//            message = "您正在服務" + chatRoom.getName() + "，確認要結束此次服務嗎？"; //當前服務者
//        } else if (ServiceNumberType.BOSS.equals(chatRoom.getServiceNumberType())) {
//            if (!TokenPref.getInstance(requireActivity()).getUserId().equals(chatRoom.getServiceNumberOwnerId())) {
//                message = "您正在為" + chatRoom.getServiceNumberName() + "的客戶" + chatRoom.getName() + "服務，您確認要結束此次服務嗎？"; //秘書號
//            } else {
//                message = "您正在服務" + chatRoom.getName() + "，確認要結束此次服務嗎？"; //擁有者
//            }
//        }
//
//        new AlertView.Builder()
//                .setContext(getCtx())
//                .setStyle(AlertView.Style.Alert)
//                .setMessage(message)
//                .setOthers(new String[]{"取消", "確認"})
//                .setOnItemClickListener((o, position) -> {
//                    if (position == 1) {
//                        stopService();
//                    }
//                })
//                .build()
//                .setCancelable(true)
//                .show();
//    }
//
//    // EVAN_FLAG 2019-11-19 (1.8.0) 結束多渠道服務，
//    //  並停止專人服務
//    private void doAgentStopService(ServiceNumberChatroomAgentServicedRequest.Resp resp) {
//        // 商業服務號右下角頭像可切換身份
//        if (ServiceNumberType.BOSS.equals(chatRoom.getServiceNumberType()) && !chatRoom.getServiceNumberOwnerId().equals(getUserId())) {
//            showSwitchIdentityDialog(resp);
//        } else {
//            showStopServiceDialog(resp);
//        }
//    }
//
//
//    private void stopService() {
//        showLoadingView(R.string.wording_service_closing_noe);
//        String roomId = this.chatRoom.getId();
//        ApiManager.doAgentStopService(getCtx(), roomId, new ApiListener<String>() {
//            @Override
//            public void onSuccess(String s) {
//                ApiManager.doServiceNumberStopService(requireActivity(), roomId, new ApiListener<Boolean>() {
//                    @Override
//                    public void onSuccess(Boolean result) {
//                        hideLoadingView();
//                        if (!result) {
//                            Toast.makeText(requireActivity(), "關閉服務失敗", Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                        boolean status = ChatRoomReference.getInstance().updateServiceNumberAgentIdById(roomId, "");
//
//                        if (appointResp != null) {
//                            if (ChannelType.QBI.equals(appointResp.getLastFrom())) {
//                                binding.chatKeyboardLayout.setChatDisable("qbi通路無法離線回覆");
//                            } else {
//                                binding.chatKeyboardLayout.setChatEnable();
//                            }
//                            appointResp.setStatus(ServiceNumberStatus.OFF_LINE);
//                            setupAppointStatus(appointResp);
//                        }
//                        Toast.makeText(requireActivity(), "成功關閉服務", Toast.LENGTH_SHORT).show();
//
//                        chatRoom.setServiceNumberAgentId("");
//                        ChatRoomReference.getInstance().updateServiceNumberStatusById(roomId, ServiceNumberStatus.OFF_LINE);
//                        setup();
//                        clearProvisionalMember();
//                        chatViewModel.clearConsultRoom();
//                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REMOVE_GROUP_FILTER, roomId));
//                        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REFRESH_ROOM_BY_LOCAL, JsonHelper.getInstance().toJson(chatRoom)));
//
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//                        hideLoadingView();
//                        Toast.makeText(requireActivity(), "關閉服務失敗", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//                hideLoadingView();
//                Toast.makeText(requireActivity(), "關閉服務失敗", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void clearProvisionalMember() {
//        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//            binding.scopeProvisionalMemberList.setVisibility(View.GONE);
//            chatRoom.getProvisionalIds().clear();
//            provisionalMemberAdapter.submitList(Lists.newArrayList());
//        });
//    }
//
//    private void doSwitchChannel(ChannelType newFrom) {
//        if (ServiceNumberType.PROFESSIONAL.equals(this.chatRoom.getServiceNumberType()) && !Strings.isNullOrEmpty(this.chatRoom.getServiceNumberAgentId()) && !getUserId().equals(this.chatRoom.getServiceNumberAgentId())) {
//            Toast.makeText(requireActivity(), "專人服務中，無法切換渠道", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        showLoadingView(R.string.wording_switching_now);
//        ApiManager.doFromSwitch(getCtx(), chatRoom.getId(), appointResp.getLastFrom().getValue(), newFrom.getValue(), new ApiListener<Map<String, String>>() {
//            @Override
//            public void onSuccess(Map<String, String> data) {
//                assert data != null;
//                String oldFrom = data.get("oldFrom");
//                String from = data.get("newFrom");
//                hideLoadingView();
//                appointResp.setLastFrom(newFrom);
////                        appointResp.setStatus(FromAppointRequest.ResponseVo.Status.OFF_LINE);
//                if (ChannelType.QBI.equals(newFrom)) {
//                    binding.chatKeyboardLayout.setChatDisable("qbi通路無法離線回覆");
//                } else {
//                    binding.chatKeyboardLayout.setChatEnable();
//                }
//
//                setupAppointStatus(appointResp);
//                Toast.makeText(requireActivity(), "切換成功", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//                hideLoadingView();
//                Toast.makeText(requireActivity(), "切換失敗", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//    }
//
//    @Override
//    public void refreshPager(ChatRoomEntity session) {
//        ChatActivity activity = (ChatActivity) getActivity();
//        activity.refreshPager(session);
//    }
//
//
//    @Override
//    public void refreshUnReadNum() {
//    }
//
//    @Override
//    public void refreshUI() {
//        refreshListView();
//
//    }
//
//    @Override
//    public MessageEntity getLastMessage() {
//        if (mainMessageData.size() > 0) {
//            return mainMessageData.get(mainMessageData.size() - 1);
//        } else {
//            return null;
//        }
//    }
//
//    @Override
//    public void updateSenderName(String senderName, String senderId) {
//        for (MessageEntity message : mainMessageData) {
//            if (message.getSenderId() != null && message.getSenderId().equals(senderId)) {
//                message.setSenderName(senderName);
//            }
//        }
//        refreshListView();
//    }
//
//    @Override
//    public void onLoadMoreMsg(List<MessageEntity> messages) {
//        binding.xrefreshLayout.completeRefresh();
//        if (!messages.isEmpty()) {
//            mainMessageData.addAll(messages);
//            refreshListView();
//        } else {
//            presenter.setRecordMode(false);
//        }
//    }
//
//    @Override
//    public void transferModeDisplay(List<MessageEntity> messages) {
//        mainMessageData.clear();
////        this.messageIds.clear();
//        for (MessageEntity msg : messages) {
//            if (msg.getSendNum() > 0) {
//                msg.setStatus(MessageStatus.SUCCESS);
//            }
//            if (msg.getReceivedNum() > 0) {
//                msg.setStatus(RECEIVED);
//            }
//            if (msg.getReadedNum() > 0) {
//                msg.setStatus(MessageStatus.READ);
//            }
//            mainMessageData.add(msg);
////            this.messageIds.add(message.getId());
//        }
////        refreshToBottom();
//        binding.messageRV.refreshToBottom(this.actionStatus.status());
//    }
//
//    @Override
//    public void scrollToTop() {
//        binding.messageRV.post(() -> {
//            binding.messageRV.scrollToPosition(0);
//        });
//    }
//
//    @Override
//    public long getLastMsgTime() {
//        if (!mainMessageData.isEmpty()) {
//            return mainMessageData.get(mainMessageData.size() - 1).getSendTime();
//        }
//        return 0;
//    }
//
//    @Override
//    public void finishActivity() {
//        requireActivity().finish();
//    }
//
//    /**
//     * 處理用戶在多渠道服務號是否上線
//     * 檢查為服務號聊天室，及狀態欄位為顯示狀態
//     */
//    @Override
//    public void doChannelOnLineStatus(String roomId, MessageEntity message) {
//        if (this.chatRoom.isService(getUserId())) {
////            binding.chatKeyboardLayout.setBackgroundColor(Color.parseColor("#F1F4F5"));
//            // EVAN_FLAG 2019-11-19 (1.8.0) 多渠道進線錯亂影響到其它聊天室
//            if (this.chatRoom.getId().equals(roomId)) {
//                doAppointStatus(roomId);
//            }
//        }
//    }
//
//    @Override
//    public void doChangeAgentStatus() {
//
//    }
//
//    public void doShowSnatchRobotChat(String roomId, HadEditText.SendData sendData) {
//        getSnatchDialog(getString(R.string.text_robot_servicing_snatch_by_agent))
//                .setOthers(new String[]{getString(R.string.picture_cancel), getString(R.string.text_robot_servicing_transfer)})
//                .setOnItemClickListener((o, position) -> {
//                    if (position == 1) {
//                        presenter.doRobotSnatchByAgent(requireContext(), roomId, sendData);
//                        showLoadingView();
//                    }
//                })
//                .build()
//                .setCancelable(true)
//                .show();
//    }
//
//    public AlertView.Builder getSnatchDialog(String message) {
//        return  new AlertView.Builder()
//                .setContext(getCtx())
//                .setStyle(AlertView.Style.Alert)
//                .setMessage(message)
//                .setOthers(new String[]{getString(R.string.picture_cancel), getString(R.string.text_robot_servicing_transfer)});
//    }
//    private void stopIosProgressBar() {
//        try {
//            if (progressBar != null && progressBar.isShowing())
//                progressBar.dismiss();
//        } catch (Exception ignored) {
//        }
//    }
//
//    @Override
//    public void doChatRoomSnatchByAgent(boolean isSuccess, HadEditText.SendData sendData) {
//        if (isSuccess) {
//            binding.scopeRobotChat.setVisibility(View.GONE);
//            binding.ivTurnToAiServices.setVisibility(View.GONE);
//            if (getActivity() instanceof ChatActivity) {
//                ((ChatActivity) getActivity()).showTopMenu(true);
//            }
//            chatRoom.setServiceNumberStatus(ServiceNumberStatus.ON_LINE);
//            setInputHint();
//            doServiceNumberServicedStatus(chatRoom);
//            sendMessage(sendData);
//            initBottomRoomList();
//            stopIosProgressBar();
//        } else
//            ToastUtils.showToast(requireActivity(), getString(R.string.text_can_not_snatch));
//    }
//
//    @Override
//    public void onCompleteProvisionalMemberList(List<UserProfileEntity> entities, List<String> newMemberIds) {
//        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//            binding.scopeProvisionalMemberList.setVisibility(!entities.isEmpty() ? View.VISIBLE : View.GONE);
//            if (chatRoom == null) return;
//            if(chatRoom.getProvisionalIds() != null) {
//                if (chatRoom.getProvisionalIds().isEmpty())
//                    chatRoom.setProvisionalIds(newMemberIds);
//            }
//            provisionalMemberAdapter.submitList(entities);
//        });
//    }
//
//    @Override
//    public void onAddProvisionalMember(List<String> addNewMemberIds) {
//        List<String> oldProvisionalMemberList = Lists.newArrayList(chatRoom.getProvisionalIds());
//        oldProvisionalMemberList.removeAll(addNewMemberIds);
//        oldProvisionalMemberList.addAll(addNewMemberIds);
//        chatRoom.setProvisionalIds(oldProvisionalMemberList);
//        provisionalMemberAdapter.submitList(presenter.getMemberProfileEntity(oldProvisionalMemberList));
//    }
//
//    @Override
//    public void showErrorMsg(String msg) {
//        ToastUtils.showToast(requireActivity(), msg);
//    }
//    @Override
//    public void showErrorToast(String errorMessage) {
//        ToastUtils.showToast(requireContext(), errorMessage);
//    }
//
//
//    @Override
//    public void showNoMoreMessage() {
//        adapter.setData(adapter.getData(), keyWord, chatRoomMemberTable);
//    }
//
//    @Override
//    public void showIsNotMemberMessage(String errorMessage) {
//        if (!isAdded() || requireActivity() == null) return;
//        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> ToastUtils.showCenterToast(requireActivity(), errorMessage));
//        if (!errorMessage.equals(getString(R.string.api_there_no_internet_connection))) {
//            chatViewModel.deletedRoom(chatRoom.getId());
//        }
//    }
//
//    @Override
//    public void setSearchKeyWord(String keyWord) {
//        Objects.requireNonNull(binding.messageRV.getAdapter()).setKeyword(keyWord).refreshData();
//    }
//
//    public void release() {
//        chatRoom = null;
//        presenter.clearSession();
//    }
//
//    @Override
//    public void onBackgroundClick(XRefreshLayout refreshLayout) {
//        this.actionStatus = ActionStatus.SCROLL;
//        if (binding.messageRV.getIsShowCheckBox()) {
//            binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT);
//        }
//        // 當搜索模式開啟不控制背景點擊
//        if (binding.searchBottomBar.getVisibility() == View.VISIBLE) {
//
//        } else {
//            binding.chatKeyboardLayout.showKeyboard();
//        }
//    }
//
//    private void sendFacebookPrivateReply(MessageEntity message, HadEditText.SendData content) {
//        if (!message.isFacebookPrivateReplied()) {
//            presenter.sendFacebookPrivateReply(message, content);
//            closeFacebookTheme();
//            binding.messageRV.refreshToBottom(true);
//        } else {
//            Toast.makeText(requireContext(), getString(R.string.facebook_already_private_replied), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void sendFacebookPublicReply(MessageEntity message, HadEditText.SendData content) {
//        presenter.sendFacebookPublicReply(message, content);
//        binding.messageRV.refreshToBottom(true);
//        closeFacebookTheme();
//        if (isFacebookReplyOverTime) {
//            binding.chatKeyboardLayout.getFacebookOverTimeView().setVisibility(View.VISIBLE);
//        }
//    }
//
//    private void closeFacebookTheme() {
//        if (requireActivity() != null) {
//            requireActivity().runOnUiThread(() -> {
//                binding.chatKeyboardLayout.getInputHET().setText("");
//                binding.themeMRV.setVisibility(View.GONE);
//                KeyboardHelper.hide(binding.chatKeyboardLayout.getInputHET());
//            });
//        }
//    }
//
//    private void sendMessage(HadEditText.SendData sendData) {
//        if (sendData.getType() == MessageType.TEXT && Strings.isNullOrEmpty(
//                sendData.getContent().trim())) {
//            Toast.makeText(requireActivity(), requireActivity().getString(R.string.text_can_not_send_empty_message), Toast.LENGTH_SHORT).show();
//            Log.e("TAG", "onSendBtnClick sendData 不可發送空白訊息");
//            return;
//        }
//        //判斷是否有機器人服務，是否強直接手
//        if (ServiceNumberStatus.ROBOT_SERVICE.equals(chatRoom.getServiceNumberStatus())
//                && !chatRoom.getOwnerId().equals(getUserId())) {
//            doShowSnatchRobotChat(chatRoom.getId(), sendData);
//        } else {
//            // 送其他內容，隱藏 QuickReply
//            binding.rvQuickReplyList.setVisibility(View.GONE);
//            presenter.sendButtonClicked(chatRoom);
//        }
//    }
//
//    private void updateChatRoom(String roomId) {
//        chatRoom = ChatRoomEntity.Build()
//                .id(roomId)
//                .name(userName)
//                .ownerId(userId)
//                .updateTime(System.currentTimeMillis())
//                .type(ChatRoomType.friend)
//                .build();
//        ChatRoomReference.getInstance().save(chatRoom);
//        UserPref.getInstance(requireContext()).setCurrentRoomId(roomId);
//        App.getInstance().chatRoomId = chatRoom.getId();
//        presenter = new ChatPresenter(chatRoom, this, null, requireActivity());
//        setup();
//        presenter.init(this.messageEntity, keyWord);
//    }
//
//
//    private boolean checkMicAvailable() {
//        if (AudioLib.getInstance(requireActivity()).isPlaying()) {
//            Toast.makeText(requireActivity(), "麥克風正在被佔用", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//
//        if (binding.chatKeyboardLayout.isRecording) {
//            Toast.makeText(requireActivity(), "正在錄音", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        return true;
//    }
//
//    public class KeyBoardBarListener implements ChatKeyboardLayout.OnChatKeyBoardListener {
//        @Override
//        public void onSendBtnClick(HadEditText.SendData sendData, boolean enableSend) {
//            if (chatRoom == null) {
//                ChatService.getInstance().addContact(new ApiListener<String>() {
//                    @Override
//                    public void onSuccess(final String roomId) {
//                        //虛擬聊天室
//                        Log.d("TAG", "加好友成功");
//                        updateChatRoom(roomId);
//
//                        ApiManager.doAddressbookSync(getCtx(), new ApiListener<List<UserProfileEntity>>() {
//                            @Override
//                            public void onSuccess(List<UserProfileEntity> profiles) {
//                                Log.d("TAG", "資料同步成功 : " + profiles.toString());
//                                sendMessage(sendData);
//                            }
//
//                            @Override
//                            public void onFailed(String errorMessage) {
//                                ToastUtils.showToast(getCtx(), "資料同步失敗" + errorMessage);
//                                Log.d("TAG", "資料同步失敗" + errorMessage);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//                        if (errorMessage.contains("已經是您的好友")) {
//                            ApiManager.doAddressbookSync(getCtx(), new ApiListener<List<UserProfileEntity>>() {
//                                @Override
//                                public void onSuccess(List<UserProfileEntity> profiles) {
//                                    Log.d("TAG", "資料同步成功 : " + profiles.toString());
//                                    sendMessage(sendData);
//                                }
//
//                                @Override
//                                public void onFailed(String errorMessage) {
//                                    ToastUtils.showToast(getCtx(), "資料同步失敗" + errorMessage);
//                                    Log.d("TAG", "資料同步失敗" + errorMessage);
//                                }
//                            });
//                        } else {
//                            ToastUtils.showToast(getCtx(), errorMessage);
//                            Log.d("TAG", "加好友失敗 : " + errorMessage);
//                        }
//                    }
//                }, userId, userName);
//            } else {
//                MessageEntity message = mainMessageData.stream().filter(messageEntity -> messageEntity.getId().equals(themeId)).findFirst().orElse(null);
//                if (message != null && message.getFrom() == ChannelType.FB && isFacebookReplyPublic) {
//                    // 公開回覆
//                    sendFacebookPublicReply(message, sendData);
//                } else if (message != null && message.getFrom() == ChannelType.FB && !isFacebookReplyPublic) {
//                    // 私訊回覆
//                    sendFacebookPrivateReply(message, sendData);
//                }else {
//                    sendMessage(sendData);
//                }
//            }
//        }
//
//        @Override
//        public void onRecordingSendAction(String path, final int duration) {
//            presenter.sendVoice(path, duration, isFacebookReplyPublic);
//        }
//
//        @Override
//        public void onRecordingStartAction() {
//            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
//                if (checkMicAvailable()) {
//                    showRecordingWindow();
//                }
//            } else {
//                recordPermissionResult.launch(Manifest.permission.RECORD_AUDIO);
//            }
//        }
//
//        @Override
//        public void onUserDefEmoticonClicked(String tag, String packageId) {
////            presenter.sendSticker(tag, packageId);
//        }
//
//        @Override
//        public void onStickerClicked(String stickerId, String packageId) {
//            presenter.sendSticker(stickerId, packageId);
//        }
//
//        private File createVideoFile() throws IOException {
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String imageFileName = "Aile_" + timeStamp + "_";
//            File storageDir = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_MOVIES);
//            // Save a file: path for use with ACTION_VIEW intents
//            return File.createTempFile(
//                    imageFileName,  /* prefix */
//                    ".mp4",         /* suffix */
//                    storageDir      /* directory */
//            );
//        }
//
//        @Override
//        public void onOpenVideo() {
//            binding.funMedia.setVisibility(View.GONE);
//            IS_ACTIVITY_FOR_RESULT = true;
//            binding.chatKeyboardLayout.clearIconState();
//            String[] permissions_TIRAMISU = {CAMERA};
//            String[] permissions = {CAMERA, WRITE_EXTERNAL_STORAGE};
//
//            String[] combinedPermissions;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                combinedPermissions = permissions_TIRAMISU;
//            } else {
//                combinedPermissions = permissions;
//            }
//            XXPermissions.with(requireActivity()).permission(combinedPermissions).request(new OnPermissionCallback() {
//                @Override
//                public void onGranted(List<String> permissions, boolean all) {
//                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//                    try {
//                        videoFile = createVideoFile();
//                        Uri uri = FileProvider.getUriForFile(requireActivity(), "tw.com.chainsea.chat.fileprovider", videoFile);
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, CamcorderProfile.QUALITY_HIGH);
//                        startActivityForResult(intent, REQUEST_VIDEO);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onDenied(List<String> permissions, boolean never) {
//                    Toast.makeText(requireActivity(), requireActivity().getString(R.string.text_need_camera_permission), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//
//        /**
//         * 打開文件夾列表
//         */
//        @Override
//        public void onOpenFolders() {
//            binding.funMedia.setVisibility(View.GONE);
//            IS_ACTIVITY_FOR_RESULT = true;
//            binding.chatKeyboardLayout.clearIconState();
//            startActivityForResult(new Intent(requireActivity(), FileExplorerActivity.class), RESULT_FILE);
//            requireActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
//        }
//
//        private File createImageFile() throws IOException {
//            // Create an image file name
//            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//            String imageFileName = "Aile_" + timeStamp + "_";
//            File storageDir = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_PICTURES);
//            // Save a file: path for use with ACTION_VIEW intents
//            //String currentPhotoPath = image.getAbsolutePath();
//            return File.createTempFile(
//                    imageFileName,  /* prefix */
//                    ".jpg",         /* suffix */
//                    storageDir      /* directory */
//            );
//        }
//
//        @Override
//        public void onOpenCamera() {
//            binding.funMedia.setVisibility(View.GONE);
//            IS_ACTIVITY_FOR_RESULT = true;
//            binding.chatKeyboardLayout.clearIconState();
//            String[] permissions_TIRAMISU = {CAMERA};
//            String[] permissions = {CAMERA, WRITE_EXTERNAL_STORAGE};
//
//            String[] combinedPermissions;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                combinedPermissions = permissions_TIRAMISU;
//            } else {
//                combinedPermissions = permissions;
//            }
//            XXPermissions.with(requireActivity()).permission(combinedPermissions).request(new OnPermissionCallback() {
//                @Override
//                public void onGranted(List<String> permissions, boolean all) {
//                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    try {
//                        photoFile = createImageFile();
//                        Uri uri = FileProvider.getUriForFile(requireActivity(), "tw.com.chainsea.chat.fileprovider", photoFile);
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//                        startActivityForResult(intent, REQUEST_CAMERA);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onDenied(List<String> permissions, boolean never) {
//                    Toast.makeText(requireActivity(), requireActivity().getString(R.string.text_need_camera_permission), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//
//        // set Digital Style
//        private PictureSelectorStyle getDigitalStyle() {
//            PictureSelectorStyle pictureSelectorStyle = new PictureSelectorStyle();
//
//            TitleBarStyle blueTitleBarStyle = new TitleBarStyle();
//            blueTitleBarStyle.setTitleBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
//
//            BottomNavBarStyle numberBlueBottomNavBarStyle = new BottomNavBarStyle();
//            numberBlueBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_9b));
//            numberBlueBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
//            numberBlueBottomNavBarStyle.setBottomNarBarBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_color_white));
//            numberBlueBottomNavBarStyle.setBottomSelectNumResources(R.drawable.album_num_selected);
//            numberBlueBottomNavBarStyle.setBottomEditorTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_53575e));
//            numberBlueBottomNavBarStyle.setBottomOriginalTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_53575e));
//
//
//            SelectMainStyle numberBlueSelectMainStyle = new SelectMainStyle();
//            numberBlueSelectMainStyle.setStatusBarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
//            numberBlueSelectMainStyle.setSelectNumberStyle(true);
//            numberBlueSelectMainStyle.setPreviewSelectNumberStyle(true);
//            numberBlueSelectMainStyle.setSelectBackground(R.drawable.album_num_selector);
//            numberBlueSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(requireContext(), R.color.ps_color_white));
//            numberBlueSelectMainStyle.setPreviewSelectBackground(R.drawable.album_preview_num_selector);
//
//            numberBlueSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(requireContext(), R.color.ps_color_9b));
//            numberBlueSelectMainStyle.setSelectTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
//            numberBlueSelectMainStyle.setSelectText(R.string.ps_completed);
//
//            pictureSelectorStyle.setTitleBarStyle(blueTitleBarStyle);
//            pictureSelectorStyle.setBottomBarStyle(numberBlueBottomNavBarStyle);
//            pictureSelectorStyle.setSelectMainStyle(numberBlueSelectMainStyle);
//            return pictureSelectorStyle;
//        }
//
//        //聊天室圖片選擇器
//        @Override
//        public void onOpenGallery() {
//            IS_ACTIVITY_FOR_RESULT = true;
//            binding.chatKeyboardLayout.clearIconState();
//
//            // 选择照片
//            PictureSelector.create(requireActivity())
//                    .openGallery(SelectMimeType.ofAll())
//                    .setSelectorUIStyle(getDigitalStyle())
//                    .setSelectionMode(SelectModeConfig.MULTIPLE)
//                    .isWithSelectVideoImage(true)
//                    .setMaxSelectNum(9)
//                    .setMinSelectNum(0)
//                    .setMinVideoSelectNum(0)
//                    .setMaxVideoSelectNum(9)
//                    .isPreviewVideo(true)
//                    .isPreviewImage(true)
//                    .isDisplayCamera(false)
//                    .isOriginalSkipCompress(false)
//                    .isGif(true)
//                    .isOpenClickSound(false)
//                    .setSelectFilterListener(media -> {
//                        //CELog.d("Kyle2 type="+media.getMimeType()+", size="+media.getSize());
//                        if (media.getMimeType().contains("video/") && !media.getMimeType().equals("video/mp4")) {
//                            ToastUtils.showToast(requireActivity(), getString(R.string.text_video_limit_mp4_format));
//                            return true;
//                        }
//
//                        if (appointResp != null && appointResp.getLastFrom() != null && appointResp.getLastFrom().equals(ChannelType.LINE)) {
//                            if (media.getMimeType().contains("image/") && media.getSize() >= 10 * FileSizeUnit.MB) {
//                                ToastUtils.showToast(requireActivity(), getString(R.string.text_file_size_limit, 10));
//                                return true;
//                            } else if (media.getMimeType().contains("video/mp4") && media.getSize() >= 200 * FileSizeUnit.MB) {
//                                ToastUtils.showToast(requireActivity(), getString(R.string.text_file_size_limit, 200));
//                                return true;
//                            }
//                        } else if (appointResp != null && appointResp.getLastFrom() != null && appointResp.getLastFrom().equals(ChannelType.FB)) {
//                            if (media.getMimeType().contains("image/") && media.getSize() >= 25 * FileSizeUnit.MB) {
//                                ToastUtils.showToast(requireActivity(), getString(R.string.text_file_size_limit, 25));
//                                return true;
//                            } else if (media.getMimeType().contains("video/mp4") && media.getSize() >= 25 * FileSizeUnit.MB) {
//                                ToastUtils.showToast(requireActivity(), getString(R.string.text_file_size_limit, 25));
//                                return true;
//                            }
//                        } else if (appointResp != null && appointResp.getLastFrom() != null && appointResp.getLastFrom().equals(ChannelType.IG)) {
//                            if (media.getMimeType().contains("image/") && media.getSize() >= 8 * FileSizeUnit.MB) {
//                                ToastUtils.showToast(requireActivity(), getString(R.string.text_file_size_limit, 8));
//                                return true;
//                            } else if (media.getMimeType().contains("video/mp4") && media.getSize() >= 25 * FileSizeUnit.MB) {
//                                ToastUtils.showToast(requireActivity(), getString(R.string.text_file_size_limit, 25));
//                                return true;
//                            }
//                        } else if (appointResp != null && appointResp.getLastFrom() != null && appointResp.getLastFrom().equals(ChannelType.GOOGLE)) {
//                            if (media.getMimeType().contains("image/") && media.getSize() >= 5 * FileSizeUnit.MB) {
//                                ToastUtils.showToast(requireActivity(), getString(R.string.text_file_size_limit, 5));
//                                return true;
//                            }
//                        }
////                        else {
////                            if((media.getMimeType().contains("image/") && media.getSize() >= 10 * FileSizeUnit.MB) ||
////                                    (media.getMimeType().contains("video/mp4") && media.getSize() >= 200 * FileSizeUnit.MB)
////                            ) {
////                                return true;
////                            }
////                        }
//
//                        return false;
//                    })
//                    .setImageEngine(GlideEngine.Companion.createGlideEngine())
//                    .forResult(new OnResultCallbackListener<LocalMedia>() {
//                        @Override
//                        public void onResult(ArrayList<LocalMedia> result) {
//                            for (LocalMedia localMedia : result) {
//                                String pictureType = localMedia.getMimeType();
//                                if (Strings.isNullOrEmpty(pictureType)) {
//                                    return;
//                                }
//                                //sendFileSize = result.size();
//                                if ("image/gif".equalsIgnoreCase(pictureType)) {
//                                    BitmapBean bitmapBean = PictureParse.parseGifPath(getCtx(), localMedia.getRealPath());
//                                    presenter.sendGifImg(bitmapBean.url, localMedia.getRealPath(), bitmapBean.width, bitmapBean.height);
//                                } else if ("video/mp4".equals(pictureType)) {
//                                    IVideoSize iVideoSize = new VideoSizeFromVideoFile(localMedia.getRealPath());
//                                    presenter.sendVideo(iVideoSize);
//                                } else if ("image/png".equalsIgnoreCase(pictureType) || "image/jpeg".equalsIgnoreCase(pictureType)) {
//                                    String[] path = PictureParse.parsePath(getCtx(), localMedia.getRealPath());
//                                    presenter.sendImage(path[0], path[1], isFacebookReplyPublic);
//                                }
//                            }
//
//                        }
//
//                        @Override
//                        public void onCancel() {
//
//                        }
//                    });
//        }
//
//        /**
//         * 打開表情功能
//         */
//        @Override
//        public void onOpenEmoticon() {
//            binding.funMedia.setVisibility(View.GONE);
//
//        }
//
//        /**
//         * 打開錄音功能
//         */
//        @Override
//        public void onOpenRecord() {
//            binding.funMedia.setVisibility(View.GONE);
//        }
//
//        /**
//         * 打開多媒體選擇器
//         */
//        @Override
//        public void onOpenMultimediaSelector() {
//            binding.funMedia.setType(MultimediaHelper.Type.FILE, themeStyle, -1);
//            if (binding.funMedia.getVisibility() == View.GONE) {
//                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//                    binding.funMedia.setVisibility(View.VISIBLE);
//                });
//            }
//        }
//
//        /**
//         * 打開圖片選擇器
//         */
//        @Override
//        public void onOpenPhotoSelector(boolean isChange) {
//            String[] permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO};
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                for (String permission : permissions) {
//                    int isPermissionGranted = requireContext().checkSelfPermission(permission);
//                    if (isPermissionGranted == PackageManager.PERMISSION_DENIED) {
//                        launcher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES,
//                                Manifest.permission.READ_MEDIA_AUDIO,
//                                Manifest.permission.READ_MEDIA_VIDEO});
//                        return;
//                    }
//                }
//                binding.funMedia.setChangeVisibility();
//                showFunMedia(false);
//            } else {
//                if(requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
//                    binding.funMedia.setChangeVisibility();
//                    showFunMedia(isChange);
//                } else {
//                    storagePermissionResult.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//                }
//            }
//
////            }
//        }
//
//        @Override
//        public void onOpenConsult() {
////            binding.funMedia.setVisibility(View.GONE);
////            ArrayList<String> blacklist = Lists.newArrayList(chatRoom.getId(), chatRoom.getServiceNumberId());
////            ActivityTransitionsControl.navigateToServiceNumberConsultSelect(requireActivity(), blacklist, new ActivityTransitionsControl.CallBack<Intent, String>() {
////                @Override
////                public void complete(Intent intent, String s) {
////                    startActivityForResult(intent, SERVICE_NUMBER_CONSULT_SELECTOR_REQUEST_CODE);
////                }
////            });
//
////            ChatServiceNumberService.findConsultList(requireActivity(), Sets.newHashSet(chatRoom.getId(), chatRoom.getServiceNumberId()), RefreshSource.REMOTE, new ServiceCallBack<List<ChatRoomEntity>, RefreshSource>() {
////                @Override
////                public void complete(List<ChatRoomEntity> entities, RefreshSource source) {
////                    List<AlertView.ImageText> imageTests = Lists.newArrayList();
////                    for (ChatRoomEntity r : entities) {
////                        imageTests.add(new AlertView.ImageText.Builder().bind(r).text(r.getServiceNumberName()).res(R.drawable.wechat_icon).build());
////                    }
////                    new AlertView.Builder()
////                            .setContext(requireActivity())
////                            .setStyle(AlertView.Style.ImageTextAlert)
////                            .setTitle("選擇切換團隊")
////                            .setImageTexts(imageTests, false)
////                            .setCancelText("取消")
////                            .setOnItemClickListener((o, position) -> {
////
////                            })
////                            .build()
////                            .setCancelable(true)
////                            .show();
////                }
////
////                @Override
////                public void error(String message) {
////
////                }
////            });
//        }
//
//        /**
//         * 導航到圖片選擇器預覽
//         */
//        @Override
//        public void toMediaSelectorPreview(boolean isOriginal, String type, String current, TreeMap<String, String> data, int position) {
//            mediaPreviewARL.launch(
//                    new Intent(requireActivity(), MediaSelectorPreviewActivity.class)
//                            .putExtra(BundleKey.IS_ORIGINAL.key(), isOriginal)
//                            .putExtra(BundleKey.MAX_COUNT.key(), current)
//                            .putExtra(BundleKey.TYPE.key(), type)
//                            .putExtra(BundleKey.CURRENT.key(), current)
//                            .putExtra(BundleKey.VIDEO_POSITION.key(), position)
//                            .putExtra(BundleKey.DATA.key(), JsonHelper.getInstance().toJson(data))
//            );
//        }
//
//        @Override
//        public void onMediaSelector(MultimediaHelper.Type type, List<AMediaBean> list, boolean isOriginal) {
//            sendFileSize = list.size();
//            isSendSingleFile = sendFileSize == 1;
//            switch (type) {
//                case IMAGE:
//                    executeSendPhotos(list, isOriginal);
//                    break;
//                case FILE:
//                    for (AMediaBean bean : list) {
//                        int fileType = FileUtil.getFileType(bean.getPath());
//                        switch (fileType) {
//                            case Global.FileType_Png:
//                            case Global.FileType_Jpg:
//                            case Global.FileType_jpeg:
//                            case Global.FileType_bmp:
//                                String[] path = PictureParse.parsePath(getCtx(), bean.getPath());
////                                showSendVideoProgress("圖片發送中");
//                                presenter.sendImage(path[0], path[1], isFacebookReplyPublic);
//                                break;
//                            case Global.FileType_gif:
//                                BitmapBean bitmapBean = PictureParse.parseGifPath(getCtx(), bean.getPath());
//                                presenter.sendGifImg(bitmapBean.url, bean.getPath(), bitmapBean.width, bitmapBean.height);
//                                break;
//                            case Global.FileType_mov:
//                            case Global.FileType_mp4:
//                            case Global.FileType_rmvb:
//                            case Global.FileType_avi:
//                                IVideoSize iVideoSize = new VideoSizeFromVideoFile(bean.getPath());
//                                presenter.sendVideo(iVideoSize);
//                                break;
//                            default:
//                                presenter.sendFile(bean.getPath());
//                                break;
//                        }
//                    }
//                    break;
//                case VIDEO:
//                    executeSendVideos(list, isOriginal);
//                    break;
//            }
//        }
//
//        @Override
//        public void onInputClick() {
//            binding.funMedia.setVisibility(View.GONE);
//        }
//
//        @Override
//        public void onSoftKeyboardStartOpened(int keyboardHeightInPx) {
////            if (binding.scopeRobotChat.getVisibility() == View.VISIBLE)
////                binding.guideLine.setGuidelinePercent(0.79f);
//        }
//
//        @Override
//        public void onSoftKeyboardEndOpened(int keyboardHeightInPx) {
//
//        }
//
//        @Override
//        public void onSoftKeyboardClosed() {
////            binding.lyChildChat.onSoftKeyboardClosed(0);
//            if (binding.messageRV.getAdapter() != null)
//                binding.messageRV.getAdapter().refreshData();
//
////            if (binding.scopeRobotChat.getVisibility() == View.VISIBLE)
////                binding.guideLine.setGuidelinePercent(0.89f);
//        }
//
//        @Override
//        public void onOpenExtraArea() {
//            binding.chatKeyboardLayout.doInitExtraArea(
//                    chatViewModel.isSettingBusinessCardInfo().getValue() && chatRoom.getListClassify().equals(ChatRoomSource.SERVICE) && chatRoom.getServiceNumberOpenType().contains("O") && chatRoom.getType().equals(ChatRoomType.services),
//                    chatRoom,
//                    isProvisionMember,
//                    chatRoom.getServiceNumberType().equals(ServiceNumberType.BOSS) && chatRoom.getServiceNumberOwnerId().equals(getUserId()) && chatRoom.getType().equals(ChatRoomType.services)
//            );
//            if(chatViewModel.getChannelType().isEmpty())
//                chatViewModel.getLastChannelFrom(chatRoom.getId()); //取得渠道名稱以發送電子名片
//        }
//
//        @Override
//        public void onBusinessCardSend() {
//            showLoadingView(R.string.welcome_tip_04);
//            chatViewModel.doSendBusinessCard(chatRoom.getId());
//        }
//
//        @Override
//        public void onBusinessMemberCardSend() {
//            showLoadingView(R.string.welcome_tip_04);
//            chatViewModel.doSendBusinessMemberCard(chatRoom.getId());
//        }
//
//        @Override
//        public void onOpenCameraDialog() {
//            new BottomSheetDialogBuilder(requireContext(), getLayoutInflater()).doOpenMedia(
//                    () -> {
//                        binding.chatKeyboardLayout.doCameraAction();
//                        return null;
//                    }, () -> {
//                        binding.chatKeyboardLayout.doVideoAction();
//                        return null;
//                    }).show();
//        }
//
//        @Override
//        public void onPicSelected(List<PhotoBean> list) {
//            if (list == null || list.size() == 0) {
//                Toast.makeText(requireActivity(), "請先選擇圖片", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            for (int i = 0; i < list.size(); i++) {
//                int fileType = FileUtil.getFileType(list.get(i).path);
//                switch (fileType) {
//                    case Global.FileType_Png:
//                    case Global.FileType_Jpg:
//                    case Global.FileType_jpeg:
//                    case Global.FileType_bmp:
//                        PictureParse.parsePath(getCtx(), list.get(i).path, false, new ServiceCallBack<String[], Enum>() {
//                            @Override
//                            public void complete(String[] path, Enum anEnum) {
////                                showSendVideoProgress("圖片發送中");
//                                presenter.sendImage(path[0], path[1], isFacebookReplyPublic);
//                            }
//
//                            @Override
//                            public void error(String message) {
//                                CELog.e(message);
//                            }
//                        });
////                        String[] path = PictureParse.parsePath(getCtx(), list.get(i).path);
////                        presenter.sendImage(path[0], path[1]);
//                        break;
//                    case Global.FileType_gif:
//                        BitmapBean bitmapBean = PictureParse.parseGifPath(getCtx(), list.get(i).path);
//                        presenter.sendGifImg(bitmapBean.url, list.get(i).path, bitmapBean.width, bitmapBean.height);
//                        break;
//                    default:
//                        presenter.sendFile(list.get(i).path);
//                        break;
//                }
//            }
//        }
//    }
//
//    // 設定 Quick Reply list
//    @Override
//    public void setQuickReply(List<QuickReplyItem> quickReplyItemList) {
//        if (quickReplyItemList.isEmpty()) return;
//        binding.rvQuickReplyList.setVisibility(View.VISIBLE);
//        binding.messageRV.scrollToPosition(binding.messageRV.getAdapter().getItemCount() -1 );
//        binding.rvQuickReplyList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
//        QuickReplyAdapter quickReplyAdapter = new QuickReplyAdapter(quickReplyItemList);
//        quickReplyAdapter.setOnQuickReplyClickListener((type, data) -> {
//            chatViewModel.sendQuickReplyMessage(chatRoom.getId(), type, data);
//            binding.rvQuickReplyList.setVisibility(View.GONE);
//        });
//        binding.rvQuickReplyList.setAdapter(quickReplyAdapter);
//    }
//
//    @Override
//    public void onSendFacebookImageReplySuccess() {
//        closeFacebookTheme();
//    }
//
//    @Override
//    public void onSendFacebookImageReplyFailed(String errorMessage) {
//        closeFacebookTheme();
//        if (requireActivity() != null) {
//            requireActivity().runOnUiThread(() -> {
//                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
//            });
//        }
//    }
//
//
//    @Override
//    public void showFacebookOverTimeView() {
//        if (isAdded() && requireActivity() != null) {
//            requireActivity().runOnUiThread(() -> {
//                isFacebookReplyOverTime = true;
//                binding.chatKeyboardLayout.getFacebookOverTimeView().setVisibility(View.VISIBLE);
//            });
//        }
//    }
//
//
//    private Dialog sendVideoProgress;
//    ProgressSendVideoBinding sendVideoProgressBinding = null;
//    @Override
//    public void showSendVideoProgress(String message) {
//        if (sendFileSize == 0) return;
//        int screenWidth = CEUtils.getScreenWidth(requireActivity());
//        if (sendVideoProgressBinding == null) {
//            sendVideoProgressBinding = ProgressSendVideoBinding.inflate(LayoutInflater.from(requireContext()));
//        }
//        if (sendVideoProgress == null) {
//            sendVideoProgress = new Dialog(requireContext());
//            sendVideoProgress.setContentView(sendVideoProgressBinding.getRoot());
//            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth / 3,
//                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
//            sendVideoProgressBinding.getRoot().setLayoutParams(params);
//        } else {
//            sendVideoProgressBinding.progressBar.setProgress(0);
//        }
//        sendVideoProgress.setCancelable(false);
//        sendVideoProgressBinding.message.setText(message);
//        if (!sendVideoProgress.isShowing()) {
//            sendVideoProgress.show();
//        }
//    }
//
//    @Override
//    public void dismissSendVideoProgress() {
//        if (sendVideoProgress != null && sendVideoProgress.isShowing()) {
//            sendVideoProgress.dismiss();
//        }
//    }
//
//    @Override
//    public void updateSendVideoProgress() {
//        if (sendFileSize == 0) {
//            dismissSendVideoProgress();
//            addProgressValue = 0;
//            return;
//        }
//        if (sendVideoProgress != null && sendFileSize != 0) {
//            ProgressBar progressBar = sendVideoProgress.findViewById(R.id.progress_bar);
//            if (addProgressValue == 0) {
//                addProgressValue = 100 / sendFileSize;
//            }
//            progressBar.setProgress(progressBar.getProgress() + addProgressValue);
//            sendFileSize--;
//            if (sendFileSize == 0) {
//                dismissSendVideoProgress();
//                addProgressValue = 0;
//            }
//        }
//    }
//
//    @Override
//    public void updateSendVideoProgress(int progress) {
//        if (!isSendSingleFile) return;
//        if (sendVideoProgress == null) return;
//        ProgressBar progressBar = sendVideoProgress.findViewById(R.id.progress_bar);
//        progressBar.setProgress(progress);
//        if (progress >= 100) {
//            sendFileSize = 0;
//            dismissSendVideoProgress();
//        }
//    }
//
//    @Override
//    public void setServicedGreenStatus() {
//        binding.ivServicedStatus.setImageResource(R.drawable.circle_reeen_bg);
//    }
//
//    @Override
//    public void showLoadingView(int resId) {
//        if (getActivity() == null) return;
//        progressBar = IosProgressBar.show(getActivity(), resId, true, false, dialog -> {
//
//        });
//    }
//
//    @Override
//    public void hideLoadingView() {
//        if (progressBar != null && progressBar.isShowing()) {
//            progressBar.dismiss();
//        }
//    }
//
//    public ChatRoomEntity getChatRoom() {
//        return chatRoom;
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        this.undeadLineDrawable = true;
//        if (chatRoom != null) {
//            this.binding.chatKeyboardLayout.setChatRoomEntity(this.chatRoom, ChatRoomType.FRIEND_or_GROUP_or_DISCUS_or_SERVICE_MEMBER.contains(this.chatRoom.getType()), ChatRoomType.GROUP_or_DISCUSS_or_SERVICE_MEMBER.contains(this.chatRoom.getType()));
//            this.mentionSelectAdapter = new MentionSelectAdapter(requireActivity()).setUserProfiles(this.chatRoom.getMembersLinkedList());
////        ChatRoomEntity roomEntity = ChatRoomReference.findById(chatRoom.getId());
//        }
//
//        queryMemberIsBlock();
//        if (chatRoom != null) {
//            presenter.setVisible(true);
//            presenter.sendNoticeReaded();
//            setInputHint(); //second call
//            doServiceNumberServicedStatus(this.chatRoom);
////            checkHasNotEmployeeMember(DBManager.getInstance().findMembersByRoomId(chatRoom.getId()));
//        }
//        if (!isReply) setupToDefault();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (chatRoom != null) presenter.setVisible(false);
//        binding.chatKeyboardLayout.hideBottomPop();
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        InputLogBean bean = binding.chatKeyboardLayout.getInputHET().getUnfinishedEditBean();
//        bean.setTheme(this.isThemeOpen && !Strings.isNullOrEmpty(this.themeId));
//        bean.setId(this.themeId);
//
//        if (chatRoom != null) {
//            String unfinishedEdited = ChatRoomReference.getInstance().getUnfinishedEdited(chatRoom.getId());
//            InputLogBean localBean = InputLogBean.from(unfinishedEdited);
//            //草稿訊息
//            if(!localBean.getText().equals(bean.getText())) {
//                String roomId = chatRoom.getId();
//                String content = Strings.isNullOrEmpty(bean.getText().trim()) ? "" : bean.getText();
//                bean.setText(content);
//                ChatRoomReference.getInstance().updateUnfinishedEditedAndTimeById(roomId, bean.toJson());
//                chatRoom.setUnfinishedEdited(content);
//                EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REFRESH_ROOM_BY_LOCAL));
//            }
//            //發送失敗訊息
//            if(chatRoom.getFailedMessage()!= null) EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.REFRESH_ROOM_BY_LOCAL));
//        }
//    }
//
//    @Override
//    public void onDestroy() {
//
//        if (chatRoom != null) {
//            if (mainMessageData.size() > 0) {
//                presenter.clearUnReadNumber(mainMessageData.get(mainMessageData.size() - 1));
//            }
//            presenter.release();
//        }
//        EventBusUtils.unregister(this);
//        App.getInstance().chatRoomId = "";
//        mediaPreviewARL.unregister();
//        binding.funMedia.onDestroy();
//        if (binding.messageRV.getAdapter() != null) {
//            binding.messageRV.getAdapter().onDestroy();
//        }
//        super.onDestroy();
//    }
//
//    public void refreshListView() {
//        binding.messageRV.refreshData();
//    }
//
//    /**
//     * 移動訊息 Item UI 到指定可視位置
//     */
//    public void refreshDataAndscrollToPosition(int index) {
//        binding.messageRV.scrollToPosition(this.actionStatus.status(), index);
//    }
//
//    @Override
//    public void highLightUnReadLine(boolean needProcess) {
//        if (needProcess) {
//            ThreadExecutorHelper.getHandlerExecutor().execute(() -> {
//                int unReadIndex = this.mainMessageData.indexOf(new MessageEntity.Builder().id("以下為未讀訊息").build());
//                if (unReadIndex > 0) {
//                    MessageEntity unReadMessage = this.mainMessageData.get(unReadIndex);
//                    doScrollToSelectMessageItemAndSetKeyword("", unReadMessage);
//                }
//            }, 300L);
//        }
//    }
//
//    /**
//     * updateSendVideoProgress 影片轉碼發送流程
//     */
//    String tempProgress = "";
//    @Override
//    public void updateSendVideoProgress(boolean isConvert, String progress) {
//        int screenWidth = CEUtils.getScreenWidth(requireActivity());
//        if (sendVideoProgressBinding == null) {
//            sendVideoProgressBinding = ProgressSendVideoBinding.inflate(LayoutInflater.from(requireContext()));
//        }
//        if (sendVideoProgress == null) {
//            sendVideoProgress = new Dialog(requireContext());
//            sendVideoProgress.setContentView(sendVideoProgressBinding.getRoot());
//            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth / 3,
//                    ConstraintLayout.LayoutParams.WRAP_CONTENT);
//            sendVideoProgressBinding.getRoot().setLayoutParams(params);
//        } else {
//            sendVideoProgress.setCancelable(false);
//        }
//
//        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//            if(isConvert) {
//                sendVideoProgressBinding.message.setText(requireContext().getString(R.string.text_transferring_video_message));
//                sendVideoProgressBinding.progressBar.setProgress(Integer.parseInt(progress));
//
//                if (!sendVideoProgress.isShowing()) {
//                    sendVideoProgress.show();
//                }
//                if(Objects.equals(progress, "6666666")) {
//                    //轉碼完成接著發送
//                    sendVideoProgressBinding.message.setText(requireContext().getString(R.string.text_sending_video_message));
//                    sendVideoProgressBinding.progressBar.setProgress(0);
//                }
//            }else{
//                //發送影片進度
//                if(!progress.equals(tempProgress)) {
//                    tempProgress = progress;
//                    sendVideoProgressBinding.progressBar.setProgress(Integer.parseInt(progress));
//                    if (progress.equals("100")) {
//                        sendVideoProgress.dismiss();
//                    }
//                }
//            }
//        });
//    }
//
//    public void stopRefresh() {
//        binding.xrefreshLayout.completeRefresh();
//    }
//
//    public void updateAccountForMessage(UserProfileEntity mAccount) {
//        for (int i = 0; i < mainMessageData.size(); i++) {
//            MessageEntity msg = mainMessageData.get(i);
//            if (mAccount.getId().equals(msg.getSenderId())) {
//                if (mAccount.getAlias() != null && !"".equals(mAccount.getAlias())) {
//                    mainMessageData.get(i).setSenderName(mAccount.getAlias());
//                } else {
//                    mainMessageData.get(i).setSenderName(mAccount.getNickName());
//                }
//            }
//            refreshListView();
//        }
//    }
//
//    /**
//     * Jocket 收到信息會廣域通知到這裡
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void handleEvent(EventMsg eventMsg) {
////        this.advisoryRoomAdapter.handleEvent(eventMsg);
//
////        if (this.binding.lyChildChat.getVisibility() == View.VISIBLE) {
////            this.binding.lyChildChat.handleEvent(eventMsg);
////        }
//
//        binding.chatKeyboardLayout.handleEvent(eventMsg);
//
//        switch (eventMsg.getCode()) {
////            case MsgConstant.NOTICE_REFRESH_MENTION_DATA:
////                List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(chatRoom.getId());
////
////                break;
//////                this.mentionSelectAdapter = new MentionSelectAdapter(requireActivity()).setUserProfiles(this.chatRoom.getMembersLinkedList());
//////                this.mentionSelectAdapter.refreshData();
//////                break;
//            case MsgConstant.SCROLL_TO_TARGET_MESSAGE_POSITION:
//                Map<String, String> scrollData = JsonHelper.getInstance().from(eventMsg.getString(), Map.class);
//                if (scrollData.get("roomId").equals(this.chatRoom.getId())) {
//                    int unReadIndex = this.mainMessageData.indexOf(new MessageEntity.Builder().id(scrollData.get("messageId")).build());
//                    if (unReadIndex > 0) {
//                        MessageEntity unReadMessage = this.mainMessageData.get(unReadIndex);
//                        doScrollToSelectMessageItemAndSetKeyword("", unReadMessage);
//                    }
//                }
//                break;
//            case MsgConstant.NOTICE_NEW_MESSAGE_CHECKING_SEVICENUMBER_SERVICED:
////                if (eventMsg.getString().equals(this.chatRoom.getId()) && ChatRoomType.SERVICES.equals(this.chatRoom.getType())) {
////                    if (Strings.isNullOrEmpty(this.chatRoom.getServiceNumberAgentId())) {
////                        doFromAppoint(this.chatRoom);
////                    }
////                }
//                break;
//            case MsgConstant.NOTICE_APPEND_NEW_MESSAGE_IDS:
//                List<String> appendNewMessageIds = JsonHelper.getInstance().fromToList(eventMsg.getString(), String[].class);
//                if (chatRoom != null) {
//                    List<MessageEntity> localEntities = MessageReference.findByIdsAndRoomId(null, appendNewMessageIds.toArray(new String[appendNewMessageIds.size()]), chatRoom.getId());
//                    for (MessageEntity entity : localEntities) {
//                        if (entity.getTag() != null && !entity.getTag().isEmpty()) {
//                            FacebookTag facebookTag = JsonHelper.getInstance().from(entity.getTag(), FacebookTag.class);
//                            if (facebookTag.getData().getReplyType() != null && !facebookTag.getData().getReplyType().isEmpty()) {
//                                if ("private".equals(facebookTag.getData().getReplyType())) {
//                                    String commentId = facebookTag.getData().getCommentId();
//                                    if (commentId == null) continue;
//                                    List<MessageEntity> targetMessageList = mainMessageData.stream().filter(message -> (message.getTag() != null && !message.getTag().isEmpty()) && message.getTag().contains(commentId)).toList();
//                                    targetMessageList.forEach( message -> {
//                                        MessageReference.updateFacebookPrivateReplyStatus(chatRoom.getId(), message.getId(), true);
//                                        message.setFacebookPrivateReplied(true);
//                                        updateFacebookStatus(message);
//                                    });
//                                }
//                            }
//                        }
//                        displayMainMessage(true, false, true, entity, chatRoom.getType() == ChatRoomType.friend);
//                    }
//                }
//                break;
//            case MsgConstant.NOTICE_APPEND_OFFLINE_MESSAGE_IDS:
//                List<String> appendOffLineMessageIds = JsonHelper.getInstance().fromToList(eventMsg.getString(), String[].class);
//                if (chatRoom != null) {
//                    List<MessageEntity> localEntities = MessageReference.findByIdsAndRoomId(null, appendOffLineMessageIds.toArray(new String[appendOffLineMessageIds.size()]), chatRoom.getId());
//                    for (MessageEntity entity : localEntities) {
//                        displayMainMessage(false, false, false, entity, chatRoom.getType() == ChatRoomType.friend);
//                    }
//                }
//                break;
//            case MsgConstant.NOTICE_APPEND_MESSAGE: // EVAN_FLAG 2020-08-14 (1.12.0) 補訊插入
//                MessageEntity appendMessage = JsonHelper.getInstance().from(eventMsg.getString(), MessageEntity.class);
//                if (chatRoom != null && appendMessage != null) {
//                    if (chatRoom.getId().equals(appendMessage.getRoomId())) {
////                        CELog.i("補訓插入::" + eventMsg.getString());
//                        displayMainMessage(false, false, false, appendMessage, chatRoom.getType() == ChatRoomType.friend);
//                    }
//                }
//                break;
//            case MsgConstant.SEND_PHOTO_MEDIA_SELECTOR:
//                String selectorJson = eventMsg.getString();
//                Map<String, String> selectorData = JsonHelper.getInstance().fromToMap(selectorJson);
//                String isOriginalStr = selectorData.get("isOriginal");
//                String listStr = selectorData.get("list");
//
//                List<ImageBean> list = JsonHelper.getInstance().fromToList(listStr, ImageBean[].class);
//                sendFileSize = list.size();
//                isSendSingleFile = sendFileSize == 1;
//                CELog.e(selectorJson);
//                executeSendPhotos(Lists.newArrayList(list), Boolean.parseBoolean(isOriginalStr));
//                break;
//            case MsgConstant.NOTICE_EXECUTION_BUSINESS_CREATE_ACTION:
//                String path = eventMsg.getString();
//                executeTask(Lists.newArrayList(), path, false);
//                break;
//            case MsgConstant.NOTICE_EXECUTION_BUSINESS_BINDING_ROOM_ACTION:
//                String json = eventMsg.getData().toString();
//                Map<String, Object> executionData = JsonHelper.getInstance().fromToMap(json);
//                BusinessContent businessContent = JsonHelper.getInstance().from(executionData.get("business"), BusinessContent.class);
//                CELog.i("");
//                bindingBusiness((String) executionData.get("roomId"), businessContent);
//                break;
//            case MsgConstant.ACCOUNT_REFRESH_FILTER:
//                if (chatRoom != null) {
//                    UserProfileEntity userProfile = (UserProfileEntity) eventMsg.getData();
//                    List<UserProfileEntity> members = chatRoom.getMembers();
//                    if (members != null && !members.isEmpty()) {
//                        Iterator<UserProfileEntity> iterator = members.iterator();
//                        boolean hasUser = false;
//                        while (iterator.hasNext()) {
//                            UserProfileEntity user = iterator.next();
//                            if (user.getId().equals(userProfile.getId())) {
//                                iterator.remove();
//                                hasUser = true;
//                                break;
//                            }
//                        }
//                        if (hasUser) {
//                            chatRoom.getMembers().add(userProfile);
//                        }
//                        binding.messageRV.refreshData();
//                    }
//                }
//                break;
//            case MsgConstant.REFRESH_FILTER:
//                if (!presenter.recordMode) {
//                    presenter.messageListRequestAsc(getLastMessage());
//                }
//                break;
//            case MsgConstant.CLEAN_MSGS_FILTER:
//                onCleanMsgs();
//                break;
//            case MsgConstant.REMOVE_FRIEND_FILTER:
//                UserProfileEntity account = (UserProfileEntity) eventMsg.getData();
//                if (account != null && account.getRoomId().equals(chatRoom.getId())) {
//                    finishActivity();
//                }
//                break;
//            case MsgConstant.INTERNET_STSTE_FILTER:
//                // EVAN_FLAG 2020-02-18 1.10.0 暫時拔除 linphone
////                clearCall();
//                List<MessageEntity> iMessages = DBManager.getInstance().dimQueryLastReadMsg(chatRoom.getId());
//                if (iMessages != null && iMessages.size() > 0) {
//                    presenter.messageListRequestAsc(iMessages.get(0));
//                }
//                break;
//            case MsgConstant.SESSION_REFRESH_FILTER:
//                try {
//                    Map<String, String> data = JsonHelper.getInstance().fromToMap(eventMsg.getString());
//                    String key = data.get("key");
//                    String values = data.get("values");
//                    String roomId = data.get("roomId");
//                    if (key == null || TextUtils.isEmpty(key) || !chatRoom.getId().equals(roomId)) {
//                        return;
//                    }
//                    switch (key) {
//                        case "avatarUrl":
//                            chatRoom.setAvatarId(values);
//                            refreshPager(chatRoom);
//                            break;
//                        case "name":
//                            chatRoom.setName(values);
//                            refreshPager(chatRoom);
//                            break;
//                        case "ownerId":
//                            chatRoom.setOwnerId(values);
//                            refreshPager(chatRoom);
//                            hideNoOwnerNotify();
//                            break;
//                        default:
//                            refreshPager(chatRoom);
//                            break;
//                    }
//                } catch (Exception e) {
//                    CELog.e(e.getMessage());
//                }
//
//                break;
//
//            case MsgConstant.CANCEL_FILTER:
//                presenter.mIsCancel = true;
//                cancel();
//                break;
//            case MsgConstant.UPDATE_PROFILE:
//                // Ian-note: 這個地方根本廣播就進不來，不知道為什麼寫了一堆沒機會用到的 code 在此
//                UpdateProfileBean mUpdateProfileBean = (UpdateProfileBean) eventMsg.getData();
//                String title = mUpdateProfileBean.getTitle();
//                String userId = mUpdateProfileBean.getUserId();
//                String roomId = mUpdateProfileBean.getRoomId();
//
//                if (chatRoom != null) {
//                    List<UserProfileEntity> members = ChatMemberCacheService.getChatMember(chatRoom.getId());
////                    List<UserProfileEntity> members = DBManager.getInstance().findMembersByRoomId(chatRoom.getId());
//                    if (!members.isEmpty()) {
//                        chatRoom.setMembers(members);
//                        binding.messageRV.refreshData();
//                    }
//                }
//
//
//                //单聊&&在当前聊天室
//                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(userId) && !TextUtils.isEmpty(roomId)
//                        && chatRoom != null && chatRoom.getType() == ChatRoomType.friend
//                        && chatRoom.getId().equals(roomId)) {
//                    //改标题
//                    EventBusUtils.sendEvent(new EventMsg(MsgConstant.CHAT_TITLE_FILTER, title));
//                    //改发送者名称
//                    updateSenderName(title, userId);
////                    chatRoom.setSenderName(title);
//                    chatRoom.setName(title);
//                }
//
//                // EVAN_FLAG 2019-12-17 (1.8.0) 更新該聊天室標註訊息內容
//                binding.chatKeyboardLayout.onUpdateProfile(roomId);
//                break;
//            case MsgConstant.MSG_RECEIVED_FILTER:
//                MessageEntity receiverMessage = JsonHelper.getInstance().from(eventMsg.getString(), MessageEntity.class);
//                if (receiverMessage != null) {
//                    presenter.onReceive(receiverMessage);
//                }
//                break;
//            case MsgConstant.MSG_STATUS_FILTER:
//                MsgStatusBean mMsgStatusBean = (MsgStatusBean) eventMsg.getData();
//                String messageId = mMsgStatusBean.getMessageId();
//                int sendNum = mMsgStatusBean.getSendNum();
//                long sendTime = mMsgStatusBean.getSendTime();
//                if (sendTime <= 0) {
//                    sendTime = System.currentTimeMillis();
//                }
//
//                if (sendNum < 0) {
////                    chatRoom.setFailNum(chatRoom.getFailNum() + 1);
//                }
//                updateMsgStatus(messageId, sendNum, sendTime);
//                break;
//            case MsgConstant.MSG_NOTICE_FILTER:
//                MsgNoticeBean mMsgNoticeBean = (MsgNoticeBean) eventMsg.getData();
//                String messageId1 = mMsgNoticeBean.getMessageId();
//                int receivedNum1 = mMsgNoticeBean.getReceivedNum();
//                int readNum1 = mMsgNoticeBean.getReadNum();
//                int sendNum1 = mMsgNoticeBean.getSendNum();
//                updateMsgNotice(messageId1, receivedNum1, readNum1, sendNum1);
//                break;
//            case MsgConstant.USER_EXIT:
//                UserExitBean mUserExitBean = (UserExitBean) eventMsg.getData();
//                String roomId1 = mUserExitBean.getRoomId();
//                String title1 = mUserExitBean.getTitle();
//                String imageUrl1 = mUserExitBean.getImageUrl();
//                String userId1 = mUserExitBean.getUserId();
//                AccountRoomRelReference.deleteRelByRoomIdAndAccountId(null, roomId1, userId1);
////                DBManager.getInstance().delAccount_Room(roomId1, userId1);
//                if (chatRoom.getId().equals(mUserExitBean.getRoomId())) {
//                    chatRoom.setName(title1);
//                    chatRoom.setAvatarId(imageUrl1);
//                    refreshPager(chatRoom);
//
//                    if (chatRoom.getOwnerId().equals(mUserExitBean.getUserId())) {
//                        chatRoom.setOwnerId("");
//                    }
//                    getMemberPrivilege();
//                }
//                break;
//            case MsgConstant.SEND_UPDATE_AVATAR:
//                UpdateAvatarBean mUpdateAvatarBean = (UpdateAvatarBean) eventMsg.getData();
//                String avatar = mUpdateAvatarBean.getAvatar();
//                if (avatar == null || "".equals(avatar)) {
//                    return;
//                }
//                boolean needRefresh = false;
//                if (NoticeCode.UPDATE_USER_AVATAR.getName().equals(mUpdateAvatarBean.getType())) {
//
////                if (JocketManager.UPDATE_USER_AVATAR.equals(mUpdateAvatarBean.getType())) {
//                    for (int i = 0; i < mainMessageData.size(); i++) {
//                        MessageEntity msg = mainMessageData.get(i);
//                        if (mUpdateAvatarBean.getUserId().equals(msg.getSenderId())) {
//                            msg.setAvatarId(avatar);
//                            Log.e(TAG, " message sender avatar id ");
////                            msg.setAvatarId(NetConfig.getInstance().assembleAvatarUrl(avatar, PicSize.SMALL));
//                            needRefresh = true;
//                        }
//                    }
//                    if (needRefresh) {
//                        refreshListView();
//                    }
//                }
//                break;
////            case MsgConstant.SERVICE_NUMBER_PERSONAL_START:
////                Map<String, String> data = (Map) eventMsg.getData();
////                String _roomId = data.get("roomId");
//////                String serviceNumberAgentId = data.get("serviceNumberAgentId");
//////                String _userId = TokenPref.getInstance(requireActivity()).getUserId();
//////                this.chatRoom.setServiceNumberAgentId(serviceNumberAgentId);
////                if (this.chatRoom.getId().equals(_roomId)) {
////                    doFromAppoint(this.chatRoom);
//////                    if (ServiceNumberType.PROFESSIONAL.equals(this.chatRoom.getServiceNumberType()) && !_userId.equals(serviceNumberAgentId)) {
//////                        setupAppointStatus(appointResp);
//////                    } else {
//////                        setupAppointStatus(appointResp);
//////                    }
////                }
////                break;
//            case MsgConstant.NOTICE_SERVICE_NUMBER_CONSULT_EVENT:
//                Map<String, String> data = JsonHelper.getInstance().fromToMap(eventMsg.getString());
//                String selfId = TokenPref.getInstance(requireContext()).getUserId();
//                String consultRoomId = data.get("consultRoomId");
//                String transferReason = data.get("transferReason");
//
//                // 移除諮詢服務
//                if ("ConsultComplete".equals(data.get("event"))) {
//                    chatViewModel.removeConsultRoom(consultRoomId);
//                } else if ("ConsultStart".equals(data.get("event"))) {
//                    // 加入諮詢服務
//                    if (selfId.equals(data.get("userId")) && chatRoom.getServiceNumberAgentId().equals(data.get("userId"))
//                    && chatRoom.getId().equals(data.get("srcRoomId"))) {
//                        chatViewModel.addConsultRoom(consultRoomId);
//                    }
//                }
//                break;
//            case MsgConstant.SERVICE_NUMBER_PERSONAL_START:
//                Map<String, String> map = (Map<String, String>) eventMsg.getData();
//                if (chatRoom.getId().equals(map.get("roomId"))) {
//                    if (!getUserId().equals(map.get("serviceNumberAgentId"))) {
//                        doServiceNumberServicedStatus(this.chatRoom);
//                        chatViewModel.getProvisionalMember(chatRoom.getId());
//                    }
//                }
//                break;
//            case MsgConstant.SERVICE_NUMBER_TRANSFER_STATUS:
//            case MsgConstant.SERVICE_NUMBER_PERSONAL_STOP:
//                String targetRoomId = (String)eventMsg.getData();
//                if (chatRoom.getId().equals(targetRoomId)) {
//                    doServiceNumberServicedStatus(this.chatRoom);
//                    chatViewModel.getProvisionalMember(chatRoom.getId());
//                }
//                break;
//            case MsgConstant.APPOINT_STATUS_CHECKING:
//                String rId = (String) eventMsg.getData();
//                if (this.chatRoom.isService(getUserId())) {
////                    binding.chatKeyboardLayout.setBackgroundColor(Color.parseColor("#F1F4F5"));
//                    if (this.chatRoom.getId().equals(rId)) {
//                        doAppointStatus(rId);
//                    }
//                }
//                break;
//            case MsgConstant.UPDATE_MESSAGE_STATUS:
//                if (eventMsg.getData() instanceof MessageEntity) {
//                    MessageEntity msg = (MessageEntity) eventMsg.getData();
//                    int position = this.mainMessageData.indexOf(msg);
//                    this.mainMessageData.get(position).setContent(msg.content().toStringContent());
//                    if (position >= 0) {
//                        binding.messageRV.refreshData(position, msg);
//                    }
//                }
//                break;
//            case MsgConstant.BUSINESS_BINDING_ROOM_EVENT:
//                Map<String, String> datas = (Map<String, String>) eventMsg.getData();
//                String bindRoomId = datas.get("roomId");
//                if (ChatRoomType.FRIEND_or_DISCUSS.contains(this.chatRoom.getType()) && this.chatRoom.getId().equals(bindRoomId)) {
//                    bindingBusiness(bindRoomId,
//                            new BusinessContent(datas.get("businessId"), datas.get("businessName"), BusinessCode.of(datas.get("businessCode"))), false
//                    );
//                }
//                break;
//            case MsgConstant.NOTICE_ROBOT_SERVICE_WARNED:
//                AiServiceWarnedSocket aiServiceWarnedSocket =  JsonHelper.getInstance().from(eventMsg.getData(), AiServiceWarnedSocket.class);
//                if (chatRoom.getServiceNumberStatus().equals(ServiceNumberStatus.ROBOT_SERVICE) && chatRoom.getId().equals(aiServiceWarnedSocket.getRoomId())) {
//                    chatRoom.setWarned(!aiServiceWarnedSocket.getContent().getCancel());
//                    setAiServices();
//                }
//                break;
//            case MsgConstant.NOTICE_PROVISIONAL_MEMBER_REMOVED:
//                Set<String> idSets = (Set<String>) eventMsg.getData();
//                Iterator<String> existIds = chatRoom.getProvisionalIds().iterator();
//                while (existIds.hasNext()) {
//                    for (String id : idSets) {
//                        if (existIds.next().equals(id)) {
//                            existIds.remove();
//                            break;
//                        }
//                    }
//                }
//                presenter.initProvisionalMemberList(chatRoom.getProvisionalIds());
//                break;
//            case MsgConstant.NOTICE_PROVISIONAL_MEMBER_ADDED:
//            case MsgConstant.NOTICE_SERVICE_NUMBER_ADD_FROM_PROVISIONAL:
//                ProvisionalMemberAddedSocket provisionalMemberAddedSocket = JsonHelper.getInstance().from(eventMsg.getData(), ProvisionalMemberAddedSocket.class);
//                if (provisionalMemberAddedSocket.getRoomId().equals(chatRoom.getId())) {
//                    chatViewModel.getProvisionalMember(provisionalMemberAddedSocket.getRoomId());
//                }
//                break;
//            case MsgConstant.GROUP_REFRESH_FILTER: // 刷新成員列表
////                try {
////                    GroupRefreshBean refreshBean = JsonHelper.getInstance().from(eventMsg.getData(), GroupRefreshBean.class);
////                    if (refreshBean.getSessionId().equals(chatRoom.getId())) {
////                        chatViewModel.getRoomItem(refreshBean.getSessionId());
////                        chatViewModel.getChatMember(refreshBean.getSessionId());
////                    }
////                }catch (Exception e) {
////                    Log.e("ChatFragment", "error="+e.getMessage());
////                }
//                break;
//            case MsgConstant.MESSAGE_QUICK_REPLY:
//                Type listType = new TypeToken<List<QuickReplyItem>>(){}.getType();
//                QuickReplySocket quickReplySocket = JsonHelper.getInstance().from(eventMsg.getData().toString(), QuickReplySocket.class);
//                if (quickReplySocket.getRoomId().equals(chatRoom.getId())) {
//                    setQuickReply(quickReplySocket.getItems());
//                }
//                break;
//            case MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_IMAGE: // 引用AI諮詢聊天室圖片
//                String quotedImage = (String)eventMsg.getData();
//                sendAiQuoteImage(quotedImage);
//                break;
//            case MsgConstant.MESSAGE_AI_CONSULTATION_QUOTED_VIDEO: // 引用AI諮詢聊天室影片
//                String quotedVideo = (String)eventMsg.getData();
//                sendAiQuoteVideo(quotedVideo);
//                break;
//            case MsgConstant.NOTICE_CLEAR_CHAT_ROOM_ALL_MESSAGE: //清空聊天紀錄
//                binding.messageRV.getAdapter().clearMessage();
//                break;
//            case MsgConstant.UPDATE_SERVICE_BADGE_NUMBER_EVENT:
//                BadgeDataModel badgeDataModel = (BadgeDataModel) eventMsg.getData();
//                if (advisoryRoomAdapter != null) {
//                    advisoryRoomAdapter.shouldNotifyUnreadIcon(badgeDataModel.getRoomId());
//                }
//                break;
//            case MsgConstant.FACEBOOK_COMMENT_DELETE:
//                // Facebook 留言刪除
//            case MsgConstant.FACEBOOK_COMMENT_UPDATE:
//                // Facebook 留言編輯
//                String updateRoomId = ((JSONObject)eventMsg.getData()).optString("roomId");
//                if (!updateRoomId.equals(chatRoom.getId())) return;
//                String commentId = ((JSONObject)eventMsg.getData()).optString("commentId");
//                String event = ((JSONObject)eventMsg.getData()).optString("event");
//                FacebookCommentStatus facebookCommentStatus = FacebookCommentStatus.of(event);
//                for (int i = 0; i < mainMessageData.size(); i++) {
//                    MessageEntity message = mainMessageData.get(i);
//                    if (message.getTag() != null) {
//                        FacebookTag facebookTag = JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
//                        if (facebookTag.getData() == null) continue;
//                        if (facebookTag.getData().getCommentId() == null) continue;
//                        if (facebookTag.getData().getCommentId().equals(commentId)) {
//                            if (facebookCommentStatus == FacebookCommentStatus.Delete) {
//                                if (isThemeOpen && message.getId().equals(themeId)) {
//                                    closeFacebookTheme();
//                                    Toast.makeText(requireContext(), getString(R.string.facebook_comment_deleted), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                            message.setFacebookCommentStatus(facebookCommentStatus);
//                            presenter.refreshFacebookComment(message, facebookCommentStatus, mainMessageData);
//                            return;
//                        }
//                    }
//                }
//                break;
//
//            case MsgConstant.FACEBOOK_POST_DELETE:
//                // Facebook 貼文刪除
//                JSONArray roomIds = ((JSONObject)eventMsg.getData()).optJSONArray("roomIds");
//                if (!roomIds.toString().contains(chatRoom.getId())) return;
//                String postId = ((JSONObject)eventMsg.getData()).optString("postId");
//                String postEvent = ((JSONObject)eventMsg.getData()).optString("event");
//                for (int i = 0; i < mainMessageData.size(); i++) {
//                    MessageEntity message = mainMessageData.get(i);
//                    if (message.getTag() != null) {
//                        FacebookTag facebookTag = JsonHelper.getInstance().from(message.getTag(), FacebookTag.class);
//                        if (facebookTag.getData() == null) continue;
//                        if (facebookTag.getData().getPostId() == null) continue;
//                        if (facebookTag.getData().getPostId().equals(postId)) {
//                            if (isThemeOpen && message.getId().equals(themeId)) {
//                                closeFacebookTheme();
//                                Toast.makeText(requireContext(), getString(R.string.facebook_post_deleted), Toast.LENGTH_SHORT).show();
//                            }
//
//                            message.setFacebookPostStatus(FacebookPostStatus.of(postEvent));
//                            MessageReference.updateFacebookPostStatus(chatRoom.getId(), message.getId(), FacebookPostStatus.Delete);
//                            binding.messageRV.notifyChange(message);
//                            presenter.updateFacebookPostStatus(message, mainMessageData);
//                        }
//                    }
//                }
//                break;
//            case MsgConstant.ON_FACEBOOK_PRIVATE_REPLY:
//                String facebookReplyId = (String)eventMsg.getData();
//                MessageEntity facebookMessage = MessageReference.findById(facebookReplyId);
//                binding.messageRV.notifyChange(facebookMessage);
//                break;
//            case MsgConstant.UPDATE_LINE_CUSTOMER_AVATAR:
//                binding.messageRV.refreshData();
//                break;
//            case MsgConstant.Do_UPDATE_CONTACT_BY_LOCAL:
//                String contactId = (String)eventMsg.getData();
//                if(binding.messageRV.getAdapter()!=null)
//                    binding.messageRV.getAdapter().updateMessageList(contactId);
//                break;
//            case MsgConstant.UPDATE_TODO_EXPIRED_COUNT_EVENT:
//            case MsgConstant.UI_NOTICE_TODO_REFRESH:
//                chatViewModel.getConsultTodoList(chatRoom.getType(), chatRoom.getId());
//                break;
//            default:
//                break;
//        }
//    }
//
//    /**
//     * 打開模式定義共用function，後其從夠定義列舉函數
//     */
//    private void openBottomRichMenu(RichMenuBottom type, OpenBottomRichMeunType action, List<RichMenuBottom> gridMenus) {
//        switch (action) {
//            case RANGE_SELECTION:
//                binding.messageRV.setAdapterMode(MessageAdapterMode.RANGE_SELECTION);
//                // 先關閉背景點擊，CheckBox太小會誤觸發
//                binding.xrefreshLayout.setOnBackgroundClickListener(null);
//                binding.chatKeyboardLayout.setRichMenuGridCount(gridMenus.size()).setOnItemClickListener(null, gridMenus, Lists.newArrayList(), new BottomRichMeunAdapter.OnItemClickListener() {
//                    @Override
//                    public void onClick(MessageEntity msg, RichMenuBottom menu, int position) {
//                        actionStatus = ActionStatus.SCROLL;
//                        switch (menu) {
//                            case ANONYMOUS:
//                                MessageDomino.clear();
//                                binding.messageRV.switchAnonymous();
//                                break;
//                            case PREVIEW:
//                            case NEXT:
//                            case SAVE:
//                            case SHARE:
//                            case CONFIRM:
//                                executeScreenshots(RichMenuBottom.NEXT.equals(menu), menu);
//                                binding.clBottomServicedBar.setVisibility(View.VISIBLE);
//                                break;
//                            case CANCEL:
//                                clearScreenshotsFunction();
//                                binding.clBottomServicedBar.setVisibility(View.VISIBLE);
//                                break;
//                        }
//                    }
//
//                    @Override
//                    public void onCancle() {
//                        actionStatus = ActionStatus.SCROLL;
//                        binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT);
//                    }
//                }, null);
//                binding.chatKeyboardLayout.hideKeyboard();
//                break;
//            case MULTIPLE_SELECTION:
//                binding.messageRV.setAdapterMode(MessageAdapterMode.SELECTION);
//                // 先關閉背景點擊，CheckBox太小會誤觸發
//                binding.xrefreshLayout.setOnBackgroundClickListener(null);
//                binding.chatKeyboardLayout.setRichMenuGridCount(2).
//                        setOnItemClickListener(null, gridMenus, Lists.newArrayList(), new BottomRichMeunAdapter.OnItemClickListener() {
//                                    @Override
//                                    public void onClick(MessageEntity msg, RichMenuBottom menu, int position) {
//                                        actionStatus = ActionStatus.SCROLL;
//                                        if (position == 1) {
//                                            for (MessageEntity m : mainMessageData) {
//                                                m.setDelete(false);
//                                            }
//                                            presenter.isObserverKeboard = false;
//                                            binding.chatKeyboardLayout.showKeyboard();
//                                        } else if (position == 0) {
//                                            // EVAN_REFACTOR: 2019-09-09 選取後邏輯過濾。
//                                            List<MessageEntity> messages = Lists.newArrayList();
//                                            for (MessageEntity m : mainMessageData) {
//                                                if (m.isDelete()) {
//                                                    messages.add(m);
//                                                }
//                                            }
//
//                                            if (messages.isEmpty()) {
//                                                Toast.makeText(getCtx(), "至少選擇一筆信息", Toast.LENGTH_SHORT).show();
//                                                return;
//                                            }
//
//                                            switch (type) {
//                                                case MULTI_TRANSPOND:
//                                                    executeTranspond(messages);
//                                                    break;
//                                                case MULTI_COPY:
//                                                    executeCopy(messages);
//                                                    break;
//                                                case TASK:
//                                                    executeTask(messages, "", false);
//                                                    break;
//                                            }
//                                            for (MessageEntity m : mainMessageData) {
//                                                m.setDelete(false);
//                                            }
//                                            presenter.isObserverKeboard = false;
//                                            binding.chatKeyboardLayout.showKeyboard();
//                                        }
//
//                                        binding.xrefreshLayout.setOnBackgroundClickListener(ChatFragment.this);
//                                        binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT);
//                                    }
//
//                                    @Override
//                                    public void onCancle() {
//                                        actionStatus = ActionStatus.SCROLL;
//                                        binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT);
//                                    }
//                                }
//                                , null);
//
//                binding.chatKeyboardLayout.hideKeyboard();
//                break;
//            case DIRECT_EXECUTION:
//                break;
//        }
//    }
//
//    /**
//     * 處理送出圖片
//     */
//    private void executeSendPhotos(List<AMediaBean> list, boolean isOriginal) {
//        if (list == null || list.size() == 0) {
//            Toast.makeText(requireActivity(), getString(R.string.text_select_image_first_please), Toast.LENGTH_SHORT).show();
//            return;
//        }
////        showSendVideoProgress("圖片發送中");
//        for (int i = 0; i < list.size(); i++) {
//            int fileType = FileUtil.getFileType(list.get(i).getPath());
//            switch (fileType) {
//                case Global.FileType_Png:
//                case Global.FileType_Jpg:
//                case Global.FileType_jpeg:
//                case Global.FileType_bmp:
//                    PictureParse.parsePath(getCtx(), list.get(i).getPath(), isOriginal, new ServiceCallBack<String[], Enum>() {
//                        @Override
//                        public void complete(String[] path, Enum anEnum) {
////                            updateSendVideoProgress();
//                            presenter.sendImage(path[0], path[1], isFacebookReplyPublic);
//                        }
//
//                        @Override
//                        public void error(String message) {
//                            updateSendVideoProgress();
//                            CELog.e(message);
//                        }
//                    });
//                    break;
//                case Global.FileType_gif:
//                    BitmapBean bitmapBean = PictureParse.parseGifPath(getCtx(), list.get(i).getPath());
//                    presenter.sendGifImg(bitmapBean.url, list.get(i).getPath(), bitmapBean.width, bitmapBean.height);
//                    break;
//                case Global.FileType_mp4:
//                    IVideoSize iVideoSize = new VideoSizeFromVideoFile(list.get(i).getPath());
//                    presenter.sendVideo(iVideoSize);
//                    break;
//                default:
//                    presenter.sendFile(list.get(i).getPath());
//                    break;
//            }
//        }
//    }
//
//    /**
//     * 處理送出影片
//     */
//    private void executeSendVideos(List<AMediaBean> list, boolean isOriginal) {
//        if (list == null || list.size() == 0) {
//            Toast.makeText(requireActivity(), getString(R.string.text_select_video_first_please), Toast.LENGTH_SHORT).show();
//            return;
//        }
//        for (int i = 0; i < list.size(); i++) {
//            int fileType = FileUtil.getFileType(list.get(i).getPath());
//            switch (fileType) {
//                case Global.FileType_mp4:
//                    IVideoSize iVideoSize = new VideoSizeFromVideoFile(list.get(i).getPath());
//                    presenter.sendVideo(iVideoSize);
//                    break;
//                default:
//                    presenter.sendFile(list.get(i).getPath());
//                    break;
//            }
//        }
//    }
//
//    /**
//     * 執行多筆轉發
//     * 完成多筆刪除邏輯。
//     * date 2019/09/10
//     */
//    private void executeTranspond(List<MessageEntity> messages) {
//        ActivityTransitionsControl.toTransfer(getCtx(), messages, Lists.newArrayList(chatRoom.getId()), (intent, s) -> {
//            intent.putExtra(BundleKey.ROOM_ID.key(), this.chatRoom.getId());
//            //if (getActivity() != null) getActivity().finish();
//            IntentUtil.INSTANCE.start(requireContext(), intent);
//        });
//    }
//
//    /**
//     * 執行多筆刪除
//     * 完成進階選單刪除多筆信息邏輯。
//     * date 2019/09/10
//     */
//    private void executeDelete(List<MessageEntity> messages) {
//        new AlertView.Builder()
//                .setContext(getCtx())
//                .setStyle(AlertView.Style.Alert)
//                .setMessage("僅會從您的消息記錄中刪除，不會刪除對方的消息記錄。")
//                .setOthers(new String[]{"取消", "刪除"})
//                .setOnItemClickListener((o, position) -> {
//                    if (position == 1) {
//                        // EVAN_FLAG 2019-12-16 (1.8.0) William 額外需求，去除主題聊天是訊息，禁止刪除
//                        Iterator<MessageEntity> iterator = messages.iterator();
//                        boolean hasThemeMessage = false;
//                        while (iterator.hasNext()) {
//                            MessageEntity m = iterator.next();
//                            if (checkIsThemeMessage(m)) {
//                                iterator.remove();
//                                hasThemeMessage = true;
//                                Toast.makeText(requireActivity(), "有主題訊息不可被刪除", Toast.LENGTH_SHORT).show();
//                                continue;
//                            }
//                            if (!Strings.isNullOrEmpty(m.getNearMessageId())) {
//                                iterator.remove();
//                                hasThemeMessage = true;
//                            }
//                        }
//
//                        if (hasThemeMessage) {
//                            Toast.makeText(requireActivity(), "有主題訊息不可被刪除", Toast.LENGTH_SHORT).show();
//                        }
//
//                        if (messages.isEmpty()) {
//                            return;
//                        }
//
//                        presenter.deleteMessages(messages);
//                    }
//                })
//                .build()
//                .setCancelable(true)
//                .show();
//    }
//
//    /**
//     * 檢查是否為主題訊息
//     */
//    private boolean checkIsThemeMessage(MessageEntity msg) {
//        for (MessageEntity mainMessageDatum : mainMessageData) {
//            if (msg.getId().equals(mainMessageDatum.getThemeId())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
////    /**
////     * 是否為主題最後一筆
////     */
////    private boolean checkIsNearMessage(MessageEntity msg) {
////        Iterator<MessageEntity> iterator = mainMessageData.iterator();
////        while (iterator.hasNext()) {
////            if (msg.getId().equals(iterator.next().getNearMessageId())) {
////                return true;
////            }
////        }
////        return false;
////    }
//
//    /**
//     * 執行多筆收回
//     * 完成多筆收回邏輯。
//     * date 2019/09/10
//     */
//    int executeRetractCount = 0;
//
//    private void executeRecover(List<MessageEntity> messages) {
//        if (messages == null) {
//            return;
//        }
//
//        // EVAN_FLAG 2019-12-16 (1.8.0) William 額外需求，去除主題聊天是訊息，禁止回收
//        boolean hasThemeMessage = false;
//        Iterator<MessageEntity> iterator = messages.iterator();
//        while (iterator.hasNext()) {
//            MessageEntity m = iterator.next();
//            if (checkIsThemeMessage(m)) {
//                iterator.remove();
//                Toast.makeText(requireActivity(), "有主題訊息不可被收回", Toast.LENGTH_SHORT).show();
//                continue;
//            }
//
//            if (!Strings.isNullOrEmpty(m.getNearMessageId())) {
//                iterator.remove();
//                hasThemeMessage = true;
//            }
//        }
//
//        if (hasThemeMessage) {
//            Toast.makeText(requireActivity(), "有主題訊息不可被收回", Toast.LENGTH_SHORT).show();
//        }
//
//        if (messages.isEmpty()) {
//            return;
//        }
//
//        showLoadingView(R.string.wording_retracting);
//        executeRetractCount = 0;
//        for (MessageEntity m : messages) {
//            ApiManager.doMessageRetract(getCtx(), chatRoom.getId(), m.getId(), new ApiListener<String>() {
//                @Override
//                public void onSuccess(String s) {
//                    executeRetractCount++;
//                    // 自己撤回消息
//                    //1、构造系統消息
//                    m.setFlag(MessageFlag.RETRACT);
////                    m.setFlag(Constant.RETRACT_FLAG);
////                    IMessage retractMsg = MsgKitAssembler.assembleTimeMsg(m.getSessionId(), Constant.RETRACT_MSG, m.getTime(), TipMsgFormat.NOTICE);
//                    //2、撤回消息内容放入输入框
//                    // EVAN_FLAG 2019-09-10 因為實現多筆撤回機制，暫時不把所撤回信息重新放入 輸入框內。
////                    if (m.getMsgType() == MessageType.TEXT) {
////                        TextMsgFormat format = (TextMsgFormat) m.getFormat();
////                        binding.chatKeyboardLayout.setInputArea(format.getContent());
////                    }
//                    //3、刷新界面
////                    int index = ChatFragment.this.messageIds.indexOf(m.getId());
////                    int index = ChatFragment.this.mainMessageData.indexOf(m);
////                    retractMsg.setMsgId(m.getId());
////                    retractMsg.setFlag(Constant.RETRACT_FLAG);
////                    retractMsg.setTime(m.getTime());
////                    retractMsg.setSenderId(m.getSenderId());
////                    retractMsg.setSenderName(m.getSenderName());
//                    if (!Strings.isNullOrEmpty(m.getNearMessageId())) {
//                        boolean d = MessageReference.deleteByIds(new String[]{m.getNearMessageId()});
//
//                    }
//
//                    MessageReference.save(m.getRoomId(), m);
//
//
////                    if (index > -1) {
//////                        ChatFragment.this.messageIds.remove(m.getId());
////                        mainMessageData.remove(m);
////                        mainMessageData.add(index, retractMsg);
//////                        ChatFragment.this.messageIds.add(index, retractMsg.getId());
////                        DBManager.getInstance().deleteMessage(m.getId());
////                        if (!Strings.isNullOrEmpty(m.getNearMsgId())) {
////                            boolean d = DBManager.getInstance().deleteMessage(m.getNearMsgId());
////
////                        }
////
//////                        if (index == ChatFragment.this.mainMessageData.size()) {
//////                            presenter.upDateSession(mainMessageData.get(index - 1));
//////                        }
//////                        if (index == ChatFragment.this.messageIds.size()) {
//////                            presenter.upDateSession(mainMessageData.get(index - 1));
//////                        }
////                        CELog.e("被撤回消息的时间：" + m.getTime());
////                        CELog.e("撤回消息的时间：" + m.getTime());
//////                        DBManager.getInstance().insertMessage(m.getSessionId(), m);
////                    }
//
//                    if (executeRetractCount == messages.size()) {
//                        refreshListView();
//                        hideLoadingView();
//                    }
//                    binding.scopeRetractTip.setVisibility(View.GONE);
//                }
//
//                @Override
//                public void onFailed(String errorMessage) {
//                    executeRetractCount++;
//                    if (executeRetractCount == messages.size()) {
//                        refreshListView();
//                        hideLoadingView();
//                    }
//                    // EVAN_FLAG 2019-09-10 因為實現多筆撤回機制，暫時無法組裝無法被撤回的信息，超過60分鐘的信息。
//                    Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }
//
//    /**
//     * 組裝截圖資料
//     */
//    private void buildUpRangeScreenshotData(MessageEntity entity) {
//        //取消選取
//        if (entity.isShowSelection()) {
//            entity.setShowSelection(false);
//            screenShotData.remove(entity);
//            int removeIndex = mainMessageData.indexOf(entity);
//            binding.messageRV.getAdapter().notifyItemChanged(removeIndex, false);
//            return;
//        }
//
//        int currentIndex = 0;
//        //點擊的 message item
//        int clickIndex = mainMessageData.indexOf(entity);
//
//        if (screenShotData.isEmpty()) {
//            //進入截圖狀態之後，都沒有選取
//            currentIndex = clickIndex;
//        } else if (screenShotData.size() == 1) {
//            //剛進截圖狀態
//            currentIndex = mainMessageData.indexOf(screenShotData.get(0));
//        } else {
//            int first = mainMessageData.indexOf(screenShotData.get(0));
//            int last = mainMessageData.indexOf(screenShotData.get(screenShotData.size() -1 ));
//            if (clickIndex < first) currentIndex = first;
//            if (clickIndex > last) currentIndex = last;
//
//            if (clickIndex > first && clickIndex < last) {
//                MessageEntity selectMessage = mainMessageData.get(clickIndex);
//                selectMessage.setShowSelection(true);
//                screenShotData.add(selectMessage);
//                binding.messageRV.getAdapter().notifyItemChanged(clickIndex, false);
//                return;
//            }
//        }
//
//        int startIndex = Math.min(currentIndex, clickIndex);
//        int endIndex = Math.max(currentIndex, clickIndex);
//        for (int i = startIndex; i <= endIndex; i++) {
//            MessageEntity selectMessage = mainMessageData.get(i);
//            if (!screenShotData.contains(selectMessage)) {
//                selectMessage.setShowSelection(true);
//                screenShotData.add(selectMessage);
//                binding.messageRV.getAdapter().notifyItemChanged(i, false);
//            }
//        }
//        Collections.sort(screenShotData);
//    }
//
//    private void sendAiQuoteImage(String imagePath) {
//        sendFileSize = 1;
//        String[] filePath = PictureParse.parsePath(getCtx(), imagePath);
////        showSendVideoProgress(requireContext().getString(R.string.text_image_quote_on_the_way));
//        presenter.sendImage(filePath[0], filePath[1]);
//    }
//
//    private void sendAiQuoteVideo(String videoPath) {
//        sendFileSize = 1;
////        showSendVideoProgress(requireContext().getString(R.string.text_video_quote_on_the_way));
//        IVideoSize iVideo = new VideoSizeFromVideoFile(videoPath);
//        presenter.sendVideo(iVideo, true);
//    }
//
//    /**
//     * 執行任務鍵
//     */
//    public void executeTask(List<MessageEntity> entities, String path, boolean isToolbarSource) {
//        // api 取得任務列表，成功show list，失敗 show toast
//        List<MessageEntity> messages = Lists.newArrayList();
//        // 過濾物件訊息
//        for (MessageEntity entity : entities) {
//            if (!MessageType.BUSINESS.equals(entity.getType())) {
//                messages.add(entity);
//            }
//        }
//        String[] messageIds = new String[messages.size()];
//        for (int i = 0; i < messages.size(); i++) {
//            messageIds[i] = messages.get(i).getId();
//        }
//
//        if (ChatRoomType.discuss.equals(chatRoom.getType())) {
//            businessRelationOrCreate(isToolbarSource ? BusinessTaskAction.CREATE_BUSINESS_RELATIONAL : BusinessTaskAction.CREATE_BUSINESS, null, messageIds, path);
//        } else if (ChatRoomType.services.equals(chatRoom.getType())) {
//            businessRelationOrCreate(BusinessTaskAction.CREATE_BUSINESS, null, messageIds, path);
//        } else {
//            BusinessTaskAction action = (ChatRoomType.FRIEND_or_DISCUSS.contains(chatRoom.getType()) && isToolbarSource) ? BusinessTaskAction.CREATE_BUSINESS_RELATIONAL : BusinessTaskAction.CREATE_BUSINESS;
//            businessRelationOrCreate(action, null, messageIds, path);
//        }
//    }
//
//    /**
//     * 關連物件或建立物件聊天室
//     * EVAN_FLAG 2020-03-30 (1.10.0) 物件聊天室相關
//     */
//    public void businessRelationOrCreate(BusinessTaskAction action, BusinessContent businessContent, String[] messageIds, String path) {
//        ActivityManager.addActivity((ChatActivity) getActivity());
//        if (businessContent != null) {
//            businessContent.setCode(BusinessCode.TASK);
//            businessContent.setAvatarUrl("");
//        }
//
//        if (BusinessTaskAction.CREATE_BUSINESS.equals(action)) {
//            ActivityTransitionsControl.navigateToCreateBusinessTask(requireActivity(), action.name(), chatRoom, businessContent, messageIds, path, (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
//        } else if (BusinessTaskAction.CREATE_BUSINESS_ROOM.equals(action)) {
//            ActivityTransitionsControl.navigateToCreateBusinessTask(requireActivity(), action.name(), chatRoom, businessContent, messageIds, path, (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
//        } else if (BusinessTaskAction.CREATE_SERVICE_BUSINESS_ROOM.equals(action)) {
//            ActivityTransitionsControl.navigateToCreateBusinessTask(requireActivity(), action.name(), chatRoom, businessContent, messageIds, path, (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
//        } else if (BusinessTaskAction.CREATE_BUSINESS_RELATIONAL.equals(action)) {
//            ActivityTransitionsControl.navigateToCreateBusinessTask(requireActivity(), action.name(), chatRoom, businessContent, messageIds, path, (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
//        } else {
//            bindingBusiness(chatRoom.getId(), businessContent);
//        }
//    }
//
//    /**
//     * 綁定物件
//     */
//    private void bindingBusiness(String roomId, BusinessContent businessContent) {
//        ApiManager.doChatRoomBusinessRelation(requireActivity(), roomId, businessContent, new ApiListener<Map<String, String>>() {
//            @Override
//            public void onSuccess(Map<String, String> data) {
//                if (Strings.isNullOrEmpty(businessContent.getId())) {
//                    businessContent.setId(data.get("businessId"));
//                    businessContent.setCode(BusinessCode.of(data.get("businessCode")));
//                }
//                ApiManager.doRoomItem(requireActivity(), roomId, "", new ApiListener<ChatRoomEntity>() {
//                    @Override
//                    public void onSuccess(ChatRoomEntity entity) {
//                        bindingBusiness(entity.getId(), businessContent, true);
//                        CELog.d(businessContent.toString());
//                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_RESULT_BUSINESS_BINDING_ROOM_ACTION, ""));
//                    }
//
//                    @Override
//                    public void onFailed(String errorMessage) {
//                        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_RESULT_BUSINESS_BINDING_ROOM_ACTION, errorMessage));
//                    }
//                });
//            }
//
//            @Override
//            public void onFailed(String errorMessage) {
//                CELog.d(errorMessage);
//                ThreadExecutorHelper.getMainThreadExecutor().execute(() -> Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show());
//            }
//        });
//    }
//
//    public void bindingBusiness(String roomId, BusinessContent content, boolean isSendLastMessage) {
//        boolean status = ChatRoomReference.getInstance().updateBusinessContent(roomId, content);
//        if (status) {
//            chatRoom.setBusinessId(content.getId());
//            chatRoom.setBusinessCode(content.getCode());
//            chatRoom.setBusinessName(content.getName());
//        }
//
//        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
//            // 改聊天室UI 成為 物件聊天室風格
//            // Toolbar themeStyle
////            ((ChatActivity) requireActivity()).setThemeStyle(RoomThemeStyle.BUSINESS);
////            ((ChatActivity) requireActivity()).getTitleText();
//            // content themeStyle
//            themeStyle();
//            // KeyboardLayout themeStyle
//            binding.chatKeyboardLayout.setThemeStyle(RoomThemeStyle.BUSINESS);
//            Toast.makeText(requireActivity(), "聊天室已關聯任務", Toast.LENGTH_SHORT).show();
//            if (isSendLastMessage) {
//                presenter.sendBusiness(content);
//            }
//        });
//    }
//
//    /**
//     * 執行多筆複製。
//     * 複製以空格區分，並以信息時間排序
//     * date 2019/09/10
//     */
//    private void executeCopy(List<MessageEntity> messages) {
//        sortMessageByDate(messages, SortType.ASC);
//        StringBuilder builder = new StringBuilder();
//        for (MessageEntity m : messages) {
//            if (MessageType.AT_or_TEXT.contains(m.getType())) {
//                IMessageContent content = m.content();
//                if (content instanceof AtContent) {
//                    try {
//                        List<MentionContent> ceMentions = ((AtContent) content).getMentionContents();
//                        SpannableStringBuilder ssb = AtMatcherHelper.matcherAtUsers("@", ceMentions, this.chatRoom.getMembersTable());
//                        builder.append(ssb).append(" ");
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                if (content instanceof TextContent) {
//                    builder.append(((TextContent) content).getText()).append(" ");
//                }
//            }
//        }
//
//        if (builder.length() > 0) {
//            builder.deleteCharAt(builder.length() - 1);
//        }
//
//        android.text.ClipboardManager cmb = (android.text.ClipboardManager) getCtx().getSystemService(Activity.CLIPBOARD_SERVICE);
//        cmb.setText(builder.toString());
//        Toast.makeText(getCtx(), getCtx().getString(R.string.warning_copied), Toast.LENGTH_SHORT).show();
//        ChatFragment.this.actionStatus = ActionStatus.SCROLL;
//    }
//
//    /**
//     * 關閉截圖功能
//     */
//    private void clearScreenshotsFunction() {
//        for (MessageEntity m : mainMessageData) {
//            m.setShowSelection(false);
//        }
//        screenShotData.clear();
////        rangeScreenshot.clear();
//        presenter.isObserverKeboard = false;
//        binding.chatKeyboardLayout.showKeyboard();
//        if (getActivity() instanceof ChatActivity) {
//            ((ChatActivity) getActivity()).showToolBar(true);
//        }
//        themeStyle();
//        binding.xrefreshLayout.setOnBackgroundClickListener(ChatFragment.this);
//        binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT)
//                .clearAnonymous();
//    }
//
//    int height = 0;
//    int executeCount = 0;
//
//    /**
//     * ScreenshotsPreview
//     * 依照選取範圍截圖
//     */
//    public void executeScreenshots(boolean isTask, RichMenuBottom action) {
//        if (screenShotData.isEmpty()) {
//            return;
//        }
//        height = 0;
//        executeCount = 0;
//        showLoadingView(R.string.wording_processing);
//
//        binding.messageRV.setBackgroundColor(Color.WHITE);
//        MessageAdapter adapter = binding.messageRV.getAdapter();
//
//        //獲取item的數量
//        //recycler的完整高度 用於創建bitmap時使用
////        int height = 0;
//        //獲取最大可用內存
//        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
//        // 使用1/8的緩存
//        final int cacheSize = maxMemory / 8;
//        //把每個item的繪圖緩存存儲在LruCache中
//        LruCache<String, Bitmap> bitmapCache = new LruCache<>(cacheSize);
//        List<Integer> indexList = Lists.newArrayList();
//        screenShotData.forEach(selectedMessage -> {
//            indexList.add(mainMessageData.indexOf(selectedMessage));
//        });
//
//        for (int i : indexList) {
//            MessageViewBase holder = adapter.getHolder(i);
//            adapter.getHolder(i).itemView.setDrawingCacheEnabled(true);
//            adapter.getHolder(i).itemView.buildDrawingCache();
////            adapter.getHolder(i).itemView.getDrawingCache();
//            //手動調用創建和綁定ViewHolder方法，
////            MessageViewBase holder = adapter.onCreateViewHolder(binding.messageRV, adapter.getItemViewType(i));
//            holder.setIsRecyclable(false);
////            adapter.onBindViewHolderCache(holder, i);
//
//            new Handler().postDelayed(() -> {
//                //測量
//                holder.itemView.measure(
//                        View.MeasureSpec.makeMeasureSpec(binding.messageRV.getWidth(), View.MeasureSpec.EXACTLY),
//                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
////                //佈局
//                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(), holder.itemView.getMeasuredHeight());
////                //開啟繪圖緩存
//                holder.itemView.setDrawingCacheEnabled(true);
//                holder.itemView.buildDrawingCache();
//                Bitmap drawingCache = holder.itemView.getDrawingCache();
//                if (drawingCache != null) {
//                    bitmapCache.put(String.valueOf(i), drawingCache);
//                }
//                //獲取itemView的實際高度並累加
//                height += holder.itemView.getMeasuredHeight();
//                executeCount++;
//                if (executeCount == indexList.size()) {
//                    //創建保存截圖的bitmap
//                    Bitmap bigBitmap = null;
//                    //根據計算出的recyclerView高度創建bitmap
//                    bigBitmap = Bitmap.createBitmap(binding.messageRV.getMeasuredWidth(), height, Bitmap.Config.RGB_565);
//                    //創建一個canvas畫板
//                    Canvas canvas = new Canvas(bigBitmap);
//                    //獲取recyclerView的背景顏色
//                    Drawable background = binding.messageRV.getBackground();
//                    //畫出recyclerView的背景色 這裡只用了color一種 有需要也可以自己擴展
//                    if (background instanceof ColorDrawable) {
//                        ColorDrawable colorDrawable = (ColorDrawable) background;
//                        int color = colorDrawable.getColor();
//                        canvas.drawColor(color);
//                    }
//                    //當前bitmap的高度
//                    int top = 0;
//                    //畫筆
//                    Paint paint = new Paint();
//                    for (int ii : indexList) {
//                        Bitmap bitmap = bitmapCache.get(String.valueOf(ii));
//                        if (bitmap != null) {
//                            canvas.drawBitmap(bitmap, 0f, top, paint);
//                            top += bitmap.getHeight();
//                        }
//                    }
//
//                    binding.messageRV.setBackgroundColor(Color.TRANSPARENT);
//
//                    // ~~~~~ end ~~~~~~
//                    hideLoadingView();
//                    try {
//                        File file = BitmapHelper.bitmapToFile(bigBitmap, requireContext().getExternalCacheDir().getPath(), "screenshots.jpg");
//                        if (RichMenuBottom.SAVE.equals(action)) {
//                            saveScreenshots(file.getPath());
//                            clearScreenshotsFunction();
//                            return;
//                        }
//
//                        if (RichMenuBottom.SHARE.equals(action)) {
//                            Bundle bundle = new Bundle();
//                            bundle.putString(BundleKey.FROM_ROOM_IDS.key(), chatRoom.getId());
//                            bundle.putString(BundleKey.FILE_PATH.key(), file.getPath());
//                            bundle.putSerializable(BundleKey.ROOM_TYPE.key(), InvitationType.ShareIn.name());
//                            IntentUtil.INSTANCE.launchIntent(requireContext(), MemberInvitationActivity.class , shareScreenShotResult, bundle);
//                            clearScreenshotsFunction();
//                            return;
//                        }
//
//                        if (RichMenuBottom.CONFIRM.equals(action)) {
//                            executeTask(Lists.newArrayList(), file.getPath(), false);
//                            clearScreenshotsFunction();
//                            return;
//                        }
//
//                        String actionName = (isTask ? RichMenuBottom.TASK.name() : RichMenuBottom.SAVE.name());
//                        ActivityTransitionsControl.navigateToScreenshotsPreview(requireActivity(), chatRoom, file.getPath(), actionName, (intent, s) -> {
//                            IntentUtil.INSTANCE.start(requireContext(), intent);
//                            clearScreenshotsFunction();
//                        });
//                    } catch (Exception e) {
//                        CELog.e(e.getMessage());
//                    }
//                }
//            }, 100L);
//        }
//    }
//
//    /**
//     * 儲存截圖
//     */
//    private void saveScreenshots(String filePath) {
//        Bitmap screenshots = BitmapHelper.getBitmapFromLocal(filePath);
//        String saveImagePath = null;
//        String imageFileName = "JPEG_" + "down" + System.currentTimeMillis() + ".jpg";
//        File storageDir = FileHelper.getStorageDir();
//        boolean success = true;
//        if (!storageDir.exists()) {
//            success = storageDir.mkdirs();
//        }
//        if (success) {
//            File imageFile = new File(storageDir, imageFileName);
//            saveImagePath = imageFile.getAbsolutePath();
//            try {
//                OutputStream fout = new FileOutputStream(imageFile);
//                screenshots.compress(Bitmap.CompressFormat.JPEG, 100, fout);
//                fout.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            File f = new File(saveImagePath); //權限問題會不能使用
//            Uri contentUri = Uri.fromFile(f);
//            mediaScanIntent.setData(contentUri);
//            requireActivity().sendBroadcast(mediaScanIntent);
//            Toast.makeText(requireActivity(), String.format(getString(R.string.bruce_photo_save), saveImagePath), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//
//    private void doExecutionSendImage(List<String> sessionIds, String path) {
//        //            String[] paths = PictureParse.parseUri(requireContext(), uri);
//        String token = TokenPref.getInstance(requireContext()).getTokenId();
////            String path = DaVinci.with().getImageLoader().getAbsolutePath(paths[0]);
//        FileService.uploadFile(requireContext(), true, token, Media.findByFileType(path), path, path, new AServiceCallBack<UploadManager.FileEntity, RefreshSource>() {
//            @Override
//            public void complete(UploadManager.FileEntity entity, RefreshSource refreshSource) {
//                for (int i = 0; i < sessionIds.size(); i++) {
//                    String mMessageId = tw.com.chainsea.chat.lib.Tools.generateMessageId();
//                    String roomId = sessionIds.get(i);
//                    ApiManager.doMessageSend(requireActivity(), roomId, mMessageId, "",
//                            new MsgBuilder(MessageType.IMAGE)
//                                    .url(entity.getUrl())
//                                    .size(entity.getSize())
//                                    .width(entity.getWidth())
//                                    .height(entity.getHeight())
//                                    .thumbnailUrl(entity.getThumbnailUrl())
//                                    .thumbnailSize(entity.getThumbnailSize())
//                                    .name("pic")
//                            , new MessageSendRequest.Listener<MessageEntity>() {
//                                @Override
//                                public void onFailed(MessageEntity message, String errorMessage) {
//                                    Toast.makeText(requireContext(), getString(R.string.text_share_media_failure), Toast.LENGTH_SHORT).show();
//                                }
//
//                                @Override
//                                public void onSuccess(MessageEntity message) {
//                                    Toast.makeText(requireContext(), "分享成功", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onProgress(float progress, long total) {
//
//            }
//
//            @Override
//            public void error(String message) {
//                CELog.e(message);
//                Toast.makeText(requireContext(), getString(R.string.text_share_media_failure), Toast.LENGTH_SHORT).show();
//            }
//
//        });
//    }
//    /**
//     * 依照時間排序信息
//     * date 2019/09/10
//     */
//    private static void sortMessageByDate(List<MessageEntity> messages, SortType type) {
//        Collections.sort(messages, (t1, t2) -> {
//            if (SortType.ASC.equals(type)) {
//                return Longs.compare(t2.getSendTime(), t1.getSendTime());
//            } else {
//                return Longs.compare(t2.getSendTime(), t1.getSendTime());
//            }
//        });
//    }
//
//    /**
//     * 執行重發邏輯
//     * date 2019/09/10
//     */
//    private void executeReRetry(MessageEntity message) {
////        int index = this.messageIds.indexOf(message.getId());
////        mainMessageData.remove(index);
////        this.messageIds.remove(index);
//        mainMessageData.remove(message);
//        presenter.retrySend(message);
//        ChatFragment.this.actionStatus = ActionStatus.SCROLL;
//    }
//
//    /**
//     * 執行分享邏輯
//     * date 2019/09/10
//     */
//    private void executeShare(MessageEntity message) {
//        presenter.share(message, "", "");
//        ChatFragment.this.actionStatus = ActionStatus.SCROLL;
//    }
//
//    /**
//     * 顯示 Facebook 回覆
//     * */
//    private void executeFacebookReply(MessageEntity message) {
//        isReply = true;
//        themeId = "";
//        binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0d / 3.0d));
//        binding.themeMRV.setVisibility(View.VISIBLE);
//        isThemeOpen = true;
//        themeId = !Strings.isNullOrEmpty(message.getThemeId()) ? message.getThemeId() : message.getId();
//        showFacebookThemeView(themeId);
//        actionStatus = ActionStatus.SCROLL;
//    }
//
//    /**
//     * 顯示 Facebook 主題聊天室
//     */
//    private void showFacebookThemeView(String themeId) {
//        if (binding.searchBottomBar.getVisibility() == View.VISIBLE) {
//            return;
//        }
//        binding.expandIV.setVisibility(View.GONE);
//        binding.themeMRV.clearData();
//        binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0d / 3.0d));
//        MessageEntity themeMessages = findFirstMessageByThemeId(themeId);
//        displayThemeMessage(themeId, false, themeMessages);
//        binding.themeMRV.setVisibility(View.VISIBLE);
//        isThemeOpen = true;
//        themeId = themeId;
//        binding.themeMRV.post(() -> {
//            int maxHeight = setupDefaultThemeHeight(2.0d / 3.0d);
//            int height = binding.themeMRV.getHeight();
//            if (maxHeight == height) {
//                this.binding.expandIV.setVisibility(View.VISIBLE);
//            } else {
//                this.binding.expandIV.setVisibility(View.GONE);
//            }
//        });
//    }
//
//    public MessageEntity findFirstMessageByThemeId(String themeId) {
//        for (MessageEntity message: mainMessageData) {
//            if (message.getId().equals(themeId)) {
//                return message;
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 執行回復邏輯(主題聊天室)
//     * date 2019/09/10
//     */
//    private void executeReply(MessageEntity message) {
//        isReply = true;
//        this.themeId = "";
//        binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0d / 3.0d));
//        binding.chatKeyboardLayout.hideBottomPop();
//        binding.themeMRV.setVisibility(View.VISIBLE);
//        this.isThemeOpen = true;
//        this.themeId = !Strings.isNullOrEmpty(message.getThemeId()) ? message.getThemeId() : message.getId();
//        showThemeView(themeId);
//        ChatFragment.this.actionStatus = ActionStatus.SCROLL;
//    }
//
//    /**
//     * 執行回復邏輯(主題聊天室)
//     * date 2019/09/10
//     */
//    private void executeTodo(MessageEntity message) {
//
//        String title = "新訊息";
//        switch (message.getType()) {
//            case AT:
//                title = AtMatcherHelper.matcherAtUsers("@", ((AtContent) message.content()).getMentionContents(), this.chatRoom.getMembersTable()).toString();
//                break;
//            default:
//                title = message.content().simpleContent();
//                break;
//        }
//
//        long now = System.currentTimeMillis();
//
//        TodoEntity entity = new TodoEntity.Builder()
//                .title(title)
//                .status(TodoStatus.PROGRESS)
//                .processStatus(ProcessStatus.UN_SYNC_CREATE)
//                .openClock(EnableType.N.isStatus())
//                .remindTime(-1)
//                .createTime(now)
//                .updateTime(now)
//                .roomId(message.getRoomId())
//                .messageId(message.getId())
//                .userId(getUserId())
//                .build();
//        TodoSettingDialog todoSettingDialog = new TodoSettingDialog(requireContext(), entity.getRoomId(), entity.getId(), Lists.newArrayList(entity));
//        todoSettingDialog.setRemindListener(setRemindListener);
//        todoSettingDialog.show();
//        ChatFragment.this.actionStatus = ActionStatus.SCROLL;
//    }
//
//    private OnSetRemindTime setRemindListener = isRemind -> {
//        if (isRemind) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (!Settings.canDrawOverlays(requireContext())) {
//                    DateTime today = new DateTime();
//                    DateTime setNoticeDate = new DateTime(TokenPref.getInstance(requireContext()).getRemindNotice());
//
//                    if (today.isAfter(setNoticeDate)) {
//                        String[] others = new String[]{requireContext().getString(R.string.alert_cancel),
//                                requireContext().getString(R.string.alert_confirm)};
//                        new AlertView.Builder()
//                                .setContext(requireContext())
//                                .setStyle(AlertView.Style.Alert)
//                                .setMessage("為了讓您有更好的操作體驗，請允許使用浮動視窗權限。")
//                                .setOthers(others)
//                                .setOnItemClickListener((o, pos) -> {
//                                    if (pos == 1) {
//                                        Intent intent2 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//                                        if (requireContext() instanceof Activity) {
//                                            requireActivity().startActivityForResult(intent2, TodoListFragment.REQUEST_CODE);
//                                        }
//                                    } else {
//                                        DateTime dt = new DateTime();
//                                        DateTimeFormatter forPattern = DateTimeFormat.forPattern("yyyy-MM-dd");
//                                        DateTime dtp = forPattern.parseDateTime(dt.plusDays(7).toString("yyyy-MM-dd"));
//                                        //拒絕給予權限時，紀錄時間，一週後再問一次
//                                        TokenPref.getInstance(requireContext()).setRemindNotice(dtp.getMillis());
//                                        Toast.makeText(requireContext(), "許可權授予失敗，無法開啟浮動視窗", Toast.LENGTH_SHORT).show();
//                                    }
//                                })
//                                .build()
//                                .setCancelable(true)
//                                .show();
//                    } else {
//                        Toast.makeText(requireContext(), "請賦予懸浮視窗權限以獲得最即時的通知", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        }
//    };
//
//    /**
//     * 底部進階選單，依照分類建立按鈕
//     * date 2019/09/10
//     */
//
//    private List<RichMenuBottom> setupRichMenuData(RichMenuType type, MessageEntity msg) {
//        // EVAN_FLAG 2020-03-03 (1.10.0) 長按先判斷是否為主題聊天串，如果是，則處理收回與刪除不同邏輯
//        boolean hasThemeMessage = false;
//        if (checkIsThemeMessage(msg)) {
//            hasThemeMessage = true;
//        }
//        if (!Strings.isNullOrEmpty(msg.getNearMessageId())) {
//            hasThemeMessage = true;
//        }
//
//        List<RichMenuBottom> gridNames = Lists.newArrayList();
//        if (RichMenuType.CALL_RICH.equals(type)) {
//            gridNames.addAll(type.get());
//            return gridNames;
//        }
//
//        if (msg.getSenderId().equals(TokenPref.getInstance(getCtx()).getUserId())) {
//            try {
//                int value = msg.getStatus().getValue();
//                ChatRoomEntity entity = ChatRoomReference.getInstance().findById(msg.getRoomId());
//                // 改服務號可回收信息
//                if (value > 0 && value != 2 &&
//                        !ChatRoomType.subscribe.equals(entity.getType()) &&
//                        (System.currentTimeMillis() - msg.getSendTime())/1000 <= TokenPref.getInstance(requireActivity()).getRetractValidMinute()* 60L) {
//                    gridNames.add(RichMenuBottom.RECOVER);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        gridNames.addAll(type.get());
//
//        // EVAN_FLAG 2020-03-03 (1.10.0) 如果是主題串
//        if (hasThemeMessage) {
//            gridNames.remove(RichMenuBottom.DELETE);
//            gridNames.remove(RichMenuBottom.RECOVER);
//        }
//
//        // EVAN_FLAG 2020-03-03 (1.10.0) 如果是失敗訊息只給 刪除功能
//        if (MessageStatus.FAILED_or_ERROR.contains(msg.getStatus())) {
//            return Lists.newArrayList(RichMenuBottom.DELETE);
//        }
//        return gridNames;
//    }
//
//    public void showRecordingWindow() {
//        // 通知禁止休眠
//        EventBusUtils.sendEvent(new EventMsg(MsgConstant.NOTICE_KEEP_SCREEN_ON));
//
//        binding.chatKeyboardLayout.hideBottomPop();
//        binding.chatKeyboardLayout.setRecordIconState();
//        binding.chatKeyboardLayout.post(()-> {
//            binding.chatKeyboardLayout.setSelect(ChatKeyboardLayout.BottomFunType.FUN_RECORD, true);
//        });
//    }
//
//    public void doFloatingLastMessageClickAction(View view) {
//        view.setVisibility(View.GONE);
//        binding.messageRV.refreshToBottom(this.actionStatus.status());
//    }
//
//    /**
//     * 內部訊息搜索模式開啟
//     */
//    public void doSearchAction(View searchBar, TextInputEditText editText, AppCompatImageView clearInput) {
//        binding.scopeProvisionalMemberList.setVisibility(View.GONE);
//        searchBar.setVisibility(View.VISIBLE);
//        binding.scopeSearch.setVisibility(View.VISIBLE);
//        binding.scopeSearch.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.white));
//        // 關閉訊息點擊事件
//        binding.chatKeyboardLayout.invisibleKeyboard();
//        isFloatViewOpenAndExecuteClose();
//        List<MessageEntity> searchMessages = Lists.newArrayList();
//        binding.searchBottomBar.setVisibility(View.GONE);
//        binding.scopeSectioned.setVisibility(View.GONE);
//        binding.searchRV.setLayoutManager(new LinearLayoutManager(getCtx()));
//        binding.searchRV.setItemAnimator(new DefaultItemAnimator());
//        binding.searchRV.setHasFixedSize(true);
//
//        chatRoomMemberTable.clear();
//        if(chatRoom.getMembers() != null) {
//            for(UserProfileEntity user: chatRoom.getMembers()) {
//                chatRoomMemberTable.put(user.getId(), !Strings.isNullOrEmpty(user.getAlias()) ? user.getAlias() : user.getNickName());
//            }
//        }
//        chatViewModel.getSendShowLoadMoreMsg().observe(getViewLifecycleOwner(), messageEntities -> {
//            List<MessageEntity> allSearchMessages = adapter.getData();
//            int origin = allSearchMessages.size();
//            sortMessageByDate(messageEntities, SortType.DESC);
//            adapter.setData(chatViewModel.getSearchMessageList(messageEntities, true), keyWord, chatRoomMemberTable);
//            binding.sectionedTitle.setText(requireActivity().getString(R.string.text_sectioned_search_news, messageEntities.size()));
//            if(messageEntities.size() > origin)
//                binding.searchRV.scrollToPosition(origin + 1);
//        });
//        adapter = new ChatRoomMessageSearchAdapter(new OnMessageItemClick() {
//            @Override
//            public void onItemClick(@NonNull MessageEntity item, int position, @NonNull View itemView) {
//                binding.scopeSearch.setVisibility(View.GONE);
//                binding.searchBottomBar.setVisibility(View.VISIBLE);
//                binding.scopeSectioned.setVisibility(View.GONE);
//                searchSelectorIndex = position;
//                binding.indicatorTV.setText(MessageFormat.format("{0}/{1}", searchSelectorIndex + 1, adapter.getData().size()));
//                doScrollToSelectMessageItemAndSetKeyword(keyWord, item);
//            }
//
//            @Override
//            public void onLoadMoreClick() {
//                if (chatRoom != null) {
//                    if (!mainMessageData.isEmpty()) {
//                        MessageEntity msg = getFirstMsgId();
//                        presenter.searchMoreMsg(msg);
//                    } else {
//                        presenter.searchMoreMsg(null);
//                        stopRefresh();
//                    }
//                }
//            }
//        });
//        binding.searchRV.setAdapter(adapter);
//        binding.scopeSectioned.setOnClickListener(v -> {
//            binding.scopeSearch.setVisibility(binding.scopeSearch.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//            binding.icExtend.setImageResource(binding.scopeSearch.getVisibility() == View.VISIBLE ? R.drawable.ic_arrow_top : R.drawable.ic_arrow_down);
//            binding.emptyLayout.setVisibility(binding.scopeSearch.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//        });
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @Override
//            public void afterTextChanged(Editable s) {
//                clearInput.setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
//            }
//        });
//        clearInput.setOnClickListener(v -> {
//            Objects.requireNonNull(editText.getText()).clear();
//            binding.scopeSectioned.setVisibility(View.GONE);
//            adapter.setData(Lists.newArrayList(), "", Maps.newHashMap());
//            KeyboardHelper.open(searchBar);
//            binding.searchBottomBar.setVisibility(View.GONE);
//        });
//        editText.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                if (Strings.isNullOrEmpty(v.getText().toString().trim())) {
//                    Toast.makeText(requireActivity(), "請輸入搜尋關鍵字", Toast.LENGTH_SHORT).show();
//                } else {
//                    KeyboardHelper.hide(searchBar);
//                    doMessageSearch(binding.scopeSearch, adapter, v.getText().toString(), searchMessages, binding.indicatorTV, binding.upIV, binding.downIV);
//                    editText.clearFocus();
//                }
//                return true;
//            }
//            return false;
//        });
//
//        binding.downIV.setOnClickListener(v -> {
//            searchSelectorIndex--;
//            if (searchSelectorIndex < 0) {
//                searchSelectorIndex = 0;
//            }
//            doSearchSelectors(binding.upIV, v, editText, binding.indicatorTV, adapter.getData());
//        });
//
//        binding.upIV.setOnClickListener(v -> {
//            searchSelectorIndex++;
//            if (searchSelectorIndex >= adapter.getData().size() - 1) {
//                searchSelectorIndex = adapter.getData().size() - 1;
//            }
//            doSearchSelectors(v, binding.downIV, editText, binding.indicatorTV, adapter.getData());
//        });
//    }
//
//    /**
//     * 內部訊息搜索結果
//     */
//    private void doSearchSelectors(View upIV, View downIV, TextInputEditText editText, TextView tv, List<MessageEntity> searchMessages) {
//        tv.setText(MessageFormat.format("{0}/{1}", searchSelectorIndex + 1, searchMessages.size()));
//        upIV.setEnabled(true);
//        upIV.setAlpha(1.0f);
//        downIV.setEnabled(true);
//        downIV.setAlpha(1.0f);
//        if (searchSelectorIndex <= 0) {
//            downIV.setEnabled(false);
//            downIV.setAlpha(0.3f);
//        } else if (searchSelectorIndex >= searchMessages.size() - 1) {
//            upIV.setEnabled(false);
//            upIV.setAlpha(0.3f);
//        }
//
//        MessageEntity message = searchMessages.get(searchSelectorIndex);
//        String keyword = Objects.requireNonNull(editText.getText()).toString();
//        doScrollToSelectMessageItemAndSetKeyword(keyword, message);
//    }
//
//    /**
//     * 內部訊息搜索，移動上下鍵到該被搜索到的訊息位置
//     */
//    private void doScrollToSelectMessageItemAndSetKeyword(String keyword, MessageEntity message) {
//        int index = mainMessageData.indexOf(message);
//        if (index != 0) {
//            MessageEntity entity = mainMessageData.get(index);
//            entity.setAnimator(true);
////            entity.setAnimator(true);
//            ((LinearLayoutManager) binding.messageRV.getLayoutManager()).scrollToPositionWithOffset(index - 1, 0);
//            binding.messageRV.getAdapter().notifyItemChanged(index, entity);
////            binding.messageRV.getAdapter().notifyItemChanged(index - 1);
//        } else {
//            ((LinearLayoutManager) binding.messageRV.getLayoutManager()).scrollToPositionWithOffset(0, 0);
//            binding.messageRV.getAdapter().notifyItemChanged(0);
//        }
//    }
//
//    int searchSelectorIndex = 0;
//
//    private void doMessageSearch(View resultLayout, ChatRoomMessageSearchAdapter adapter, String keyword, List<MessageEntity> searchMessages, TextView tv, View... controllers) {
//        if (resultLayout.getVisibility() == View.GONE) {
//            resultLayout.setVisibility(View.VISIBLE);
//        }
//        searchSelectorIndex = 0;
//        searchMessages.clear();
//        searchMessages.addAll(chatViewModel.filterMessageFromLocalDB(keyword, this.mainMessageData));
//        binding.sectionedTitle.setText(requireActivity().getString(R.string.text_sectioned_search_news, searchMessages.size()));
//        resultLayout.setBackgroundColor(ContextCompat.getColor(requireActivity(), !Strings.isNullOrEmpty(keyword) && !searchMessages.isEmpty() ? R.color.white : R.color.transparent));
//        binding.scopeSectioned.setVisibility(!Strings.isNullOrEmpty(keyword) && !searchMessages.isEmpty() ? View.VISIBLE : View.GONE);
//        tv.setText(MessageFormat.format("{0}/{1}", searchSelectorIndex + 1, searchMessages.size()));
//
//        for (View v : controllers) {
//            v.setEnabled(searchMessages.size() != 0);
//            v.setAlpha(searchMessages.size() == 0 ? 0.3f : 1.0f);
//        }
//        sortMessageByDate(searchMessages, SortType.DESC);
//        keyWord = keyword;
//        if (searchMessages.isEmpty()) {
//            Toast.makeText(requireActivity(), requireContext().getString(R.string.text_not_find_any_result), Toast.LENGTH_SHORT).show();
//            return;
//        }else
//            adapter.setData(chatViewModel.getSearchMessageList(searchMessages, true), keyword, chatRoomMemberTable);
//
//        binding.messageRV.setKeyword(keyword).refreshData();
//    }
//
//    public boolean isFloatViewOpenAndExecuteClose() {
//        if (binding == null || binding.chatKeyboardLayout == null) return false;
//        // 先關閉進階選單
//        View v = binding.chatKeyboardLayout.getRichMenuRecyclerView();
//        if (v != null && v.getVisibility() == View.VISIBLE) {
//            binding.chatKeyboardLayout.showKeyboard();
//            for (MessageEntity m : mainMessageData) {
//                m.setDelete(false);
//                m.setShowSelection(false);
//            }
//            themeStyle();
//            if (getActivity() instanceof ChatActivity) {
//                ((ChatActivity) getActivity()).showToolBar(true);
//            }
//            screenShotData.clear();
////            rangeScreenshot.clear();
//            binding.messageRV.getAdapter().setAnonymous(false);
//            binding.messageRV.setAdapterMode(MessageAdapterMode.DEFAULT);
//            presenter.isObserverKeboard = true;
//            return true;
//        }
//
//        if (binding.chatKeyboardLayout.isOpenFuncView()) {
//            return true;
//        }
//
//        // 關閉主題聊天室
//        if (this.binding.themeMRV.getVisibility() == View.VISIBLE) {
//            this.binding.expandIV.setTag(null);
//            this.binding.themeMRV.setMaxHeight(setupDefaultThemeHeight(2.0d / 3.0d));
//            this.binding.themeMRV.clearData();
//            this.binding.themeMRV.setVisibility(View.GONE);
//            this.isThemeOpen = false;
//            this.themeId = "";
//            if (isFacebookReplyOverTime) {
//                binding.chatKeyboardLayout.getFacebookOverTimeView().setVisibility(View.VISIBLE);
//            }
//            return true;
//        }
//
////        if (binding.funMedia.getVisibility() == View.VISIBLE) {
////            binding.funMedia.setVisibility(View.GONE);
////            binding.chatKeyboardLayout.clearIconState();
////            return true;
////        }
//
////        if (binding.lyChildChat.getVisibility() == View.VISIBLE) {
////            binding.lyChildChat.setVisibility(View.GONE);
////            return true;
////        }
//        return false;
//    }
//
//    /**
//     * 內部訊息搜索模式關閉
//     */
//    public void doSearchCancelAction() {
//        searchSelectorIndex = 0;
//        binding.indicatorTV.setText("0/0");
//        binding.chatKeyboardLayout.showKeyboard();
//        binding.scopeSearch.setVisibility(View.GONE);
//        binding.scopeSectioned.setVisibility(View.GONE);
//        binding.searchBottomBar.setVisibility(View.GONE);
//        // 打開訊息點擊事件
//        binding.messageRV.getAdapter()
//                .setMode(MessageAdapterMode.DEFAULT)
//                .setKeyword("")
//                .refreshData();
//        binding.chatKeyboardLayout.getInputHET().requestFocus();
//        if (!chatRoom.getProvisionalIds().isEmpty() && chatRoom.getRoomType() != ChatRoomType.provisional) {
//            binding.scopeProvisionalMemberList.setVisibility(View.VISIBLE);
//        }
//    }
//
//    /**
//     * 回覆聊天室預設值樣式
//     */
//    public void setupToDefault() {
//        // 進階選單 & 關閉標註模式 & 關閉主題聊天室
//        if (!this.IS_ACTIVITY_FOR_RESULT) {
//            isFloatViewOpenAndExecuteClose();
//        }
//        // 關閉標註模式
//        this.binding.chatKeyboardLayout.hideMention(null, "");
//        // 底部浮動信息欄
//        if (this.binding.floatingLastMessageTV.getVisibility() == View.VISIBLE) {
//            this.binding.floatingLastMessageTV.setVisibility(View.GONE);
//        }
//    }
//
//    public void hideKeyboard() {
//        binding.chatKeyboardLayout.hideKeyboard();
//        binding.chatKeyboardLayout.showKeyboard();
//    }
//
//    private void showFunMedia(boolean isChange) {
//        if (isChange) {
//            binding.funMedia.setChangeVisibility();
//        } else {
//            binding.funMedia.setType(MultimediaHelper.Type.IMAGE, themeStyle, MEDIA_SELECTOR_REQUEST_CODE);
//        }
//
//        binding.funMedia.setVisibility(View.VISIBLE);
//    }
//
//    //AIFF聊天室消息菜單
//    private List<RichMenuInfo> classifyAiffInMenu(ChatRoomEntity entity) {
//
//        List<RichMenuInfo> aiffList = Lists.newArrayList();
//        //Aiff消息菜單
//        List<AiffInfo> aiffInfoList = AiffDB.getInstance(requireContext()).getAiffInfoDao().getAiffInfoListByUseTime();
//        if (aiffInfoList.size() > 0) {
//            for (AiffInfo aiff : aiffInfoList) {
//                //CELog.d("Kyle2 name="+aiff.getName()+", embed="+aiff.getEmbedLocation()+", diaplay="+aiff.getDisplayLocation());
//                if (aiff.getEmbedLocation().equals(AiffEmbedLocation.MessageMenu.name())) {
//
//                    if (ChatRoomType.self.equals(entity.getType())) {
//                        RichMenuInfo info =
//                                new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (system.equals(entity.getType())) {
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (discuss.equals(entity.getType())) {
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (ChatRoomType.friend.equals(entity.getType())) {
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (ChatRoomType.group.equals(entity.getType())) {
//                        //社團聊天室
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (ChatRoomType.serviceMember.equals(entity.getType()) && ServiceNumberType.BOSS.equals(entity.getServiceNumberType())) {
//                        //商務號秘書群聊天室
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (ChatRoomType.services.equals(entity.getType()) && ServiceNumberType.BOSS.equals(entity.getServiceNumberType())) {
//                        //商務號聊天室
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (ChatRoomType.services.equals(entity.getType()) && entity.getServiceNumberOpenType().contains("I") && !entity.getOwnerId().equals(userId)) {
//                        //服務號員工進線聊天室(服務人員)
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (ChatRoomType.services.equals(entity.getType()) && entity.getServiceNumberOpenType().contains("I") && entity.getOwnerId().equals(userId)) {
//                        //服務號員工進線聊天室(詢問者)
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (ChatRoomType.serviceMember.equals(entity.getType()) && !ServiceNumberType.BOSS.equals(entity.getServiceNumberType())) {
//                        //服務號服務成員聊天室
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    } else if (ChatRoomType.services.equals(entity.getType()) && entity.getServiceNumberOpenType().contains("O") && !entity.getOwnerId().equals(userId)) {
//                        //服務號客戶進線聊天室
//                        RichMenuInfo info = new RichMenuInfo(RichMenuInfo.MenuType.AIFF.getType(), aiff.getId(), aiff.getPictureId(),
//                                aiff.getTitle(), aiff.getName(), aiff.getPinTimestamp(), aiff.getUseTimestamp());
//                        aiffList.add(info);
//                    }
//                }
//            }
//        }
//        Collections.sort(aiffList);
//        return aiffList;
//    }
//}