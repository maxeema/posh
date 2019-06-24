//
//
//JSONPath docs - https://www.baeldung.com/guide-to-jayway-jsonpath
//Usage - kotlinc -cp app.kts.data/slf4j-api-1.7.25.jar:app.kts.data/json-smart-2.3.jar:app.kts.data/json-path-2.4.0.jar -script app.kts
//
//
import com.jayway.jsonpath.JsonPath
import net.minidev.json.*
import net.minidev.json.parser.JSONParser
import kotlin.math.max
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.Comparator
import java.util.function.ToIntFunction
//
import java.awt.image.BufferedImage
import java.lang.RuntimeException
import javax.imageio.ImageIO
//
//
private object Experiences {
    val url = URL("https://poshmark.com/api/meta/experiences")
    val file = File("app.kts.data/experiences.json")
}
private object Markets {
    val order = arrayOf("home_a", "kids", "all", "men", "women")
    const val jsonFile = "src/main/res/raw/markets.json"
    const val iconsPath = "src/main/res/mipmap-xxxhdpi"
}
private val UTF_8 = Charset.forName("UTF-8")

println("- start")
//
fun JSONArray.appendElement(idx:Int, item:JSONObject) = run { add(idx, item) }
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
        mutableMapOf<String, JSONObject>().apply {
            JsonPath.compile("presentation.groups").read<Collection<JSONObject>>(j).stream().skip(1).forEach { jp ->
                put(jp.getAsString("id").let {
                    if (it == "home") "home_a" else it
                }, jp)
            }
        }.toSortedMap(Comparator{ id1,id2 -> Markets.order.indexOf(id1).compareTo(Markets.order.indexOf(id2)) }).forEach { entry ->
            val (id, jp) = entry
            println("market id: " + id)
            jdmap.get(id)!!.also { jdm-> JSONObject().also { jm ->
                jout.get("markets").let{ it as JSONArray }.appendElement(jm.appendField("id", id).appendField("label", jdm.getAsString("short_display_name")))
                Markets.iconsPath.let{File(it, "market_$id.png")}.takeUnless{ it.length() > 1 }?.run {
                    ImgFilter.process(URL(jdm.getAsString("img_url_large")), this)}
//                    wgetAsBinary(URL(jdm.getAsString("img_url_large")), this) }
                JsonPath.compile("content.data.*.id").read<Collection<String>>(jp).forEach { dId: String ->
                    if (id == dId) return@forEach //skip the same market and department like "women" contains "women"
                    if (dId == "wholesale") return@forEach//Wholesale dept. isn't accessible by default for all users and I can't even try/open it
                    if (dId == "luxury" && id == "women") return@forEach
                    println(" dept: " + dId)
                    jdmap.get(dId)!!.also { jdd ->
                        jm.run { get("departments") as JSONArray? ?: JSONArray().also { put("departments", it)} }
                            .appendElement(JSONObject().appendField(dId, jdd.getAsString("short_display_name")))
                        Markets.iconsPath.let{File(it, "department_$dId.png")}.takeUnless { it.length() > 1 }?.run {
                            ImgFilter.process(URL(jdd.getAsString("img_url_large")), this) }
//                            wgetAsBinary(URL(jdd.getAsString("img_url_large")), this) }
                    }
                }
            }}
        }
        println("jout: " + jout)
        Markets.jsonFile.let { File(it)}.writer(UTF_8).use { jout.writeJSONString(it) }
//        println(JsonPath.compile("markets[0].departments").read<JSONArray>(jout).get(0).let {it as JSONObject}.entries.first())
    }
}
fun wgetAsText(url: URL, to: File) = to.apply {
    print("-- wgetAsText - $url -> $to ...")
    writeText(url.readText(UTF_8), UTF_8)
    println(" completed -> file size - ${to.length()}")
}
fun wgetAsBinary(url: URL, to: File) = to.apply {
    print("-- wgetAsBinary - $url -> $to ...")
    url.openStream().copyTo(outputStream())
    println(" completed -> file size - ${to.length()}")
}
//
//
println("- end")
//
//@usage ImgFilter.process(java.net.URL("https://d2zlsagv0ouax1.cloudfront.net/assets/poshmarkets/api/market-women@3x-6d69fbd835686076d9fb1169c22ff137.png"), File("/home/max/Downloads/market-women-new.png"))
//
object ImgFilter {
    //
    private final val ADJUST_PARAMS = Triple(1.0f, 1.0f, 1.2f)
    private final val BALANCE_PARAMS = Triple(1.0f, 1.0f, 1.4f)
    //

