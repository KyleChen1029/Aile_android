package tw.com.chainsea.chat.view.account

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ActivityChangeTenantBinding
import tw.com.chainsea.chat.service.ActivityTransitionsControl
import tw.com.chainsea.chat.util.SystemKit
import tw.com.chainsea.chat.view.BaseActivity
import tw.com.chainsea.chat.view.account.adapter.TenantAdapter
import tw.com.chainsea.chat.view.group.GroupCreateActivity
import tw.com.chainsea.custom.view.alert.AlertView

class ChangeTenantActivity : BaseActivity() {


    private lateinit var binding: ActivityChangeTenantBinding
    private val tenantAdapter by lazy {  TenantAdapter(::showChangeTenantDialog, TokenPref.getInstance(this).cpCurrentTenant) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeTenantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
        initListener()
    }


    private fun initView() {
        val tenantList = TokenPref.getInstance(this).cpRelationTenantList
        tenantAdapter.setData(tenantList)
        binding.rvTenantList.apply {
            layoutManager = LinearLayoutManager(this@ChangeTenantActivity)
            adapter = tenantAdapter
        }
    }

    private fun showChangeTenantDialog(relationTenant: RelationTenant) {
        AlertView.Builder().setContext(this)
            .setStyle(AlertView.Style.Alert)
            .setTitle("切換團隊")
            .setMessage("你確定要切換到 ${relationTenant.abbreviationTenantName} 嗎？")
            .setOthers(
                arrayOf(
                    getString(R.string.cancel),
                    getString(R.string.text_for_sure)
                ))
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 1) {
                    SystemKit.changeTenant(this, relationTenant, false)
                }
            }
            .build()
            .setCancelable(true).show()
    }

    private fun initListener() {
        binding.ivAddTeam.setOnClickListener {
            showTenantAction()
        }
    }

    private fun showTenantAction() {
        AlertView.Builder().setContext(this)
            .setStyle(AlertView.Style.ActionSheet)
            .setOthers(
                arrayOf(
                    getString(R.string.join_group),
                    getString(R.string.create_group)
                ))
            .setCancelText(getString(R.string.alert_cancel))
            .setOnItemClickListener { o: Any?, position: Int ->
                if (position == 0) {
                    ActivityTransitionsControl.navigateScannerJoinGroup(this)
                } else if (position == 1) {
                    startActivity(Intent(this, GroupCreateActivity::class.java))
                }
            }
            .build()
            .setCancelable(true).show()
    }
}