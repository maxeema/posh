//
//
//https://www.baeldung.com/guide-to-jayway-jsonpath
//cd app.kts.data
// kotlinc -cp json-path-2.4.0.jar:json-smart-2.3.jar:slf4j-api-1.7.25.jar -script ../app.kts
//
//
import com.jayway.jsonpath.JsonPath
import net.minidev.json.*
import net.minidev.json.parser.JSONParser
import java.io.File
import java.net.URL

private val url = URL("https://poshmark.com/api/meta/experiences")
private val file = File("experiences.json")

println("- start")
//
class Market(val id: String, val action: String, val label: String, val icon: String) {
    private var isDept = false
    companion object {
        fun from(j: JSONObject) = with (j) {
            val id = getAsString("short_name");
            return@with Market("market_$id", id, getAsString("short_display_name"), getAsString("img_url_large"))
        }
    }
    override fun toString(): String {
        return if (isDept) "$id" else "$id - $action - $label - $icon - ${depts}"
    }
    private var depts : MutableList<Market>? = null
    fun addDept(dept: Market) {
        dept.isDept = true
        if (depts != null) depts!!.add(dept)
        else depts = mutableListOf(dept)
    }
}
kotlin.run {
    println("$file -> isFile: ${file.isFile} - length: ${file.length()}")
    val j = (file.takeIf { it.length() > 1 } ?: wget(url, file)).run {
        inputStream().use { JSONParser(JSONParser.MODE_PERMISSIVE).parse(it) }
    }
    val markets = mutableListOf<Market>()
    JsonPath.compile("$.presentation.groups").read<JSONArray>(j).stream().skip(1).forEach { (it as JSONObject).also {
        var id = it.getAsString("id")
        if (id == "home") //Home market has 'home_a' alias
            id = "home_a"
        println("id: " +id)
        with (JsonPath.compile("data.[?(@.short_name == '$id')]").read<JSONArray>(j).get(0) as JSONObject) {
            markets.add(Market.from(this).apply {
                JsonPath.compile("content.data.*.id").read<JSONArray>(it).forEach { with (it as String) {
                    if (it == id) return@with //skip the same dept as market like "women" and "women"
                    if (it == "wholesale") return@with //Wholesale isn't accessible for all users by default and I can't even try/open it
                    println(" child: " + it)
                    with(JsonPath.compile("data.[?(@.short_name == '$it')]").read<JSONArray>(j).get(0) as JSONObject) {
                        addDept(Market.from(this))
                    }
                }
            }})
        }
    }}
    println("markets: $markets")
}
fun wget(url: URL, file: File) = url.openStream().buffered().use {
    println(" wget - $url -> $file")
    val bin = it; file.outputStream().use {
        ByteArray(8096).apply {
            do {
                val r = bin.read(this, 0, this.size)
                if (r > 0) it.write(this, 0, r)
            } while (r > 0)
        }
    }
    file
}
//
println("- end")
//
//
