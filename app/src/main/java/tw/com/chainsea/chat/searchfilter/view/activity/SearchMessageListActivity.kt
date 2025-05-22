package tw.com.chainsea.chat.searchfilter.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivitySearchMessageListBinding
import tw.com.chainsea.chat.searchfilter.viewmodel.SearchMessageListViewModel
import tw.com.chainsea.chat.ui.activity.ChatNormalActivity
import tw.com.chainsea.chat.ui.adapter.SearchMessageListAdapter
import tw.com.chainsea.chat.util.IntentUtil
import tw.com.chainsea.chat.util.ThemeHelper
import tw.com.chainsea.chat.util.serializable
import tw.com.chainsea.chat.view.BaseActivity

class SearchMessageListActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchMessageListBinding
    private val searchMessageListViewModel by viewModels<SearchMessageListViewModel>()
    private val searchMessageListAdapter: SearchMessageListAdapter by lazy {
        SearchMessageListAdapter(searchMessageListViewModel, ThemeHelper.isGreenTheme())
    }
    private var searchKeyWord: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivitySearchMessageListBinding>(this, R.layout.activity_search_message_list).apply {
            binding = this

            intent?.let {
                val messages: Array<MessageEntity>? = it.serializable(BundleKey.EXTRA_MESSAGE.key())

                searchKeyWord = it.getStringExtra(BundleKey.SEARCH_KEY.key())
                messages?.let { list ->
                    sectionedTitle.text = getString(R.string.text_sectioned_search_news, list.size)
                    searchKeyWord?.let { keyWord ->
                        val listDetail = list.toMutableList()
                        listDetail.sortedBy { item -> item.sendTime }
                        searchMessageListViewModel.checkMemberIsForbidden(listDetail, keyWord)
                    }
                }
            }
            this@SearchMessageListActivity.searchMessageListViewModel.apply {
                sendNavigateToChatRoom
                    .onEach {
                        val intent =
                            Intent(this@SearchMessageListActivity, ChatNormalActivity::class.java)
                                .putExtra(BundleKey.EXTRA_SESSION_ID.key(), it.roomId)
                                .putExtra(BundleKey.EXTRA_MESSAGE.key(), it)
                                .putExtra(BundleKey.SEARCH_KEY.key(), searchKeyWord)
                        IntentUtil.start(this@SearchMessageListActivity, intent)
                    }.launchIn(this@SearchMessageListActivity.lifecycleScope)

                sendUpdatedListResult
                    .onEach {
                        searchMessageListAdapter.setData(it.second, it.first)
                        rvMessageList.adapter = searchMessageListAdapter
                    }.launchIn(this@SearchMessageListActivity.lifecycleScope)
            }
            toolbar.leftAction.setOnClickListener {
                back()
            }
            scopeSectioned.setOnClickListener {
                rvMessageList.visibility = if (rvMessageList.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                icExtend.setImageResource(if (rvMessageList.visibility == View.VISIBLE) R.drawable.ic_arrow_down else R.drawable.ic_arrow_top)
            }
            lifecycleOwner = this@SearchMessageListActivity
        }
    }

    fun back() {
        finish()
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out)
    }
}
