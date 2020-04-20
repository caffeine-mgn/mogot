package mogot

interface Field {
    enum class Type {
        INT, FLOAT, BOOL, VEC2, VEC3, VEC4, STRING
    }

    val name: String
    val type: Type
    var value: Any
}