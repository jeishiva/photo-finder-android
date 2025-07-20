package com.experiment.facedetector.ui

sealed class TimeRange(val label: String) {
    object OneMonth : TimeRange("1 Month")
    object ThreeMonths : TimeRange("3 Months")
    object SixMonths : TimeRange("6 Months")
    object TwelveMonths : TimeRange("12 Months")

    companion object {
        fun toList(): List<TimeRange> = listOf(
            OneMonth,
            ThreeMonths,
            SixMonths,
            TwelveMonths
        )
    }
}

