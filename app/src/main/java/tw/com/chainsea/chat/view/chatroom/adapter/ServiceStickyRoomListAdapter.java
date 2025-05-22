package tw.com.chainsea.chat.view.chatroom.adapter;


/**
 * Created by yushuangping on 2018/8/23.
 */

//public class ServiceStickyRoomListAdapter extends SectionedRecyclerViewAdapter<ServiceStickyRoomListAdapter.HeaderHolder, ServiceStickyRoomListAdapter.ServiceContentHolder, RecyclerView.ViewHolder> {
//
//    private Context ctcx;
//    public List<ServiceRoomSection> sections = Lists.newArrayList();
//    private OnItemClickListener onItemClickListener;
//    //记录下哪个section是被打开的
//    private SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();
//
//    public ServiceStickyRoomListAdapter(Context ctcx) {
//        this.ctcx = ctcx;
//    }
//
//    protected int getSectionCount() {
//        return Util.isEmpty(this.sections) ? 0 : this.sections.size();
//    }
//
//    @Override
//    protected int getItemCountForSection(int section) {
//        int count = this.sections.get(section).getTagInfoList().size();
//        if (count >= 1 && !this.sparseBooleanArray.get(section)) {
//            count = 0;
//        }
//        if (section == 0 && this.sparseBooleanArray.get(section)) {
//            count = this.sections.get(section).getTagInfoList().size();
//        }
//        return Util.isEmpty(this.sections.get(section).getTagInfoList()) ? 0 : count;
//    }
//
//    @Override
//    protected boolean hasFooterInSection(int section) {
//        return false;
//    }
//
//    @Override
//    protected HeaderHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.service_num_name_item, parent, false);
//        return new HeaderHolder(view);
//    }
//
//    @Override
//    protected ServiceContentHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(getContext()).inflate(R.layout.service_num_content_item, parent, false);
//        return new ServiceContentHolder(view);
//    }
//
//    @Override
//    protected RecyclerView.ViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
//        return null;
//    }
//
//    @Override
//    protected void onBindSectionHeaderViewHolder(final HeaderHolder holder, final int section) {
//        holder.itemView.setOnClickListener(v -> {
//            boolean isOpen = this.sparseBooleanArray.get(section);
//            this.sparseBooleanArray.put(section, !isOpen);
//            notifyDataSetChanged();
//        });
//        holder.titleView.setText(this.sections.get(section).getSectionName());
//        holder.openView.setImageResource(this.sparseBooleanArray.get(section) ? R.drawable.ic_expand : R.drawable.ic_close);
//    }
//
//    @Override
//    protected void onBindSectionFooterViewHolder(RecyclerView.ViewHolder holder, int section) {
//
//    }
//
//    @Override
//    protected void onBindItemViewHolder(ServiceContentHolder holder, final int section, final int position) {
//        String logo = NetConfig.getInstance().avatarUrl(this.sections.get(section).getTagInfoList().get(position).getTagUrl(), PicSize.SMALL);
//        if (URLUtil.isValidUrl(logo)) {
//            Glide.with(getContext())
//                    .load(logo)
//                    .apply(new RequestOptions()
//                            .placeholder(R.drawable.default_avatar)
//                            .error(R.drawable.default_avatar)
//                            .fitCenter())
//                    .into(holder.itemLogo);
//        } else {
//            Glide.with(getContext())
//                    .load(R.drawable.default_avatar)
//                    .into(holder.itemLogo);
//        }
//
//        holder.serviceNumberIconCIV.setVisibility(View.GONE);
//        holder.itemTitle.setText(this.sections.get(section).getTagInfoList().get(position).getTagName());
//        if (this.sections.get(section).getTagInfoList().get(position).getMSessions().size() == 1) {
//            ChatRoomEntity session = this.sections.get(section).getTagInfoList().get(position).getMSessions().get(0);
//
//
//            holder.serviceNumberIconCIV.setVisibility(View.GONE);
//            holder.itemServiceTime.setVisibility(View.GONE);
//            // EVAN_FLAG 2020-02-15 (1.9.1) 最後一筆訊息時間處理 and 渠道
//            MessageEntity lastMessageEntity = session.getLastMessage();
//            if (lastMessageEntity != null){
//                holder.itemServiceTime.setVisibility(View.VISIBLE);
//                if (lastMessageEntity.getSendTime() <= 0){
//                    holder.itemServiceTime.setVisibility(View.GONE);
//                }else {
//                    holder.itemServiceTime.setText(TimeUtil.getTimeShowString(lastMessageEntity.getSendTime(), true));
//                }
//
//
//                // EVAN_FLAG 2020-02-15 (1.9.1) 渠道資料處理
//                ChannelType channel = lastMessageEntity.getFrom();
//                if (channel == null) {
//                    holder.serviceNumberIconCIV.setVisibility(View.GONE);
//                } else {
//                    switch (channel) {
//                        case FB:
//                            holder.serviceNumberIconCIV.setImageResource(R.drawable.facebook_icon);
//                            break;
//                        case LINE:
//                            holder.serviceNumberIconCIV.setImageResource(R.drawable.line_icon);
//                            break;
//                        case QBI:
//                            holder.serviceNumberIconCIV.setImageResource(R.drawable.qbi_icon);
//                            break;
//                        case WEICHAT:
//                            holder.serviceNumberIconCIV.setImageResource(R.drawable.wechat_icon);
//                            break;
//                        case GOOGLE:
//                            holder.serviceNumberIconCIV.setImageResource(R.drawable.google_icon);
//                            break;
//                        case CE:
//                        case UNDEF:
//                        default:
//                            holder.serviceNumberIconCIV.setVisibility(View.GONE);
//                            break;
//                    }
//                }
//            }else {
////                holder.tvTime.setVisibility(View.GONE);
//            }
//
//
////            if (session.getMFrom() != null) {
////                holder.serviceNumberIconCIV.setVisibility(View.VISIBLE);
////                ChannelType from = session.getMFrom();
////                switch (from) {
////                    case FB:
////                        holder.serviceNumberIconCIV.setImageResource(R.drawable.facebook_icon);
////                        break;
////                    case LINE:
////                        holder.serviceNumberIconCIV.setImageResource(R.drawable.line_icon);
////                        break;
////                    case QBI:
////                        holder.serviceNumberIconCIV.setImageResource(R.drawable.qbi_icon);
////                        break;
////                    case WEICHAT:
////                        holder.serviceNumberIconCIV.setImageResource(R.drawable.wechat_icon);
////                        break;
////                    case GOOGLE:
////                        holder.serviceNumberIconCIV.setImageResource(R.drawable.google_icon);
////                        break;
////                    case CE:
////                    case UNDEF:
////                    default:
////                        holder.serviceNumberIconCIV.setVisibility(View.GONE);
////                        break;
////                }
////            }
////            String senderId = session.getSenderId();
////            String senderName = session.getSenderName();
//            String userId = UserPref.getInstance(getContext()).getUserId();
//            if (TextUtils.isEmpty(senderName) && !TextUtils.isEmpty(senderId)) {
//                UserProfileEntity accountCE = DBManager.getInstance().queryFriend(senderId);
//                String alias = accountCE.getAlias();
//                String nickname = accountCE.getNickname();
//                if (accountCE != null && !TextUtils.isEmpty(alias)) {
//                    senderName = alias;
//                } else if (accountCE != null && !TextUtils.isEmpty(nickname)) {
//                    senderName = nickname;
//                } else {
//                    senderName = "";
//                }
//            }
//
//            if (Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(session.getSenderId()) || !userId.equals(session.getSenderId())) {
//                holder.itemContent.setText(senderName + ": " + session.getContent());
//            } else {
//                holder.itemContent.setText("我: " + session.getContent());
//            }
//
////            final long milliseconds = this.sections.get(section).getTagInfoList().get(position).getMSessions().get(0).getTime();
////            if (milliseconds != 0) {
////                holder.itemServiceTime.setText(TimeUtil.getTimeShowString(milliseconds, true));
////            }
//
//            holder.itemSize.setVisibility(View.GONE);
//            holder.itemIcon.setVisibility(View.GONE);
////            holder.itemServiceTime.setVisibility(View.VISIBLE);
//        } else {
//            holder.itemContent.setText("");
//            holder.itemContent.setVisibility(View.GONE);
//            holder.itemSize.setVisibility(View.VISIBLE);
//            holder.itemIcon.setVisibility(View.VISIBLE);
////            holder.itemServiceTime.setVisibility(View.GONE);
//        }
//
//        holder.itemSize.setText(this.sections.get(section).getTagInfoList().get(position).getMSessions().size() + "");
//        holder.itemView.setOnClickListener(view -> {
//            if (this.onItemClickListener != null) {
//                this.onItemClickListener.onItemClick(section, position);
//            }
//        });
//    }
//
//    public ServiceStickyRoomListAdapter setProfileData(List<ServiceRoomSection> sectionss) {
//        this.sections = sectionss;
//        sparseBooleanArray.put(0, true);
//        return this;
//    }
//
//    public ServiceStickyRoomListAdapter setOnItemClick(OnItemClickListener listener) {
//        this.onItemClickListener = listener;
//        return this;
//    }
//
//
//    public void refreshData() {
////        sort(this.chatRooms);
//        notifyDataSetChanged();
//    }
//
//    /**
//     * 更新聊天室未完成編輯內容
//     *
//     * @param entity
//     * @return
//     */
//    public ServiceStickyRoomListAdapter changeUnEditByRoomId(ChatRoomEntity entity) {
//        if (entity == null || this.sections == null || this.sections.isEmpty()) {
//            return this;
//        }
//        Iterator<ServiceRoomSection> iterator = this.sections.iterator();
//        while (iterator.hasNext()) {
//            ServiceRoomSection section = iterator.next();
//            Iterator<TagInfo> iteratorTag = section.getTagInfoList().iterator();
//            while (iteratorTag.hasNext()) {
//                TagInfo tagInfo = iteratorTag.next();
//                Iterator<ChatRoomEntity> iteratorEntity = tagInfo.getMSessions().iterator();
//                while (iteratorEntity.hasNext()) {
//                    ChatRoomEntity chat = iteratorEntity.next();
//                    if (chat.getId().equals(entity.getId())) {
//                        chat.setUnfinishedEditedTime(entity.getUnfinishedEditedTime());
//                        chat.setUnfinishedEdited(entity.getUnfinishedEdited());
//                    }
//                }
//            }
//        }
//        return this;
//    }
//
//    public interface OnItemClickListener {
//        void onItemClick(int section, int position);
//    }
//
//
//    public class HeaderHolder extends RecyclerView.ViewHolder {
//        @BindView(R.id.tv_title)
//        TextView titleView;
//        @BindView(R.id.tv_open)
//        ImageView openView;
//
//        public HeaderHolder(View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//    }
//
//    public class ServiceContentHolder extends RecyclerView.ViewHolder {
//
//        @BindView(R.id.itemRl)
//        RelativeLayout itemRl;
//        @BindView(R.id.item_logo)
//        ImageView itemLogo;
//        @BindView(R.id.serviceNumberIconCIV)
//        ImageView serviceNumberIconCIV;
//        @BindView(R.id.icon)
//        ImageView itemIcon;
//        @BindView(R.id.item_title)
//        TextView itemTitle;
//        @BindView(R.id.item_content)
//        TextView itemContent;
//        @BindView(R.id.item_size)
//        TextView itemSize;
//        @BindView(R.id.service_time)
//        TextView itemServiceTime;
//
//        public ServiceContentHolder(View itemView) {
//            super(itemView);
//            ButterKnife.bind(this, itemView);
//        }
//    }
//}
