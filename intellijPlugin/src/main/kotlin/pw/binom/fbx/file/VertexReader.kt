package pw.binom.fbx.file

class VertexData {
    var v1 = 0
    var v2 = 0
    var v3 = 0
    var uv1 = 0
    var uv2 = 0
    var uv3 = 0
    var n1 = 0
    var n2 = 0
    var n3 = 0
}

fun readFaces(indexs: IntArray, uvIndex: IntArray, func: (VertexData) -> Unit) {
    var count = 0

    indexs.forEachIndexed { index, value ->
        count++
        var data = VertexData()
        if (value < 0) {
            val vv = value.let { if (it >= 0) it else -it - 1 }
            when (count) {
                3 -> {
                    data.v1 = indexs[index - 2]
                    data.v2 = indexs[index - 1]
                    data.v3 = vv
                    data.uv1 = uvIndex[index - 2]
                    data.uv2 = uvIndex[index]
                    data.uv3 = uvIndex[index - 1]
                    data.n1 = index - 2
                    data.n2 = index - 1
                    data.n3 = index
                    func(data)
                }
                4 -> {
                    data.v1 = indexs[index - 2]
                    data.v2 = vv
                    data.v3 = indexs[index - 3]
                    data.uv1 = uvIndex[index - 2]
                    data.uv2 = uvIndex[index]
                    data.uv3 = uvIndex[index - 3]
                    data.n1 = index - 2
                    data.n2 = index
                    data.n3 = index - 3
                    func(data)

                    data.v1 = indexs[index - 2]
                    data.v2 = indexs[index - 1]
                    data.v3 = vv
                    data.uv1 = uvIndex[index - 2]
                    data.uv2 = uvIndex[index - 1]
                    data.uv3 = uvIndex[index]
                    data.n1 = index - 2
                    data.n2 = index - 1
                    data.n3 = index
                    func(data)
                }
                else -> TODO("count: $count")
            }
            count = 0
        }
    }
    if (count != 0)
        TODO()
}

fun faceCount(indexs: IntArray): Int {
    var count = 0
    readFaces(indexs, indexs) { _ ->
        count++
    }
    return count
}