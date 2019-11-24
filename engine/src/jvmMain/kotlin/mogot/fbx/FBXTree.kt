package mogot.fbx

object FBXTree {
    private fun typeToStr(obj: Any) = when (obj) {
        is String -> "string"
        is Boolean -> "bool"
        is Byte -> "byte"
        is Double -> "double"
        is Int -> "int"
        is Long -> "long"
        is BooleanArray -> ""
        is IntArray -> ""
        is ByteArray -> "byte[]"
        is FloatArray -> ""
        is DoubleArray -> ""
        is LongArray -> ""
        else -> obj::class.java.name
    }

    fun draw(list: List<FbxFile.Element>, level: Int) {
        list.forEach {
            print("|")
            print(" " * 3 * (level))
            print("+")
            print("-" * 2)
            print("${it.id}")
            if (it.properties.isEmpty())
                println()
            else
                println("(${it.properties.joinToString { "${toString(it)} ${typeToStr(it)}" }})")
            draw(it.childs, level + 1)
        }
    }

    private fun toString(any: Any) =
            when (any) {
                is FloatArray -> "float [${any.joinToString()}]"
                is IntArray -> "int [${any.joinToString()}]"
                is LongArray -> "long [${any.joinToString()}]"
                is BooleanArray -> "bool [${any.joinToString()}]"
                is DoubleArray -> "double [${any.joinToString()}]"
                else -> any.toString()
            }

    fun draw(file: FbxFile) {
        draw(file.elements, 0)
    }
}

private operator fun String.times(i: Int): String {
    val sb = StringBuilder()
    (0 until i).forEach { sb.append(this) }
    return sb.toString()
}