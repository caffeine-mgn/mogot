package mogot

import mogot.math.Matrix4f
import mogot.math.Vector2i
import mogot.math.Vector3f
import pw.binom.io.Closeable

/**
 * Object Pool for math classes
 */
class MathPool : Closeable {
    val vec2i = Vector2iPool()
    val vec3f = Vector3fPool()
    val vec4f = Vector4fPool()
    val mat4f = Matrix4fPool()

    override fun close() {
        vec3f.close()
        vec4f.close()
        mat4f.close()
    }
}

class Vector2iPool : ObjectPool<Vector2i>() {
    override fun create() = Vector2i()
}

class Vector3fPool : ObjectPool<Vector3f>() {
    override fun create() = Vector3f()
}

class Vector4fPool : ObjectPool<Vector3f>() {
    override fun create() = Vector3f()
}

class Matrix4fPool : ObjectPool<Matrix4f>() {
    override fun create() = Matrix4f()
}