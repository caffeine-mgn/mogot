package pw.binom.material.uniformEditor

import mogot.Engine
import mogot.gl.Shader
import java.awt.BorderLayout
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

object FloatEditorFactory : UniformEditor.EditorFactory {
    override fun create(uniformEditor: UniformEditor, uniform: UniformEditor.Uniform): UniformEditor.Editor? {
        if (uniform.type == "float")
            return FloatEditor(uniformEditor, uniform)
        return null
    }
}

class FloatEditor(uniformEditor: UniformEditor, uniform: UniformEditor.Uniform) : BaseEditor(uniformEditor, uniform) {
    private val spiner = JSpinner(SpinnerNumberModel(
            0.0,
            uniform.min?.toDouble() ?: -100.0,
            uniform.max?.toDouble() ?: 100.0,
            uniform.step?.toDouble() ?: 0.1
    ))

    init {
        add(spiner, BorderLayout.CENTER)
        spiner.value = data?.toFloatOrNull() ?: 0.0


        spiner.addChangeListener {
            data = spiner.value.asFloat().toString()
            dispatchChange()
        }
    }

    override fun apply(engine: Engine, shader: Shader) {
        shader.uniform(uniform.name, spiner.value.asFloat())
    }

    override val value: Any
        get() = spiner.value.asFloat()
}