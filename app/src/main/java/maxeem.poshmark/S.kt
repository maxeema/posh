//package com.poshmark.app2
//
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.content.pm.ShortcutInfo
//import android.content.pm.ShortcutManager
//import android.graphics.drawable.Icon
//import android.net.Uri
//import android.os.Build
//import androidx.annotation.DrawableRes
//import androidx.annotation.RequiresApi
//import androidx.annotation.StringRes
//import java.util.*
//
//@RequiresApi(Build.VERSION_CODES.N_MR1)
//class Shortcuts {
//
//    val scheme = "poshmark"
//    val campaign = "utm_source=android&utm_campaign=shortcuts"
//
//    fun init(ctx: Context) {
//        if (!isSupported)
//            return
//        ctx.getSystemService(ShortcutManager::class.java).dynamicShortcuts = Arrays.asList(
////            createShortcut(ctx, "sell", R.string.shortcut_sell, R.drawable.shortcut_sell),
//            createShortcut(ctx, "bundles", R.string.shortcut_bundles, R.drawable.shortcut_bundles)
//        )
//    }
//
//    fun createShortcut(ctx: Context, id: String, @StringRes label: Int, @DrawableRes icon: Int) =
//        ShortcutInfo.Builder(ctx, id)
//            .setShortLabel(ctx.getString(label)).setLongLabel(ctx.getString(label))
//            .setIcon(Icon.createWithResource(ctx, icon))
//            .setIntent(Intent(Intent.ACTION_VIEW, Uri.parse("$scheme://$id?$campaign")))
//            .build()
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    fun createPinnedShortcut(ctx: Context, id: String, @StringRes label: Int, @DrawableRes icon: Int) {
//        val s = createShortcut(ctx, id, label, icon)
//        val m = ctx.getSystemService(ShortcutManager::class.java)
//        val pi = PendingIntent.getBroadcast(ctx, 0, m.createShortcutResultIntent(s),0)
//        m.requestPinShortcut(s, pi.intentSender)
//    }
//
//}
