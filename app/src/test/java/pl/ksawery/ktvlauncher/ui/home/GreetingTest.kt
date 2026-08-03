package pl.ksawery.ktvlauncher.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {
    @Test
    fun `morning starts at five`() {
        assertEquals("Dzień dobry", greetingFor(5))
    }

    @Test
    fun `evening starts at eighteen`() {
        assertEquals("Dobry wieczór", greetingFor(18))
    }
}
