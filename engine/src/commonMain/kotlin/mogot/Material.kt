package mogot

import mogot.math.Matrix4fc

interface Material : Resource {
    fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext)
    fun unuse()
}

