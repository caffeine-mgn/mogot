package mogot.collider

import mogot.math.Vector3f
import kotlin.test.Test
import mogot.test.assertEquals
import kotlin.test.assertTrue

class ColliderTest {

    @Test
    fun test() {
        val p0 = Vector3f(0.5f, -1f, 1f)
        val p1 = Vector3f(1f, -1f, 1f)
        val p2 = Vector3f(-1f, -1f, -1f)
        val rayPos = Vector3f(0f, 3f, 0f)
        val rayDir = Vector3f(0f, -1f, 0f)
        val out = Vector3f(-10f, -10f, -10f)
        assertTrue(Collider.rayCastTrangle(p0, p1, p2, rayPos, rayDir, out))
        assertEquals(0f, out.x)
        assertEquals(-1f, out.y)
        assertEquals(0f, out.z)
    }
}