package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.gl.checkError
import mogot.math.Matrix4f
import mogot.math.Matrix4fc

open class SceneRenderPass(nextPass:RenderPass?) : ToTextureRenderPass(nextPass) {
    private val cameraModel3DMatrix = Matrix4f()
    //private val cameraModel2DMatrix = Matrix4f()

    init {
        outputRenderPassData.values["stage"] = SceneRenderPass::class.simpleName!!
    }
    override fun render(context: Display.Context, gl: GL, camera: Camera?, camera2D: Camera2D?, root: Node, dt: Float, inputRenderPassData: RenderPassData): RenderPassData {
        resize(context,inputRenderPassData, gl)
        requireNotNull(renderTargetTexture).apply {
            begin()
            gl.checkError{""}
            gl.clear(gl.COLOR_BUFFER_BIT or gl.DEPTH_BUFFER_BIT)
            gl.checkError{""}
            gl.enable(gl.DEPTH_TEST)
            gl.checkError{""}
            gl.enable(gl.CULL_FACE)
            gl.checkError{""}
            if (camera != null)
                if(!bypass) {
                    camera.globalToLocalMatrix(cameraModel3DMatrix.identity())
                    renderNode3D(root, camera.transform, cameraModel3DMatrix, context)
                }
            gl.disable(gl.DEPTH_TEST)
            gl.disable(gl.CULL_FACE)
            end()
            gl.checkError{""}
            outputRenderPassData.values[RenderPassData.RENDER_TARGET_TEXTURE] = this
        }
        return super.render(context,gl,camera,camera2D,root, dt, outputRenderPassData)
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