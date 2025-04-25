package com.example.easy

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    // Reactive list to store chat messages
    val messageList by lazy { mutableStateListOf<MessageModel>() }

    // Initialize the generative model with the provided API key and model name
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constant.apikey
    )

    /**
     * Sends a user message to the AI model and appends the response to the chat history.
     * @param question The user's message.
     */
    fun sendMessage(question: String) {
        if (question.isBlank()) return // Ignore blank inputs

        viewModelScope.launch {
            try {
                // Add the user's message to the list
                messageList.add(MessageModel(question, "user"))

                // Add a temporary placeholder for AI response
                val typingPlaceholder = MessageModel("Typing...", "model")
                messageList.add(typingPlaceholder)

                // Prepare the chat history for the AI model
                val chatHistory = messageList.map {
                    content(it.role) { text(it.message) }
                }

                // Start a chat session and send the user's message
                val chatSession = generativeModel.startChat(history = chatHistory)
                val response = chatSession.sendMessage(question)

                // Replace the placeholder with the actual AI response
                val aiResponse = response.text.orEmpty()
                messageList[messageList.indexOf(typingPlaceholder)] =
                    MessageModel(aiResponse, "model")

            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    /**
     * Updates an existing message in the chat and retrieves a new AI response.
     * @param oldMessage The message to be updated.
     * @param newMessage The updated message content.
     */
    fun updateMessage(oldMessage: MessageModel, newMessage: String) {
        val index = messageList.indexOf(oldMessage)
        if (index != -1) {
            // Update the user's message
            messageList[index] = oldMessage.copy(message = newMessage)

            // Remove the AI response placeholder if it exists
            val modelResponseIndex = index + 1
            if (modelResponseIndex < messageList.size && messageList[modelResponseIndex].role == "model") {
                messageList.removeAt(modelResponseIndex)  // Remove the old AI response
            }

            // Add a placeholder for the new AI response
            val typingPlaceholder = MessageModel("Typing...", "model")
            messageList.add(modelResponseIndex, typingPlaceholder)

            // Retrieve the new AI response
            viewModelScope.launch {
                try {
                    // Prepare the chat history
                    val chatHistory = messageList.map {
                        content(it.role) { text(it.message) }
                    }

                    // Generate a new response from the AI model
                    val chatSession = generativeModel.startChat(history = chatHistory)
                    val response = chatSession.sendMessage(newMessage)

                    // Replace the placeholder with the new response
                    val aiResponse = response.text.orEmpty()
                    messageList[modelResponseIndex] = MessageModel(aiResponse, "model")
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        }
    }

    /**
     * Deletes both user and model messages from the chat history.
     * @param userMessage The user's message to be deleted.
     */
    fun deleteMessage(userMessage: MessageModel) {
        // Find the index of the user message
        val userIndex = messageList.indexOf(userMessage)
        if (userIndex != -1) {
            // Check if there's a corresponding model message after the user message
            if (userIndex + 1 < messageList.size && messageList[userIndex + 1].role == "model") {
                // Remove both user and model messages
                messageList.removeAt(userIndex + 1)  // Remove model message
                messageList.removeAt(userIndex)      // Remove user message
            } else {
                // Remove just the user message if no model message is found
                messageList.removeAt(userIndex)
            }
        }
    }

    /**
     * Handles errors that occur during communication with the AI model.
     * @param exception The exception that occurred.
     */
    private fun handleError(exception: Exception) {
        Log.e("ChatViewModel", "Error during AI communication: ${exception.message}", exception)

        // Replace the placeholder with an error message
        val typingPlaceholderIndex = messageList.indexOfFirst { it.message == "Typing..." }
        if (typingPlaceholderIndex != -1) {
            messageList[typingPlaceholderIndex] = MessageModel(
                "Oops! Something went wrong while communicating with the AI. Please try again.",
                "model"
            )
        } else {
            messageList.add(
                MessageModel(
                    "Oops! Something went wrong while communicating with the AI. Please try again.",
                    "model"
                )
            )
        }
    }
}




/*
package com.example.easy

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    // A list to hold chat messages, backed by Compose's mutableStateListOf for reactivity
    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    // Initialize the generative model with the given API key and model name
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constant.apikey
    )

    /**
     * Sends a user message and retrieves a response from the generative AI model.
     * @param question The question or message from the user.
     */
    fun sendMessage(question: String) {
        if (question.isBlank()) return // Ignore empty or blank messages

        viewModelScope.launch {
            try {
                // Convert existing chat history into the required format
                val chatHistory = messageList.map {
                    content(it.role) { text(it.message) }
                }

                // Start a chat session with the AI model
                val chat = generativeModel.startChat(history = chatHistory.toList())

                // Add the user's message to the chat
                messageList.add(MessageModel(question, "user"))

                // Temporarily display "Typing..." to indicate the model is processing
                messageList.add(MessageModel("Typing...", "model"))

                // Send the user's question and retrieve the model's response
                val response = chat.sendMessage(question)

                // Replace the "Typing..." message with the actual response
                messageList.removeLastOrNull()
                messageList.add(MessageModel(response.text.toString(), "model"))

            } catch (e: Exception) {
                // Log the error for debugging
                Log.e("ChatViewModel", "Error fetching AI response: ${e.message}", e)

                // Replace "Typing..." with an error message if it exists
                messageList.removeLastOrNull()
                messageList.add(MessageModel("Error: Unable to get a response. Please try again.", "model"))
            }
        }
    }

    /**
     * Updates a previously sent message and retrieves a response from the AI.
     * @param oldMessage The original message to be updated.
     * @param newMessage The new message to replace the old one.
     */
    fun updateMessage(oldMessage: MessageModel, newMessage: String) {
        val index = messageList.indexOf(oldMessage)
        if (index != -1) {
            // Update the message in the list
            messageList[index] = oldMessage.copy(message = newMessage)

            // Trigger the bot response after updating the message
            sendMessage(newMessage)
        }
    }

    /**
     * Cancels the editing of a message by clearing the current edit state.
     */
    fun cancelEdit() {
        // Clear any previous message if editing is canceled
    }
}



/*
package com.example.easy

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    // A list to hold chat messages, backed by Compose's mutableStateListOf for reactivity
    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    // Initialize the generative model with the given API key and model name
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constant.apikey
    )

    /**
     * Sends a user message and retrieves a response from the generative AI model.
     * @param question The question or message from the user.
     */
    fun sendMessage(question: String) {
        if (question.isBlank()) return // Ignore empty or blank messages

        viewModelScope.launch {
            try {
                // Convert existing chat history into the required format
                val chatHistory = messageList.map {
                    content(it.role) { text(it.message) }
                }

                // Start a chat session with the AI model
                val chat = generativeModel.startChat(history = chatHistory.toList())

                // Add the user's message to the chat
                messageList.add(MessageModel(question, "user"))

                // Temporarily display "Typing..." to indicate the model is processing
                messageList.add(MessageModel("Typing...", "model"))

                // Send the user's question and retrieve the model's response
                val response = chat.sendMessage(question)

                // Replace the "Typing..." message with the actual response
                messageList.removeLastOrNull()
                messageList.add(MessageModel(response.text.toString(), "model"))

            } catch (e: Exception) {
                // Log the error for debugging
                Log.e("ChatViewModel", "Error fetching AI response: ${e.message}", e)

                // Replace "Typing..." with an error message if it exists
                messageList.removeLastOrNull()
                messageList.add(MessageModel("Error: Unable to get a response. Please try again.", "model"))
            }
        }
    }

    /**
     * Updates a previously sent message and retrieves a response from the AI.
     * @param oldMessage The original message to be updated.
     * @param newMessage The new message to replace the old one.
     */
    fun updateMessage(oldMessage: MessageModel, newMessage: String) {
        val index = messageList.indexOf(oldMessage)
        if (index != -1) {
            // Update the message in the list
            messageList[index] = oldMessage.copy(message = newMessage)

            // Trigger the bot response after updating the message
            sendMessage(newMessage)
        }
    }
}

 */




/*
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constant.apikey
    )

    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                // Create chat history from existing messages
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.message) }
                    }.toList()
                )

                // Add the user's question to the message list
                messageList.add(MessageModel(question, "user"))

                // Show "Typing..." message temporarily
                messageList.add(MessageModel("Typing...", "model"))

                // Get the model's response
                val response = chat.sendMessage(question)

                // Remove the "Typing..." message
                messageList.removeAt(messageList.lastIndex)

                // Add the actual response from the model
                messageList.add(MessageModel(response.text.toString(), "model"))
            } catch (e: Exception) {
                // Remove the "Typing..." message if it exists
                if (messageList.lastOrNull()?.message == "Typing...") {
                    messageList.removeAt(messageList.lastIndex)
                }

                // Add an error message to the message list
                messageList.add(MessageModel("Error: ${e.message}", "model"))
            }
        }
    }
}

*/




/*
// Define the messages list (private so it can't be modified directly from outside)
private var _messages by mutableStateOf<List<Message>>(emptyList())
val messages: List<Message> get() = _messages  // Expose messages as a read-only list

// Simulate sending a message
fun sendMessage(text: String) {
    val newMessage = Message(text = text, isUser = true)  // Assuming it's a user message
    _messages = _messages + newMessage
}
}
*/