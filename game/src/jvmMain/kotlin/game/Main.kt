package game

import mogot.Camera
import mogot.gl.GLView
import mogot.gl.SimplePostEffect
import mogot.gl.getFXAAShader
import mogot.waitFrame
import java.awt.Dialog
import javax.swing.JFrame

class GameView : GLView(TODO()) {
    public override var camera: Camera? = null
    lateinit var game: TestGame
    private var closed = false
    private var inited = false
    override fun init() {
        super.init()
        backgroundColor.set(0f, 0.5f, 0.7f, 1f)
        game = TestGame(engine)
        camera = game.camera
        postEffectPipeline!!.addEffect(SimplePostEffect(engine, getFXAAShader(engine.gl)))
        inited = true
    }

    override fun dispose() {
        closed = true
        super.dispose()
    }

//    fun startRender() {
//        while (!inited) {
//            Thread.sleep(1)
//        }
//        while (!closed) {
//            display()
//        }
//    }
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val f = JFrame()
        f.setSize(800, 600)
        f.setLocationRelativeTo(null)
        f.title = "Demo"
        f.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        f.modalExclusionType = Dialog.ModalExclusionType.NO_EXCLUDE
        val view = GameView()
        f.add(view)
        f.isVisible = true
        view.startRender()
    }
}
