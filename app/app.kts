//
//
//https://www.baeldung.com/guide-to-jayway-jsonpath
//kotlinc -cp app.kts.data/com.jhlabs-filters-2.0.235.jar:app.kts.data/slf4j-api-1.7.25.jar:app.kts.data/json-smart-2.3.jar:app.kts.data/json-path-2.4.0.jar -script app.kts
//
//
import com.jayway.jsonpath.JsonPath
import com.jhlabs.image.*
import net.minidev.json.*
import net.minidev.json.parser.JSONParser
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import javax.imageio.ImageIO

//
//
private object Conts {
    val UTF_8 = Charset.forName("UTF-8")
}
private object Experiences {
    val url = URL("https://poshmark.com/api/meta/experiences")
    val file = File("app.kts.data/experiences.json")
}
private object Markets {
    val order = arrayOf("home_a", "kids", "all", "men", "women")
    const val jsonFile = "src/main/res/raw/markets.json"
    const val iconsPath = "src/main/res/mipmap-xxxhdpi"
}
private object Icons {
    val effect : (id:String)->Pair<Img.Effect,Any?> = l@{ id ->
        for (entry in effectToId.entries) if (entry.value.contains(id)) return@l entry.key to null
        idToEffect.get(id)?.also {
            if (it is Img.Effect) return@l it to null
            return@l it as Pair<Img.Effect, Any>
        }
        Img.Effect.NO to null
    }
    private val effectToId = mapOf(
        Img.Effect.MODERN to setOf("luxur_k","gifts","luxur_m","petit_w","plus","promd_w")
    )
    private val idToEffect = mapOf(
        "mater_w" to Img.Effect.RETRO,
        "bouti_m" to (Img.Effect.Filter to ContrastFilter().apply{ brightness = .9f; contrast=1.2f }),
        "makeup" to (Img.Effect.Filter to ContrastFilter().apply{ brightness = 0.85f; contrast=1.35f }),
        "activ_k" to (Img.Effect.Filter to CompoundFilter(
                HSBAdjustFilter().apply{ sFactor = 0.1f }, ContrastFilter().apply{ brightness = 0.8f; contrast=1.15f }))
    )
//    fun test() {
//        val f = "/home/max/Downloads/bouti_m.png"; ImageIO.write(CompoundFilter(
//            HSBAdjustFilter().apply { sFactor = 0.1f }, ContrastFilter().apply { brightness = 0.9f; contrast = 1.2f }
//        ).filter(ImageIO.read(File(f)), null), "png", File("$f-new"))
//        kotlin.checkNotNull(null) { "interrupt here" }
//    }
}
//
println("- start")
//
fun JSONArray.appendElement(idx:Int, item:JSONObject) = run { add(idx, item) }
//
kotlin.run {
    println("${Experiences.file} -> isFile: ${Experiences.file.isFile} - length: ${Experiences.file.length()}")
    Experiences.file.run {
        takeUnless {length() > 1}?.apply { Utils.wgetAsText(Experiences.url, this) }
        JSONParser(JSONParser.MODE_PERMISSIVE).parse(readText(Conts.UTF_8))
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
            jdmap.getValue(id).also { jdm-> JSONObject().also { jm ->
                jout.get("markets").let{ it as JSONArray }.appendElement(jm.appendField("id", id).appendField("label", jdm.getAsString("short_display_name")))
                Markets.iconsPath.let{File(it, "market_$id.png")}.takeUnless{ it.length() > 1 }?.also { f->
                    Icons.effect(id).also { val (effect, arg) = it; effect.apply(arg, URL(jdm.getAsString("img_url_large")), f) }}
                JsonPath.compile("content.data.*.id").read<Collection<String>>(jp).forEach { dId: String ->
                    if (id == dId) return@forEach //skip the same market and department like "women" contains "women"
                    if (dId == "wholesale") return@forEach//Wholesale dept. isn't accessible by default for all users and I can't even try/open it
                    if (dId == "luxury" && id == "women") return@forEach
                    println(" dept: " + dId)
                    jdmap.getValue(dId).also { jdd ->
                        jm.run { get("departments") as JSONArray? ?: JSONArray().also { put("departments", it)} }
                            .appendElement(JSONObject().appendField(dId, jdd.getAsString("short_display_name")))
                        Markets.iconsPath.let{File(it, "department_$dId.png")}.takeUnless{ it.length() > 1 }?.also { f->
                            Icons.effect(dId).also { val (effect, arg) = it; effect.apply(arg, URL(jdd.getAsString("img_url_large")), f) }}
                    }
                }
            }}
        }
        println("jout: " + jout)
        Markets.jsonFile.let { File(it)}.writer(Conts.UTF_8).use { jout.writeJSONString(it) }
    }
}
//
//
println("- end")
//
object Utils {
    fun wgetAsText(url: URL, to: File) = to.apply {
        print("-- Utils.wgetAsText - $url -> $to ...")
        writeText(url.readText(Conts.UTF_8), Conts.UTF_8)
        println(" completed -> file size - ${to.length()}")
    }
    fun wgetAsBinary(url: URL, to: File) = to.apply {
        print("-- Utils.wgetAsBinary - $url -> $to ...")
        url.openStream().copyTo(outputStream())
        println(" completed -> file size - ${to.length()}")
    }
}
//
//
object Img {
    enum class Effect(private val action: (arg:Any?, src:BufferedImage, dst:BufferedImage?) -> BufferedImage) {
        //com.jhlabs-filters
        Filter({ arg,src,dst-> val o = arg as AbstractBufferedImageOp
//            println(" Img.Effect.Filter: ${arg.javaClass.simpleName}")
            o.filter(src, dst)
        }),
        //Poshmark's effects
        MODERN({ arg,src, dst ->
            with (PoshmarkFilter()) {
                filter(src, dst) { x,y,pxl ->
                    balanceColor(pxl, Triple(1.0f, 1.0f, 1.4f)).let {
                        adjustImage(it, Triple(1.0f, 1.0f, 1.2f)) }
                }
            }
        }),
        CHIC({ arg,src,dst ->
            with (PoshmarkFilter()) {
                filter(src, dst) { x, y, pxl ->
                    balanceColor(pxl, Triple(1.2f, 1.0f, 0.7f)).let {
                        adjustImage(it, Triple(1.2f, 1.0f, 0.7f))
                    }
                }
            }
        }),
        RETRO({ arg,src,dst ->
            with (PoshmarkFilter()) {
                filter(src, dst) { x, y, pxl -> balanceColor(pxl, Triple(1.4f, 1.3f, 1.0f)) }
            }
        }),
        STREET({ arg,src, dst ->
            with (PoshmarkFilter()) {
                filter(src, dst) { x, y, pxl -> adjustImage(pxl, Triple(1.5f, 1.0f, 1.4f)) }
            }
        }),
////        VINTAGE({ arg,src, dst ->
//            //TODO
////        }),
        //no
        NO({ arg,src,dst ->
            throw Exception("Impossible is NOT possible")
        });
        //@param what - URL, File, InputStream
        public fun apply(arg:Any?, what: URL, to: File) {
            if (this == NO) {
                Utils.wgetAsBinary(what, to)
            } else {
                print("-- Img.Effect.apply ${if (this==Filter) arg!!.javaClass.simpleName else this} on $what -> $to ...")
                ImageIO.write(this.action(arg,ImageIO.read(what), null), "png", to).takeUnless { it }?.apply { throw Exception("Image write error $what to $to") }
                println(" completed -> file size - ${to.length()}")
            }
        }
        override fun toString() = name
    }
    //
}
class PoshmarkFilter {
    fun adjustImage(pxl: Int, args: Triple<Float, Float, Float>): Int {
        val (f1, f2, f3) = args
        val r = r(pxl).times(f3); val g = g(pxl).times(f3); val b = b(pxl).times(f3)
        val f4 = 0.2125.times(r).plus(0.7154.times(g)).plus(0.0721.times(b))
        return argb(pxl.ushr(24), r.minus(f4).times(f2).plus(f4).minus(0.5).times(f1).plus(0.5).toInt(),
                g.minus(f4).times(f2).plus(f4).minus(0.5).times(f1).plus(0.5).toInt(),
                    b.minus(f4).times(f2).plus(f4).minus(0.5).times(f1).plus(0.5).toInt())
    }
    fun balanceColor(pxl: Int, args: Triple<Float, Float, Float>) =
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
    fun filter(src: BufferedImage, dest: BufferedImage?, filterRGB:(x:Int, y:Int, pxl:Int)->Int) : BufferedImage {
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