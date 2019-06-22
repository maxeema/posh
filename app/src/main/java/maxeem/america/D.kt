package maxeem.america

import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import org.json.JSONObject

object D { //Data

    class Item (val id: String, val action: String, val label: String, val icon: Icon) {
        constructor(id: String, action: String, label: String, @DrawableRes icon: Int) :
                this(id, action, label, Icon.createWithResource(U.ctx, icon))
    }

    fun validate(id: String) = actionById(id) != null
    fun actionById(id: String) = items.firstOrNull { id == it.id }?.action

    val items = arrayOf(
//        Item("tab_feed", "feed", "Feed", R.drawable.tab_feed),
        Item("tab_shop", "shop", "Shop", R.drawable.tab_shop),
        Item("tab_sell", "sell", "Sell", R.drawable.tab_sell),
        Item("tab_news", "news", "News", R.drawable.tab_news),
        Item("tab_account", "account", "Me", R.drawable.tab_me)
    )

    val markets = mutableListOf<Item>().apply {
        App.instance.resources.openRawResource(R.raw.markets).bufferedReader().use {
            JSONObject(it.readText())
        }.apply {
            U.log("markets j: " + this)
        }
    }

}