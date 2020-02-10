package pw.binom.sceneEditor.properties

import com.intellij.openapi.ui.ComboBox
import mogot.Node
import mogot.onlySpatial2D
import pw.binom.sceneEditor.SceneEditorView
import javax.swing.JComponent
import mogot.physics.d2.PhysicsBody2D
import org.jbox2d.dynamics.BodyType
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.utils.common
import pw.binom.utils.isEmpty

object PhysicsBody2DPropertyPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = PhysicsBody2DProperty(view)

}

class PhysicsBody2DProperty(val view: SceneEditorView) : Property, Spoler("PhysicsBody2D") {
    private var nodes: List<PhysicsBody2D>? = null
    private val flex = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    private var type = ComboBox(arrayOf(BodyType.KINEMATIC, BodyType.STATIC, BodyType.DYNAMIC))
            .appendTo(flex)
    private var changeEventEnabled = false
    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.asSequence().onlySpatial2D().mapNotNull { it as? PhysicsBody2D }.toList()
        update()
    }

    fun update() {
        val nodes = nodes?.asSequence() ?: return

        changeEventEnabled = false
        if (nodes.isEmpty) {
            type.isEnabled = false
            return
        } else {
            val common = nodes.map { it.bodyType }.common
            type.isEnabled = common != null
            if (common != null)
                type.selectedItem = common
        }
        changeEventEnabled = true
    }

    init {
        type.addActionListener {
            if (changeEventEnabled) {
                nodes?.map { it.bodyType = type.selectedItem as BodyType }
                view.repaint()
            }
        }
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }

}