package game

import mogot.*
import mogot.math.Vector3f

class TestGame(val engine: Engine) {
    val root = Spatial()
    val camera = Camera()
    val box = CSGBox(engine)

    init {
        camera.parent = root
        box.parent = root
        box.width = 1f
        box.depth = 1f
        box.height = 1f
        box.material = SimpleMaterial(engine.gl)

        camera.position.x = 5f
        camera.position.y = 5f
        camera.position.z = 5f
        val l = OmniLight()
        l.parent = camera

        camera.lookTo(Vector3f(0f, 0f, 0f))
    }
}