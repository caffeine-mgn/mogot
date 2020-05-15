package game.game

import mogot.*
import mogot.gl.GLView
import mogot.math.MATRIX4_ONE
import mogot.math.Matrix4f
import mogot.math.Vector2f
import mogot.math.Vector3f
import pw.binom.ComponentListenerImpl
import pw.binom.MockFileSystem
import pw.binom.sceneEditor.*
import pw.binom.ui.AnimateFrameView
import pw.binom.ui.AnimatePropertyView
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.DisplayMode
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.util.*
import javax.swing.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin


/*
class DDShaderMaterial(gl: GL, vp: String, fp: String) : MaterialGLSL(gl) {
    override val shader: Shader = Shader(gl, vp, fp)

    val diffuseColor = Vector4f(1f, 1f, 1f, 1f)

    override fun close() {
        shader.close()
    }

    override fun use(model: Matrix4fc, projection: Matrix4fc, renderContext: RenderContext) {
        super.use(model, projection, renderContext)
        shader.uniform("diffuseColor", diffuseColor.x, diffuseColor.y, diffuseColor.z, diffuseColor.w)
    }
}
*/

class FFF : Behaviour() {
    var r = 0f
    override fun onUpdate(delta: Float) {
        val node = node as? Spatial ?: return
        r += delta
        node.position.x = sin(r) * 3f
        node.position.z = cos(r) * 3f
        super.onUpdate(delta)
    }
}

class DDD : GLView(mogot.rendering.Display(listOf(mogot.rendering.SceneToTextureRenderPass(), mogot.rendering.CanvasFinalRenderPass())), MockFileSystem()) {

    private var closed = false
    private var inited = false


    override fun init() {
        super.init()
        root = Spatial()
        display.context.camera = Camera(engine)
        backgroundColor.set(0.5f, 0.5f, 0.5f, 1f)

        val gg = Grid3D(engine.gl)
        gg.material.value = Default3DMaterial(engine.gl)
        gg.parent = root
        inited = true
        camera!!.parent = root
        camera!!.position.set(5f, 5f, 5f)
        //cam.lookTo(Vector3f(6f, 6f, 6f))
        camera!!.behaviour = FpsCamB(engine)
        val box = CSGBox(engine)
        box.material.value = SimpleMaterial(engine.gl)
        box.parent = root
        camera!!.lookTo(Vector3f(0f, 0f, 0f))
    }

    override fun dispose() {
        closed = true
        super.dispose()
    }
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
//        val view3d = FbxViewer(File("C:\\Users\\User\\IdeaProjects\\test2\\src\\main\\resources\\untitled.fbx").inputStream().readAllBytes())
        val view = DDD()
        val f = JFrame()

//        val view =FileInputStream(File("C:\\Users\\User\\IdeaProjects\\test2\\src\\assets\\box.fbx")).use {
//            FbxViewer(it.readBytes())
//        }
//        f.contentPane.add(view.glcanvas)
        val gTop = GuideLine(GuideLine.Place.TOP)
        val gLeft = GuideLine(GuideLine.Place.LEFT)
        f.contentPane.add(gTop, BorderLayout.NORTH)
        f.contentPane.add(gLeft, BorderLayout.WEST)
        val p = JPanel().also { it.background = Color.GRAY }
        var mx = 0
        var my = 0

        p.addMouseMotionListener(object : MouseMotionListener {
            override fun mouseMoved(e: MouseEvent) {
                mx = e.x
                my = e.y
            }

            override fun mouseDragged(e: MouseEvent) {
                val dx = e.x - mx
                val dy = e.y - my
                mx = e.x
                my = e.y
                gTop.position += dx / gTop.scale
                gLeft.position += dy / gLeft.scale
            }
        })
        p.addMouseWheelListener {
            //            gTop.scale -= it.wheelRotation / 5f
//            gLeft.scale -= it.wheelRotation / 5f
            println("g.scale=${gTop.scale}")
        }
        f.contentPane.add(p, BorderLayout.CENTER)

        f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        f.size = Dimension(800, 600)
//        f.add(view3d)
//        view.startRender()
        f.isVisible = true
        view.startRender()
    }
}


class FrameLine : AnimateFrameView.FrameLine {
    inner class FrameImpl(override val color: Color, time: Int) : AnimateFrameView.Frame {
        override var time: Int = time
            set(value) {
                if (value == field)
                    return
                frames.remove(field)
                field = value
                frames[value] = this
            }
    }

