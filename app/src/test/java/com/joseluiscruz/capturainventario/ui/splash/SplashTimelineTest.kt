package com.joseluiscruz.capturainventario.ui.splash

import org.junit.Assert.assertEquals
import org.junit.Test

class SplashTimelineTest {

    @Test
    fun segmentClampsValuesOutsideItsRange() {
        assertEquals(0f, SplashTimeline.segment(0.10f, 0.20f, 0.60f), 0.0001f)
        assertEquals(1f, SplashTimeline.segment(0.80f, 0.20f, 0.60f), 0.0001f)
    }

    @Test
    fun segmentInterpolatesValuesInsideItsRange() {
        assertEquals(0.5f, SplashTimeline.segment(0.40f, 0.20f, 0.60f), 0.0001f)
    }

    @Test
    fun introUsesTheExpectedDuration() {
        assertEquals(3_500, SplashTimeline.DURATION_MILLIS)
    }
}
