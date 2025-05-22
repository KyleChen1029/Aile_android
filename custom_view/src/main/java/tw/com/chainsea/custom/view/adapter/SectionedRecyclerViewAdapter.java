package tw.com.chainsea.custom.view.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tw.com.chainsea.custom.view.recyclerview.AnimationAdapter;

public abstract class SectionedRecyclerViewAdapter<H extends RecyclerView.ViewHolder,
    VH extends RecyclerView.ViewHolder,
    F extends RecyclerView.ViewHolder>
    extends AnimationAdapter<RecyclerView.ViewHolder> {

    protected static final int TYPE_SECTION_HEADER = -1;
    protected static final int TYPE_SECTION_FOOTER = -2;
    protected static final int TYPE_ITEM = -3;

    private int[] sectionForPosition = null;
    private int[] positionWithinSection = null;
    private boolean[] isHeader = null;
    private boolean[] isFooter = null;
    private int count = 0;

    private Context context;

    public SectionedRecyclerViewAdapter() {
        super();
        registerAdapterDataObserver(new SectionDataObserver());
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        setupIndices();
    }

    /**
     * Returns the sum of number of items for each section plus headers and footers if they
     * are provided.
     */
    @Override
    public int getItemCount() {
        return count;
    }

    private void setupIndices() {
        count = countItems();
        allocateAuxiliaryArrays(count);
        precomputeIndices();
    }

    private int countItems() {
        int count = 0;
        int sections = getSectionCount();

        for (int i = 0; i < sections; i++) {
            count += 1 + getItemCountForSection(i) + (hasFooterInSection(i) ? 1 : 0);
        }
        return count;
    }

    private void precomputeIndices() {
        int sections = getSectionCount();
        int index = 0;

        for (int i = 0; i < sections; i++) {
            // 設置頭部
            setPrecomputedItem(index, new ItemInfo(true, false, i, 0));
            index++;

            // 設置項目
            for (int j = 0; j < getItemCountForSection(i); j++) {
                setPrecomputedItem(index, new ItemInfo(false, false, i, j));
                index++;
            }

            // 設置尾部
            if (hasFooterInSection(i)) {
                setPrecomputedItem(index, new ItemInfo(false, true, i, 0));
                index++;
            }
        }
    }

    // 內部靜態類封裝項目信息
    private static class ItemInfo {
        final boolean isHeader;
        final boolean isFooter;
        final int section;
        final int position;

        ItemInfo(boolean isHeader, boolean isFooter, int section, int position) {
            this.isHeader = isHeader;
            this.isFooter = isFooter;
            this.section = section;
            this.position = position;
        }
    }

    private void setPrecomputedItem(int index, ItemInfo info) {
        this.isHeader[index] = info.isHeader;
        this.isFooter[index] = info.isFooter;
        sectionForPosition[index] = info.section;
        positionWithinSection[index] = info.position;
    }

    private void allocateAuxiliaryArrays(int count) {
        sectionForPosition = new int[count];
        positionWithinSection = new int[count];
        isHeader = new boolean[count];
        isFooter = new boolean[count];
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        RecyclerView.ViewHolder viewHolder;

        if (isSectionHeaderViewType(viewType)) {
            viewHolder = onCreateSectionHeaderViewHolder(parent, viewType);
        } else if (isSectionFooterViewType(viewType)) {
            viewHolder = onCreateSectionFooterViewHolder(parent, viewType);
        } else {
            viewHolder = onCreateItemViewHolder(parent, viewType);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int section = sectionForPosition[position];
        int index = positionWithinSection[position];

        if (isSectionHeaderPosition(position)) {
            onBindSectionHeaderViewHolder((H) holder, section);
        } else if (isSectionFooterPosition(position)) {
            onBindSectionFooterViewHolder((F) holder, section);
        } else {
            onBindItemViewHolder((VH) holder, section, index);
        }
    }

    @Override
    public int getItemViewType(int position) {

        if (sectionForPosition == null) {
            setupIndices();
        }

        int section = sectionForPosition[position];
        int index = positionWithinSection[position];

        if (isSectionHeaderPosition(position)) {
            return getSectionHeaderViewType(section);
        } else if (isSectionFooterPosition(position)) {
            return getSectionFooterViewType(section);
        } else {
            return getSectionItemViewType(section, index);
        }

    }

    protected int getSectionHeaderViewType(int section) {
        return TYPE_SECTION_HEADER;
    }

    protected int getSectionFooterViewType(int section) {
        return TYPE_SECTION_FOOTER;
    }

    protected int getSectionItemViewType(int section, int position) {
        return TYPE_ITEM;
    }

    /**
     * Returns true if the argument position corresponds to a header
     */
    public boolean isSectionHeaderPosition(int position) {
        if (isHeader == null) {
            setupIndices();
        }
        return isHeader[position];
    }

    /**
     * Returns true if the argument position corresponds to a footer
     */
    public boolean isSectionFooterPosition(int position) {
        if (isFooter == null) {
            setupIndices();
        }
        return isFooter[position];
    }

    protected boolean isSectionHeaderViewType(int viewType) {
        return viewType == TYPE_SECTION_HEADER;
    }

    protected boolean isSectionFooterViewType(int viewType) {
        return viewType == TYPE_SECTION_FOOTER;
    }

    public Context getContext() {
        return this.context;
    }

    /**
     * Returns the number of sections in the RecyclerView
     */
    protected abstract int getSectionCount();

    /**
     * Returns the number of items for a given section
     */
    protected abstract int getItemCountForSection(int section);

    /**
     * Returns true if a given section should have a footer
     */
    protected abstract boolean hasFooterInSection(int section);

    /**
     * Creates a ViewHolder of class H for a Header
     */
    protected abstract H onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType);

    /**
     * Creates a ViewHolder of class F for a Footer
     */
    protected abstract F onCreateSectionFooterViewHolder(ViewGroup parent, int viewType);

    /**
     * Creates a ViewHolder of class VH for an Item
     */
    protected abstract VH onCreateItemViewHolder(ViewGroup parent, int viewType);

    /**
     * Binds data to the header view of a given section
     */
    protected abstract void onBindSectionHeaderViewHolder(H holder, int section);

    /**
     * Binds data to the footer view of a given section
     */
    protected abstract void onBindSectionFooterViewHolder(F holder, int section);

    /**
     * Binds data to the item view for a given position within a section
     */
    protected abstract void onBindItemViewHolder(VH holder, int section, int position);

    class SectionDataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            setupIndices();
        }
    }

    public int getItemPosition(int position) {
        return positionWithinSection[position];
    }
}
