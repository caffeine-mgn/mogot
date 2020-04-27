package mogot

import mogot.math.*
import kotlin.test.Test

class CameraTest {

    val engine = mockEngine()

    @Test
    fun screenPointToRayNoParent() {
        val cam = Camera(engine)
        cam.resize(200, 100)
        cam.position.set(0f, 0f, -1f)
        cam.lookTo(Vector3f(0f, 0f, 1f))
        val ray = MutableRay()
        cam.screenPointToRay(100, 50, ray)
        ray.direction.x.eq(0f, 0.001f)
        ray.direction.y.eq(0f)
        ray.direction.z.eq(1f, 0.01f)
        ray.position.x.eq(0f)
        ray.position.y.eq(0f)
        ray.position.z.eq(-0.7f, 0.1f)
    }

    @Test
    fun screenProjection() {
        val cam = Camera(engine)
        cam.resize(200, 200)
        cam.position.set(3f, 3f, 3f)
        cam.lookTo(Vector3f(0f, 0f, 0f))
        val pos = Vector2i()
        cam.worldToScreenPoint(Vector3f(), pos).eq(true)
        pos.x.eq(100)
        pos.y.eq(99)
    }

    @Test
    fun screenPointToRayWithParent() {
        val root = Spatial()
        val cam = Camera(engine)
        root.position.set(0f, 0f, -1f)
        cam.position.set(0f, 0f, 0f)
        cam.parent = root
        cam.lookTo(Vector3f(0f, 0f, 1f))
        cam.resize(200, 100)

        val ray = MutableRay()
        cam.screenPointToRay(100, 50, ray)
        println("ray=$ray")
        ray.direction.x.eq(0f, 0.001f)
        ray.direction.y.eq(0f)
        ray.direction.z.eq(1f, 0.01f)
        ray.position.x.eq(0f)
        ray.position.y.eq(0f)
        ray.position.z.eq(-0.7f, 0.1f)
    }

    @Test
    fun test2() {
        val cam = Camera(engine)
        cam.resize(200, 200)
        cam.position.set(10f, 10f, 10f)
        cam.lookTo(Vector3f(0f, 0f, 0f))
        val ray = MutableRay()
        cam.screenPointToRay(cam.width / 2, cam.height / 2, ray)

        println("ray=$ray")
        ray.direction.also {
            it.x.eq(-0.57f, 0.1f)
            it.y.eq(-0.57f, 0.1f)
            it.z.eq(-0.57f, 0.1f)
        }
        ray.position.also {
            it.x.eq(9.8f, 0.1f)
            it.y.eq(9.8f, 0.1f)
            it.z.eq(9.8f, 0.1f)
        }
    }

    @Test
    fun test3() {
        val cam = Camera(engine)
        cam.resize(1113, 561)
        cam.position.set(1f, 10f, 0f)
        cam.lookTo(Vector3f(0f, 0f, 0f))
        val ray = MutableRay()
        cam.screenPointToRay(cam.width / 2, cam.height / 2, ray)

        println("ray=$ray")
        ray.direction.also {
            it.x.eq(-0.1f, 0.1f)
            it.y.eq(-0.99f, 0.01f)
            it.z.eq(0f, 0.1f)
        }
        ray.position.also {
            it.x.eq(1f, 0.1f)
            it.y.eq(9.7f, 0.1f)
            it.z.eq(0f, 0.1f)
        }
    }

    @Test
    fun test4() {
        val cam = Camera(engine)
        cam.resize(200, 100)
        cam.position.set(0f, 0f, 2f)
        cam.lookTo(Vector3f(0f, 0f, -2f))
        val ray = MutableRay()
        cam.screenPointToRay(100, 50, ray)
        ray.direction.x.eq(0f, 0.001f)
        ray.direction.y.eq(0f)
        ray.direction.z.eq(-1f, 0.01f)
        ray.position.x.eq(0f)
        ray.position.y.eq(0f)
        ray.position.z.eq(1.7f, 0.1f)
    }
}