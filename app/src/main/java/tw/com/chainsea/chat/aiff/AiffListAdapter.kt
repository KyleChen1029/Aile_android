package tw.com.chainsea.chat.aiff

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import com.google.common.collect.Lists
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.aiff.database.AiffDB
import tw.com.chainsea.chat.aiff.database.entity.AiffInfo
import tw.com.chainsea.chat.databinding.ItemAiffRichMenuBinding
import tw.com.chainsea.chat.ui.adapter.entity.RichMenuInfo
import tw.com.chainsea.chat.util.AvatarKit
import tw.com.chainsea.chat.util.NameKit

class AiffListAdapter(private val roomId: String) :
    RecyclerView.Adapter<AiffListAdapter.AiffViewHolder>() {
    private var aiffList: MutableList<RichMenuInfo> = mutableListOf()
    val nameKit = NameKit()
    val avatarKit = AvatarKit()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AiffViewHolder {
        val binding =
            ItemAiffRichMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AiffViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return aiffList.size
    }

    override fun onBindViewHolder(holder: AiffViewHolder, position: Int) {
        holder.bind(aiffList[position])
    }

    fun setData(richMenuInfo: List<RichMenuInfo>) {
        this.aiffList.clear()
        this.aiffList.addAll(richMenuInfo)
        notifyItemRangeChanged(0, aiffList.size)
    }


    private fun sort() {
        CoroutineScope(Dispatchers.IO).launch {
            aiffList.sort()
            withContext(Dispatchers.Main) {
                notifyItemRangeChanged(0, aiffList.size)
            }
        }
    }

    inner class AiffViewHolder(private val binding: ItemAiffRichMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(aiffInfo: RichMenuInfo) {
            val shortName: String = nameKit.getAvatarName(aiffInfo.name)
            binding.tvMenuTitle.text = aiffInfo.title
            binding.tvMenuName.text = aiffInfo.name
            if (aiffInfo.pinTimestamp != 0L) {
                binding.ivMenuPin.visibility = View.VISIBLE
                binding.ivPin.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.icon_un_pin
                    )
                )
            } else {
                binding.ivMenuPin.visibility = View.GONE
                binding.ivPin.setImageDrawable(
                    ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.icon_pin
                    )
                )
            }

            if (Strings.isNullOrEmpty(aiffInfo.image)) {
                binding.ivAiffIcon.visibility = View.GONE
                binding.tvAvatar.visibility = View.VISIBLE
                binding.tvAvatar.text = shortName
                val gradientDrawable = binding.tvAvatar.background as GradientDrawable
                gradientDrawable.setColor(Color.parseColor(nameKit.getBackgroundColor(shortName)))
            } else avatarKit.loadCEAvatar(
                aiffInfo.image,
                binding.ivAiffIcon,
                binding.tvAvatar,
                aiffInfo.name
            )

            binding.clPin.setOnClickListener {
                val info = AiffDB.getInstance(it.context).aiffInfoDao.getAiffInfo(aiffInfo.id);
                if (info.pinTimestamp == 0L) {
                    aiffInfo.pinTimestamp = System.currentTimeMillis()
                    info.pinTimestamp = System.currentTimeMillis()
                    binding.ivPin.setImageDrawable(
                        ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.icon_un_pin
                    ))
                } else {
                    aiffInfo.pinTimestamp = 0L
                    info.pinTimestamp = 0L
                    binding.ivPin.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.icon_pin
                        ))
                }
                binding.esLayout.resetStatus()
                AiffDB.getInstance(it.context).aiffInfoDao.upsert(info)
                sort()
            }

            binding.clContentView.setOnClickListener {
                val info: AiffInfo =
                    AiffDB.getInstance(it.context).aiffInfoDao.getAiffInfo(aiffInfo.id)
                val aiffManager = AiffManager(it.context, roomId)
                aiffManager.showAiffViewByInfo(info)
                val useTimestamp = System.currentTimeMillis()
                info.useTimestamp = useTimestamp
                AiffDB.getInstance(it.context).aiffInfoDao.upsert(info)
                aiffInfo.useTimestamp = useTimestamp
                sort()
            }
        }
    }
}