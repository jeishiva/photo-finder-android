package com.experiment.facedetector.common

import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.android.gms.tasks.Task
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (cont.isActive) cont.cancel()
    }
}


fun FloatArray.toByteArray(): ByteArray {
    val buffer = ByteBuffer.allocate(this.size * 4)
    buffer.order(ByteOrder.nativeOrder())
    this.forEach { buffer.putFloat(it) }
    return buffer.array()
}

fun ByteArray.toFloatArray(): FloatArray {
    val buffer = ByteBuffer.wrap(this)
    buffer.order(ByteOrder.nativeOrder())
    val floatArray = FloatArray(this.size / 4)
    for (i in floatArray.indices) {
        floatArray[i] = buffer.getFloat(i * 4)
    }
    return floatArray
}
