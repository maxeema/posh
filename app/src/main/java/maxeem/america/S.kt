package maxeem.america

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri

object S { //Shortcuts

//    fun isPinnedSupported() = U.ctx.getSystemService(ShortcutManager::class.java).isRequestPinShortcutSupported

//    fun init(ctx: Context) {
//        ctx.getSystemService(ShortcutManager::class.java).dynamicShortcuts = Arrays.asList(
////            createShortcut(ctx, "sell", R.string.shortcut_sell, R.drawable.shortcut_sell),
////            createShortcut(ctx, "bundles", R.string.shortcut_bundles, R.drawable.shortcut_bundles)
//        )
//    }

//    fun requestPinned(id: String, action: String, label: String, @DrawableRes icon: Int) : Boolean {
//        return requestPinned(id, action, label, Icon.createWithResource(ctx, icon))
//    }
    fun requestPinned(id: String, label: String, icon: Icon) =
        U.ctx.getSystemService(ShortcutManager::class.java).run {
            create(id, label, icon).let {
                val pi = PendingIntent.getBroadcast(U.ctx, 0, createShortcutResultIntent(it),0)
                requestPinShortcut(it, pi.intentSender)
            }
        }

    private fun create(id: String, label: String, icon: Icon) =
        ShortcutInfo.Builder(U.ctx, id).run {
            setIcon(icon).setShortLabel(label).setLongLabel(label)
            setActivity(ComponentName(U.ctx, AS::class.java))
            setIntent(Intent(U.ctx, AL::class.java).also {
                it.data = Uri.parse(id)
                it.`package` = U.ctx.packageName
                it.action = Intent.ACTION_VIEW
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
            })
            build()
        }
//    private fun create(ctx: Context, id: String, action: String, label: String, @DrawableRes icon: Int) =
//            create(ctx, id, action, label, Icon.createWithResource(ctx, icon))
//    private fun create(ctx: Context, id: String, action: String, @StringRes label: Int, @DrawableRes icon: Int) =
//            create(ctx, id, action, ctx.getString(label), Icon.createWithResource(ctx, icon))
}
