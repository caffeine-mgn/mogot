package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.gl.TextureObject
import mogot.gl.checkError
import mogot.math.Matrix4f
import mogot.math.Matrix4fc

class SceneFinalRenderPass : BaseRenderPass(null) {
    init {
        outputRenderPassData.values["stage"] = SceneRenderPass::class.simpleName!!
    }
    override fun render(context: Display.Context, gl: GL, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        if (context.camera != null)
            if(!bypass) {
                gl.clear(gl.COLOR_BUFFER_BIT or gl.DEPTH_BUFFER_BIT)
                gl.enable(gl.DEPTH_TEST)
                gl.enable(gl.CULL_FACE)
                renderNode3D(root, context.camera!!.transform, context.camera!!.projectionMatrix, context)
                gl.checkError{""}
                gl.disable(gl.DEPTH_TEST)
                gl.disable(gl.CULL_FACE)
            }
        return super.render(context, gl, root, dt, outputRenderPassData)
    }

    override fun setup(context: Display.Context, gl: GL, width: Int, height: Int, msaaLevel: TextureObject.MSAALevels) {
        super.setup(context, gl, width, height, msaaLevel)
    }

    /*private fun renderNode3D(node: Node, model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        var pos = model
        if (node.isVisualInstance) {
            node as VisualInstance
            if (!node.visible)
                return
            pos = node.matrix
            node.render(node.matrix, projection, context)
        }

        node.childs.forEach {
            renderNode3D(it, pos, projection, context)
        }
    }*/
}