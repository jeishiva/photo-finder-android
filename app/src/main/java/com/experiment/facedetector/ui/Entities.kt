package com.experiment.facedetector.ui

sealed class TimeRangeOption(val label: String) {
    object OneMonth : TimeRangeOption("1 Month")
    object ThreeMonths : TimeRangeOption("3 Months")
    object SixMonths : TimeRangeOption("6 Months")
    object TwelveMonths : TimeRangeOption("12 Months")

    companion object {
        fun toList(): List<TimeRangeOption> = listOf(
            OneMonth,
            ThreeMonths,
            SixMonths,
            TwelveMonths
        )
    }
}