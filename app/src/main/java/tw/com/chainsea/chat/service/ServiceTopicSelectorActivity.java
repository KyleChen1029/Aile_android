package tw.com.chainsea.chat.service;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.android.common.json.JsonHelper;
import tw.com.chainsea.ce.sdk.bean.broadcast.TopicEntity;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.ActivityServiceTopicSelectorBinding;
import tw.com.chainsea.chat.view.service.ServiceTopicFragment;
import tw.com.chainsea.chat.view.service.adapter.ServiceTopicAdapter;
import tw.com.chainsea.custom.view.adapter.TabViewPager2Adapter;

/**
 * 服務號訂閱邀請選擇器 topic list
 */
public class ServiceTopicSelectorActivity extends AppCompatActivity implements ServiceTopicAdapter.OnTopicSelectListener {
    private ActivityServiceTopicSelectorBinding binding;
    TabViewPager2Adapter<ServiceTopicFragment> adapter;
    ServiceTopicAdapter selectBeansAdapter = new ServiceTopicAdapter(ServiceTopicAdapter.Type.GRID);
    List<ServiceTopicFragment> fragments = Lists.newArrayList();
    List<TopicEntity> selectTopicEntities = Lists.newArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_service_topic_selector);
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        getWindow().setStatusBarColor(0xFF6BC2BA);
        adapter = new TabViewPager2Adapter<>(this);
        binding.viewPager.setAdapter(adapter);
        adapter.setData(fragments);
        if (getIntent().hasExtra(BundleKey.TOPIC_IDS.key())) {
            fragments.add(ServiceTopicFragment.newInstance(getIntent().getExtras()));
        }else {
            fragments.add(ServiceTopicFragment.newInstance());
        }

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("1"));

        for (ServiceTopicFragment f : fragments) {
            f.setOnTopicSelectListener(this);
        }
        adapter.add(fragments);
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
        binding.tabLayout.setVisibility(fragments.size() < 2 ? View.GONE : View.VISIBLE);

        binding.rvSelectBeans.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false));
        binding.rvSelectBeans.setAdapter(selectBeansAdapter);
        selectBeansAdapter.setOnTopicSelectListener(this);

        binding.searchView.setQueryHint("請輸入搜尋文字");
        // 點擊則進入輸入模式
        binding.searchView.setOnClickListener(v -> binding.searchView.setIconified(false));
        // 當距焦則進入輸入模式
        binding.searchView.setOnCloseListener(() -> true);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                setKeyword(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                setKeyword(s);
                return false;
            }
        });

        initListener();
    }



    private void setKeyword(String keyword) {
        for (ServiceTopicFragment fragment : fragments) {
            fragment.setKeyword(keyword);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }


    private void initListener() {
        binding.leftAction.setOnClickListener(this::doBackAction);
        binding.btnConfirm.setOnClickListener(this::doConfirmAction);
    }

    // --------- Event Binding ------------
    void doBackAction(View view) {
        finish();
    }

    void doConfirmAction(View view) {
        EventBusUtils.sendEvent(new EventMsg<>(MsgConstant.NOTICE_TOPIC_SELECTOR, JsonHelper.getInstance().toJson(selectTopicEntities)));
        finish();
    }

    @Override
    public void onTopicSelect(TopicEntity topicEntity) {
        if (selectTopicEntities.contains(topicEntity)) {
            selectTopicEntities.remove(topicEntity);
        } else {
            selectTopicEntities.add(topicEntity);
        }

        Set<String> ids = Sets.newHashSet();
        for (TopicEntity b : selectTopicEntities) {
            ids.add(b.getId());
        }

        for (ServiceTopicFragment f : fragments) {
            f.setSelectIds(ids);
        }

        selectBeansAdapter.setData(selectTopicEntities).refresh();
        binding.clSelectedPreview.setVisibility(!selectTopicEntities.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onTopicAdd(TopicEntity topicEntity) {

    }
}
