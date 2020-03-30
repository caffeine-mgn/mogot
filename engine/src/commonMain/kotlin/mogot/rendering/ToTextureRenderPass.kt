package mogot.rendering

import mogot.gl.GL
import mogot.gl.RenderTargetTexture
import mogot.gl.TextureObject

abstract class ToTextureRenderPass(nextPass:RenderPass?) : BaseRenderPass(nextPass) {
    protected var width:Int = 0
    protected var height:Int = 0
    protected var renderTargetTexture: RenderTargetTexture? = null

    fun resize(inputRenderPassData: RenderPassData, gl: GL){
        if((renderTargetTexture?.width!=width)||(renderTargetTexture?.height!=height)){
            renderTargetTexture?.close()
            val msaaParam = inputRenderPassData.values[RenderPassData.MSAA]
            var msaa = TextureObject.MSAALevels.Disable
            msaaParam?.let {
                msaa = when(it){
                    "4" -> TextureObject.MSAALevels.MSAAx4
                    "16" -> TextureObject.MSAALevels.MSAAx16
                    "8" -> TextureObject.MSAALevels.MSAAx8
                    else -> TextureObject.MSAALevels.Disable
                }
            }
            renderTargetTexture = RenderTargetTexture(gl,width,height,msaa)
        }
    }

    override fun cleanup() {
        renderTargetTexture?.close()
        super.cleanup()
    }
}