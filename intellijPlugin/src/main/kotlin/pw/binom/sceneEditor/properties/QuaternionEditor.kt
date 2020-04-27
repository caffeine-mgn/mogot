package pw.binom.sceneEditor.properties

import mogot.math.*
import mogot.math.set
import pw.binom.sceneEditor.NodeService
import pw.binom.sceneEditor.SceneEditor
import pw.binom.ui.*
import pw.binom.utils.Vector3mDegrees
import pw.binom.utils.common

class QuaternionEditor(sceneEditor: SceneEditor, fields: List<NodeService.Field>) : AbstractEditor(sceneEditor, fields) {

    private val layout = gridBagLayout()
    private val title = PropertyName(fields.first().displayName).appendTo(layout, 0, 0)
    private val editor = Vector3Value().appendTo(layout, 1, 0)

    private var enableEvents = true
    private var enableEvents2 = true

    private fun refreshValues() {
        enableEvents = false
        editor.value.set(fields.asSequence().map { it.currentValue as Quaternionfc }.map { Vector3mDegrees(RotationVector(Quaternionf(it))) }.common)
        enableEvents = true
    }

    init {
        fields.forEach {
            closable += it.eventChange.on {
                if (enableEvents2)
                    refreshValues()
            }
            Unit
        }
        editor.eventChange.on {
            enableEvents2 = false
            val x = toRadians(editor.value.x)
            val y = toRadians(editor.value.y)
            val z = toRadians(editor.value.z)
            if (enableEvents && (!x.isNaN() || !y.isNaN() || !z.isNaN())) {

                if (!x.isNaN() && !y.isNaN() && !z.isNaN()) {
                    val v = Quaternionf()
                    v.setRotation(z, y, x)
                    println("v=$v")
                    fields.forEach {
                        it.clearTempValue()
                        it.currentValue = v
                    }

                } else {
                    fields.forEach {
                        val currentValue = it.currentValue as Quaternionfc
                        val q = Quaternionf()
                        q.setRotation(
                                z.takeIf { !it.isNaN() } ?: currentValue.yaw,
                                y.takeIf { !it.isNaN() } ?: currentValue.pitch,
                                x.takeIf { !it.isNaN() } ?: currentValue.roll
                        )
                        it.currentValue = q
                    }
                }
            }
            enableEvents2 = true
        }
        refreshValues()
    }
}