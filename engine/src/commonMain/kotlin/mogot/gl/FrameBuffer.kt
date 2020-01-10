package mogot.gl

import mogot.Engine

class FrameBuffer(val engine: Engine) {
    private var fbo: GLFrameBuffer? = null
    enum class MSAALevels(val level:Int){
        MSAADisable(0),
        MSAAx2(2),
        MSAAx4(4),
        MSAAx8(8),
        MSAAx16(16)
    }
    fun init(widh:Int,height:Int,msaaLevel:MSAALevels){
        val textureTarget = when(msaaLevel){
            MSAALevels.MSAADisable -> engine.gl.TEXTURE_2D
            else -> engine.gl.TEXTURE_2D_MULTISAMPLE
        }

    }
    fun close(){

    }
    fun use(){

    }
    fun unuse(){

    }
}