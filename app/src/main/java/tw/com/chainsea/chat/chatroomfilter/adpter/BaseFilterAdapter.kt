package tw.com.chainsea.chat.chatroomfilter.adpter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.com.chainsea.ce.sdk.bean.msg.MessageType
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.chatroomfilter.BaseFilterModel
import tw.com.chainsea.chat.chatroomfilter.MultipleChoiceCallback
import tw.com.chainsea.chat.chatroomfilter.StickHeaderDecoration
import java.util.concurrent.CopyOnWriteArrayList

abstract class BaseFilterAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    StickHeaderDecoration.StickHeaderInterFace {
    var multipleChoiceCallback: MultipleChoiceCallback? = null
    protected val data: MutableList<BaseFilterModel> = mutableListOf()
    protected var isMultipleChoiceMode = false
    private val multipleChoiceList = CopyOnWriteArrayList<BaseFilterModel>()

    abstract override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder

    abstract override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    )

    override fun isStick(position: Int): Boolean = getItem(position).type == MessageType.SYSTEM

    override fun getItemCount(): Int = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<BaseFilterModel>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun setIsMultipleChoiceMode(isMultipleChoiceMode: Boolean) {
        this.isMultipleChoiceMode = isMultipleChoiceMode
        notifyItemRangeChanged(0, data.size, true)
    }

    fun getMultipleChoiceList(): MutableList<*> = multipleChoiceList

    protected fun getItem(position: Int): BaseFilterModel = data[position]

    /**
     * 檢查是否超過上限
     * @param context Context
     * */
    private suspend fun checkIsOverChoice(context: Context): Boolean =
        withContext(Dispatchers.Main) {
            if (multipleChoiceList.size == 9) {
                Toast
                    .makeText(
                        context,
                        context.getText(R.string.text_filter_selected_max),
                        Toast.LENGTH_SHORT
                    ).show()
                return@withContext true
            }
            return@withContext false
        }

    /**
     * 新增選中的項目到 list
     * @param selectView item 左上角的勾選按鈕
     * @param baseFilterModel 選中的 model
     * */
    @SuppressLint("SetTextI18n")
    protected fun <T : BaseFilterModel> addMultipleChoiceItem(
        selectView: TextView,
        baseFilterModel: BaseFilterModel
    ) = CoroutineScope(Dispatchers.IO).launch {
        if (!selectView.isSelected) {
            if (checkIsOverChoice(selectView.context)) return@launch
            multipleChoiceList.add(baseFilterModel)
            baseFilterModel.selectedNumber = multipleChoiceList.size
            withContext(Dispatchers.Main) {
                selectView.text = multipleChoiceList.size.toString()
            }
        } else {
            withContext(Dispatchers.Main) {
                selectView.text = ""
            }
            multipleChoiceList.removeIf { it == baseFilterModel }
            baseFilterModel.selectedNumber = 0
        }

        selectView.isSelected = selectView.isSelected.not()

        subSelectPosition()
        withContext(Dispatchers.Main) {
            multipleChoiceCallback?.onSelected(multipleChoiceList.size)
        }
    }

    /**
     * 重新計算選中的位置
     * */
    private fun subSelectPosition() =
        CoroutineScope(Dispatchers.IO).launch {
            data.forEachIndexed { index, baseFilterModel ->
                for (i in 0 until multipleChoiceList.size) {
                    if (multipleChoiceList[i] === baseFilterModel) {
                        baseFilterModel.selectedNumber = i + 1
                        withContext(Dispatchers.Main) {
                            notifyItemChanged(index, false)
                        }
                    }
                }
            }
        }

    /**
     * 清除所有選中的項目
     * */
    private fun clearSelected() =
        CoroutineScope(Dispatchers.IO).launch {
            multipleChoiceList.clear()
            data.forEach {
                it.selectedNumber = 0
            }
        }

    @SuppressLint("SetTextI18n")
    protected fun checkIsMultipleChoiceMode(
        tvSelect: TextView,
        baseFilterModel: BaseFilterModel
    ) = CoroutineScope(Dispatchers.Main).launch {
        if (!isMultipleChoiceMode) {
            clearSelected()
            tvSelect.isSelected = false
            tvSelect.text = ""
            tvSelect.visibility = View.GONE
        } else {
            tvSelect.visibility = View.VISIBLE
            if (baseFilterModel.selectedNumber != 0) {
                tvSelect.isSelected = true
                tvSelect.text = baseFilterModel.selectedNumber.toString()
            } else {
                tvSelect.isSelected = false
                tvSelect.text = ""
            }
        }
    }
}
