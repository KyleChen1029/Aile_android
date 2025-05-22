package tw.com.chainsea.custom.view.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min


class CircularImageView: AppCompatImageView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
        defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: return

        if (width == 0 || height == 0) {
            return
        }
        try {
            val b = (drawable as BitmapDrawable).bitmap

            val bitmap = b.copy(Bitmap.Config.ARGB_8888, true)

            val w = width /*, h = getHeight( )*/

            val roundBitmap = getCroppedBitmap(bitmap, w)
            canvas.drawBitmap(roundBitmap, 0f, 0f, null)
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }

    private fun getCroppedBitmap(bmp: Bitmap, radius: Int): Bitmap {
        val bitmap: Bitmap

        if (bmp.width != radius || bmp.height != radius) {
            val smallest =
                min(bmp.width.toDouble(), bmp.height.toDouble()).toFloat()
            val factor = smallest / radius
            bitmap = Bitmap.createScaledBitmap(
                bmp,
                (bmp.width / factor).toInt(),
                (bmp.height / factor).toInt(), false
            )
        } else {
            bitmap = bmp
        }

        val output = Bitmap.createBitmap(
            radius, radius,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)

        val paint = Paint()
        val rect = Rect(0, 0, radius, radius)

        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.parseColor("#BAB399")
        canvas.drawCircle(
            radius / 2 + 0.7f,
            radius / 2 + 0.7f, radius / 2 + 0.1f, paint
        )
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

}