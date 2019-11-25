package mogot

import mogot.gl.GL
import mogot.math.Vector2ic

interface Stage {
    val gl: GL

    val mouseDown: EventValueDispatcher<Int>
    val mouseUp: EventValueDispatcher<Int>
    fun isMouseDown(button: Int): Boolean
    fun isKeyDown(code: Int): Boolean
    val mousePosition: Vector2ic
    var lockMouse: Boolean
    var cursorVisible: Boolean
    val size: Vector2ic
}