package io.keepcoding.chat

import androidx.annotation.DrawableRes

data class User( // Define el usuario
	val id: String,
	@DrawableRes val profileImageRes: Int, // Este entero tiene que ser un id de una imagen
	val name: String,
)

data class Message( // Modela el mensaje
	val id: String, // id propio
	val timestamp: Long, // El mensaje cuando ha sido enviado
	val text: String, // Texto del mensaje
	val sender: User, // Quien envio el mensaje
)

data class Channel(
	val id: String,
	@DrawableRes val channelImageRes: Int, // Imagen de resource
	val name: String,
	val lastMessageTimestamp: Long, // Ultimo mensaje enviado
)