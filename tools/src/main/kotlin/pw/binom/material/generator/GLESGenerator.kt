package pw.binom.material.generator

import pw.binom.material.DCE
import pw.binom.material.RootModule
import pw.binom.material.SourceModule
import pw.binom.material.compiler.*

abstract class GLESGenerator(val module: SourceModule) {

    init {
        GLSLFixVarPassLayout.fix(module)
    }

    protected val dce = DCE(module)

    protected val MethodDesc.glslName: String
        get() {
            if (this.parent == null)
                return this.name
            return "${parent.clazz.name}_$name"
        }

    fun isProperty(field: FieldDesc) = field.annotations.any {
        it.clazz == RootModule.propertyType
    }

    fun isExternal(field: FieldDesc) =
            field == module.model
                    || field == module.modelView
                    || field == module.vertex
                    || field == module.normal
                    || field == module.uv
                    || field == module.projection
                    || field == module.camera
}