    fun process(url: URL, to: File) = to.apply {
        print("-- ImgFilter.process - $url -> $to ...")
        ImageIO.write(filter(ImageIO.read(url), null), "png", to).takeUnless { it }?.apply { throw Exception("Image write error to $to of $url")}
        println(" completed -> file size - ${to.length()}")
    }
    private fun processImpl(src: File, dest: File) = ImageIO.write(filter(ImageIO.read(src), null), "png", dest)
    private fun processImpl(url: java.net.URL, dest: File) = ImageIO.write(filter(ImageIO.read(url), null), "png", dest)
    private fun processImpl(input: java.io.InputStream, dest: File) = ImageIO.write(filter(ImageIO.read(input), null), "png", dest)
    //
    private fun filterRGB(x:Int, y:Int, pxl: Int) =
            balanceColor(pxl, BALANCE_PARAMS).let { it -> adjustImage(it, ADJUST_PARAMS) }
    //
    private fun adjustImage(pxl: Int, args: Triple<Float, Float, Float>): Int {
        val (f1, f2, f3) = args
        val r = r(pxl).times(f3); val g = g(pxl).times(f3); val b = b(pxl).times(f3)
        val f4 = 0.2125.times(r).plus(0.7154.times(g)).plus(0.0721.times(b))
        return argb(pxl.ushr(24), r.minus(f4).times(f2).plus(f4).minus(0.5).times(f1).plus(0.5).toInt(),
                g.minus(f4).times(f2).plus(f4).minus(0.5).times(f1).plus(0.5).toInt(),
                b.minus(f4).times(f2).plus(f4).minus(0.5).times(f1).plus(0.5).toInt())
    }
    private fun balanceColor(pxl: Int, args: Triple<Float, Float, Float>) =
            argb(pxl.ushr(24), r(pxl).plus(1).times(args.first).toInt(),
                    g(pxl).plus(1).times(args.second).toInt(),
                    b(pxl).plus(1).times(args.third).toInt())
    private fun r(n: Int) = 0x00ff0000.and(n).shr(16)
    private fun g(n: Int) = 0x0000ff00.and(n).shr(8)
    private fun b(n: Int) = 0x000000ff.and(n)
    private fun argb(a:Int, r:Int, g:Int, b:Int) =
            a.shl(24).or(validate(r).shl(16)).or(validate(g).shl(8)).or(validate(b))
    private val validate = { i:Int -> when {i in 0..255 -> i; i<0 -> 0; else -> 255 } }
    //
    private fun filter(src: BufferedImage, dest: BufferedImage?) : BufferedImage {
        val (width,height) = src.width to src.height
        val (type, srcRaster) = src.type to src.raster
        val dst = dest ?: run {
            val dstCM = src.colorModel
            BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(width, height),
                    dstCM.isAlphaPremultiplied(), null as java.util.Hashtable<*,*>?);
        }
        val dstRaster = dst.raster
        val inPixels = IntArray(width)
        for (y in 0 until height) {
            if (type == 2) {
                srcRaster.getDataElements(0, y, width, 1, inPixels)
                for (x in 0 until width)
                    inPixels[x] = filterRGB(x, y, inPixels[x])
                dstRaster.setDataElements(0, y, width, 1, inPixels)
            } else {
                src.getRGB(0, y, width, 1, inPixels, 0, width)
                for (x in 0 until width)
                    inPixels[x] = filterRGB(x, y, inPixels[x])
                dst.setRGB(0, y, width, 1, inPixels, 0, width)
            }
        }
        return dst;
    }
}
//
//