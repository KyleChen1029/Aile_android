package tw.com.chainsea.chat.messagekit.listener

import tw.com.aile.sdk.bean.message.MessageEntity

interface OnRobotChatMessageClickListener {
    fun onLinkClickListener(link: String)
}

interface OnChatRoomTitleChangeListener {
    fun onTitleChangeListener(title: String)
}

interface OnTemplateClickListener {
    fun onTemplateClick(content: String)
}

interface OnMessageSlideReply {
    fun onMessageSlideReply(messageEntity: MessageEntity)
}

interface PlayerCallback {
    fun onVideoPlayedTimeChanged(currentTime: Int, duration: Int)
}

interface OnFacebookReplyClick {
    fun onPublicReply(message: MessageEntity, postId: String, commentId: String)

    fun onPrivateReply(message: MessageEntity)
}

interface CheckFacebookCommentStatus {
    fun checkStatus(message: MessageEntity)
}