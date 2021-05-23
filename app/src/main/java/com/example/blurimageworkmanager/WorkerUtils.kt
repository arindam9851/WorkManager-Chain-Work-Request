@file:JvmName("WorkerUtils")

package com.example.blurimageworkmanager

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build

import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.renderscript.Allocation
import androidx.renderscript.Element
import androidx.renderscript.RenderScript
import androidx.renderscript.ScriptIntrinsicBlur

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID


/**
 * Writes bitmap to a temporary file and returns the Uri for the file
 * @param applicationContext Application context
 * @param bitmap Bitmap to write to temp file
 * @return Uri for temp file with bitmap
 * @throws FileNotFoundException Throws if bitmap file cannot be found
 */
@Throws(FileNotFoundException::class)
fun writeBitmapToFile(applicationContext: Context, bitmap: Bitmap): Uri {
    val name = String.format("blur-filter-output-%s.png", UUID.randomUUID().toString())
    val outputDir = File(applicationContext.filesDir, OUTPUT_PATH)
    if (!outputDir.exists()) {
        outputDir.mkdirs() // should succeed
    }
    val outputFile = File(outputDir, name)
    var out: FileOutputStream? = null
    try {
        out = FileOutputStream(outputFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
    } finally {
        out?.let {
            try {
                it.close()
            } catch (ignore: IOException) {
            }

        }
    }
    return Uri.fromFile(outputFile)
}


/**
 * Blurs the given Bitmap image
 * @param bitmap Image to blur
 * @param applicationContext Application context
 * @return Blurred bitmap image
 */
@WorkerThread
fun blurBitmap(bitmap: Bitmap, applicationContext: Context): Bitmap {
    lateinit var rsContext: RenderScript
    try {

        // Create the output bitmap
        val output = Bitmap.createBitmap(
            bitmap.width, bitmap.height, bitmap.config)

        // Blur the image
        rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
        val inAlloc = Allocation.createFromBitmap(rsContext, bitmap)
        val outAlloc = Allocation.createTyped(rsContext, inAlloc.type)
        val theIntrinsic = ScriptIntrinsicBlur.create(rsContext, Element.U8_4(rsContext))
        theIntrinsic.apply {
            setRadius(10f)
            theIntrinsic.setInput(inAlloc)
            theIntrinsic.forEach(outAlloc)
        }
        outAlloc.copyTo(output)

        return output
    } finally {
        rsContext.finish()
    }
}

 fun makeStatusNotification(desc: String,applicationContext: Context) {

    val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
                "simple",
                "simple",
                NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
    }
    val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, "simple")
                    .setContentTitle("Blur")
                    .setContentText(desc)
                    .setSmallIcon(R.mipmap.sym_def_app_icon)
    manager.notify(1, builder.build())
}

