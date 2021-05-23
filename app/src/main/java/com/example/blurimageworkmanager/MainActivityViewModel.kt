package com.example.blurimageworkmanager

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.work.*

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val workManager = WorkManager.getInstance()
    internal var outputUri: Uri? = null


    val outputWorkInfos: LiveData<List<WorkInfo>> = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    val outputSaveImageWorkInfos: LiveData<List<WorkInfo>> = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT_SAVE_IMAGE)
    fun blurImage(imageUri: Uri?) {

        val data = Data.Builder()
                .putString(KEY_IMAGE_URI, imageUri.toString())
                .build()
        val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .build()
        val cleanUpRequest = OneTimeWorkRequest.Builder(CleanupWorker::class.java)
                .setConstraints(constraints)
                .build()


        val oneTimeRequest = OneTimeWorkRequest.Builder(LittleBlurImageWorker::class.java)
                .setInputData(data)
                .addTag(TAG_OUTPUT)
                .build()


        val saveImageWorkRequest = OneTimeWorkRequest.Builder(SaveImageToFileWorker::class.java)
                .addTag(TAG_OUTPUT_SAVE_IMAGE)
                .build()



        workManager.beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.KEEP, cleanUpRequest).then(oneTimeRequest).then(saveImageWorkRequest).enqueue()
    }


    fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

     fun setOutputUri(outputImageUri: String?) {
        outputUri = Uri.parse(outputImageUri)
    }

}