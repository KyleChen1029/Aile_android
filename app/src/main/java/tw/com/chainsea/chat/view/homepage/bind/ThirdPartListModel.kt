package tw.com.chainsea.chat.view.homepage.bind

data class ThirdPartListModel(
    val icon: Int,
    var id: String = "",
    var name: String,
    var fansPageString: String = "",
    val type: ThirdPartEnum
)