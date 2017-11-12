package me.fru1t.android.content.res

import android.content.res.TypedArray

/** Executes [block] for this [TypedArray] and recycles it afterward. */
inline fun TypedArray.use(block: (TypedArray) -> Unit) {
    try {
        block(this)
    } finally {
        this.recycle()
    }
}
