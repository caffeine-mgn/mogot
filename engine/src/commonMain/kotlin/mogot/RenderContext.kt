package mogot

import mogot.math.Vector4fc

interface RenderContext {
    val pointLights: List<PointLight>
    val sceneColor: Vector4fc
}