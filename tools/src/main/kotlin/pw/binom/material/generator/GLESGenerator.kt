package pw.binom.material.generator

import pw.binom.material.DCE
import pw.binom.material.compiler.*

abstract class GLESGenerator(val compiler: Compiler) {

    protected val dce = DCE(compiler)

    protected val MethodDesc.glslName: String
        get() {
            if (this.parent == null)
                return this.name
            return "${parent.clazz.name}_$name"
        }

}