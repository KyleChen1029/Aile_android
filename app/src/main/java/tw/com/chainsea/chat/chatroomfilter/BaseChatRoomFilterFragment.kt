package tw.com.chainsea.chat.chatroomfilter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.FragmentBaseFilterBinding

abstract class BaseChatRoomFilterFragment : Fragment() {

    abstract fun setIsMultipleChoiceMode(isMultipleChoiceMode: Boolean)
    abstract fun getMultipleChoiceList(): MutableList<*>
    abstract fun isHasData(): Boolean

    protected var binding: FragmentBaseFilterBinding? = null
    protected var sort: String = "DESC"

    protected val stickHeaderDecoration by lazy {
        StickHeaderDecoration(binding?.rvList!!)
    }

    private val viewModel by activityViewModels<ChatRoomFilterViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBaseFilterBinding.inflate(LayoutInflater.from(requireContext()))
        viewModel.queryChatRoomMessage()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (this) {
            is FilterMediaFragment -> {
                binding?.ivNoData?.setImageResource(R.drawable.icon_filter_media_no_data)
            }

            is FilterLinkFragment -> {
                binding?.ivNoData?.setImageResource(R.drawable.icon_filter_link_no_data)
            }

            is FilterFileFragment -> {
                binding?.ivNoData?.setImageResource(R.drawable.icon_filter_file_no_data)
            }

        }
    }

    protected fun checkDataListIsEmpty(list: List<BaseFilterModel>) = CoroutineScope(Dispatchers.Main).launch {
        if (list.isEmpty()) {
            binding?.ivNoData?.visibility = View.VISIBLE
        } else {
            binding?.ivNoData?.visibility = View.GONE
        }
        binding?.rvList?.post {
            binding?.rvList?.scrollToPosition(0)
        }
    }
}