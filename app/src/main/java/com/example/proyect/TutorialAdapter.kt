package com.example.proyect

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class TutorialAdapter(
    private val context: Context,
    private val tutoriales: MutableList<Tutorials>,
    private val onItemClick: (Tutorials) -> Unit
) : RecyclerView.Adapter<TutorialAdapter.TutorialViewHolder>() {

    class TutorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val videoThumbnail: ImageView = view.findViewById(R.id.videoThumbnail)
        val tituloTextView: TextView = view.findViewById(R.id.tituloTextView)
        val directorTextView: TextView = view.findViewById(R.id.directorTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tutorial, parent, false)
        return TutorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        val tutorial = tutoriales[position]
        holder.tituloTextView.text = tutorial.titulo
        holder.directorTextView.text = tutorial.director
        Glide.with(context)
            .load(tutorial.urlVideo)
            .placeholder(R.drawable.ic_placeholder)
            .into(holder.videoThumbnail)

        holder.itemView.setOnClickListener {
            onItemClick.invoke(tutorial)
        }

        holder.itemView.setOnLongClickListener {
            showDeleteConfirmationDialog(holder.adapterPosition)
            true
        }
    }

    override fun getItemCount(): Int = tutoriales.size

    private fun showDeleteConfirmationDialog(position: Int) {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Eliminar Tutorial")
        builder.setMessage("¿Estás seguro de que deseas eliminar este tutorial?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            deleteItemFromDatabase(tutoriales[position])
            tutoriales.removeAt(position)
            notifyItemRemoved(position)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun deleteItemFromDatabase(tutorial: Tutorials) {
        val db = FirebaseFirestore.getInstance()
        db.collection("tutoriales")
            .whereEqualTo("titulo", tutorial.titulo)
            .whereEqualTo("director", tutorial.director)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    db.collection("tutoriales").document(document.id).delete()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error al eliminar el tutorial: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
