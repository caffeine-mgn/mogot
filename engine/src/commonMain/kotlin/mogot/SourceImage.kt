package mogot

import pw.binom.ByteDataBuffer

class SourceImage(val type: Type, val width: Int, val height: Int, val data: ByteDataBuffer?): ResourceImpl() {
    enum class Type(val elementSize: Int) {
        RGB(3),
        RGBA(4)
    }

    override fun dispose() {
        data?.close()
    }

}