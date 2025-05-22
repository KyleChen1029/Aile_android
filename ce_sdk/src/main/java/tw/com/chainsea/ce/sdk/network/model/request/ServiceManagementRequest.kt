package tw.com.chainsea.ce.sdk.network.model.request

data class ServiceManagementRequest(val serviceNumberId: String, val userIds: List<String>)

data class ServiceModifyOwnerRequest(val serviceNumberId: String, val userId: String)

data class GetServiceItemRequest(val serviceNumberId: String)
