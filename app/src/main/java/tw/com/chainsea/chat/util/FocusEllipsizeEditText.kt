package tw.com.chainsea.chat.util

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextUtils
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import tw.com.chainsea.chat.R

class FocusEllipsizeEditText : TextInputEditText {
    private var originalText = ""

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        post {
            originalText = text.toString()
            ellipsizeText()
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            text = Editable.Factory.getInstance().newEditable(originalText)
        } else {
            originalText = text.toString()
            ellipsizeText()
        }
    }

    private fun ellipsizeText() {
        val paint = paint
        val width = width - paddingStart - paddingEnd
        val ellipsizedText = TextUtils.ellipsize(originalText, paint, width.toFloat(), TextUtils.TruncateAt.END)
        text = Editable.Factory.getInstance().newEditable(ellipsizedText)
    }
}
