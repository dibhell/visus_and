package com.visus.app.engine

import android.content.Context
import androidx.annotation.RawRes
import com.visus.app.R

class ShaderRepository(private val context: Context) {
    private val shaderList = listOf(
        ShaderDefinition("00_NONE", 0, R.raw.shader_base),
        ShaderDefinition("1_RGB_SHIFT", 1, R.raw.shader_base),
        ShaderDefinition("2_INVERT_COLOR", 2, R.raw.shader_base),
        ShaderDefinition("3_GLITCH_LINES", 3, R.raw.shader_base),
        ShaderDefinition("4_PIXELATE", 4, R.raw.shader_base)
    )

    fun getByName(name: String): ShaderDefinition? = shaderList.find { it.name == name }

    fun load(@RawRes resId: Int): String {
        return context.resources.openRawResource(resId).bufferedReader().use { it.readText() }
    }
}
