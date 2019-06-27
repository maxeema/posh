package maxeem.america

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.os.AsyncTask
import android.text.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.URL

object Closet {
    //
    private const val URL = "https://poshmark.com/closet"
    private const val DEF_CLOSET = "@jaw_breaker"
    private const val REGEX = "@?[A-Za-z\\d-_]+"
    private const val MAX_LENGTH = 30
    //
    fun wanna(a: Activity) {
        lateinit var nd : AlertDialog
        lateinit var ed : EditText
        MaterialAlertDialogBuilder(a)
        .setTitle(R.string.closet)
        .setView(LinearLayout(a).also { ll ->
            ll.setPadding(U.dpToPxi(30), U.dpToPxi(10), U.dpToPxi(15), U.dpToPxi(5))
            ll.addView(EditText(a).apply {
                ed = this
                setText(DEF_CLOSET)
                maxLines = 1
                inputType = InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                filters = filters.plusElement(InputFilter.LengthFilter(MAX_LENGTH))
                addTextChangedListener(object: TextWatcher {
                    override fun afterTextChanged(p0: Editable?) { }
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
                    override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        nd.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = validate(ed.text.toString().trim())
                    }
                })
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }).setPositiveButton(R.string.add) { d, v->
            add(a, nd, correct(ed.text.toString().trim()))
        }.create().apply {
            nd = this
            setOnShowListener { d->
                ed.requestFocus()
                Selection.setSelection(ed.text, ed.length())
            }
        }.show()
    }
    //
    private val regex = Regex(REGEX)
    private fun validate(name:String) = name.isNotBlank() && name.matches(regex)
    private fun correct(name:String) = if (name.startsWith("@")) name.substringAfter("@") else name
    //
    private fun add(a: Activity, nd: AlertDialog, name: String) {
        MaterialAlertDialogBuilder(a).setView(ProgressBar(a).apply {
            alpha = .6f
        })
        .setCancelable(false)
        .setBackground(ColorDrawable(Color.parseColor("#99222222")))
        .create().also { d ->
            d.setOnShowListener { _ ->
                PinTask(a, nd, d,"closet_$name", "@$name", "$URL/$name") { html->
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

    private class PinTask(val a: Activity, val nd: AlertDialog, val pd: AlertDialog,
                  val id:String, val label:String, val pageUrl:String, val iconExtract: (html:String)->String)
            : AsyncTask<Unit, Unit, Any>() {
        override fun doInBackground(vararg p0: Unit?): Any {
            return runCatching {
                Thread.sleep(500)
                val iconUrl = iconExtract(URL(pageUrl).readText(U.UTF_8))
                if (BuildConfig.DEBUG)
                    U.log(" icon url -> $iconUrl")
                URL(iconUrl).openStream().use { `in`->
                    S.requestPinned(id, label, Icon.createWithAdaptiveBitmap(BitmapFactory.decodeStream(`in`)))
                }
            }
        }
        override fun onCancelled(result: Any?) {
            if (BuildConfig.DEBUG)
                U.log(" onCancelled $result")
            pd.dismiss()
        }
        override fun onPostExecute(result: Any) {
            if (BuildConfig.DEBUG)
                U.log(" onPostExecute: isCancelled $isCancelled; status $status; pd.isShowing ${pd.isShowing}" +
                    "\n  result -> $result")
            if (isCancelled || !pd.isShowing) return
            pd.dismiss()
            let { result as Result<Boolean> }.onSuccess { pinned ->
                if (!pinned)
                    U.toast(R.string.cant_pin)
            }.onFailure { e ->
                when (e) {
                    is java.io.FileNotFoundException -> R.string.not_found
                    is java.net.UnknownHostException -> R.string.cant_connect
                    else -> R.string.smth_wrong
                }.also { msg ->
                    MaterialAlertDialogBuilder(a)
                    .setMessage(Html.fromHtml(U.ctx.getString(msg).let {
                        if (msg == R.string.smth_wrong) it.plus("<br/><small>${e.javaClass.name}</small>") else it
                    }, Html.FROM_HTML_MODE_COMPACT))
                    .setPositiveButton(R.string.got_it) {d, btn -> d.dismiss()}.create().apply {
                        setOnDismissListener { nd.show() }
                    }.show()
                }
            }
        }
    }
//
}