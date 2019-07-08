package maxeem.america

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.`as`.*
import org.jetbrains.anko.applyRecursively
import kotlin.math.max
import kotlin.random.Random

class AS : Activity() { //Shortcuts

    companion object {
        const val MULTI_WINDOW_VIEW_SCALE_FACTOR = 0.85f
    }

    private val mainHandler = Handler(app.mainLooper)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        populate()
    }
    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration?) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        markets.applyRecursively(::style)
        style(footer)
    }
    private fun populate() {
        setContentView(R.layout.`as`)
        populateMarkets()
        populateDescription()
        populateTabs()
    }
    private fun populateMarkets() {
        lateinit var chipGroup : ChipGroup
        D.markets.forEach { m -> createChip(m).apply {
            chipStartPadding = U.dpf(7)
            m.icon.loadDrawableAsync(app, { d-> mainHandler.postDelayed( {
                if (isFinishing || isDestroyed)
                    return@postDelayed
                chipIcon = d
                styleMarket(this)
            }, max(100, Random.nextInt(1500)).toLong())}, mainHandler)
            if (!m.isDepartment) {
                markets.addView(HorizontalScrollView(this@AS).also { s-> s.addView(ChipGroup(this@AS).also { g->
                    with (s) {
                        isHorizontalFadingEdgeEnabled = true
                        setFadingEdgeLength(U.dp(60))
                        scrollBarStyle = View.SCROLLBARS_OUTSIDE_INSET
                    }
                    chipGroup = g
                    g.setSingleLine(true)
                })}, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).also {
                    if (markets.childCount > 0) it.topMargin = U.dp(10)
                })
            }
            styleMarket(this)
            chipGroup.addView(this)
        }}
    }
    private fun populateDescription() = with (LinearLayout(this)) {
        orientation = LinearLayout.VERTICAL
        tag = "description"
        addView(View(this@AS).apply {
            setBackgroundColor(getColor(R.color.mtrl_chip_background_color))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, U.dp(1)).apply {
            bottomMargin = U.dp(5)//U.dp(5)
        })
        addView(TextView(this@AS).apply {
            text = getString(R.string.tap_or_touch)
            alpha = .7f
            gravity = Gravity.CENTER_HORIZONTAL
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        markets.addView(this, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = U.dp(10)
            bottomMargin = topMargin
        })
        style(this)
    }
    private fun createChip(item: D.Item) = Chip(this).apply {
        textStartPadding = 0f; textEndPadding = 0f
        tag = item
        setOnClickListener(::onClick)
        setOnLongClickListener(this@AS::onLongClick)
    }
    private fun populateTabs() {
        tabs.apply { D.tabs.forEach { tab -> addView(createChip(tab).apply {
            chipIcon = tab.icon.loadDrawable(this@AS)
            chipIconSize = chipIcon!!.intrinsicWidth.toFloat()
            chipMinHeight = U.dpf(44)
            with(U.dpf(20f.let { if (tab.action == "sell") it.times(1.35f) else it })) {
                chipStartPadding = this; chipEndPadding = this
            }
        })}}
        style(footer)
    }

    private fun style(v: View) { when {
        v.tag is D.Market -> styleMarket(v as Chip)
        v.tag == "description" || v === footer -> v.visibility = if (isInMultiWindowMode) View.GONE else View.VISIBLE
    }}
    private fun styleMarket(chip: Chip) = with (chip) {
        chipIcon?.also { chipIconSize = it.intrinsicWidth.times(if (isInMultiWindowMode) MULTI_WINDOW_VIEW_SCALE_FACTOR else 1f) }
        chipMinHeight = U.dpf(56.times(if (isInMultiWindowMode) MULTI_WINDOW_VIEW_SCALE_FACTOR else 1f))
    }

    private fun onClick(v: View) = (v.tag as D.Item).let { item->
        if (!S.requestPinned(item.id, item.label, item.icon))
            U.toast(R.string.cant_pin)
        Unit
    }
    private fun onLongClick(view: View) = (view.tag as D.Item).let { item ->
        P.go(this, item.action)
        true
    }

    fun addCloset(view: View) {
        Closet.wanna(this)
    }

}
