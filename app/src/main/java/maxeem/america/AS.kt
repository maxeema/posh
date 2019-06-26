package maxeem.america

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.`as`.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import kotlin.random.Random

class AS : Activity() { //Shortcuts

    private val h = Handler(App.instance.mainLooper)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.`as`)
        populateMarkets()
        populateTabs()
    }

    private fun populateTabs() {
        tabs.apply {
            D.tabs.forEach { tab-> addView(createChip(tab).apply {
                chipMinHeight = U.dpToPxf(48)
                with (U.dpToPxf(20f.let{ if(tab.action=="sell") it.times(1.35f) else it })) {
                    chipStartPadding = this; chipEndPadding = this
                }
            })}
        }
    }
    private fun populateMarkets() {
        lateinit var chipGroup : ChipGroup
        D.markets.forEach { m -> createChip(m).apply {
            chipMinHeight = U.dpToPxf(56)
            if (!m.isDepartment) {
                markets.addView(HorizontalScrollView(this@AS).also { s-> s.addView(ChipGroup(this@AS).also {g->
                    with(s) {
                        isHorizontalFadingEdgeEnabled = true
                        setFadingEdgeLength(U.dpToPxi(60))
                        scrollBarStyle = View.SCROLLBARS_OUTSIDE_INSET
                    }
                    chipGroup = g
                    g.setSingleLine(true)
                })}, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                    it.bottomMargin = U.dpToPxi(10)
                })
            }
            chipStartPadding = U.dpToPxf(7)
            chipGroup.addView(this)
        }}
        markets.addView(View(this).apply {
            setBackgroundColor(getColor(R.color.mtrl_chip_background_color))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, U.dpToPxi(1)).apply {
            topMargin = U.dpToPxi(5)
            bottomMargin = topMargin
        })
        markets.addView(TextView(this).apply {
            text = getString(R.string.clickOrTap)
            alpha = .75f
            gravity = Gravity.CENTER_HORIZONTAL
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = U.dpToPxi(10)
        })
    }
    private fun createChip(item: D.Item) = Chip(this).apply {
        item.icon.loadDrawableAsync(U.ctx, { d-> h.postDelayed( {
            if (!isFinishing)
                chipIcon = d
            chipIconSize = kotlin.math.max(d.intrinsicHeight, d.intrinsicWidth).toFloat()
            if (BuildConfig.DEBUG)
                Log.e("posh", "${U.ctx.resources.displayMetrics.density} ${this@AS} (${d.intrinsicWidth}x${d.intrinsicHeight})" +
                        " loadDrawableAsync " + item.id + "; a.isFinishing: " + isFinishing )
        }, Random.nextInt(1500).toLong())}, h)
        with(0f) {
            textStartPadding = this; textEndPadding = this;
        }
        tag = item
        setOnClickListener(::onClick)
        setOnLongClickListener(this@AS::onLongClick)
//android.util.Log.d("posh", "icon: " + File(ctx.filesDir, "men.png").absolutePath + " - " + File(ctx.filesDir, "men.png").canonicalFile)
        //chipIcon = Drawable.createFromPath(File(a.filesDir, "men.png").absolutePath)
    }
    private fun onClick(v: View) = (v.tag as D.Item).let { item->
        P.go(this, item.action)
        Unit //finish()
    }
    private fun onLongClick(view: View) = (view.tag as D.Item).let { item ->
        //P.pin(entry.key, entry.key, entry.value,
//        IconCompat.createWithBitmap(BitmapFactory.decodeFile("/data/data/maxeem.america.debug/files/men.png")))
//        IconCompat.createWithAdaptiveBitmap(BitmapFactory.decodeFile("/data/data/maxeem.america.debug/files/men.png")))
        if (!S.requestPinned(item.id, item.label, item.icon))
            U.toast(R.string.cant_pin)
        true
    }

    fun addCloset(view: View) {
        lateinit var d : AlertDialog
        lateinit var t : TextView
        MaterialAlertDialogBuilder(this).setView(EditText(this).also {
            t = it
            t.text = "jaw_breaker"
            t.addTextChangedListener(object: TextWatcher {
                override fun afterTextChanged(p0: Editable?) { }
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    d.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !text.isNullOrBlank()
                }
            })
        }).setPositiveButton("Find & Add") { d, v->
            performCloset(t.text.toString())
        }.create().also {
            d = it
        }.show()
    }
    private fun performCloset(name: String) {
        lateinit var d : AlertDialog
        MaterialAlertDialogBuilder(this).setView(ProgressBar(this).apply {
            alpha = .6f
        })
        .setCancelable(false)
        .create().also {
            d = it
            d.setOnShowListener { d ->
                object: AsyncTask<String, Unit, Boolean> () {
                    override fun doInBackground(vararg p0: String?): Boolean {
                        Thread.sleep(1000)
                        URL("https://poshmark.com/closet/$name").readText(U.UTF_8).also {
                            var s = it.substring(it.indexOf("http", it.indexOf("user-image-con", ignoreCase = true), true))
                            val url = s.substring(0, s.indexOf(".jpg", ignoreCase = true)+".jpg".length)
                            U.log("$name closet url -> $url")
                            URL(url).openStream().use {
                                S.requestPinned("closet_$name", name, Icon.createWithAdaptiveBitmap(BitmapFactory.decodeStream(it)))
                            }
                        }
                        return true
                    }
                    override fun onPostExecute(result: Boolean?) {
                        d?.dismiss()
                    }
                }.execute(name)
            }
        }.show()
    }

}
