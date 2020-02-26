package pw.binom.sceneEditor

import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.gl.flip2
import pw.binom.ByteDataBuffer
import pw.binom.io.Closeable
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.ByteBuffer

abstract class ExternalTexture(val engine: Engine) : ResourceImpl() {
    protected abstract fun isNeedUpdateTexture(): Boolean
    protected abstract fun textureStream(func: (InputStream) -> Unit)

    private fun loadPng(stream: InputStream): Texture2D {
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
            val b = ByteDataBuffer.alloc(buf.remaining())
            for( i in 0 until buf.remaining()){
                b[i] = buf[i]
            }
            SourceImage(colorType, png.width, png.height, b)
        }
        return Texture2D(engine, image)
    }

    private var oldGl: Texture2D? = null
    val gl: Texture2D
        get() {
            if (oldGl == null || isNeedUpdateTexture()) {
                oldGl?.dec()
                textureStream {
                    oldGl = loadPng(it)
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
}

class ExternalTextureInternal(engine: Engine, val path: String) : ExternalTexture(engine) {
    override fun isNeedUpdateTexture(): Boolean = false

    override fun textureStream(func: (InputStream) -> Unit) {
        this::class.java.classLoader.getResourceAsStream(path)?.use {
            func(it)
        } ?: throw FileNotFoundException("Resource $path")
    }

}

class ExternalTextureFS(engine: Engine, val file: VirtualFile) : ExternalTexture(engine) {
    override fun isNeedUpdateTexture(): Boolean = modificationStamp == null || file.modificationStamp > modificationStamp ?: 0L
    override fun textureStream(func: (InputStream) -> Unit) {
        file.inputStream.use {
            func(it)
            modificationStamp = file.modificationStamp
        }
    }

    private var modificationStamp: Long? = null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExternalTextureFS

        if (file != other.file) return false

        return true
    }

    override fun hashCode(): Int {
        return file.hashCode()
    }

}

class TextureManager(val engine: Engine) : Closeable {
    private val files = HashMap<String, ExternalTexture>()
    fun instance(file: VirtualFile): ExternalTextureFS {
        val tex = files.getOrPut(file.path) { ExternalTextureFS(engine, file) }
        tex.disposeListener = {
            files.remove(file.path)
        }
        return tex as ExternalTextureFS
    }

    fun instance(path: String): ExternalTextureInternal {
        val id = "R$path"
        val tex = files.getOrPut(id) { ExternalTextureInternal(engine, path) }
        tex.disposeListener = {
            files.remove(id)
        }
        return tex as ExternalTextureInternal
    }


    override fun close() {
        files.values.forEach {
            while (!it.dec()) {
            }
        }
        files.clear()
    }
}

fun Resources.loadTexture(file: VirtualFile): ExternalTextureFS {
    val manager = engine.manager("TextureManager") { TextureManager(engine) }
    return manager.instance(file)
}

fun Resources.loadTextureResource(path: String): ExternalTextureInternal {
    val manager = engine.manager("TextureManager") { TextureManager(engine) }
    return manager.instance(path)
}