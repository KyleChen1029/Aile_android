package tw.com.chainsea.chat.view.base

enum class PreloadStepEnum(val type: Int) {
    SYNC_LABEL(1),
    SYNC_TODO(2),
    SYNC_CONTACT_PERSON(3),
    SYNC_SERVICE_NUMBER(4),
    SYNC_CHAT_ROOM(5),
    SYNC_DONE(6);

    companion object {
        fun of(type: Int):PreloadStepEnum {
            PreloadStepEnum.values().forEach {
                if (it.ordinal == type) return it
            }
            return SYNC_LABEL
        }
    }
}