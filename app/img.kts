//
//
import java.awt.image.*
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.*

kotlin.run {
    Img.process(File("/home/max/Downloads/market_men.png"), File("/home/max/Downloads/market_men-new.png"))
//    Img.process(File("/home/max/Downloads/cat1.png"), File("/home/max/Downloads/cat1-new.png"))
//    Img.process(File("/home/max/Downloads/market_home_a.png"), File("/home/max/Downloads/market_home_a-new.png"))
}
private object Img : com.jhlabs.image.PointFilter() {
    //
    private final val ADJUST_PARAMS = Triple(1.0f, 1.0f, 1.2f)
    private final val BALANCE_PARAMS = Triple(1.0f, 1.0f, 1.4f)
    //
    fun process(file: File, dest: File) = ImageIO.write(this.filter(ImageIO.read(file), null), "png", dest)
    //
    override fun filterRGB(x:Int, y:Int, pixel: Int) = Img.balanceColor(pixel, BALANCE_PARAMS).let { it -> Img.adjustImage(it, ADJUST_PARAMS) }
    //
    private fun adjustImage(pixel: Int, args: Triple<Float, Float, Float>): Int {
        val (f1, f2, f3) = args
        val alpha = pixel.ushr(24)
        val red = 0x00ff0000.and(pixel).shr(16).times(f3)
        val green = 0x0000ff00.and(pixel).ushr(8).times(f3)
        val blue = 0x000000ff.and(pixel).times(f3)
        val f4 = run { 0.2125.times(red).plus(0.7154.times(green)).plus(0.0721.times(blue)) }.toFloat()
        val d = f1.toDouble()
        var r = validate(red.minus(f4).times(f2).plus(f4).minus(0.5).times(d).plus(0.5).toInt())
        var g = validate(green.minus(f4).times(f2).plus(f4).minus(0.5).times(d).plus(0.5).toInt())
        var b = value(blue.minus(f4).times(f2).plus(f4).minus(0.5).times(d).plus(0.5).toInt())
        if (r < 0.0f) {
            r = 0.0f
        } else if (r > 255.0f) {
            r = 255.0f
        }
        val f8 = max(0.0f, min(b, 255f))
        return alpha.shl(24).or(r.toInt().shl(16)).or(g.toInt().shl(8)).or(f8.toInt())
    }
    private fun balanceColor(pixel: Int, args: Triple<Float, Float, Float>): Int {
        val (f1, f2, f3) = args
        val a = pixel.ushr(24)
        var r = validate(0x00ff0000.and(pixel).shr(16).plus(1).times(f1).toInt())
        var g = validate(0x0000ff00.and(pixel).shr(8).plus(1).times(f2).toInt())
        var b = validate(0x000000ff.and(pixel).plus(1).times(f3).toInt().toInt())
        return a.shl(24).or(r.shl(16)).or(g.shl(8)).or(b)
    }
    private val validate = { i:Int -> when {i in 0..255 -> i; i<0 -> 0; else -> 255 } }
}
//
//