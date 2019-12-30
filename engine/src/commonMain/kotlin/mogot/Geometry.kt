package mogot

interface Geometry : Resource {
    enum class RenderMode {
        TRIANGLES,
        LINES
    }

    var mode: RenderMode
    fun draw()
}