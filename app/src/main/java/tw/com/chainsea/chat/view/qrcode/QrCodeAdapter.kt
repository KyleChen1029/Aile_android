package tw.com.chainsea.chat.view.qrcode

import android.content.ClipData
import android.content.ClipboardManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.CLIPBOARD_SERVICE
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.bean.common.EnableType
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberType
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity
import tw.com.chainsea.ce.sdk.database.sp.TokenPref
import tw.com.chainsea.ce.sdk.reference.UserProfileReference
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ItemQrCodeBinding

class QrCodeAdapter : ListAdapter<QrCodeData, QrCodeAdapter.QrCodeViewHolder>(QrCodeListAdapterDiffCallBack()) {
    private val tempHolder = HashMap<Int, QrCodeViewHolder>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): QrCodeViewHolder {
        val binding = ItemQrCodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QrCodeViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: QrCodeViewHolder,
        position: Int
    ) {
        tempHolder[position] = holder
        holder.bind(getItem(position))
    }

    fun getCurrentQrCodeData(position: Int): QrCodeData = getItem(position)

    suspend fun isCanShowBottomBar(position: Int): Boolean =
        withContext(Dispatchers.Main) {
            val qrCodeData = getItem(position)
            qrCodeData.serviceNumber?.let {
                return@withContext if (it.enable == EnableType.N) {
                    false
                } else if (it.businessCardInfo == null) {
                    false
                } else if (Strings.isNullOrEmpty(it.allChannelURL) || it.businessCardInfo.imageCardUrl.isEmpty() || it.businessCardInfo.shareLiffUrl.isEmpty()) {
                    false
                } else {
                    true
                }
            } ?: run {
                return@withContext false
            }
        }

    suspend fun getCurrentTitle(position: Int): String =
        withContext(Dispatchers.IO) {
            return@withContext if (getItem(position).serviceNumberType == ServiceNumberType.BOSS) {
                "商務號"
            } else {
                "官方服務號"
            }
        }

    inner class QrCodeViewHolder(
        val binding: ItemQrCodeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(qrCodeData: QrCodeData) {
            if (qrCodeData.qrCodeLink.isNotEmpty()) {
                binding.icCantUse.visibility = View.GONE
                binding.ivProgressBar.visibility = View.GONE
                binding.qrCode.setImageBitmap(qrCodeData.qrCode)
                qrCodeData.serviceNumber?.let {
                    binding.icLogo.visibility = View.VISIBLE
                    binding.icLogo.loadAvatarIcon(
                        it.avatarId,
                        it.name,
                        it.serviceNumberId
                    )
                }
            }

            qrCodeData.serviceNumber?.let {
                when (it.serviceNumberType) {
                    ServiceNumberType.BOSS.type -> {
                        setBossServiceNumber(it, qrCodeData)
                    }

                    ServiceNumberType.OFFICIAL.type -> {
                        setOfficialServiceNumber(it, qrCodeData)
                    }

                    else -> {
                        // nothing
                    }
                }
            } ?: run {
                when (qrCodeData.serviceNumberType) {
                    ServiceNumberType.BOSS -> {
                        setBossServiceNumberNotEnable(qrCodeData)
                    }

                    ServiceNumberType.OFFICIAL -> {
                        binding.icCantUse.visibility = View.VISIBLE
                        binding.qrCode.setImageResource(R.mipmap.icon_fake_qr_qr_code)
                        binding.ivProgressBar.visibility = View.GONE
                        binding.tvNotice.text = binding.tvNotice.context.getString(R.string.text_qr_code_boss_service_number_not_enable)
                    }

                    else -> {
                        // nothing
                    }
                }
            }
        }

        private fun setBossServiceNumberNotEnable(qrCodeData: QrCodeData) =
            CoroutineScope(Dispatchers.IO).launch {
                val selfUserProfile =
                    UserProfileReference.findById(
                        null,
                        TokenPref.getInstance(binding.root.context).userId
                    )
                withContext(Dispatchers.Main) {
                    binding.icCantUse.visibility = View.VISIBLE
                    binding.qrCode.setImageResource(R.mipmap.icon_fake_qr_qr_code)
                    binding.ivProgressBar.visibility = View.GONE
                    binding.tvNotice.text = binding.tvNotice.context.getString(R.string.text_qr_code_boss_service_number_not_enable)
                    binding.tvTenantName.text = qrCodeData.tenantName
                    binding.tvDuty.text = qrCodeData.duty
                    binding.tvName.text = selfUserProfile.nickName
                    binding.icLogo.visibility = View.VISIBLE
                }
            }

        // 商務號
        private fun setBossServiceNumber(
            serviceNumberEntity: ServiceNumberEntity,
            qrCodeData: QrCodeData
        ) = CoroutineScope(Dispatchers.Main).launch {
            binding.ivAvatar.loadAvatarIcon(
                serviceNumberEntity.avatarId,
                serviceNumberEntity.name,
                serviceNumberEntity.serviceNumberId
            )
            binding.icLogo.loadAvatarIcon(
                serviceNumberEntity.avatarId,
                serviceNumberEntity.name,
                serviceNumberEntity.serviceNumberId
            )

            withContext(Dispatchers.IO) {
                val selfUserProfile =
                    UserProfileReference.findById(
                        null,
                        TokenPref.getInstance(binding.root.context).userId
                    )
                withContext(Dispatchers.Main) {
                    binding.tvName.text = selfUserProfile.nickName
                }
            }
            binding.tvTenantName.text = qrCodeData.tenantName
            binding.tvDuty.text = qrCodeData.duty
            when {
                // 商務號未啟用
                serviceNumberEntity.enable == EnableType.N -> {
                    setBossServiceNumberNotEnable(qrCodeData)
                }

                // 商務號未設定電子名片
                serviceNumberEntity.businessCardInfo == null -> {
                    setNotSettingBusinessCard(qrCodeData)
                }

                // 商務號未設定電子名片
                serviceNumberEntity.businessCardInfo != null &&
                    Strings.isNullOrEmpty(serviceNumberEntity.allChannelURL) ||
                    serviceNumberEntity.businessCardInfo!!.imageCardUrl.isEmpty() ||
                    serviceNumberEntity.businessCardInfo!!.shareLiffUrl.isEmpty() -> {
                    setNotSettingBusinessCard(qrCodeData)
                }

                else -> {
                    binding.icCantUse.visibility = View.GONE
                    binding.tvNotice.text =
                        String.format(
                            binding.root.context.getString(R.string.text_slide_up_notice),
                            binding.root.context.getString(R.string.text_qrcode_business_title)
                        )
                    binding.qrCode.setOnLongClickListener {
                        val clipboard: ClipboardManager =
                            it.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip =
                            ClipData.newPlainText(
                                it.context.getString(R.string.app_name),
                                qrCodeData.qrCodeLink
                            )
                        clipboard.setPrimaryClip(clip)
                        Toast
                            .makeText(
                                it.context,
                                it.context.getString(R.string.text_qr_code_copy_success),
                                Toast.LENGTH_SHORT
                            ).show()
                        true
                    }
                }
            }
        }

        private fun setNotSettingBusinessCard(qrCodeData: QrCodeData) {
            if (qrCodeData.isLoading) {
                binding.icCantUse.visibility = View.GONE
                binding.ivProgressBar.visibility = View.VISIBLE
                binding.icLogo.visibility = View.GONE
            } else {
                binding.icCantUse.visibility = View.VISIBLE
                binding.ivProgressBar.visibility = View.GONE
                binding.icLogo.visibility = View.VISIBLE
            }
            binding.qrCode.setImageResource(R.mipmap.icon_fake_qr_qr_code)
            binding.tvNotice.text =
                binding.tvNotice.context.getString(R.string.text_qr_code_boss_service_not_have_business_card)
        }

        // 官方服務號
        private fun setOfficialServiceNumber(
            serviceNumberEntity: ServiceNumberEntity,
            qrCodeData: QrCodeData
        ) {
            if (serviceNumberEntity.serviceNumberId != null &&
                serviceNumberEntity.serviceNumberId.isNotEmpty() &&
                qrCodeData.qrCode != null &&
                qrCodeData.qrCodeLink.isNotEmpty() ||
                (serviceNumberEntity.faceBookFansPages != null || serviceNumberEntity.instagramFansPages != null)
            ) {
                binding.tvName.text = serviceNumberEntity.name
                binding.ivAvatar.loadAvatarIcon(
                    serviceNumberEntity.avatarId,
                    serviceNumberEntity.name,
                    serviceNumberEntity.serviceNumberId
                )
                binding.icLogo.visibility = View.VISIBLE
                binding.icLogo.loadAvatarIcon(
                    serviceNumberEntity.avatarId,
                    serviceNumberEntity.name,
                    serviceNumberEntity.serviceNumberId
                )
                binding.tvNotice.text =
                    String.format(
                        binding.root.context.getString(R.string.text_slide_up_notice),
                        binding.root.context.getString(R.string.text_qr_code__title_official_service_number)
                    )
                binding.qrCode.setOnLongClickListener {
                    val clipboard: ClipboardManager =
                        it.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip =
                        ClipData.newPlainText(
                            it.context.getString(R.string.app_name),
                            qrCodeData.qrCodeLink
                        )
                    clipboard.setPrimaryClip(clip)
                    Toast
                        .makeText(
                            it.context,
                            it.context.getString(R.string.text_qr_code_copy_success),
                            Toast.LENGTH_SHORT
                        ).show()
                    true
                }
            } else {
                binding.tvName.text = binding.root.context.getString(R.string.text_qr_code__title_official_service_number)
                binding.tvDuty.visibility = View.INVISIBLE
                binding.tvTenantName.visibility = View.INVISIBLE
                val notice = binding.tvNotice.context.getString(R.string.text_qr_code_coming_soon)
                binding.tvNotice.text = notice
                if (qrCodeData.isLoading) {
                    binding.icCantUse.visibility = View.GONE
                    binding.ivProgressBar.visibility = View.VISIBLE
                } else {
                    binding.icCantUse.visibility = View.GONE
                    binding.ivProgressBar.visibility = View.GONE
                }
                binding.icLogo.visibility = View.VISIBLE
                binding.icCantUse.visibility = View.VISIBLE
                binding.ivAvatar.loadAvatarIcon(
                    binding.root.context.getString(R.string.text_qr_code__title_official_service_number),
                    binding.root.context.getString(R.string.text_qr_code__title_official_service_number),
                    binding.root.context.getString(R.string.text_qr_code__title_official_service_number)
                )
                binding.icLogo.loadAvatarIcon(
                    binding.root.context.getString(R.string.text_qr_code__title_official_service_number),
                    binding.root.context.getString(R.string.text_qr_code__title_official_service_number),
                    binding.root.context.getString(R.string.text_qr_code__title_official_service_number)
                )
            }
        }
    }

    class QrCodeListAdapterDiffCallBack : DiffUtil.ItemCallback<QrCodeData>() {
        override fun areItemsTheSame(
            oldItem: QrCodeData,
            newItem: QrCodeData
        ): Boolean {
            oldItem.serviceNumber?.let { oldServiceNumber ->
                newItem.serviceNumber?.let { newServiceNumber ->
                    return oldServiceNumber.serviceNumberId == newServiceNumber.serviceNumberId
                }
            }
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: QrCodeData,
            newItem: QrCodeData
        ): Boolean = false
    }
}
