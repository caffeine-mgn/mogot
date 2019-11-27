package mogot

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.set
import org.w3c.dom.Image
import kotlin.browser.window
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class Resources actual constructor(private val engine: Engine) {
    actual suspend fun createTexture2D(path: String): Texture2D {
        val img = loadImage(path)
        return Texture2D(engine, img)
    }

    actual fun createEmptyTexture2D(): Texture2D {
        val data = Uint8Array(2 * 2 * 4)
        for (i in 0 until data.length) {
            data[i] = 0
        }
        return Texture2D(engine, data, 2, 2)
    }
}

suspend fun loadImage(path: String): Image {
    val img = Image()
    img.crossOrigin = ""
    img.width = 600
    img.height = 600
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