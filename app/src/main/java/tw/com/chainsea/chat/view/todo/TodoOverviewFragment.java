package tw.com.chainsea.chat.view.todo;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.tabs.TabLayout;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import tw.com.chainsea.ce.sdk.bean.SearchBean;
import tw.com.chainsea.ce.sdk.bean.business.BusinessEntity;
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.ce.sdk.database.sp.UserPref;
import tw.com.chainsea.ce.sdk.event.EventBusUtils;
import tw.com.chainsea.ce.sdk.event.EventMsg;
import tw.com.chainsea.ce.sdk.event.MsgConstant;
import tw.com.chainsea.ce.sdk.service.BusinessService;
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack;
import tw.com.chainsea.ce.sdk.service.type.RefreshSource;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.chatroomfilter.OnDataGetCallback;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentTodoOverviewBinding;
import tw.com.chainsea.chat.ui.fragment.BaseFragment;
import tw.com.chainsea.custom.view.adapter.TabViewPagerAdapter;

public class TodoOverviewFragment extends BaseFragment implements BaseFragment.OnActionListener {

    private FragmentTodoOverviewBinding binding;
    List<BaseFragment<TodoOverviewType, ?>> fragments = Lists.newArrayList();
    BusinessExecutorFragment businessExecutorFragment;
    BusinessManagerFragment businessManagerFragment;
    BusinessCooperateFragment businessCooperateFragment;
    TodoListFragment todoListFragment;
    String userId = "";
    private final List<String> beanStrings = Lists.newLinkedList();
    private ImageView searchViewIcon;
    private OnDataGetCallback callback;

    public static TodoOverviewFragment newInstance() {
        return new TodoOverviewFragment();
    }

    public static TodoOverviewFragment newInstance(OnDataGetCallback listener) {
        TodoOverviewFragment todoOverviewFragment = new TodoOverviewFragment();
        todoOverviewFragment.setOnDataGetCallback(listener);
        return todoOverviewFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_todo_overview, container, false);
        EventBusUtils.register(this);
        return binding.getRoot();
    }

    public TodoOverviewFragment setBundle(Bundle args) {
        setArguments(args);
        return this;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.userId = TokenPref.getInstance(getContext()).getUserId();
        binding.viewPager.setOffscreenPageLimit(4);
        binding.tabLayout.removeAllTabs();
        if (UserPref.getInstance(getContext()).hasBusinessSystem()) {
            businessExecutorFragment = BusinessExecutorFragment.newInstance();
            businessManagerFragment = BusinessManagerFragment.newInstance();
            businessCooperateFragment = BusinessCooperateFragment.newInstance();

            businessExecutorFragment.setOnActionListener(this);
            businessManagerFragment.setOnActionListener(this);
            businessCooperateFragment.setOnActionListener(this);

            fragments.add(businessExecutorFragment);
            fragments.add(businessManagerFragment);
            fragments.add(businessCooperateFragment);
        }
        String roomId = "";
        if (getArguments() != null) {
            roomId = getArguments().getString(BundleKey.ROOM_ID.key());
        }
        todoListFragment = TodoListFragment.newInstance(callback, roomId);
        todoListFragment.setOnActionListener(this);
        todoListFragment.setOnDataGetCallback(isEmpty -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (isEmpty) {
                        binding.ivNoData.setVisibility(View.VISIBLE);
                    } else {
                        binding.ivNoData.setVisibility(View.GONE);
                    }
                });
            }
        });
        fragments.add(todoListFragment);

        TabViewPagerAdapter<BaseFragment<TodoOverviewType, ?>> adapter = new TabViewPagerAdapter<>(getChildFragmentManager(), fragments);
        binding.viewPager.setAdapter(adapter);

        for (BaseFragment<TodoOverviewType, ?> f : fragments) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setCustomView(getTabView(getString(f.getType().getResId()), f.getDataCount())));
        }

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                if (v != null) {
                    TextView tvCount = v.findViewById(R.id.tv_count);
                    int count = fragments.get(tab.getPosition()).getDataCount();
                    tvCount.setText(String.format(Locale.TAIWAN, "%d", count));
                }
                binding.viewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

//        binding.tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        binding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabLayout));

        binding.tabLayout.setVisibility(fragments.size() <= 1 ? View.GONE : View.VISIBLE);

        @SuppressLint("RestrictedApi") SearchView.SearchAutoComplete autoComplete = binding.searchView.findViewById(R.id.search_src_text);
        if (autoComplete != null) {
            autoComplete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15.0f);
        }
        View searchPlate = binding.searchView.findViewById(R.id.search_plate);
        if (searchPlate != null) {
            searchPlate.setBackgroundColor(Color.TRANSPARENT);
        }
        searchViewIcon = binding.searchView.findViewById(R.id.search_close_btn);
        searchViewIcon.setVisibility(binding.searchView.getQuery().toString().isEmpty() ? View.GONE : View.VISIBLE);
