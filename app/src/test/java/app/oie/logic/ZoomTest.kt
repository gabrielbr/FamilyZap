package app.oie.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoomTest {
    // A 1000×750 area; the photo fits as 1000×750 (same shape).
    private val w = 1000f; private val h = 750f

    @Test fun pinchingOutGrowsUpToFourTimes() {
        var s = Zoom.State()
        repeat(10) { s = Zoom.gesture(s, 1.5f, 0f, 0f, 0f, 0f, w, h, w, h) }
        assertEquals(Zoom.MAX, s.scale, 0.001f)
    }

    @Test fun pinchingInNeverGoesBelowTheWholePhoto() {
        val s = Zoom.gesture(Zoom.State(), 0.5f, 0f, 0f, 0f, 0f, w, h, w, h)
        assertEquals(Zoom.MIN, s.scale, 0.001f)
        assertFalse(s.zoomed)
    }

    @Test fun thePointUnderTheFingersStaysUnderThem() {
        // Pinch around (200, 100) from the centre: that point of the photo must stay at (200, 100).
        val s = Zoom.gesture(Zoom.State(), 2f, 200f, 100f, 0f, 0f, w, h, w, h)
        // The photo point p maps to p * scale + offset: the points under the fingers stay put.
        assertEquals(200f, 200f * s.scale + s.x, 0.001f)
        assertEquals(100f, 100f * s.scale + s.y, 0.001f)
        assertEquals(-200f, s.x, 0.001f)
        assertEquals(-100f, s.y, 0.001f)
    }

    @Test fun draggingStopsAtThePhotosEdges() {
        val zoomed = Zoom.State(2f, 0f, 0f)
        val s = Zoom.gesture(zoomed, 1f, 0f, 0f, 5000f, -5000f, w, h, w, h)
        // At 2× the photo is 2000×1500 in a 1000×750 area: it can move 500 and 375 each way.
        assertEquals(500f, s.x, 0.001f)
        assertEquals(-375f, s.y, 0.001f)
    }

    @Test fun atTheWholePhotoADragDoesNothing() {
        val s = Zoom.gesture(Zoom.State(), 1f, 0f, 0f, 300f, 300f, w, h, w, h)
        assertEquals(0f, s.x, 0.001f); assertEquals(0f, s.y, 0.001f)
    }

    @Test fun aNarrowPhotoStaysCentredWhereItIsSmallerThanTheArea() {
        // A tall photo fitted as 375×750 in the 1000×750 area, at 2× it is 750 wide: still narrower than the area.
        val (fw, fh) = Zoom.fitted(600f, 1200f, w, h)
        assertEquals(375f, fw, 0.001f); assertEquals(750f, fh, 0.001f)
        val s = Zoom.gesture(Zoom.State(2f, 0f, 0f), 1f, 0f, 0f, 400f, 0f, fw, fh, w, h)
        assertEquals(0f, s.x, 0.001f)
    }

    @Test fun doubleTapZoomsInAroundThePointAndBackOut() {
        val zin = Zoom.doubleTap(Zoom.State(), 100f, 50f, w, h, w, h)
        assertEquals(Zoom.DOUBLE_TAP, zin.scale, 0.001f)
        assertTrue(zin.zoomed)
        // The tapped point stays where it was: 100 * 2.5 + x = 100.
        assertEquals(100f, 100f * zin.scale + zin.x, 0.001f)
        assertEquals(50f, 50f * zin.scale + zin.y, 0.001f)
        assertEquals(Zoom.State(), Zoom.doubleTap(zin, 0f, 0f, w, h, w, h))
    }

    @Test fun talkBacksZoomInDoublesUpToTheLimit() {
        var s = Zoom.State()
        s = Zoom.zoomIn(s, w, h, w, h); assertEquals(2f, s.scale, 0.001f)
        s = Zoom.zoomIn(s, w, h, w, h); assertEquals(4f, s.scale, 0.001f)
        s = Zoom.zoomIn(s, w, h, w, h); assertEquals(4f, s.scale, 0.001f)
    }
}
