package tw.com.chainsea.chat.view.account.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemTenantBinding
import tw.com.chainsea.chat.util.AvatarKit

class TenantAdapter(
    private val changeTenant: (RelationTenant) -> Unit, private val currentTenant: RelationTenant
) : RecyclerView.Adapter<TenantAdapter.TenantViewHolder>() {

    private val relationTenantList = arrayListOf<RelationTenant>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val binding = ItemTenantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TenantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        holder.bind(relationTenantList[position])
    }

    override fun getItemCount(): Int {
        return relationTenantList.size
    }

    fun setData(data: List<RelationTenant>) {
        val originSize = data.size
        this.relationTenantList.clear()
        this.relationTenantList.addAll(data)
        val size = data.size
        notifyItemRangeChanged(originSize, size)
    }

    inner class TenantViewHolder(private val binding: ItemTenantBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val avatarKit = AvatarKit()
        fun bind(relationTenant: RelationTenant) {
            binding.txtName.text = relationTenant.abbreviationTenantName
            binding.txtName.setTextColor(Color.BLACK)
            binding.root.setOnClickListener {
                changeTenant.invoke(relationTenant)
            }
            avatarKit.loadCpTenantAvatar(relationTenant.avatarId, binding.img)

            if (relationTenant.tenantId == currentTenant.tenantId) {
                itemView.setBackgroundColor(Color.parseColor("#f0faff"))
                binding.ivCheck.visibility = View.VISIBLE
            } else {
                itemView.setBackgroundColor(Color.parseColor("#ffffff"))
                binding.ivCheck.visibility = View.GONE
            }
        }
    }
}


