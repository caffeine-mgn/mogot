package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.gl.TextureObject
import mogot.math.Matrix4fc

abstract class BaseRenderPass(override var next: RenderPass?) : RenderPass {
    protected val outputRenderPassData = RenderPassData()
    var bypass = false
    override fun render(context: Display.Context, gl: GL, root: Node, dt:Float, inputRenderPassData: RenderPassData): RenderPassData {
        return next?.render(context,gl,root,dt,outputRenderPassData)?:outputRenderPassData
    }

    override fun cleanup() {
        next?.cleanup()
    }

    protected fun renderNode3D(node: Node, model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
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

    protected fun renderNode2D(node: Node, projection: Matrix4fc, context: Display.Context) {
        if (node.isVisualInstance2D()) {
            if (!node.visible)
                return
            node.render(node.matrix, projection, context)
        }
        node.childs.forEach {
            renderNode2D(it, projection, context)
        }
    }

    override fun setup(context: Display.Context, gl: GL, msaaLevel: TextureObject.MSAALevels) {
        next?.setup(context,gl, msaaLevel)
    }
}