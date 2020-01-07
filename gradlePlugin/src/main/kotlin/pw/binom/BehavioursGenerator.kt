package pw.binom

import java.io.File

class BehavioursGenerator {
    class BehavioursGeneratorException(message: String, throwable: Throwable? = null) : Exception(message, throwable)
    enum class Type {
        FLOAT,
        VEC3F,
        VEC4F,
        INT,
        BOOLEAN
    }

    class Property(val fileName: String, val name: String, val type: Type) {
        fun readValueFunc() = when (type) {
            Type.FLOAT -> "readFloat"
            Type.VEC3F -> "readVec3f"
            Type.VEC4F -> "readVec4f"
            Type.INT -> "readInt"
            Type.BOOLEAN -> "readBoolean"
        }
    }

    companion object {
        private fun createPropertyFromValue(fileName: String, name: String, value: String) =
                when {
                    value.startsWith("FLOAT ") -> Property(fileName, name, Type.FLOAT)
                    value.startsWith("VEC3F ") -> Property(fileName, name, Type.VEC3F)
                    value.startsWith("VEC4F ") -> Property(fileName, name, Type.VEC4F)
                    else -> throw IllegalArgumentException("Unknown property value type. Value is \"$value\"")
                }
    }

    class BehaviourClass(val className: String) {
        val properties = HashMap<String, Property>()
        fun addProperty(file: String, name: String, value: String) {
            try {
                val v = properties[name]
                val new = createPropertyFromValue(file, name, value)
                if (v != null) {
                    if (new.type != v.type)
                        throw IllegalArgumentException("Different types of same Behaviour Class: Behaviour Class is ${className}, Property: $name in files ${v.fileName}")
                } else {
                    properties[name] = new
                }
            } catch (e: Throwable) {
                throw BehavioursGeneratorException("Can't read property $className:$name for file $file", e)
            }
        }
    }

    fun generate(className: String, output: Appendable) {
        output.append("package mogot.gen\n\n")
        output.append("object ").append(className).append(" : mogot.scene.BehavioursLoader() {\n")
        output.append("\toverride fun readBehaviour(engine: mogot.Engine, data: Map<String, String>): mogot.Behaviour? {\n")
                .append("\t\tval className = data[\"behaviour.class\"] ?: return null\n")
        output.append("\t\treturn when (className) {\n")
        classes.values.forEach {
            output.append("\t\t\t\"${it.className}\" -> {\n")
            output.append("\t\t\t\tval out = ${it.className}(engine)\n")
            it.properties.values.forEach { prop ->
                output
                        .append("\t\t\t\tdata[\"behaviour.property.")
                        .append(prop.name)
                        .append("\"]?.let { out.")
                        .append(prop.name)
                        .append("=")
                        .append(prop.readValueFunc())
                        .append("(it) }\n")
            }
            output.append("\t\t\t\tout\n")
            output.append("\t\t\t}\n")
        }
        output.append("\t\t\telse -> throw IllegalArgumentException(\"Unknown Behaviour Class: \$className\")\n")
        output.append("\t\t}\n")
        output.append("\t}\n")
        output.append("}\n")
    }

    private val classes = HashMap<String, BehaviourClass>()

    fun getClass(name: String) = classes.getOrPut(name) { BehaviourClass(name) }
}