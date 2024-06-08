package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firestore = FirebaseFirestore.getInstance()

        val editTextName = findViewById<EditText>(R.id.editTextName)
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setTitle("")
        toolbar.setSubtitle("")
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val imageView = findViewById<ImageView>(R.id.backgroundImageView)

        Glide.with(this)
            .asGif()
            .load(R.drawable.fondo2)
            .into(imageView)

        buttonRegister.setOnClickListener {
            val name = editTextName.text.toString()
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (name.isEmpty()) {
                editTextName.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                editTextEmail.error = "El correo electrónico es obligatorio"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editTextPassword.error = "La contraseña es obligatoria"
                return@setOnClickListener
            }

            val cardIdentifier = UUID.randomUUID().toString()

            val newUser = hashMapOf(
                "name" to name,
                "email" to email,
                "password" to password,
                "birthDate" to null,
                "photoUrl" to null,
                "cardIdentifier" to cardIdentifier
            )

            firestore.collection("users")
                .add(newUser)
                .addOnSuccessListener {
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
