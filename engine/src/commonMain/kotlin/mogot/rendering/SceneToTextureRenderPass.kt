package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.gl.checkError

open class SceneToTextureRenderPass() : ToTextureRenderPass() {
    init {
        outputRenderPassData.values["stage"] = SceneToTextureRenderPass::class.simpleName!!
    }
    override fun render(context: Display.Context, gl: GL, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        requireNotNull(renderTargetTexture).apply {
            begin()
            gl.checkError{""}
            gl.clear(gl.COLOR_BUFFER_BIT or gl.DEPTH_BUFFER_BIT)
            gl.checkError{""}
            gl.enable(gl.DEPTH_TEST)
            gl.enable(gl.CULL_FACE)
            gl.checkError{""}
            if (context.camera != null)
                if(!bypass) {
                    customPreDraw3D(context.camera!!.transform, context.camera!!.projectionMatrix, context)
                    context.renderNode3D(root, context.camera!!.transform, context.camera!!.projectionMatrix, context)
                }
            gl.disable(gl.DEPTH_TEST)
            gl.disable(gl.CULL_FACE)
            end()
            gl.checkError{""}
            outputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] = this
        }
        return super.render(context,gl,root, dt, outputRenderPassData)
    }
}