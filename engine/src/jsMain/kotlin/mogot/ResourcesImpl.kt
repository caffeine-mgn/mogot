package mogot

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import pw.binom.ByteDataBuffer
import pw.binom.io.FileSystem
import kotlin.browser.document
import kotlin.coroutines.suspendCoroutine

actual class Resources actual constructor(actual val engine: Engine, actual val fileSystem: FileSystem<Unit>) {
    actual suspend fun createTexture2D(path: String): Texture2D {
        val img = loadImage(path)
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
        canvas.width = img.width
        canvas.height = img.height
        ctx.drawImage(img,0.0,0.0)
        val imgData = ctx.getImageData(0.0,0.0,img.width.toDouble(),img.height.toDouble())
        val data = imgData.data
        val byteDataBuffer = ByteDataBuffer.alloc(data.length)
        for(i in 0 until data.length){
            byteDataBuffer[i] = data[i]
        }
        return Texture2D(engine, SourceImage(SourceImage.Type.RGBA,imgData.width,imgData.height,byteDataBuffer))
    }

    actual fun createEmptyTexture2D(): Texture2D {
        val data = Uint8Array(2 * 2 * 4)
        for (i in 0 until data.length) {
            data[i] = 0
        }
        val byteDataBuffer = ByteDataBuffer.alloc(data.length)
        for(i in 0 until data.length){
            byteDataBuffer[i] = data[i]
        }
        return Texture2D(engine, SourceImage(SourceImage.Type.RGBA,2,2,byteDataBuffer))
    }
}

suspend fun loadImage(path: String): Image {
    val img = Image()
    return suspendCoroutine {
        img.onload = { _ ->
            it.resumeWith(Result.success(img))
        }
        img.onerror = { message, url, line, col, errorObj ->
            it.resumeWith(Result.failure(RuntimeException("Can't load image from path. $message")))
        }
        img.src = path
    }
}