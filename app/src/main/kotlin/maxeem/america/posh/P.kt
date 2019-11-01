package maxeem.america.posh

import android.content.Context
import android.content.Intent
import android.net.Uri

object P { //Poshmark

    private const val packet    = "com.poshmark.app"
    private const val flags     = 0x1000c000
    private const val scheme    = "poshmark"
    private const val campaign  = "utm_source=maxeem&utm_medium=android&utm_campaign=shortcut"

    private fun deeplink(action: String) = Uri.parse("$scheme://$action"
        .plus(if (action.contains("?")) "&" else "?").plus(campaign))

    fun go(ctx: Context, action: String) {
        if (BuildConfig.DEBUG)
            U.debug("go $action")
        runCatching {
            action(ctx, action)
            "okay"
        }.onFailure {
            U.toast(R.string.install_first)
            runCatching {
                install(ctx)
            }//maybe we're running on emulator without Play Store
        }
    }
    private fun action(ctx: Context, action: String) {
        if (BuildConfig.DEBUG)
            U.debug("action: ${deeplink(action)}")
        ctx.startActivity(Intent(Intent.ACTION_VIEW, deeplink(action)).also {
            it.`package` = packet
            it.flags = flags
        })
    }
    private fun install(ctx: Context) {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=$packet")
            `package` = "com.android.vending"
            ctx.startActivity(this)
        }
    }

}