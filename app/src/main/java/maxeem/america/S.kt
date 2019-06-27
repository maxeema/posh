package maxeem.america

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri

object S { //Shortcuts

    fun requestPinned(id: String, label: String, icon: Icon) = U.ctx.getSystemService(ShortcutManager::class.java).run {
        create(id, label, icon).let {
            val pi = PendingIntent.getBroadcast(U.ctx, 0, createShortcutResultIntent(it),0)
            requestPinShortcut(it, pi.intentSender)
        }
    }

    private fun create(id: String, label: String, icon: Icon) = ShortcutInfo.Builder(U.ctx, id).run {
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

}

