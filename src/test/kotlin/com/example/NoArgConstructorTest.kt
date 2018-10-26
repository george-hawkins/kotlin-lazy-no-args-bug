package com.example

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NoArgConstructorTest {
    private val json = """
       {
            "@id": 1,
            "beta": {
                "@id": 2,
                "alpha": 1
            }
        }
        """
    private val mapper = jacksonObjectMapper()

    // -----------------------------------------------------------------

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator::class, property="@id")
    private class Alpha1(var beta: Beta1?)

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator::class, property="@id")
    private class Beta1(val alpha: Alpha1)

    @Test
    fun `test plugin created no-arg constructors`() {
        val alpha = mapper.readValue(json, Alpha1::class.java)

        assertNotNull(alpha.beta)
    }

    // -----------------------------------------------------------------

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator::class, property="@id")
    private class Alpha2() {
        constructor(beta: Beta2) : this() { this.beta = beta }

        var beta: Beta2? = null
    }

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator::class, property="@id")
    private class Beta2() {
        constructor(alpha: Alpha2): this() { this.alpha = alpha }

        var alpha: Alpha2? = null
    }

    @Test
    fun `test hand coded no-arg constructors`() {
        val alpha = mapper.readValue(json, Alpha2::class.java)

        assertNotNull(alpha.beta)
    }

    // -----------------------------------------------------------------

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator::class, property="@id")
    private class Alpha3(var beta: Beta3?) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun creator() = Alpha3::class.java.getConstructor().newInstance()
        }
    }

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator::class, property="@id")
    private class Beta3(val alpha: Alpha3) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun creator() = Beta3::class.java.getConstructor().newInstance()
        }
    }

    @Test
    fun `test manipulating KotlinModule behavior`() {
        val alpha = mapper.readValue(json, Alpha3::class.java)

        assertNotNull(alpha.beta)
    }

    // -----------------------------------------------------------------

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator::class, property="@id")
    private class Alpha4(var beta: Beta4?) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun creator() = UnsupportedOperationException()
        }
    }

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator::class, property="@id")
    private class Beta4(val alpha: Alpha4) {
        companion object {
            @JvmStatic
            @JsonCreator
            fun creator() = UnsupportedOperationException()
        }
    }

    @Test
    fun `test exception throwing creators`() {
        val alpha = mapper.readValue(json, Alpha4::class.java)

        assertNotNull(alpha.beta)
    }

    // -----------------------------------------------------------------

    @Test
    fun `test that Alpha1, Alpha2 etc have the same constructors`() {
        val beta1 = getCleanedUpConstructorSignatures(Beta1::class.java)
        val beta2 = getCleanedUpConstructorSignatures(Beta2::class.java)
        val beta3 = getCleanedUpConstructorSignatures(Beta3::class.java)
        val beta4 = getCleanedUpConstructorSignatures(Beta4::class.java)
        val constructors = listOf(beta1, beta2, beta3, beta4)

        constructors.forEach {
            assertEquals(2, it.size)
        }

        constructors.drop(1).forEach {
            assertEquals(beta1, it)
        }
    }

    private fun getCleanedUpConstructorSignatures(clazz: Class<*>): List<String> {
        println(clazz.simpleName)
        println(clazz.declaredConstructors.joinToString("\n* ", "* "))
        println("--")

        // Strip out the digits that distinguish Alpha1, Alpha2 etc.
        return clazz.declaredConstructors.map { it.toString().replace("[0-9]".toRegex(), "") }
    }
}