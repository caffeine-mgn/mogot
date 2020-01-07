package pw.binom.sceneEditor.properties.behaviour

import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.PsiClassReferenceType
import mogot.EventDispatcher
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClass
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.sceneEditor.properties.*
import pw.binom.sceneEditor.properties.meterial.MaterialProperties
import pw.binom.utils.equalsAll
import pw.binom.utils.equalsAllBy
import java.io.Closeable
import javax.swing.JComponent
import mogot.math.*

class BehaviourProperties(val property: BehaviourProperty) : Panel() {
    interface PropertyEditor : Closeable {
        var value: String?
        val component: JComponent
        val eventChange: EventDispatcher
        val property: Property
    }

    class Property(
            val display: String,
            val minFloat: Float?,
            val maxFloat: Float?,
            val isColor: Boolean,
            val readOnly: Boolean,
            val type: String,
            val name: String
    )

    private val flex = FlexLayout(this, FlexLayout.Direction.COLUMN)
    private val components = ArrayList<PropertyEditor>()
    private var enableDispatchChange = true

    private fun changeEvent(propertyEditor: PropertyEditor) {
        println("update enableDispatchChange=$enableDispatchChange")
        if (!enableDispatchChange)
            return
        property.nodes.asSequence().forEach {
            val value = propertyEditor.value
            println("Value: \"$value\"")
            if (value == null)
                property.view.engine.behaviourManager.getOrCreate(it).properties.remove(propertyEditor.property.name)
            else
                property.view.engine.behaviourManager.getOrCreate(it).properties[propertyEditor.property.name] = value
        }
    }

    fun refresh() {
        val clazz = property.clazz
        if (clazz == null) {
            println("class is null")
            components.clear()
            removeAll()
            return
        }
        println("clazz: ${clazz::class.java.name}")
        println("Annatations of ${clazz.qualifiedName}:")
        println("Methods:")
        clazz.allMethods.forEach { method ->
            method.allAnnatation.forEach {
                println("${method.name} it.qualifiedName->${it.qualifiedName}")
            }
        }
        println("Fields:")
        clazz.allFields.forEach { field ->
            field.allAnnatation.forEach {
                println("${field.name} it.qualifiedName->${it.qualifiedName}")
            }
        }

        val properties = clazz.allMethods
                .asSequence()
                .filter { it.getAnnotation(mogot.annotations.Property::class.java) != null }
                .filter { it.hasModifierProperty("public") }
                .map {
                    val name = it.name.toPropertyName()
                    val readOnly = clazz.allMethods.any { m -> m.name == "set${it.name.removePrefix("get")}" && it.hasModifierProperty("public") }
                    val property = it.getAnnotation(mogot.annotations.Property::class.java)!!
                    val display = property.string("display") ?: name.toHumanReadbleName()
                    val min = it.getAnnotation(mogot.annotations.RestrictionMin::class.java)?.float("value")
                    val max = it.getAnnotation(mogot.annotations.RestrictionMax::class.java)?.float("value")
                    val isColor = it.getAnnotation(mogot.annotations.ColorProperty::class.java) != null
                    val returnTypeElement = it.returnTypeElement
                    val returnType = it.returnType
                    val type = when (returnType) {
                        PsiType.FLOAT -> "float"
                        PsiType.INT -> "int"
                        PsiType.BOOLEAN -> "boolean"
                        is PsiClassReferenceType -> returnType.reference.qualifiedName
                        else -> null
                    } ?: return@map null
                    Property(
                            display = display,
                            minFloat = min,
                            maxFloat = max,
                            readOnly = readOnly,
                            isColor = isColor,
                            type = type,
                            name = name
                    )
                }.filterNotNull().toList()

        removeAll()
        components.clear()

        properties.mapNotNull<Property, BehaviourProperties.PropertyEditor> {
            when (it.type) {
                "float" -> FloatEditor(it)
                Vector3f::class.java.name,
                Vector3fc::class.java.name,
                Vector3fm::class.java.name -> Vector3Editor(it)
                else -> {
                    println("Unknown property \"${it.type}\"")
                    return@mapNotNull null
                }
            }
        }.forEach {
            it.component.appendTo(flex)
            it.eventChange.on {
                changeEvent(it)
            }
            it.component.isEnabled = !it.property.readOnly
            components += it
        }

        if (property.nodes.isNotEmpty())
            components.forEach { component ->
                enableDispatchChange = false
                val vec = property.nodes.asSequence().map {
                    property.view.engine.behaviourManager.getOrCreate(it).properties[component.property.name]
                }
                component.value = if (vec.equalsAll())
                    vec.firstOrNull()
                else
                    null
                enableDispatchChange = true
            }
    }
}

private fun String.toHumanReadbleName(): String {
    val sb = StringBuilder()
    var first = true
    forEach {
        if (first) {
            sb.append(it.toUpperCase())
            first = false
            return@forEach
        }
        if (it.isUpperCase())
            sb.append(" ")
        sb.append(it)
    }
    return sb.toString()
}

private fun String.toPropertyName(): String {
    val sb = StringBuilder()
    var first = true
    val name = removePrefix("get")
    name.forEach {
        if (first) {
            sb.append(it.toLowerCase())
            first = false
            return@forEach
        }
        sb.append(it)
    }
    return sb.toString()
}