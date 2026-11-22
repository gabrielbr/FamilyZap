package app.oie.logic

/**
 * v0.53 (flow docs/flows/photo-zoom.md): the zoom of a photo shown on its own screen. Scale 1 is the whole photo,
 * fitted; the offset moves it, and is clamped so its edges never come inside the screen's edges.
 * Coordinates are in px, relative to the centre of the area the photo is shown in.
 */
object Zoom {
    const val MIN = 1f
    const val MAX = 4f
    /** What a double-tap zooms to. */
    const val DOUBLE_TAP = 2.5f

    data class State(val scale: Float = MIN, val x: Float = 0f, val y: Float = 0f) {
        val zoomed: Boolean get() = scale > MIN + 0.01f
    }

    /**
     * One step of a pinch or drag: [zoom] is the change in scale, ([cx], [cy]) the fingers' centre and ([dx], [dy])
     * the drag, both relative to the area's centre. The point under the fingers stays under them.
     * [w] and [h] are the photo's fitted size at scale 1; [areaW] and [areaH] the area's.
     */
    fun gesture(s: State, zoom: Float, cx: Float, cy: Float, dx: Float, dy: Float, w: Float, h: Float, areaW: Float, areaH: Float): State {
        val scale = (s.scale * zoom).coerceIn(MIN, MAX)
        val k = scale / s.scale
        val x = cx - (cx - s.x) * k + dx
        val y = cy - (cy - s.y) * k + dy
        return clamp(State(scale, x, y), w, h, areaW, areaH)
    }

    /** A double-tap at ([tx], [ty]): zoomed out, it zooms in around that point; zoomed in, back to the whole photo. */
    fun doubleTap(s: State, tx: Float, ty: Float, w: Float, h: Float, areaW: Float, areaH: Float): State =
        if (s.zoomed) State() else clamp(State(DOUBLE_TAP, tx * (1 - DOUBLE_TAP), ty * (1 - DOUBLE_TAP)), w, h, areaW, areaH)

    /** TalkBack's "Ampliar": zooms in around the centre. */
    fun zoomIn(s: State, w: Float, h: Float, areaW: Float, areaH: Float): State =
        clamp(State((s.scale * 2f).coerceIn(MIN, MAX), s.x * 2f, s.y * 2f), w, h, areaW, areaH)

    /** Keeps the photo's edges outside the area's edges (or centred on an axis where the photo is smaller). */
    fun clamp(s: State, w: Float, h: Float, areaW: Float, areaH: Float): State {
        val maxX = ((w * s.scale - areaW) / 2f).coerceAtLeast(0f)
        val maxY = ((h * s.scale - areaH) / 2f).coerceAtLeast(0f)
        return State(s.scale, s.x.coerceIn(-maxX, maxX), s.y.coerceIn(-maxY, maxY))
    }

    /** The photo's size fitted in the area (ContentScale.Fit). */
    fun fitted(photoW: Float, photoH: Float, areaW: Float, areaH: Float): Pair<Float, Float> {
        if (photoW <= 0f || photoH <= 0f) return areaW to areaH
        val k = minOf(areaW / photoW, areaH / photoH)
        return photoW * k to photoH * k
    }
}
