package pw.binom.material.uniformEditor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.border.TitledBorder

private val globalUniformKey = Key<String>("UniformValues")

abstract class BaseEditor(val uniformEditor: UniformEditor, override val uniform: UniformEditor.Uniform) : UniformEditor.Editor, JBPanel<ImageEditor>(BorderLayout()) {
    override fun isEquals(uniform: UniformEditor.Uniform): Boolean =
            this.uniform == uniform

    private val _data = UniformData(uniformEditor.editor.document, globalUniformKey)

    private val uniformKey = "${uniform.type} ${uniform.name}"

    protected var data: String?
        get() = _data[uniformKey]
        set(value) {
            _data[uniformKey] = value
        }

    override val component: JComponent
        get() = this

    override fun reinit() {
        //NOP
    }

    private val listeners = ArrayList<() -> Unit>()

    override fun addChangeListener(listener: () -> Unit) {
        listeners += listener
    }

    protected fun dispatchChange() {
        listeners.forEach {
            it()
        }
    }

    init {
        border = TitledBorder("${uniform.name}  ${uniform.type}")
    }
}

internal fun Any?.asFloat() = when (this) {
    null -> 0f
    is Double -> toFloat()
    is Float -> this
    is String -> toFloatOrNull() ?: 0f
    else -> throw IllegalStateException("Can't cast $this (${this::class.java.name}) to Float")
}