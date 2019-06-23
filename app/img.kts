//
//
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

kotlin.run {
    ImgFilter.process(File("/home/max/Downloads/market_men.png"), File("/home/max/Downloads/market_men-new.png"))
//    ImgFilter.process(java.net.URL("https://d2zlsagv0ouax1.cloudfront.net/assets/poshmarkets/api/market-women@3x-6d69fbd835686076d9fb1169c22ff137.png"), File("/home/max/Downloads/market-women-new.png"))
}
object ImgFilter {
    //
    private final val ADJUST_PARAMS = Triple(1.0f, 1.0f, 1.2f)
    private final val BALANCE_PARAMS = Triple(1.0f, 1.0f, 1.4f)
    //
    fun process(src: File, dest: File) = ImageIO.write(filter(ImageIO.read(src), null), "png", dest)
    fun process(url: java.net.URL, dest: File) = ImageIO.write(filter(ImageIO.read(url), null), "png", dest)
    fun process(input: java.io.InputStream, dest: File) = ImageIO.write(filter(ImageIO.read(input), null), "png", dest)
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