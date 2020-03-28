package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.gl.RenderTargetTexture
import mogot.gl.TextureObject
import mogot.math.Matrix4f
import mogot.math.Matrix4fc

class SceneRenderPass : TextureRenderPass() {
    private val cameraModel3DMatrix = Matrix4f()
    //private val cameraModel2DMatrix = Matrix4f()

    init {
        outputRenderPassData.values["stage"] = SceneRenderPass::class.simpleName!!
    }
    override fun render(renderContext: RenderContext, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        resize(inputRenderPassData, gl)
        requireNotNull(renderTargetTexture).apply {
            camera?.globalToLocalMatrix(cameraModel3DMatrix.identity())//camera2D?.globalToLocalMatrix(cameraModel2DMatrix.identity())?: cameraModel2DMatrix.identity()
            begin()
            gl.clear(gl.COLOR_BUFFER_BIT or gl.DEPTH_BUFFER_BIT)
            gl.enable(gl.DEPTH_TEST)
            gl.enable(gl.CULL_FACE)
            if (camera != null)
                renderNode3D(root,camera.transform,cameraModel3DMatrix,renderContext)
            gl.disable(gl.DEPTH_TEST)
            gl.disable(gl.CULL_FACE)
            end()
            outputRenderPassData.values[RenderPassData.WIDTH] = width
            outputRenderPassData.values[RenderPassData.HEIGHT] = height
            outputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] = this
        }
        return super.render(renderContext,gl,camera,camera2D,root, dt, outputRenderPassData)
    }

    private fun renderNode3D(node: Node, model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        var pos = model
        if (node.isVisualInstance) {
            node as VisualInstance
            if (!node.visible)
                return
            pos = node.matrix
            node.render(node.matrix, projection, renderContext)
        }

        node.childs.forEach {
            renderNode3D(it, pos, projection, renderContext)
        }
    }
}