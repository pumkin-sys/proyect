package com.example.proyect

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var ndefIntentFilters: Array<IntentFilter>? = null

    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firestore = FirebaseFirestore.getInstance()

        val imageView = findViewById<ImageView>(R.id.backgroundImageView)

        Glide.with(this)
            .asGif()
            .load(R.drawable.fondo2)
            .into(imageView)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC no es compatible con este dispositivo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        pendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE)
        val ndefIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndefIntentFilter.addDataType("text/plain")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            e.printStackTrace()
        }
        ndefIntentFilters = arrayOf(ndefIntentFilter)

        val editTextEmail = findViewById<EditText>(R.id.editTextEmailLogin)
        val editTextPassword = findViewById<EditText>(R.id.editTextPasswordLogin)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonGoToRegister = findViewById<Button>(R.id.buttonGoToRegister)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            firestore.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val user = querySnapshot.documents[0].toObject(User::class.java)
                        if (user != null) {
                            val Name = user.name
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("User_name", Name)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al iniciar sesión: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }


        buttonGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, ndefIntentFilters, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            val ndef = Ndef.get(tag)
            val message = ndef.cachedNdefMessage
            val records = message.records
            if (records.isNotEmpty() && records[0].payload.isNotEmpty()) {
                val payload = records[0].payload
                val recordText = String(payload, 3, payload.size - 3, Charsets.UTF_8)

                firestore.collection("users")
                    .whereEqualTo("cardIdentifier", recordText)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val user = querySnapshot.documents[0].toObject(User::class.java)
                            if (user != null) {
                                val email = user.email
                                val password = user.password
                                val name = user.name
                                if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                                    firestore.collection("users")
                                        .whereEqualTo("email", email)
                                        .whereEqualTo("password", password)
                                        .get()
                                        .addOnSuccessListener { innerQuerySnapshot ->
                                            if (!innerQuerySnapshot.isEmpty) {
                                                val intent = Intent(this, MainActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                intent.putExtra("User_name",name)
                                                startActivity(intent)
                                                finish()
                                            } else {
                                                Toast.makeText(this, "Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Toast.makeText(this, "Error al iniciar sesión: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(this, "Datos del usuario incompletos en la base de datos", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "No se encontró ningún usuario asociado a esta tarjeta NFC", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Error al consultar la base de datos: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
