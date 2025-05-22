package tw.com.chainsea.chat.ui.utils.countrycode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.Locale

class CountryViewModel : ViewModel() {
    var countryChangeUtil: GetCountryNameSort? = null
    val mAllCountryList: MutableList<CountrySortModel> = mutableListOf()
    var pinyinComparator: CountryComparator? = null
    val sendUpdateListView = MutableSharedFlow<MutableList<CountrySortModel>>()
    val sendOnItemClick = MutableSharedFlow<Pair<String, String>>()

    /**
     * 获取国家列表
     */
    fun getCountryList(countryList: Array<String>) =
        viewModelScope.launch(Dispatchers.IO) {
            var i = 0
            val length = countryList.size
            while (i < length) {
                val country =
                    countryList[i]
                        .split("\\*".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()

                val countryName = country[0]
                val countryNumber = country[1]
                val countrySortKey =
                    PinyinHelper.toPinyin(countryName, PinyinStyleEnum.NORMAL, "").uppercase(
                        Locale.getDefault()
                    )
                val countrySortModel = CountrySortModel(countryName, countryNumber, countrySortKey)
                val sortLetter = countryChangeUtil?.getSortLetterBySortKey(countrySortKey) ?: countryChangeUtil?.getSortLetterBySortKey(countryName)

                countrySortModel.sortLetters = sortLetter
                mAllCountryList.add(countrySortModel)
                i++
            }

            Collections.sort(mAllCountryList, pinyinComparator)
            CoroutineScope(Dispatchers.Main).launch {
                sendUpdateListView.emit(mAllCountryList)
            }
        }

    fun onItemClick(
        countryName: String,
        countryNumber: String
    ) = viewModelScope.launch {
        sendOnItemClick.emit(Pair(countryName, countryNumber))
    }
}
