package maxeem.america.posh

import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.annotation.Dimension
import androidx.annotation.StringRes

object U { //Utils

    fun dp(@Dimension(unit = Dimension.DP) value : Int) = kotlin.math.ceil(dpf(value)).toInt()
    fun dpf(@Dimension(unit = Dimension.DP) value : Int) = dpf(value.toFloat())
    fun dpf(@Dimension(unit = Dimension.DP) value : Float) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, app.resources.displayMetrics)

    fun toast(@StringRes text: Int) = Toast.makeText(app, text, Toast.LENGTH_LONG).show()

    fun identifier(name:String, type:String) = app.resources.getIdentifier(name, type, app.packageName)

    fun debug(msg: String)= Log.d(App.TAG, msg)

    fun dump(any: Any) = any.also {
        println("- dump fields of $it")
        for (field in it.javaClass.declaredFields) { with(field) {
            println( "$name = ${get(it)}")
        }}
    }

}
