package mogot.gl

import mogot.Engine
import pw.binom.io.Closeable

class TextureObject(val engine: Engine,val width:Int, val height:Int,val minFilter:FilterParameter = FilterParameter.Linear, magFilter:FilterParameter = FilterParameter.Linear, val multisample: MSAALevels = MSAALevels.MSAADisable):Closeable {
    enum class FilterParameter{
        Nearest,
        Linear
    }
    enum class MSAALevels(val level:Int){
        MSAADisable(0),
        MSAAx2(2),
        MSAAx4(4),
        MSAAx8(8),
        MSAAx16(16)
    }
    val glTexture:GLTexture = engine.gl.createTexture()
    val target = if(multisample == MSAALevels.MSAADisable) engine.gl.TEXTURE_2D_MULTISAMPLE else engine.gl.TEXTURE_2D
    init {
        engine.gl.enable(target)
        bind()
        if(multisample == MSAALevels.MSAADisable) {
            engine.gl.enable(engine.gl.MULTISAMPLE)
            engine.gl.texImage2DMultisample(engine.gl.TEXTURE_2D_MULTISAMPLE, multisample.level, engine.gl.RGB, width, height, false)
        }
        unbind()
    }
    fun bind(){
        engine.gl.bindTexture(target,glTexture)
    }
    fun unbind(){
        engine.gl.bindTexture(target,null)
    }
    override fun close() {
        engine.gl.disable(target)
        if(multisample != MSAALevels.MSAADisable)
            engine.gl.disable(engine.gl.MULTISAMPLE)
    }


}