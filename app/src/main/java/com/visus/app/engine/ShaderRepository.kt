package com.visus.app.engine

import android.content.Context

class ShaderRepository(private val context: Context) {
    // Placeholder: will map shader ids from the TypeScript SHADER_LIST into Android-compatible GLSL files.
    fun loadDefaultShader(): String = """
        #version 300 es
        precision mediump float;
        out vec4 fragColor;
        void main() {
            fragColor = vec4(0.07, 0.1, 0.2, 1.0);
        }
    """.trimIndent()

    fun loadShader(name: String): String {
        // TODO: read shader source from assets/res/raw and apply required header replacements.
        return loadDefaultShader()
    }
}
