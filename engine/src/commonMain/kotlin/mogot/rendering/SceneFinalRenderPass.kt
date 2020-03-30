package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.math.Matrix4f
import mogot.math.Matrix4fc

class SceneFinalRenderPass : BaseRenderPass(null) {
    private val cameraModel3DMatrix = Matrix4f()
    init {
        outputRenderPassData.values["stage"] = SceneRenderPass::class.simpleName!!
    }
    override fun render(context: Display.Context, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        if (camera != null)
            if(!bypass) {
                gl.clear(gl.COLOR_BUFFER_BIT or gl.DEPTH_BUFFER_BIT)
                gl.enable(gl.DEPTH_TEST)
                gl.enable(gl.CULL_FACE)
                camera.globalToLocalMatrix(cameraModel3DMatrix.identity())
                renderNode3D(root, camera.transform, cameraModel3DMatrix, context)
                gl.disable(gl.DEPTH_TEST)
                gl.disable(gl.CULL_FACE)
            }
        return super.render(context, gl, camera, camera2D, root, dt, outputRenderPassData)
    }

    private fun renderNode3D(node: Node, model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
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
    }
}