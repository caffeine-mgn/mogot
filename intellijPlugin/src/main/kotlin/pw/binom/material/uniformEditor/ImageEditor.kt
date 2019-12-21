package pw.binom.material.uniformEditor

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import mogot.Engine
import mogot.Texture2D
import mogot.gl.Shader
import pw.binom.material.MaterialViewer
import java.awt.BorderLayout
import java.io.File
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.Document

object ImageEditorFactory : UniformEditor.EditorFactory {
    override fun create(uniformEditor: UniformEditor, uniform: UniformEditor.Uniform): UniformEditor.Editor? {
        if (uniform.type == "sampler2D")
            return ImageEditor(uniformEditor, uniform)
        return null
    }

}

class ImageEditor(uniformEditor: UniformEditor, uniform: UniformEditor.Uniform) : BaseEditor(uniformEditor, uniform) {
//    val btn = JButton("no image")


    private var file: File? = null
    private var oldFile: File? = null
    private var oldFileTime: Long? = null
    private var texture: Texture2D? = null


    override fun apply(engine: Engine, shader: Shader) {
        val gl = uniformEditor.editor.materialViewer.engine.gl
        if (file != null) {
            if (oldFile?.path != file!!.path || file!!.lastModified() != oldFileTime || texture == null) {
                oldFile = file
                oldFileTime = file!!.lastModified()
                texture?.dec()
                val tex = engine.resources.syncCreateTexture2D(file!!.path)
                tex.inc()
                texture = tex
            } else {
                if (texture != null)
                    gl.bindTexture(gl.TEXTURE_2D, texture!!.gl)
            }
        } else {
            texture?.dec()
            gl.activeTexture(gl.TEXTURE0)
            gl.bindTexture(gl.TEXTURE_2D, null)
            shader.uniform(uniform.name, 0)
            texture = null
        }


    }

    override var value: Any? = null


    override fun reinit() {
        texture = null
        oldFile = null
    }

    private fun setFile(file: File?) {
        if (file != null) {
            this.file = File(file.path)
        }
//        btn.text = this.file?.path ?: "no image"
        data = file?.path
        //value = file?.let { MaterialViewer.TextureFile(it) }
        dispatchChange()
    }

    val btn2 = TextFieldWithBrowseButton()

    init {

        add(btn2, BorderLayout.CENTER)
//        add(btn, BorderLayout.WEST)


        data?.let {
            btn2.textField.text = it
            val file = File(it).takeIf { it.isFile }
            setFile(file)
        }

        btn2.textField.document.addChangeListener {
            val file = File(btn2.textField.text).takeIf { it.isFile }
            setFile(file)
            println("Set $file")
            dispatchChange()
        }

        btn2.addBrowseFolderListener(null, null, uniformEditor.editor.project, FileChooserDescriptor(
                true,
                false,
                false,
                false,
                false,
                false
        ).withFileFilter { it.extension?.toLowerCase() == "png" })
        /*
        btn.addActionListener {
            val file = FileChooser.chooseFiles(
                    FileChooserDescriptor(
                            true,
                            false,
                            false,
                            false,
                            false,
                            false
                    ).withFileFilter { it.extension?.toLowerCase() == "png" },
                    uniformEditor.editor.project,
                    null
            ).let { it.firstOrNull() }
            setFile(file?.let { File(it.path) })
        }
        */
    }
}

fun Document.addChangeListener(func: () -> Unit) {
    addDocumentListener(object : DocumentListener {
        override fun changedUpdate(e: DocumentEvent?) {
            func()
        }

        override fun insertUpdate(e: DocumentEvent?) {
            func()
        }

        override fun removeUpdate(e: DocumentEvent?) {
            func()
        }
    })
}