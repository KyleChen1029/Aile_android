package tw.com.chainsea.chat.keyboard.emoticon;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.common.collect.Queues;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.system.ThreadExecutorHelper;
import tw.com.chainsea.ce.sdk.bean.sticker.EmoticonType;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerPackageEntity;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.http.ce.request.StickerDownloadRequest;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentNewEmoticonBinding;
import tw.com.chainsea.chat.keyboard.emoticon.adapter.NewEmoticonAdapter;
import tw.com.chainsea.chat.widget.AnimatorToast;
import tw.com.chainsea.chat.widget.GridItemDecoration;
import tw.com.chainsea.custom.view.progress.IosProgressBar;

/**
 * current by evan on 2020-10-05
 *
 * @author Evan Wang
 * date 2020-10-05
 */
public class NewEmoticonFragment extends Fragment {
    private FragmentNewEmoticonBinding binding;
    public EmoticonType type = EmoticonType.STICKER; //Emoji type
    public int fileSize = 0;
    NewEmoticonAdapter adapter;

    public static NewEmoticonFragment newInstance() {
        return new NewEmoticonFragment();
    }

    public static NewEmoticonFragment newInstance(EmoticonType type, StickerPackageEntity packageEntity, int fileSize) {
        NewEmoticonFragment fragment = new NewEmoticonFragment();
        Bundle args = new Bundle();
        args.putString(BundleKey.TYPE.key(), type.name());
        args.putString(BundleKey.DATA.key(), JsonHelper.getInstance().toJson(packageEntity));
        fragment.type = type;
        fragment.fileSize = fileSize;
        fragment.adapter = new NewEmoticonAdapter(type, fileSize, packageEntity);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBusUtils.register(this);
        Bundle args = getArguments();
        if (args != null && this.adapter == null) {
            String type = args.getString(BundleKey.TYPE.key());
            this.type = EmoticonType.valueOf(type);
            String json = args.getString(BundleKey.DATA.key());
            StickerPackageEntity packageEntity = JsonHelper.getInstance().from(json, StickerPackageEntity.class);
            this.adapter = new NewEmoticonAdapter(this.type, this.fileSize, packageEntity);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_emoticon, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (this.type) {
            case STICKER:
            case EMOJI:
            default:
                binding.difbBackspace.setVisibility(View.GONE);
                break;
        }

        String json = getArguments().getString(BundleKey.DATA.key());
        StickerPackageEntity packageEntity = JsonHelper.getInstance().from(json, StickerPackageEntity.class);

        if (EmoticonType.EMOJI.equals(this.type)) {
            binding.rvEmoticonList.setLayoutManager(new GridLayoutManager(getContext(), packageEntity.getColumns(), GridLayoutManager.VERTICAL, false));
        } else {
            int size = StickerService.getStickerThumbnailsSize(getContext(), packageEntity.getId());
            if (size == 0 || size != packageEntity.getCount()) {
                adapter.clearData();
                binding.rvEmoticonList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            } else {
                binding.rvEmoticonList.setLayoutManager(new GridLayoutManager(getContext(), packageEntity.getColumns(), GridLayoutManager.VERTICAL, false));
            }
        }

        binding.rvEmoticonList.addItemDecoration(new GridItemDecoration(Color.TRANSPARENT));
        binding.rvEmoticonList.setItemAnimator(new DefaultItemAnimator());
        binding.rvEmoticonList.setHasFixedSize(true);

        binding.rvEmoticonList.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }

    public void setOnEmoticonSelectListener(NewEmoticonLayout.OnEmoticonSelectListener<StickerItemEntity> onEmoticonSelectListener) {
        if (adapter != null) {
            adapter.setOnEmoticonSelectListener(onEmoticonSelectListener);
        }
    }

    public void scrollToPosition(int position) {
        binding.rvEmoticonList.scrollToPosition(position);
    }

    IosProgressBar progressBar;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(EventMsg eventMsg) {
        if (eventMsg.getCode() == MsgConstant.DOWNLOAD_STICKER_RESOURCES_BY_PACKAGE_ID) {
            String id = eventMsg.getString();
            String json = getArguments().getString(BundleKey.DATA.key());
            StickerPackageEntity packageEntity = JsonHelper.getInstance().from(json, StickerPackageEntity.class);

            if (packageEntity.getId().equals(id)) {
                if (progressBar != null) {
                    progressBar.hide();
                    progressBar.show();
                } else {
                    progressBar = IosProgressBar.show(getContext(), "下載中", true, false, dialog -> {
                    });
                }

                StickerService.getStickerEntities(getContext(), packageEntity.getId(), RefreshSource.REMOTE, new ServiceCallBack<List<StickerItemEntity>, RefreshSource>() {
                    @Override
                    public void complete(List<StickerItemEntity> entities, RefreshSource source) {
                        StickerService.postDownloads(getContext(), Queues.newConcurrentLinkedQueue(entities), StickerDownloadRequest.Type.THUMBNAIL_PICTURE, new ServiceCallBack<String, RefreshSource>() {
                            @Override
                            public void complete(String drawable, RefreshSource source) {
                                downloadCompleted(entities, null);
                            }

                            @Override
                            public void error(String message) {
                                downloadCompleted(entities, message);
                            }
                        });
                    }

                    @Override
                    public void error(String message) {

                    }
                });
            }
        }
    }

    /**
     * @param entities
     * @param errorMsg 若有 error message 代表從 server 下載失敗
     */
    @SuppressLint("NotifyDataSetChanged")
    private void downloadCompleted(List<StickerItemEntity> entities, String errorMsg) {
        ThreadExecutorHelper.getMainThreadExecutor().execute(() -> {
            String json = getArguments().getString(BundleKey.DATA.key());
            StickerPackageEntity packageEntity = JsonHelper.getInstance().from(json, StickerPackageEntity.class);
            packageEntity.setCount(entities.size());
            packageEntity.setColumns(4);
            packageEntity.setRow(entities.size() / 4);
            packageEntity.setStickerItems(entities);
            getArguments().putString(BundleKey.DATA.key(), JsonHelper.getInstance().toJson(packageEntity));
            Collections.sort(entities);
            progressBar.hide();
            progressBar.dismiss();
            if (errorMsg != null) {
                CELog.e(errorMsg);
                // file is missing
                AnimatorToast.makeErrorToast(getContext(), "檔案缺失，請重新下載").show();
            } else {
                AnimatorToast.makeSccessToast(getContext(), "下載完成").show();
                binding.rvEmoticonList.setLayoutManager(new GridLayoutManager(getContext(), 4, GridLayoutManager.VERTICAL, false));
                adapter.setFileSize(entities.size())
                    .setData(entities)
                    .notifyDataSetChanged();
            }
        });
    }


    public boolean findLast(int position) {
        GridLayoutManager layoutManager = (GridLayoutManager) binding.rvEmoticonList.getLayoutManager();
        int findLast = layoutManager.findLastCompletelyVisibleItemPosition();
        int findLast2 = layoutManager.findLastVisibleItemPosition();
        CELog.e("position:: " + position + ", findLast:: " + findLast + ", findLast2:: " + findLast2);
        return findLast == position;
    }

}
