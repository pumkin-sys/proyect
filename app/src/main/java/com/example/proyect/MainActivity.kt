package com.example.proyect

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.appcompat.widget.Toolbar
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*

import com.example.proyect.R.menu.activity_main_menu
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var textViewName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firestore = FirebaseFirestore.getInstance()
        textViewName = findViewById(R.id.textViewName)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        val cardView1: CardView = findViewById(R.id.cardView1)
        val cardView2: CardView = findViewById(R.id.cardView2)


        asignarEventoClic(cardView1, ReadTutorialActivity::class.java)
        asignarEventoClic(cardView2, CreateTutorialActivity::class.java)

        val userName = intent.getStringExtra("User_name")
        if (!userName.isNullOrEmpty()) {
            textViewName.text = "Listo para ver un Tutorial? $userName"
        }

        val database = FirebaseDatabase.getInstance()
        val notificationsRef = database.getReference("notifications")

        notificationsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val notification = snapshot.getValue(Notification::class.java)
                notification?.let {
                    sendNotification(it.title, it.body)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendNotification(title: String, messageBody: String) {
        val channelId = "default_channel_id"
        val channelName = "Default Channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(activity_main_menu, menu)
        return true
    }

    private fun asignarEventoClic(cardView: CardView, actividad: Class<*>) {
        cardView.setOnClickListener {
            abrirOtraActividad(actividad)
        }
    }

    private fun abrirOtraActividad(actividad: Class<*>) {
        val intent = Intent(this, actividad)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout_menu -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }

            R.id.session_menu -> {
                val userName = intent.getStringExtra("User_name")
                val intent = Intent(this, UserDetailsActivity::class.java)
                intent.putExtra("User_name", userName)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
