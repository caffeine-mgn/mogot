package mogot.collider

import mogot.Camera
import mogot.Spatial
import mogot.math.MutableRay
import mogot.math.Vector3f
import kotlin.test.Test
import kotlin.test.assertEquals

class PanelColliderTest {

    @Test
    fun test() {
        val cam = Camera()
        cam.resize(800, 600)
        cam.position.set(10f, 10f, 10f)
        cam.lookTo(Vector3f(0f, 0f, 0f))
        val s = Spatial()
        val collider = PanelCollider(100f, 100f)
        collider.node = s
        val ray = MutableRay()
        cam.screenPointToRay(cam.width / 2, cam.height / 2, ray)
        val out = Vector3f()
        assertEquals(true, collider.rayCast(ray, out))
        println("ray=$ray out=$out")
        assertEquals(1f, out.x)
        assertEquals(-5f, out.y)
        assertEquals(1f, out.z)
    }
}