package pw.binom.material.compiler

import pw.binom.material.SourcePoint

class CompileException(message: String, val position: Int, val length: Int) : Exception(message) {
    constructor(message: String, source: SourcePoint) : this(message, source.position, source.length)
}