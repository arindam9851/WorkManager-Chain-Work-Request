package com.example.blurimageworkmanager

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.FileNotFoundException
import java.lang.IllegalArgumentException
import java.lang.RuntimeException

class LittleBlurImageWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {


    override fun doWork(): Result {
        val appContext=applicationContext
        val imageUri=inputData.getString(KEY_IMAGE_URI)
        makeStatusNotification("BlurringImage",applicationContext)
        Thread.sleep(4000)

        return try {

            val outputData=createBlurImage(appContext,imageUri)
            Result.success(outputData)

        }
        catch (fileNotFoundException : FileNotFoundException)
        {
            throw RuntimeException("Failed to decode input stream",fileNotFoundException)
        }

    }

    private fun createBlurImage(appContext: Context, resourceUri: String?): Data {

        if(resourceUri.isNullOrEmpty()){
            throw IllegalArgumentException("Invalid Uri")
        }
        val resolver = appContext.contentResolver

        val picture = BitmapFactory.decodeStream(
            resolver.openInputStream(Uri.parse(resourceUri)))

        val output = blurBitmap(picture, appContext)

        // Write bitmap to a temp file
        val outputUri = writeBitmapToFile(appContext, output)

        val outputData= workDataOf(KEY_IMAGE_URI to outputUri.toString())
        return outputData
    }


}