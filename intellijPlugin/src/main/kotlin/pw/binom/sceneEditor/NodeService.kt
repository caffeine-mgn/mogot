package pw.binom.sceneEditor

import com.intellij.openapi.vfs.VirtualFile
import mogot.EventDispatcher
import mogot.Node
import mogot.collider.Collider
import mogot.collider.Collider2D
import mogot.math.*
import pw.binom.Services
import pw.binom.sceneEditor.nodeController.EmptyNodeService
import pw.binom.sceneEditor.properties.PropertyFactory

interface NodeService {

    enum class FieldType {
        VEC3, VEC2, FLOAT, STRING
    }

    interface Field<T> {
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
        val fieldType: FieldType
        val node: Node
        val eventChange: EventDispatcher
    }

    abstract class FieldVec3 : Field<Vector3fc> {
        override val eventChange = EventDispatcher()
        override fun saveAsString(): String {
            val value = currentValue
            return "VEC3 ${value.x};${value.y};${value.z}"
        }

        override fun loadFromString(value: String) {
            check(value.startsWith("VEC3 "))
            val items = value.substring(5).split(';')
            val x = items[0].toFloatOrNull() ?: 0f
            val y = items[0].toFloatOrNull() ?: 0f
            val z = items[0].toFloatOrNull() ?: 0f
            this.currentValue = Vector3f(x, y, z)
        }

        override val isEmpty: Boolean = false
        override val fieldType: FieldType
            get() = FieldType.VEC3
    }

    abstract class FieldVec2 : Field<Vector2fc> {
        override val eventChange = EventDispatcher()
        override fun saveAsString(): String {
            val value = currentValue
            return "VEC2 ${value.x};${value.y}"
        }

        override fun loadFromString(value: String) {
            check(value.startsWith("VEC2 "))
            val items = value.substring(5).split(';')
            val x = items[0].toFloatOrNull() ?: 0f
            val y = items[0].toFloatOrNull() ?: 0f
            this.currentValue = Vector2f(x, y)
        }

        override val isEmpty: Boolean = false
        override val fieldType: FieldType
            get() = FieldType.VEC2
    }

    abstract class FieldFloat : Field<Float> {
        override val eventChange = EventDispatcher()
        override fun saveAsString(): String {
            val value = currentValue
            return "FLOAT $value"
        }

        override fun loadFromString(value: String) {
            check(value.startsWith("FLOAT "))
            this.currentValue = value.substring(6).toFloatOrNull() ?: 0f
        }

        override val isEmpty: Boolean = false
        override val fieldType: FieldType
            get() = FieldType.FLOAT
    }

    abstract class FieldString : Field<String> {
        override val eventChange = EventDispatcher()
        override fun saveAsString(): String {
            val value = currentValue
            return "STR $value"
        }

        override fun loadFromString(value: String) {
            check(value.startsWith("STR "))
            this.currentValue = value.substring(4)
        }

        override val isEmpty: Boolean = false
        override val fieldType: FieldType
            get() = FieldType.STRING
    }

    fun getFields(view: SceneEditorView, node: Node): List<Field<*>> = emptyList()
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