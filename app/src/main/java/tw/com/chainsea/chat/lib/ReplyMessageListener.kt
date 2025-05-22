package tw.com.chainsea.chat.lib

import tw.com.chainsea.ce.sdk.http.ce.request.MessageReplyRequest

open class ReplyMessageListener: MessageReplyRequest.Listener {

    override fun onSuccess(id: String?, sendNum: Int, sendTime: Long, roomId: String?) {
        ChatService.getInstance().sendMsgSuccess(id, sendNum, sendTime, roomId)
    }

    override fun onFailed(roomId: String?, msgId: String?, errorMessage: String?) {
        ChatService.getInstance().handleSendMessageFail(roomId, msgId)
    }
}