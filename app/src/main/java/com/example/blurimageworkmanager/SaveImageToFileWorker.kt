package com.example.blurimageworkmanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class SaveImageToFileWorker(context: Context, workerParams: WorkerParameters):Worker(context, workerParams) {

    private val Title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat(
            "yyyy.MM.dd 'at' HH:mm:ss z",
            Locale.getDefault()
    )

    override fun doWork(): Result {

        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Saving image", applicationContext)
        Thread.sleep(4000)

        val resolver = applicationContext.contentResolver
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)))

            val imageUrl= saveImageToExternalStorage(bitmap)

            if (!imageUrl.isNullOrEmpty()) {

                val outputData= Data.Builder()
                        .putString(KEY_IMAGE_URI,imageUrl)
                        .build()
                val output = workDataOf(KEY_IMAGE_URI to imageUrl)

                Result.success(output)
            } else {
                Result.failure()
            }
        } catch (exception: Exception) {
            Result.failure()
        }
    }
    private fun saveImageToExternalStorage(bitmap: Bitmap):String{
        // Get the external storage directory path
        val path = Environment.getExternalStorageDirectory().toString()

        // Create a file to save the image
        val file = File(path, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the output stream
            stream.flush()

            // Close the output stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }

        // Return the saved image path to uri
        return Uri.parse(file.absolutePath).toString()
    }
}