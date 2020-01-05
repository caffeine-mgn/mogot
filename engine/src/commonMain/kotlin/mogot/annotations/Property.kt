package mogot.annotations

annotation class Property(val display: String = "")
annotation class RestrictionMin(val value: Float)
annotation class RestrictionMax(val value: Float)
annotation class ColorProperty(val value: Float)