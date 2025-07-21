package com.experiment.facedetector.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.experiment.facedetector.image.BitmapHelper
import com.experiment.facedetector.common.toFaceId
import com.experiment.facedetector.config.FullImageConfig
import com.experiment.facedetector.face.FaceDetectionProcessor
import com.experiment.facedetector.data.local.entities.FaceEntity
import com.experiment.facedetector.domain.entities.FaceTag
import com.experiment.facedetector.domain.entities.FullImageWithFaces
import com.experiment.facedetector.domain.repo.MediaRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FullImageViewModel(
    savedStateHandle: SavedStateHandle,
    private val faceDetectionProcessor: FaceDetectionProcessor,
    private val imageHelper: BitmapHelper,
    private val mediaRepo: MediaRepo,
) : ViewModel() {
    private val _fullImageResult = MutableStateFlow<FullImageWithFaces?>(null)
    val fullImageResult: StateFlow<FullImageWithFaces?> = _fullImageResult
    private val mediaId : Long = savedStateHandle["mediaId"] ?: error("Missing mediaId")

    init {
        init()
    }

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            val mediaEntity = mediaRepo.getMedia(mediaId)
            val savedFaceTags = mediaRepo.getFaces(mediaId)
            val bitmap = imageHelper.decodeBitmap(
                mediaEntity.contentUri.toUri(), FullImageConfig.MAX_HEIGHT,
                FullImageConfig.MAX_WIDTH
            )
            val faces = faceDetectionProcessor.detectFaces(bitmap)
            val faceTags = faces.map {
                val faceId = it.toFaceId(mediaId)
                val savedFaceEntity = savedFaceTags.find { it.faceId == faceId }
                FaceTag(
                    id = faceId,
                    left = it.boundingBox.left,
                    top = it.boundingBox.top,
                    right = it.boundingBox.right,
                    bottom = it.boundingBox.bottom,
                    height = it.boundingBox.height(),
                    width = it.boundingBox.width(),
                    tag = savedFaceEntity?.tag ?: "",
                    savedFaceEntity
                )
            }
            val result = FullImageWithFaces(mediaId, bitmap, faceTags)
            _fullImageResult.value = result
        }
    }

    fun saveFaceTag(faceTag: FaceTag, newTag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedFaceEntity = faceTag.savedFaceEntity?.copy(tag = newTag)
                ?: FaceEntity(
                    mediaId = mediaId,
                    faceId = faceTag.id,
                    tag = newTag
                )
            mediaRepo.insertOrUpdateFace(updatedFaceEntity)
            _fullImageResult.update { current ->
                current?.copy(
                    faces = current.faces.map {
                        if (it.id == faceTag.id) {
                            it.copy(tag = newTag, savedFaceEntity = updatedFaceEntity)
                        } else it
                    }
                )
            }
        }
    }
}



