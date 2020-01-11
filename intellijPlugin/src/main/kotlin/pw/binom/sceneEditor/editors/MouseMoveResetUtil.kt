package pw.binom.sceneEditor.editors

import mogot.math.Vector2i
import pw.binom.sceneEditor.SceneEditorView
import java.awt.MouseInfo
import java.awt.Robot

class MouseMoveResetUtil(val view: SceneEditorView) {
    private val robot = Robot()

    fun check(): Vector2i? {
        var needRefresh = false
        val viewLocation = view.locationOnScreen
        val mouseLocation = MouseInfo.getPointerInfo().location
        var x = mouseLocation.x
        var y = mouseLocation.y
        if (mouseLocation.x < viewLocation.x) {
            x = viewLocation.x + view.width
            needRefresh = true
        }

        if (mouseLocation.x > viewLocation.x + view.width) {
            x = viewLocation.x
            needRefresh = true
        }

        if (mouseLocation.y < viewLocation.y) {
            y = viewLocation.y + view.height
            needRefresh = true
        }

        if (mouseLocation.y > viewLocation.y + view.height) {
            y = viewLocation.y
            needRefresh = true
        }

        if (needRefresh) {
            robot.mouseMove(x, y)
            return Vector2i(x-viewLocation.x, y-viewLocation.y)
        }
        return null
    }
}