package pw.binom.sceneEditor

import mogot.EventDispatcher
import mogot.Node
import mogot.collider.Collider
import mogot.collider.Collider2D
import mogot.math.AABBm
import mogot.math.Vector2fc
import mogot.math.Vector3fc
import pw.binom.Services
import pw.binom.sceneEditor.nodeController.EditableNode
import pw.binom.sceneEditor.nodeController.EmptyNodeService
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.sceneEditor.properties.QuaternionEditor
import pw.binom.ui.*

interface NodeService {

    interface Field {
        val id: Int
        val groupName: String
        var currentValue: Any
        val value: Any
        fun clearTempValue()
        val name: String
        val displayName: String
        fun setTempValue(value: Any)
        fun resetValue()
        val fieldType: mogot.Field.Type
        val node: Node
        val eventChange: EventDispatcher
        val subFieldsEventChange: EventDispatcher
        fun makeEditor(sceneEditor: SceneEditor, fields: List<Field>): AbstractEditor
        fun getSubFields(): List<Field> = emptyList()
        fun isEquals(field: Field): Boolean
    }

    abstract class AbstractField : Field {
        protected abstract var realValue: Any
        protected abstract fun cloneRealValue(): Any
        private var originalValue: Any? = null
        override val id: Int
            get() = this::class.java.hashCode()

        override var currentValue: Any
            get() = realValue
            set(value) {
                realValue = value
            }
        override val value: Any
            get() = originalValue ?: currentValue

        override fun clearTempValue() {
            originalValue = null
        }


        override fun setTempValue(value: Any) {
            if (originalValue == null)
                originalValue = cloneRealValue()
            realValue = value
        }

        override fun resetValue() {
            if (originalValue != null) {
                realValue = originalValue!!
                originalValue = null
            }
        }

        override val eventChange = EventDispatcher()
        override fun getSubFields(): List<Field> = emptyList()
        override val subFieldsEventChange = EventDispatcher()
        override fun isEquals(field: Field): Boolean = TODO()
        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field>): AbstractEditor =
                when (fieldType) {
                    mogot.Field.Type.VEC3 -> EditorVec3(sceneEditor, fields)
                    mogot.Field.Type.INT -> EditorInt(sceneEditor, fields)
                    mogot.Field.Type.FLOAT -> EditorFloat(sceneEditor, fields)
                    mogot.Field.Type.BOOL -> TODO()
                    mogot.Field.Type.VEC2 -> EditorVec2(sceneEditor, fields)
                    mogot.Field.Type.VEC4 -> TODO()
                    mogot.Field.Type.STRING -> EditorString(sceneEditor, fields)
                    mogot.Field.Type.FILE -> EditorString(sceneEditor, fields)
                    mogot.Field.Type.QUATERNION -> QuaternionEditor(sceneEditor, fields)
                }
    }

    abstract class FieldVec3 : Field {
        override val subFieldsEventChange = mogot.EventDispatcher()
        override val eventChange = EventDispatcher()

        override fun isEquals(field: Field): Boolean {
            val currentValue = currentValue as Vector3fc
            val field_currentValue = field.currentValue as Vector3fc
            return currentValue.x == field_currentValue.x
                    && currentValue.y == field_currentValue.y
                    && currentValue.z == field_currentValue.z
        }

        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.VEC3

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field>): AbstractEditor {
            return EditorVec3(sceneEditor, fields)
        }
    }

    abstract class FieldInt : Field {
        override val subFieldsEventChange = EventDispatcher()
        override val eventChange = EventDispatcher()

        override fun isEquals(field: Field): Boolean =
                currentValue == field.currentValue

        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.INT

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field>): AbstractEditor {
            return EditorInt(sceneEditor, fields)
        }
    }

    abstract class FieldVec2 : Field {
        override val subFieldsEventChange = EventDispatcher()
        override val eventChange = EventDispatcher()

        override fun isEquals(field: Field): Boolean {
            val currentValue = currentValue as Vector2fc
            val field_currentValue = field.currentValue as Vector2fc
            return currentValue.x == field_currentValue.x
                    && currentValue.y == field_currentValue.y
        }

        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.VEC2

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field>): AbstractEditor {
            return EditorVec2(sceneEditor, fields)
        }
    }

    abstract class FieldFloat : Field {
        override val subFieldsEventChange = EventDispatcher()
        override val eventChange = EventDispatcher()

        override fun isEquals(field: Field): Boolean =
                currentValue == field.currentValue

        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.FLOAT

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field>): AbstractEditor {
            return EditorFloat(sceneEditor, fields)
        }
    }

    abstract class FieldString : Field {
        override val subFieldsEventChange = mogot.EventDispatcher()
        override val eventChange = EventDispatcher()

        override fun isEquals(field: Field): Boolean =
                currentValue == field.currentValue

        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.STRING

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field>): AbstractEditor {
            return EditorString(sceneEditor, fields)
        }
    }

    abstract class FieldFile : Field {
        override val subFieldsEventChange = mogot.EventDispatcher()
        override val eventChange = EventDispatcher()

        override fun isEquals(field: Field): Boolean =
                currentValue == field.currentValue

        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.STRING

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field>): AbstractEditor {
            return EditorString(sceneEditor, fields)
        }
    }

    fun getFields(view: SceneEditorView, node: Node): List<Field> {
        if (node is EditableNode)
            return node.getEditableFields()
        return emptyList()
    }

    fun getClassName(node: Node): String = nodeClass
    fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        //NOP
    }

    fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = emptyList()
    fun isEditor(node: Node): Boolean
    fun clone(view: SceneEditorView, node: Node): Node? {
        if (!isEditor(node))
            return null
        val newInstance = newInstance(view)
        newInstance as EditableNode
        node as EditableNode
        newInstance.id = node.id
        node.getEditableFields().forEach { from ->
            val to = newInstance.getEditableFields().find { it.name == from.name }!!
            to.currentValue = from.currentValue

            to.getSubFields().forEach { fromSub ->
                val toSub = to.getSubFields().find { it.name == fromSub.name }!!
                toSub.currentValue = fromSub.currentValue
            }
        }
        return newInstance
    }

    fun delete(view: SceneEditorView, node: Node) {
        EmptyNodeService.nodeDeleted(view.engine, node)
    }

    fun getAABB(node: Node, aabb: AABBm): Boolean = false
    fun getCollider(node: Node): Collider? = null
    fun getCollider2D(view: SceneEditorView, node: Node): Collider2D? = null
    fun isInternalChilds(node: Node): Boolean = false
    fun hover(view: SceneEditorView, node: Node, hover: Boolean) {
        //NOP
    }

    fun deepClone(view: SceneEditorView, node: Node): Node? {
        val clone = clone(view, node)
        val services by Services.byClassSequence(NodeService::class.java)
        node.childs.forEach { child ->
            val childService = services.find { it.isEditor(child) } ?: return@forEach
            childService.deepClone(view, child)?.parent = clone
        }
        return clone
    }

    val nodeClass: String
    fun newInstance(view: SceneEditorView): Node
}