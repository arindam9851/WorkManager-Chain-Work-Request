package com.example.blurimageworkmanager

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.lang.Thread.sleep

class CleanupWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {


    override fun doWork(): Result {
        val appContext=applicationContext
        makeStatusNotification("Cleaning up old temporary files",appContext)
        Thread.sleep(4000)
        return try {
            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)
            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries != null) {
                    for (entry in entries) {
                        val name = entry.name
                        if (name.isNotEmpty() && name.endsWith(".png")) {
                            val deleted = entry.delete()
                            println("Delete: $name - $deleted")
                        }
                    }
                }
            }
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }

    }
}