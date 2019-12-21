package pw.binom.material.psi

interface Global {
    val position: Int
    val length: Int
    companion object {
        fun read(lex: LexStream<TokenType>): Global? =
                Property.read(lex)
                        ?: Uv.read(lex)
                        ?: Model.read(lex)
                        ?: Vertex.read(lex)
                        ?: Normal.read(lex)
                        ?: Projection.read(lex)
                        ?: ClassDef.read(lex)
                        ?: GlobalVar.read(lex)
                        ?: GlobalMethod.read(lex)
    }
}