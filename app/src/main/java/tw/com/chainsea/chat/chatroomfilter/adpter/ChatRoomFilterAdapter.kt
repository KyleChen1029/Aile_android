package tw.com.chainsea.chat.chatroomfilter.adpter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import tw.com.chainsea.chat.chatroomfilter.BaseChatRoomFilterFragment

class ChatRoomFilterAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    val fragmentList: List<Fragment>
) : FragmentStateAdapter(fragmentManager, lifecycle) {


    var currentFragment: Fragment? = null
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun getCurrentFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}