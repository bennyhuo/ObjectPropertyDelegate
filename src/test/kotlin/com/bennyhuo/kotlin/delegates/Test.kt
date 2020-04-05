package com.bennyhuo.kotlin.delegates

import org.junit.Assert
import org.junit.Test
import java.lang.AssertionError

class Test{
    @Test
    fun test() {
        val wrapper = Wrapper()
        Assert.assertTrue(!wrapper.x)
        wrapper.x = true
        Assert.assertEquals(wrapper.y, 0)
        wrapper.y = 1
        Assert.assertTrue(wrapper.x)
        Assert.assertEquals(wrapper.y, 1)
        Assert.assertEquals(wrapper.yGetter, 12)
        Assert.assertEquals(wrapper.z, 10)

        Assert.assertFalse(wrapper.wrappedLazyInitialized)
        Assert.assertTrue(wrapper.xLazy)
        Assert.assertTrue(wrapper.wrappedLazyInitialized)

        Assert.assertEquals(wrapper.z, 10)
    }
}

class Wrapper {
    private val wrapped: Wrapped = Wrapped(false)

    var wrappedLazyInitialized = false
    private val wrappedLazy by lazy {
        wrappedLazyInitialized = true
        Wrapped(true)
    }

    var x by wrapped::x.delegator()
    var y by wrapped::setY.delegator(defaultValue = 0)
    val yGetter by wrapped::getY.delegator()
    val z by wrapped::z.delegator()

    var x0 by delegateOf(wrapped::x, wrapped::x::set)
    var x1 by delegateOf(wrapped::setY)

    var y1 by delegateWithReceiverOf(wrapped, Wrapped::setY)
    var y2 by delegateOf(wrapped, Wrapped::setY)

    var y3 by delegateWithReceiverOf(wrapped, Wrapped::getY, Wrapped::setY)
    var y4 by delegateOf(wrapped, Wrapped::getY, Wrapped::setY)

    var xLazy by delegateLazyOf(Wrapped::x, Wrapped::x::set, false) {
        wrappedLazy
    }
}

class Wrapped(var x: Boolean) {
    val z = 10L
    fun setY(y: Int) {

    }

    fun getY() = 12
}