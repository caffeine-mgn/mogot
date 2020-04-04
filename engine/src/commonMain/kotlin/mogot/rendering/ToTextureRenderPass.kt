package mogot.rendering

import mogot.gl.GL
import mogot.gl.RenderTargetTexture
import mogot.gl.TextureObject

abstract class ToTextureRenderPass(nextPass:RenderPass?) : BaseRenderPass(nextPass) {
    protected var renderTargetTexture: RenderTargetTexture? = null
    override fun setup(context: Display.Context, gl: GL, width:Int, height:Int, msaaLevel: TextureObject.MSAALevels) {
        renderTargetTexture?.close()
        renderTargetTexture = RenderTargetTexture(gl,context.width,context.height,msaaLevel)
        super.setup(context, gl, width, height, msaaLevel)
    }
    override fun cleanup() {
        renderTargetTexture?.close()
        super.cleanup()
    }
}