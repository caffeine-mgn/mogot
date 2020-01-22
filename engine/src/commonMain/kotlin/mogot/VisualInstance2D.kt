package mogot

val Node.isVisualInstance2D
    get() = (type and VISUAL_INSTANCE2D_TYPE) != 0

abstract class VisualInstance2D(engine: Engine): Spatial2D(engine) {
    var visible = true
    override val type: Int
        get() = VISUAL_INSTANCE2D_TYPE
}