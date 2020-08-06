package com.kazakago.cacheflowable.sample.viewmodel.livedata

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

open class LiveEvent<T> : LiveData<T>() {

    private val dispatchedTagSet = mutableSetOf<String>()

    @MainThread
    @Deprecated(
        message = "Multiple observers registered but only one will be notified of changes. set tags for each observer.",
        replaceWith = ReplaceWith("observe(owner, \"\", observer)"),
        level = DeprecationLevel.HIDDEN
    )
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        observe(owner, "", observer)
    }

    @MainThread
    @Deprecated(
        message = "Multiple observers registered but only one will be notified of changes. set tags for each observer.",
        replaceWith = ReplaceWith("observeForever(\"\", observer)"),
        level = DeprecationLevel.HIDDEN
    )
    override fun observeForever(observer: Observer<in T>) {
        super.observeForever(observer)
    }

    @MainThread
    @Deprecated(
        message = "should not use getValue() in LiveEvent.",
        level = DeprecationLevel.HIDDEN
    )
    override fun getValue(): T? {
        return super.getValue()
    }

    @MainThread
    open fun observe(owner: LifecycleOwner, tag: String, observer: Observer<in T>) {
        super.observe(owner, Observer<T> {
            val internalTag = owner::class.java.name + "#" + tag
            if (!dispatchedTagSet.contains(internalTag)) {
                dispatchedTagSet.add(internalTag)
                observer.onChanged(it)
            }
        })
    }

    @MainThread
    open fun observeForever(tag: String, observer: Observer<in T>) {
        super.observeForever {
            if (!dispatchedTagSet.contains(tag)) {
                dispatchedTagSet.add(tag)
                observer.onChanged(it)
            }
        }
    }

    @MainThread
    protected open fun call(t: T) {
        dispatchedTagSet.clear()
        value = t
    }

}