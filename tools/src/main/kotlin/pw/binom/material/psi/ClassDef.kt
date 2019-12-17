package pw.binom.material.psi

import pw.binom.checkNext
import pw.binom.ifType
import pw.binom.skipSpace

class ClassDef(val name: String, val fields: List<GlobalVar>, val methods: List<GlobalMethod>, override val position: Int, override val length: Int) : Global {
    companion object {
        fun read(lexStream: LexStream<TokenType>) = lexStream.safe {
            val start = lexStream.skipSpace(true)?.ifType(TokenType.CLASS) ?: return@safe null
            val id = lexStream.skipSpace(false)?.ifType(TokenType.ID) ?: return@safe null
            lexStream.skipSpace(true)?.ifType(TokenType.LEFT_BRACE) ?: return@safe null

            val fields = ArrayList<GlobalVar>()
            val methods = ArrayList<GlobalMethod>()
            var end: LexStream.Element<TokenType>? = null
            while (true) {
                if (lexStream.checkNext(true)?.element == TokenType.RIGHT_BRACE) {
                    end = lexStream.skipSpace(true)
                    break
                }

                val item = GlobalVar.read(lexStream) ?: GlobalMethod.read(lexStream) ?: return@safe null
                when (item) {
                    is GlobalVar -> fields += item
                    is GlobalMethod -> methods += item
                    else -> TODO()
                }
            }

            return@safe ClassDef(id.text, fields, methods, start.position, end!!.position + 1 - start.position)
        }
    }
}