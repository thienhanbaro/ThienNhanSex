package com.example.spnew2

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class ShellUserService() : IShellService.Stub() {

    @Suppress("UNUSED_PARAMETER")
    constructor(context: Context) : this()

    override fun exec(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).readText()
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).readText()
            process.waitFor()
            (stdout + stderr).trim()
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }

    override fun destroy() {
        System.exit(0)
    }
}
