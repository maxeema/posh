package maxeem.america

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.`as`.*
import kotlin.random.Random

class AS : Activity() { //Shortcuts

    private val h = Handler(App.instance.mainLooper)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.`as`)
        populate()
    }

    private fun populate() {
        tabs.apply {
            D.tabs.forEach { tab-> addView(createChip(tab).apply {
                chipMinHeight = U.dpToPxf(48)
                with (U.dpToPxf(20f.let{ if(tab.action=="sell") it.times(1.35f) else it })) {
                    chipStartPadding = this; chipEndPadding = this
                }
            })}
        }
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
//    private fun addSep() {
//        markets.addView(View(this).apply {
//            setBackgroundColor(getColor(R.color.mtrl_chip_background_color))
//        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, U.dpToPxi(1)).apply {
//            topMargin = U.dpToPxi(10)
//            bottomMargin = topMargin
//        })
//    }

}
