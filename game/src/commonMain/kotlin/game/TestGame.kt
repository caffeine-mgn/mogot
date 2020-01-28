package game

import mogot.*
import mogot.math.*

class TestGame(val engine: Engine) {
    val root = Node()
    val camera = Camera()

    init {
        camera.parent = root

        val mat = SimpleMaterial(engine).also {
            it.tex = engine.resources.createEmptyTexture2D()
        }

        val box = CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material.value = mat
        }

        CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material.value = mat
            position.set(7f, 5f, 5f)
        }

        CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material.value = mat
            position.set(3f, 5f, 5f)
        }

        CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material.value = mat
            position.set(5f, 7f, 5f)
        }

        CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material.value = mat
            position.set(5f, 3f, 5f)
        }
        camera.position.set(5f, 5f, 5f)
        val l = DirectLight()
        l.parent = camera

        camera.lookTo(Vector3f(0f, 0f, 0f))
        camera.behaviour = FpsCam(engine)

        val s = Sprite(engine)

        root.addChild(s)
        s.size.set(100f, 100f)
        s.material.value = mat

        engine.waitFrame {
            //val tex = engine.resources.createTexture2D("res/2.png")
            //mat.tex = tex
        }
    }
}