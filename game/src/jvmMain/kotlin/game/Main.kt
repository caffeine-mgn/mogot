package game

import mogot.Camera
import mogot.gl.GLView
import javax.swing.JFrame

class GameView : GLView() {
    public override var camera: Camera? = null
    lateinit var game: TestGame
    override fun init() {
        super.init()
        backgroundColor.set(0f, 0.5f, 0.7f, 1f)
        game = TestGame(engine)
        camera = game.camera
    }
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val f = JFrame()
        f.setSize(800, 600)
        f.setLocationRelativeTo(null)
        f.title = "Demo"
        f.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        val view = GameView()
        f.add(view)
        f.isVisible = true
    }
}
