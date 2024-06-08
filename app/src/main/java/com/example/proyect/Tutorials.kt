package com.example.proyect

data class Tutorials(
    val titulo: String = "",
    val director: String = "",
    val genero: String = "",
    val urlVideo: String = "",
    val comentarios: List<String> = emptyList(),
    var estrellas: Int= 0
)

