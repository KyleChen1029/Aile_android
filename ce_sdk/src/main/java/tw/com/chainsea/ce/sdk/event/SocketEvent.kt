package tw.com.chainsea.ce.sdk.event

class SocketEvent(val type: SocketEventEnum, val data: Any) {
    constructor(type: SocketEventEnum) : this(type, Any())
}


