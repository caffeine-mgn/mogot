package mogot

val Node.isVisualInstance2D
    get() = (type and VISUAL_INSTANCE2D_TYPE) != 0

open class VisualInstance2D : Spatial2D() {
    var visible = true
    override val type: Int
        get() = VISUAL_INSTANCE2D_TYPE
}