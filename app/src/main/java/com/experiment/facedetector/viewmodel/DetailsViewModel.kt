package com.experiment.facedetector.viewmodel

import androidx.compose.ui.util.trace
import androidx.lifecycle.ViewModel

class DetailsViewModel : ViewModel() {
    val hashmap = HashMap<String, String>()
    init {
        hotspot()
    }

    private fun hotspot() {
        trace("Hotspot Loop Trace") {

        }
    }
}