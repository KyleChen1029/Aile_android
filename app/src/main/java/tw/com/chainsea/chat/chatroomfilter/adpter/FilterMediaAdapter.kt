package tw.com.chainsea.chat.chatroomfilter.adpter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.ce.sdk.bean.msg.content.VideoContent
import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEnum
import tw.com.chainsea.chat.App
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.chatroomfilter.BaseFilterModel
import tw.com.chainsea.chat.chatroomfilter.DateVideHolder
import tw.com.chainsea.chat.chatroomfilter.FilterDataType
import tw.com.chainsea.chat.chatroomfilter.model.FilterMediaModel
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ItemFilterMediaBinding
import tw.com.chainsea.chat.databinding.ItemFilterTimeBinding
import tw.com.chainsea.chat.mediagallery.view.MediaGalleryActivity
import tw.com.chainsea.chat.util.DownloadUtil
import tw.com.chainsea.chat.util.IntentUtil.startIntent

class FilterMediaAdapter : BaseFilterAdapter() {
    private var roomId: String = ""
    private var sort: String = "DESC"

    fun setRoomId(roomId: String) {
        this.roomId = roomId
    }

    fun setSort(sort: String) {
        this.sort = sort
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        when (viewType) {
            FilterDataType.DATE.ordinal -> {
                val binding =
                    ItemFilterTimeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return DateVideHolder(binding)
            }

            FilterDataType.MEDIA.ordinal -> {
                val binding =
                    ItemFilterMediaBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return FilterMediaViewHolder(binding)
            }

            else -> {
                val binding =
                    ItemFilterMediaBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return FilterMediaViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (getItemViewType(position)) {
            FilterDataType.DATE.ordinal -> {
                (holder as DateVideHolder).bind(getItem(position))
            }

            FilterDataType.MEDIA.ordinal -> {
                (holder as FilterMediaViewHolder).bind(getItem(position), position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).type == MessageType.SYSTEM) {
            FilterDataType.DATE.ordinal
        } else {
            FilterDataType.MEDIA.ordinal
        }

    inner class FilterMediaViewHolder(
        private val binding: ItemFilterMediaBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            baseFilterModel: BaseFilterModel,
            position: Int
        ) = CoroutineScope(Dispatchers.IO).launch {
            baseFilterModel as FilterMediaModel
            binding.ivImage.tag = position
            checkIsMultipleChoiceMode(binding.tvSelect, baseFilterModel)

            withContext(Dispatchers.Main) {
                val layoutParams = binding.root.layoutParams
                val phoneWidth = binding.root.context.resources.displayMetrics.widthPixels
                layoutParams.width = phoneWidth / 3
                layoutParams.height = phoneWidth / 3
                binding.root.layoutParams = layoutParams
                binding.root.invalidate()
                binding.root.requestLayout()
            }

            CoroutineScope(Dispatchers.Main).launch {
                if (baseFilterModel.type == MessageType.VIDEO) {
                    binding.tvVideoDuration.text =
                        if (baseFilterModel.videoDuration == 0.0) {
                            val videoContent = baseFilterModel.messageEntity.content() as VideoContent
                            val path =
                                DownloadUtil.downloadFileDir + baseFilterModel.messageEntity.sendTime + "_" + videoContent.name
                            getVideoDuration(path)
                        } else {
                            getUpdateTimeFormat((baseFilterModel.videoDuration * 1000).toInt())
                        }
                    binding.tvVideoDuration.visibility = View.VISIBLE
                } else {
                    binding.tvVideoDuration.visibility = View.GONE
                }

                binding.root.setOnClickListener {
                    if (isMultipleChoiceMode) {
                        // 多選模式
                        addMultipleChoiceItem<FilterMediaModel>(binding.tvSelect, baseFilterModel)
                    } else {
                        val bundle = Bundle()
                        bundle.putSerializable(
                            BundleKey.PHOTO_GALLERY_MESSAGE.key(),
                            baseFilterModel.messageEntity
                        )
                        bundle.putString(BundleKey.ROOM_ID.key(), roomId)
                        bundle.putString(BundleKey.ROOM_TYPE.key(), ChatRoomEnum.NORMAL_ROOM.name)
                        bundle.putBoolean(BundleKey.IS_FROM_FILTER.key(), true)
                        bundle.putString(BundleKey.MESSAGE_SORT.key(), sort)
                        startIntent(
                            binding.root.context,
                            MediaGalleryActivity::class.java,
                            bundle
                        )
                    }
                }
            }

            Glide
                .with(App.getInstance())
                .load(baseFilterModel.thumbnail)
                // 以下設定可以讓 adapter 刷新時不會閃爍
                .transition(DrawableTransitionOptions.withCrossFade(100))
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .dontAnimate()
                .into(
                    object : CustomTarget<Drawable>() {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            if (binding.ivImage.tag != position) return
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.tvVideoDuration.visibility = View.GONE
                                binding.clLoadingThumbnailError.visibility = View.VISIBLE
                                binding.ivImage.visibility = View.GONE
                                if (baseFilterModel.type == MessageType.VIDEO) {
                                    binding.ivLoadingThumbnailError.setImageResource(R.drawable.icon_filter_error_video)
                                } else {
                                    binding.ivLoadingThumbnailError.setImageResource(R.drawable.icon_filter_error_image)
                                }
                            }
                        }

                        override fun onLoadStarted(placeholder: Drawable?) {
                            super.onLoadStarted(placeholder)
                            if (binding.ivImage.tag != position) return
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            if (binding.ivImage.tag != position) return
                            CoroutineScope(Dispatchers.Main).launch {
                                binding.clLoadingThumbnailError.visibility = View.GONE
                                binding.ivImage.visibility = View.VISIBLE
                                binding.ivImage.setImageDrawable(resource)
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    }
                )
//                .submit()
        }

        /**
         * 若影片長度超過 1 小時, 格式為 h:mm:ss, e.g., 影片時長1小時3分06秒, 顯示為 1:03:06
         * 若影片長度少於 1 小時, 格式為 m:ss, e.g., 影片時長3分06秒, 顯示為 3:06
         * 若影片時長為5秒，則顯示為 0:05
         */
        @SuppressLint("DefaultLocale")
        private suspend fun getUpdateTimeFormat(millisecond: Int): String =
            withContext(Dispatchers.IO) {
                // 将毫秒转换为秒
                val second = millisecond / 1000
                // 计算小时
                val hh = second / 3600
                // 计算分钟
                val mm = second % 3600 / 60
                // 计算秒
                val ss = second % 60
                // 判断时间单位的位数
                val str: String =
                    if (hh != 0) { // 表示时间单位为三位
                        String.format("%02d:%02d:%02d", hh, mm, ss)
                    } else {
                        String.format("%02d:%02d", mm, ss)
                    }
                return@withContext str
            }

        private suspend fun getVideoDuration(videoPath: String?): String =
            withContext(Dispatchers.IO) {
                // 設定影片路徑
                try {
                    videoPath?.let {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(it)
                        // 取得影片長度
                        val time =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        time?.let {
                            val timeInMilliSec = it.toInt()
                            retriever.release()
                            return@withContext getUpdateTimeFormat(timeInMilliSec)
                        } ?: run {
                            retriever.release()
                            return@withContext "00:00"
                        }
                    } ?: run {
                        return@withContext "00:00"
                    }
                } catch (e: Exception) {
                }
                return@withContext "00:00"
            }
    }
}
