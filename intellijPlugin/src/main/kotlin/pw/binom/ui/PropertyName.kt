package pw.binom.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.impl.ActionButton
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.properties.Panel
import javax.swing.JLabel

class PropertyName(text: String) : Panel() {
    private val flex = FlexLayout(this, direction = FlexLayout.Direction.ROW)
    private val title = JLabel(text).appendTo(flex)
    private var resetButton = ActionButton(ResetAction(), Presentation("").also { it.icon = AllIcons.General.Reset }, ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
            .appendTo(flex, grow = 0)

    var resetVisible: Boolean
        get() = resetButton.isVisible
        set(value) {
            resetButton.isVisible = value
        }

    var resetAction: (() -> Unit)? = null

    init {
        isOpaque = false
        resetVisible = false
    }

    private inner class ResetAction : AnAction() {
        override fun actionPerformed(e: AnActionEvent) {
            resetAction?.invoke()
        }
    }

    fun resetAction(func: () -> Unit): PropertyName {
        this.resetAction = func
        return this
    }
}