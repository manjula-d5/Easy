package com.example.easy

//import androidx.compose.ui.semantics.Role
//import java.util.UUID

/*
data class MessageModel(
    val id: Int, // Unique ID for the message
    val userMessage: String, // User's input message
    val modelReply: String? // Model's response (could be null initially)
)
*/
/*
data class MessageModel(
    val message: String,
    val role: String,
    val imageUri: String? = null // Add this property to store image URI
)
*/
/*
data class MessageModel(
    val id: String,
    val content: String,
    val status: String? = null // Make `status` nullable or give it a default value
)
*/



data class MessageModel(
    val message: String,
    val role: String,
    val status: String = ""  // New status field to track message edit status

)

/*
data class MessageModel(
    val message : String,
    val role : String
)
*/

 /*data class MessageModel(
    val id: String = UUID.randomUUID().toString(),  // Unique ID for each message
    val message: String,
    val role: String // "user" or "model"
)
*/