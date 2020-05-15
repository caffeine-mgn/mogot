package pw.binom.material

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import mogot.*
import mogot.gl.GL
import mogot.gl.MaterialGLSL
import mogot.gl.Shader
import mogot.math.*
import mogot.rendering.Display
import pw.binom.ModuleHolder
import pw.binom.SimpleMaterial
import pw.binom.View3D
import pw.binom.material.compiler.SingleType
import pw.binom.material.compiler.TypeDesc
import pw.binom.material.generator.gles300.GLES300Generator
import pw.binom.sceneEditor.ExternalTexture
import pw.binom.sceneEditor.loadTexture
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

private class ChangeWaiter(val timeForWait: Long, val changeListener: () -> Unit) : Closeable {

    private val lastChange = AtomicLong(0)

    private var waitThread: Water? = null
    private val updated = AtomicBoolean(false)

    private inner class Water(val maxWaitTime: Long, val checkInterval: Long) : Thread() {
        override fun run() {
            while (!isInterrupted) {
                val dif = System.currentTimeMillis() - lastChange.get()
                if (dif > maxWaitTime)
                    break
                if (!updated.get() && dif > timeForWait) {
                    try {
                        updated.set(true)
                        changeListener()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                }
                sleep(checkInterval)
            }
        }
    }

    fun dispatchChange() {
        lastChange.set(System.currentTimeMillis())
        updated.set(false)
        if (waitThread == null || !waitThread!!.isAlive) {
            waitThread = Water(5000L, 300L).also {
                it.start()
            }
        }
    }

    override fun close() {
        waitThread?.interrupt()
        waitThread = null
    }
}

class MaterialViewer(val project: Project) : View3D() {

    lateinit var material: DynamicMaterialGLSL
    private val checkUpdater = ChangeWaiter(700L) {
        if (inited.get()) {
            file?.let { refresh(it) }
        }
    }

    private val listener = object : DocumentListener {
        private var bulkUpdate = false
//        override fun bulkUpdateFinished(document: Document) {
//            bulkUpdate = false
//            checkUpdater.dispatchChange()
//        }
//
//        override fun bulkUpdateStarting(document: Document) {
//            bulkUpdate = true
//        }

        override fun documentChanged(event: DocumentEvent) {
            if (!bulkUpdate) {
                checkUpdater.dispatchChange()
            }
        }
    }

    private var document: Document? = null

    var file: VirtualFile? = null
        set(value) {
            if (field == value)
                return

            document?.removeDocumentListener(listener)
            document = null
            field = value
            if (value != null) {
                document = FileDocumentManager.getInstance().getDocument(value)!!
                document!!.addDocumentListener(listener)
                checkUpdater.dispatchChange()
            }
        }

    private var vp = ""
    private var fp = ""
    private var vpOld = ""
    private var fpOld = ""

    private val params = HashMap<String, Any>()

    private val activeTextures = ArrayList<ExternalTexture>()
    private val textureId = HashMap<ExternalTexture, Int>()

    var ExternalTexture.id: Int
        get() = textureId[this] ?: -1
        set(value) {
            textureId[this] = value
        }

    private fun updateTextureIndexes() {
        activeTextures.forEachIndexed { index, textureFile ->
            textureFile.id = index
        }
    }

    fun set(name: String, value: Any?) {
        val oldValue = params.remove(name)
        if (oldValue is ExternalTexture) {
            activeTextures.remove(oldValue)
            textureId.remove(oldValue)
            oldValue.dec()
        }
        if (value is ExternalTexture) {
            activeTextures.add(value)
            value.inc()
        }
        if (value != null)
            params[name] = value
        updateTextureIndexes()
    }


    inner class DynamicMaterialGLSL(gl: GL) : MaterialGLSL(gl) {
        private val standart = SimpleMaterial(gl)

        init {
            standart.inc()
        }

        var dynamicShader: Shader? = null
        override val shader: Shader
            get() = dynamicShader ?: standart.shader

        override fun dispose() {
            standart.dec()
            dynamicShader?.close()
            super.dispose()
        }

        override fun use(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
            super.use(model, modelView, projection, context)
            params.forEach { (name, value) ->
                val v = when (value) {
                    is Int -> shader.uniform(name, value)
                    is Float -> shader.uniform(name, value)
                    is Vector3fc -> shader.uniform(name, value)
                    is Vector3ic -> shader.uniform(name, value)
                    is Vector4fc -> shader.uniform(name, value)
                    is ExternalTexture -> {
                        gl.activeTexture(gl.TEXTURE0 + value.id)
                        gl.bindTexture(gl.TEXTURE_2D, value.gl.gl)
                        shader.uniform(name, value.id)
                    }
                    else -> throw IllegalStateException("Unknown uniform type ${value::class.java.name}")
                }
            }
        }
    }

//    private val fpPrefix = """#version 300 es
//
//#ifdef GL_ES
//precision mediump float;
//#endif
//"""

