package pw.binom.animation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import mogot.Field
import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.Vector3f
import mogot.math.Vector3fc
import pw.binom.array
import pw.binom.obj
import java.io.Reader

object Animation {
    interface AnimationVisitor {
        fun obj(path: String): ObjectVisitor?
        fun start(frameInSecond: Int, frameCount: Int)
        fun end() {}
    }

    interface ObjectVisitor {
        fun property(display: String, name: String, type: Field.Type): PropertyVisitor?
    }

    interface PropertyVisitor {
        fun addFrame(time: Int, value: Any)
    }

//    enum class PropertyType {
//        VEC3,
//        VEC2,
//        FLOAT,
//        STRING,
//        INT
//    }

    private fun fromString(type: Field.Type, value: String) =
            when (type) {
                Field.Type.FLOAT -> value.toFloatOrNull() ?: 0f
                Field.Type.VEC2 -> value.split(';').let { Vector2f(it[0].toFloat(), it[1].toFloat()) }
                Field.Type.VEC3 -> value.split(';').let { Vector3f(it[0].toFloat(), it[1].toFloat(), it[2].toFloat()) }
                Field.Type.INT -> value.toIntOrNull() ?: 0
                Field.Type.STRING -> value
                Field.Type.BOOL -> value.toInt() > 0
            }

    private fun toString(type: Field.Type, value: Any): String? =
            when (type) {
                Field.Type.FLOAT -> (value as Float?)?.toString()
                Field.Type.VEC2 -> (value as Vector2fc?)?.let { "${it.x};${it.y}" }
                Field.Type.VEC3 -> (value as Vector3fc?)?.let { "${it.x};${it.y};${it.z}" }
                Field.Type.STRING -> value as String?
                Field.Type.INT -> (value as Int).toString()
                Field.Type.BOOL -> if (value as Boolean) "1" else "0"
            }

    fun load(reader: Reader, visitor: AnimationVisitor) {
        val mapper = ObjectMapper()
        val tree = mapper.readTree(reader)
        visitor.start(
                frameInSecond = tree["frameInSecond"]?.intValue() ?: 0,
                frameCount = tree["frameCount"]?.intValue() ?: 0
        )

        tree["objects"]?.array?.forEach { line ->
            val obj = visitor.obj(line.obj["path"].textValue()) ?: return@forEach
            line.obj["properties"].array.forEach { property ->
                val type = Field.Type.valueOf(property.obj["type"].textValue())
                val prop = obj.property(
                        display = property.obj["text"].textValue(),
                        name = property.obj["name"].textValue(),
                        type = type
                )
                if (prop != null)
                    property.obj["frames"]?.array?.forEach { frame ->
                        val time = frame.obj["time"].intValue()
                        val value = fromString(type, frame.obj["val"]!!.textValue())
                        prop.addFrame(time, value)
                    }
            }
        }
        visitor.end()
    }

    class AnimationJsonVisitor(private val stream: Appendable) : AnimationVisitor {
        private val rootJson = JsonNodeFactory.instance.objectNode()
        private val objectsJson by lazy {
            val o = JsonNodeFactory.instance.arrayNode()
            rootJson.set<JsonNode>("objects", o)
            o
        }

        override fun start(frameInSecond: Int, frameCount: Int) {
            rootJson.put("frameInSecond", frameInSecond)
            rootJson.put("frameCount", frameCount)
        }

        override fun obj(path: String): ObjectVisitor? {
            val obj = JsonNodeFactory.instance.objectNode()
            objectsJson.add(obj)
            obj.put("path", path)
            return ObjectVisitorJson(obj)
        }

        override fun end() {
            val mapper = ObjectMapper()
            stream.append(mapper.writeValueAsString(rootJson))
        }
    }

    private class ObjectVisitorJson(node: ObjectNode) : ObjectVisitor {
        private val objectsJson by lazy {
            val o = JsonNodeFactory.instance.arrayNode()
            node.set<JsonNode>("properties", o)
            o
        }

        override fun property(display: String, name: String, type: Field.Type): PropertyVisitor? {
            val prop = JsonNodeFactory.instance.objectNode()
            prop.put("type", type.name)
            prop.put("text", display)
            prop.put("name", name)
            objectsJson.add(prop)
            return PropertyVisitorJson(prop, type)
        }
    }

    private class PropertyVisitorJson(node: ObjectNode, private val type: Field.Type) : PropertyVisitor {
        private val framesJson by lazy {
            val o = JsonNodeFactory.instance.arrayNode()
            node.set<JsonNode>("frames", o)
            o
        }

        override fun addFrame(time: Int, value: Any) {
            val prop = JsonNodeFactory.instance.objectNode()
            prop.put("time", time)
            prop.put("val", toString(type, value))
            framesJson.add(prop)
        }

    }
}