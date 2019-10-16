package com.jcmo.pruebacargafoto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.ByteArrayOutputStream

class Main2Activity : AppCompatActivity() {

    private lateinit var root: View
    val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var storage: FirebaseStorage

    private var id: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        foto.setOnClickListener {
            takePhoto()
        }

        Agregar.setOnClickListener {
            //savedUser("asd")
            saveImage(foto)
        }




    }

    private fun saveImage(iPicture: ImageView?) {
        storage = FirebaseStorage.getInstance()
        val photoRef = storage.reference.child("usuarios").child("123")
        iPicture?.isDrawingCacheEnabled = true
        iPicture?.buildDrawingCache()
        val bitmap = (iPicture?.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = photoRef.putBytes(data)

        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation photoRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                savedUser(downloadUri.toString())
                Log.i("OK",downloadUri.toString())
            } else {
                Log.i("nOK","fallo")
                // Handle failures
                // ...
            }
        }

    }

    private fun savedUser(urlFoto:String) {
        val id ="789"
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("usuarios")
        myRef.child(id).setValue(urlFoto)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(this.applicationContext.packageManager)?.also {
                startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            foto.setImageBitmap(imageBitmap)
            //saveImage(root.iPicture)
        }
    }


}
