package com.example.easy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource // Use this for accessing drawables
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.easy.ui.theme.EasyTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val RQ_Speech_Rec = 102 // Request code for speech recognition
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatHistoryManager: ChatHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the ViewModel and ChatHistoryManager
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        chatHistoryManager = ChatHistoryManager(this)

        // Load chat history from SharedPreferences
        val savedMessages = chatHistoryManager.loadChatHistory()
        chatViewModel.messageList.addAll(savedMessages)

        setContent {
            EasyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        // Chat Page Composable
                        ChatPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            viewModel = chatViewModel,
                            askSpeechInput = { askSpeechInput() } // Pass the askSpeechInput function as a lambda
                        )

                        // Button to clear chat history
                        Button(
                            onClick = {
                                chatHistoryManager.clearChatHistory() // Clear chat history
                                chatViewModel.messageList.clear() // Clear ViewModel messages
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Clear Chat History")
                        }

                        /*
                        // Mic button to trigger speech input, using custom drawable resource
                        IconButton(
                            onClick = { askSpeechInput() },
                            modifier = Modifier
                                .padding(16.dp)
                                .size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_mic_24), // Custom mic drawable
                                contentDescription = "Mic"
                            )
                        }

                         */
                    }
                }
            }
        }
    }

    // Function to start speech input
    private fun askSpeechInput() {
        if (!android.speech.SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech Recognition is not available", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something")
            }
            startActivityForResult(intent, RQ_Speech_Rec)
        }
    }

    // Handle the result of speech input
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_Speech_Rec && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val voiceInput = result?.get(0).toString()
            if (voiceInput.isNotEmpty()) {
                // Send the recognized speech as a message to the chat
                chatViewModel.sendMessage(voiceInput)
            }
        }
    }

    // Save chat history when the app is paused
    override fun onPause() {
        super.onPause()
        chatHistoryManager.saveChatHistory(chatViewModel.messageList)
    }
}














/*
package com.example.easy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource // Use this for accessing drawables
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.easy.ui.theme.EasyTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val RQ_Speech_Rec = 102 // Request code for speech recognition
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatHistoryManager: ChatHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the ViewModel and ChatHistoryManager
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        chatHistoryManager = ChatHistoryManager(this)

        // Load chat history from SharedPreferences
        val savedMessages = chatHistoryManager.loadChatHistory()
        chatViewModel.messageList.addAll(savedMessages)

        setContent {
            EasyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        // Chat Page Composable
                        ChatPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            viewModel = chatViewModel
                        )

                        // Button to clear chat history
                        Button(
                            onClick = {
                                chatHistoryManager.clearChatHistory() // Clear chat history
                                chatViewModel.messageList.clear() // Clear ViewModel messages
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Clear Chat History")
                        }

                        // Mic button to trigger speech input, using custom drawable resource
                        IconButton(
                            onClick = { askSpeechInput() },
                            modifier = Modifier
                                .padding(16.dp)
                                .size(48.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_mic_24), // Custom mic drawable
                                contentDescription = "Mic"
                            )
                        }
                    }
                }
            }
        }
    }

    // Function to start speech input
    private fun askSpeechInput() {
        if (!android.speech.SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech Recognition is not available", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something")
            }
            startActivityForResult(intent, RQ_Speech_Rec)
        }
    }

    // Handle the result of speech input
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_Speech_Rec && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val voiceInput = result?.get(0).toString()
            if (voiceInput.isNotEmpty()) {
                // Send the recognized speech as a message to the chat
                chatViewModel.sendMessage(voiceInput)
            }
        }
    }

    // Save chat history when the app is paused
    override fun onPause() {
        super.onPause()
        chatHistoryManager.saveChatHistory(chatViewModel.messageList)
    }
}

 */






