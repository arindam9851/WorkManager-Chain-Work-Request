package com.example.blurimageworkmanager

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private val pickImage = 100
    private var imageUri: Uri? = null
    private lateinit var viewModel: MainActivityViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        btn_loadImage.setOnClickListener(View.OnClickListener {
            checkImagePermission()

        })

        btn_go.setOnClickListener(View.OnClickListener {
           if(imageUri!=null){
               progressbar.visibility = View.VISIBLE
               btn_cancel.visibility = View.VISIBLE
               lin_content.visibility = View.GONE
               viewModel.blurImage(imageUri)

           }
        })

        btn_cancel.setOnClickListener(View.OnClickListener {
            viewModel.cancelWork()
        })

        btn_seefile.setOnClickListener(View.OnClickListener {
            viewModel.outputUri?.let { currentUri ->
                   val file = File(currentUri.path)
                   if (file.exists()) {
                       println("debug: file exist ${file.absolutePath}")
                       val uri = file?.let { FileProvider.getUriForFile(this@MainActivity!!, this@MainActivity!!.applicationContext.packageName.toString() + ".provider", file) }
                       val intent = Intent(Intent.ACTION_VIEW)
                       intent.setDataAndType(uri, "image/*")
                       intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                       startActivity(intent)
                   }




            }
        })


        viewModel.outputWorkInfos
                .observe(this, Observer {
                    if (it.size > 0) {
                        var workInfo = it[0]
                        println("debug: $workInfo")
                        if (workInfo.state.isFinished) {
                            progressbar.visibility = View.GONE
                            btn_cancel.visibility = View.GONE
                            lin_content.visibility = View.VISIBLE
                            val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)

                            blur_imageview.setImageURI(null)
                            if (!outputImageUri.isNullOrEmpty()) {
                                viewModel.setOutputUri(outputImageUri)
                                blur_imageview.setImageURI(outputImageUri)
                            }
                        }

                    }

                })


        viewModel.outputSaveImageWorkInfos
                .observe(this, Observer {
                    if (it.size > 0) {
                        var workInfo = it[0]
                        println("debug: $workInfo")
                        if (workInfo.state.isFinished) {
                            val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)
                            if (!outputImageUri.isNullOrEmpty()) {
                                viewModel.setOutputUri(outputImageUri)
                            }
                        }

                    }
                })

    }



    private fun checkImagePermission() {
        Dexter.withActivity(this)
            .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                        startActivityForResult(gallery, pickImage)

                    }

                }

                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            blur_imageview.setImageURI(imageUri)
        }
    }
}

private fun ImageView.setImageURI(outputImageUri: String?) {
    blur_imageview.setImageURI(Uri.parse(outputImageUri))

}
