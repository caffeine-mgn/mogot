package pw.binom.sceneEditor

import com.intellij.openapi.vfs.VirtualFile
import mogot.EventDispatcher
import mogot.Node
import mogot.collider.Collider
import mogot.collider.Collider2D
import mogot.math.*
import pw.binom.Services
import pw.binom.sceneEditor.nodeController.EditableNode
import pw.binom.sceneEditor.nodeController.EmptyNodeService
import pw.binom.sceneEditor.properties.PropertyFactory
import pw.binom.ui.*

interface NodeService {

//    enum class FieldType {
//        VEC3, VEC2, FLOAT, STRING, INT
//    }

    interface Field<T : Any> {
        val id: Int
        val groupName: String
        var currentValue: T
        val value: T
        val isEmpty: Boolean
        fun clearTempValue()
        fun saveAsString(): String
        fun loadFromString(value: String)
        val name: String
        val displayName: String
        fun setTempValue(value: T)
        fun resetValue()
        val fieldType: mogot.Field.Type
        val node: Node
        val eventChange: EventDispatcher
        val subFieldsEventChange: EventDispatcher
        fun makeEditor(sceneEditor: SceneEditor, fields: List<Field<T>>): AbstractEditor<T>
        fun getSubFields(): List<Field<out Any>> = emptyList()
        fun isEquals(field: Field<T>): Boolean
    }

    abstract class FieldVec3 : Field<Vector3fc> {
        override val subFieldsEventChange = mogot.EventDispatcher()
        override val eventChange = EventDispatcher()
        override fun saveAsString(): String {
            val value = currentValue
            return "VEC3 ${value.x};${value.y};${value.z}"
        }

        override fun isEquals(field: Field<Vector3fc>): Boolean =
                currentValue.x == field.currentValue.x
                        && currentValue.y == field.currentValue.y
                        && currentValue.z == field.currentValue.z

        override fun loadFromString(value: String) {
            check(value.startsWith("VEC3 "))
            val items = value.substring(5).split(';')
            val x = items[0].toFloatOrNull() ?: 0f
            val y = items[0].toFloatOrNull() ?: 0f
            val z = items[0].toFloatOrNull() ?: 0f
            this.currentValue = Vector3f(x, y, z)
        }

        override val isEmpty: Boolean = false
        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.VEC3

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field<Vector3fc>>): AbstractEditor<Vector3fc> {
            return EditorVec3(sceneEditor, fields)
        }
    }

    abstract class FieldInt : Field<Int> {
        override val subFieldsEventChange = mogot.EventDispatcher()
        override val eventChange = EventDispatcher()
        override fun saveAsString(): String {
            val value = currentValue
            return "INT $value"
        }

        override fun isEquals(field: Field<Int>): Boolean =
                currentValue == field.currentValue

        override fun loadFromString(value: String) {
            check(value.startsWith("INT "))
            this.currentValue = value.substring(4).toInt()
        }

        override val isEmpty: Boolean = false
        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.INT

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field<Int>>): AbstractEditor<Int> {
            return EditorInt(sceneEditor, fields)
        }
    }

    abstract class FieldVec2 : Field<Vector2fc> {
        override val subFieldsEventChange = mogot.EventDispatcher()
        override val eventChange = EventDispatcher()
        override fun saveAsString(): String {
            val value = currentValue
            return "VEC2 ${value.x};${value.y}"
        }

        override fun isEquals(field: Field<Vector2fc>): Boolean =
                currentValue.x == field.currentValue.x
                        && currentValue.y == field.currentValue.y

        override fun loadFromString(value: String) {
            check(value.startsWith("VEC2 "))
            val items = value.substring(5).split(';')
            val x = items[0].toFloatOrNull() ?: 0f
            val y = items[0].toFloatOrNull() ?: 0f
            this.currentValue = Vector2f(x, y)
        }

        override val isEmpty: Boolean = false
        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.VEC2

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field<Vector2fc>>): AbstractEditor<Vector2fc> {
            return EditorVec2(sceneEditor, fields)
        }
    }

    abstract class FieldFloat : Field<Float> {
        override val subFieldsEventChange = mogot.EventDispatcher()
        override val eventChange = EventDispatcher()
        override fun saveAsString(): String {
            val value = currentValue
            return "FLOAT $value"
        }

        override fun isEquals(field: Field<Float>): Boolean =
                currentValue == field.currentValue

        override fun loadFromString(value: String) {
            check(value.startsWith("FLOAT "))
            this.currentValue = value.substring(6).toFloatOrNull() ?: 0f
        }

        override val isEmpty: Boolean = false
        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.FLOAT

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field<Float>>): AbstractEditor<Float> {
            return EditorFloat(sceneEditor, fields)
        }
    }

    abstract class FieldString : Field<String> {
        override val subFieldsEventChange = mogot.EventDispatcher()
        override val eventChange = EventDispatcher()
        override fun saveAsString(): String {
            val value = currentValue
            return "STR $value"
        }

        override fun isEquals(field: Field<String>): Boolean =
                currentValue == field.currentValue

        override fun loadFromString(value: String) {
            check(value.startsWith("STR "))
            this.currentValue = value.substring(4)
        }

        override val isEmpty: Boolean = false
        override val fieldType: mogot.Field.Type
            get() = mogot.Field.Type.STRING

        override fun makeEditor(sceneEditor: SceneEditor, fields: List<Field<String>>): AbstractEditor<String> {
            return EditorString(sceneEditor, fields)
        }
    }

    fun getFields(view: SceneEditorView, node: Node): List<Field<out Any>> {
        if (node is EditableNode)
            return node.getEditableFields()
        return emptyList()
    }

    fun getClassName(node: Node): String = node::class.java.name
    fun load(view: SceneEditorView, file: VirtualFile, clazz: String, properties: Map<String, String>): Node?
    fun save(view: SceneEditorView, node: Node): Map<String, String>?
    fun selected(view: SceneEditorView, node: Node, selected: Boolean) {
        //NOP
    }

    fun getProperties(view: SceneEditorView, node: Node): List<PropertyFactory> = emptyList()
    fun isEditor(node: Node): Boolean
    fun clone(view: SceneEditorView, node: Node): Node?
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
}