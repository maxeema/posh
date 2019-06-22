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
import java.nio.charset.Charset

object Experiences {
    val url = URL("https://poshmark.com/api/meta/experiences")
    val file = File("app.kts.data/experiences.json")
}
object Markets {
    const val jsonFile = "src/main/res/raw/markets.json"
    const val iconsPath = "src/main/res/mipmap-xxxhdpi"
}

println("- start")
//
kotlin.run {
    println("${Experiences.file} -> isFile: ${Experiences.file.isFile} - length: ${Experiences.file.length()}")
    (Experiences.file.takeIf { it.length() > 1 } ?: wget(Experiences.url, Experiences.file)).inputStream()
        .use { input -> JSONParser(JSONParser.MODE_PERMISSIVE).parse(input) }
    .also { j ->
        val jdmap = mutableMapOf<String, JSONObject>().apply {
            JsonPath.compile("data.*").read<List<JSONObject>>(j)
                .forEach { put(it.getAsString("short_name"), it) }
        }
        val jout = JSONObject().apply {
            appendField("markets", JSONArray())
            appendField("date", JsonPath.compile("experiences.updated_at").read(j))
        }
        JsonPath.compile("presentation.groups").read<Collection<JSONObject>>(j).stream().skip(1).forEach { jp ->
            var id = jp.getAsString("id")
            if (id == "home") //Home market has the 'home_a' alias now
                id = "home_a"
            println("market id: " + id)
            jdmap.get(id)!!.also { jdm-> JSONObject().also { jm ->
                jout.get("markets").let{ it as JSONArray }.appendElement(jm.appendField("id", id).appendField("label", jdm.getAsString("short_display_name")))
                Markets.iconsPath.let{File(it, "market_$id.png")}.takeUnless { it.length() > 1}?.run {
                    wget(URL(jdm.getAsString("img_url_large")), this) }
                JsonPath.compile("content.data.*.id").read<Collection<String>>(jp).forEach { dId: String ->
                    if (id == dId) return@forEach //skip the same market and department like "women" contains "women"
                    if (dId == "wholesale") return@forEach//Wholesale dept. isn't accessible by default for all users and I can't even try/open it
                    println(" dept: " + dId)
                    jdmap.get(dId)!!.also { jdd ->
                        jm.run { get("departments") as JSONArray? ?: JSONArray().also { put("departments", it)} }
                            .appendElement(JSONObject().appendField(dId, jdd.getAsString("short_display_name")))
                        Markets.iconsPath.let{File(it, "department_$dId.png")}.takeUnless { it.length() > 1 }?.run {
                            wget(URL(jdd.getAsString("img_url_large")), this) }
                    }
                }
            }}
        }
        println("jout: " + jout)
        Markets.jsonFile.let{File(it)}.writer(Charset.forName("UTF-8")).use { jout.writeJSONString(it) }
//        println(JsonPath.compile("markets[0].departments").read<JSONArray>(jout).get(0).let {it as JSONObject}.entries.first())
    }

}
fun wget(url: URL, file: File) = url.openStream().buffered().use { bin ->
        file.outputStream().use { fout ->
    println(" wget - $url -> $file")
    ByteArray(8096).apply {
        var r: Int; do {
            r = bin.read(this, 0, this.size)
            if (r > 0) fout.write(this, 0, r)
        } while (r > 0)
    }
    file
}}
//
println("- end")
//
//
