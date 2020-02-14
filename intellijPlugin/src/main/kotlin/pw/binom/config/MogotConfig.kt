package pw.binom.config

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.Property
import mogot.math.*
import pw.binom.MogotStateLifecycle

@State(name = "MogotConfig", storages = [
    Storage("mogot_global.xml")
])
class MogotConfig : PersistentStateComponent<MogotConfig.State> {

    data class State(
            @Property val color2DBackground: Vector3fc = Vec3Const(0.376f, 0.376f, 0.376f),
            @Property val color2DGrid: Vector3fc = Vec3Const(1f, 1f, 1f),
            @Property val color3DGrid: Vector3fc = Vec3Const(1f, 1f, 1f)
    )

    private var myState = State(

    )

    override fun getState(): MogotConfig.State = myState

    override fun loadState(state: MogotConfig.State) {
        val prevState = myState
        myState = state

        if (prevState != myState) {
            MogotStateLifecycle.publisher.update(myState)
        }
    }
}

class Vec3Const(override val x: Float, override val y: Float, override val z: Float) : Vector3fc