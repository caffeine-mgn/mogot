package mogot

import mogot.math.Vector4fc

interface RenderContext {
    val pointLights: List<Light>
    val sceneColor: Vector4fc
}