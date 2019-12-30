package mogot.gl

import com.jogamp.opengl.GLAutoDrawable
import com.jogamp.opengl.GLCapabilities
import com.jogamp.opengl.GLEventListener
import com.jogamp.opengl.awt.GLCanvas
import com.jogamp.opengl.util.FPSAnimator
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JLayeredPane
import javax.swing.JPanel

open class OpenGLPanel(profile: GLCapabilities) : JPanel() {
    val glcanvas = GLCanvas(profile)
    val animator = FPSAnimator(glcanvas, 60)

    init {
        layout = BorderLayout()
        add(glcanvas, BorderLayout.CENTER)
//        add(glcanvas)
        glcanvas.background = Color.red
        background = Color.blue

        glcanvas.addGLEventListener(object : GLEventListener {
            override fun reshape(drawable: GLAutoDrawable?, x: Int, y: Int, width: Int, height: Int) {
                println("reshape")
            }

            override fun display(drawable: GLAutoDrawable?) {
            }

            override fun init(drawable: GLAutoDrawable?) {
                println("init")
            }

            override fun dispose(drawable: GLAutoDrawable?) {
                println("dispose")
            }

        })
        animator.start()
    }

    override fun addNotify() {
        println("OpenGLPanel.addNotify")
        //glcanvas.addNotify()
        super.addNotify()
    }

    override fun removeNotify() {
        println("OpenGLPanel.removeNotify")
        super.removeNotify()
    }

    fun addGLEventListener(listener: GLEventListener) {
        glcanvas.addGLEventListener(listener)
    }

    fun display() {
        glcanvas.display()
    }

    fun destroy() {
        glcanvas.destroy()
    }

    fun swapBuffers() {
        glcanvas.swapBuffers()
    }

    fun startRender() {
        animator.start()
    }

    fun stopRender() {
        animator.pause()
    }


}