package mogot

import mogot.math.*

interface Field {
    enum class Type(val prefix: String, val type: Byte) {
        INT("i", 0),
        FLOAT("f", 1),
        BOOL("b", 2),
        VEC2("v2", 3),
        VEC3("v3", 4),
        VEC4("v4", 5),
        STRING("s", 6),

        /**
         * String with a path to file. Can contain list of file separated "|". Also after file path can be path to
         * some entity inside file separated ">". For Example "models/hero.fbx>Body".
         */
        FILE("l", 7);

        fun toString(value: Any) =
                when (this) {
                    INT -> "${INT.prefix} $value"
                    FLOAT -> "${FLOAT.prefix} $value"
                    BOOL -> "${BOOL.prefix} ${if (value as Boolean) "t" else "f"}"
                    VEC2 -> {
                        value as Vector2fc
                        "${VEC2.prefix} ${value.x};${value.y}"
                    }
                    VEC3 -> {
                        value as Vector3fc
                        "${VEC3.prefix} ${value.x};${value.y};${value.z}"
                    }
                    VEC4 -> {
                        value as Vector4fc
                        "${VEC4.prefix} ${value.x};${value.y};${value.z};${value.w}"
                    }
                    STRING -> "${STRING.prefix} $value"
                    FILE -> "${FILE.prefix} $value"
                }

        companion object {
            const val LIST_SPLITOR = '|'
            const val INTERNAL_SPLITOR = '>'
            fun findByType(type: Byte) =
                    values().find { it.type == type } ?: throw RuntimeException("Can't find type $type")

            fun findByPrefix(value: String) =
                    values().find { value.startsWith(it.prefix) }
                            ?: throw RuntimeException("Can't find type by prefix for value $value")

            fun fromText(value: String): Any {
                val type = findByPrefix(value)
                val str = value.removePrefix(type.prefix).removePrefix(" ")
                return when (type) {
                    INT -> str.toInt()
                    FLOAT -> str.toFloat()
                    BOOL -> str == "t"
                    VEC2 -> {
                        val it = str.splitToSequence(';').iterator()
                        Vector2f(
                                it.next().toFloat(),
                                it.next().toFloat()
                        )
                    }
                    VEC3 -> {
                        val it = str.splitToSequence(';').iterator()
                        Vector3f(
                                it.next().toFloat(),
                                it.next().toFloat(),
                                it.next().toFloat()
                        )
                    }
                    VEC4 -> {
                        val it = str.splitToSequence(';').iterator()
                        Vector4f(
                                it.next().toFloat(),
                                it.next().toFloat(),
                                it.next().toFloat(),
                                it.next().toFloat()
                        )
                    }
                    STRING -> str
                    FILE -> str
                }
            }
        }
    }

    val name: String
    val type: Type
    var value: Any

    suspend fun setAsync(value: Any) {
        this.value = value
    }
}