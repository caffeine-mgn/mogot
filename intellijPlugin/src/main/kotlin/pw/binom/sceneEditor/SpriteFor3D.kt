package pw.binom.sceneEditor

import mogot.*
import mogot.math.Matrix4fc

class SpriteFor3D(val view: SceneEditorView) : Sprite(view.engine) {

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (!view.render3D)
            return
        super.render(model, projection, renderContext)
    }
}