package me.fru1t.android.view

import android.support.annotation.IdRes
import android.view.View

/** Shorthand for [View.findViewById] */
inline fun <reified T: View> View.find(@IdRes res: Int): T = findViewById(res) as T
