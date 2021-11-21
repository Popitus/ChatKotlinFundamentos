package io.keepcoding.chat.extensions

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import kotlinx.coroutines.*

inline val View.inflater: LayoutInflater
    get() = LayoutInflater.from(context)
