package pw.binom.sceneEditor.properties

import com.intellij.ide.util.AbstractTreeClassChooserDialog
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.ide.util.TreeFileChooserFactory
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.search.GlobalSearchScope
import mogot.Node
import mogot.Behaviour
import org.jetbrains.kotlin.asJava.classes.KtUltraLightClass
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.refactoring.fqName.getKotlinFqName
import org.jetbrains.kotlin.idea.search.allScope
import org.jetbrains.kotlin.load.java.JavaClassFinder
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.nj2k.inference.common.getLabel
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.resolve.jvm.KotlinJavaPsiFacade
import pw.binom.FlexLayout
import pw.binom.appendTo
import pw.binom.glsl.psi.GLSLFile
import pw.binom.sceneEditor.SceneEditorView
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

object BehaviourPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property =
            BehaviourProperty(view)
}

class BehaviourProperty(val view: SceneEditorView) : Property, Spoler("Behaviour") {
    private val flex = FlexLayout(stage, direction = FlexLayout.Direction.COLUMN)
    private val editor = JPanel().appendTo(flex, grow = 0)
    private val editorFlex = FlexLayout(editor, direction = FlexLayout.Direction.ROW)
    private val selectBtn = JButton("select").appendTo(editorFlex)
    private val clearBtn = JButton("X").appendTo(editorFlex, grow = 0)

    init {

        val facade = KotlinJavaPsiFacade.getInstance(view.editor1.project)
        fun findClass(name: String): Any? {
            val behaviourClassId = ClassId.fromString("$name")
            val behaviourRequest = JavaClassFinder.Request(behaviourClassId)
            return facade.findClass(behaviourRequest, view.project.allScope())
                    ?: JavaPsiFacade.getInstance(view.editor1.project)
                            .findClass(name, view.project.allScope())
        }

        val behaviourClass = JavaPsiFacade.getInstance(view.editor1.project)
                .findClass(Behaviour::class.java.name, view.project.allScope())
                ?: throw IllegalStateException("Can't find ${Behaviour::class.java.name}. Are you shure that you added mogot engine as dependency?")

        println("behaviourClass=${behaviourClass::class.java}")
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
//                    .createFileChooser(
//                            "Choose Behaviour Class",
//                            null,
//                            KotlinFileType.INSTANCE,
//                            {
//                                println("file->$it")
//                                true
//                            }
//                    )
            classChooser.showDialog()
            val selected = classChooser.selected as KtUltraLightClass? ?: return@addActionListener
            val getNormal = selected.allMethods
                    .filter { it.name == "getNormalSpeed" }.first()

//                    .forEach {
//                        //val returnType = (it.getReturnType() as PsiClassReferenceType).reference
//                        println("Method: ${it.name} return=${it.returnType}")
//                        it.annotations.forEach {
//                            println("owner=${it.owner} nameReferenceElement=${it.nameReferenceElement}")
//                        }
//                    }
            //(selected as KtUltraLightClass).ownMethods[1].kotlinOrigin.annotationEntries[1].
            val field = selected.allFields.filter { it.name=="normalSpeed" }
            println()
            //(selected.ownMethods[1].kotlinOrigin.annotationEntries[1].calleeExpression.typeReference.typeElement as KtUserType).referenceExpression.getReferencedNameAsName().

//            selected.allFields.forEach {
//                println("Field: ${it.name}")
//                it.annotations.forEach {
//                    println("owner=${it.owner} nameReferenceElement=${it.nameReferenceElement} parameterList=${it.parameterList} label=${it.getLabel()} getKotlinFqName=${it.getKotlinFqName()}")
//                }
//            }
//            val chooser = TreeFileChooserFactory
//                    .getInstance(view.editor1.project)
//                    .createFileChooser(
//                            "Select Material",
//                            null,
//                            null
//                    ) {
//                        it is KtFile
//                    }
//            chooser.showDialog()
//            val file = chooser.selectedFile
//            println("File: $file")
        }
    }

    override fun setNodes(nodes: List<Node>) {

    }

    override val component: JComponent
        get() = this

    override fun close() {

    }

}