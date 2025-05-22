package tw.com.chainsea.chat.ui.utils.countrycode

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.CoogameCountryItemBinding
import java.util.Locale

class CountryAdapter(
    private val viewModel: CountryViewModel
) : ListAdapter<CountrySortModel, CountryAdapter.CountryViewHolder>(CountryDiffUtil()),
    SectionIndexer {
    override fun getSections(): Array<Any> = emptyArray()

    override fun getPositionForSection(section: Int): Int {
        if (section != 42) {
            for (i in 0 until itemCount) {
                val sortStr: String = currentList[i].sortLetters
                val firstChar = sortStr.uppercase(Locale.ENGLISH).first()
                if (firstChar.code == section) {
                    return i
                }
            }
        } else {
            return 0
        }
        return -1
    }

    override fun getSectionForPosition(position: Int): Int = currentList[position].sortLetters[0].code

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountryViewHolder {
        val binding =
            DataBindingUtil.inflate<CoogameCountryItemBinding>(
                LayoutInflater.from(parent.context),
                R.layout.coogame_country_item,
                parent,
                false
            )
        return CountryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CountryViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position), viewModel, position)
    }

    inner class CountryViewHolder(
        private val binding: CoogameCountryItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: CountrySortModel,
            viewModel: CountryViewModel,
            position: Int
        ) {
            val section = getSectionForPosition(position)
            if (position == getPositionForSection(section)) {
                binding.countryCatalog.visibility = View.VISIBLE
                binding.countryCatalog.text = item.sortLetters
            } else {
                binding.countryCatalog.visibility = View.GONE
            }

            val countryName: String =
                if (item.countryName.startsWith("#")) {
                    item.countryName.substring(1)
                } else {
                    item.countryName
                }
            binding.countryName.text = countryName
            binding.countryNumber.text = item.countryNumber
            binding.root.setOnClickListener {
                viewModel.onItemClick(countryName, item.countryNumber)
            }
        }
    }
}

class CountryDiffUtil : DiffUtil.ItemCallback<CountrySortModel>() {
    override fun areItemsTheSame(
        oldItem: CountrySortModel,
        newItem: CountrySortModel
    ): Boolean = oldItem.countryNumber == newItem.countryNumber && oldItem.countryName == newItem.countryName

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(
        oldItem: CountrySortModel,
        newItem: CountrySortModel
    ): Boolean = oldItem == newItem
}
