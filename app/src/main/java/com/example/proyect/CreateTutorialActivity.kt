package com.example.proyect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class CreateTutorialActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_tutorial)

        val selectVideoButton = findViewById<Button>(R.id.selectVideoButton)
        val submitButton = findViewById<Button>(R.id.submitButton)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.title = ""
        db = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        selectVideoButton.setOnClickListener {
            selectVideo()
        }

        submitButton.setOnClickListener {
            if (videoUri != null) {
                uploadVideoAndSaveData()
            } else {
                showToast("Por favor selecciona un video.")
            }
        }
    }

    private fun selectVideo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        startActivityForResult(intent, REQUEST_CODE_VIDEO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_VIDEO && resultCode == Activity.RESULT_OK) {
            videoUri = data?.data
            val videoThumbnailImageView = findViewById<ImageView>(R.id.videoThumbnailImageView)
            Glide.with(this)
                .load(videoUri)
                .into(videoThumbnailImageView)
        }
    }

    private fun uploadVideoAndSaveData() {
        val tituloEditText = findViewById<EditText>(R.id.tituloEditText)
        val directorEditText = findViewById<EditText>(R.id.directorEditText)
        val generoEditText = findViewById<EditText>(R.id.generoEditText)

        val titulo = tituloEditText.text.toString()
        val director = directorEditText.text.toString()
        val genero = generoEditText.text.toString()

        if (videoUri != null) {
            val videoRef = storageReference.child("videos/${UUID.randomUUID()}.mp4")
            videoRef.putFile(videoUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    videoRef.downloadUrl.addOnSuccessListener { uri ->
                        val urlVideo = uri.toString()
                        val tutorial = Tutorials(titulo, director, genero, urlVideo)

                        db.collection("tutoriales")
                            .add(tutorial)
                            .addOnSuccessListener { documentReference ->
                                showToast("Tutorial agregado")
                                saveNotification(titulo)
                            }
                            .addOnFailureListener { e ->
                                showToast("Error al agregar tutorial: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    showToast("Error al subir el video: ${e.message}")
                }
        }
    }

    private fun saveNotification(titulo: String) {
        val database = FirebaseDatabase.getInstance()
        val notificationsRef = database.getReference("notifications")

        val notification = Notification(titulo, "Se ha agregado un nuevo tutorial")
        notificationsRef.push().setValue(notification)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_VIDEO = 1
    }
}
