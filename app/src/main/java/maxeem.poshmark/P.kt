package maxeem.poshmark

import android.content.Context
import android.content.Intent
import android.net.Uri

object P {

    const val packet = "com.poshmark.app"
    const val scheme = "poshmark"
    const val campaign = "utm_source=maxeem&utm_medium=android&utm_campaign=shortcut"

    fun sell(ctx : Context) {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("$scheme://sell?$campaign")).apply {
            `package` = packet
            flags=0x1000c000
        })
    }
    fun install(ctx : Context) {
        ctx.startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=$packet")
            `package` = "com.android.vending"
        })
    }

}