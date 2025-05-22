package tw.com.chainsea.chat.view.todo

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tw.com.chainsea.ce.sdk.bean.todo.TodoEntity
import tw.com.chainsea.ce.sdk.bean.todo.Type
import tw.com.chainsea.ce.sdk.service.TodoService
import tw.com.chainsea.ce.sdk.service.listener.ServiceCallBack
import tw.com.chainsea.ce.sdk.service.type.RefreshSource
import tw.com.chainsea.chat.databinding.PopupWindowTodoSettingBinding
import tw.com.chainsea.chat.view.todo.adapter.TodoSettingAdapter
import tw.com.chainsea.custom.view.banner.CardLinearSnapHelper


class TodoSettingDialog(
    private val context: Context,
    private val roomId: String,
    private val currentId: String,
    private val data: MutableList<TodoEntity>?
) : Dialog(context), TodoSettingAdapter.OnTodoSettingListener {


    private lateinit var binding: PopupWindowTodoSettingBinding
    private val todoListAdapter = TodoSettingAdapter()
    private val helper: CardLinearSnapHelper = CardLinearSnapHelper()
    private var onListener : OnListener? = null
    private var setRemindListener: OnSetRemindTime? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PopupWindowTodoSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initListener()
        window?.setBackgroundDrawable(ColorDrawable(-0x72000000))
    }

    private fun init() = CoroutineScope(Dispatchers.IO).launch {
        binding.rvTodoList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = todoListAdapter
        }
        helper.attachToRecyclerView(binding.rvTodoList)
        setRemindListener?.let { todoListAdapter.setRemindListener(setRemindListener) }
        if (data != null && data.isNotEmpty()) {
            todoListAdapter.setData(data).refresh()
            setCurrentItem(data)
        } else {
            TodoService.getTodoEntities(context, roomId, RefreshSource.LOCAL, object :
                ServiceCallBack<List<TodoEntity>, RefreshSource> {
                override fun complete(t: List<TodoEntity>, e: RefreshSource) {
                    todoListAdapter.setData(t).refresh()
                    setCurrentItem(t)
                }

                override fun error(message: String?) {
                }
            })
        }
    }

    private fun initListener() {
        binding.root.setOnClickListener { dismiss() }
        todoListAdapter.setOnTodoSettingListener(this)
        todoListAdapter.setBackgroundOnClickListener {
            dismiss()
        }
    }

    private fun setCurrentItem(list: List<TodoEntity>) {
        val index = list.indexOf(TodoEntity.Builder().type(Type.MAIN).id(currentId).build())
        if (index > 0) {
            helper.setCurrentItem(index, false)
        }
    }

    fun setOnListener(onListener: OnListener) {
        this.onListener = onListener
    }

    fun setRemindListener(setRemindListener: OnSetRemindTime) {
        this.setRemindListener = setRemindListener
    }

    override fun dismiss() {
        super.dismiss()
        onListener?.onDismiss(this)
    }

    override fun show() {
        super.show()
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.setAttributes(lp)
    }

    override fun onCancel() {
        dismiss()
    }

    override fun navigateToChat(entity: TodoEntity?) {
        onListener?.navigateToChat(entity)
    }

    override fun remove(entity: TodoEntity?) {
        this.data?.remove(entity)
        todoListAdapter.setData(data).refresh()
    }

    interface OnListener {
        fun navigateToChat(entity: TodoEntity?)
        fun onDismiss(todoSettingDialog: TodoSettingDialog)
        fun onShow(todoSettingDialog: TodoSettingDialog)
    }
}