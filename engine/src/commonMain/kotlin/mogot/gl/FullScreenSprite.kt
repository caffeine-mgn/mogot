package mogot.gl

import mogot.ResourceHolder
import mogot.ResourceImpl
import mogot.math.MATRIX4_ONE
import mogot.math.Matrix4f
import mogot.rendering.Display

class FullScreenSprite(gl: GL): ResourceImpl() {
    //    var defaultMaterial = FullScreenMaterial(engine)
    var material by ResourceHolder<MaterialGLSL>(FullScreenMaterial(gl))
    private val rect = ScreenRect(gl)

    fun draw(context: Display.Context) {
        val mat = material ?: TODO()
        mat.use(MATRIX4_ONE, MATRIX4_ONE, Matrix4f().identity(), context)
        rect.draw()
        mat.unuse()
    }

    override fun dispose() {
        material = null
        super.dispose()
    }
}