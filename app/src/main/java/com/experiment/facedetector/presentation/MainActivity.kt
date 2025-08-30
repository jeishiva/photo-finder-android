package com.experiment.facedetector.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.experiment.facedetector.navigation.AppNavGraph
import com.experiment.facedetector.presentation.entities.AppUiModel
import com.experiment.facedetector.presentation.theme.AndroidFaceDetectorTheme
import com.experiment.facedetector.viewmodel.AppViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by inject()
    val appUiModel = AppUiModel(
        actions = AppUiModel.Actions(
            onPermissionGranted = {
                startMediaScanning()
            }
        )
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppEntryPoint(appUiModel)
        }
    }

    fun startMediaScanning() {
        appViewModel.startMediaScanning()
    }

    companion object {
        const val TAG = "MainActivity"
    }
}

@Composable
fun AppEntryPoint(appUiModel: AppUiModel) {
    AndroidFaceDetectorTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainApp(appUiModel)
        }
    }
}

@Composable
fun MainApp(appUiModel: AppUiModel) {
    val navController = rememberNavController()
    AndroidFaceDetectorTheme {
        AppNavGraph(
            navController = navController,
            appUiModel = appUiModel
        )
    }
}
