package com.experiment.facedetector.domain.index

/**
 * High-level contract for building and maintaining the media index.
 * Implemented in the data layer (using MediaSource + pipelines + repos).
 */
interface MediaIndexer {

    /**
     * Perform a full refresh of the media index:
     * - Page through the source
     * - Upsert media rows
     * - Generate thumbnails
     * - Extract and persist face embeddings
     */
    suspend fun refreshAll()

    /**
     * Refresh only a single page of media items.
     * Useful for incremental work or testing.
     *
     * @param offset Page start offset in the source.
     * @param limit  Number of items to fetch and process.
     */
    suspend fun refreshPage(offset: Int, limit: Int)

    /**
     * Process a single media item by its stable id from the source.
     * Useful for retrying failed items or targeted updates.
     *
     * @param mediaId The stable id of the media item in the index.
     * @return true if processed successfully, false otherwise.
     */
    suspend fun processSingleItem(mediaId: Long): Boolean
}
