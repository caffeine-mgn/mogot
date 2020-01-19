package mogot

internal const val SPATIAL_TYPE = 0b1
internal const val VISUAL_INSTANCE3D_TYPE = 0b10 or SPATIAL_TYPE

internal const val SPATIAL2D_TYPE = 0b100
internal const val VISUAL_INSTANCE2D_TYPE = 0b1000 or SPATIAL2D_TYPE