package me.fru1t.android.slik

import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.slik.annotations.Named
import me.fru1t.android.slik.annotations.Singleton
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
    private val bindings = HashMap<KClass<*>, KClass<*>>()

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
     * Binds an [abstraction] to an [implementation] so that Slik may resolve a dependency for the
     * abstract class.
     */
    fun <T1 : Any, T2 : T1> bind(abstraction: KClass<T1>, implementation: KClass<T2>): Slik {
        bindings.put(abstraction, implementation)
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
     * Retrieves an instance of [injectedClass] by following the injection rules of Slik. If the
     * class is marked as singleton, only a single instance per scope per value will be created.
     * Otherwise, a new instance will be attempted. The class must be marked as [Inject]able and
     * must have a primary constructor (ie. be a Kotlin class).
     */
    private fun <T: Any> resolve(kClass: KClass<T>, name: Named? = null): T {
        val singletonName = makeClassKey(kClass, name?.value)
        var injectedClass = kClass

        // If we have a reference, it's a singleton
        if (singletons.containsKey(singletonName)) {
            @Suppress("UNCHECKED_CAST")
            return singletons[singletonName] as T
        }

        // Sanity check
        if (injectedClass.isData
                || injectedClass.java.isEnum
                || injectedClass.java.isAnnotation
                || injectedClass.java.isArray) {
            throw SlikException("${injectedClass.qualifiedName} must be a regular kotlin class in" +
                    " order for Slik to create an instance of it.")
        }

        // Check for bindings
        if (injectedClass.java.isInterface || injectedClass.isAbstract) {
            if (!bindings.containsKey(injectedClass)) {
                throw SlikException("${injectedClass.qualifiedName} is an interface or abstract " +
                        "class that must be #bound to an implementation before Slik can inject it.")
            }

            @Suppress("UNCHECKED_CAST")
            injectedClass = bindings[kClass]!! as KClass<T>
        }

        // Is it injectable?
        if (injectedClass.findAnnotation<Inject>() == null) {
            throw SlikException("${injectedClass.qualifiedName} must be @Inject annotated.")
        }

        // Get the class's constructor
        val constructor = injectedClass.primaryConstructor?.javaConstructor
                ?: throw SlikException("${injectedClass.qualifiedName} must be a kotlin class.")

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
                        "${injectedClass.qualifiedName}'s dependencies couldn't be fulfilled." +
                                "\r\n\t ${e.message}")
            }
        })

        // Cache as singleton if required
        val result = constructor.newInstance(*fulfillments)!!
        if (injectedClass.findAnnotation<Singleton>() != null) {
            singletons.put(singletonName, result)
        }

        return result
    }

    private fun makeClassKey(kClass: KClass<*>, name: String?): String =
            "${kClass.qualifiedName}:$name"
}
