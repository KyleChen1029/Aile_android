package tw.com.chainsea.chat.view.homepage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.base.Strings;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import tw.com.chainsea.android.common.ui.UiHelper;
import tw.com.chainsea.ce.sdk.bean.CustomerEntity;
import tw.com.chainsea.ce.sdk.bean.account.UserType;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.config.BundleKey;
import tw.com.chainsea.chat.databinding.FragmentCustomerListBinding;
import tw.com.chainsea.chat.databinding.ItemContactPersonCustomerBinding;
import tw.com.chainsea.chat.network.contact.ViewModelFactory;
import tw.com.chainsea.chat.searchfilter.viewmodel.ContactPersonClientSearchViewModel;
import tw.com.chainsea.chat.service.ActivityTransitionsControl;
import tw.com.chainsea.chat.util.IntentUtil;
import tw.com.chainsea.chat.util.NoDoubleClickListener;
import tw.com.chainsea.chat.view.homepage.viewmodel.ServiceNumberViewModel;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemBaseViewHolder;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperCallback;
import tw.com.chainsea.custom.view.recyclerview.itemdecoration.ItemTouchHelperExtension;

public class CustomerListFragment extends Fragment {
    private FragmentCustomerListBinding binding;
    private Context context;
    private ServiceNumberViewModel viewModel;

    private ContactPersonClientSearchViewModel contactViewModel;

    private RecyclerAdapter adapter = new RecyclerAdapter();

    public static CustomerListFragment newInstance(String serviceNumberId) {
        CustomerListFragment customerListFragment = new CustomerListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKey.SERVICE_NUMBER_ID.key(), serviceNumberId);
        customerListFragment.setArguments(bundle);
        return customerListFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_customer_list, container, false);
        ViewGroup.LayoutParams params = binding.viewSpace.getLayoutParams();
        params.height = UiHelper.dip2px(context, 24);
        binding.viewSpace.setLayoutParams(params);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initViewModel();
        observeData();
        initListener();
    }

    private void initListener() {
        binding.toolbar.leftAction.setOnClickListener(clickListener);
        binding.searchView.setOnClickListener(v -> binding.searchView.setIconified(false));
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    private void initView() {
        binding.toolbar.title.setText(getString(R.string.customer_list));
        ItemTouchHelperExtension itemTouchHelper = new ItemTouchHelperExtension(new ItemTouchHelperCallback(ItemTouchHelper.START));
        itemTouchHelper.attachToRecyclerView(binding.recycler);
        adapter.setItemTouchHelperExtension(itemTouchHelper);
        binding.recycler.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        binding.recycler.setAdapter(adapter);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setItemAnimator(new DefaultItemAnimator());
        binding.recycler.setNestedScrollingEnabled(false);
    }

    private void observeData() {
        viewModel.getCustomers().observe(requireActivity(), customerEntities -> {
            adapter.setData(customerEntities);
            binding.toolbar.title.setText(MessageFormat.format(getString(R.string.customer_list) + "({0})", customerEntities.size()));
        });

        contactViewModel.getToCustomerChatRoom().observe(requireActivity(), roomId -> {
            if (getContext() != null) {
                ActivityTransitionsControl.navigateToChat(getContext(), roomId, (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
            }
        });

        contactViewModel.getToCustomerChatRoomError().observe(requireActivity(), errorMessage -> {
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
        });
    }

    private void initViewModel() {
        ViewModelFactory viewModelFactory = new ViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(requireActivity()).get(ServiceNumberViewModel.class);
        contactViewModel = new ViewModelProvider(this, viewModelFactory).get(ContactPersonClientSearchViewModel.class);
    }

    private final NoDoubleClickListener clickListener = new NoDoubleClickListener() {
        @Override
        protected void onNoDoubleClick(View v) {
            if (v.equals(binding.toolbar.leftAction)) {
                requireActivity().onBackPressed();
            }
        }
    };

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> implements Filterable {
        private Context context;
        private List<CustomerEntity> data;
        private List<CustomerEntity> filterData;
        private ItemTouchHelperExtension itemTouchHelperExtension;
        private NameFilter filter;

        @SuppressLint("NotifyDataSetChanged")
        public void setData(List<CustomerEntity> data) {
            this.data = data;
            filterData = new ArrayList<>(data);
            notifyDataSetChanged();
        }

        public RecyclerAdapter setItemTouchHelperExtension(ItemTouchHelperExtension itemTouchHelperExtension) {
            this.itemTouchHelperExtension = itemTouchHelperExtension;
            return this;
        }

        @NonNull
        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            context = parent.getContext();
            return new ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_contact_person_customer, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
            CustomerEntity entity = data.get(position);
            holder.binding.ivHome.setVisibility(View.VISIBLE);
            holder.binding.ivCall.setVisibility(View.GONE);
            holder.binding.tvName.setText(!Strings.isNullOrEmpty(entity.getName()) ? entity.getName() : entity.getNickName());
            holder.binding.civIcon.loadAvatarIcon(entity.getAvatarId(), entity.getNickName(), entity.getId());
            holder.binding.clContentItem.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) itemTouchHelperExtension.closeOpened();
                String roomId = DBManager.getInstance().queryCustomBossServiceId(entity.getId());
                if (roomId.isEmpty()) {
                    String serviceNumberId = getArguments().getString(BundleKey.SERVICE_NUMBER_ID.key(), "");
                    contactViewModel.getServiceCustomerRoomId(serviceNumberId, entity.getId());
                } else {
                    ActivityTransitionsControl.navigateToChat(holder.itemView.getContext(), roomId, (intent, s) -> IntentUtil.INSTANCE.start(requireContext(), intent));
                }
            });

            holder.binding.civIcon.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) itemTouchHelperExtension.closeOpened();
                ActivityTransitionsControl.navigateToVisitorHomePage(getContext(), entity.getId(), entity.getRoomId(), UserType.VISITOR, entity.getNickName(), (intent, s) ->
                    startActivity(intent.putExtra(BundleKey.WHERE_COME.key(), getClass().getSimpleName()))
                );
            });

            holder.binding.ivHome.setOnClickListener(v -> {
                if (itemTouchHelperExtension != null) itemTouchHelperExtension.closeOpened();
                ActivityTransitionsControl.navigateToVisitorHomePage(getContext(), entity.getId(), entity.getRoomId(), UserType.VISITOR, entity.getNickName(), (intent, s) ->
                    startActivity(intent.putExtra(BundleKey.WHERE_COME.key(), getClass().getSimpleName()))
                );
            });
        }

        @Override
        public int getItemCount() {
            return (data != null) ? data.size() : 0;
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new NameFilter();
            }
            return filter;
        }

        private static class ViewHolder extends ItemBaseViewHolder {
            ItemContactPersonCustomerBinding binding;

            private ViewHolder(ItemContactPersonCustomerBinding binding) {
                super(binding.getRoot());
                super.setContentItemView(binding.clContentItem);
                this.binding = binding;
            }
        }

        private class NameFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<CustomerEntity> results = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    results.addAll(filterData);
                } else {
                    for (CustomerEntity entity : filterData) {
                        if ((entity.getName() != null && entity.getName().contains(constraint)) || (entity.getNickName() != null && entity.getNickName().contains(constraint))) {
                            results.add(entity);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = results;
                return filterResults;
            }

            @Override
            @SuppressLint("NotifyDataSetChanged")
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data.clear();
                data.addAll((Collection<? extends CustomerEntity>) results.values);
                notifyDataSetChanged();
            }
        }
    }
}
