package pw.binom.material.lex

class ParserException(message: String, val position: Int, val length: Int) : Exception("$message on $position")