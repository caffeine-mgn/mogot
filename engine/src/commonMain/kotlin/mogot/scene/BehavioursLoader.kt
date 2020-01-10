package mogot.scene

import mogot.math.Vector3f
import mogot.math.Vector4f

abstract class BehavioursLoader {
    fun readInt(str: String) = str.removePrefix("INT ").toInt()
    fun readFloat(str: String) = str.removePrefix("FLOAT ").toFloat()
    fun readBoolean(str: String) = str.removePrefix("BOOL ").let { it == "true" }
    fun readVec3f(str: String) = str.removePrefix("VEC3F").let {
        val items = it.split(';')
        Vector3f(items[0].toFloat(), items[1].toFloat(), items[2].toFloat())
    }

    fun readVec4f(str: String) = str.removePrefix("VEC3F").let {
        val items = it.split(';')
        Vector4f(items[0].toFloat(), items[1].toFloat(), items[2].toFloat(), items[3].toFloat())
    }
    abstract fun readBehaviour(engine: mogot.Engine, data: Map<String, String>): mogot.Behaviour?
}