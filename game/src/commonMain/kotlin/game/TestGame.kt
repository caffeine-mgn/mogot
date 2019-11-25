package game

import mogot.*
import mogot.math.*

class TestGame(val engine: Engine) {
    val root = Spatial()
    val camera = Camera()

    init {
        camera.parent = root

        val box = CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material = SimpleMaterial(engine.gl)
        }

        CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material = SimpleMaterial(engine.gl)
            position.set(7f, 5f, 5f)
        }

        CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material = SimpleMaterial(engine.gl)
            position.set(3f, 5f, 5f)
        }

        CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material = SimpleMaterial(engine.gl)
            position.set(5f, 7f, 5f)
        }

        CSGBox(engine).apply {
            parent = root
            width = 1f
            depth = 1f
            height = 1f
            material = SimpleMaterial(engine.gl)
            position.set(5f, 3f, 5f)
        }
        camera.position.x = 5f
        camera.position.y = 5f
        camera.position.z = 5f
        val l = OmniLight()
        l.parent = camera

        camera.lookTo(Vector3f(0f, 0f, 0f))
        camera.behaviour = FpsCam(engine)
    }
}

class FpsCam(val engine: Engine) : Behaviour() {
    override val node: Camera
        get() = super.node as Camera

    val normalSpeed = 3f
    val fastSpeed = 6f

    private var x = 0f
    private var y = 0f

    override fun onUpdate(delta: Float) {

        engine.stage.mouseDown.on {
            if (it == 2 || it == 3)
                engine.stage.lockMouse = true
        }
        engine.stage.mouseUp.on {
            if (it == 2 || it == 3)
                engine.stage.lockMouse = false
        }
        val delta = 0.01f
        super.onUpdate(delta)

        val moveSpeed = if (engine.stage.isKeyDown(340)) fastSpeed else normalSpeed

        if (engine.stage.isKeyDown(87)) {
            node.position.add(node.quaternion.forward * delta * moveSpeed)
            println("go forward")
//            node.position.add(node.rotation.forward * dt * moveSpeed)
        }

        if (engine.stage.isKeyDown(83)) {
            node.position.add(node.quaternion.forward * delta * -moveSpeed)
//            node.position.add(-node.rotation.forward * dt * moveSpeed)
        }

        if (engine.stage.isKeyDown(68)) {
            node.position.add(-node.quaternion.left * delta * moveSpeed)
        }

        if (engine.stage.isKeyDown(65)) {
            node.position.add(node.quaternion.left * delta * moveSpeed)
        }

        if (engine.stage.isKeyDown(69)) {
            node.position.add(node.quaternion.up * moveSpeed * delta)
        }

        if (engine.stage.isKeyDown(81)) {
            node.position.add(-node.quaternion.up * moveSpeed * delta)
        }
        if (engine.stage.isMouseDown(2) || engine.stage.isMouseDown(3)) {
            engine.stage.cursorVisible = false
//            engine.stage.lockMouse = true
//            node.rotation.rotateLocalX(((Input.mousePosition.y - oldMousePosition.y).toDouble() * dt / 10f).toFloat())
//            node.rotation.rotateLocalY(-((Input.mousePosition.x - oldMousePosition.x).toDouble() * dt / 10f).toFloat())

            val centerX = engine.stage.size.x / 2
            val centerY = engine.stage.size.y / 2
            x += (engine.stage.mousePosition.x - centerX) * delta / 5f
            y += (engine.stage.mousePosition.y - centerY) * delta / 5f

            node.quaternion.identity()
            node.quaternion.rotateXYZ(y, x, 0f)
//            node.quaternion.rotateXYZ(x,y,0f)
//            node.rotation2.y += (Input.mousePosition.x - oldMousePosition.x) * dt / 10f
//            node.rotation2.x += (Input.mousePosition.y - oldMousePosition.y) * dt / 10f
        } else {
            engine.stage.cursorVisible = true
//            engine.stage.lockMouse = false
        }
    }
//
//    override fun onUpdate(delta: Float) {
//        engine.stage.lockMouse = engine.stage.isMouseDown(1)
//    }
}