package me.fru1t.android.app

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View

/** Shorthand for [View.findViewById] */
inline fun <reified T: View> Activity.find(@IdRes res: Int): T = findViewById(res) as T
