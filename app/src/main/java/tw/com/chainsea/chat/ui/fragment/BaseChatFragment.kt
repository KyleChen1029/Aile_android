package tw.com.chainsea.chat.ui.fragment

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.media.CamcorderProfile
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.common.base.Strings
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.FileSizeUnit
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.style.BottomNavBarStyle
import com.luck.picture.lib.style.PictureSelectorStyle
import com.luck.picture.lib.style.SelectMainStyle
import com.luck.picture.lib.style.TitleBarStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.android.common.log.CELog
import tw.com.chainsea.android.common.multimedia.AMediaBean
import tw.com.chainsea.android.common.multimedia.MultimediaHelper
import tw.com.chainsea.android.common.video.IVideoSize
import tw.com.chainsea.android.common.video.VideoSizeFromVideoFile
import tw.com.chainsea.ce.sdk.bean.account.UserProfileEntity
import tw.com.chainsea.ce.sdk.bean.account.UserType
import tw.com.chainsea.ce.sdk.bean.msg.ChannelType
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.bean.msg.content.VoiceContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEnum
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomType
import tw.com.chainsea.ce.sdk.database.DBManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.reference.MessageReference
import tw.com.chainsea.ce.sdk.service.UserProfileService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.keyboard.ChatKeyboardLayout.OnChatKeyBoardListener
import tw.com.chainsea.chat.keyboard.view.HadEditText.SendData
import tw.com.chainsea.chat.lib.ActivityManager
import tw.com.chainsea.chat.lib.PictureParse
import tw.com.chainsea.chat.lib.ToastUtils
import tw.com.chainsea.chat.mediagallery.view.MediaGalleryActivity
import tw.com.chainsea.chat.messagekit.lib.FileUtil
import tw.com.chainsea.chat.messagekit.lib.Global
import tw.com.chainsea.chat.messagekit.listener.OnMainMessageControlEventListener
import tw.com.chainsea.chat.network.contact.ViewModelFactory
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.ui.activity.ChatActivity
import tw.com.chainsea.chat.ui.fragment.ChatFragment.KeyBoardBarListener
import tw.com.chainsea.chat.util.GlideEngine.Companion.createGlideEngine
import tw.com.chainsea.chat.util.IntentUtil.start
import tw.com.chainsea.chat.util.IntentUtil.startIntent
import tw.com.chainsea.chat.view.chat.ChatViewModel
import java.util.TreeMap

open class BaseChatFragment : Fragment() {
    protected val chatViewModel: ChatViewModel by lazy {
        val viewModelFactory = ViewModelFactory(requireActivity().application)
        ViewModelProvider(requireActivity(), viewModelFactory)[ChatViewModel::class.java]
    }

