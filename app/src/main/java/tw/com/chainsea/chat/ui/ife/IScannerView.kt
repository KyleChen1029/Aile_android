package tw.com.chainsea.chat.ui.ife

import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant

interface IScannerView {
    fun onJoinSuccess(relationTenant: RelationTenant)
    fun onJoinFailure(error: String)
    fun showLoading()
    fun dismissLoading()
}