    fun add(color: Color, time: Int) {
        frames[time] = FrameImpl(color, time)
    }

    val frames = TreeMap<Int, AnimateFrameView.Frame>()
    override fun iterator(): Iterator<AnimateFrameView.Frame> {
        return frames.values.iterator()
    }

    override fun frame(time: Int): AnimateFrameView.Frame? = frames[time]

    override fun floorFrame(time: Int): AnimateFrameView.Frame? = frames.floorEntry(time)?.value

    override fun ceilingFrame(time: Int): AnimateFrameView.Frame? = frames.ceilingEntry(time)?.value

    override fun remove(frame: AnimateFrameView.Frame) {
        frames.remove(frame.time)
    }

    override fun remove(time: Int) {
        TODO("Not yet implemented")
    }
}

class AnimateModel : AnimateFrameView.Model {
    val frameLines = ArrayList<AnimateFrameView.FrameLine>()
    override val frameCount: Int
        get() = 50
    override val frameInSeconds: Int
        get() = 24
    override val lineCount: Int
        get() = frameLines.size

    override fun line(index: Int): AnimateFrameView.FrameLine = frameLines[index]
}

class AnimateNode(override val icon: Icon?, override val text: String, override val properties: List<AnimatePropertyView.Property>) : AnimatePropertyView.Node {
    override var lock: Boolean = false
    override var visible: Boolean = false
    override fun remove(property: AnimatePropertyView.Property) {
        TODO("Not yet implemented")
    }
}

class AnimateProperty(override val text: String) : AnimatePropertyView.Property {
    override var lock: Boolean = false
}

class PropertyModel(override val nodes: List<AnimatePropertyView.Node>) : AnimatePropertyView.Model {
    override fun remove(node: AnimatePropertyView.Node) {
        TODO("Not yet implemented")
    }
}

object Main2 {

    @JvmStatic
    fun main(args: Array<String>) {
        val f = JFrame()
        f.setSize(800, 600)

        f.add(DDD())
        f.isVisible = true
    }

    @JvmStatic
    fun main1(args: Array<String>) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        val propertiesModel = PropertyModel(listOf(
                AnimateNode(null, "AnimateSprite", listOf(
                        AnimateProperty("Position"),
                        AnimateProperty("Rotation")
                ))
        ))

        val model = AnimateModel()
        model.frameLines.add(FrameLine().also {
            it.add(Color.YELLOW, 0)
            it.add(Color.YELLOW, 10)
            it.add(Color.GREEN, 20)
            it.add(Color.GREEN, 25)
        })

        model.frameLines.add(FrameLine().also {
            it.add(Color.BLUE, 0)
            it.add(Color.BLUE, 15)
            it.add(Color.GREEN, 21)
            it.add(Color.BLUE, 30)
        })

        (0 until 10).forEach {
            model.frameLines.add(FrameLine())
        }

        model.frameLines.add(FrameLine().also {
            it.add(Color.BLUE, 0)
            it.add(Color.BLUE, 15)
            it.add(Color.GREEN, 21)
            it.add(Color.BLUE, 30)
        })

        val f = JFrame()
//        val hScroll = JScrollBar(JScrollBar.HORIZONTAL)
//        val vScroll = JScrollBar()


        f.size = Dimension(1000, 200)
        f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE


        val pan = JPanel()


        val propertyView = AnimatePropertyView()
        propertyView.model = propertiesModel

        val view = AnimateFrameView()
        view.model = model
        pan.layout = BorderLayout()
        val scrollPanel = JScrollPane(view)
        pan.add(scrollPanel, BorderLayout.CENTER)
//        pan.add(vScroll, BorderLayout.EAST)
//        pan.add(hScroll, BorderLayout.SOUTH)
        val split = JSplitPane()
        split.leftComponent = propertyView
        split.rightComponent = pan
        split.resizeWeight = 0.2
        f.add(split)

//        hScroll.addAdjustmentListener {
//            view.scrollX = maxOf(0, hScroll.value)
//        }
        scrollPanel.verticalScrollBar.addAdjustmentListener {
            //view.scrollY = maxOf(0, scrollPanel.verticalScrollBar.value)
            propertyView.scrollY = maxOf(0, scrollPanel.verticalScrollBar.value)
        }
        f.isVisible = true
    }
}