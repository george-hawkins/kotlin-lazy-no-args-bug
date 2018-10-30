package com.example

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlin.test.Test
import kotlin.test.assertEquals

class LazyUnmarshallTest {
    @NoArg
    private class Foo(var name: String) {
        @get:JsonIgnore val lazyName by lazy { "lazy $name" }
    }

    @Test fun `test normal construction`() {
        val foo = Foo("alpha")

        foo.name = "beta"

        assertEquals("beta", foo.name)
        assertEquals("lazy beta", foo.lazyName)
    }

    @Test fun `test no-args construction`() {
        val foo = Foo::class.java.getConstructor().newInstance()

        foo.name = "beta"

        assertEquals("beta", foo.name)

        // Internally Foo has a property lazyName$delegate of type kotlin.SynchronizedLazyImpl
        // However the no-args constructor doesn't initialize this property - even though it doesn't depend on any constructor arguments.
        // So this assertion fails due to a NPE accessing lazyName$delegate.
        assertEquals("lazy beta", foo.lazyName)
    }
}