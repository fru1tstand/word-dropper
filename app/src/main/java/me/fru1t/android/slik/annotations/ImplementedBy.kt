package me.fru1t.android.slik.annotations

import kotlin.reflect.KClass

/** Denotes this interface or abstract class's default implementation. */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ImplementedBy(val value: KClass<*>)
