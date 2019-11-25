package mogot.fbx

import mogot.*
import mogot.gl.GL
import mogot.math.Quaternionf
import mogot.math.Vector3f
import java.io.InputStream
import java.util.logging.Logger

object FbxImporter {

    private val LOG = Logger.getLogger(this::class.java.simpleName)

    fun import(stream: InputStream): FbxModel2 {

        val root = FbxModel2(FbxFile.Element("", emptyList(), listOf(FbxFile.Element("Properties70", emptyList(), emptyList()))))
        val file = FbxFile()
        FbxReader.read(stream, file)

        val objects = file.get("Objects").first().childs.asSequence().map {
            when (it.id) {
                "Geometry" -> FbxGeometry(it)
                "NodeAttribute" -> null
                "Model" -> FbxModel2(it)
                "Pose" -> null
                "Deformer" -> {
                    when (it.properties[2] as String) {
                        "Skin" -> FbxSkin(it)
                        "Cluster" -> FbxCluster(it)
                        else -> TODO()
                    }
                }
                "AnimationStack" -> FbxAnimationStack(it)
                "AnimationLayer" -> FbxAnimationLayer(it)
                "AnimationCurveNode" -> AnimationCurveNode(it)
                "AnimationCurve" -> AnimationCurve(it)
                "Material" -> FbxMaterial(it)
                "Texture" -> null
                "Video" -> null
                else -> TODO("Unknown element ${it.id}")
            }
        }.filterNotNull().associate { it.id to it }

        file.get("Connections").single().get("C").forEach {
            val op = it.properties[0] as String
            val from = it.properties[1] as Long
            val to = it.properties[2] as Long

            val params = if (op == "OP") it.properties[3] as String else null

            val fromObj = objects[from]
            val toObj = if (to == 0L) root else objects[to]

            if (fromObj == null) {
                LOG.warning("Node from #${from} not found")
                return@forEach
            }

            if (toObj == null) {
                LOG.warning("Node to #${to} not found")
                return@forEach
            }

            fromObj.connectTo(toObj, params)
        }




        return root
    }

    fun import(gl: GL, obj: FbxModel2, material: () -> Material): Spatial {
        val node =
                if (obj.geometry != null)
                    GeomNode().also {
                        it.geom = import2(gl, obj.geometry!!)
                        it.material = material()
                    }
                else
                    Spatial()

        node.position.set(obj.translation)
        node.quaternion.rotateZYX(obj.rotation.z, obj.rotation.y, obj.rotation.x)
        node.scale.set(obj.scale)

//        obj.geometry?.also {
//            node.use(GeomNode3D2(import2(it), material()))
//        }

        if (obj.animations.isNotEmpty()) {
            val anim = AnimationBehaviour()
            node.behaviour = anim
            obj.animations.forEach { animation ->
                anim.duration = animation.layout.animationStack.duration

                when (animation.type) {
                    AnimationCurveNode.Type.ROTATION -> {
                        animation.calc().forEach { time, vec ->
                            val f = Quaternionf()
                            f.rotateZYX(Math.toRadians(vec.z.toDouble()).toFloat(), Math.toRadians(vec.y.toDouble()).toFloat(), Math.toRadians(vec.x.toDouble()).toFloat())
                            anim.addRotation(time = (time * FbxFile.SECONDS_PER_UNIT).toFloat(), rotation = f)
                        }
                    }
                    AnimationCurveNode.Type.POSITION -> {
                        animation.calc().forEach { time, vec ->
                            anim.addPosition(time = (time * FbxFile.SECONDS_PER_UNIT).toFloat(), position = vec)
                        }
                    }

                    AnimationCurveNode.Type.SCALE -> {
                        animation.calc().forEach { time, vec ->
                            anim.addScale(time = (time * FbxFile.SECONDS_PER_UNIT).toFloat(), scale = vec)
                        }
                    }
                }
            }
        }


        obj.childs.forEach {
            import(gl, it, material).parent = node
        }

        return node
    }

