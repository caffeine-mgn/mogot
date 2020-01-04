package mogot

val Node.isVisualInstance2D
    get() = (type and 0x8) > 0

open class VisualInstance2D : Spatial2D() {
    var visible = true
    override val type: Int
        get() = 0x8 or super.type
}