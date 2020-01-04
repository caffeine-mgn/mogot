package mogot

val Node.isVisualInstance
    get() = (type and 0x2) > 0

open class VisualInstance : Spatial() {
    override val type: Int
        get() = 0x2 or super.type
}