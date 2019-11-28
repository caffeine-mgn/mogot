package pw.binom.glsl.uniformEditor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.border.TitledBorder

private val globalUniformKey = Key<String>("UniformValues")

abstract class BaseEditor(val document: Document, val uniform: UniformEditor.Uniform) : UniformEditor.Editor, JBPanel<ImageEditor>(BorderLayout()) {
    override fun isEquals(uniform: UniformEditor.Uniform): Boolean =
            this.uniform == uniform

    private val _data = UniformData(document, globalUniformKey)

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

    init {
        border = TitledBorder("${uniform.name}  ${uniform.type}")
    }
}

internal fun Any?.asFloat() = when (this) {
    null -> 0f
    is Double -> toFloat()
    is String -> toFloatOrNull() ?: 0f
    else -> throw IllegalStateException()
}