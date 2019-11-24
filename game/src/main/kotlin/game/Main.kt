package game

import fonts.Arial12
import jodot.root
import mogot.Camera
import mogot.FPSCam
import mogot.Node2D
import mogot.Spatial
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL45
import org.tlsys.engine.GLFWWindowStage
import org.tlsys.engine.Stage

class Text : Node2D() {
    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: Stage.RenderContext) {
        super.render(model, projection, renderContext)
        Arial12.drawText(Vector4f(0f, 0f, 0f, 1f), "Привет мир!      Hello world!", model = model, projection = projection, renderContext = renderContext)
    }
}

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        val root by lazy { root() }
        val w = object : GLFWWindowStage() {
            override fun resize(width: Int, height: Int) {
                root.camera.resize(width, height)
            }
        }
        w.rootNode = root
        w.camera = root.camera
        root.camera.behaviour = FPSCam()
        val txt = Text()
        root.addChild(txt)

        txt.position.set(100f, 10f)
//        root.childs.map { it as? Camera }.filterNotNull().forEach { it.position.set(0f, 0f, 0f) }
        root.childs.asSequence().map { it as? Camera }.filterNotNull().first().position.set(0f, 60f, 0f)
        w.start()
    }
}