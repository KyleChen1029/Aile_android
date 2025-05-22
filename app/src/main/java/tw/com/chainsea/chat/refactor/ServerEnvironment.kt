package tw.com.chainsea.chat.refactor

enum class ServerEnvironment(val server: String) {
    FORMAL("F"),
    UAT("UAT"),
    DEV("DEV"),
    QA("QA"),
    SELF_DEFINE("SELF_DEFINE");

    companion object {
        fun parse(value: String): ServerEnvironment = values().firstOrNull {
            it.server == value
        } ?: FORMAL
    }
}