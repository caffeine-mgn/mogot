package mogot.gl

import mogot.Engine
import pw.binom.io.Closeable

class TextureObject(val engine: Engine,multisample:Boolean = false):Closeable {
    enum class FilterParameter{
        Nearest,
        Linear
    }
    val texture:GLTexture = engine.gl.createTexture()
    private val target = if(multisample) engine.gl.TEXTURE_2D_MULTISAMPLE else engine.gl.TEXTURE_2D
    fun bind(){
        engine.gl.enable(target)
        engine.gl.bindTexture(target,texture)
    }
    fun unbind(){

    }
    override fun close() {

    }


}