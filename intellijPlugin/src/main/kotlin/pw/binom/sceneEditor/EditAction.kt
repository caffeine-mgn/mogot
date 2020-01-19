package pw.binom.sceneEditor

import mogot.math.*
import java.awt.event.MouseEvent

interface EditAction {
    fun keyDown(code: Int) {}
    fun keyUp(code: Int) {}
    fun mouseDown(e: MouseEvent) {}
    fun mouseUp(e: MouseEvent) {}
    fun render(dt: Float) {}
    fun onStop() {}
}


fun Sequence<Vector2fc>.avg(): Vector2f {
    val out = Vector2f()
    var count = 0
    forEach {
        out.add(it)
        count++
    }
    if (count == 0)
        return out
    out.x = if (out.x == 0f) 0f else out.x / count.toFloat()
    out.y = if (out.y == 0f) 0f else out.y / count.toFloat()
    return out
}

fun Sequence<Vector3fc>.avg(): Vector3f {
    val out = Vector3f()
    var count = 0
    forEach {
        out.add(it)
        count++
    }
    if (count == 0)
        return out
    out.x = if (out.x == 0f) 0f else out.x / count.toFloat()
    out.y = if (out.y == 0f) 0f else out.y / count.toFloat()
    out.z = if (out.z == 0f) 0f else out.z / count.toFloat()
    return out
}