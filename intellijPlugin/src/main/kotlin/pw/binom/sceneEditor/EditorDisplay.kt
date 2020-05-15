package pw.binom.sceneEditor

import mogot.*
import mogot.gl.GL
import mogot.rendering.*

class EditorDisplay(val sceneRender: EditorSceneToTextureRenderPass = EditorSceneToTextureRenderPass(), val canvasRender: EditorCanvasToTexturePass = EditorCanvasToTexturePass(), startRenderPassData: RenderPassData = RenderPassData()) : Display(listOf(sceneRender, CanvasToTextureRenderPass(), FinalRenderPass()), startRenderPassData) {

    fun setCamera(camera: Camera?) {
        if (context.camera == camera)
            return
        context.camera?.enabled = false
        context.camera = camera
        context.camera?.enabled = true
        camera?.resize(context.width, context.height)
    }

    fun setCamera2D(camera2D: Camera2D?) {
        if (context.camera2D == camera2D)
            return
        context.camera2D?.enabled = false
        context.camera2D = camera2D
        context.camera2D?.enabled = true
        camera2D?.resize(context.width, context.height)
    }

    override fun process(root: Node) {
        context.lights.clear()
        root.walk {
            if (it is Light) {
                context.lights += it
            }
            true
        }
    }
}