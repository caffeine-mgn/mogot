package mogot.math

expect object Math  {
    val PI:Double

    fun sin(value: Double):Double
    fun cos(value: Double):Double
    fun toRadians(value: Double):Double
}

val MATRIX4_ONE: Matrix4fc = Matrix4f().identity()