    val keyBoardBarListener: KeyBoardBarListener by lazy { KeyBoardBarListener() }
    protected val selfUserId: String by lazy {
        TokenPref.getInstance(requireContext()).userId
    }
    val onMessageControlEventListener: OnMainMessageControlEventListener<MessageEntity> by lazy {
        object : OnMainMessageControlEventListener<MessageEntity>() {
            override fun onItemClick(entity: MessageEntity) {
            }

            /**
             * 範圍選取
             */
            override fun doRangeSelection(entity: MessageEntity) {
//                buildUpRangeScreenshotData(entity)
            }

            /**
             * 補漏訓 id to id
             * @param current  // 比較新
             * @param previous // 比較舊
             */
            override fun makeUpMessages(
                current: MessageEntity,
                previous: MessageEntity
            ) {
            }

            override fun onItemChange(entity: MessageEntity) {
            }

            override fun onInvalidAreaClick(entity: MessageEntity) {
//                binding.chatKeyboardLayout.showKeyboard()
            }

            override fun onImageClick(entity: MessageEntity) {
                if (MessageType.BUSINESS == entity.type) {
                    if (activity is ChatActivity) {
                        (activity as ChatActivity).triggerToolbarClick()
                    }
                } else if (MessageType.IMAGE == entity.type) {
                    chatViewModel.roomEntity?.let {
                        val bundle = Bundle()
                        bundle.putSerializable(BundleKey.PHOTO_GALLERY_MESSAGE.key(), entity)
                        bundle.putString(BundleKey.ROOM_ID.key(), it.id)
                        bundle.putString(BundleKey.CHAT_ROOM_NAME.key(), it.name)
                        bundle.putString(BundleKey.ROOM_TYPE.key(), ChatRoomEnum.NORMAL_ROOM.name)
                        startIntent(
                            requireContext(),
                            MediaGalleryActivity::class.java,
                            bundle
                        )
                    }
                }
            }

            override fun onLongClick(
                entity: MessageEntity,
                pressX: Int,
                pressY: Int
            ) {
//                if (binding.themeMRV.visibility == View.VISIBLE) {
//                    binding.themeMRV.visibility = View.GONE
//                }
//                if (binding.searchBottomBar.visibility == View.VISIBLE) {
//                    return
//                }
//                binding.funMedia.visibility = View.GONE
//                chatViewModel.isObserverKeyboard = false
//                actionStatus = ActionStatus.RICH_MENU
//                binding.chatKeyboardLayout.hideKeyboard()
//                binding.chatKeyboardLayout.isOpenFuncView
//                val gridMenus: MutableList<RichMenuBottom> = Lists.newArrayList()
//
//                val messageType = entity.type
//                if (!Strings.isNullOrEmpty(entity.themeId) && !Strings.isNullOrEmpty(entity.nearMessageContent)) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.REPLY_RICH, entity))
//                } else if (MessageType.AT == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.AT_RICH, entity))
//                } else if (MessageType.TEXT == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.TEXT_RICH, entity))
//                } else if (MessageType.VOICE == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.VOICE_RICH, entity))
//                } else if (MessageType.FILE == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.OTHER_RICH, entity))
//                } else if (MessageType.STICKER == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.STICKER_RICH, entity))
//                } else if (MessageType.IMAGE == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.IMAGE_RICH, entity))
//                } else if (MessageType.IMAGE_TEXT == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.OTHER_RICH, entity))
//                } else if (MessageType.CALL == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.CALL_RICH, entity))
//                } else if (MessageType.BUSINESS_TEXT == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.TEXT_RICH, entity))
//                } else if (MessageType.VIDEO == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.VIDEO_RICH, entity))
//                } else if (MessageType.TEMPLATE == messageType) {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.TEMPLATE_RICH, entity))
//                } else {
//                    gridMenus.addAll(setupRichMenuData(RichMenuType.TEXT_RICH, entity))
//                }
//                chatViewModel.roomEntity?.let {
//                    if (ChatRoomType.subscribe == it.type || !UserPref.getInstance(
//                            requireContext()
//                        ).hasBusinessSystem()
//                    ) {
//                        gridMenus.remove(RichMenuBottom.TASK)
//                    }
//
//                    //1. 如果使用者是owner(ios 只有移除諮詢服務號), 請服務號是諮詢服務號則不顯示
//                    //2. 如果是 serviceNumber opentype 有O代表對外, 不顯示
//                    if (selfUserId == it.ownerId && it.serviceNumberOpenType.contains("C")
//                        || (it.serviceNumberOpenType.contains("O") && ChatRoomType.serviceMember != it.type)
//                    ) {
//                        gridMenus.remove(RichMenuBottom.REPLY)
//                    }
//
//                    if (ChatRoomType.services == it.type) {
//                        gridMenus.remove(RichMenuBottom.RECOVER)
//                    }
//
//                    if (ChatRoomType.system == it.type) {
//                        gridMenus.remove(RichMenuBottom.TODO)
//                    }
//
//                    binding.chatKeyboardLayout.setRichMenuGridCount(5)
//                        .setOnItemClickListener(
//                            entity,
//                            gridMenus,
//                            classifyAiffInMenu(it),
//                            object : BottomRichMeunAdapter.OnItemClickListener {
//                                override fun onClick(
//                                    msg: MessageEntity,
//                                    menu: RichMenuBottom,
//                                    position: Int
//                                ) {
//                                    binding.chatKeyboardLayout.showKeyboard()
//                                    chatViewModel.isObserverKeyboard = true
//
//                                    when (menu) {
//                                        RichMenuBottom.MULTI_COPY -> {
//                                            msg.isDelete = menu.isMulti
//                                            openBottomRichMenu(
//                                                RichMenuBottom.MULTI_COPY,
//                                                OpenBottomRichMeunType.MULTIPLE_SELECTION,
//                                                Lists.newArrayList(
//                                                    RichMenuBottom.MULTI_COPY,
//                                                    RichMenuBottom.CANCEL
//                                                )
//                                            )
//                                        }
//
//                                        RichMenuBottom.MULTI_TRANSPOND -> {
//                                            msg.isDelete = menu.isMulti
//                                            openBottomRichMenu(
//                                                RichMenuBottom.MULTI_TRANSPOND,
//                                                OpenBottomRichMeunType.MULTIPLE_SELECTION,
//                                                Lists.newArrayList(
//                                                    RichMenuBottom.MULTI_TRANSPOND,
//                                                    RichMenuBottom.CANCEL
//                                                )
//                                            )
//                                        }
//
//                                        RichMenuBottom.DELETE -> {
//                                            executeDelete(Lists.newArrayList(msg))
//                                            actionStatus = ActionStatus.SCROLL
//                                        }
//
//                                        RichMenuBottom.RECOVER -> {
//                                            binding.tvTip.text =
//                                                getString(
//                                                    R.string.text_retract_tip,
//                                                    TokenPref.getInstance(requireActivity()).retractValidMinute
//                                                )
//                                            binding.scopeRetractTip.visibility = View.VISIBLE
//                                            if (TokenPref.getInstance(requireContext()).retractRemind) {
//                                                binding.scopeRetractTipText.visibility = View.GONE
//                                            }
//                                            binding.cbTip.setOnClickListener { v ->
//                                                if (binding.scopeRetractTip.visibility == View.GONE) {
//                                                    binding.scopeRetractTip.visibility = View.VISIBLE
//                                                }
//                                            }
//                                            binding.btnEdit.setOnClickListener { v ->
//                                                //編輯
//                                                if (binding.cbTip.isChecked) TokenPref.getInstance(
//                                                    requireContext()
//                                                ).retractRemind = true
//                                                executeRecover(
//                                                    Lists.newArrayList(
//                                                        msg
//                                                    )
//                                                )
//                                                actionStatus = ActionStatus.SCROLL
//                                                onTipClick(msg)
//                                            }
//                                            binding.btnRetract.setOnClickListener { v ->
//                                                //收回
//                                                if (binding.cbTip.isChecked) TokenPref.getInstance(
//                                                    requireContext()
//                                                ).retractRemind = true
//                                                executeRecover(
//                                                    Lists.newArrayList(
//                                                        msg
//                                                    )
//                                                )
//                                                actionStatus = ActionStatus.SCROLL
//                                            }
//                                        }
//
//                                        RichMenuBottom.REPLY -> {
//                                            executeReply(msg)
//                                            actionStatus = ActionStatus.SCROLL
//                                        }
//
//                                        RichMenuBottom.SHARE -> {
//                                            executeShare(msg)
//                                            actionStatus = ActionStatus.SCROLL
//                                        }
//
//                                        RichMenuBottom.SCREENSHOTS -> {
//                                            msg.isShowSelection = true
//                                            screenShotData.add(msg)
//                                            binding.xrefreshLayout.setBackgroundColor(-0xadadae)
//                                            binding.messageRV.setBackgroundColor(-0xadadae)
//                                            if (activity is ChatActivity) {
//                                                (activity as ChatActivity?)!!.showToolBar(false)
//                                            }
//                                            binding.clBottomServicedBar.visibility = View.GONE
//                                            val richMenuBottoms: List<RichMenuBottom> =
//                                                Lists.newArrayList(
//                                                    RichMenuBottom.ANONYMOUS.position(0),
//                                                    RichMenuBottom.PREVIEW.position(1),
//                                                    RichMenuBottom.SHARE.position(2),
//                                                    RichMenuBottom.CANCEL.position(3),
//                                                    RichMenuBottom.SAVE.position(4)
//                                                )
//                                            openBottomRichMenu(
//                                                RichMenuBottom.SCREENSHOTS,
//                                                OpenBottomRichMeunType.RANGE_SELECTION,
//                                                richMenuBottoms
//                                            )
//                                        }
//
//                                        RichMenuBottom.TASK -> {
//                                            msg.isShowSelection = true
//                                            screenShotData.add(msg)
//                                            binding.xrefreshLayout.setBackgroundColor(-0xadadae)
//                                            binding.messageRV.setBackgroundColor(-0xadadae)
//                                            if (activity is ChatActivity) {
//                                                (activity as ChatActivity?)!!.showToolBar(false)
//                                            }
//                                            val taskRichMenus: List<RichMenuBottom> =
//                                                Lists.newArrayList(
//                                                    RichMenuBottom.ANONYMOUS.position(0),
//                                                    RichMenuBottom.NEXT.position(1)
//                                                        .str(R.string.alert_preview),
//                                                    RichMenuBottom.CANCEL.position(2),
//                                                    RichMenuBottom.CONFIRM.position(3)
//                                                )
//                                            openBottomRichMenu(
//                                                RichMenuBottom.TASK,
//                                                OpenBottomRichMeunType.RANGE_SELECTION,
//                                                taskRichMenus
//                                            )
//                                        }
//
//                                        RichMenuBottom.TODO -> {
//                                            executeTodo(msg)
//                                            actionStatus = ActionStatus.SCROLL
//                                        }
//                                        else -> {}
//                                    }
//                                }
//
//                                override fun onCancle() {
//                                    CoroutineScope(Dispatchers.Main).launch {
//                                        delay(1000)
//                                        chatViewModel.isObserverKeyboard = true
//                                        actionStatus = ActionStatus.SCROLL
//                                    }
//                                }
//                            }
//                        ) { _, aiffId ->
//                            val aiffInfo =
//                                AiffDB.getInstance(requireContext()).aiffInfoDao.getAiffInfo(aiffId)
//                            aiffManager!!.showAiffViewByInfo(aiffInfo)
//                        }
//                }
            }

            override fun onAtSpanClick(userId: String) {
                if (activity is ChatActivity) {
                    val account = DBManager.getInstance().queryFriend(userId)
                    (activity as ChatActivity).toChatRoomByUserProfile(account)
                }
            }

            override fun onSendNameClick(sendId: String) {
//                binding.chatKeyboardLayout.appendMentionSelectById(sendId)
            }

            override fun onTipClick(entity: MessageEntity) {
//                if (MessageType.AT_or_TEXT.contains(entity.type)) {
//                    var input = ""
//                    val content: IMessageContent<*> = entity.content()
//                    if (content is TextContent) {
//                        input = content.simpleContent()
//                    } else if (content is AtContent) {
//                        chatViewModel.roomEntity?.let {
//                            val ceMentions = content.mentionContents
//                            val builder =
//                                AtMatcherHelper.matcherAtUsers("@", ceMentions, it.membersTable)
//                            input = builder.toString()
//                        }
//                    }
//
//                    if (!Strings.isNullOrEmpty(input)) {
//                        binding.chatKeyboardLayout.clearInputArea()
//                        binding.chatKeyboardLayout.setInputHETText(input)
//                        KeyboardHelper.open(requireView())
//                    }
//                }
            }

            override fun onSubscribeAgentAvatarClick(senderId: String) {
                if (activity is ChatActivity) {
                    (activity as ChatActivity).navigateToSubscribePage()
                }
            }

            override fun onAvatarClick(senderId: String) {
                chatViewModel.roomEntity?.let { entity ->
                    if (ChatRoomType.system == entity.type) {
                        return
                    }
                    UserProfileService.getProfile(
                        requireContext(),
                        RefreshSource.LOCAL,
                        senderId,
                        object : ServiceCallBack<UserProfileEntity, RefreshSource> {
                            override fun complete(
                                profileEntity: UserProfileEntity,
                                source: RefreshSource
                            ) {
                                if (ChatRoomType.services == entity.type) {
                                    ActivityManager.addActivity(activity as ChatActivity)
                                    if (!(requireContext() as ChatActivity).checkClientMainPageFromAiff()) {
                                        ActivityTransitionsControl.navigateToVisitorHomePage(
                                            requireContext(),
                                            entity.ownerId,
                                            profileEntity.roomId,
                                            UserType.VISITOR,
                                            profileEntity.nickName
                                        ) { intent: Intent, s: String? ->
                                            start(
                                                requireContext(),
                                                intent.putExtra(
                                                    BundleKey.WHERE_COME.key(),
                                                    profileEntity.name
                                                )
                                            )
                                        }
                                    }
                                    return
                                }

                                if (ChatRoomType.friend == entity.type) {
                                    val userType = profileEntity.userType
                                    if (UserType.VISITOR == userType) {
                                        Toast
                                            .makeText(
                                                requireContext(),
                                                getString(R.string.text_no_guest_page),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        return
                                    }
                                    ActivityManager.addActivity(activity as ChatActivity?)
                                    ActivityTransitionsControl.navigateToEmployeeHomePage(
                                        requireContext(),
                                        profileEntity.id,
                                        profileEntity.userType
                                    ) { intent: Intent, s: String? ->
                                        startActivityForResult(
                                            intent,
                                            100
                                        )
//                                        IS_ACTIVITY_FOR_RESULT = true
                                    }
                                } else if (!Strings.isNullOrEmpty(profileEntity.roomId)) {
                                    ActivityTransitionsControl.navigateToChat(
                                        requireActivity(),
                                        profileEntity.roomId
                                    ) { intent: Intent, _: String? ->
                                        start(
                                            requireContext(),
                                            intent
                                        )
                                        requireActivity().finish()
                                    }
                                } else {
                                    if (!(requireActivity() as ChatActivity).checkClientMainPageFromAiff()) {
                                        ActivityTransitionsControl.navigateToVisitorHomePage(
                                            requireContext(),
                                            entity.ownerId,
                                            profileEntity.roomId,
                                            UserType.VISITOR,
                                            profileEntity.nickName
                                        ) { intent: Intent, s: String? ->
                                            start(
                                                requireContext(),
                                                intent.putExtra(
                                                    BundleKey.WHERE_COME.key(),
                                                    profileEntity.name
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            override fun error(message: String) {
                                CELog.e(message)
                            }
                        }
                    )
                }
            }

            override fun onAvatarLoad(
                iv: ImageView,
                senderUrl: String?
            ) {
                if (senderUrl == null) {
                    return
                }
                if (URLUtil.isValidUrl(senderUrl)) {
                    try {
                        Glide
                            .with(requireContext())
                            .load(senderUrl)
                            .apply(
                                RequestOptions()
                                    .placeholder(R.drawable.custom_default_avatar)
                                    .error(R.drawable.custom_default_avatar)
                                    .fitCenter()
                            ).into(iv)
                    } catch (ignored: Exception) {
                    }
                }
            }

            override fun onContentUpdate(
                msgId: String,
                formatName: String,
                formatContent: String
            ) {
                MessageReference.updateMessageFormat(msgId, formatName, formatContent)
            }

            override fun copyText(entity: MessageEntity) {
//                executeCopy(Lists.newArrayList(entity))
            }

            override fun replyText(entity: MessageEntity) {
//                executeReply(entity)
            }

            override fun tranSend(entity: MessageEntity) {
//                executeTranspond(Lists.newArrayList(entity))
            }

            override fun retry(entity: MessageEntity) {
//                executeReRetry(entity)
            }

            override fun cellect(entity: MessageEntity) {
            }

            override fun shares(
                entity: MessageEntity,
                image: View
            ) {
//                executeShare(entity)
            }

            override fun choice() {
//                showChecked()
            }

            override fun delete(entity: MessageEntity) {
//                executeDelete(Lists.newArrayList(entity))
            }

            override fun enLarge(entity: MessageEntity) {
                ActivityTransitionsControl.navigateToEnLargeMessage(
                    requireContext(),
                    entity
                ) { intent: Intent, s: String? ->
                    start(
                        requireContext(),
                        intent
                    )
                    requireActivity().overridePendingTransition(
                        R.anim.open_enter,
                        R.anim.open_exit
                    )
                }
            }

            override fun onPlayComplete(msg: MessageEntity) {
                val index = chatViewModel.mainMessageData.indexOf(msg)
                if (chatViewModel.mainMessageData.size - 1 > index) {
                    for (i in index + 1 until chatViewModel.mainMessageData.size) {
                        if (MessageType.VOICE == msg.type && msg.content() is VoiceContent) {
                            val voiceContent = (msg.content() as VoiceContent)
                            if (voiceContent.isRead) {
                                continue
                            } else {
//                                val holder = binding.messageRV.adapter!!.getHolder(i)
//                                if (holder is VoiceMessageView) {
//                                    holder.playLeft()
//                                }
                                return
                            }
                        }
                    }
                }
            }

            override fun retractMsg(msg: MessageEntity) {
//                executeRecover(Lists.newArrayList(msg))
            }

            override fun showRePlyPanel(msg: MessageEntity) {
//                showThemeView(msg.themeId)
            }

            override fun findReplyMessage(messageId: String) {
                CoroutineScope(Dispatchers.IO).launch {
//                    val filterData: List<MessageEntity> = chatViewModel.mainMessageData.filter { it.id == messageId }
//                    if (filterData.isNotEmpty()) {
//                        val message = filterData[0]
//                        val index = chatViewModel.mainMessageData.indexOf(message)
//                        binding.messageRV.post {
//                            message.isAnimator = true
//                            (binding.messageRV.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
//                                index,
//                                0
//                            )
//                            binding.messageRV.adapter!!.notifyItemChanged(index)
//                        }
//                    } else {
//                        if (activity != null && !activity!!.isDestroyed) {
//                            chatViewModel.refreshMoreMsg(chatViewModel.getFirstMsgId())
//                            findReplyMessage(messageId)
//                        }
//                    }
                }
            }

            override fun onVideoClick(entity: MessageEntity) {
                val bundle = Bundle()
                bundle.putSerializable(BundleKey.PHOTO_GALLERY_MESSAGE.key(), entity)
                bundle.putString(BundleKey.ROOM_ID.key(), entity.roomId)
                bundle.putString(BundleKey.ROOM_TYPE.key(), ChatRoomEnum.NORMAL_ROOM.name)
                startIntent(
                    requireContext(),
                    MediaGalleryActivity::class.java,
                    bundle
                )
            }

            override fun updateReplyMessageWhenVideoDownload(messageId: String?) {
            }

            override fun locationMsg(msg: MessageEntity) {
            }

            override fun onStopOtherVideoPlayback(msg: MessageEntity) {
                val iterator: Iterator<MessageEntity> =
                    chatViewModel.mainMessageData.iterator()
                var index = 0
                while (iterator.hasNext()) {
                    val m = iterator.next()
                    if (MessageType.VIDEO == m.type && m.content() is VideoContent) {
                        if (m != msg && (m.content() as VideoContent).isPlaying) { // 如果不是該視頻訊息且在播放
//                            binding.messageRV.adapter!!.notifyItemChanged(index)
                        }
                    }
                    index++
                }
            }
        }
    }

    inner class KeyBoardBarListener : OnChatKeyBoardListener {
        override fun onSendBtnClick(
            sendData: SendData,
            enableSend: Boolean
        ) {
            chatViewModel.roomEntity?.let {
//                val message = chatViewModel.mainMessageData.stream()
//                    .filter { messageEntity: MessageEntity -> messageEntity.id == themeId }
//                    .findFirst().orElse(null)
//                if (message != null && message.from == ChannelType.FB && isFacebookReplyPublic) {
//                     公開回覆
//                    sendFacebookPublicReply(message, sendData)
//                } else if (message != null && message.from == ChannelType.FB && !isFacebookReplyPublic) {
//                     私訊回覆
//                    sendFacebookPrivateReply(message, sendData)
//                } else {
//                    sendMessage(sendData)
//                }
            } ?: run {
//                chatViewModel.doAddContact(userId, userName, sendData)
            }
        }

        override fun onRecordingSendAction(
            path: String,
            duration: Int
        ) {
//            chatViewModel.sendVoice(path, duration, isFacebookReplyPublic)
        }

        override fun onRecordingStartAction() {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
//                if (checkMicAvailable()) {
//                    showRecordingWindow()
//                }
            } else {
//                recordPermissionResult.launch(permission.RECORD_AUDIO)
            }
        }

        override fun onUserDefEmoticonClicked(
            tag: String,
            packageId: String
        ) {
//            presenter.sendSticker(tag, packageId);
        }

        override fun onStickerClicked(
            stickerId: String,
            packageId: String
        ) {
            chatViewModel.sendSticker(stickerId, packageId)
        }

        override fun onOpenVideo() {
//            binding.funMedia.visibility = View.GONE
//            IS_ACTIVITY_FOR_RESULT = true
//            binding.chatKeyboardLayout.clearIconState()

            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            try {
//                videoFile = createVideoFile()
//                val uri = FileProvider.getUriForFile(
//                    requireActivity(), "tw.com.chainsea.chat.fileprovider",
//                    videoFile
//                )
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                intent.putExtra(
                    MediaStore.EXTRA_VIDEO_QUALITY,
                    CamcorderProfile.QUALITY_HIGH
                )
//                sendVideoCaptureResult.launch(intent)
            } catch (error: Exception) {
//                Log.e(TAG, "error=${error.message}")
            }
        }

        /**
         * 打開文件夾列表
         */
        override fun onOpenFolders() {
//            binding.funMedia.visibility = View.GONE
//            IS_ACTIVITY_FOR_RESULT = true
//            binding.chatKeyboardLayout.clearIconState()

//            sendFileOpenResult.launch(
//                Intent(requireActivity(), FileExplorerActivity::class.java)
//            )
            requireActivity().overridePendingTransition(
                R.anim.slide_right_in,
                R.anim.slide_right_out
            )
        }

        private fun checkForSelfPermissions(permission: Array<String>): Boolean {
            for (i in permission.indices) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        permission[i]
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    return false
                }
            }
            return true
        }

        override fun onOpenCamera() {
//            binding.funMedia.visibility = View.GONE
//            IS_ACTIVITY_FOR_RESULT = true
//            binding.chatKeyboardLayout.clearIconState()

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
//                photoFile = createImageFile()
//                val uri = FileProvider.getUriForFile(
//                    requireActivity(),
//                    "tw.com.chainsea.chat.fileprovider",
//                    photoFile
//                )
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//                sendImageCaptureResult.launch(intent)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        private val digitalStyle: PictureSelectorStyle
            // set Digital Style
            get() {
                val pictureSelectorStyle = PictureSelectorStyle()

                val blueTitleBarStyle = TitleBarStyle()
                blueTitleBarStyle.titleBackgroundColor =
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary)

                val numberBlueBottomNavBarStyle = BottomNavBarStyle()
                numberBlueBottomNavBarStyle.bottomPreviewNormalTextColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_9b)
                numberBlueBottomNavBarStyle.bottomPreviewSelectTextColor =
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                numberBlueBottomNavBarStyle.bottomNarBarBackgroundColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_white)
                numberBlueBottomNavBarStyle.bottomSelectNumResources = R.drawable.album_num_selected
                numberBlueBottomNavBarStyle.bottomEditorTextColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_53575e)
                numberBlueBottomNavBarStyle.bottomOriginalTextColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_53575e)

                val numberBlueSelectMainStyle = SelectMainStyle()
                numberBlueSelectMainStyle.statusBarColor =
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                numberBlueSelectMainStyle.isSelectNumberStyle = true
                numberBlueSelectMainStyle.isPreviewSelectNumberStyle = true
                numberBlueSelectMainStyle.selectBackground = R.drawable.album_num_selector
                numberBlueSelectMainStyle.mainListBackgroundColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_white)
                numberBlueSelectMainStyle.previewSelectBackground =
                    R.drawable.album_preview_num_selector

                numberBlueSelectMainStyle.selectNormalTextColor =
                    ContextCompat.getColor(requireContext(), R.color.ps_color_9b)
                numberBlueSelectMainStyle.selectTextColor =
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                numberBlueSelectMainStyle.setSelectText(R.string.ps_completed)

                pictureSelectorStyle.titleBarStyle = blueTitleBarStyle
                pictureSelectorStyle.bottomBarStyle = numberBlueBottomNavBarStyle
                pictureSelectorStyle.selectMainStyle = numberBlueSelectMainStyle
                return pictureSelectorStyle
            }

