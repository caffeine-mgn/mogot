package mogot

import mogot.gl.GL
import mogot.math.Vector2i
import mogot.math.Vector2ic
import pw.binom.io.AsyncOutputStream
import pw.binom.io.FileSystem
import pw.binom.io.FileSystemAccess
import kotlin.math.abs
import kotlin.test.assertEquals

class MockStage(size: Vector2ic) : Stage {
    override val gl: GL
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val mouseDown = EventValueDispatcher<Int>()
    override val mouseUp = EventValueDispatcher<Int>()
    var mouseDownButtens = HashSet<Int>()
    var keyDownButtens = HashSet<Int>()
    override fun isMouseDown(button: Int): Boolean = mouseDownButtens.contains(button)

    override fun isKeyDown(code: Int): Boolean = keyDownButtens.contains(code)

    override val mousePosition = Vector2i()
    override var lockMouse = false
    override var cursorVisible = true
    override val size = Vector2i(size)
}

class MockFileSystem : FileSystem<Unit> {
    override suspend fun get(user: Unit, path: String): FileSystem.Entity<Unit>? = null

    override suspend fun getDir(user: Unit, path: String): Sequence<FileSystem.Entity<Unit>>? = null

    override suspend fun mkdir(user: Unit, path: String): FileSystem.Entity<Unit> {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }

    override suspend fun new(user: Unit, path: String): AsyncOutputStream {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }
}

fun mockEngine(width: Int = 800, height: Int = 600): Engine {
    val stage = MockStage(Vector2i(width, height))
    return Engine(stage, MockFileSystem())
}

fun Camera2D.resize() {
    resize(engine.stage.size.x, engine.stage.size.y)
}

fun <T : Any> T.eq(value: T): T {
    assertEquals(value, this)
    return this
}

fun assertEquals(expected: Float, actual: Float, delta: Float = 0f) {
    if (abs(actual - expected) > abs(delta))
        throw AssertionError("expected: $expected, but was: $actual")
}

fun Float.eq(expected: Float, delta: Float = 0f): Float {
    if (abs(this - expected) > abs(delta))
        throw AssertionError("expected: $expected, but was: $this")
    return this
}