package mogot.rendering

import mogot.*
import mogot.gl.*


class FinalRenderPass() : BaseRenderPass() {
    private var sprite:FullScreenSprite? = null
    private var mat:FullScreenMaterial? = null
    init {
        outputRenderPassData.values["stage"] = FinalRenderPass::class.simpleName!!
    }
    override fun render(context: Display.Context, gl: GL, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        val texture = inputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] as RenderTargetTexture
        gl.bindFrameBuffer(gl.FRAMEBUFFER, null)
        gl.clear(gl.COLOR_BUFFER_BIT)
        mat?.texture2D = texture.getGlTexture()
        sprite?.material = mat
        //gl.bindTexture(texture.getGlTextureTarget()!!, texture.getGlTexture())
        sprite?.draw(context)
        gl.checkError{""}
        //gl.bindTexture(texture.getGlTextureTarget()!!, null)
        return super.render(context, gl, root, dt, outputRenderPassData)
    }

    override fun cleanup() {
        sprite?.dec()
        mat?.dec()
    }

    override fun setup(context: Display.Context, gl: GL, msaaLevel: TextureObject.MSAALevels) {
        if(sprite==null)
            sprite = FullScreenSprite(gl)
        if(mat == null)
            mat = FullScreenMaterial(gl)
    }
}