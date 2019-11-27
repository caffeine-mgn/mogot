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
    val width = image.width
    val height = image.height
    val id
        get() = (gl as JGLTexture).id

    init {
        val glColor = when (image.type) {
            SourceImage.Type.RGB -> GL2.GL_RGB
            SourceImage.Type.RGBA -> GL2.GL_RGBA
        }
        val mipMapCount = 3

        engine.gl.bindTexture(engine.gl.TEXTURE_2D, gl)
        engine.gl.gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR)
        engine.gl.gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR)
        engine.gl.gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, glColor, width, height, 0, glColor, GL2.GL_UNSIGNED_BYTE, image.data)
        engine.gl.gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAX_LEVEL, mipMapCount - 1)
        engine.gl.gl.glGenerateMipmap(GL2.GL_TEXTURE_2D)
        engine.gl.bindTexture(engine.gl.TEXTURE_2D, null)
    }

    override fun dispose() {
        engine.gl.deleteTexture(gl)
    }
}