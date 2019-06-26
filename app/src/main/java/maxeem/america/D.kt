package maxeem.america

import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import org.json.JSONObject

object D { //Data

    open class Item (val id: String, val action: String, val label: String, val icon: Icon) {
        constructor(id: String, action: String, label: String, @DrawableRes icon: Int) :
                this(id, action, label, Icon.createWithResource(U.ctx, icon))
        override fun hashCode() = id.hashCode()
        override fun toString() = id
        override fun equals(other: Any?) = this === other || id == other.let { if (it is Item) it.id else null }
    }
    class Market : Item {
        enum class Type(val id: String) {
            M("market"), D("department");
            fun create(id:String, label:String) = Market(this, id, label)
        }
        private constructor(type: Type, id:String, label:String)
                : super("${type.id}_$id", "action/switch_base_market?base_market=$id", label,
                        U.identifier("${type.id}_$id","mipmap")) {
            this.type = type
        }
        val type: Type
        val isDepartment
            get() = type==Type.D
    }

    fun validate(id: String) = actionById(id) != null
    fun actionById(id: String) = when {
        id.startsWith("closet_") -> "closet/" + id.substringAfter("closet_")
        else -> tabs.firstOrNull { id == it.id }?.action ?: markets.firstOrNull { id == it.id }?.action
    }

    val tabs = arrayOf(
        Item("tab_feed", "feed", "Feed", R.drawable.tab_feed),
        Item("tab_shop", "shop", "Shop", R.drawable.tab_shop),
        Item("tab_sell", "sell", "Sell", R.drawable.tab_sell),
        Item("tab_news", "news", "News", R.drawable.tab_news),
        Item("tab_account", "account", "Me", R.drawable.tab_me)
    )

    val markets = mutableListOf<Market>().apply {
        App.instance.resources.openRawResource(R.raw.markets).bufferedReader().use {
            JSONObject(it.readText())
        }.also { j->
            if (BuildConfig.DEBUG)
                U.log("markets j: $j")
            j.getJSONArray("markets").also { markets->
                for (i in 0 until markets.length()) markets.getJSONObject(i).also { m->
                    add(Market.Type.M.create(m.getString("id"), m.getString("label")))
                    m.optJSONArray("departments")?.also { departments->
                        for (i in 0 until departments.length()) departments.getJSONObject(i).also { d->
                            d.keys().forEach { id->
                                add(Market.Type.D.create(id, d.getString(id)))
                            }
                        }
                    }
                }
            }
        }
        if (BuildConfig.DEBUG)
            U.log("markets: " + this)
    }

}