package maxeem.america

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
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
        l.addView(ChipGroup(this).apply {
            chipSpacingVertical = U.dpToPxi(7)
            D.tabs.forEach { addView(createChip(it)) }
        })
        addSep()
        l.addView(ChipGroup(this).apply {
            chipSpacingVertical = U.dpToPxi(7)
            D.markets.forEach { m-> addView(createChip(m).apply {
                chipMinHeight *= 2; chipIconSize = chipIconSize.times(2)
                if (m.isDepartment) {
                    text = ""; textEndPadding = 0f; textStartPadding = 0f
                } else {
                    text = m.label[0].toString()
                }
            })}
        })
//        addSep()
    }
    private fun createChip(item: D.Item) = Chip(this).apply {
        item.icon.loadDrawableAsync(U.ctx, { h.postDelayed( {
            if (!isFinishing)
                chipIcon = it
            if (BuildConfig.DEBUG)
                Log.e("posh", "${this@AS} loadDrawableAsync " + item.id + "; a.isFinishing: " + isFinishing)
        }, Random.nextInt(500).toLong())}, h)
        chipStartPadding = U.dpToPxf(8)
        text = item.label
        tag = item
        setOnClickListener(::onClick)
        setOnLongClickListener(this@AS::onLongClick)
//android.util.Log.d("posh", "icon: " + File(ctx.filesDir, "men.png").absolutePath + " - " + File(ctx.filesDir, "men.png").canonicalFile)
        //chipIcon = Drawable.createFromPath(File(a.filesDir, "men.png").absolutePath)
    }
    private fun onClick(v: View) {
        (v.tag as D.Item).also { item -> P.go(this, item.action) }
        finish()
    }
    private fun onLongClick(view: View) = (view.tag as D.Item).let { item ->
        //P.pin(entry.key, entry.key, entry.value,
//        IconCompat.createWithBitmap(BitmapFactory.decodeFile("/data/data/maxeem.america.debug/files/men.png")))
//        IconCompat.createWithAdaptiveBitmap(BitmapFactory.decodeFile("/data/data/maxeem.america.debug/files/men.png")))
        if (!S.requestPinned(item.id, item.label, item.icon))
            U.toast(R.string.cant_pin)
        true
    }
    private fun addSep() {
        l.addView(View(this).apply {
            setBackgroundColor(getColor(R.color.mtrl_chip_background_color))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, U.dpToPxi(1)).apply {
            topMargin = U.dpToPxi(10)
            bottomMargin = topMargin
        })
    }

}
