package me.fru1t.android.slick.annotations

/**
 * Used to disambiguate dependencies of the same type.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Named(val name: String)
