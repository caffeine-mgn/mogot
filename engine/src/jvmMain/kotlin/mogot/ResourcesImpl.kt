package mogot

import pw.binom.io.file.File
import pw.binom.io.file.FileInputStream
import pw.binom.io.file.FileNotFoundException
import pw.binom.io.use

actual class Resources actual constructor(private val engine: Engine) {

    actual fun createTexture2D(path: String): Texture2D {
        val file = File(path)
        if (!file.isFile)
            throw FileNotFoundException()
        return FileInputStream(file).use {
            Texture2D(engine, SourceImage(engine, PNGDecoder(it)))
        }
    }

}