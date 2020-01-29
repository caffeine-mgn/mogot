package mogot.physics.d2

import mogot.math.Vector2f
import mogot.math.Vector2fm
import mogot.math.add
import mogot.physics.box2d.common.Vec2
import mogot.physics.box2d.dynamics.World
import pw.binom.io.Closeable

class Manager2D : Closeable {

    internal var world = World(Vec2(0f, 0f))
    val gravity: Vector2fm = Vector2fUpdater { x, y ->
        world.setGravity(Vec2(x, y))
    }

    fun step(dt: Float) {
        world.step(dt, 8, 3)
    }

    override fun close() {

    }
}

private class Vector2fUpdater(val updater: (Float, Float) -> Unit) : Vector2f() {
    override fun set(x: Float, y: Float): Vector2fm {
        if (super.x != x || super.y != y)
            updater(x, y)
        return super.set(x, y)
    }

    override var x: Float
        get() = super.x
        set(value) {
            if (super.x != value) {
                updater(value, super.y)
            }
            super.x = value
        }
    override var y: Float
        get() = super.y
        set(value) {
            if (super.y != value) {
                updater(super.x, value)
            }
            super.y = value
        }
}