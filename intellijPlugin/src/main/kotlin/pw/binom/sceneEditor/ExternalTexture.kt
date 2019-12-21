package pw.binom.sceneEditor

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.gl.flip2
import pw.binom.io.Closeable
import pw.binom.io.wrap
import java.io.InputStream
import java.nio.ByteBuffer

class ExternalTexture(val engine: Engine, val file: VirtualFile) : ResourceImpl() {

    private fun loadPng(stream: InputStream): Texture2D {
        val image = stream.let {
            val png = PNGDecoder(it.wrap())
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

    private var oldGl: Texture2D? = null
    private var modificationStamp: Long? = null



    val gl: Texture2D
        get() {
            if (oldGl == null || modificationStamp == null || file.modificationStamp > modificationStamp ?: 0L) {
                oldGl?.dec()
                modificationStamp = file.modificationStamp
                oldGl = file.inputStream.use {
                    loadPng(it)
                }
                oldGl!!.inc()
            }
            return oldGl!!
        }

    override fun dispose() {
        oldGl?.dec()
        oldGl = null
        super.dispose()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExternalTexture

        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }

}

class TextureManager(val engine: Engine) : Closeable {
    private val files = HashMap<String, ExternalTexture>()
    fun instance(file: VirtualFile): ExternalTexture {
        val tex = files.getOrPut(file.path) { ExternalTexture(engine, file) }
        tex.disposeListener = {
            files.remove(file.path)
        }
        return tex
    }

    override fun close() {
        files.values.forEach {
            while (!it.dec()) {
            }
        }
    }
}

fun Resources.loadTexture(file: VirtualFile): ExternalTexture {
    val manager = engine.manager("TextureManager") { TextureManager(engine) }
    return manager.instance(file)
}