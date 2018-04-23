package com.bennyhuo.kotlin.opd

import kotlin.jvm.internal.PropertyReference
import kotlin.reflect.*

fun <T> delegateOf(getter: (() -> T)? = null, setter: ((T) -> Unit)? = null, defaultValue: T? = null) = ObjectPropertyDelegate0(getter, setter, defaultValue)
fun <T, R> delegateWithReceiverOf(receiver: R, getter: ((R) -> T)? = null, setter: ((R, T) -> Unit)? = null, defaultValue: T? = null) = ObjectPropertyDelegate1(receiver, getter, setter, defaultValue)

fun <T> KProperty0<T>.delegator(defaultValue: T? = null) = ObjectPropertyDelegate0(propertyRef = this as PropertyReference, defaultValue = defaultValue)
fun <T, R> KProperty1<R, T>.delegator(receiver: R, defaultValue: T? = null) = ObjectPropertyDelegate1(receiver, property = this, defaultValue = defaultValue)
fun <T> KFunction1<T, Unit>.delegator(defaultValue: T? = null) = ObjectPropertyDelegate0(setter = this, defaultValue = defaultValue)
fun <T, R> KFunction2<R, T, Unit>.delegator(receiver: R, defaultValue: T? = null) = ObjectPropertyDelegate1(receiver, setter = this, defaultValue = defaultValue)

class ObjectPropertyDelegate0<T>(val getter: (() -> T)? = null, val setter: ((T) -> Unit)? = null, defaultValue: T? = null) {

    constructor(propertyRef: PropertyReference, defaultValue: T? = null)
            : this((propertyRef as KProperty0<T>)::get, if (propertyRef is KMutableProperty0<*>) (propertyRef as KMutableProperty0<T>)::set else null, defaultValue)

    private var value: T? = defaultValue

    operator fun getValue(ref: Any, property: KProperty<*>): T {
        return getter?.invoke() ?: value!!
    }

    operator fun setValue(ref: Any, property: KProperty<*>, value: T) {
        setter?.invoke(value)
        this.value = value
    }
}

class ObjectPropertyDelegate1<T, R>(val receiver: R, val getter: ((R) -> T)? = null, val setter: ((R, T) -> Unit)? = null, defaultValue: T? = null) {

    constructor(target: R, property: KProperty1<R, T>, defaultValue: T? = null)
            : this(target, property, if (property is KMutableProperty1<*, *>) (property as KMutableProperty1<R, T>)::set else null, defaultValue)

    private var value: T? = defaultValue

    operator fun getValue(ref: Any, property: KProperty<*>): T {
        return getter?.invoke(receiver) ?: value!!
    }

    operator fun setValue(ref: Any, property: KProperty<*>, value: T) {
        setter?.invoke(receiver, value)
        this.value = value
    }
}