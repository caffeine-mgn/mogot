package pw.binom.material.lex

import pw.binom.material.SourcePoint

interface Global {
    val source:SourcePoint

    companion object {
        fun read(lex: LexStream<TokenType>): Global? = run {
            CommentDef.read(lex)
            Property.read(lex)
                    ?: AnnotationExp.read(lex)
                    ?: ClassDef.read(lex)
                    ?: GlobalVar.read(lex)
                    ?: GlobalMethod.read(lex)
        }
    }
}