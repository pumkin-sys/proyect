package com.example.proyect

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class TutorialDetailActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var storageRef: StorageReference
    private lateinit var tituloTextView: TextView
    private lateinit var directorTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.title = ""

        videoView = findViewById(R.id.videoView)
        tituloTextView = findViewById(R.id.tituloTextView)
        directorTextView = findViewById(R.id.directorTextView)
        storageRef = FirebaseStorage.getInstance().reference

        val videoUrl = intent.getStringExtra("videoUrl")
        val titulo = intent.getStringExtra("titulo")
        val director = intent.getStringExtra("director")

        tituloTextView.text = titulo
        directorTextView.text = director

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        videoUrl?.let { url ->
            val videoUri = Uri.parse(url)
            videoView.setVideoURI(videoUri)
            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.start()
            }
        }
    }
}
