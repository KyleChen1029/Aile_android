package tw.com.chainsea.chat.view.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.ce.sdk.http.cp.respone.RelationTenant
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemTenantBinding
import tw.com.chainsea.chat.util.AvatarKit
import tw.com.chainsea.chat.util.UnreadUtil

class OtherTenantAdapter(
    private val joinTenant: (View) -> Unit,
    private val changeTenant: (RelationTenant) -> Unit
) : RecyclerView.Adapter<OtherTenantAdapter.OtherTenantViewHolder>() {
    private val data = arrayListOf<RelationTenant>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OtherTenantViewHolder {
        val binding = ItemTenantBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OtherTenantViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(
        holder: OtherTenantViewHolder,
        position: Int
    ) {
        holder.bind(data[position])
    }

    fun setData(data: List<RelationTenant>) {
        val originSize = data.size
        this.data.clear()
        this.data.addAll(data)
        val size = data.size
        notifyItemRangeChanged(originSize, size)
    }

    inner class OtherTenantViewHolder(
        private val binding: ItemTenantBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val avatarKit = AvatarKit()

        fun bind(data: RelationTenant) {
            binding.txtName.text = data.abbreviationTenantName
            if (data.tenantId.isEmpty()) {
                binding.root.setOnClickListener {
                    joinTenant.invoke(binding.txtName)
                }
                binding.img.setImageResource(R.drawable.icon_join_group)
            } else {
                binding.root.setOnClickListener {
                    changeTenant.invoke(data)
                }
                avatarKit.loadCpTenantAvatar(data.avatarId, binding.img)
            }
            binding.txtUnread.visibility = if (data.unReadNum > 0) View.VISIBLE else View.GONE
            binding.txtUnread.text = UnreadUtil.getUnreadText(data.unReadNum, isCanOver = false)
        }
    }
}
