package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VideoRulesTest {
    @Test fun upToThreeMinutes() {
        assertEquals(VideoRules.Check.OK, VideoRules.length(180_000))
        assertEquals(VideoRules.Check.OK, VideoRules.length(180_900))
        assertEquals(VideoRules.Check.TOO_LONG, VideoRules.length(182_000))
    }

    @Test fun sizeAfterShrinking() {
        assertEquals(VideoRules.Check.OK, VideoRules.size(VideoRules.MAX_BYTES))
        assertEquals(VideoRules.Check.TOO_BIG, VideoRules.size(VideoRules.MAX_BYTES + 1))
    }

    @Test fun shrinksTo720pKeepingTheShape() {
        assertEquals(1280 to 720, VideoRules.target(1920, 1080, 0))
        // A portrait video recorded sideways: shown 1080×1920.
        assertEquals(720 to 1280, VideoRules.target(1920, 1080, 90))
        assertEquals(720 to 960, VideoRules.target(1440, 1920, 0))
        assertNull(VideoRules.target(1280, 720, 0))
        assertNull(VideoRules.target(640, 480, 0))
    }

    @Test fun gifsBecomeSmallEvenFrames() {
        assertEquals(480 to 270, VideoRules.gifSize(1000, 562))
        assertEquals(200 to 150, VideoRules.gifSize(201, 151))
        assertEquals(2 to 2, VideoRules.gifSize(1, 1))
        assertEquals(30, VideoRules.gifFrames(2_000))
        assertEquals(1, VideoRules.gifFrames(0))
        assertEquals(VideoRules.GIF_MAX_MS / 1000 * VideoRules.GIF_FPS, VideoRules.gifFrames(90_000))
    }
}
