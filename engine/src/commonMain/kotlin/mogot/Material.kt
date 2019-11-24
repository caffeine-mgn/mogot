package mogot

import mogot.math.Matrix4fc
import pw.binom.io.Closeable

interface Material : Closeable {
    fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext)
    fun unuse()
    fun free()
}

