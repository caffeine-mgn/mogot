package mogot

import mogot.math.Matrix4fc
import mogot.rendering.Display

interface Material : Resource {
    fun use(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context)
    fun unuse()
}

