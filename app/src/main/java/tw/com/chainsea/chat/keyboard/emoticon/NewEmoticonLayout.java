package tw.com.chainsea.chat.keyboard.emoticon;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.sticker.EmoticonType;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerItemEntity;
import tw.com.chainsea.ce.sdk.bean.sticker.StickerPackageEntity;
import tw.com.chainsea.ce.sdk.service.StickerService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.databinding.LayoutKeyboardBottomEmoticonBinding;
import tw.com.chainsea.custom.view.adapter.TabViewPager2Adapter;

/**
 * current by evan on 2020-10-05
 *
 * @author Evan Wang
 * date 2020-10-05
 */
public class NewEmoticonLayout extends CoordinatorLayout {

    Context context;
    LayoutKeyboardBottomEmoticonBinding binding;
    TabViewPager2Adapter<NewEmoticonFragment> adapter;

    private final List<NewEmoticonFragment> fragments = Lists.newArrayList();

    public NewEmoticonLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NewEmoticonLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NewEmoticonLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        binding = LayoutKeyboardBottomEmoticonBinding.inflate(LayoutInflater.from(context), this, true);
        FragmentActivity fa = ((AppCompatActivity) this.context);
        adapter = new TabViewPager2Adapter<>(fa);
        adapter.setData(fragments);

        StickerService.getStickerPackageEntities(context, RefreshSource.LOCAL, new ServiceCallBack<List<StickerPackageEntity>, RefreshSource>() {
            @Override
            public void complete(List<StickerPackageEntity> stickerPackageEntities, RefreshSource source) {
                if (stickerPackageEntities != null) {
                    StickerService.postPackageIcons(context, Queues.newLinkedBlockingQueue(stickerPackageEntities), Maps.newLinkedHashMap(), new ServiceCallBack<Map<String, Drawable>, RefreshSource>() {
                        @Override
                        public void complete(Map<String, Drawable> data, RefreshSource refreshSource) {
                            for (StickerPackageEntity packageEntity : stickerPackageEntities) {
                                Drawable drawable = data.get(packageEntity.getId());
                                int size = EmoticonType.EMOJI.equals(packageEntity.getEmoticonType()) ? packageEntity.getCount() : StickerService.getStickerThumbnailsSize(context, packageEntity.getId());
                                fragments.add(NewEmoticonFragment.newInstance(packageEntity.getEmoticonType(), packageEntity, size));
                                TabLayout.Tab tab = binding.tabLayout.newTab().setCustomView(newTabView(drawable)).setTag(packageEntity);
                                tabs.add(tab);
                            }
                            adapter.add(fragments);
                            setListenerAndChanged();
                        }

                        @Override
                        public void error(String message) {
                            CELog.e(message);
                        }
                    });
                }
            }

            @Override
            public void error(String message) {
                CELog.e(message);
            }
        });
    }

    List<TabLayout.Tab> tabs = Lists.newArrayList();

    @SuppressLint("InflateParams")
    private View newTabView(Drawable drawable) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.tab_emoticon_icon, null);
        ImageView ivTabIcon = view.findViewById(R.id.iv_tab_icon);
        ivTabIcon.setImageDrawable(drawable);
        return view;
    }


    private void sortTabs(List<TabLayout.Tab> tabs) {
        Collections.sort(tabs, (o1, o2) -> {
            StickerPackageEntity s1 = null;
            StickerPackageEntity s2 = null;

            if (o1.getTag() != null && o1.getTag() instanceof StickerPackageEntity) {
                s1 = (StickerPackageEntity) o1.getTag();
            } else if (o1.getTag() == null) {
                s1 = StickerPackageEntity.Build().joinTime(System.currentTimeMillis()).build();
            }

            if (o2.getTag() != null && o2.getTag() instanceof StickerPackageEntity) {
                s2 = (StickerPackageEntity) o2.getTag();
            } else if (o2.getTag() == null) {
                s2 = StickerPackageEntity.Build().joinTime(System.currentTimeMillis()).build();
            }
            return ComparisonChain.start()
                .compare(s1.getJoinTime(), s2.getJoinTime())
                .result();
        });
    }

    private void setListenerAndChanged() {
        binding.tabLayout.removeAllTabs();
        sortTabs(tabs);
        for (TabLayout.Tab t : tabs) {
            binding.tabLayout.addTab(t);
        }
        for (NewEmoticonFragment f : fragments) {
            f.setOnEmoticonSelectListener(this.onEmoticonSelectListener);
        }
        binding.viewPager.setAdapter(adapter);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
            }
        });

    }


    private NewEmoticonLayout.OnEmoticonSelectListener<StickerItemEntity> onEmoticonSelectListener;

    public void setOnEmoticonSelectListener(NewEmoticonLayout.OnEmoticonSelectListener<StickerItemEntity> onEmoticonSelectListener) {
        this.onEmoticonSelectListener = onEmoticonSelectListener;
        for (NewEmoticonFragment f : fragments) {
            f.setOnEmoticonSelectListener(onEmoticonSelectListener);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            binding.tabLayout.setScrollPosition(0, 0f, true);
            binding.viewPager.setCurrentItem(0);
            for (NewEmoticonFragment f : fragments) {
                f.scrollToPosition(0);
            }
        }
        super.setVisibility(visibility);
    }

    public interface OnEmoticonSelectListener<T> {
        void onEmoticonSelect(T t);

        void onStickerSelect(T t, Drawable drawable);
    }
}
