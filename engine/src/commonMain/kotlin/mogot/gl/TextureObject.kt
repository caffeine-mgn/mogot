package mogot.gl

import pw.binom.io.Closeable

class TextureObject(val gl:GL,val width:Int, val height:Int,val minFilter:FilterParameter = FilterParameter.Linear, magFilter:FilterParameter = FilterParameter.Linear, val multisample: MSAALevels = MSAALevels.Disable):Closeable {
    enum class FilterParameter{
        Nearest,
        Linear
    }
    enum class MSAALevels(val level:Int){
        Disable(0),
        MSAAx2(2),
        MSAAx4(4),
        MSAAx8(8),
        MSAAx16(16)
    }
    val glTexture:GLTexture = gl.createTexture()
    val target = if(multisample != MSAALevels.Disable) gl.TEXTURE_2D_MULTISAMPLE else gl.TEXTURE_2D
    init {
        gl.enable(target)
        bind()
        if(multisample != MSAALevels.Disable) {
            gl.texImage2DMultisample(gl.TEXTURE_2D_MULTISAMPLE, multisample.level, gl.RGB, width, height, false)
        }else{
            gl.texImage2D(gl.TEXTURE_2D,0,gl.RGB,width,height,0,gl.RGB,gl.UNSIGNED_BYTE,null)
        }
        unbind()
    }
    fun bind(){
        gl.bindTexture(target,glTexture)
    }
    fun unbind(){
        gl.bindTexture(target,null)
    }
    override fun close() {
        gl.disable(target)

    }

    fun enable(){
        if(multisample != MSAALevels.Disable)
            gl.enable(gl.MULTISAMPLE)
    }

    fun disable(){
        if(multisample != MSAALevels.Disable)
            gl.disable(gl.MULTISAMPLE)
    }


}