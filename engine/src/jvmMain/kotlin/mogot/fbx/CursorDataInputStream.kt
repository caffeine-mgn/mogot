package mogot.fbx

import java.io.InputStream

internal class CursorDataInputStream(private val stream: InputStream) : InputStream() {
    private var cursor = 0L

    val fill: Long
        get() = cursor

    override fun skip(n: Long): Long {
        try {
            return super.skip(n)
        } finally {
            cursor += n
        }
    }

    override fun read(): Int {
        val data = stream.read()
        cursor++
        return data
    }

    override fun read(b: ByteArray?): Int {
        val readed = stream.read(b)
        cursor += readed
        return readed
    }

    override fun read(b: ByteArray?, off: Int, len: Int): Int {
        val readed = stream.read(b, off, len)
        cursor += readed
        return readed
    }

    override fun available(): Int {
        return stream.available()
    }

    override fun close() {
        stream.close()
    }

    override fun reset() {
        throw RuntimeException("Not supported")
    }

    override fun mark(readlimit: Int) {
        throw RuntimeException("Not supported")
    }

    override fun markSupported(): Boolean {
        return super.markSupported()
    }
}