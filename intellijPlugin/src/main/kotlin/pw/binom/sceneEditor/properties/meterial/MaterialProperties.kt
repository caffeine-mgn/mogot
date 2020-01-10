package pw.binom.sceneEditor.properties.meterial

import mogot.EventDispatcher
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.MaterialInstance
import pw.binom.sceneEditor.properties.MaterialProperty
import pw.binom.sceneEditor.properties.Panel
import pw.binom.utils.equalsAllBy
import java.io.Closeable
import javax.swing.JComponent

class MaterialProperties(val property: MaterialProperty) : Panel() {
    private val flex = FlexLayout(this, FlexLayout.Direction.COLUMN)

    interface PropertyEditorFactory {
        fun create(panel: MaterialProperties, uniform: List<MaterialInstance.Uniform>): PropertyEditor?
    }

    interface PropertyEditor : Closeable {
        var value: Any?
        val component: JComponent
        val eventChange: EventDispatcher
        val uniform: List<MaterialInstance.Uniform>
    }

    private val components = ArrayList<PropertyEditor>()
    private val factories = listOf(TexturePropertyEditorFactory)
    private fun refresh() {
        components.forEach {
            flex.remove(it.component)
        }
        if (materials.isEmpty())
            return
        val uniforms = materials
                .asSequence()
                .flatMap { it.uniforms.asSequence() }
                .groupBy { it.name }
                .values
        println("Uniforms:")
        uniforms.flatten().forEach {
            println("${it.name} (${it.type.name} <- ${it.materialInstance.root.file})")
        }
        uniforms
                .asSequence()
                .mapNotNull { uniformList ->
                    factories.asSequence().mapNotNull { it.create(this, uniformList) }.firstOrNull()
                }
                .forEach { editor ->
                    components += editor
                    editor.component.appendTo(flex, grow = 0)
                    editor.eventChange.on {
                        editor.uniform.forEach {
                            it.materialInstance.set(it, editor.value)
                        }
                    }
                }
    }

    private var materials: List<MaterialInstance> = emptyList()
    fun setMaterials(materials: List<MaterialInstance>) {
        check(materials.asSequence().equalsAllBy { it.root }) { "Can't edit different materials" }
        this.materials = materials
        refresh()
    }

//    var material: MaterialInstance? = null
//        set(value) {
//            field = value
//            refresh()
//        }

    init {
        refresh()
    }
}