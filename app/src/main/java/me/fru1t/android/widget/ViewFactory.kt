package me.fru1t.android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import me.fru1t.android.slik.annotations.Inject
import me.fru1t.android.slik.annotations.Singleton

/** Factory that dynamically creates any view. */
@Inject
@Singleton
class ViewFactory(val context: Context) {
    /** Creates a new [T] optionally given [attrs] and [defStyleAttr] */
    inline fun <reified T : View> create(attrs: AttributeSet? = null, defStyleAttr: Int = 0): T =
        T::class.java
                .getConstructor(Context::class.java, AttributeSet::class.java, Int::class.java)
                .newInstance(context, attrs, defStyleAttr)
}
