package mogot

import mogot.gl.flip2
import pw.binom.io.InputStream
import pw.binom.io.file.File
import pw.binom.io.file.FileInputStream
import pw.binom.io.file.FileNotFoundException
import pw.binom.io.use
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

actual class Resources actual constructor(private val engine: Engine) {

    private val tasks = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    fun syncCreateTexture2D(stream:InputStream): Texture2D {
        val image = stream.let {
            val png = PNGDecoder(it)
            val color = if (png.hasAlpha())
                PNGDecoder.Format.RGBA
            else
                PNGDecoder.Format.RGB
            val buf = ByteBuffer.allocateDirect(png.width * png.height * color.numComponents)
            png.decode(buf, png.width * color.numComponents, color)
            val colorType = if (png.hasAlpha())
                SourceImage.Type.RGBA
            else
                SourceImage.Type.RGB
            buf.flip2()
            SourceImage(colorType, png.width, png.height, buf)
        }
        return Texture2D(engine, image)
    }

    fun syncCreateTexture2D(path: String): Texture2D {
        val file = File(path)
        if (!file.isFile)
            throw FileNotFoundException(path)
        val image = FileInputStream(file).use {
            val png = PNGDecoder(it)
            val color = if (png.hasAlpha())
                PNGDecoder.Format.RGBA
            else
                PNGDecoder.Format.RGB
            val buf = ByteBuffer.allocateDirect(png.width * png.height * color.numComponents)
            png.decode(buf, png.width * color.numComponents, color)
            val colorType = if (png.hasAlpha())
                SourceImage.Type.RGBA
            else
                SourceImage.Type.RGB
            buf.flip2()
            SourceImage(colorType, png.width, png.height, buf)
        }
        return Texture2D(engine, image)
    }

    actual suspend fun createTexture2D(path: String): Texture2D {
        val file = File(path)
        if (!file.isFile)
            throw FileNotFoundException(path)
        val image = tasks.async {
            FileInputStream(file).use {
                val png = PNGDecoder(it)
                val color = if (png.hasAlpha())
                    PNGDecoder.Format.RGBA
                else
                    PNGDecoder.Format.RGB
                val buf = ByteBuffer.allocateDirect(png.width * png.height * color.numComponents)
                png.decode(buf, png.width * color.numComponents, color)
                val colorType = if (png.hasAlpha())
                    SourceImage.Type.RGBA
                else
                    SourceImage.Type.RGB
                buf.flip2()
                SourceImage(colorType, png.width, png.height, buf)
            }
        }.await()
        engine.waitFrame()
        return Texture2D(engine, image)
    }

    actual fun createEmptyTexture2D(): Texture2D {
        val buf = ByteBuffer.allocateDirect(2 * 2 * 4)
        return Texture2D(engine, SourceImage(SourceImage.Type.RGBA, 2, 2, buf))
    }

}

fun <T> ExecutorService.async(f: suspend () -> T): FeaturePromise<T> {
    val promise = FeaturePromise<T>()
    this.submit {
        f.startCoroutine(object : Continuation<T> {
            override val context: CoroutineContext = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                promise.resume(result)
            }
        })
    }
    return promise
}

