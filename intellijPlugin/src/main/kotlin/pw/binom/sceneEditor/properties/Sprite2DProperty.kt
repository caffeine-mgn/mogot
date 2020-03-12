package pw.binom.sceneEditor.properties

import com.intellij.psi.PsiManager
import mogot.Node
import mogot.math.Vector2f
import mogot.math.Vector2fc
import mogot.math.set
import mogot.onlySpatial2D
import pw.binom.sceneEditor.SceneEditorView
import pw.binom.sceneEditor.loadTexture
import pw.binom.sceneEditor.nodeController.EditableSprite
import pw.binom.ui.*
import pw.binom.utils.common
import pw.binom.utils.equalsAll
import pw.binom.utils.isEmpty
import javax.imageio.ImageIO
import javax.swing.JComponent


object Sprite2DPropertyFactory : PropertyFactory {
    override fun create(view: SceneEditorView): Property = Sprite2DProperty(view)
}

class Sprite2DProperty(val view: SceneEditorView) : Property, Spoler("Sprite2D") {

    private val layout = stage.gridBagLayout()

    //    private val flex = FlexLayout(stage, FlexLayout.Direction.COLUMN)
    private val sizeTitle = PropertyName("Size").appendTo(layout, 0, 0)

    private val sizeEditor = Vector2Value().appendTo(layout, 1, 0)

    private val textureTitle = PropertyName("Texture").appendTo(layout, 0, 1)

    private val texture = TextureSelector(view.project).appendTo(layout, 1, 1)

    private fun perfectSize(): Vector2fc {
        val texFile = texture.selected?.virtualFile
        if (texFile != null) {
            val img = texFile.inputStream.use {
                ImageIO.read(it)
            }
            return Vector2f(img.width.toFloat(), img.height.toFloat())
        } else {
            return Vector2f(0f, 0f)
        }
    }

    init {
        sizeEditor.eventChange.on {
            val perfectSize = perfectSize()
            sizeTitle.resetVisible = sizeEditor.value.x != perfectSize.x || sizeEditor.value.y != perfectSize.y
        }
        sizeTitle.resetAction {
            val perfectSize = perfectSize()
            sizeEditor.value.set(perfectSize)
        }


        texture.selectedChangeEvent.on {
            textureTitle.resetVisible = texture.selected != null
        }
        textureTitle.resetAction {
            texture.selected = null
            val perfectSize = perfectSize()
            sizeTitle.resetVisible = sizeEditor.value.x != perfectSize.x || sizeEditor.value.y != perfectSize.y
        }
    }

    private var changeEventEnabled = true

    private var nodes: List<EditableSprite>? = null

    fun update() {
        val nodes = nodes?.asSequence() ?: return

        changeEventEnabled = false
        if (nodes.isEmpty) {
            sizeEditor.isEnabled = false
            texture.isEnabled = false
            return
        } else {
            sizeEditor.isEnabled = true
            texture.isEnabled = true
            sizeEditor.value.set(nodes.map { it.size }.common)
            if (nodes.map { it.texture }.equalsAll()) {
                val texture = nodes.first().texture
                this.texture.selected = if (texture == null) null else PsiManager.getInstance(view.project).findFile(texture.file)
                if (texture != null)
                    sizeTitle.resetVisible = sizeEditor.value.x != texture.width.toFloat() || sizeEditor.value.y != texture.height.toFloat()
                else
                    sizeTitle.resetVisible = sizeEditor.value.x != 0f || sizeEditor.value.y != 0f
            } else {
                texture.selected = null
                sizeTitle.resetVisible = sizeEditor.value.x != 0f || sizeEditor.value.y != 0f
            }
        }
        changeEventEnabled = true
    }

    override fun setNodes(nodes: List<Node>) {
        this.nodes = nodes.asSequence().onlySpatial2D().mapNotNull { it as? EditableSprite }.toList()
        update()
    }

    init {
        sizeEditor.eventChange.on {
            if (changeEventEnabled) {
                nodes?.asSequence()
                        ?.mapNotNull { it as? EditableSprite }
                        ?.forEach {
                            it.size.set(
                                    sizeEditor.value.x.takeUnless { it.isNaN() } ?: it.size.x,
                                    sizeEditor.value.y.takeUnless { it.isNaN() } ?: it.size.y
                            )
                        }
                view.repaint()
            }
        }

        texture.selectedChangeEvent.on {
            if (changeEventEnabled) {
                nodes?.asSequence()
                        ?.forEach {
                            it.texture = texture.selected?.virtualFile?.let { view.engine.resources.loadTexture(it) }
                        }
                view.repaint()
            }
        }
    }

    override val component: JComponent
        get() = this

    override fun close() {
    }
}