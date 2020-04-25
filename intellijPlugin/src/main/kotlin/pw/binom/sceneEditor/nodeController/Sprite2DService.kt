package pw.binom.sceneEditor.nodeController

import mogot.AbstractSprite
import mogot.Node
import mogot.Sprite
import mogot.Texture2D
import mogot.collider.Collider2D
import mogot.collider.Panel2DCollider
import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.Vector2fm
import mogot.math.set
import pw.binom.sceneEditor.*
import pw.binom.sceneEditor.properties.BehaviourPropertyFactory
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.Sprite2DPropertyFactory
import pw.binom.sceneEditor.properties.Transform2DPropertyFactory
import pw.binom.utils.Vector2fmDelegator
import java.io.Closeable
import javax.swing.Icon
import kotlin.collections.set

object Sprite2DCreator: NodeCreator {
    override val name: String
        get() = "Sprite2D"
    override val icon: Icon?
        get() = null

    override fun create(view: SceneEditorView): Node? {
        val node = EditableSprite(view)
        view.nodesMeta[node] = SpriteMeta(view, node)
        return node
    }

}

private class SpriteMeta(view: SceneEditorView, node: EditableSprite) : Closeable {

    override fun close() {

    }
}

object Sprite2DService : NodeService {

    override fun getClassName(node: Node): String = Sprite::class.java.name

    private val props = listOf(Transform2DPropertyFactory, Sprite2DPropertyFactory, BehaviourPropertyFactory)
    override fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> =
            props


    override fun delete(view: SceneEditorView, node: Node) {
        super.delete(view, node)
        println("Delete sprite!")
        (view.nodesMeta.remove(node) as SpriteMeta).close()
    }

    override fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        node as EditableSprite
        val material = node.material
        material.selected = selected
        val meta = view.nodesMeta[node] as? SpriteMeta
        node.center.visible = selected
    }

    override fun isEditor(node: Node): Boolean = node::class.java == EditableSprite::class.java
    override fun clone(view: SceneEditorView, node: Node): Node? {
        if (node !is EditableSprite) return null
        val out = EditableSprite(view)
        out.size.set(node.size)
        Spatial2DService.cloneSpatial2D(node, out)
        out.texture = node.texture
        view.nodesMeta[out] = SpriteMeta(view, out)
        return out
    }

    override fun hover(view: SceneEditorView, node: Node, hover: Boolean) {
        node as EditableSprite
        val m = node.material
        m.hover = hover
    }

    override val nodeClass: String
        get() = Sprite::class.java.name

    override fun newInstance(view: SceneEditorView): Node {
        val node = EditableSprite(view)
        view.nodesMeta[node] = SpriteMeta(view, node)
        return node
    }

    override fun getCollider2D(view: SceneEditorView, node: Node): Collider2D? {
        node as EditableSprite
        val c = Panel2DCollider()
        c.node = node
        c.size.set(node.size)
        return c
    }
}

class SpriteSizeField(override val node: EditableSprite) : NodeService.FieldVec2() {
    override val id: Int
        get() = SpriteSizeField::class.java.hashCode()
    override val groupName: String
        get() = "Sprite"
    override var currentValue: Any
        get() = node.size
        set(value) {
            node.size.set(value as Vector2fc)
        }
    override val value: Vector2fc
        get() = node.size
    private var originalValue: Vector2fc? = null
    override fun clearTempValue() {
        originalValue = null
    }

    override val name: String
        get() = "size"
    override val displayName: String
        get() = "Size"

    override fun setTempValue(value: Any) {
        if (originalValue == null)
            originalValue = Vector2f(currentValue as Vector2fc)
        currentValue = value
    }

    override fun resetValue() {
        if (originalValue != null) {
            currentValue = originalValue!!
            originalValue = null
        }
    }
}


class EditableSprite(view: SceneEditorView) : AbstractSprite(view.engine), EditableNode {

    val transformField = PositionField2D(this)
    val rotationField = RotationField2D(this)
    val scaleField = ScaleField2D(this)
    val textureField = TextureSpriteField(view, this)
    val sizeField = SpriteSizeField(this)

    override val position: Vector2fm = Vector2fmDelegator(super.position) {
        transformField.eventChange.dispatch()
    }
    override val scale: Vector2fm = Vector2fmDelegator(super.scale) {
        scaleField.eventChange.dispatch()
    }

    override val size: Vector2fm = Vector2fmDelegator(super.size) {
        sizeField.eventChange.dispatch()
    }

    override var rotation: Float
        get() = super.rotation
        set(value) {
            super.rotation = value
            rotationField.eventChange.dispatch()
        }
    var textureFile: ExternalTextureFS?
        get() = material.image
        set(value) {
            material.image = value
        }

    override val isReady: Boolean
        get() = true

    override var texture: Texture2D?
        get() = TODO("Not yet implemented")
        set(value) {}

    public override val material = view.default2DMaterial.instance()

    override fun close() {
        center.free()
        texture = null
        material.dec()
        super.close()
    }

    private val fields = listOf(transformField, rotationField, scaleField, sizeField, textureField)
    override fun getEditableFields(): List<NodeService.Field> = fields

    var selected: Boolean = false
    val center = CenterNode2D(this, view)

    init {
        center.parent = view.editorRoot
        center.visible = false
    }
}

