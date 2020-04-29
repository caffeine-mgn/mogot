package pw.binom.sceneEditor

import mogot.*
import mogot.math.Matrix4fc
import mogot.rendering.Display

class SpriteFor3D(val view: SceneEditorView) : AbstractSprite(view.engine) {
    var internalMaterial by ResourceHolder<Material>()
    override val material
        get() = internalMaterial!!
    override val isReady: Boolean
        get() = true
    override var texture: Texture2D?
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun render(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        if (!view.render3D)
            return
        super.render(model, projection, context)
    }
}