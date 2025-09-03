package com.experiment.facedetector.data.local.repo

import androidx.paging.PagingData
import com.experiment.facedetector.domain.entities.MediaWithFacesDomain
import kotlinx.coroutines.flow.Flow


interface MediaPagingRepository {

    fun getGallerySourceFlow(pageSize: Int): Flow<PagingData<MediaWithFacesDomain>>

    fun getSearchFaceFlow(pageSize: Int): Flow<PagingData<MediaWithFacesDomain>>
}
