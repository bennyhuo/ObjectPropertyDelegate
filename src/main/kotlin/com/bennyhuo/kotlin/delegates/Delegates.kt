package com.bennyhuo.kotlin.delegates

import kotlin.jvm.internal.PropertyReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.*

fun <T> delegateOf(setter: ((T) -> Unit), initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter0(setter, initializedValue)
fun <T> delegateOf(getter: (() -> T), setter: ((T) -> Unit)? = null, initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate0(getter, setter, initializedValue)

fun <T, R> delegateOf(setter: ((R, T) -> Unit), receiver: R, initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter1(receiver, setter, initializedValue)
fun <T, R> delegateOf(getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, receiver: R, initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate1(receiver, getter, setter, initializedValue)

fun <T, R> delegateLazyOf(setter: ((R, T) -> Unit), initializedValue: T? = null, lazyReceiver: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetterLazy1(lazyReceiver, setter, initializedValue)
fun <T, R> delegateLazyOf(getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, initializedValue: T? = null, lazyReceiver: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateLazy1(lazyReceiver, getter, setter, initializedValue)

@Deprecated(message = "Please use delegateOf instead.",
        replaceWith = ReplaceWith("delegateOf(setter, receiver, initializedValue)", "com.bennyhuo.kotlin.delegates"))
fun <T, R> delegateWithReceiverOf(receiver: R, setter: ((R, T) -> Unit), initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter1(receiver, setter, initializedValue)
@Deprecated(message = "Please use delegateOf instead.",
        replaceWith = ReplaceWith("delegateOf(getter, setter, receiver, initializedValue)", "com.bennyhuo.kotlin.delegates"))
fun <T, R> delegateWithReceiverOf(receiver: R, getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate1(receiver, getter, setter, initializedValue)

fun <T> KProperty0<T>.delegator(initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate0(propertyRef = this as PropertyReference, initializedValue = initializedValue)
fun <T, R> KProperty1<R, T>.delegator(receiver: R, initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate1(receiver, property = this, initializedValue = initializedValue)
fun <T, R> KProperty1<R, T>.delegatorLazy(initializedValue: T? = null, lazyReceiver: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateLazy1(lazyReceiver, property = this, initializedValue = initializedValue)

@JvmName("delegatorGetter")
fun <T> KFunction0<T>.delegator(initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate0(this, initializedValue = initializedValue)

@JvmName("delegatorGetter")
fun <T, R> KFunction1<R, T>.delegator(receiver: R, initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate1(receiver, this, initializedValue = initializedValue)

@JvmName("delegatorGetterLazy")
fun <T, R> KFunction1<R, T>.delegatorLazy(initializedValue: T? = null, lazyReceiver: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateLazy1(lazyReceiver, this, initializedValue = initializedValue)

fun <T> KFunction1<T, Unit>.delegator(initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter0(setter = this, initializedValue = initializedValue)
fun <T, R> KFunction2<R, T, Unit>.delegator(receiver: R, initializedValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter1(receiver, setter = this, initializedValue = initializedValue)
fun <T, R> KFunction2<R, T, Unit>.delegatorLazy(initializedValue: T? = null, lazyReceiver: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetterLazy1(lazyReceiver, setter = this, initializedValue = initializedValue)

internal class ObjectPropertyDelegateNoGetter0<T>(val setter: ((T) -> Unit), initializedValue: T? = null) : ReadWriteProperty<Any, T> {
    private var value: T? = initializedValue

    init {
        initializedValue?.let { setter.invoke(it) }
    }

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value!!
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setter.invoke(value)
        this.value = value
    }
}

@Suppress("UNCHECKED_CAST")
internal class ObjectPropertyDelegate0<T>(val getter: (() -> T), val setter: ((T) -> Unit)? = null, initializedValue: T? = null) : ReadWriteProperty<Any, T> {

    constructor(propertyRef: PropertyReference, initializedValue: T? = null)
            : this((propertyRef as KProperty0<T>)::get, if (propertyRef is KMutableProperty0<*>) (propertyRef as KMutableProperty0<T>)::set else null, initializedValue)

    init {
        initializedValue?.let { setter?.invoke(it) }
    }

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return getter.invoke()
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setter?.invoke(value)
    }
}

internal abstract class AbsObjectPropertyDelegateNoGetter1<T, R>(val setter: ((R, T) -> Unit), initializedValue: T? = null) : ReadWriteProperty<Any, T> {
    private var value: T? = initializedValue

    protected abstract val receiver: R

    final override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value!!
    }

    final override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setter.invoke(receiver, value)
        this.value = value
    }
}

internal class ObjectPropertyDelegateNoGetter1<T, R>(override val receiver: R, setter: ((R, T) -> Unit), initializedValue: T? = null)
    : AbsObjectPropertyDelegateNoGetter1<T, R>(setter, initializedValue) {

    init {
        initializedValue?.let { setter.invoke(receiver, it) }
    }
}

internal class ObjectPropertyDelegateNoGetterLazy1<T, R>(lazyReceiver: () -> R, setter: ((R, T) -> Unit), initializedValue: T? = null)
    : AbsObjectPropertyDelegateNoGetter1<T, R>(setter, initializedValue) {

    override val receiver by lazy {
        lazyReceiver().also { receiver ->
            initializedValue?.let { setter.invoke(receiver, it) }
        }
    }
}

internal abstract class AbsObjectPropertyDelegate1<T, R>(val getter: ((R) -> T), val setter: ((R, T) -> Unit)? = null) : ReadWriteProperty<Any, T> {

    protected abstract val receiver: R

    final override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return getter.invoke(receiver)
    }

    final override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setter?.invoke(receiver, value)
    }
}

internal class ObjectPropertyDelegate1<T, R>(override val receiver: R, getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, initializedValue: T? = null)
    : AbsObjectPropertyDelegate1<T, R>(getter, setter) {

    constructor(receiver: R, property: KProperty1<R, T>, initializedValue: T? = null)
            : this(receiver, property, if (property is KMutableProperty1<*, *>) (property as KMutableProperty1<R, T>)::set else null, initializedValue)

    init {
        initializedValue?.let { setter?.invoke(receiver, it) }
    }
}

internal class ObjectPropertyDelegateLazy1<T, R>(lazyReceiver: () -> R, getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, initializedValue: T? = null)
    : AbsObjectPropertyDelegate1<T, R>(getter, setter) {

    constructor(lazyReceiver: () -> R, property: KProperty1<R, T>, initializedValue: T? = null)
            : this(lazyReceiver, property, if (property is KMutableProperty1<*, *>) (property as KMutableProperty1<R, T>)::set else null, initializedValue)

    override val receiver by lazy {
        lazyReceiver().also { receiver ->
            initializedValue?.let { setter?.invoke(receiver, it) }
        }
    }
}