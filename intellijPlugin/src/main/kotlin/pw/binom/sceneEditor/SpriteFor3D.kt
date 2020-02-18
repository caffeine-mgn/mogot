package pw.binom.sceneEditor

import mogot.*
import mogot.math.Matrix4fc

class SpriteFor3D(val view: SceneEditorView) : AbstractSprite(view.engine) {
    var internalMaterial by ResourceHolder<Material>()
    override val material
        get() = internalMaterial!!

    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (!view.render3D)
            return
        super.render(model, projection, renderContext)
    }
}