        // 聊天室圖片選擇器
        override fun onOpenGallery() {
//            IS_ACTIVITY_FOR_RESULT = true
//            binding.chatKeyboardLayout.clearIconState()

            // 选择照片
            PictureSelector
                .create(requireActivity())
                .openGallery(SelectMimeType.ofAll())
                .setSelectorUIStyle(digitalStyle)
                .setSelectionMode(SelectModeConfig.MULTIPLE)
                .isWithSelectVideoImage(true)
                .setMaxSelectNum(9)
                .setMinSelectNum(0)
                .setMinVideoSelectNum(0)
                .setMaxVideoSelectNum(9)
                .isPreviewVideo(true)
                .isPreviewImage(true)
                .isDisplayCamera(false)
                .isOriginalSkipCompress(false)
                .isGif(true)
                .isOpenClickSound(false)
                .setSelectFilterListener { media: LocalMedia ->
                    if (media.mimeType
                            .contains("video/") &&
                        media.mimeType != "video/mp4"
                    ) {
                        ToastUtils.showToast(
                            requireActivity(),
                            getString(R.string.text_video_limit_mp4_format)
                        )
                        return@setSelectFilterListener true
                    }
                    chatViewModel.appointResp?.let { resp ->
                        if (resp.lastFrom != null && resp.lastFrom == ChannelType.LINE) {
                            if (media.mimeType
                                    .contains("image/") &&
                                media.size >= 10 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 10)
                                )
                                return@setSelectFilterListener true
                            } else if (media.mimeType
                                    .contains("video/mp4") &&
                                media.size >= 200 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 200)
                                )
                                return@setSelectFilterListener true
                            }
                        } else if (resp.lastFrom != null && resp.lastFrom == ChannelType.FB) {
                            if (media.mimeType
                                    .contains("image/") &&
                                media.size >= 25 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 25)
                                )
                                return@setSelectFilterListener true
                            } else if (media.mimeType
                                    .contains("video/mp4") &&
                                media.size >= 25 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 25)
                                )
                                return@setSelectFilterListener true
                            }
                        } else if (resp.lastFrom != null && resp.lastFrom == ChannelType.IG) {
                            if (media.mimeType
                                    .contains("image/") &&
                                media.size >= 8 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 8)
                                )
                                return@setSelectFilterListener true
                            } else if (media.mimeType
                                    .contains("video/mp4") &&
                                media.size >= 25 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 25)
                                )
                                return@setSelectFilterListener true
                            }
                        } else if (resp.lastFrom != null && resp.lastFrom == ChannelType.GOOGLE) {
                            if (media.mimeType
                                    .contains("image/") &&
                                media.size >= 5 * FileSizeUnit.MB
                            ) {
                                ToastUtils.showToast(
                                    requireActivity(),
                                    getString(R.string.text_file_size_limit, 5)
                                )
                                return@setSelectFilterListener true
                            }
                        }
                    }
                    false
                }.setImageEngine(createGlideEngine())
                .forResult(
                    object : OnResultCallbackListener<LocalMedia> {
                        override fun onResult(result: ArrayList<LocalMedia>) {
                            for (localMedia in result) {
                                val pictureType = localMedia.mimeType
                                if (Strings.isNullOrEmpty(pictureType)) {
                                    return
                                }
                                // sendFileSize = result.size();
                                if ("image/gif".equals(pictureType, ignoreCase = true)) {
                                    val bitmapBean = PictureParse.parseGifPath(requireContext(), localMedia.realPath)
                                    chatViewModel.sendGifImg(
                                        bitmapBean.url,
                                        localMedia.realPath,
                                        bitmapBean.width,
                                        bitmapBean.height
                                    )
                                } else if ("video/mp4" == pictureType) {
                                    val iVideoSize: IVideoSize =
                                        VideoSizeFromVideoFile(localMedia.realPath)
                                    chatViewModel.sendVideo(iVideoSize)
                                } else if ("image/png".equals(
                                        pictureType,
                                        ignoreCase = true
                                    ) ||
                                    "image/jpeg".equals(pictureType, ignoreCase = true)
                                ) {
                                    val path = PictureParse.parsePath(requireContext(), localMedia.realPath)
//                                chatViewModel.sendImage(path[0], path[1], isFacebookReplyPublic)
                                }
                            }
                        }

                        override fun onCancel() {
                        }
                    }
                )
        }

        /**
         * 打開表情功能
         */
        override fun onOpenEmoticon() {
//            binding.funMedia.visibility = View.GONE
        }

        /**
         * 打開錄音功能
         */
        override fun onOpenRecord() {
//            binding.funMedia.visibility = View.GONE
        }

        /**
         * 打開多媒體選擇器
         */
        override fun onOpenMultimediaSelector() {
//            binding.funMedia.setType(MultimediaHelper.Type.FILE, themeStyle, -1)
//            if (binding.funMedia.visibility == View.GONE) {
//                binding.funMedia.visibility = View.VISIBLE
//            }
        }

        /**
         * 打開圖片選擇器
         */
        override fun onOpenPhotoSelector(isChange: Boolean) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                for (permission in permissions) {
//                    val isPermissionGranted = requireContext().checkSelfPermission(permission)
//                    if (isPermissionGranted == PackageManager.PERMISSION_DENIED) {
//                        launcher.launch(
//                            arrayOf(
//                                Manifest.permission.READ_MEDIA_IMAGES,
//                                Manifest.permission.READ_MEDIA_AUDIO,
//                                Manifest.permission.READ_MEDIA_VIDEO
//                            )
//                        )
//                        return
//                    }
//                }
//                binding.funMedia.setChangeVisibility()
//                showFunMedia(false)
            } else {
                if (requireContext().checkSelfPermission(permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                    binding.funMedia.setChangeVisibility()
//                    showFunMedia(isChange)
                } else {
//                    storagePermissionResult.launch(permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }

        override fun onOpenConsult() {
        }

        /**
         * 導航到圖片選擇器預覽
         */
        override fun toMediaSelectorPreview(
            isOriginal: Boolean,
            type: String,
            current: String,
            data: TreeMap<String, String>,
            position: Int
        ) {
//            mediaPreviewARL.launch(
//                Intent(requireActivity(), MediaSelectorPreviewActivity::class.java)
//                    .putExtra(BundleKey.IS_ORIGINAL.key(), isOriginal)
//                    .putExtra(BundleKey.MAX_COUNT.key(), current)
//                    .putExtra(BundleKey.TYPE.key(), type)
//                    .putExtra(BundleKey.CURRENT.key(), current)
//                    .putExtra(BundleKey.VIDEO_POSITION.key(), position)
//                    .putExtra(BundleKey.DATA.key(), JsonHelper.getInstance().toJson(data))
//            )
        }

        override fun onSlideUpSendImage(
            type: MultimediaHelper.Type,
            list: List<AMediaBean>,
            isOriginal: Boolean
        ) {
            onMediaSelector(type, list, isOriginal)
        }

        override fun onMediaSelector(
            type: MultimediaHelper.Type,
            list: List<AMediaBean>,
            isOriginal: Boolean
        ) {
//            sendFileSize = list.size
//            isSendSingleFile = sendFileSize == 1
            when (type) {
//                MultimediaHelper.Type.IMAGE -> executeSendPhotos(list, isOriginal)
                MultimediaHelper.Type.FILE -> for (bean in list) {
                    val fileType = FileUtil.getFileType(bean.path)
                    when (fileType) {
                        Global.FileType_Png, Global.FileType_Jpg, Global.FileType_jpeg, Global.FileType_bmp -> {
                            val path = PictureParse.parsePath(requireContext(), bean.path)
//                            chatViewModel.sendImage(path[0], path[1], isFacebookReplyPublic)
                        }

                        Global.FileType_gif -> {
                            val bitmapBean = PictureParse.parseGifPath(requireContext(), bean.path)
                            chatViewModel.sendGifImg(
                                bitmapBean.url,
                                bean.path,
                                bitmapBean.width,
                                bitmapBean.height
                            )
                        }

                        Global.FileType_mov, Global.FileType_mp4, Global.FileType_rmvb, Global.FileType_avi -> {
                            val iVideoSize: IVideoSize = VideoSizeFromVideoFile(bean.path)
                            chatViewModel.sendVideo(iVideoSize)
                        }

                        else -> chatViewModel.sendFile(bean.path)
                    }
                }

//                MultimediaHelper.Type.VIDEO -> executeSendVideos(list, isOriginal)
                else -> {}
            }
        }

        override fun onInputClick() {
//            binding.funMedia.visibility = View.GONE
        }

        override fun onSoftKeyboardStartOpened(keyboardHeightInPx: Int) {
//            if (binding.scopeRobotChat.getVisibility() == View.VISIBLE)
//                binding.guideLine.setGuidelinePercent(0.79f);
        }

        override fun onSoftKeyboardEndOpened(keyboardHeightInPx: Int) {
        }

        override fun onSoftKeyboardClosed() {
//            binding.lyChildChat.onSoftKeyboardClosed(0);
//            if (binding.messageRV.adapter != null) binding.messageRV.adapter?.refreshData()

            //            if (binding.scopeRobotChat.getVisibility() == View.VISIBLE)
//                binding.guideLine.setGuidelinePercent(0.89f);
        }

        override fun onOpenExtraArea() {
            chatViewModel.roomEntity?.let {
//                binding.chatKeyboardLayout.doInitExtraArea(
//                    chatViewModel.isSettingBusinessCardInfo.value && it.listClassify == ChatRoomSource.SERVICE && it.serviceNumberOpenType.contains(
//                        "O"
//                    ) && it.type == ChatRoomType.services,
//                    it,
//                    isProvisionMember,
//                    it.serviceNumberType == ServiceNumberType.BOSS && it.serviceNumberOwnerId == selfUserId && it.type == ChatRoomType.services
//                )
                if (chatViewModel.channelType.isEmpty()) {
                    // 取得渠道名稱以發送電子名片
                    chatViewModel.getLastChannelFrom(it.id)
                }
            }
        }

        override fun onBusinessCardSend() {
//            showLoadingView(R.string.welcome_tip_04)
            chatViewModel.roomEntity?.let {
                chatViewModel.doSendBusinessCard(it.id)
            }
        }

        override fun onBusinessMemberCardSend() {
//            showLoadingView(R.string.welcome_tip_04)
            chatViewModel.roomEntity?.let {
                chatViewModel.doSendBusinessMemberCard(it.id)
            }
        }

        override fun onOpenCameraDialog() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//                if (!checkForSelfPermissions(permissionsCamera)) {
//                    cameraPermissionLauncher.launch(permissionsCamera)
//                } else {
//                    doOpenCamera()
//                }
            } else {
//                if (!checkForSelfPermissions(permissionsTIRAMISU)) {
//                    cameraPermissionLauncher.launch(permissionsTIRAMISU)
//                } else {
//                    doOpenCamera()
//                }
            }
        }
    }
}
