package tw.com.chainsea.ce.sdk.bean.room

data class AiServiceWarnedSocket(val roomId: String, val content:AiServiceWarnedContentSocket)


data class AiServiceWarnedContentSocket(val cancel: Boolean)