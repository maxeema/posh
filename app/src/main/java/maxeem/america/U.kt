package maxeem.america

import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.Dimension
import androidx.annotation.StringRes

object U { //Utils

    val ctx = App.instance

    private const val TAG = "posh"

    fun dpToPxi(@Dimension(unit = Dimension.DP) value : Int) = kotlin.math.ceil(dpToPxf(value)).toInt()
    fun dpToPxf(@Dimension(unit = Dimension.DP) value : Int) = dpToPxf(value.toFloat())
    fun dpToPxf(@Dimension(unit = Dimension.DP) value : Float) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, ctx.resources.displayMetrics)

    fun toast(@StringRes text: Int) {
        Toast.makeText(ctx, text, Toast.LENGTH_LONG).show()
    }

    fun log(msg: String) {
        Log.d(TAG, msg)
    }

    fun identifier(name:String, type:String) = ctx.resources.getIdentifier(name, type, ctx.packageName)

}