package mogot

import mogot.gl.GL
import mogot.math.Math


object Geoms {

    fun panel(gl: GL, x: Float, z: Float) = Geom3D2(gl, vertex = floatArrayOf(
            //верх
            -x / 2f, 0f, z / 2f,
            x / 2f, 0f, z / 2f,
            -x / 2f, 0f, -z / 2f,
            x / 2f, 0f, -z / 2f

    ), uvs = floatArrayOf(
            //верх
            0f, 1f,//0
            1f, 1f,//1
            0f, 0f,//2
            1f, 0f//3

    ), normals = floatArrayOf(
            //верх
            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f
    ),
            index = intArrayOf(
                    0, 1, 2,
                    1, 3, 2
            )
    )

    fun buildCube2(gl: GL, size: Float) = Geom3D2(gl,
            vertex = floatArrayOf(

                    //верх
                    -size, size, size,
                    size, size, size,
                    -size, size, -size,

                    size, size, size,
                    size, size, -size,
                    -size, size, -size,

                    //низ
                    size, -size, size,
                    -size, -size, size,
                    -size, -size, -size,

                    size, -size, -size,
                    size, -size, size,
                    -size, -size, -size,

                    //перед
                    size, size, size,
                    -size, size, size,
                    -size, -size, size,

                    size, size, size,
                    -size, -size, size,
                    size, -size, size,

                    //зад
                    -size, size, -size,
                    size, size, -size,
                    -size, -size, -size,

                    -size, -size, -size,
                    size, size, -size,
                    size, -size, -size,

                    //лево
                    -size, size, size,
                    -size, size, -size,
                    -size, -size, -size,

                    -size, size, size,
                    -size, -size, -size,
                    -size, -size, size,

                    //право
                    size, size, -size,
                    size, size, size,
                    size, -size, -size,

                    size, -size, -size,
                    size, size, size,
                    size, -size, size
            ), uvs = floatArrayOf(
            //верх
            0f, 1f,
            1f, 1f,
            0f, 0f,

            1f, 1f,
            1f, 0f,
            0f, 0f,

            //низ
            1f, 1f,
            0f, 1f,
            0f, 0f,

            1f, 0f,
            1f, 1f,
            0f, 0f,

            //перед
            1f, 1f,
            0f, 1f,
            0f, 0f,

            1f, 1f,
            0f, 0f,
            1f, 0f,

            //зад
            0f, 1f,
            1f, 1f,
            0f, 0f,

            0f, 0f,
            1f, 1f,
            1f, 0f,

            //лево
            1f, 1f,
            0f, 1f,
            0f, 0f,

            1f, 1f,
            0f, 0f,
            1f, 0f,

            //право
            0f, 1f,
            1f, 1f,
            0f, 0f,

            0f, 0f,
            1f, 1f,
            1f, 0f
    ), normals = floatArrayOf(
            //верх
            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f,

            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f,

            //низ
            0f, -1f, 0f,
            0f, -1f, 0f,
            0f, -1f, 0f,

            0f, -1f, 0f,
            0f, -1f, 0f,
            0f, -1f, 0f,

            //перед
            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,

            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,

            //зад
            0f, 0f, -1f,
            0f, 0f, -1f,
            0f, 0f, -1f,

            0f, 0f, -1f,
            0f, 0f, -1f,
            0f, 0f, -1f,

            //лево
            -1f, 0f, 0f,
            -1f, 0f, 0f,
            -1f, 0f, 0f,

            -1f, 0f, 0f,
            -1f, 0f, 0f,
            -1f, 0f, 0f,

            //право
            1f, 0f, 0f,
            1f, 0f, 0f,
            1f, 0f, 0f,

            1f, 0f, 0f,
            1f, 0f, 0f,
            1f, 0f, 0f
    ),
            index = intArrayOf(
                    0, 1, 2,
                    3, 4, 5,

                    6, 7, 8,
                    9, 10, 11,

                    12, 13, 14,
                    15, 16, 17,

                    18, 19, 20,
                    21, 22, 23,

                    24, 25, 26,
                    27, 28, 29,

                    30, 31, 32,
                    33, 34, 35
            )
    )

