package mogot

val Node.isVisualInstance
    get() = (type and VISUAL_INSTANCE3D_TYPE) != 0

open class VisualInstance : Spatial() {
    var visible = true
    var shadow = false
    override val type: Int
        get() = VISUAL_INSTANCE3D_TYPE
}