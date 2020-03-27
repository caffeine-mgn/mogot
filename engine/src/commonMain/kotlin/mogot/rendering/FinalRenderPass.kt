package mogot.rendering

import mogot.*
import mogot.gl.FullScreenMaterial
import mogot.gl.FullScreenSprite
import mogot.gl.GL
import mogot.gl.RenderTargetTexture


class FinalRenderPass(val engine:Engine) : BaseRenderPass() {
    val sprite = FullScreenSprite(engine)
    private val mat = FullScreenMaterial(engine)
    init {
        outputRenderPassData.values["stage"] = FinalRenderPass::class.simpleName!!
    }
    override fun render(renderContext: RenderContext, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        val texture = inputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] as RenderTargetTexture
        gl.bindFrameBuffer(gl.FRAMEBUFFER, null)
        gl.clear(gl.COLOR_BUFFER_BIT)
        mat.texture2D = texture.getGlTexture()
        sprite.material = mat
        gl.bindTexture(texture.getGlTextureTarget()!!, texture.getGlTexture())
        sprite.draw(renderContext)
        gl.bindTexture(texture.getGlTextureTarget()!!, null)
        return super.render(renderContext, gl, camera, camera2D, root, dt, outputRenderPassData)
    }
}