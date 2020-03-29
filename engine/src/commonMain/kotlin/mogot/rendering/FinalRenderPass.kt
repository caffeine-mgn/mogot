package mogot.rendering

import mogot.*
import mogot.gl.FullScreenMaterial
import mogot.gl.FullScreenSprite
import mogot.gl.GL
import mogot.gl.RenderTargetTexture


class FinalRenderPass(nextPass:RenderPass) : BaseRenderPass(nextPass) {
    private var sprite:FullScreenSprite? = null
    private var mat:FullScreenMaterial? = null
    init {
        outputRenderPassData.values["stage"] = FinalRenderPass::class.simpleName!!
    }
    override fun render(context: Display.Context, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        if(sprite==null)
            sprite = FullScreenSprite(gl)
        if(mat == null)
            mat = FullScreenMaterial(gl)
        val texture = inputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] as RenderTargetTexture
        gl.bindFrameBuffer(gl.FRAMEBUFFER, null)
        gl.clear(gl.COLOR_BUFFER_BIT)
        mat?.texture2D = texture.getGlTexture()
        sprite?.material = mat
        gl.bindTexture(texture.getGlTextureTarget()!!, texture.getGlTexture())
        sprite?.draw(context)
        gl.bindTexture(texture.getGlTextureTarget()!!, null)
        return super.render(context, gl, camera, camera2D, root, dt, outputRenderPassData)
    }

    override fun cleanup() {
        sprite?.dec()
        mat?.dec()
        super.cleanup()
    }
}