package mogot.fbx

class FbxAnimationLayer(val el: FbxFile.Element) : FbxObject {
    override val id: Long
        get() = el.properties[0] as Long

    lateinit var animationStack: FbxAnimationStack

    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is FbxAnimationStack) {
            obj.layers += this
            animationStack = obj
            return
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val nodes = ArrayList<AnimationCurveNode>()
}