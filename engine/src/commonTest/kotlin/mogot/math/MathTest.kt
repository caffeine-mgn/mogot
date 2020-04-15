package mogot.math

import mogot.eq
import kotlin.test.Test

class MathTest {

    @Test
    fun lerpFloat() {
        10f.lerp(20f, 0.5f).eq(15f)
    }
}