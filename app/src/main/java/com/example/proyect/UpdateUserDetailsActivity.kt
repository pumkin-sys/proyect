package com.example.proyect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UpdateUserDetailsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextBirthDate: EditText
    private lateinit var imageViewUserPhoto: ImageView
    private lateinit var buttonSelectPhoto: Button
    private lateinit var buttonUpdateDetails: Button

    private val REQUEST_IMAGE_PICK = 2
    private var currentPhotoPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_user_details)

        firestore = FirebaseFirestore.getInstance()
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextBirthDate = findViewById(R.id.editTextBirthDate)
        imageViewUserPhoto = findViewById(R.id.imageViewUserPhoto)
        buttonSelectPhoto = findViewById(R.id.buttonSelectPhoto)
        buttonUpdateDetails = findViewById(R.id.buttonUpdateDetails)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.title = ""

        val userName = intent.getStringExtra("User_name")
        if (userName != null) {
            loadUserDetails(userName)
        }

        buttonSelectPhoto.setOnClickListener {
            selectImageFromGallery()
        }

        buttonUpdateDetails.setOnClickListener {
            updateUserDetails()
        }
    }

    private fun loadUserDetails(userName: String) {
        firestore.collection("users")
            .whereEqualTo("name", userName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val user = querySnapshot.documents[0].toObject(User::class.java)
                    if (user != null) {
                        editTextName.setText(user.name)
                        editTextEmail.setText(user.email)
                        editTextPassword.setText(user.password)
                        editTextBirthDate.setText(user.birthDate)
                        if (user.photoUrl != null) {
                            Glide.with(this)
                                .load(user.photoUrl)
                                .into(imageViewUserPhoto)
                        }
                    }
                } else {
                    Log.e("UpdateUserDetails", "No user found with the provided name.")
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar los datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("UpdateUserDetails", "Error loading user details: ${exception.message}")
            }
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        uploadImageToFirebaseStorage(selectedImageUri)
                    }
                }
            }
        }
    }

    private fun uploadImageToFirebaseStorage(selectedImageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userImageRef = storageRef.child("user_images/${selectedImageUri.lastPathSegment}")
        val uploadTask = userImageRef.putFile(selectedImageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            userImageRef.downloadUrl.addOnSuccessListener { uri ->
                currentPhotoPath = uri.toString()
                imageViewUserPhoto.setImageURI(selectedImageUri)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error al subir la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
            Log.e("UpdateUserDetails", "Error uploading image: ${exception.message}")
        }
    }

    private fun updateUserDetails() {
        val name = editTextName.text.toString()
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()
        val birthDate = editTextBirthDate.text.toString()
        val photoUrl = currentPhotoPath

        val userName = intent.getStringExtra("User_name")
        if (!userName.isNullOrEmpty()) {
            firestore.collection("users")
                .whereEqualTo("name", userName)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents[0]
                        val updates = hashMapOf<String, Any>()
                        updates["name"] = name
                        updates["email"] = email
                        updates["password"] = password
                        updates["birthDate"] = birthDate
                        if (photoUrl != null) {
                            updates["photoUrl"] = photoUrl
                        }

                        document.reference.update(updates)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
                                Log.i("UpdateUserDetails", "User details updated successfully")
                                val intent = Intent(this, MainActivity::class.java)
                                intent.putExtra("User_name", name)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error al actualizar los datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                                Log.e("UpdateUserDetails", "Error updating user details: ${exception.message}")
                            }
                    } else {
                        Log.e("UpdateUserDetails", "No user found with the provided name.")
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al consultar la base de datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UpdateUserDetails", "Error querying the database: ${exception.message}")
                }
        } else {
            Log.e("UpdateUserDetails", "User_name is null or empty")
        }
    }
}