//        binding.searchView.setQueryHint("請輸入搜尋文字");
        // 點擊則進入輸入模式
        binding.searchView.setOnClickListener(v -> {
            binding.searchView.setIconified(false);
            searchViewIcon.setVisibility(binding.searchView.getQuery().toString().isEmpty() ? View.GONE : View.VISIBLE);
        });
        // 當距焦則進入輸入模式
        binding.searchView.setOnCloseListener(() -> true);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                binding.searchView.clearFocus();
                setKeyword(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if ("".equals(s)) {
                    setBeansTextToDefault();
                }
                setKeyword(s);
                searchViewIcon.post(() -> searchViewIcon.setVisibility(binding.searchView.getQuery().toString().isEmpty() ? View.GONE : View.VISIBLE));
                return false;
            }
        });

        binding.searchView.setOnFocusChangeListener((v, hasFocus) -> searchViewIcon.post(() -> searchViewIcon.setVisibility(binding.searchView.getQuery().toString().isEmpty() ? View.GONE : View.VISIBLE)));
        setUpHistorys();

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(() -> searchViewIcon.setVisibility(binding.searchView.getQuery().toString().isEmpty() ? View.GONE : View.VISIBLE));
    }

    @Override
    public void setKeyword(String keyword) {
        for (BaseFragment fragment : fragments) {
            fragment.setKeyword(keyword);
        }
    }

    public void selectToTodoItem(TodoEntity entity) {
        if (entity != null) {
            binding.viewPager.setCurrentItem(fragments.size() - 1);
            todoListFragment.selectToTodoItem(entity);
        }
    }


    public void refreshNowTime() {
        if (todoListFragment != null) {
            todoListFragment.refreshNowTime();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshList(RefreshSource.LOCAL);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.searchView.clearFocus();
        refreshList(RefreshSource.REMOTE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }

    private void refreshList(RefreshSource refreshSource) {
        if (UserPref.getInstance(getContext()).hasBusinessSystem()) {
            BusinessService.getBusinessListMe(getContext(), refreshSource, new ServiceCallBack<List<BusinessEntity>, RefreshSource>() {
                @Override
                public void complete(List<BusinessEntity> entities, RefreshSource source) {
                    for (BaseFragment f : fragments) {
                        if (f instanceof IBusinessFragment) {
                            f.setData(Lists.newArrayList(entities));
                            f.refresh();
                        }
                    }
                }

                @Override
                public void error(String message) {
                }
            });
        }
    }

    @SuppressLint("InflateParams")
    public View getTabView(String title, int count) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.tab_badge_view, null);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setTextColor(0xFF6B93C2);
        TextView tvCount = view.findViewById(R.id.tv_count);
        tvCount.setBackgroundResource(R.drawable.unread_bg);
        tvCount.setVisibility(View.GONE);
//        tvCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        tvTitle.setText(title);
        tvCount.setText(String.valueOf(count));
        return view;
    }

    private void setUpHistorys() {
        binding.historyAql.setVisibility(View.GONE);
//        List<SearchBean> searchBeans = DBManager.getInstance().querySearchHistory();
//        for (SearchBean bean : searchBeans) {
//            setBeanView(-1, bean);
//        }
    }

    private void setBeanView(SearchBean bean) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_select_date, binding.historyAql, false);
        view.setBackground(null);
        TextView textView = view.findViewById(R.id.txt_date);
        if (getContext() != null) {
            textView.setText(bean.getContent());
        }
        beanStrings.add(bean.getContent().toUpperCase());
        textView.setOnClickListener(v -> {
            setBeansTextToDefault();
            binding.searchView.onActionViewExpanded();
            onHistoryClick(textView, bean);
        });

        binding.historyAql.addView(view, 0);

        if (binding.historyAql.getChildCount() > 10) {
            binding.historyAql.removeViewAt(binding.historyAql.getChildCount() - 1);
        }

        binding.historyAql.setVisibility(binding.historyAql.getChildCount() > 0 ? View.VISIBLE : View.GONE);
    }

    private void saveSearchHistory(String content) {
        if (!"".equals(content)) {
            // 如果不包含，與大於10筆
            if (!beanStrings.contains(content.toUpperCase()) && beanStrings.size() >= 10) {
                beanStrings.remove(beanStrings.size() - 1);
            }

            if (!beanStrings.contains(content.toUpperCase())) {
                String time = "" + new Date().getTime();
                DBManager.getInstance().insertSearchHistory(content, time);
                SearchBean bean = new SearchBean(0, content, time);
                setBeanView(bean);
                setBeansTextToDefault();
            }
        }
    }

    /**
     * 歷史查詢記錄Filter
     */
    private void onHistoryClick(TextView textView, SearchBean bean) {
        if (getContext() != null) {
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            textView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.selector_select_date_s));
        }
        binding.searchView.setQuery(bean.getContent(), true);
    }

    public void setBeansTextToDefault() {

    }

    public void setOnDataGetCallback(OnDataGetCallback callback) {
        this.callback = callback;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMainEvent(EventMsg eventMsg) {
        if (eventMsg.getCode() == MsgConstant.UI_NOTICE_UPDATE_TODO_OVER_VIEW_ITEM_COUNT) {
            int select = binding.tabLayout.getSelectedTabPosition();
            binding.tabLayout.removeAllTabs();
            for (BaseFragment<TodoOverviewType, ?> f : fragments) {
                if (f.getDataCount() > 0) {
                    binding.tabLayout.addTab(binding.tabLayout.newTab().setCustomView(getTabView(getString(f.getType().getResId()) + "(" + f.getDataCount() + ")", f.getDataCount())));
                } else {
                    binding.tabLayout.addTab(binding.tabLayout.newTab().setCustomView(getTabView(getString(f.getType().getResId()), f.getDataCount())));
                }
            }
            try {
                Objects.requireNonNull(binding.tabLayout.getTabAt(select)).select();
            } catch (Exception ignored) {

            }
        }
    }


    @Override
    public void action(String action, Object o) {
        if ("select".equals(action)) {
            String query = binding.searchView.getQuery().toString();
            if (!Strings.isNullOrEmpty(query)) {
                saveSearchHistory(query);
            }
        }
    }
}
