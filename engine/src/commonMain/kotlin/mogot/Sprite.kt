package mogot

import mogot.math.*
import pw.binom.IntDataBuffer

open class Sprite(engine: Engine) : VisualInstance2D(engine), MaterialNode by MaterialNodeImpl() {

    companion object {

        fun calcPolygonTriangulation(vertex: List<Vector2fc>): List<Int> {
            val array = DoubleArray(vertex.size * 2)
            vertex.forEachIndexed { index, vector2fc ->
                array[index * 2 + 0] = vector2fc.x.toDouble()
                array[index * 2 + 1] = vector2fc.y.toDouble()
            }
            return Earcut.earcut(array, null, 2)
        }

        fun calcPolygonTriangulationIndexSize(vertex: List<Vector2fc>): Int {
            check(vertex.size >= 3)
            return (vertex.size - 3) * 3 + 3
        }

        fun calcPolygonTriangulation(vertex: List<Vector2fc>, dest: IntDataBuffer) {
            fun Array<*>.index(index: Int): Int {
                check(size > 0)
                if (index == 0)
                    return 0
                return if (index > 0) {
                    index % size
                } else {
                    (size - (index % -size))
                }
            }

            fun Array<Boolean>.nextFree(index: Int): Int {
                val i = index(index)
                (i until i + size).forEach {
                    if (this[it])
                        return it
                }
                return -1
            }

            if (dest.size != calcPolygonTriangulationIndexSize(vertex))
                throw IllegalArgumentException("Invalid dest buffer size.")
            check(vertex.size >= 3)
            if (vertex.size == 3) {
                dest[0] = 0
                dest[1] = 1
                dest[2] = 2
                return
            }
            val set = Array(vertex.size) { true }
            var resultIndex = 0
            var i = 0
            do {
                var skip = false
                while (i < set.size) {
                    val first = set.index(i)
                    i = first
                    val middle = set.nextFree(first + 1)
                    val end = set.nextFree(middle + 1)
                    val angle = vertexAngle(
                            vertex[first],
                            vertex[middle],
                            vertex[end]
                    )
                    if (angle > 0) {
                        dest[resultIndex++] = first
                        dest[resultIndex++] = middle
                        dest[resultIndex++] = end
                        set[middle] = false
                        i += 2
                    } else {
                        skip = true
                        i++
                    }
                }
            } while (skip)
            return
        }
    }

    val size = Vector2f()
    private var geom by ResourceHolder<Rect2D>()

    private val oldSize = Vector2f(size)
    override fun render(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        if (geom == null) {
            geom = Rect2D(engine.gl, size)
        }

        if (size != oldSize) {
            geom!!.size.set(size)
            oldSize.set(size)
        }
        super.render(model, projection, renderContext)
        val mat = material.value ?: return

        mat.use(model, projection, renderContext)
        geom!!.draw()
        mat.unuse()
    }

    override fun close() {
        material.dispose()
        geom = null
        super.close()
    }
}