package tw.com.chainsea.chat.refactor.welcomePage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.view.welcome.WelcomeGuildFragment

internal class WelcomeGuildAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {
    private val tips = intArrayOf(R.string.welcome_tip_01, R.string.welcome_tip_02, R.string.welcome_tip_03, R.string.welcome_tip_04)
    private val titles = intArrayOf(R.string.welcome_title_01, R.string.welcome_title_02, R.string.welcome_title_03, R.string.welcome_title_04)
    private val banners = intArrayOf(R.drawable.welcome_01, R.drawable.welcome_02, R.drawable.welcome_03, R.drawable.welcome_04)

    override fun createFragment(position: Int): Fragment = WelcomeGuildFragment.newInstance(tips[position], titles[position], banners[position])

    override fun getItemCount(): Int = banners.size
}
