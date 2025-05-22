package tw.com.chainsea.chat.messagekit.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import tw.com.chainsea.ce.sdk.bean.msg.TemplateContent
import tw.com.chainsea.ce.sdk.bean.msg.TemplateElement
import tw.com.chainsea.ce.sdk.bean.msg.TemplateElementAction
import tw.com.chainsea.ce.sdk.bean.msg.content.Action
import tw.com.chainsea.chat.databinding.ItemMsgkitTemplateBinding
import tw.com.chainsea.chat.messagekit.main.viewholder.Constant

class TemplateMultiAdapter(
    private val template: TemplateContent,
    private val callback: (Any) -> Unit,
    private val onLongClick: (Any) -> Unit
) : RecyclerView.Adapter<TemplateMultiAdapter.TemplateMultiViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateMultiViewHolder {
        val binding =
            ItemMsgkitTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TemplateMultiViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return template.elements?.size ?: 1
    }

    override fun onBindViewHolder(holder: TemplateMultiViewHolder, position: Int) {
        template.elements?.let {
            holder.bind(it[position])
        } ?: run {
            holder.bindNormal(template)
        }
    }

    inner class TemplateMultiViewHolder(private val binding: ItemMsgkitTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.lineTop.visibility = View.VISIBLE;
            binding.layoutButtonV.visibility = View.VISIBLE
            binding.layoutButtonH.visibility = View.GONE
            binding.line0H.visibility = View.GONE
            binding.line1H.visibility = View.GONE
            binding.btn0H.visibility = View.GONE
            binding.btn1H.visibility = View.GONE
            binding.btn2H.visibility = View.GONE
        }

        // 單個卡片
        fun bindNormal(templateContent: TemplateContent) {
            setTemplateInfo(templateContent.imageUrl, templateContent.title, templateContent.text)

            setListener(templateContent)


            templateContent.actions?.let { action ->
                if (action.isNotEmpty()) {
                    when (templateContent.orientation) {
                        Constant.Orientation.VERTICAL -> {
                            setTemplateAction(action)
                        }

                        Constant.Orientation.HORIZONTAL -> {
                            when (action.size) {
                                3 -> {
                                    binding.line1H.visibility = View.VISIBLE
                                    binding.btn2H.visibility = View.VISIBLE
                                    binding.btn2H.text = action[2].label
                                    binding.btn2H.setOnClickListener { callback.invoke(action[2]) }
                                }

                                2 -> {
                                    binding.line0H.visibility = View.VISIBLE
                                    binding.btn1H.visibility = View.VISIBLE
                                    binding.btn1H.text = action[1].label
                                    binding.btn1H.setOnClickListener { callback.invoke(action[1]) }
                                }

                                1 -> {
                                    binding.btn0H.visibility = View.VISIBLE
                                    binding.btn0H.text = action[0].label
                                    binding.btn0H.setOnClickListener { callback.invoke(action[0]) }
                                }
                            }
                        }
                    }
                } else {
                    binding.layoutButtonH.visibility = View.GONE;
                    binding.layoutButtonV.visibility = View.GONE;
                    binding.lineTop.visibility = View.GONE;
                }
            }
        }

        // 多卡片
        fun bind(templateElement: TemplateElement) {
            setTemplateInfo(templateElement.imageUrl, templateElement.title)
            setListener(templateElement)
            setTemplateAction(templateElement.actions)
        }

        /**
         * 設置卡片資訊
         * @param url 圖片 url
         * @param title 卡片標題
         * @param content 卡片內容
         * */
        private fun setTemplateInfo(url: String?, title: String?, content: String? = "") {
            url?.let {
                Glide.with(binding.img)
                    .load(it)
                    .apply(RequestOptions().centerCrop())
                    .into(binding.img)
            }
            title?.let {
                binding.txtTitle.text = it
            }
            content?.let {
                binding.txtContent.text = it
            }
        }

        private fun setListener(data: Any) {
            binding.root.setOnLongClickListener {
                onLongClick.invoke(data)
                false
            }

            if (data is TemplateContent) {
                data.defaultAction?.let { action ->
                    binding.img.setOnClickListener { callback.invoke(action) }
                    binding.txtContent.setOnClickListener { callback.invoke(action) }
                }
            }
        }

        /**
         * 設置水平卡片的按鈕動作
         * */
        private fun setTemplateAction(actionList: List<Any>?) {
            actionList?.forEachIndexed { index, data ->
                val text = if (data is TemplateElementAction) data.title else (data as Action).label
                val action = if (data is TemplateElementAction) data else (data as Action)
                when (index) {
                    2 -> {

                        binding.line1V.visibility = View.VISIBLE
                        binding.btn2V.visibility = View.VISIBLE
                        binding.btn2V.text = text
                        binding.btn2V.setOnClickListener {
                            callback.invoke(action)
                        }
                    }
                    1 -> {
                        binding.line0V.visibility = View.VISIBLE
                        binding.btn1V.visibility = View.VISIBLE
                        binding.btn1V.text = text
                        binding.btn1V.setOnClickListener {
                            callback.invoke(action)
                        }
                    }
                    0 -> {
                        binding.btn0V.visibility = View.VISIBLE
                        binding.btn0V.text = text
                        binding.btn0V.setOnClickListener {
                            callback.invoke(action)
                        }
                    }
                }
            }
        }


    }
}