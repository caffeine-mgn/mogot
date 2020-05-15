package pw.binom.material.compiler

import pw.binom.material.SourcePoint

class AnnotationDesc(
        val clazz: SingleType,
        val properties: Map<FieldDesc,
                ExpressionDesc>,
        override val source: SourcePoint):SourceElement{
    override val childs: Sequence<SourceElement>
        get() = properties.keys.asSequence() + properties.values.asSequence()
}