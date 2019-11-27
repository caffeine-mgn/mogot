package mogot

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.Image

actual class Texture2D private constructor(private val engine: Engine) : Resource(engine) {

    actual val gl = engine.gl.createTexture()

    constructor(engine: Engine, image: Image) : this(engine) {
        prepare()
        val glColor = WebGLRenderingContext.RGBA
        engine.gl.ctx.texImage2D(engine.gl.TEXTURE_2D, 0, glColor, glColor, WebGLRenderingContext.UNSIGNED_BYTE, image)
        final()
    }

    constructor(engine: Engine, image: ArrayBufferView, width:Int, height:Int) : this(engine) {
        prepare()
        val glColor = WebGLRenderingContext.RGBA
        engine.gl.ctx.texImage2D(engine.gl.TEXTURE_2D, 0, glColor, width, height, 0, glColor, WebGLRenderingContext.UNSIGNED_BYTE, image)
        final()
    }


    private fun prepare() {
        engine.gl.bindTexture(engine.gl.TEXTURE_2D, gl)
        engine.gl.ctx.texParameteri(engine.gl.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MIN_FILTER, WebGLRenderingContext.LINEAR_MIPMAP_LINEAR)
        engine.gl.ctx.texParameteri(WebGLRenderingContext.TEXTURE_2D, WebGLRenderingContext.TEXTURE_MAG_FILTER, WebGLRenderingContext.LINEAR)
    }

    private fun final() {
        val mipMapCount = 3
        engine.gl.ctx.texParameteri(WebGLRenderingContext.TEXTURE_2D, engine.gl.TEXTURE_MAX_LEVEL, mipMapCount - 1)
        engine.gl.ctx.generateMipmap(WebGLRenderingContext.TEXTURE_2D)
        engine.gl.bindTexture(engine.gl.TEXTURE_2D, null)
    }

    override fun dispose() {
        engine.gl.deleteTexture(gl)
    }
}

