package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel

class DetailsViewModel : ViewModel() {

    init {
        hotspot()
    }

    private fun hotspot() {
        val hashmap = HashMap<String, String>()
        for (index in 0..1000000) {
            hashmap["$index"] = "2342342334234 + $index"
        }
    }
}