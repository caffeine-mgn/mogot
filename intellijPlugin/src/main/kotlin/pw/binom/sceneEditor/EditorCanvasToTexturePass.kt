package pw.binom.sceneEditor

import mogot.math.Matrix4fc
import mogot.rendering.CanvasFinalRenderPass
import mogot.rendering.Display

class EditorCanvasToTexturePass : CanvasFinalRenderPass() {
    var grid: Grid2D? = null

    override fun customPreDraw2D(projection: Matrix4fc, context: Display.Context) {
        if (grid != null) {
            context.update(grid!!)
            context.renderNode2D(grid!!, projection, context)
        }
    }
}