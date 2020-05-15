package pw.binom.sceneEditor.nodeController

import mogot.*
import mogot.collider.Circle2DCollider
import mogot.collider.Collider2D
import mogot.math.Matrix4fc
import mogot.math.Vector2fm
import mogot.math.Vector4f
import mogot.math.set
import mogot.physics.d2.PhysicsBody2D
import mogot.physics.d2.shapes.CircleShape2D
import mogot.rendering.Display
import pw.binom.sceneEditor.CenterNode2D
import pw.binom.sceneEditor.NodeCreator
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.*
import pw.binom.utils.Vector2fmDelegator
import javax.swing.Icon
import mogot.physics.d2.shapes.Shape2D

object CircleShapeNodeCreator : NodeCreator {
    override val name: String
        get() = "CircleShape2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? {
        return CircleShape2DView(view)
    }

}

class RadiusEditableField(override val node: Node, val shape: CircleShape2DView) : NodeService.AbstractField() {
    override val groupName: String
        get() = "Shape"

    override var realValue: Any
        get() = shape.radius
        set(value) {
            shape.radius = value as Float
        }

    override fun cloneRealValue(): Any = shape.radius

    override val name: String
        get() = "raduis"
    override val displayName: String
        get() = "Radius"
    override val fieldType: Field.Type
        get() = Field.Type.FLOAT
}

class DensityEditableField(override val node: Node, val shape: ShapeEditorNode) : NodeService.AbstractField() {
    override val groupName: String
        get() = "Shape"

    override var realValue: Any
        get() = shape.density
        set(value) {
            shape.density = value as Float
        }

    override fun cloneRealValue(): Any = shape.density

    override val name: String
        get() = "density"
    override val displayName: String
        get() = "Density"
    override val fieldType: Field.Type
        get() = Field.Type.FLOAT
}

class FrictionEditableField(override val node: Node, val shape: ShapeEditorNode) : NodeService.AbstractField() {
    override val groupName: String
        get() = "Shape"

    override var realValue: Any
        get() = shape.friction
        set(value) {
            shape.friction = value as Float
        }

    override fun cloneRealValue(): Any = shape.density

    override val name: String
        get() = "friction"
    override val displayName: String
        get() = "Friction"
    override val fieldType: Field.Type
        get() = Field.Type.FLOAT
}

class RestitutionEditableField(override val node: Node, val shape: ShapeEditorNode) : NodeService.AbstractField() {
    override val groupName: String
        get() = "Shape"

    override var realValue: Any
        get() = shape.restitution
        set(value) {
            shape.restitution = value as Float
        }

    override fun cloneRealValue(): Any = shape.density

    override val name: String
        get() = "restitution"
    override val displayName: String
        get() = "Restitution"
    override val fieldType: Field.Type
        get() = Field.Type.FLOAT
}

class SensorEditableField(override val node: Node, val shape: ShapeEditorNode) : NodeService.AbstractField() {
    override val groupName: String
        get() = "Shape"

    override var realValue: Any
        get() = shape.sensor
        set(value) {
            shape.sensor = value as Boolean
        }

    override fun cloneRealValue(): Any = shape.density

    override val name: String
        get() = "sensor"
    override val displayName: String
        get() = "Sensor"
    override val fieldType: Field.Type
        get() = Field.Type.BOOL
}

object CircleShapeService : NodeService {

    private val properties = listOf(Transform2DPropertyFactory, CircleShape2DPropertyFactory, PhysicsShapePropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            properties

    override fun getClassName(node: Node): String =
            CircleShape2D::class.java.name

    override fun isEditor(node: Node): Boolean = node::class.java === CircleShape2DView::class.java

    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is CircleShape2DView) return null
        val out = CircleShape2DView(view)
        PhysicsShapeUtils.clone(node, out)
        Spatial2DService.cloneSpatial2D(node, out)
        out.radius = node.radius
        return out
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        if (node !is CircleShape2DView) return
        node.selected = selected
    }

    override fun hover(view: SceneEditorView, node: Node, hover: Boolean) {
        if (node !is CircleShape2DView) return
        node.hover = hover
    }

    override val nodeClass: String
        get() = CircleShape2D::class.java.name

    override fun newInstance(view: SceneEditorView): Node = CircleShape2DView(view)

    override fun getCollider2D(view: SceneEditorView, node: Node): Collider2D? {
        if (node !is CircleShape2DView) return null
        return node.collider
    }
}

class CircleShape2DView(val view: SceneEditorView) : VisualInstance2D(view.engine), ShapeEditorNode, EditableNode {
    private var geom by ResourceHolder<Geom2D>()
    private var material by ResourceHolder(view.default3DMaterial.instance(Vector4f()))
    private var center: CenterNode2D? = null
    val collider = Circle2DCollider().also {
        it.node = this
    }

    val transformField = PositionField2D(this)
    val radiusEditableField = RadiusEditableField(this, this)
    val rotationField = RotationField2D(this)
    val densityEditableField = DensityEditableField(this, this)
    val frictionEditableField = FrictionEditableField(this, this)
    val restitutionEditableField = RestitutionEditableField(this, this)
    val sensorEditableField = SensorEditableField(this, this)

    override var rotation: Float
        get() = super.rotation
        set(value) {
            super.rotation = value
            rotationField.eventChange.dispatch()
        }
    override val position: Vector2fm = Vector2fmDelegator(super.position) {
        transformField.eventChange.dispatch()
    }

    private val fields = listOf(transformField, rotationField, densityEditableField, frictionEditableField, restitutionEditableField, sensorEditableField, radiusEditableField)
    override fun getEditableFields(): List<NodeService.Field> = fields

    var radius = 50f
        set(value) {
            field = value
            collider.radius = value
            radiusEditableField.eventChange.dispatch()
        }


    private val body
        get() = parent as? PhysicsBody2D

    private fun refreshColor() {
        material!!.color.set(view.settings.getShapeColor(
                body?.bodyType,
                hover,
                selected
        ))
    }

    var hover = false
        set(value) {
            field = value
            refreshColor()
        }
    var selected = false
        set(value) {
            field = value
            center!!.visible = value
            refreshColor()
        }

    override var parent: Node?
        get() = super.parent
        set(value) {
            super.parent = value
            refreshColor()
        }

    override fun render(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        if (center == null) {
            center = CenterNode2D(this, view)
            center!!.parent = view.editorRoot
            center!!.visible = selected
        }
        if (geom == null) {
            geom = Geoms.circle(engine.gl, 0.5f, 12)
            refreshColor()
        }
        val mat = engine.mathPool.mat4f.poll()
        mat.set(model)
        mat.scale(radius * 2f, radius * 2f, 1f)
        material!!.use(mat, modelView, projection, context)
        engine.mathPool.mat4f.push(mat)
        geom!!.draw()
        material!!.unuse()
    }

    override fun close() {
        material = null
        geom = null
        center?.free()
        center = null
    }

    override var sensor: Boolean = false
    override var density: Float = 1f
    override var friction: Float = 0.5f
    override var restitution: Float = 0.2f
}