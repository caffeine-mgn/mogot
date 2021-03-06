package game

import mogot.Camera
import mogot.gl.*
import mogot.waitFrame
import pw.binom.io.FileSystemAccess
import pw.binom.io.file.File
import pw.binom.io.file.LocalFileSystem
import pw.binom.io.fullAccess
import java.awt.Dialog
import javax.swing.JFrame

class GameView : GLView(LocalFileSystem<Unit>(
        File("F:\\dev\\github\\mogot\\game"),
        FileSystemAccess.fullAccess()
)) {
    public override var camera: Camera? = null
    lateinit var game: TestGame
    private var closed = false
    private var inited = false
    override fun init() {
        super.init()
        backgroundColor.set(0f, 0.5f, 0.7f, 1f)
        game = TestGame(engine)
        camera = game.camera
        camera?.postEffectPipeline = PostEffectPipeline(engine)
        //camera?.postEffectPipeline?.addEffect(SimplePostEffect(engine, getNoColorShader(engine.gl)))
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
        f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        val view = GameView()
        f.add(view)
        f.isVisible = true
        view.startRender()
    }
}
