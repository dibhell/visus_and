package com.visus.app.engine

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.view.Surface

class EglRecorderSurface(
    private val targetSurface: Surface,
    sharedContext: EGLContext
) {
    private val eglDisplay: EGLDisplay = requireNotNull(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)) { "No EGL display" }
    private val eglConfig: EGLConfig
    private val eglContext: EGLContext
    private val eglSurface: EGLSurface

    init {
        val version = IntArray(2)
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            0x3142, 1, // EGL_RECORDABLE_ANDROID
            EGL14.EGL_NONE
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, numConfigs, 0)
        eglConfig = configs[0] ?: throw IllegalStateException("No EGL config for recorder")

        val contextAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE)
        eglContext = EGL14.eglCreateContext(eglDisplay, eglConfig, sharedContext, contextAttribs, 0)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, targetSurface, intArrayOf(EGL14.EGL_NONE), 0)
    }

    fun draw(block: () -> Unit) {
        val previousDisplay = EGL14.eglGetCurrentDisplay()
        val previousDrawSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW)
        val previousReadSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_READ)
        val previousContext = EGL14.eglGetCurrentContext()

        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        block()
        EGL14.eglSwapBuffers(eglDisplay, eglSurface)

        EGL14.eglMakeCurrent(previousDisplay, previousDrawSurface, previousReadSurface, previousContext)
    }

    fun release() {
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)
        targetSurface.release()
    }
}
