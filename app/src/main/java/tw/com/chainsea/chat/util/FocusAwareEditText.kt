package tw.com.chainsea.chat.util

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.TextUtils
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import tw.com.chainsea.chat.R

class FocusAwareEditText : TextInputEditText {
    private var originalText = ""

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        setHintTextColor(if(focused) ContextCompat.getColor(context, R.color.hint_color) else ContextCompat.getColor(context, R.color.black))
        if (focused) {
            text = Editable.Factory.getInstance().newEditable(originalText)
        } else {
            originalText = text.toString()
            val paint = paint
            val width = width - paddingStart - paddingEnd

            val ellipsizedText = TextUtils.ellipsize(originalText, paint, width.toFloat(), TextUtils.TruncateAt.END)
            text = Editable.Factory.getInstance().newEditable(ellipsizedText)
        }
    }
}