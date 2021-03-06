package mogot.collider

import mogot.Camera
import mogot.Spatial
import mogot.eq
import mogot.math.MutableRay
import mogot.math.Vector3f
import mogot.mockEngine
import kotlin.test.Test
import kotlin.test.assertEquals

class PanelColliderTest {

    val engine = mockEngine()

    @Test
    fun vec0_0_0() {
        val cam = Camera(engine)
        cam.resize(800, 600)
        cam.position.set(10f, 10f, 10f)
        cam.lookTo(Vector3f(0f, 0f, 0f))
        val s = Spatial()
        val collider = Panel3DCollider(100f, 100f)
        collider.node = s
        val ray = MutableRay()
        cam.screenPointToRay(cam.width / 2, cam.height / 2, ray)
        val out = Vector3f()
        assertEquals(true, collider.rayCast(ray, out))
        println("ray=$ray out=$out")
        out.x.eq(0f, 0.01f)
        out.y.eq(0f, 0.01f)
        out.z.eq(0f, 0.01f)
    }

    @Test
    fun vec0_1_0() {
        val cam = Camera(engine)
        cam.resize(800, 600)
        cam.near = 0.1f
        cam.position.set(10f, 10f, 10f)
        cam.lookTo(Vector3f(0f, 1f, 0f))
        val s = Spatial()
        s.position.set(0f, 1f, 0f)
        val collider = Panel3DCollider(100f, 100f)
        collider.node = s
        val ray = MutableRay()
        cam.screenPointToRay(cam.width / 2, cam.height / 2, ray)
        val out = Vector3f()
        assertEquals(true, collider.rayCast(ray, out))
        println("ray=$ray out=$out")
        out.x.eq(0f, 0.01f)
        out.y.eq(1f, 0.01f)
        out.z.eq(0f, 0.01f)
    }
}