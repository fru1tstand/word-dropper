package me.fru1t.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.slik.annotations.Singleton
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor

/** Factory that dynamically creates any view. */
@Inject
@Singleton
class ViewFactory(val context: Context) {
    /** Creates a new [T] optionally given [attrs] and [defStyleAttr] */
    inline fun <reified T : View> create(attrs: AttributeSet? = null, defStyleAttr: Int = 0): T =
            T::class.primaryConstructor!!.javaConstructor!!.newInstance(
                    context, attrs, defStyleAttr)
}
