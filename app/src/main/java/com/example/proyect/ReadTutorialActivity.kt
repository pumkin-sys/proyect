package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ReadTutorialActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tutorialAdapter: TutorialAdapter
    private val tutorialesList = mutableListOf<Tutorials>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_tutorial)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.title = ""

        db = FirebaseFirestore.getInstance()

        tutorialAdapter = TutorialAdapter(this, tutorialesList) { tutorial ->
            showTutorialDetailActivity(tutorial)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = tutorialAdapter

        fetchTutorials()
    }

    private fun fetchTutorials() {
        db.collection("tutoriales")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val tutorial = document.toObject(Tutorials::class.java)
                    tutorialesList.add(tutorial)
                }
                tutorialAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                showToast("Error al obtener tutoriales: ${exception.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showTutorialDetailActivity(tutorial: Tutorials) {
        val intent = Intent(this, TutorialDetailActivity::class.java)
        intent.putExtra("videoUrl", tutorial.urlVideo)
        intent.putExtra("titulo", tutorial.titulo)
        intent.putExtra("director", tutorial.director)
        startActivity(intent)
    }
}
