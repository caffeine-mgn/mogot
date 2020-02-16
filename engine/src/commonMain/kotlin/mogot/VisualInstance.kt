package mogot

import mogot.math.Matrix4fc

val Node.isVisualInstance
    get() = (type and VISUAL_INSTANCE3D_TYPE) != 0

open class VisualInstance : Spatial() {
    var visible = true
    override val type: Int
        get() = VISUAL_INSTANCE3D_TYPE

    open fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        //NOP
    }
}