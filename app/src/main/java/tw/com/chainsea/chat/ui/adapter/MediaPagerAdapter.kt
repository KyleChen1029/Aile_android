package tw.com.chainsea.chat.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.chat.mediagallery.view.MediaGalleryActivity
import tw.com.chainsea.chat.mediagallery.view.MediaGalleryFragment

class MediaPagerAdapter(fragmentActivity: MediaGalleryActivity) : FragmentStateAdapter(fragmentActivity) {
    private var mediaList: List<MessageEntity> = emptyList()
    fun setData(data: List<MessageEntity>) {
        mediaList = data
    }
    override fun getItemCount(): Int = mediaList.size
    override fun createFragment(position: Int): Fragment {
        return MediaGalleryFragment.newInstance(mediaList[position])
    }
}
