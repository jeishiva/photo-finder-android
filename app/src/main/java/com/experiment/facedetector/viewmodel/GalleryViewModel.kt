package com.experiment.facedetector.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.experiment.facedetector.common.CAMERA_WORKER_TAG
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.data.local.worker.CameraImageWorker
import com.experiment.facedetector.domain.entities.ProcessedMediaItem
import com.experiment.facedetector.domain.repo.MediaRepo
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 *  work manager triggers camera processor from here
 *  currently work manager state and page source state are not unified
 *  todo : unify both state and expose to UI
 */

class GalleryViewModel(
    mediaRepo: MediaRepo,
    private val workManager: WorkManager
) : ViewModel() {

    private val _workInfoStateFlow = MutableStateFlow<List<WorkInfo>>(emptyList())
    val isWorkerRunning: StateFlow<Boolean> = _workInfoStateFlow
        .map { workInfos -> workInfos.any { it.state == WorkInfo.State.RUNNING } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = _workInfoStateFlow.value.any { it.state == WorkInfo.State.RUNNING }
        )
    init {
        observeWorkStatus()
    }

    @OptIn(FlowPreview::class)
    val userImageFlow: Flow<PagingData<ProcessedMediaItem>> =
        mediaRepo.getPagedMedia()
            .cachedIn(viewModelScope)
            .debounce(200)
            .onEach {
                LogManager.d("GalleryViewModel", "New paging data emitted")
            }

    private fun observeWorkStatus() {
        workManager
            .getWorkInfosByTagLiveData(WORK_TAG)
            .asFlow()
            .onEach {
                _workInfoStateFlow.value = it
                LogManager.d("GalleryViewModel", "Work status: ${it.map { it.state }}")
            }
    }

    fun startInitialWork() {
        val workRequest = OneTimeWorkRequestBuilder<CameraImageWorker>()
            .addTag(CAMERA_WORKER_TAG)
            .build()
        workManager.enqueueUniqueWork(
            CAMERA_WORKER_TAG,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    companion object {
        const val PAGE_SIZE = 10
        const val INITIAL_LOAD_SIZE = 20
        const val PREFETCH_DISTANCE = 10
        private const val WORK_TAG = "camera_image_work"
    }
}


