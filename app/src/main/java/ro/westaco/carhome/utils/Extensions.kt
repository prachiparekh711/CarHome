package ro.westaco.carhome.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlin.reflect.KMutableProperty1

// init MutableLiveData with default value
fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }

// LiveData observe only once
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

// set object's property by name
fun <T, V> setProperty(instance: T, prop: KMutableProperty1<T, V>, value: V) {
    prop.set(instance, value)
}

// try-catch wrapper for possible exception throwing code blocks
inline fun safeExecute(block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}