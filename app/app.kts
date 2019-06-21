//
//
//JSONPath docs - https://www.baeldung.com/guide-to-jayway-jsonpath
//Usage - kotlinc -cp app.kts.data/slf4j-api-1.7.25.jar:app.kts.data/json-smart-2.3.jar:app.kts.data/json-path-2.4.0.jar -script app.kts
//
//
import com.jayway.jsonpath.JsonPath
import net.minidev.json.*
import net.minidev.json.parser.JSONParser
import java.io.File
import java.net.URL

private val url = URL("https://poshmark.com/api/meta/experiences")
private val file = File("app.kts.data/experiences.json")

println("- start")
//
class Market(val id: String, val action: String, val label: String, val icon: String) {
    private var depts : MutableList<Market>? = null
    private var isDept = false
    companion object {
        fun from(j: JSONObject) = with (j) {
            val id = getAsString("short_name");
            Market("market_$id", id, getAsString("short_display_name"), getAsString("img_url_large"))
        }
    }
    fun addDept(d: Market) = run {
        d.isDept = true
        if (depts == null) depts = mutableListOf()
        depts!!.add(d)
    }
    override fun toString() = if (isDept) "$id" else "$id - $action - $label - $icon - ${depts}"
}
kotlin.run {
    println("$file -> isFile: ${file.isFile} - length: ${file.length()}")
    val markets = mutableListOf<Market>()
    (file.takeIf { it.length() > 1 } ?: wget(url, file)).inputStream()
        .use { input -> JSONParser(JSONParser.MODE_PERMISSIVE).parse(input) }
    .also { j ->
        val jdmap = mutableMapOf<String, JSONObject>().apply {
            JsonPath.compile("data.[?(@.short_name)]").read<List<JSONObject>>(j)
                .forEach { put(it.getAsString("short_name"), it) }
        }
        JsonPath.compile("$.presentation.groups").read<Collection<JSONObject>>(j).stream().skip(1).forEach { jp ->
            var id = jp.getAsString("id")
            if (id == "home") //Home market has the 'home_a' alias now
                id = "home_a"
            println("market id: " + id)
            markets.add(Market.from(jdmap.get(id)!!).apply {
                JsonPath.compile("content.data.*.id").read<Collection<String>>(jp).forEach { dId: String ->
                    if (id == dId) return@forEach //skip the same market and department like "women" contains "women"
                    if (dId == "wholesale") return@forEach//Wholesale dept. isn't accessible by default for all users and I can't even try/open it
                    println(" dept: " + dId)
                    addDept(Market.from(jdmap.get(dId)!!))
                }
            })
        }
    }
    //process the markets and their departments
    markets.forEach { m ->
        println()
        println("[market] $m")
    }
    //generate markets json

    //and download icons

}
fun wget(url: URL, file: File) = url.openStream().buffered().use { bin -> file.outputStream().use { fout ->
    println(" wget - $url -> $file")
    ByteArray(8096).apply {
        do {
            val r = bin.read(this, 0, this.size)
            if (r > 0) fout.write(this, 0, r)
        } while (r > 0)
    }
    file
}}
//
println("- end")
//
//
