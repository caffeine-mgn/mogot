package mogot.annotations

//@Retention(AnnotationRetention.RUNTIME)
@Target(allowedTargets = [AnnotationTarget.PROPERTY])
annotation class Property(val display: String = "")

//@Retention(AnnotationRetention.RUNTIME)
@Target(allowedTargets = [AnnotationTarget.PROPERTY_GETTER])
annotation class RestrictionMin(val value: Float)

//@Retention(AnnotationRetention.RUNTIME)
@Target(allowedTargets = [AnnotationTarget.PROPERTY_GETTER])
annotation class RestrictionMax(val value: Float)

//@Retention(AnnotationRetention.RUNTIME)
@Target(allowedTargets = [AnnotationTarget.PROPERTY_GETTER])
annotation class ColorProperty()