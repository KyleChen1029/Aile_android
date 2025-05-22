package tw.com.chainsea.chat.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import tw.com.chainsea.ce.sdk.network.model.response.ServicesIdentityListResponse
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemServicesIdentityBinding

class ServicesIdentityListAdapter(
    private val identities: List<ServicesIdentityListResponse>,
    val callback: (ServicesIdentityListResponse) -> Unit
) : Adapter<IdentityListViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): IdentityListViewHolder {
        val binding =
            ItemServicesIdentityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IdentityListViewHolder(binding, callback)
    }

    override fun getItemCount(): Int = identities.size

    override fun onBindViewHolder(
        holder: IdentityListViewHolder,
        position: Int
    ) {
        holder.bind(identities[position])
    }
}

class IdentityListViewHolder(
    val binding: ItemServicesIdentityBinding,
    val callback: (ServicesIdentityListResponse) -> Unit
) : ViewHolder(binding.root) {
    @SuppressLint("StringFormatMatches")
    fun bind(servicesIdentityListResponse: ServicesIdentityListResponse) {
        binding.ivIdentity.loadAvatarIcon(
            servicesIdentityListResponse.avatarId,
            servicesIdentityListResponse.name,
            servicesIdentityListResponse.serviceNumberId
        )

        binding.tvIdentityName.text =
            String.format(
                binding.root.context.getString(
                    R.string.switch_identity_toast,
                    servicesIdentityListResponse.text
                )
            )

        binding.root.setOnClickListener {
            callback.invoke(servicesIdentityListResponse)
        }
    }
}
