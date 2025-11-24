package com.visus.app.engine

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Size
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraController(context: Context) {
    private val appContext = context.applicationContext
    private var cameraProvider: ProcessCameraProvider? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    fun startCamera(owner: LifecycleOwner, surfaceTexture: SurfaceTexture, size: Size = Size(1280, 720)) {
        surfaceTexture.setDefaultBufferSize(size.width, size.height)
        val surface = Surface(surfaceTexture)
        val futureProvider = ProcessCameraProvider.getInstance(appContext)
        futureProvider.addListener({
            val provider = futureProvider.get().also { cameraProvider = it }
            provider.unbindAll()
            val preview = Preview.Builder()
                .setTargetResolution(size)
                .build()
            preview.setSurfaceProvider { request ->
                request.provideSurface(surface, executor) { }
            }
            provider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
        }, ContextCompat.getMainExecutor(appContext))
    }

    fun stopCamera() {
        cameraProvider?.unbindAll()
    }

    fun shutdown() {
        executor.shutdown()
    }
}
