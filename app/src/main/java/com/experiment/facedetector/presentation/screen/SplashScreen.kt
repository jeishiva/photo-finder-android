package com.experiment.facedetector.presentation.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.experiment.facedetector.R
import com.experiment.facedetector.common.permission.RequestPermission
import com.experiment.facedetector.presentation.theme.AndroidFaceDetectorTheme
import android.provider.Settings
import com.experiment.facedetector.navigation.NavigationManager
import com.experiment.facedetector.presentation.app.model.AppUiModel
import com.experiment.facedetector.viewmodel.SplashViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashScreen(navigationManager: NavigationManager, appUiModel: AppUiModel) {
    var showPermissionRequest by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val viewModel: SplashViewModel = koinViewModel()

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        showPermissionRequest = true
    }

    AndroidFaceDetectorTheme {
        Scaffold(
            containerColor = Color.Transparent, modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                SplashContent(
                    showPermissionRequest = showPermissionRequest,
                    permissionGranted = permissionGranted,
                    onPermissionResult = { granted ->
                        permissionGranted = granted
                        if (granted) {
                            appUiModel.actions.onPermissionGranted()
                            navigationManager.navigateToGallery()
                        } else {
                            openAppSettingsWithToast(context)
                        }
                    })
            }
        }
    }
}

@Composable
fun SplashContent(
    showPermissionRequest: Boolean,
    permissionGranted: Boolean,
    onPermissionResult: (Boolean) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )
        if (showPermissionRequest && !permissionGranted) {
            Spacer(modifier = Modifier.height(16.dp))
            RequestPermission(
                permissions = listOf(readImagePermission()), onResult = onPermissionResult
            )
        }
    }
}

fun openAppSettingsWithToast(context: Context) {
    Toast.makeText(
        context, context.getString(R.string.image_permission_denied), Toast.LENGTH_LONG
    ).show()
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
    closeActivityButton(context)
}

fun closeActivityButton(context: Context) {
    val activity = context as? Activity
    activity?.finish()
}

fun readImagePermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        android.Manifest.permission.READ_MEDIA_IMAGES
    } else {
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
