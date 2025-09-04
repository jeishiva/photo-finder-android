package com.experiment.facedetector.common.permission

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

@Composable
fun RequestPermission(
    permissions: List<String>,
    onResult: ((Boolean) -> Unit)
) {
    var launched by remember { mutableStateOf(false) }
    val currentOnResult by rememberUpdatedState(onResult)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        currentOnResult(allGranted)
    }
    LaunchedEffect(Unit) {
        if (!launched) {
            permissionLauncher.launch(permissions.toTypedArray())
            launched = true
        }
    }
}