    override fun render() {
        if (vp != vpOld || fp != fpOld) {
            vpOld = vp
            fpOld = fp
            try {
                println("fp:\n$fp")
                material.dynamicShader?.close()
                material.dynamicShader = Shader(gl, vp, fp)
            } catch (e: Throwable) {
                println("MaterialView ERROR: ${e.message}")
                println("fp:")
                fp.lines().forEachIndexed { index, s ->
                    println("$index -> $s")
                }
                e.printStackTrace()
                material.dynamicShader = null
            }
        }
        super.render()
    }


    fun setShader(vp: String, fp: String) {
        this.vp = vp
        this.fp = fp
        repaint()
    }

    private val initListeners = ArrayList<() -> Unit>()

    fun addInitListener(func: () -> Unit) {
        initListeners += func
    }

    private val renderListeners = ArrayList<() -> Unit>()

    fun addRenderListener(func: () -> Unit) {
        renderListeners += func
    }

    private val inited = AtomicBoolean(false)

    override fun init() {
        super.init()
        initListeners.forEach {
            it()
        }
        material = DynamicMaterialGLSL(engine.gl)
        println("Main Material: $material")
        val node = GeomNode2().also {
//            it.geom.value = Geoms.buildCube2(gl, 1f)
            it.geom.value = Geoms.solidSphere(gl, 1f, 30, 30)
            it.material.value = material
        }
        root!!.addChild(node)

        /*
        val node1 = GeomNode().apply {
            geom.value = Geoms.solidSphere(gl, 1f, 30, 30)
            material.value = SimpleMaterial(engine.gl)
        }

        node1.position.set(-6f,6f,6f)
        root!!.addChild(node1)
        */

        val l = PointLight(engine)
        l.position.set(-6f, 6f, 6f)
        root!!.addChild(l)

        resetCam()
        repaint()
        inited.set(true)
        file?.let { refresh(it) }
    }

    override fun dispose() {
        activeTextures.forEach {
            it.dec()
        }
        activeTextures.clear()
        textureId.clear()
        params.clear()
        fpOld = ""
        vpOld = ""
        material.dynamicShader?.close()
        root!!.childs.toTypedArray().forEach {
            if (it !is Camera)
                it.parent = null
        }
    }

    fun refresh(file: VirtualFile) {
        fun setUniform(type: TypeDesc, name: String, value: String?) {
            if (type is SingleType && type.clazz.name == "sampler2D") {
                val texture = value
                        ?.let {
                            file.parent.findFileByRelativePath(value)
                                    ?.let { engine.resources.loadTexture(it) }
                        }
                set(name, texture)
            }

            if (type is SingleType && type.clazz.name == "float") {
                set(name, value?.removeSuffix("f")?.toFloat())
            }

            if (type is SingleType && type.clazz.name == "int") {
                set(name, value?.toFloat())
            }

            if (type is SingleType && type.clazz.name == "bool") {
                set(name, value == "true")
            }

            if (type is SingleType && type.clazz.name == "vec3") {
                val vector = value?.let {
                    it.split(',').map { it.trim().toFloat() }.let {
                        Vector3f(it.getOrNull(0) ?: 0f, it.getOrNull(1) ?: 0f, it.getOrNull(1) ?: 0f)
                    }
                }
                set(name, vector)
            }

            if (type is SingleType && type.clazz.name == "vec4") {
                val vector = value?.let {
                    it.split(',').map { it.trim().toFloat() }.let {
                        Vector4f(
                                it.getOrNull(0) ?: 0f,
                                it.getOrNull(1) ?: 0f,
                                it.getOrNull(2) ?: 0f,
                                it.getOrNull(3) ?: 0f
                        )
                    }
                }
                set(name, vector)
            }
        }

        try {
            val holder = ModuleHolder.getInstance(file, project)
            val module = holder.resolver.getModule(file).checkValid()
            val gen = GLES300Generator.mix(listOf(module))
            setShader(gen.vp, gen.fp)
            module.properties.asSequence()
                    .mapNotNull {
                        val prop = it.property ?: return@mapNotNull null
                        it to prop.value
                    }
                    .forEach {
                        setUniform(it.first.type, it.first.name, it.second)
                    }
            println("OK")
        } catch (e: Throwable) {
            material.dynamicShader = null
            println("ERROR: ${e.message}")
            e.printStackTrace()
        }
    }

    private inner class GeomNode2 : VisualInstance(), MaterialNode by MaterialNodeImpl() {
        var geom = ResourceHolder<Geom3D2>()
        override fun render(model: Matrix4fc, modelView: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
            material.value?.use(model, modelView, projection, context)
            renderListeners.forEach {
                it()
            }
            geom.value?.draw()
            material.value?.unuse()
        }

        override fun close() {
            geom.dispose()
            material.dispose()
        }
    }
}
