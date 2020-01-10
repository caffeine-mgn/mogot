package pw.binom.fbx.file

class FbxAnimationStack(val el: FbxRoot.Element): FbxObject {
    override val id: Long
        get() = el.properties[0] as Long

    override fun connectTo(obj: FbxObject, param:String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val duration:Float = el.get("Properties70").first().get("P").filter { it.string(0) == "LocalStop" }.first().long(4).let {
        (it * FbxRoot.SECONDS_PER_UNIT).toFloat()
    }

    val layers = ArrayList<FbxAnimationLayer>()

}