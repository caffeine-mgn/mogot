package mogot.fbx

import org.joml.Math
import org.joml.Vector3f
import org.joml.Vector3fc

fun FbxFile.connectionByValue(value: Long) =
        get("Connections").single().get("C").map {
            if (it.properties[2] as Long == value)
                it.properties[1] as Long
            else
                null
        }.filterNotNull()

fun FbxFile.connectionByKey(key: Long) =
        get("Connections").single().get("C").map {
            if (it.properties[1] as Long == key)
                it.properties[2] as Long
            else
                null
        }.filterNotNull()

fun FbxFile.connectionByKeyEx(key: Long) =
        get("Connections").single().get("C").map {
            if (it.properties[1] as Long == key)
                it
            else
                null
        }.filterNotNull()

fun FbxFile.getModels() = get("Objects").first().get("Model").map {
    val id = it.properties[0] as Long
    val translation = Vector3f(0f, 0f, 0f)
    val scale = Vector3f(1f, 1f, 1f)
    val rotation = Vector3f(0f, 0f, 0f)

    it.get("Properties70").first().get("P").forEach {
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

    FbxModel(id = id, position = translation, rotation = rotation, scale = scale, name = it.properties[1] as String)
}

class FbxModel(val id: Long, val name: String, val position: Vector3fc, val scale: Vector3fc, val rotation: Vector3fc)