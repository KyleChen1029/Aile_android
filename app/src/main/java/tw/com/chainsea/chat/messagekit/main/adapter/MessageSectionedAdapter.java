package tw.com.chainsea.chat.messagekit.main.adapter;

/**
 * current by evan on 2020-01-09
 */
//public class MessageSectionedAdapter extends SectionedRecyclerViewAdapter<MessageSectionedAdapter.HeaderViewHolder, MessageViewBase, MessageSectionedAdapter.FooterViewHolder> {
//    private static final String TAG = MessageSectionedAdapter.class.getSimpleName();
//
//
//    // data
//    private List<Sectioned<MessageEntity, Enum>> list = Lists.newArrayList();
//    private List<MessageEntity> entities = Lists.newArrayList();
//    private ChatRoomEntity chatRoomEntity;
//
//    // ui control
//    private Map<Integer, MessageViewBase> holders = Maps.newHashMap();
//    private boolean isShowCheckBox = false;
//    private String keyword = "";
//    private boolean isClickEventEnable = true;
//    private OnMainMessageControlEventListener<MessageEntity> onMessageControlEventListener;
//
//
//    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMdd日(EEE)", Locale.TAIWAN);
//
//    public MessageSectionedAdapter(List<MessageEntity> entities, ChatRoomEntity chatRoomEntity) {
//        this.entities = entities;
//        this.chatRoomEntity = chatRoomEntity;
//    }
//
//    @Override
//    protected int getSectionCount() {
//        return list.size();
//    }
//
//    @Override
//    protected int getItemCountForSection(int section) {
//        return list.get(section).getDatas().size();
//    }
//
//    @Override
//    protected boolean hasFooterInSection(int section) {
//        return false;
//    }
//
//    @Override
//    protected HeaderViewHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.msgkit_tip, null);
//        return new HeaderViewHolder(v);
//    }
//
//    @Override
//    protected FooterViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
//        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.msgkit_tip, null);
//        return new FooterViewHolder(v);
//    }
//
//    @Override
//    protected MessageViewBase onCreateItemViewHolder(ViewGroup parent, int viewType) {
//        MessageViewBase holder = null;
//        View v = null;
//        ChatRoomType type = this.chatRoomEntity.getTodoOverviewType();
//        if (type.equals(ChatRoomType.SUBSCRIBE) || type.equals(ChatRoomType.SERVICES)) {
//            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.uikit_msg_bubble_need_user, null);
//        } else {
//            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.uikit_msg_bubble, null);
//        }
//        switch (viewType) {
//            case 1:
//                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.msgkit_tip, parent, false);
//                holder = new TipMessageView(v);
//                break;
//            case 2:
//                holder = new ReplyMessageView(v);
//                break;
//            case 3:
//                holder = new TextMessageView(v);
//                break;
//            case 4:
//                holder = new AtMessageView(v);
//                break;
//            case 5:
//                holder = new ImageMessageView(v);
//                break;
//            case 6:
//                holder = new StickerMessageView(v);
//                break;
//            case 7:
//                holder = new FileMessageView(v);
//                break;
//            case 8:
//                holder = new VoiceMessageView(v);
//                break;
//            case 9:
//                holder = new VideoMessageView(v);
//                break;
//            case 10:
//                holder = new CallMessageView(v);
//                break;
//            case -99:
//            default:
//                holder = new NoneMessageView(v);
//                Log.i(TAG, viewType + "");
//                break;
//        }
//
//        return holder.setClickEventEnable(this.isClickEventEnable)
//                .serChatRoomEntity(this.chatRoomEntity)
//                .setKeyword(this.keyword)
//                .setOnMessageControlEventListener(this.onMessageControlEventListener);
//    }
//
//    @Override
//    protected void onBindSectionHeaderViewHolder(HeaderViewHolder holder, int section) {
//        holder.tvTip.setBackgroundResource(R.drawable.time_msg_bg);
//        holder.tvTip.setText(this.list.get(section).getName());
//        holder.tvTip.setTextColor(0xFF76B9CB);
////        Log.i(TAG, "" + holder.itemView + "");
////        holder.itemView.setGravity(Gravity.CENTER_HORIZONTAL);
////        holder.refresh(null);
//    }
//
//    @Override
//    protected void onBindItemViewHolder(MessageViewBase holder, int section, int position) {
//        holders.put(position, holder);
//        this.list.get(section).getDatas().get(position).setShowChecked(this.isShowCheckBox);
//
//        if (this.onMessageControlEventListener != null) {
//            this.onMessageControlEventListener.onItemChange(this.list.get(section).getDatas().get(position));
//        }
//        holder.setKeyword(this.keyword)
//                .setClickEventEnable(this.isClickEventEnable)
//                .refresh(this.list.get(section).getDatas().get(position));
//    }
//
//    @Override
//    protected void onBindSectionFooterViewHolder(FooterViewHolder holder, int section) {
//
//    }
//
//
//    @Override
//    protected int getSectionItemViewType(int section, int position) {
//        MessageEntity message = list.get(section).getDatas().get(position);
//        MessageType type = message.getTodoOverviewType();
//        // EVAN_FLAG 2020-02-10 (1.9.1) 如果是 (收回、系統訊息、以下未讀提示、時間線)
//        if (MessageFlag.RETRACT.equals(message.getFlag()) || SourceType.SYSTEM.equals(message.getSourceType())) {
//            return 1;
//        }
//        // EVAN_FLAG 2020-02-10 (1.9.1) 如果是主題訊息
//        if (!Strings.isNullOrEmpty(message.getThemeId())) {
//            return 2;
//        }
//
//        switch (type) {
//            case TEXT:
//                return 3;
//            case AT:
//                return 4;
//            case IMAGE:
//                return 5;
//            case STICKER:
//                return 6;
//            case FILE:
//                return 7;
//            case VOICE:
//                return 8;
//            case VIDEO:
//                return 9;
//            case CALL:
//                return 10;
//            default:
//                return -99;
//        }
//    }
//
//
//    public void sectionedMessageByDate(List<MessageEntity> messages) {
//        sort(messages);
//        ListMultimap<String, MessageEntity> multimap = ArrayListMultimap.create();
//
//        Iterator<MessageEntity> iterator = messages.iterator();
//        while (iterator.hasNext()) {
//            MessageEntity message = iterator.next();
//            String date = simpleDateFormat.format(message.getSendTime());
//            multimap.put(date, message);
//        }
//
//        this.list.clear();
//        for (Map.Entry<String, Collection<MessageEntity>> entry : multimap.asMap().entrySet()) {
//            Sectioned sectioned = Sectioned.Build()
//                    .isOpen(false)
//                    .name(entry.getKey())
//                    .content("")
//                    .type(null)
//                    .datas(Lists.newArrayList(entry.getValue()))
//                    .build();
//            this.list.add(sectioned);
//        }
//    }
//
//    public void append(List<MessageEntity> entities) {
//
//    }
//
//    public void refreshData() {
//        sectionedMessageByDate(this.entities);
//        for (Sectioned<MessageEntity, Enum> sectioned : this.list) {
//            filter(sectioned.getDatas());
//            sort(sectioned.getDatas());
//        }
//        notifyDataSetChanged();
//    }
//
//    public void refreshData(int position, MessageEntity m) {
//        sectionedMessageByDate(this.entities);
//        for (Sectioned<MessageEntity, Enum> sectioned : this.list) {
//            filter(sectioned.getDatas());
//            sort(sectioned.getDatas());
//        }
//        notifyItemChanged(position, m);
//    }
//
//
//    public int indexOfSectioned(MessageEntity entity) {
//        sectionedMessageByDate(this.entities);
//        String date = simpleDateFormat.format(entity.getSendTime());
//        int index = this.list.indexOf(Sectioned.Build().name(date).build());
//        return index + 1;
//    }
//
//    private static void filter(List<MessageEntity> entities) {
//
//    }
//
//    private static void sort(List<MessageEntity> entities) {
//        Collections.sort(entities);
//    }
//
//    public MessageSectionedAdapter setKeyword(String keyword) {
//        this.keyword = keyword;
//        return this;
//    }
//
//    public MessageViewBase getHolder(int pos) {
//        return holders.get(pos);
//    }
//
//    public MessageSectionedAdapter setClickEventEnable(boolean isClickEventEnable) {
//        this.isClickEventEnable = isClickEventEnable;
//        return this;
//    }
//
//
//    public MessageSectionedAdapter setOnMessageControlEventListener(OnMainMessageControlEventListener onMessageControlEventListener) {
//        this.onMessageControlEventListener = onMessageControlEventListener;
//        return this;
//    }
//
//
//    public MessageSectionedAdapter setIsShowCheckBox(boolean isShowCheckBox) {
//        this.isShowCheckBox = isShowCheckBox;
//        return this;
//    }
//
//    public boolean getIsShowCheckBox() {
//        return this.isShowCheckBox;
//    }
//
//
//    class HeaderViewHolder extends RecyclerView.ViewHolder {
////            MessageViewBase {
//
//        @Nullable
//        @BindView(R.id.msgkit_tip_content)
//        TextView tvTip;
//
//        public HeaderViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//
////        @Override
////        protected void findViews(View itemView) {
////            ButterKnife.bind(this, itemView);
////        }
//
////        @Override
////        public void refresh(MessageEntity item) {
////            Log.i(TAG, "");
////            containerLL.setGravity(Gravity.CENTER_HORIZONTAL);
////        }
//    }
//
//    class FooterViewHolder extends RecyclerView.ViewHolder {
//
//        public FooterViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//    }
//}
