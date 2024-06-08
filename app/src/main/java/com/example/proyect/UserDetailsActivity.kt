package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class UserDetailsActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private lateinit var textViewName: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var textViewBirthDate: TextView
    private lateinit var imageViewUserPhoto: ImageView
    private lateinit var buttonEditDetails: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.title = ""

        firestore = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        textViewName = findViewById(R.id.textViewName)
        textViewEmail = findViewById(R.id.textViewEmail)
        textViewBirthDate = findViewById(R.id.textViewBirthDate)
        imageViewUserPhoto = findViewById(R.id.imageViewUserPhoto)
        buttonEditDetails = findViewById(R.id.buttonEditDetails)

        val userName = intent.getStringExtra("User_name")
        if (userName != null) {
            loadUserDetails(userName)
        }

        buttonEditDetails.setOnClickListener {
            val intent = Intent(this, UpdateUserDetailsActivity::class.java)
            intent.putExtra("User_name", userName)
            startActivity(intent)
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
                        textViewName.setText(user.name)
                        textViewEmail.setText(user.email)
                        textViewBirthDate.setText(user.birthDate)
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

}
