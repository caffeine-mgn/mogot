package pw.binom.material.uniformEditor

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.Key

class UniformData(val document: Document, val key: Key<String>) {
    private val mapper = ObjectMapper()
    private fun get() =
            document.getUserData(this.key)?.let { mapper.readValue(it, Array<String>::class.java) }?.asSequence()?.associate {
                val item = it.split("=", limit = 2)
                item[0] to item[1]
            }?.toMutableMap() ?: mutableMapOf()

    private fun set(map: Map<String, String>) {
        if (map.isEmpty())
            document.putUserData(key, null)
        else
            document.putUserData(key, map.map { "${it.key}=${it.value}" }.let { mapper.writeValueAsString(it) })
    }

    operator fun get(key: String): String? = get()[key]

    operator fun set(key: String, value: String?) {
        val m = get()
        if (value == null)
            m.remove(key)
        else
            m[key] = value
        set(m)
    }
}