/*
package com.example.easy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.easy.ui.theme.EasyTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val RQ_Speech_Rec = 102 // Request code for speech recognition
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatHistoryManager: ChatHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the ViewModel and ChatHistoryManager
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        chatHistoryManager = ChatHistoryManager(this)

        // Load chat history from SharedPreferences
        val savedMessages = chatHistoryManager.loadChatHistory()
        chatViewModel.messageList.addAll(savedMessages)

        setContent {
            EasyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        // Chat Page Composable
                        ChatPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            viewModel = chatViewModel
                        )

                        // Button to clear chat history
                        Button(
                            onClick = {
                                chatHistoryManager.clearChatHistory() // Clear chat history
                                chatViewModel.messageList.clear() // Clear ViewModel messages
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Clear Chat History")
                        }

                        // Button to trigger speech input
                        Button(
                            onClick = { askSpeechInput() },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Voice Input")
                        }
                    }
                }
            }
        }
    }

    // Function to start speech input
    private fun askSpeechInput() {
        if (!android.speech.SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech Recognition is not available", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something")
            }
            startActivityForResult(intent, RQ_Speech_Rec)
        }
    }

    // Handle the result of speech input
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_Speech_Rec && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val voiceInput = result?.get(0).toString()
            if (voiceInput.isNotEmpty()) {
                // Send the recognized speech as a message to the chat
                chatViewModel.sendMessage(voiceInput)
            }
        }
    }

    // Save chat history when the app is paused
    override fun onPause() {
        super.onPause()
        chatHistoryManager.saveChatHistory(chatViewModel.messageList)
    }
}
*/





/*
package com.example.easy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.easy.ui.theme.EasyTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val RQ_Speech_Rec = 102 // Request code for speech recognition
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatHistoryManager: ChatHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModel and ChatHistoryManager
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        chatHistoryManager = ChatHistoryManager(this)

        // Load saved chat history
        val savedMessages = chatHistoryManager.loadChatHistory()
        chatViewModel.messageList.addAll(savedMessages)

        setContent {
            EasyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        // Chat Page Composable
                        ChatPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            viewModel = chatViewModel
                        )

                        // Mic Icon for speech input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = { askSpeechInput() },
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_mic_24),
                                    contentDescription = "Mic",
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Function to start speech input
    private fun askSpeechInput() {
        if (!android.speech.SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech Recognition is not available", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
            startActivityForResult(intent, RQ_Speech_Rec)
        }
    }

    // Handle the result of speech input
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_Speech_Rec && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val voiceInput = result?.get(0).orEmpty()
            if (voiceInput.isNotEmpty()) {
                // Send recognized speech to chat
                chatViewModel.sendMessage(voiceInput)
            } else {
                Toast.makeText(this, "No speech detected. Try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Speech recognition cancelled or failed.", Toast.LENGTH_SHORT).show()
        }
    }

    // Save chat history when the app is paused
    override fun onPause() {
        super.onPause()
        chatHistoryManager.saveChatHistory(chatViewModel.messageList)
    }
}
*/


/*
package com.example.easy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.easy.ui.theme.EasyTheme

class MainActivity : ComponentActivity() {
    // Declare the chatViewModel as a member of MainActivity
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatHistoryManager: ChatHistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the ViewModel
        chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // Initialize ChatHistoryManager
        chatHistoryManager = ChatHistoryManager(this)

        // Load chat history from SharedPreferences
        val savedMessages = chatHistoryManager.loadChatHistory()
        chatViewModel.messageList.addAll(savedMessages)

        setContent {
            EasyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Main Content Layout
                    Column(modifier = Modifier.padding(innerPadding)) {

                        // Chat Page Composable
                        ChatPage(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            viewModel = chatViewModel
                        )

                        // Button to clear chat history
                        Button(
                            onClick = {
                                chatHistoryManager.clearChatHistory()  // Clear the chat history
                                chatViewModel.messageList.clear() // Also clear the message list in the ViewModel
                            },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Clear Chat History")
                        }
                    }
                }
            }
        }
    }

    // Ensure to save the chat history when the app is closed
    override fun onPause() {
        super.onPause()
        // Save chat history when app is paused (before closing or backgrounded)
        chatHistoryManager.saveChatHistory(chatViewModel.messageList)
    }
}

 */



/*
package com.example.easy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.easy.ui.theme.EasyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize the ViewModel
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        setContent {
            EasyTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // Use the correct composable function name
                    ChatPage(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = chatViewModel
                    )
                }
            }
        }
    }
}

 */