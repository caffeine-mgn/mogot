package pw.binom.material.compiler

import pw.binom.material.SourcePoint

interface SourceElement {
    val source: SourcePoint

//    fun findByOffset(offset: Int): SourceElement?
    val childs: Sequence<SourceElement>
}