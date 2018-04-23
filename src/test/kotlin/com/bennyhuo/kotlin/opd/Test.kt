package com.bennyhuo.kotlin.opd

import org.junit.Assert
import org.junit.Test

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
    }
}

class Wrapper {
    private val wrapped: Wrapped = Wrapped(false)

    var x by wrapped::x.delegator()
    var y by wrapped::setY.delegator(defaultValue = 0)
    val yGetter by wrapped::getY.delegator()
    val z by wrapped::z.delegator()
}

class Wrapped(var x: Boolean) {
    val z = 10L
    fun setY(y: Int) {

    }

    fun getY() = 12
}