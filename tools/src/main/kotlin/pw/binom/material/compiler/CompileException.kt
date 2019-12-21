package pw.binom.material.compiler

class CompileException(message: String, val position: Int, val length: Int) : Exception(message)