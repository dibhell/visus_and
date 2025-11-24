package com.visus.app.engine

import android.opengl.GLES30

class ShaderProgram(
    private val vertexSrc: String,
    private val fragmentSrc: String
) {
    val programId: Int = GLES30.glCreateProgram()
    val attributes = mutableMapOf<String, Int>()
    val uniforms = mutableMapOf<String, Int>()

    init {
        val vId = compileShader(GLES30.GL_VERTEX_SHADER, vertexSrc)
        val fId = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSrc)
        GLES30.glAttachShader(programId, vId)
        GLES30.glAttachShader(programId, fId)
        GLES30.glLinkProgram(programId)
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val log = GLES30.glGetProgramInfoLog(programId)
            GLES30.glDeleteProgram(programId)
            throw RuntimeException("Program link failed: $log")
        }
        GLES30.glDeleteShader(vId)
        GLES30.glDeleteShader(fId)
    }

    fun use() {
        GLES30.glUseProgram(programId)
    }

    fun getUniform(name: String): Int {
        return uniforms.getOrPut(name) { GLES30.glGetUniformLocation(programId, name) }
    }

    fun getAttrib(name: String): Int {
        return attributes.getOrPut(name) { GLES30.glGetAttribLocation(programId, name) }
    }

    private fun compileShader(type: Int, src: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, src)
        GLES30.glCompileShader(shader)
        val status = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            val log = GLES30.glGetShaderInfoLog(shader)
            GLES30.glDeleteShader(shader)
            throw RuntimeException("Shader compile failed: $log")
        }
        return shader
    }
}
