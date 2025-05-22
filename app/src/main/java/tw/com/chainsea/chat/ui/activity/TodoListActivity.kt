package tw.com.chainsea.chat.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.config.BundleKey
import tw.com.chainsea.chat.databinding.ActivityTodoListBinding
import tw.com.chainsea.chat.view.todo.TodoOverviewFragment

class TodoListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodoListBinding
    private val roomId by lazy { intent.getStringExtra(BundleKey.ROOM_ID.key()) ?: "" }
    private lateinit var todoListFragment: TodoOverviewFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodoListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    @SuppressLint("CommitTransaction")
    private fun init() {
        todoListFragment = TodoOverviewFragment.newInstance()
        todoListFragment.setBundle(bundleOf(BundleKey.ROOM_ID.key() to roomId))
        supportFragmentManager.beginTransaction().add(R.id.fl_container, todoListFragment).commit()
        binding.leftAction.setOnClickListener { finish() }
    }
}
