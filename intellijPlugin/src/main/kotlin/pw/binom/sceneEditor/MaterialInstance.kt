package pw.binom.sceneEditor

import mogot.Material
import mogot.RenderContext
import mogot.ResourceImpl
import mogot.math.Matrix4fc

class MaterialInstance(val root: ExternalMaterial) : Material, ResourceImpl() {

    init {
        root.inc()
    }

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        root.use(model, projection, renderContext)
    }

    override fun unuse() {
        root.unuse()
    }

    override fun dispose() {
        root.dec()
        super.dispose()
    }

}