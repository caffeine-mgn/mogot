package pw.binom.sceneEditor.properties

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifierListOwner
import mogot.Behaviour
import mogot.MaterialNode
import mogot.Node
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClass
import org.jetbrains.kotlin.idea.quickfix.createFromUsage.callableBuilder.setupEditorSelection
import org.jetbrains.kotlin.idea.search.allScope
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.io.Closeable
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.properties.behaviour.BehaviourProperties
import pw.binom.sceneEditor.properties.meterial.MaterialProperties
import pw.binom.utils.equalsAllBy
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

object BehaviourPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property =
            BehaviourProperty(view)
}

class BehaviourManager : Closeable {
    val values = HashMap<Node, BehaviourValue>()
    fun getOrCreate(node: Node) = values.getOrPut(node) { BehaviourValue() }
    fun get(node: Node) = values[node]

    fun delete(node: Node) {
        values.remove(node)
    }

    override fun close() {

    }
}

val mogot.Engine.behaviourManager: BehaviourManager
    get() = manager("BehaviourManager") { BehaviourManager() }

class BehaviourValue {
    var className: String? = null
        set(value) {
            field = value
            if (value == null)
                properties.clear()
        }
    val properties = HashMap<String, String>()

    fun clone(): BehaviourValue {
        val v = BehaviourValue()
        v.className = className
        v.properties.clear()
        v.properties.putAll(properties)
        return v
    }
}

class BehaviourProperty(val view: SceneEditorView) : Property, Spoler("Behaviour") {
    private val flex = FlexLayout(stage, direction = FlexLayout.Direction.COLUMN)
    private val editor = JPanel().appendTo(flex, grow = 0)
    private val editorFlex = FlexLayout(editor, direction = FlexLayout.Direction.ROW)
    private val selectBtn = JButton("select").appendTo(editorFlex)
    private val clearBtn = JButton("X").appendTo(editorFlex, grow = 0)

    init {
        val behaviourClass = JavaPsiFacade.getInstance(view.editor1.project)
                .findClass(Behaviour::class.java.name, view.project.allScope())
                ?: throw IllegalStateException("Can't find ${Behaviour::class.java.name}. Are you shure that you added mogot engine as dependency?")

        selectBtn.addActionListener {
            val classChooser = TreeClassChooserFactory
                    .getInstance(view.editor1.project)
                    .createInheritanceClassChooser(
                            "Choose Behaviour Class",
                            view.project.allScope(),
                            behaviourClass, null
                    ) {
                        println("class ${it::class.java}")
                        true
                    }
            classChooser.showDialog()
            val selected = classChooser.selected// as KtUltraLightClass? ?: return@addActionListener
            if (selected != null)
                setClass(selected)
        }

        clearBtn.addActionListener {
            setClass(null)
        }
    }

    private var behaviourProperties = BehaviourProperties(this).appendTo(flex, grow = 0)
    @JvmField
    var nodes: List<Node> = emptyList()

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes
        nodes
                .takeIf { it.isNotEmpty() }
                ?.asSequence()
                ?.map { view.engine.behaviourManager.getOrCreate(it) }
                ?.forEach {
                    println("ClassName: ${it.className}")
                }

        val bv = nodes
                .takeIf { it.isNotEmpty() }
                ?.asSequence()
                ?.map { view.engine.behaviourManager.getOrCreate(it) }
                ?.takeIf { it.equalsAllBy { it.className } }
                ?.firstOrNull()

        val bName = bv?.className
        clazz = null
        if (bName != null) {
            clazz = JavaPsiFacade.getInstance(view.editor1.project)
                    .findClass(bName, view.project.allScope())
        }

        refreshInstances()
    }

    var clazz: PsiClass? = null
        private set
    private var properties = HashMap<String, String>()

    private fun setClass(clazz: PsiClass?) {
        this.clazz = clazz
        nodes.forEach {
            view.engine.behaviourManager.getOrCreate(it).className = clazz?.qualifiedName
        }
        refreshInstances()
    }

    private fun refreshInstances() {
        if (clazz == null)
            properties.clear()
        println("refreshInstances!")
        selectBtn.text = clazz?.name ?: "none"
        behaviourProperties.refresh()
    }

    override val component: JComponent
        get() = this

    override fun close() {

    }

}

inline val PsiModifierListOwner.allAnnatation: Array<PsiAnnotation>
    get() = AnnotationUtil.getAllAnnotations(this, true, HashSet())

inline fun PsiModifierListOwner.getAnnotation(clazz: Class<*>) =
        allAnnatation.find { it.qualifiedName == clazz.name }

inline fun PsiAnnotation.string(name: String) =
        AnnotationUtil.getStringAttributeValue(this, name)

inline fun PsiAnnotation.boolean(name: String) = AnnotationUtil.getBooleanAttributeValue(this, name)
fun PsiAnnotation.float(name: String): Float? {
    val attrValue = this.findAttributeValue(name)
    val constValue = JavaPsiFacade.getInstance(this.project).constantEvaluationHelper.computeConstantExpression(attrValue)
    return if (constValue is Float) constValue else null
}