package pw.binom.sceneEditor

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.JPanel
import javax.swing.SpringLayout

class ViewPlane(val editor1: SceneEditor, val project: Project, val file: VirtualFile, fps: Int?) : JPanel() {
    private val l = SpringLayout()
    val guideTop = GuideLine(GuideLine.Place.TOP)
    val guideLeft = GuideLine(GuideLine.Place.LEFT)
    val view = SceneEditorView(this, editor1, project, file, fps)

    init {
        this.layout = l
        add(guideTop)
        add(guideLeft)
        add(view)

        l.putConstraint(SpringLayout.NORTH, guideTop, 0, SpringLayout.NORTH, this)
        l.putConstraint(SpringLayout.WEST, guideTop, guideTop.lineHight, SpringLayout.WEST, this)
        l.putConstraint(SpringLayout.EAST, guideTop, 0, SpringLayout.EAST, this)

        l.putConstraint(SpringLayout.NORTH, guideLeft, guideTop.lineHight, SpringLayout.NORTH, this)
        l.putConstraint(SpringLayout.WEST, guideLeft, 0, SpringLayout.WEST, this)
        l.putConstraint(SpringLayout.SOUTH, guideLeft, 0, SpringLayout.SOUTH, this)

        l.putConstraint(SpringLayout.SOUTH, view, 0, SpringLayout.SOUTH, this)
        l.putConstraint(SpringLayout.EAST, view, 0, SpringLayout.EAST, this)
    }

    var guideVisible = false
        set(value) {
            field = value
            if (value) {
//                l.putConstraint(SpringLayout.NORTH, view, guideTop.lineHight, SpringLayout.NORTH, this)
//                l.putConstraint(SpringLayout.WEST, view, guideTop.lineHight, SpringLayout.WEST, this)
                l.putConstraint(SpringLayout.NORTH, view, 0, SpringLayout.SOUTH, guideTop)
                l.putConstraint(SpringLayout.WEST, view, 0, SpringLayout.EAST, guideLeft)
                guideTop.isVisible = true
                guideLeft.isVisible = true
            } else {
                l.putConstraint(SpringLayout.NORTH, view, 0, SpringLayout.NORTH, this)
                l.putConstraint(SpringLayout.WEST, view, 0, SpringLayout.WEST, this)
                guideTop.isVisible = false
                guideLeft.isVisible = false
            }
            revalidate()
            println("guideVisible=$value")
        }

    init {
        guideVisible = false
    }
}