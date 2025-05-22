package tw.com.chainsea.chat.messagekit.theme.viewholder

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import tw.com.aile.sdk.bean.message.MessageEntity
import tw.com.chainsea.ce.sdk.bean.msg.TemplateContent
import tw.com.chainsea.chat.R
import tw.com.chainsea.chat.databinding.MsgkitTemplateBinding
import tw.com.chainsea.chat.messagekit.main.viewholder.Constant.Orientation
import tw.com.chainsea.chat.messagekit.theme.viewholder.base.ThemeMessageBubbleView

class TemplateThemeMessageView(val view: View): ThemeMessageBubbleView(view) {


    private lateinit var img: ImageView
    private lateinit var txtTitle: TextView
    private lateinit var txtContent: TextView
    private lateinit var layoutButtonV: LinearLayout
    private lateinit var layoutButtonH: LinearLayout
    private lateinit var line0V: View
    private lateinit var line1V: View
    private lateinit var btn0V: TextView
    private lateinit var btn1V: TextView
    private lateinit var btn2V: TextView
    private lateinit var line0H: View
    private lateinit var line1H: View
    private lateinit var btn0H: TextView
    private lateinit var btn1H: TextView
    private lateinit var btn2H: TextView
    private lateinit var lineTop: View

    override fun onClick(v: View?, message: MessageEntity?) {
    }

    override fun onDoubleClick(v: View?, message: MessageEntity?) {
    }

    override fun onLongClick(v: View?, x: Float, y: Float, message: MessageEntity?) {
    }

    override fun showName(): Boolean {
        return !isRightMessage
    }

    override fun getContentResId(): Int {
        return R.layout.msgkit_template
    }

    override fun inflateContentView() {
        img = findView(R.id.img)
        txtTitle = findView(R.id.txt_title)
        txtContent = findView(R.id.txt_content)
        layoutButtonV = findView(R.id.layout_button_v)
        layoutButtonH = findView(R.id.layout_button_h)
        line0V = findView(R.id.line_0_v)
        line1V = findView(R.id.line_1_v)
        btn0V = findView(R.id.btn_0_v)
        btn1V = findView(R.id.btn_1_v)
        btn2V = findView(R.id.btn_2_v)

        line0H = findView(R.id.line_0_h)
        line1H = findView(R.id.line_1_h)
        btn0H = findView(R.id.btn_0_h)
        btn1H = findView(R.id.btn_1_h)
        btn2H = findView(R.id.btn_2_h)

        lineTop = findView(R.id.line_top)
    }

    override fun bindContentView() {
        if (getMsg().content() is TemplateContent) {
            val templateContent = getMsg().content() as TemplateContent
            Glide.with(img).load(templateContent.imageUrl)
                .apply(RequestOptions().centerCrop()).into(img)
            txtTitle.text = templateContent.title
            txtContent.text = templateContent.text
            val actionList = templateContent.actions
            if (actionList != null && actionList.isNotEmpty()) {
                when (templateContent.orientation) {
                    Orientation.VERTICAL -> {
                        layoutButtonV.visibility = View.VISIBLE
                        layoutButtonH.visibility = View.GONE
                        line0V.visibility = View.GONE
                        line1V.visibility = View.GONE
                        btn0V.visibility = View.GONE
                        btn1V.visibility = View.GONE
                        btn2V.visibility = View.GONE
                        when (actionList.size) {
                            3 -> {
                                line1V.visibility = View.VISIBLE
                                btn2V.visibility = View.VISIBLE
                                btn2V.text = actionList[2].label
                            }

                            2 -> {
                                line0V.visibility = View.VISIBLE
                                btn1V.visibility = View.VISIBLE
                                btn1V.text = actionList[1].label
                            }
                            1 -> {
                                btn0V.visibility = View.VISIBLE
                                btn0V.text = actionList[0].label
                            }
                        }
                    }

                    else -> {
                        layoutButtonV.visibility = View.VISIBLE
                        layoutButtonH.visibility = View.GONE
                        line0H.visibility = View.GONE
                        line1H.visibility = View.GONE
                        btn0H.visibility = View.GONE
                        btn1H.visibility = View.GONE
                        btn2H.visibility = View.GONE
                        when (actionList.size) {
                            3 -> {
                                line1H.visibility = View.VISIBLE
                                btn2H.visibility = View.VISIBLE
                                btn2V.text = actionList[2].label
                            }

                            2 -> {
                                line0H.visibility = View.VISIBLE
                                btn1H.visibility = View.VISIBLE
                                btn1H.text = actionList[1].label
                            }
                            1 -> {
                                btn0H.visibility = View.VISIBLE
                                btn0H.text = actionList[0].label
                            }
                        }
                    }
                }
            } else {
                layoutButtonH.visibility = View.GONE
                layoutButtonV.visibility = View.GONE
                lineTop.visibility = View.GONE
            }
        }
    }
}