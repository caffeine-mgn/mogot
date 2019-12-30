package pw.binom.fbx.file

import mogot.ResourceImpl
import mogot.math.Vector3f


class FbxModel2(val el: FbxRoot.Element) : ResourceImpl(), FbxObject {
    override val id: Long
        get() = el.properties[0] as Long

    val childs = ArrayList<FbxModel2>()
    val animations = ArrayList<AnimationCurveNode>()
    var geometry: FbxGeometry? = null
    var material: FbxMaterial? = null
    var parent: FbxModel2? = null

    val translation = Vector3f(0f, 0f, 0f)
    val scale = Vector3f(1f, 1f, 1f)
    val rotation = Vector3f(0f, 0f, 0f)

    init {
        el.get("Properties70").first().get("P").forEach {
            when (it.properties[0] as String) {
                "Lcl Translation" -> translation.set(it.double(4).toFloat(), it.double(5).toFloat(), it.double(6).toFloat())
                "Lcl Scaling" -> scale.set(it.double(4).toFloat(), it.double(5).toFloat(), it.double(6).toFloat())
                "Lcl Rotation" -> rotation.set(
                        Math.toRadians(it.double(4)).toFloat(),
                        Math.toRadians(it.double(5)).toFloat(),
                        Math.toRadians(it.double(6)).toFloat()
                )
            }
        }
    }

    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is FbxModel2) {
            obj.childs += this
            this.parent = obj
            return
        }
        if (obj is FbxCluster) {
            obj.bone = this
            return
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}