package com.bennyhuo.kotlin.delegates

import kotlin.jvm.internal.PropertyReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.*

fun <T> delegateOf(setter: ((T) -> Unit)? = null, defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter0(setter, defaultValue)
fun <T> delegateOf(getter: (() -> T), setter: ((T) -> Unit)? = null, defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate0(getter, setter, defaultValue)

fun <T, R> delegateOf(receiver: R, setter: ((R, T) -> Unit)? = null, defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter1(receiver, setter, defaultValue)
fun <T, R> delegateOf(receiver: R, getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate1(receiver, getter, setter, defaultValue)

fun <T, R> delegateLazyOf(setter: ((R, T) -> Unit)? = null, defaultValue: T? = null, cacheReceiver: Boolean = true, receiverGetter: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetterLazy1(receiverGetter, setter, defaultValue)
fun <T, R> delegateLazyOf(getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, defaultValue: T? = null, receiverGetter: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateLazy1(receiverGetter, getter, setter, defaultValue)

@Deprecated(message = "Please use delegateOf instead.",
        replaceWith = ReplaceWith("delegateOf(receiver, setter, defaultValue)", "com.bennyhuo.kotlin.delegates"))
fun <T, R> delegateWithReceiverOf(receiver: R, setter: ((R, T) -> Unit)? = null, defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter1(receiver, setter, defaultValue)
@Deprecated(message = "Please use delegateOf instead.",
        replaceWith = ReplaceWith("delegateOf(receiver, getter, setter, defaultValue)", "com.bennyhuo.kotlin.delegates"))
fun <T, R> delegateWithReceiverOf(receiver: R, getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate1(receiver, getter, setter, defaultValue)

fun <T> KProperty0<T>.delegator(defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate0(propertyRef = this as PropertyReference, defaultValue = defaultValue)
fun <T, R> KProperty1<R, T>.delegator(receiver: R, defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate1(receiver, property = this, defaultValue = defaultValue)
fun <T, R> KProperty1<R, T>.delegatorLazy(defaultValue: T? = null, receiverGetter: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateLazy1(receiverGetter, property = this, defaultValue = defaultValue)

@JvmName("delegatorGetter")
fun <T> KFunction0<T>.delegator(defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate0(this, defaultValue = defaultValue)

@JvmName("delegatorGetter")
fun <T, R> KFunction1<R, T>.delegator(receiver: R, defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegate1(receiver, this, defaultValue = defaultValue)

@JvmName("delegatorGetterLazy")
fun <T, R> KFunction1<R, T>.delegatorLazy(defaultValue: T? = null, receiverGetter: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateLazy1(receiverGetter, this, defaultValue = defaultValue)

fun <T> KFunction1<T, Unit>.delegator(defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter0(setter = this, defaultValue = defaultValue)
fun <T, R> KFunction2<R, T, Unit>.delegator(receiver: R, defaultValue: T? = null): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetter1(receiver, setter = this, defaultValue = defaultValue)
fun <T, R> KFunction2<R, T, Unit>.delegatorLazy(defaultValue: T? = null, receiverGetter: () -> R): ReadWriteProperty<Any, T> = ObjectPropertyDelegateNoGetterLazy1(receiverGetter, setter = this, defaultValue = defaultValue)

private class ObjectPropertyDelegateNoGetter0<T>(val setter: ((T) -> Unit)? = null, defaultValue: T? = null) : ReadWriteProperty<Any, T> {
    private var value: T? = defaultValue

    init {
        defaultValue?.let { setter?.invoke(it) }
    }

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value!!
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setter?.invoke(value)
        this.value = value
    }
}

private class ObjectPropertyDelegate0<T>(val getter: (() -> T), val setter: ((T) -> Unit)? = null, defaultValue: T? = null) : ReadWriteProperty<Any, T> {

    constructor(propertyRef: PropertyReference, defaultValue: T? = null)
            : this((propertyRef as KProperty0<T>)::get, if (propertyRef is KMutableProperty0<*>) (propertyRef as KMutableProperty0<T>)::set else null, defaultValue)

    init {
        defaultValue?.let { setter?.invoke(it) }
    }

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return getter.invoke()
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setter?.invoke(value)
    }
}

private abstract class AbsObjectPropertyDelegateNoGetter1<T, R>(val setter: ((R, T) -> Unit)? = null, defaultValue: T? = null) : ReadWriteProperty<Any, T> {
    private var value: T? = defaultValue

    protected abstract val receiver: R

    final override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value!!
    }

    final override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setter?.invoke(receiver, value)
        this.value = value
    }
}

private class ObjectPropertyDelegateNoGetter1<T, R>(override val receiver: R, setter: ((R, T) -> Unit)? = null, defaultValue: T? = null)
    : AbsObjectPropertyDelegateNoGetter1<T, R>(setter, defaultValue) {

    init {
        defaultValue?.let { setter?.invoke(receiver, it) }
    }
}

private class ObjectPropertyDelegateNoGetterLazy1<T, R>(receiverGetter: () -> R, setter: ((R, T) -> Unit)? = null, defaultValue: T? = null)
    : AbsObjectPropertyDelegateNoGetter1<T, R>(setter, defaultValue) {

    override val receiver by lazy {
        receiverGetter().also { receiver ->
            defaultValue?.let { setter?.invoke(receiver, it) }
        }
    }
}

private abstract class AbsObjectPropertyDelegate1<T, R>(val getter: ((R) -> T), val setter: ((R, T) -> Unit)? = null, defaultValue: T? = null) : ReadWriteProperty<Any, T> {

    protected abstract val receiver: R

    final override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return getter.invoke(receiver)
    }

    final override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        setter?.invoke(receiver, value)
    }
}

private class ObjectPropertyDelegate1<T, R>(override val receiver: R, getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, defaultValue: T? = null)
    : AbsObjectPropertyDelegate1<T, R>(getter, setter, defaultValue) {

    constructor(receiver: R, property: KProperty1<R, T>, defaultValue: T? = null)
            : this(receiver, property, if (property is KMutableProperty1<*, *>) (property as KMutableProperty1<R, T>)::set else null, defaultValue)

    init {
        defaultValue?.let { setter?.invoke(receiver, it) }
    }
}

private class ObjectPropertyDelegateLazy1<T, R>(receiverGetter: () -> R, getter: ((R) -> T), setter: ((R, T) -> Unit)? = null, defaultValue: T? = null)
    : AbsObjectPropertyDelegate1<T, R>(getter, setter, defaultValue) {

    constructor(receiverGetter: () -> R, property: KProperty1<R, T>, defaultValue: T? = null)
            : this(receiverGetter, property, if (property is KMutableProperty1<*, *>) (property as KMutableProperty1<R, T>)::set else null, defaultValue)

    override val receiver by lazy {
        receiverGetter().also { receiver ->
            defaultValue?.let { setter?.invoke(receiver, it) }
        }
    }
}