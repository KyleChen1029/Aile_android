package tw.com.chainsea.chat.messagekit.main

data class FileDownloadSuccessEvent(
    val messageId: String,
    val filePath: String
)