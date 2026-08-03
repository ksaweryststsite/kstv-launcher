package pl.ksawery.ktvlauncher.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {
    @Test
    fun `morning starts at five`() {
        assertEquals("Dzień dobry", greetingFor(5))
    }

    @Test
    fun `day greeting remains at eighteen`() {
        assertEquals("Dzień dobry", greetingFor(18))
    }

    @Test
    fun `evening starts at twenty`() {
        assertEquals("Dobry wieczór", greetingFor(20))
    }
}
