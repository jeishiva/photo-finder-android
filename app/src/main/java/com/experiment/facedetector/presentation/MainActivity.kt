package com.experiment.facedetector.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.experiment.facedetector.navigation.AppNavGraph
import com.experiment.facedetector.presentation.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.viewmodel.AppViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startMediaScanning()
        setContent {
            AppEntryPoint()
        }
    }

    fun startMediaScanning() {
        appViewModel.startMediaScanning()
    }
}

@Preview(showBackground = true)
@Composable
fun AppEntryPoint() {
    AndroidFaceDetectorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainApp()
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    AndroidFaceDetectorTheme {
        AppNavGraph(navController = navController)
    }
}

