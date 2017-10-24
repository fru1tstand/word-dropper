package me.fru1t.android.slick

import me.fru1t.android.slick.annotations.Inject
import me.fru1t.android.slick.annotations.Named
import me.fru1t.android.slick.annotations.Singleton
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor

/**
 * Simple Lightweight dependency Injection frameworK.
 */
class Slik {
    companion object {
        private val scopes = HashMap<KClass<*>, Slik>()

        /** Retrieves the scoped instance of Slik for the given [kClass]. */
        fun get(kClass: KClass<*>): Slik {
            if (!scopes.containsKey(kClass)) {
                scopes.put(kClass, Slik())
            }
            return scopes[kClass]!!
        }

        /** Retrieves the scoped instance of Slik for the given [clazz] */
        fun get(clazz: Class<*>): Slik = get(clazz.kotlin)
    }

    private val singletons = HashMap<String, Any>()

    /**
     * Provide a singleton [instance] to slick with an optional [name] to use when resolving
     * dependencies. Objects passed as provided will be treated like the class has a
     * [Singleton] annotation.
     */
    fun provide(instance: Any, name: String? = null): Slik {
        var key = makeClassKey(instance::class, name)
        if (singletons.containsKey(key)) {
            throw SlikException(
                    "${instance::class.qualifiedName} named \"$name\" cannot" +
                            " be provided twice.")
        }
        singletons.put(key, instance)
        instance::class.allSuperclasses.forEach {
            key = makeClassKey(it, name)
            if (!singletons.containsKey(key)) {
                singletons.put(key, instance)
            }
        }
        return this
    }

    /**
     * Resolves dependencies for the given instance. This should only be used when constructor
     * injection is not an option (ie. when you have no control over the object lifecycle). In this
     * case, any subclass that requires injection must also call [inject].
     */
    fun inject(instance: Any) {
        // Inject into annotated fields
        try {
            instance::class.java.declaredFields.forEach {
                if (it.getAnnotation(Inject::class.java) == null) {
                    return@forEach
                }
                if (!it.isAccessible) {
                    it.isAccessible = true
                }
                it.set(instance, resolve(it.type.kotlin, it.getAnnotation(Named::class.java)))
            }
        } catch (e: SlikException) {
            throw SlikException(
                    "${instance::class.qualifiedName} failed to inject its dependencies." +
                            "\r\n\t ${e.message}")
        }
    }

    /**
     * Retrieves an instance of [kClass] by following the injection rules of Slik. If the class
     * is marked as singleton, only a single instance per scope per name will be created. Otherwise,
     * a new instance will be attempted. The class must be marked as [Inject]able and must have a
     * primary constructor (ie. a Kotlin class).
     */
    private fun <T: Any> resolve(kClass: KClass<T>, name: Named? = null): T {
        val singletonName = makeClassKey(kClass, name?.name)

        // If we have a reference, it's a singleton
        if (singletons.containsKey(singletonName)) {
            @Suppress("UNCHECKED_CAST")
            return singletons[singletonName] as T
        }

        // No IOC support/basic sanity check
        if (kClass.java.isInterface
                || kClass.isAbstract
                || kClass.isData
                || kClass.java.isEnum
                || kClass.java.isAnnotation
                || kClass.java.isArray) {
            throw SlikException("${kClass.qualifiedName} must be a regular kotlin class in order" +
                    " for Slik to create an instance of it.")
        }

        // Is it injectable?
        if (kClass.findAnnotation<Inject>() == null) {
            throw SlikException("${kClass.qualifiedName} must be @Inject annotated.")
        }

        // Get the class's constructor
        val constructor = kClass.primaryConstructor?.javaConstructor
                ?: throw SlikException("${kClass.qualifiedName} must be a kotlin class.")

        // Fulfill dependencies
        val params = constructor.parameterTypes
        val paramAnnotations = constructor.parameterAnnotations
        val fulfillments = Array<Any>(params.size, {
            try {
                resolve(
                        params[it].kotlin,
                        paramAnnotations[it].firstOrNull { it is Named } as Named?)
            } catch(e: SlikException) {
                throw SlikException(
                        "${kClass.qualifiedName}'s dependencies couldn't be fulfilled." +
                                "\r\n\t ${e.message}")
            }
        })

        // Cache as singleton if required
        val result = constructor.newInstance(*fulfillments)!!
        if (kClass.findAnnotation<Singleton>() != null) {
            singletons.put(singletonName, result)
        }

        return result
    }

    private fun makeClassKey(kClass: KClass<*>, name: String?): String =
            "${kClass.qualifiedName}:$name"
}
