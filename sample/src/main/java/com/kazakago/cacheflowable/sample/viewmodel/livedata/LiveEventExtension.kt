package com.kazakago.cacheflowable.sample.viewmodel.livedata

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

@MainThread
inline fun <T> LiveEvent<T>.observe(
    owner: LifecycleOwner,
    tag: String,
    crossinline onChanged: (T) -> Unit
): Observer<T> {
    val wrappedObserver = Observer<T> { t -> onChanged.invoke(t) }
    observe(owner, tag, wrappedObserver)
    return wrappedObserver
}
