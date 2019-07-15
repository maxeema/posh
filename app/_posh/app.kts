//
//
//https://www.baeldung.com/guide-to-jayway-jsonpath
//
//
import com.jayway.jsonpath.JsonPath
import com.jhlabs.image.*
import net.minidev.json.*
import net.minidev.json.parser.JSONParser
import java.awt.image.*
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import javax.imageio.ImageIO
//
//ImageUtils.testIconEffect("luxur_m", "department")
//error("Exit after applying test icon effect.")
//
object C { //Conf
    val UTF_8 = Charset.forName("UTF-8")
    val EXPERIENCES_URL = "https://poshmark.com/api/meta/experiences"
    val EXPERIENCES_JSON = "experiences.json"
    val MARKETS_ORDER = "home_a,kids,all,men,women".split(",")
    val MARKETS_JSON = "../src/main/res/raw/markets.json"
    val ICONS_SRC = "icons"
    val ICONS_DST = "../src/main/res/mipmap"
    val ICON_ORIG_SIZE = 186
    val ICON_SIZES = mapOf("xxxhdpi" to 192, "xxhdpi" to 144, "xhdpi" to 96, "hdpi" to 72, "mdpi" to 48)
    val ICONS_EFFECTS = mapOf(
        "gifts" to CompoundFilter(ContrastFilter().apply{ brightness = 1.25f; contrast=1.4f }, RGBAdjustFilter(.1f, 0f, .1f)),
        "boutique" to CompoundFilter(ContrastFilter().apply{ contrast=1.2f }, PoshmarkModernFilter),
        "luxury" to ContrastFilter().apply{ brightness = 1.1f; contrast=1.1f },
        "activ_k" to CompoundFilter(ContrastFilter().apply{ brightness = .9f; contrast=1.3f }, RGBAdjustFilter(0f, 0f, .6f)),
        "luxur_k" to CompoundFilter(ContrastFilter().apply{ brightness = .9f; contrast=1.1f }, RGBAdjustFilter(.1f, .1f, .5f)),
        "kids" to CompoundFilter(ContrastFilter().apply{ brightness = .8f; contrast=1.25f }, PoshmarkModernFilter),
        "bouti_m" to ContrastFilter().apply{ brightness = .85f; contrast=1.65f },
        "luxur_m" to CompoundFilter(ContrastFilter().apply{ brightness = .95f; contrast=1.1f }, RGBAdjustFilter(0f, 0f, .5f)),
//"luxur_m" to ResizeCanvasFilter((C.ICON_ORIG_SIZE*1.44f).toInt(), CompoundFilter(ContrastFilter().apply{ brightness = .95f; contrast=1.1f }, RGBAdjustFilter(0f, 0f, .5f))),
        "activ_m" to CompoundFilter(ContrastFilter().apply{ contrast=1.2f }, RGBAdjustFilter(0f, .1f, .5f)),
        "kicks_m" to CompoundFilter(FlipFilter(FlipFilter.FLIP_H), CompoundFilter(ContrastFilter().apply{ brightness = 1f; contrast=1.1f }, RGBAdjustFilter(0f, 0f, .3f))),
        "men" to CompoundFilter(ContrastFilter().apply{ brightness = .9f; contrast=1.25f }, PoshmarkModernFilter),
        "men,women,home_a" to CompoundFilter(ContrastFilter().apply{ brightness = .8f; contrast=1.35f }, PoshmarkModernFilter),
        "activ_w" to CompoundFilter(ContrastFilter().apply{ contrast=1.3f }, RGBAdjustFilter(0f, 0.1f, .7f)),
        "makeup" to CompoundFilter(HSBAdjustFilter().apply{ sFactor=.05f }, CompoundFilter(FlipFilter(FlipFilter.FLIP_H), CompoundFilter(ContrastFilter().apply{ brightness = .95f; contrast=1.35f }, RGBAdjustFilter(0f, 0f, .5f)))),
        "plus" to CompoundFilter(ContrastFilter().apply{ brightness = .95f; contrast=1.2f }, PoshmarkModernFilter),
        "mater_w" to CompoundFilter(HSBAdjustFilter(.05f, 0f, 0f), CompoundFilter(ContrastFilter().apply{ brightness = 1.1f; contrast=1.2f }, RGBAdjustFilter(-.1f,-.1f,.5f))),
        "petit_w" to CompoundFilter(ContrastFilter().apply{ brightness = .9f; contrast=1.35f }, PoshmarkModernFilter),
        "promd_w" to PoshmarkModernFilter
    )
}
//
//
println("- start")
//
fun JSONArray.appendElement(idx:Int, item:JSONObject) = apply { add(idx, item) }
//
kotlin.run {
    println("${C.EXPERIENCES_JSON} -> isFile: ${File(C.EXPERIENCES_JSON).isFile} - length: ${File(C.EXPERIENCES_JSON).length()}")
    File(C.EXPERIENCES_JSON).run {
        takeUnless {length() > 1}?.apply { Utils.wgetAsText(C.EXPERIENCES_URL, this) }
        JSONParser(JSONParser.MODE_PERMISSIVE).parse(readText(C.UTF_8))
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
        }.toSortedMap(Comparator{ id1,id2 -> C.MARKETS_ORDER.indexOf(id1).compareTo(C.MARKETS_ORDER.indexOf(id2)) }).forEach { entry ->
            val (id, jp) = entry
            println("market id: " + id)
            jdmap.getValue(id).also { jdm-> JSONObject().also { jm ->
                jout.get("markets").let{ it as JSONArray }.appendElement(jm.appendField("id", id).appendField("label", jdm.getAsString("short_display_name")))
                ImageUtils.pullIcon(id, "market", jdm.getAsString("img_url_large"))
                JsonPath.compile("content.data.*.id").read<Collection<String>>(jp).forEach { dId: String ->
                    if (id == dId) return@forEach //skip the same market and department like "women" contains "women"
                    if (dId == "wholesale") return@forEach//Wholesale dept. isn't accessible by default for all users and I can't even try/open it
                    if (dId == "luxury" && id == "women") return@forEach
                    println(" dept: " + dId)
                    jdmap.getValue(dId).also { jdd ->
                        jm.run { get("departments") as JSONArray? ?: JSONArray().also { put("departments", it)} }
                            .appendElement(JSONObject().appendField(dId, jdd.getAsString("short_display_name")))
                        ImageUtils.pullIcon(dId, "department", jdd.getAsString("img_url_large"))
                    }
                }
            }}
        }
        println("jout: " + jout)
        C.MARKETS_JSON.let{ File(it) }.writer(C.UTF_8).use { jout.writeJSONString(it) }
    }
}
//
//
println("- end")
//
object Utils {
    fun wgetAsText(url: String, to: File) = to.apply {
        print("-- Utils.wgetAsText - $url -> $to ...")
        writeText(URL(url).readText(C.UTF_8), C.UTF_8)
        println(" completed -> file size - ${length()}")
    }
    fun wgetAsBinary(url: String, to: File) = to.apply {
        print("-- Utils.wgetAsBinary - $url -> $to ...")
        URL(url).openStream().copyTo(outputStream())
        println(" completed -> file size - ${length()}")
    }
}
object ImageUtils {
    val iconEffect : (id:String)->AbstractBufferedImageOp? = l@ { id ->
        for ((items, effect) in C.ICONS_EFFECTS)
            if (items.split(",").contains(id))
                return@l effect
        null
    }
    fun pullIcon(id: String, type: String, url: String) {
        check(type in listOf("market", "department")) { "wrong type - $type"}
        //get
        var src = File(C.ICONS_SRC, "$id-original.png")
        if (src.length() > 1)
            return
        Utils.wgetAsBinary(url, src)
        //check
        var img = ImageIO.read(src).apply {
            println(" - orignal size $width x $height")
            //now Poshmark's icons is C.ICON_ORIG_SIZE==186px which is very close 192px (xxxhdpi), so we use them as xxxhdpi
            require(width in C.ICON_ORIG_SIZE..192 && height in C.ICON_ORIG_SIZE..192) { "now, we have to do something with new icon sizes =)" }
        }
        //effect
        File(C.ICONS_SRC, "$id-filtered.png").apply {
            iconEffect(id)?.also { effect ->
                println(" ... applying ${effect.javaClass.simpleName} on $src")
                src = this
                ImageIO.write(effect.filter(img, createDest(img, if (effect is CustomSized) effect.customSize else null)).apply {
                    img = this
                    println(" - set 'src' to $src and update 'img' ref. to the filtered one")
                }, "png", src).takeUnless { it }?.apply { throw Exception("Image write error $url to $src") }
            } ?: delete()
        }
        //scale
        C.ICON_SIZES.forEach { dpi, size -> File("${C.ICONS_DST}-$dpi", "${type}_$id.png").apply {
            if ("xxxhdpi" == dpi) {
                println(" ... copying $src to $this")
                src.copyTo(this, true)
            } else {
                println(" ... scaling $dpi, $size $this")
                ImageIO.write(BicubicScaleFilter(size, size).filter(img, createDest(img, size to size)), "png", this)
                    .takeUnless { it }?.apply { throw Exception("Image write error $this") }
            }
        }}
    }
    fun createDest(src: BufferedImage, size:Pair<Int, Int>? = null) : BufferedImage {
        val dstCM = ColorModel.getRGBdefault()
        //work with DirectColorModel cuz src's may be IndexedColorModel which causes bad quality filter results
        check(dstCM is DirectColorModel) { "we work with DirectColorModel and don't with $dstCM" }
        val (width, height) = size ?: src.width to src.height
        return BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(width, height),
                dstCM.isAlphaPremultiplied(), null as java.util.Hashtable<*,*>?)
    }
    fun testIconEffect(id:String, type:String) {
        val file = File(C.ICONS_SRC, "$id-original.png")
        check (file.length() > 1) { "empty input file: $file"}
        println("\n - Test icon effect for $id - $type, src: $file")
        ImageIO.write(ImageIO.read(file).run {
            ImageUtils.iconEffect(id)!!.let { it.filter(this, ImageUtils.createDest(this, if (it is CustomSized) it.customSize else null)) }
        }, "png", File("$id-filtered.png").apply {
            println(" - writing to ${this.absolutePath}")
        }).takeUnless { it }?.apply { throw Exception("Image write error") }
    }
}
//
//@use it first like: ResizeCanvasFilter(432, CompoundFilter(ContrastFilter().apply{ brightness = .95f; contrast=1.1f }, RGBAdjustFilter(0f, 0f, .5f)))
class ResizeCanvasFilter constructor(override val customSize: Pair<Int, Int>, val next: AbstractBufferedImageOp? = null) : AbstractBufferedImageOp(), CustomSized {
    constructor(upToSize: Int, next: AbstractBufferedImageOp? = null) : this(upToSize to upToSize, next)
    override fun filter(src: BufferedImage, dst: BufferedImage?): BufferedImage {
        val (width,height) = src.width to src.height
        requireNotNull(dst) { "dst shouldn't be null"}
        val (upToWidth, upToHeight) = customSize
        require(width < upToWidth) { "upToWidth ($upToWidth) must be greater source width ($width)" }
        require(height < upToHeight, fun():String { return "upToHeight ($upToHeight) must be greater source height ($height)"} )
        val g = dst.createGraphics()
        g.drawImage(src, (upToWidth-width)/2, (upToHeight-height)/2, width, height, null as ImageObserver?)
        g.dispose()
        return next?.filter(dst, dst) ?: dst
    }
    override fun toString() = "Resize Canvas"
}
interface CustomSized {
    val customSize : Pair<Int, Int>
}
object PoshmarkModernFilter : PoshmarkFilter(Triple(1.0f, 1.0f, 1.4f), Triple(1.0f, 1.0f, 1.2f))
object PoshmarkChicFilter : PoshmarkFilter(Triple(1.2f, 1.0f, 0.7f), Triple(1.2f, 1.0f, 0.7f))
object PoshmarkStreetFilter : PoshmarkFilter(adjustArgs=Triple(1.5f, 1.0f, 1.4f))
object PoshmarkRetroFilter : PoshmarkFilter(Triple(1.4f, 1.3f, 1.0f))
object PoshmarkVintageFilter: PoshmarkFilter(null, null) {
    init { TODO("Not merged and implemented") }
}
open class PoshmarkFilter(val balanceArgs: Triple<Float,Float,Float>? = null,
             val adjustArgs:Triple<Float,Float,Float>? = null) : AbstractBufferedImageOp() {
    init {
        require(balanceArgs != null || adjustArgs != null) {"must be specified at least one effect"}
    }
    override fun filter(src:BufferedImage, dst:BufferedImage?) = when {
        balanceArgs != null && adjustArgs != null
            -> filter(src, dst) { _, _, pxl -> adjustImage(balanceColor(pxl, balanceArgs), adjustArgs) }
        balanceArgs != null -> filter(src, dst) { _, _, pxl -> balanceColor(pxl, balanceArgs) }
        adjustArgs != null  -> filter(src, dst) { _, _, pxl -> adjustImage(pxl, adjustArgs) }
        else -> throw IllegalStateException()
    }
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
    private fun filter(src: BufferedImage, dest: BufferedImage?, filterRGB:(x:Int, y:Int, pxl:Int)->Int) : BufferedImage {
        val (width,height) = src.width to src.height
        val (type, srcRaster) = src.type to src.raster
        val dst = dest ?: run {
            val dstCM = src.colorModel
            BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(width, height),
                    dstCM.isAlphaPremultiplied(), null as java.util.Hashtable<*,*>?)
        }
        val dstRaster = dst.raster
        val inPixels = IntArray(width)
        for (y in 0 until height) {
            if (type == 2) {
                srcRaster.getDataElements(0, y, width, 1, inPixels)
                for (x in inPixels.indices)
                    inPixels[x] = filterRGB(x, y, inPixels[x])
                dstRaster.setDataElements(0, y, width, 1, inPixels)
            } else {
                src.getRGB(0, y, width, 1, inPixels, 0, width)
                for (x in 0 until width) // same as (x in inPixels.indices)
                    inPixels[x] = filterRGB(x, y, inPixels[x])
                dst.setRGB(0, y, width, 1, inPixels, 0, width)
            }
        }
        return dst;
    }
}
//
//
