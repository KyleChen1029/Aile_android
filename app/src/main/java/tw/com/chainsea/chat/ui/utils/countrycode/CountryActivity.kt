package tw.com.chainsea.chat.ui.utils.countrycode

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.ActivityCountryBinding
import tw.com.chainsea.custom.view.progress.IosProgressBar
import java.util.Collections

class CountryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCountryBinding
    private lateinit var adapter: CountryAdapter
    private lateinit var characterParserUtil: CharacterParserUtil
    private lateinit var countryList: Array<String>
    private val viewModel: CountryViewModel by viewModels()
    private lateinit var progressBar: IosProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_country)
        initView()
        initListener()
        showLoadingView()
        viewModel.getCountryList(countryList)
    }

    override fun onResume() {
        super.onResume()
        viewModel.sendUpdateListView
            .onEach {
                adapter.submitList(it)
                dismissLoadingView()
            }.launchIn(this@CountryActivity.lifecycleScope)

        viewModel.sendOnItemClick
            .onEach {
                onItemClick(it)
            }.launchIn(this@CountryActivity.lifecycleScope)
    }

    private fun showLoadingView() {
        progressBar =
            IosProgressBar.show(
                this@CountryActivity,
                "",
                true,
                true
            ) {
            }
    }

    private fun dismissLoadingView() {
        if (progressBar.isShowing) progressBar.dismiss()
    }

    /**
     * 初始化界面
     */
    private fun initView() {
        countryList = resources.getStringArray(R.array.country_code_list)
        binding.countrySidebar.setTextView(binding.countryDialog)
        viewModel.pinyinComparator = CountryComparator()
        viewModel.countryChangeUtil = GetCountryNameSort()
        characterParserUtil = CharacterParserUtil()

        // 将联系人进行排序，按照A~Z的顺序
        Collections.sort(viewModel.mAllCountryList, viewModel.pinyinComparator)

        adapter = CountryAdapter(viewModel)
        binding.countryLvList.adapter = adapter
        binding.ivLeftAction.setOnClickListener { v -> finish() }
    }

    /****
     * 添加监听
     */
    private fun initListener() {
        binding.llCountryCleartext.setOnClickListener { v ->
            binding.countryEtSearch.setText("")
            Collections.sort(viewModel.mAllCountryList, viewModel.pinyinComparator)
            adapter.submitList(viewModel.mAllCountryList)
        }

        binding.countryEtSearch.addTextChangedListener(
            object : TextWatcher {
                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun afterTextChanged(s: Editable) {
                    val searchContent = binding.countryEtSearch.text.toString()
                    if (searchContent == "") {
                        binding.countryIvCleartext.visibility = View.INVISIBLE
                    } else {
                        binding.countryIvCleartext.visibility = View.VISIBLE
                    }

                    if (searchContent.isNotEmpty()) {
                        // 按照输入内容进行匹配
                        val filterList = viewModel.countryChangeUtil?.search(searchContent, viewModel.mAllCountryList) as ArrayList<CountrySortModel>
                        adapter.submitList(filterList)
                    } else {
                        adapter.submitList(viewModel.mAllCountryList)
                    }
                    binding.countryLvList.scrollToPosition(0)
                }
            }
        )

        // 右侧sideBar监听
        binding.countrySidebar.setOnTouchingLetterChangedListener { s ->
            // 该字母首次出现的位置
            val position = adapter.getPositionForSection(s[0].code)
            if (position != -1) {
                binding.countryLvList.post {
                    (binding.countryLvList.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
                }
            }
        }
    }

    fun onItemClick(countryInfo: Pair<String, String>) {
        val intent = Intent()
        intent.putExtra("countryName", countryInfo.first)
        intent.putExtra("countryNumber", countryInfo.second)
        setResult(RESULT_OK, intent)
        Log.d(
            "CountryActivity",
            "countryName: + " + countryInfo.first + "countryNumber: " + countryInfo.second
        )
        finish()
    }
}
