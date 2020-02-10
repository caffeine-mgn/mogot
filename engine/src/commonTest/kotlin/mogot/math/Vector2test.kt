package mogot.math

import mogot.eq
import kotlin.test.Test

class Vector2test {
    @Test
    fun vertexAngleTest() {
        vertexAngle(
                start = Vector2f(-5f, 0f),
                middle = Vector2f(0f, 0f),
                end = Vector2f(0f, 5f)
        ).eq(-PIHalf)

        vertexAngle(
                start = Vector2f(5f, 0f),
                middle = Vector2f(0f, 0f),
                end = Vector2f(0f, 5f)
        ).eq(PIHalf)
    }
}