    fun import2(gl: GL, geom: FbxGeometry): Geom3D2 {
        val vertex = geom.vertices
        val edges = geom.edges
        val polygonVertex = geom.polygonVertexIndex
        val normals = geom.normals
        val uiElements = geom.uiElements
        val uiIndex = geom.uiIndex

        val faceCount = faceCount(polygonVertex)
        val i = IntArray(faceCount * 3)
        val uvF = FloatArray(uiElements.size) { -1f }
        val normalF = FloatArray(vertex.size) { 0f }
        val normalI = IntArray(vertex.size) { 0 }

        if (uiIndex.size != polygonVertex.size)
            throw IllegalArgumentException()

        fun calc(vertexIndex: Int, index: Int) {
            normalF[vertexIndex * 3 + 0] += normals[index * 3 + 0].toFloat()
            normalI[vertexIndex * 3 + 0]++

            normalF[vertexIndex * 3 + 1] += normals[index * 3 + 1].toFloat()
            normalI[vertexIndex * 3 + 1]++

            normalF[vertexIndex * 3 + 2] += normals[index * 3 + 2].toFloat()
            normalI[vertexIndex * 3 + 2]++
        }

        var c = 0
        readFaces(polygonVertex, uiIndex) { data ->
            i[c * 3 + 0] = data.v1
            i[c * 3 + 1] = data.v2
            i[c * 3 + 2] = data.v3

            calc(data.v1, data.n1)
            calc(data.v2, data.n2)
            calc(data.v3, data.n3)

            uvF[data.v1 * 2 + 0] = uiElements[data.uv1 * 2 + 0].toFloat()
            uvF[data.v1 * 2 + 1] = uiElements[data.uv1 * 2 + 1].toFloat()

            uvF[data.v2 * 2 + 0] = uiElements[data.uv2 * 2 + 0].toFloat()
            uvF[data.v2 * 2 + 1] = uiElements[data.uv2 * 2 + 1].toFloat()

            uvF[data.v3 * 2 + 0] = uiElements[data.uv3 * 2 + 0].toFloat()
            uvF[data.v3 * 2 + 1] = uiElements[data.uv3 * 2 + 1].toFloat()
            c++
        }

        var i2 = 0
        val vv = Vector3f()
        while (i2 < vertex.size) {
            vv.set(vertex[i2 + 0].toFloat(), vertex[i2 + 1].toFloat(), vertex[i2 + 2].toFloat()).normalize()
            normalF[i2 + 0] = vv.x
            normalF[i2 + 1] = vv.y
            normalF[i2 + 2] = vv.z
            i2 += 3
        }
//        normalF.forEachIndexed { index, fl ->
//            if (normalI[index] == 0 || fl == 0f)
//                normalF[index] = fl
//            else
//                normalF[index] = fl / normalI[index].toFloat()
//        }

        return Geom3D2(
                vertex = FloatArray(vertex.size) { vertex[it].toFloat() },
                normals = normalF/*FloatArray(normals.size) { normals[it].toFloat() }*/,
                uvs = uvF,
                index = i,
                gl = gl
        )
    }

//    fun import(geom: FbxGeometry): Geom3D {
//        val vertex = geom.vertices
//        val edges = geom.edges
//        val polygonVertex = geom.polygonVertexIndex
//        val normals = geom.normals
//        val uiElements = geom.uiElements
//        val uiIndex = geom.uiIndex
//
//        val faceCount = faceCount(polygonVertex)
//        val uv = FloatArray(faceCount * 3 * 2)
//        val v = FloatArray(faceCount * 3 * 3)
//        val n = FloatArray(faceCount * 3 * 3)
//        var c = 0
//
//        readFaces(polygonVertex, uiIndex) { v1, v2, v3, uv1, uv2, uv3 ->
//            v[c * 9 + 0] = vertex[v1 * 3 + 0].toFloat()
//            v[c * 9 + 1] = vertex[v1 * 3 + 1].toFloat()
//            v[c * 9 + 2] = vertex[v1 * 3 + 2].toFloat()
//
//            v[c * 9 + 3] = vertex[v2 * 3 + 0].toFloat()
//            v[c * 9 + 4] = vertex[v2 * 3 + 1].toFloat()
//            v[c * 9 + 5] = vertex[v2 * 3 + 2].toFloat()
//
//            v[c * 9 + 6] = vertex[v3 * 3 + 0].toFloat()
//            v[c * 9 + 7] = vertex[v3 * 3 + 1].toFloat()
//            v[c * 9 + 8] = vertex[v3 * 3 + 2].toFloat()
//
//            uv[c * 6 + 0] = uiElements[uv1 * 2 + 0].toFloat()
//            uv[c * 6 + 1] = uiElements[uv1 * 2 + 1].toFloat()
//
//            uv[c * 6 + 2] = uiElements[uv2 * 2 + 0].toFloat()
//            uv[c * 6 + 3] = uiElements[uv2 * 2 + 1].toFloat()
//
//            uv[c * 6 + 4] = uiElements[uv3 * 2 + 0].toFloat()
//            uv[c * 6 + 5] = uiElements[uv3 * 2 + 1].toFloat()
//
//            c++
//        }
//
//        return Geom3D(vertex = v, normals = n, uvs = uv)
//    }
}