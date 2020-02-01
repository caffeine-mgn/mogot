package mogot

interface Geometry : Resource {
    enum class RenderMode {
        TRIANGLES,
        LINES,
        LINES_STRIP
    }

    var mode: RenderMode
    fun draw()
}