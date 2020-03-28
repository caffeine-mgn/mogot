package mogot

import mogot.math.Matrix4fc
import mogot.rendering.Display
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@UseExperimental(ExperimentalContracts::class)
fun Node.isVisualInstance2D(): Boolean {
    contract {
        returns(true) implies (this@isVisualInstance2D is VisualInstance2D)
    }
    return (type and VISUAL_INSTANCE2D_TYPE) != 0
}

abstract class VisualInstance2D(engine: Engine): Spatial2D(engine) {
    var visible = true
    override val type: Int
        get() = VISUAL_INSTANCE2D_TYPE

    open fun render(model: Matrix4fc, projection: Matrix4fc, context: Display.Context) {
        //NOP
    }
}