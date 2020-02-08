package mogot

import mogot.math.Vector4fc

interface RenderContext {
    val lights: List<Light>
    val shadowMaps: MutableList<Texture2D>
    val sceneColor: Vector4fc
}