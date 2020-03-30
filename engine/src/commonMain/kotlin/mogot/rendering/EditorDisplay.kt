package mogot.rendering

import mogot.*

class EditorDisplay(renderPassChain: RenderPass, startRenderPassData: RenderPassData = RenderPassData()) : Display(renderPassChain, startRenderPassData) {
    fun setCamera(camera: Camera?){
        context.camera = camera
        context.camera?.enabled = true
        camera?.resize(context.width,context.height)
    }
    fun setCamera2D(camera2D: Camera2D?){
        context.camera2D = camera2D
        context.camera2D?.enabled = true
        camera2D?.resize(context.width,context.height)
    }

    override fun process(root: Node) {
        context.lights.clear()
        root.walk {
            if (it is Light){
                context.lights += it
            }
            true
        }
    }
}