package mogot.rendering

import mogot.*
import mogot.gl.GL
import mogot.gl.TextureObject
import mogot.math.Matrix4f
import mogot.math.Matrix4fc

abstract class BaseRenderPass() : RenderPass {
    protected val outputRenderPassData = RenderPassData()
    var bypass = false
    override fun render(context: Display.Context, gl: GL, root: Node, dt:Float, inputRenderPassData: RenderPassData): RenderPassData {
        return outputRenderPassData
    }

    protected open fun customPreDraw2D(projection: Matrix4fc, context: Display.Context){

    }

    protected open fun customPreDraw3D(model: Matrix4fc, projection: Matrix4fc, context: Display.Context){

    }
}