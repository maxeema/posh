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

private object Experiences {
    val url = URL("https://poshmark.com/api/meta/experiences")
    val file = File("app.kts.data/experiences.json")
}
private object Markets {
    const val jsonFile = "src/main/res/raw/markets.json"
    const val iconsPath = "src/main/res/mipmap-xxxhdpi"
}
private val UTF_8 = Charset.forName("UTF-8")

println("- start")
//
kotlin.run {
    println("${Experiences.file} -> isFile: ${Experiences.file.isFile} - length: ${Experiences.file.length()}")
    Experiences.file.run {
        takeUnless {length() > 1}?.apply { wgetAsText(Experiences.url, this) }
        JSONParser(JSONParser.MODE_PERMISSIVE).parse(readText(UTF_8))
    }.also { j ->
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
                    wgetAsBinary(URL(jdm.getAsString("img_url_large")), this) }
                JsonPath.compile("content.data.*.id").read<Collection<String>>(jp).forEach { dId: String ->
                    if (id == dId) return@forEach //skip the same market and department like "women" contains "women"
                    if (dId == "wholesale") return@forEach//Wholesale dept. isn't accessible by default for all users and I can't even try/open it
                    if (dId == "luxury" && id == "women") return@forEach
                    println(" dept: " + dId)
                    jdmap.get(dId)!!.also { jdd ->
                        jm.run { get("departments") as JSONArray? ?: JSONArray().also { put("departments", it)} }
                            .appendElement(JSONObject().appendField(dId, jdd.getAsString("short_display_name")))
                        Markets.iconsPath.let{File(it, "department_$dId.png")}.takeUnless { it.length() > 1 }?.run {
                            wgetAsBinary(URL(jdd.getAsString("img_url_large")), this) }
                    }
                }
            }}
        }
        println("jout: " + jout)
        Markets.jsonFile.let { File(it)}.writer(UTF_8).use { jout.writeJSONString(it) }
//        println(JsonPath.compile("markets[0].departments").read<JSONArray>(jout).get(0).let {it as JSONObject}.entries.first())
    }
}
fun wgetAsText(url: URL, file: File) = file.apply {
    print("-- wgetAsText - $url -> $file ...")
    writeText(url.readText(UTF_8), UTF_8)
    println(" completed -> file size - ${file.length()}")
}
fun wgetAsBinary(url: URL, file: File) = file.apply {
    print("-- wgetAsBinary - $url -> $file ...")
    url.openStream().copyTo(outputStream())
    println(" completed -> file size - ${file.length()}")
}
//
//
println("- end")
//
//