    fun buildCube3(gl: GL, width: Float, height: Float, depth: Float) = Geom3D2(gl,
            vertex = floatArrayOf(

                    //верх
                    -width, height, depth,
                    width, height, depth,
                    -width, height, -depth,

                    width, height, depth,
                    width, height, -depth,
                    -width, height, -depth,

                    //низ
                    width, -height, depth,
                    -width, -height, depth,
                    -width, -height, -depth,

                    width, -height, -depth,
                    width, -height, depth,
                    -width, -height, -depth,

                    //перед
                    width, height, depth,
                    -width, height, depth,
                    -width, -height, depth,

                    width, height, depth,
                    -width, -height, depth,
                    width, -height, depth,

                    //зад
                    -width, height, -depth,
                    width, height, -depth,
                    -width, -height, -depth,

                    -width, -height, -depth,
                    width, height, -depth,
                    width, -height, -depth,

                    //лево
                    -width, height, depth,
                    -width, height, -depth,
                    -width, -height, -depth,

                    -width, height, depth,
                    -width, -height, -depth,
                    -width, -height, depth,

                    //право
                    width, height, -depth,
                    width, height, depth,
                    width, -height, -depth,

                    width, -height, -depth,
                    width, height, depth,
                    width, -height, depth
            ), uvs = floatArrayOf(
            //верх
            0f, 1f,
            1f, 1f,
            0f, 0f,

            1f, 1f,
            1f, 0f,
            0f, 0f,

            //низ
            1f, 1f,
            0f, 1f,
            0f, 0f,

            1f, 0f,
            1f, 1f,
            0f, 0f,

            //перед
            1f, 1f,
            0f, 1f,
            0f, 0f,

            1f, 1f,
            0f, 0f,
            1f, 0f,

            //зад
            0f, 1f,
            1f, 1f,
            0f, 0f,

            0f, 0f,
            1f, 1f,
            1f, 0f,

            //лево
            1f, 1f,
            0f, 1f,
            0f, 0f,

            1f, 1f,
            0f, 0f,
            1f, 0f,

            //право
            0f, 1f,
            1f, 1f,
            0f, 0f,

            0f, 0f,
            1f, 1f,
            1f, 0f
    ), normals = floatArrayOf(
            //верх
            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f,

            0f, 1f, 0f,
            0f, 1f, 0f,
            0f, 1f, 0f,

            //низ
            0f, -1f, 0f,
            0f, -1f, 0f,
            0f, -1f, 0f,

            0f, -1f, 0f,
            0f, -1f, 0f,
            0f, -1f, 0f,

            //перед
            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,

            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,

            //зад
            0f, 0f, -1f,
            0f, 0f, -1f,
            0f, 0f, -1f,

            0f, 0f, -1f,
            0f, 0f, -1f,
            0f, 0f, -1f,

            //лево
            -1f, 0f, 0f,
            -1f, 0f, 0f,
            -1f, 0f, 0f,

            -1f, 0f, 0f,
            -1f, 0f, 0f,
            -1f, 0f, 0f,

            //право
            1f, 0f, 0f,
            1f, 0f, 0f,
            1f, 0f, 0f,

            1f, 0f, 0f,
            1f, 0f, 0f,
            1f, 0f, 0f
    ),
            index = intArrayOf(
                    0, 1, 2,
                    3, 4, 5,

                    6, 7, 8,
                    9, 10, 11,

                    12, 13, 14,
                    15, 16, 17,

                    18, 19, 20,
                    21, 22, 23,

                    24, 25, 26,
                    27, 28, 29,

                    30, 31, 32,
                    33, 34, 35
            )
    )

    fun solidSphere(gl: GL, radius: Float, rings: Int, sectors: Int): Geom3D2 {
        val R = 1f / (rings - 1).toFloat()
        val S = 1f / (sectors - 1).toFloat()
        val sphere_vertices = FloatArray(rings * sectors * 3)
        val sphere_normals = FloatArray(rings * sectors * 3)
        val sphere_texcoords = FloatArray(rings * sectors * 2)

        var g = 0
        for (r in 0 until rings)
            for (s in 0 until sectors) {
                val y = Math.sin(-(Math.PI / 2) + Math.PI * r * R).toFloat()
                val x = (Math.cos(2 * Math.PI * s * S) * Math.sin(Math.PI * r * R)).toFloat()
                val z = (Math.sin(2 * Math.PI * s * S) * Math.sin(Math.PI * r * R)).toFloat()
                sphere_texcoords[g * 2 + 0] = s * S
                sphere_texcoords[g * 2 + 1] = r * R


                sphere_vertices[g * 3 + 0] = x * radius;
                sphere_vertices[g * 3 + 1] = y * radius;
                sphere_vertices[g * 3 + 2] = z * radius;

                sphere_normals[g * 3 + 0] = x;
                sphere_normals[g * 3 + 1] = y;
                sphere_normals[g * 3 + 2] = z;

                g++
            }

        val sphere_indices = IntArray(rings * sectors * 3 * 2)
        g = 0
        for (r in 0 until rings) for (s in 0 until sectors) {

//            sphere_indices[g * 4 + 0] = r * sectors + s
//            sphere_indices[g * 4 + 1] = r * sectors + (s + 1)
//            sphere_indices[g * 4 + 2] = (r + 1) * sectors + (s + 1)
//            sphere_indices[g * 4 + 3] = (r + 1) * sectors + s
//            g++

            sphere_indices[g * 3 + 0] = r * sectors + (s + 1)
            sphere_indices[g * 3 + 1] = r * sectors + s
            sphere_indices[g * 3 + 2] = (r + 1) * sectors + s
            g++

            sphere_indices[g * 3 + 0] = r * sectors + (s + 1)
            sphere_indices[g * 3 + 1] = (r + 1) * sectors + s
            sphere_indices[g * 3 + 2] = (r + 1) * sectors + (s + 1)
            g++
        }

        return Geom3D2(gl,
                index = sphere_indices,
                vertex = sphere_vertices,
                normals = sphere_normals,
                uvs = sphere_texcoords
        )
    }
}
