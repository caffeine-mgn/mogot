package pw.binom.material.psi

class ParserException(message: String, val position: Int, val length: Int) : Exception(message)