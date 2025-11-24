package com.visus.app.engine

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.SystemClock
import com.visus.app.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class VisusRenderer(private val context: Context) : GLSurfaceView.Renderer {
    private var cameraSurfaceTexture: SurfaceTexture? = null
    private var oesTextureId: Int = -1
    private var viewportWidth: Int = 0
    private var viewportHeight: Int = 0
    private var programOes: ShaderProgram? = null
    private var programEffect: ShaderProgram? = null
    private val quadVertices = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )
    private var bandValues = floatArrayOf(0f, 0f, 0f)
    private var startTimeMs = SystemClock.elapsedRealtime()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)
        oesTextureId = createOesTexture()
        cameraSurfaceTexture = SurfaceTexture(oesTextureId).apply {
            setOnFrameAvailableListener {
                // Frames will be consumed on the GL thread in onDrawFrame.
            }
        }
    }

        val vertexSrc = context.resources.openRawResource(R.raw.shader_vertex).bufferedReader().use { it.readText() }
        val fragOes = context.resources.openRawResource(R.raw.shader_oes).bufferedReader().use { it.readText() }
        val fragBase = context.resources.openRawResource(R.raw.shader_base).bufferedReader().use { it.readText() }
        programOes = ShaderProgram(vertexSrc, fragOes)
        programEffect = ShaderProgram(vertexSrc, fragBase)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        GLES30.glViewport(0, 0, width, height)
        cameraSurfaceTexture?.setDefaultBufferSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        cameraSurfaceTexture?.updateTexImage()
        drawOes()
    }

    private fun drawOes() {
        val program = programOes ?: return
        program.use()
        val posLoc = program.getAttrib("aPosition")
        GLES30.glEnableVertexAttribArray(posLoc)
        val vb = java.nio.ByteBuffer.allocateDirect(quadVertices.size * 4).order(java.nio.ByteOrder.nativeOrder()).asFloatBuffer()
        vb.put(quadVertices).position(0)
        GLES30.glVertexAttribPointer(posLoc, 2, GLES30.GL_FLOAT, false, 0, vb)

        val time = (SystemClock.elapsedRealtime() - startTimeMs) / 1000f
        GLES30.glUniform1f(program.getUniform("uTime"), time)
        GLES30.glUniform2f(program.getUniform("uResolution"), viewportWidth.toFloat(), viewportHeight.toFloat())
        GLES30.glUniform3f(program.getUniform("uBands"), bandValues[0], bandValues[1], bandValues[2])

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES30.glUniform1i(program.getUniform("uTexture"), 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        GLES30.glDisableVertexAttribArray(posLoc)
    }

    fun getCameraSurfaceTexture(): SurfaceTexture? = cameraSurfaceTexture
    fun getViewportSize(): Pair<Int, Int> = viewportWidth to viewportHeight
    fun getOesTextureId(): Int = oesTextureId

    fun updateBands(bass: Float, mid: Float, high: Float) {
        bandValues[0] = bass
        bandValues[1] = mid
        bandValues[2] = high
    }

    private fun createOesTexture(): Int {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        val texId = textures[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        return texId
    }
}
