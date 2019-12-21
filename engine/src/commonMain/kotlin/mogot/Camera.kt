package mogot

import mogot.math.*


class Camera : Spatial() {
    val projectionMatrix = Matrix4f()
    private var width = 0
    private var height = 0

    fun resize(width: Int, height: Int) {
        this.width = width
        this.height = height
        projectionMatrix.identity().perspective(Math.toRadians(45.0).toFloat(), width.toFloat() / height.toFloat(), 0.1f, 1000f)
    }

    fun applyMatrix(viewMatrix4f: Matrix4f) {
        this.asUpSequence().mapNotNull { it as? Spatial }.forEach {
            viewMatrix4f.set(it.apply(viewMatrix4f))
        }
        viewMatrix4f.rotateAffine(quaternion)
        viewMatrix4f.translate(-position)
    }

    /**
     * Проэцирует глобальную матрицу на экран данной камеры. Результат кладется в [dest]
     *
     * @param matrix глобальная матрица позиции <b>относительно данной камеры</b>
     * @param dest вектор, в который кладётся проекция [matrix] на экран данной камеры
     */
    fun worldToScreenPoint(matrix: Matrix4fc, dest: Vector2i): Boolean {
        val pos = TEMP_VEC_3F_1
        matrix.getTranslation(pos)
        val clipCoords = pos//TEMP_VEC_3F_2


        val proj = projectionMatrix
        clipCoords.z = pos.x * proj.m03 + pos.y * proj.m13 + pos.z * proj.m23 + proj.m33
        if (clipCoords.z < 0.1f)
            return false
        clipCoords.x = pos.x * proj.m00 + pos.y * proj.m10 + pos.z * proj.m20 + proj.m30
        clipCoords.y = pos.x * proj.m01 + pos.y * proj.m11 + pos.z * proj.m21 + proj.m31

        val ndc = TEMP_VEC_2F
        ndc.x = clipCoords.x / clipCoords.z;
        ndc.y = clipCoords.y / clipCoords.z;

        dest.x = ((width.toFloat() / 2 * ndc.x) + (ndc.x + width.toFloat() / 2)).toInt()
        dest.y = (-(height.toFloat() / 2 * ndc.y) + (ndc.y + height.toFloat() / 2)).toInt()
        return true
    }
}

val TEMP_VEC_2F = Vector2f()
val TEMP_VEC_3F_1 = Vector3f()
val TEMP_VEC_3F_2 = Vector3f()