package mogot.rendering

import mogot.gl.GL
import mogot.gl.RenderTargetTexture
import mogot.gl.TextureObject

abstract class ToTextureRenderPass() : BaseRenderPass() {
    protected var renderTargetTexture: RenderTargetTexture? = null
    override fun setup(context: Display.Context, gl: GL, msaaLevel: TextureObject.MSAALevels) {
        //renderTargetTexture?.close()
        if (renderTargetTexture == null)
            renderTargetTexture = RenderTargetTexture(gl, context.width, context.height, msaaLevel)
        else
            renderTargetTexture?.resize(context.width, context.height)
    }

    override fun cleanup() {
        renderTargetTexture?.close()
    }
}