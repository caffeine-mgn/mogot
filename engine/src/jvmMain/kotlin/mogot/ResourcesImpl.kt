package mogot

import mogot.gl.flip2
import pw.binom.ByteDataBuffer
import pw.binom.io.*
import pw.binom.io.file.File
import pw.binom.io.file.FileInputStream
import pw.binom.io.file.FileNotFoundException
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

actual class Resources actual constructor(actual val engine: Engine, actual val fileSystem: FileSystem<Unit>) {

    private val tasks = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    companion object {
        val IMAGE_MAGIC_BYTES = byteArrayOf(10, 65, 75, 32)
    }

    fun syncCreateTexture2D(stream: InputStream): Texture2D {
        val image = stream.let {
            val png = PNGDecoder(TODO())
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
            val byteDataBuffer = ByteDataBuffer.alloc(buf.remaining())
            for(i in 0 until buf.remaining()){
                byteDataBuffer[i] = buf[i]
            }
            SourceImage(colorType, png.width, png.height, byteDataBuffer)
        }
        return Texture2D(engine, image)
    }

    fun syncCreateTexture2D(path: String): Texture2D {
        val file = File(path)
        if (!file.isFile)
            throw FileNotFoundException(path)
        val image = FileInputStream(file).use {
            val png = PNGDecoder(TODO())
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
            val byteDataBuffer = ByteDataBuffer.alloc(buf.remaining())
            for(i in 0 until buf.remaining()){
                byteDataBuffer[i] = buf[i]
            }
            SourceImage(colorType, png.width, png.height, byteDataBuffer)
        }
        return Texture2D(engine, image)
    }

    actual suspend fun createTexture2D(path: String): Texture2D {
        val file = engine.resources.fileSystem.get(Unit, path + ".bin")?.read()
                ?: throw FileSystem.FileNotFoundException(path)
        val source = tasks.async {
            file.use {
                val magic = ByteArray(IMAGE_MAGIC_BYTES.size)
                it.readFully(magic)
                IMAGE_MAGIC_BYTES.forEachIndexed { index, byte ->
                    if (magic[index] != byte)
                        throw IllegalArgumentException("Can't load image from $path. This is not a Image")
                }
                val withAlpcha = it.read() > 0
                val width = it.readInt()
                val height = it.readInt()
                println("width=$width")
                println("height=$height")
                println("withAlpcha=$withAlpcha")
                val size = (if (withAlpcha) 4 else 3) * width * height
                val data = ByteArray(size)
                it.readFully(data)
                val buf = ByteBuffer.wrap(data)
                buf.flip2()
                SourceImage(
                        if (withAlpcha)
                            SourceImage.Type.RGBA
                        else
                            SourceImage.Type.RGB,
                        width,
                        height,
                        ByteDataBuffer.wrap(buf)
                )
            }
        }.await()
        engine.waitFrame()
        return Texture2D(engine, source)

//        val file = File(path)
//        if (!file.isFile)
//            throw FileNotFoundException(path)
//        val image = tasks.async {
//            FileInputStream(file).use {
//                val png = PNGDecoder(it)
//                val color = if (png.hasAlpha())
//                    PNGDecoder.Format.RGBA
//                else
//                    PNGDecoder.Format.RGB
//                val buf = ByteBuffer.allocateDirect(png.width * png.height * color.numComponents)
//                png.decode(buf, png.width * color.numComponents, color)
//                val colorType = if (png.hasAlpha())
//                    SourceImage.Type.RGBA
//                else
//                    SourceImage.Type.RGB
//                buf.flip2()
//                SourceImage(colorType, png.width, png.height, buf)
//            }
//        }.await()
//        engine.waitFrame()
//        return Texture2D(engine, image)
    }

    actual fun createEmptyTexture2D(): Texture2D {
        return Texture2D(engine, SourceImage(SourceImage.Type.RGBA, 2, 2, ByteDataBuffer.alloc(2 * 2 * 4)))
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

