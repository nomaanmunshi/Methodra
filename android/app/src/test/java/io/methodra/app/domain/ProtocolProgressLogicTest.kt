package io.methodra.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolProgressLogicTest {
    @Test
    fun `step completion toggles without streak semantics`() {
        assertEquals("2", ProtocolProgressLogic.toggleCompleted(null, 2))
        assertEquals("1,2", ProtocolProgressLogic.toggleCompleted("2", 1))
        assertEquals("1", ProtocolProgressLogic.toggleCompleted("1,2", 2))
    }

    @Test
    fun `ratings are bounded and recovery is only prompted for difficult days`() {
        assertEquals(1, ProtocolProgressLogic.normalizedRating(-4))
        assertEquals(5, ProtocolProgressLogic.normalizedRating(99))
        assertTrue(ProtocolProgressLogic.recoveryIsUseful(2))
        assertFalse(ProtocolProgressLogic.recoveryIsUseful(3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative step indexes are rejected`() {
        ProtocolProgressLogic.toggleCompleted("", -1)
    }
}
