package com.experiment.facedetector.core.image


data class SizeF(val width: Float, val height: Float)
data class PointF(val x: Float, val y: Float)
data class RectF(val left: Float, val top: Float, val right: Float, val bottom: Float)

class ImageCoordinateHelper(
    private val containerWidthPx: Float,
    private val containerHeightPx: Float,
    private val imageWidthPx: Float,
    private val imageHeightPx: Float
) {
    val scale: Float = minOf(
        containerWidthPx / imageWidthPx,
        containerHeightPx / imageHeightPx
    )
    val offsetX: Float = (containerWidthPx - imageWidthPx * scale) / 2f
    val offsetY: Float = (containerHeightPx - imageHeightPx * scale) / 2f

    fun imageToContainerCoords(x: Float, y: Float): Pair<Float, Float> {
        val mappedX = offsetX + x * scale
        val mappedY = offsetY + y * scale
        return mappedX to mappedY
    }

    fun scaleWidth(width: Float): Float = width * scale
    fun scaleHeight(height: Float): Float = height * scale
}