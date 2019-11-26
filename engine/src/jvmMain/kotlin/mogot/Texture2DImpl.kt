package mogot

import com.jogamp.opengl.GL2
import mogot.gl.JGLTexture
import mogot.gl.flip2
import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO


actual class Texture2D(private val engine: Engine, image: SourceImage) : Resource(engine) {
    actual val gl = engine.gl.createTexture()
    val width = image.png.width
    val height = image.png.height
    val id
        get() = (gl as JGLTexture).id

    init {
        val glColor = when (image.png.hasAlpha()) {
            false -> GL2.GL_RGB
            true -> GL2.GL_RGBA
        }

        val color = if (image.png.hasAlpha())
            PNGDecoder.Format.RGBA
        else
            PNGDecoder.Format.RGB

        println("PNG STAT: image.png.hasAlpha=${image.png.hasAlpha()} w=$width h=$height")

        val buf = ByteBuffer.allocateDirect(width * height * color.numComponents)
        image.png.decode(buf, width * color.numComponents, color)
        buf.flip2()
        val mipMapCount = 3

        engine.gl.bindTexture(engine.gl.TEXTURE_2D, gl)
        engine.gl.gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR)
        engine.gl.gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR)
        engine.gl.gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, glColor, width, height, 0, glColor, GL2.GL_UNSIGNED_BYTE, buf)
        engine.gl.gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_LEVEL, mipMapCount - 1)
        engine.gl.gl.glGenerateMipmap(GL2.GL_TEXTURE_2D)
        engine.gl.bindTexture(engine.gl.TEXTURE_2D, null)
    }

    override fun dispose() {
        engine.gl.deleteTexture(gl)
    }
}