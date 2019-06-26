package maxeem.america

import android.app.Activity
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.AsyncTask
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.FileNotFoundException
import java.net.URL

object Closet {

    val REGEX = Regex("@?[A-Za-z\\d-_]+")

    fun wanna(a: Activity) {
        lateinit var d : AlertDialog
        lateinit var t : EditText
        MaterialAlertDialogBuilder(a)
        .setTitle(R.string.add_closet)
        .setView(LinearLayout(a).also { ll ->
            ll.setPadding(U.dpToPxi(30), U.dpToPxi(10), U.dpToPxi(15), U.dpToPxi(5))
            ll.addView(EditText(a).apply {
                t = this
                setText("jaw_breaker")
                maxLines = 2
                inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                filters = filters.plusElement(InputFilter.LengthFilter(15))
U.log("filter filters")
                addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(p0: Editable?) { }
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
                    override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        d.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = validate(t.text.toString().trim())
                    }
                })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }).setPositiveButton("Find & Add") { d, v->
            findAndAdd(a, correct(t.text.toString().trim()))
        }.create().apply {
            d = this
//            setOnShowListener { d->  }
        }.show()
    }
    //
    private fun validate(name:String) = name.isNotBlank() && name.matches(REGEX)
    private fun correct(name:String) = if (name.startsWith("@")) name.substring(1) else name
    //
    private fun findAndAdd(a: Activity, name: String) {
        lateinit var d : AlertDialog
        MaterialAlertDialogBuilder(a).setView(ProgressBar(a).apply {
            alpha = .6f
        })
        .setCancelable(false)
        .create().also {
            d = it
            d.setOnShowListener { d ->
                PinTask(d,"closet_$name", name, "https://poshmark.com/closet/$name") { html->
                    var url = html.substring(html.indexOf("http", html.indexOf("user-image-con", ignoreCase = true), true)).let {
                        it.substringBefore(">").substringBefore("\"").substringBefore("'").substringBefore(" ")//extract img.src attribute value
                    }
                    if (BuildConfig.DEBUG)
                        U.log("$name closet url -> $url")
                    url
                }.execute()
            }
        }.show()
    }

    private class PinTask(val d: DialogInterface, val id:String, val action:String, val pageUrl:String, val iconExtract: (html:String)->String)
            : AsyncTask<Unit, Unit, Any>() {
        override fun doInBackground(vararg p0: Unit?): Any {
            return runCatching {
                val iconUrl = iconExtract(URL(pageUrl).readText(U.UTF_8))
                if (BuildConfig.DEBUG)
                    U.log(" icon url -> $iconUrl")
                URL(iconUrl).openStream().use { `in`->
                    return@use S.requestPinned(id, action, Icon.createWithAdaptiveBitmap(BitmapFactory.decodeStream(`in`)))
                }
            }
        }
        override fun onPostExecute(result: Result<Boolean>) {
            d.dismiss()
            if (BuildConfig.DEBUG)
                U.log(" onPostExecute $result")
            result.onSuccess { pinned ->
                if (!pinned)
                    U.toast(R.string.cant_pin)
            }.onFailure { e -> when(e) {
                is FileNotFoundException -> TODO()
                is java.net.UnknownHostException -> TODO()
                else -> TODO()
            }}
        }
    }

}