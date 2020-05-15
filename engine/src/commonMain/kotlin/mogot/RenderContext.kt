package mogot

import mogot.math.Vector3fc
import mogot.math.Vector4fc

interface RenderContext {
    val lights: List<Light>
    val sceneColor: Vector4fc
    val cameraPosition: Vector3fc
}