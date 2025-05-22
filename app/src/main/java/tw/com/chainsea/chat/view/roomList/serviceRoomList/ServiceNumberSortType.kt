package tw.com.chainsea.chat.view.roomList.serviceRoomList

enum class ServiceNumberSortType {
    BY_GROUP, BY_TIME;

    companion object {
        fun of(value: Int): ServiceNumberSortType {
            return when (value) {
                0 -> BY_GROUP
                else -> BY_TIME
            }
        }
    }
}