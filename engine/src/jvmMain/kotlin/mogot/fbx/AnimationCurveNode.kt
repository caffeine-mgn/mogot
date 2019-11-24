package mogot.fbx

import mogot.Spatial
import org.joml.Vector3f
import java.util.*

class AnimationCurveNode(val el: FbxFile.Element) : FbxObject {
    override val id: Long
        get() = el.properties[0] as Long

    lateinit var layout: FbxAnimationLayer

    override fun connectTo(obj: FbxObject, param: String?) {
        if (obj is FbxAnimationLayer) {
            obj.nodes += this
            layout = obj
            return
        }
        if (obj is FbxModel2) {
            obj.animations += this
            model = obj
            type = when (param) {
                "Lcl Translation" -> Type.POSITION
                "Lcl Scaling" -> Type.SCALE
                "Lcl Rotation" -> Type.ROTATION
                else -> TODO()
            }
            return
        }
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var x: AnimationCurve? = null
    var y: AnimationCurve? = null
    var z: AnimationCurve? = null

    var model: FbxModel2? = null
    lateinit var node: Spatial
    lateinit var type: Type

    enum class Type {
        POSITION,
        ROTATION,
        SCALE
    }

    fun calc(): Map<Long, Vector3f> {
        val map = TreeMap<Long, Vector3f>()

        x?.times?.forEachIndexed { index, it ->
            var r = map.getOrPut(it) { Vector3f() }
            r.x = x!!.values[index]
            if (y != null)
                r.y = y!!.get(it)

            if (z != null)
                r.z = z!!.get(it)
        }

        y?.times?.forEachIndexed { index, it ->
            val r = map.getOrPut(it) { Vector3f() }
            r.y = y!!.values[index]

            if (x != null)
                r.x = x!!.get(it)

            if (z != null)
                r.z = z!!.get(it)
        }

        z?.times?.forEachIndexed { index, it ->
            val r = map.getOrPut(it) { Vector3f() }
            r.z = z!!.values[index]

            if (x != null)
                r.x = x!!.get(it)

            if (y != null)
                r.y = y!!.get(it)
        }

        return map
    }
}