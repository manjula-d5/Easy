package com.example.easy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    askSpeechInput: () -> Unit // Passing the function as a parameter
) {
    var editMessage by remember { mutableStateOf<MessageModel?>(null) }

    Column(modifier = modifier) {
        AppHeader()
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList,
            onEditMessage = { message ->
                editMessage = message
            },
            onDeleteMessage = { message ->
                viewModel.deleteMessage(message)
            }
        )
        MessageInput(
            initialMessage = editMessage?.message.orEmpty(),
            onMessageSend = { newMessage ->
                if (editMessage != null) {
                    // Update the message in the list directly using the index
                    viewModel.updateMessage(editMessage!!, newMessage)
                    editMessage = null // Reset the editing state
                } else {
                    viewModel.sendMessage(newMessage)
                }
            },
            onCancelEdit = {
                editMessage = null
            },
            isEditing = editMessage != null,
            onMicClick = askSpeechInput // Call the passed function on mic click
        )
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>,
    onEditMessage: (MessageModel) -> Unit,
    onDeleteMessage: (MessageModel) -> Unit
) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Color.Gray,
            )
            Text(text = "Ask me anything", fontSize = 22.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()) { message ->
                MessageRow(
                    messageModel = message,
                    onEditMessage = onEditMessage,
                    onDeleteMessage = onDeleteMessage
                )
            }
        }
    }
}

@Composable
fun MessageRow(
    messageModel: MessageModel,
    onEditMessage: (MessageModel) -> Unit,
    onDeleteMessage: (MessageModel) -> Unit
) {
    val isModel = messageModel.role == "model"
    val context = LocalContext.current
    var showCopyMessage by remember { mutableStateOf(false) }

    // ClipboardManager to copy text
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    // Set different background color for model response
                    .background(if (isModel) Color(0xFF4CAF50) else Color(0xFF1E88E5)) // Change color here
                    .padding(16.dp)
            ) {
                Column {
                    // Display the message text
                    Text(
                        text = messageModel.message,
                        color = Color.White
                    )
                    // Show options for user messages (Edit, Delete, Copy)
                    if (!isModel) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Edit",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .clickable { onEditMessage(messageModel) }
                            )
                            IconButton(onClick = { onDeleteMessage(messageModel) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }

                            // Copy Button for user messages
                            IconButton(
                                onClick = {
                                    val clip = ClipData.newPlainText("message", messageModel.message)
                                    clipboardManager.setPrimaryClip(clip)
                                    showCopyMessage = true
                                    Toast.makeText(context, "Message copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_content_copy_24),
                                    contentDescription = "Copy"
                                )
                            }
                        }
                    }

                    // Show options for model messages (just copy)
                    if (isModel) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            // Copy Button for model messages
                            IconButton(
                                onClick = {
                                    val clip = ClipData.newPlainText("message", messageModel.message)
                                    clipboardManager.setPrimaryClip(clip)
                                    showCopyMessage = true
                                    Toast.makeText(context, "Message copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_content_copy_24),
                                    contentDescription = "Copy"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Chat Bot",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Composable
fun MessageInput(
    initialMessage: String,
    onMessageSend: (String) -> Unit,
    onCancelEdit: () -> Unit,
    isEditing: Boolean,
    onMicClick: () -> Unit // Mic button callback
) {
    var message by remember { mutableStateOf(initialMessage) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(text = "Type your message") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Send Button
        IconButton(
            onClick = {
                if (message.isNotEmpty()) {
                    onMessageSend(message)
                    message = ""
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
        }

        // Cancel Edit Button
        if (isEditing) {
            IconButton(
                onClick = {
                    onCancelEdit()
                    message = ""
                }
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Edit")
            }
        }

        // Mic Button
        IconButton(
            onClick = { onMicClick() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_mic_24), // Mic drawable
                contentDescription = "Mic",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPagePreview() {
    val mockViewModel = ChatViewModel().apply {
        messageList.addAll(
            listOf(
                MessageModel("Hello, how can I help you?", "model"),
                MessageModel("Can you tell me about Kotlin?", "user"),
                MessageModel("Kotlin is a modern, concise, and safe programming language!", "model")
            )
        )
    }

    ChatPage(viewModel = mockViewModel, askSpeechInput = {})
}


/*
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun ChatPage(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    askSpeechInput: () -> Unit // Passing the function as a parameter
) {
    var editMessage by remember { mutableStateOf<MessageModel?>(null) }

    Column(modifier = modifier) {
        AppHeader()
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList,
            onEditMessage = { message -> editMessage = message },
            onDeleteMessage = { message -> viewModel.deleteMessage(message) }
        )
        MessageInput(
            initialMessage = editMessage?.message.orEmpty(),
            onMessageSend = { newMessage ->
                if (editMessage != null) {
                    // Update the message
                    viewModel.updateMessage(editMessage!!, newMessage)
                    // Trigger bot response
                    viewModel.sendMessage(newMessage)
                    editMessage = null
                } else {
                    viewModel.sendMessage(newMessage)
                }
            },
            onCancelEdit = {
                editMessage = null
            },
            isEditing = editMessage != null,
            onMicClick = askSpeechInput // Call the passed function on mic click
        )
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>,
    onEditMessage: (MessageModel) -> Unit,
    onDeleteMessage: (MessageModel) -> Unit
) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Color.Gray,
            )
            Text(text = "Ask me anything", fontSize = 22.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()) { message ->
                MessageRow(
                    messageModel = message,
                    onEditMessage = onEditMessage,
                    onDeleteMessage = onDeleteMessage
                )
            }
        }
    }
}

@Composable
fun MessageRow(
    messageModel: MessageModel,
    onEditMessage: (MessageModel) -> Unit,
    onDeleteMessage: (MessageModel) -> Unit
) {
    val isModel = messageModel.role == "model"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if (isModel) Color.LightGray else Color.Blue)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = messageModel.message,
                        color = Color.White
                    )
                    if (!isModel) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Edit",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .clickable { onEditMessage(messageModel) }
                            )
                            IconButton(onClick = { onDeleteMessage(messageModel) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Chat Bot",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Composable
fun MessageInput(
    initialMessage: String,
    onMessageSend: (String) -> Unit,
    onCancelEdit: () -> Unit,
    isEditing: Boolean,
    onMicClick: () -> Unit // Mic button callback
) {
    var message by remember { mutableStateOf(initialMessage) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(text = "Type your message") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Send Button
        IconButton(
            onClick = {
                if (message.isNotEmpty()) {
                    onMessageSend(message)
                    message = ""
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
        }

        // Cancel Edit Button
        if (isEditing) {
            IconButton(
                onClick = {
                    onCancelEdit()
                    message = ""
                }
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Edit")
            }
        }

        // Mic Button
        IconButton(
            onClick = { onMicClick() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_mic_24), // Mic drawable
                contentDescription = "Mic",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPagePreview() {
    val mockViewModel = ChatViewModel().apply {
        messageList.addAll(
            listOf(
                MessageModel("Hello, how can I help you?", "model"),
                MessageModel("Can you tell me about Kotlin?", "user"),
                MessageModel("Kotlin is a modern, concise, and safe programming language!", "model")
            )
        )
    }

    ChatPage(viewModel = mockViewModel, askSpeechInput = {})
}
*/


/*
package com.example.easy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easy.ui.theme.ColorModelMessage
import com.example.easy.ui.theme.ColorUserMessage
import com.example.easy.ui.theme.Purple80

@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    var editMessage by remember { mutableStateOf<MessageModel?>(null) }

    Column(modifier = modifier) {
        AppHeader()
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList,
            onEditMessage = { message -> editMessage = message },
            onDeleteMessage = { message -> viewModel.deleteMessage(message) }
        )
        MessageInput(
            initialMessage = editMessage?.message.orEmpty(),
            onMessageSend = { newMessage ->
                if (editMessage != null) {
                    // Update the message
                    viewModel.updateMessage(editMessage!!, newMessage)
                    // Trigger bot response
                    viewModel.sendMessage(newMessage)
                    editMessage = null
                } else {
                    viewModel.sendMessage(newMessage)
                }
            },
            onCancelEdit = {
                editMessage = null
            },
            isEditing = editMessage != null
        )
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>,
    onEditMessage: (MessageModel) -> Unit,
    onDeleteMessage: (MessageModel) -> Unit
) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Purple80,
            )
            Text(text = "Ask me anything", fontSize = 22.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()) { message ->
                MessageRow(
                    messageModel = message,
                    onEditMessage = onEditMessage,
                    onDeleteMessage = onDeleteMessage
                )
            }
        }
    }
}

@Composable
fun MessageRow(
    messageModel: MessageModel,
    onEditMessage: (MessageModel) -> Unit,
    onDeleteMessage: (MessageModel) -> Unit
) {
    val isModel = messageModel.role == "model"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if (isModel) ColorModelMessage else ColorUserMessage)
                    .padding(16.dp)
            ) {
                SelectionContainer {
                    Column {
                        Text(
                            text = messageModel.message,
                            fontWeight = FontWeight.W500,
                            color = Color.White
                        )
                        if (!isModel) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Edit",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clickable { onEditMessage(messageModel) }
                                )
                                IconButton(onClick = { onDeleteMessage(messageModel) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Chat Bot",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Composable
fun MessageInput(
    initialMessage: String,
    onMessageSend: (String) -> Unit,
    onCancelEdit: () -> Unit,
    isEditing: Boolean
) {
    var message by remember { mutableStateOf(initialMessage) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(text = "Type your message") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Send Button
        IconButton(
            onClick = {
                if (message.isNotEmpty()) {
                    onMessageSend(message)
                    message = ""
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
        }

        // Cancel Edit Button
        if (isEditing) {
            IconButton(
                onClick = {
                    onCancelEdit()
                    message = ""
                }
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Edit")
            }
        }

        // Mic Button
        IconButton(
            onClick = {
                // Trigger speech-to-text functionality here
                // Example: call speech input function
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_mic_24), // Replace with actual mic drawable
                contentDescription = "Mic",
                modifier = Modifier.size(24.dp)
            )
        }


    }
}

@Preview(showBackground = true)
@Composable
fun ChatPagePreview() {
    val mockViewModel = ChatViewModel().apply {
        messageList.addAll(
            listOf(
                MessageModel("Hello, how can I help you?", "model"),
                MessageModel("Can you tell me about Kotlin?", "user"),
                MessageModel("Kotlin is a modern, concise, and safe programming language!", "model")
            )
        )
    }

    ChatPage(viewModel = mockViewModel)

}
 */








/*
package com.example.easy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easy.ui.theme.ColorModelMessage
import com.example.easy.ui.theme.ColorUserMessage
import com.example.easy.ui.theme.Purple80

@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    var editMessage by remember { mutableStateOf<MessageModel?>(null) }

    Column(modifier = modifier) {
        AppHeader()
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList,
            onEditMessage = { message -> editMessage = message },
            onDeleteMessage = { message -> viewModel.deleteMessage(message) }
        )
        MessageInput(
            initialMessage = editMessage?.message.orEmpty(),
            onMessageSend = { newMessage ->
                if (editMessage != null) {
                    // Update the message
                    viewModel.updateMessage(editMessage!!, newMessage)
                    // Trigger bot response
                    viewModel.sendMessage(newMessage)
                    editMessage = null
                } else {
                    viewModel.sendMessage(newMessage)
                }
            },
            onCancelEdit = {
                editMessage = null
            },
            isEditing = editMessage != null
        )
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>,
    onEditMessage: (MessageModel) -> Unit,
    onDeleteMessage: (MessageModel) -> Unit
) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Purple80,
            )
            Text(text = "Ask me anything", fontSize = 22.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()) { message ->
                MessageRow(
                    messageModel = message,
                    onEditMessage = onEditMessage,
                    onDeleteMessage = onDeleteMessage
                )
            }
        }
    }
}

@Composable
fun MessageRow(
    messageModel: MessageModel,
    onEditMessage: (MessageModel) -> Unit,
    onDeleteMessage: (MessageModel) -> Unit
) {
    val isModel = messageModel.role == "model"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if (isModel) ColorModelMessage else ColorUserMessage)
                    .padding(16.dp)
            ) {
                SelectionContainer {
                    Column {
                        Text(
                            text = messageModel.message,
                            fontWeight = FontWeight.W500,
                            color = Color.White
                        )
                        if (!isModel) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Edit",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .clickable { onEditMessage(messageModel) }
                                )
                                IconButton(onClick = { onDeleteMessage(messageModel) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Chat Bot",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Composable
fun MessageInput(
    initialMessage: String,
    onMessageSend: (String) -> Unit,
    onCancelEdit: () -> Unit,
    isEditing: Boolean
) {
    var message by remember { mutableStateOf(initialMessage) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text(text = "Type your message") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (message.isNotEmpty()) {
                    onMessageSend(message)
                    message = ""
                }
            }
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send")
        }
        if (isEditing) {
            IconButton(
                onClick = {
                    onCancelEdit()
                    message = ""
                }
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Edit")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPagePreview() {
    val mockViewModel = ChatViewModel().apply {
        messageList.addAll(
            listOf(
                MessageModel("Hello, how can I help you?", "model"),
                MessageModel("Can you tell me about Kotlin?", "user"),
                MessageModel("Kotlin is a modern, concise, and safe programming language!", "model")
            )
        )
    }

    ChatPage(viewModel = mockViewModel)
}







/*
package com.example.easy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easy.ui.theme.ColorModelMessage
import com.example.easy.ui.theme.ColorUserMessage
import com.example.easy.ui.theme.Purple80

@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    var editMessage by remember { mutableStateOf<MessageModel?>(null) }
    var previousMessage by remember { mutableStateOf<String?>(null) } // Store previous message

    Column(modifier = modifier) {
        AppHeader()
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList,
            onEditMessage = { message ->
                editMessage = message
                previousMessage = message.message // Save the previous message content
            }
        )
        MessageInput(
            initialMessage = editMessage?.message.orEmpty(),
            onMessageSend = { newMessage ->
                if (editMessage != null) {
                    // Update the message
                    viewModel.updateMessage(editMessage!!, newMessage)
                    // Trigger the bot response for the updated message
                    viewModel.sendMessage(newMessage) // Call sendMessage to send new message to bot
                    editMessage = null
                    previousMessage = null // Clear the previous message after updating
                } else {
                    viewModel.sendMessage(newMessage)
                }
            },
            onCancelEdit = {
                editMessage = null
                previousMessage = null // Clear the previous message if editing is canceled
            },
            isEditing = editMessage != null,
            previousMessage = previousMessage // Pass the previous message to the input
        )
    }
}

@Composable
fun MessageInput(
    initialMessage: String,
    onMessageSend: (String) -> Unit,
    onCancelEdit: () -> Unit,
    isEditing: Boolean,
    previousMessage: String? // Show previous message content when editing
) {
    var message by remember { mutableStateOf(initialMessage) }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("Type your message...") }
        )
        if (isEditing) {
            Text(
                text = "Editing: $previousMessage", // Show previous message when editing
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp)
            )
            IconButton(onClick = {
                onCancelEdit()
                message = ""
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Edit"
                )
            }
        }
        IconButton(onClick = {
            if (message.isNotEmpty()) {
                onMessageSend(message)
                message = ""
            }
        }) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>,
    onEditMessage: (MessageModel) -> Unit
) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Purple80,
            )
            Text(text = "Ask me anything", fontSize = 22.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()) { message ->
                MessageRow(
                    messageModel = message,
                    onEditMessage = onEditMessage
                )
            }
        }
    }
}

@Composable
fun MessageRow(
    messageModel: MessageModel,
    onEditMessage: (MessageModel) -> Unit
) {
    val isModel = messageModel.role == "model"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if (isModel) ColorModelMessage else ColorUserMessage)
                    .padding(16.dp)
            ) {
                SelectionContainer {
                    Column {
                        Text(
                            text = messageModel.message,
                            fontWeight = FontWeight.W500,
                            color = Color.White
                        )
                        if (!isModel) {
                            Text(
                                text = "Edit",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                                    .clickable { onEditMessage(messageModel) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Chat Bot",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPagePreview() {
    val mockViewModel = ChatViewModel().apply {
        messageList.addAll(
            listOf(
                MessageModel("Hello, how can I help you?", "model"),
                MessageModel("Can you tell me about Kotlin?", "user"),
                MessageModel("Kotlin is a modern, concise, and safe programming language!", "model")
            )
        )
    }

    ChatPage(viewModel = mockViewModel)
}
 */




/*
package com.example.easy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easy.ui.theme.ColorModelMessage
import com.example.easy.ui.theme.ColorUserMessage
import com.example.easy.ui.theme.Purple80



@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    var editMessage by remember { mutableStateOf<MessageModel?>(null) }
    var previousMessage by remember { mutableStateOf<String?>(null) } // Store previous message

    Column(modifier = modifier) {
        AppHeader()
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList,
            onEditMessage = { message ->
                editMessage = message
                previousMessage = message.message // Save the previous message content
            }
        )
        MessageInput(
            initialMessage = editMessage?.message.orEmpty(),
            onMessageSend = { newMessage ->
                if (editMessage != null) {
                    // Update the message
                    viewModel.updateMessage(editMessage!!, newMessage)
                    // Trigger the bot response for the updated message
                    viewModel.sendMessage(newMessage) // Call sendMessage to send new message to bot
                    editMessage = null
                    previousMessage = null // Clear the previous message after updating
                } else {
                    viewModel.sendMessage(newMessage)
                }
            },
            onCancelEdit = {
                editMessage = null
                previousMessage = null // Clear the previous message if editing is canceled
            },
            isEditing = editMessage != null,
            previousMessage = previousMessage // Pass the previous message to the input
        )
    }
}

@Composable
fun MessageInput(
    initialMessage: String,
    onMessageSend: (String) -> Unit,
    onCancelEdit: () -> Unit,
    isEditing: Boolean,
    previousMessage: String? // Show previous message content when editing
) {
    var message by remember { mutableStateOf(initialMessage) }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("Type your message...") }
        )
        if (isEditing) {
            Text(
                text = "Editing: $previousMessage", // Show previous message when editing
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp)
            )
            IconButton(onClick = {
                onCancelEdit()
                message = ""
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Edit"
                )
            }
        }
        IconButton(onClick = {
            if (message.isNotEmpty()) {
                onMessageSend(message)
                message = ""
            }
        }) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>,
    onEditMessage: (MessageModel) -> Unit
) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Purple80,
            )
            Text(text = "Ask me anything", fontSize = 22.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()) { message ->
                MessageRow(
                    messageModel = message,
                    onEditMessage = onEditMessage
                )
            }
        }
    }
}

@Composable
fun MessageRow(
    messageModel: MessageModel,
    onEditMessage: (MessageModel) -> Unit
) {
    val isModel = messageModel.role == "model"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if (isModel) ColorModelMessage else ColorUserMessage)
                    .padding(16.dp)
            ) {
                SelectionContainer {
                    Column {
                        Text(
                            text = messageModel.message,
                            fontWeight = FontWeight.W500,
                            color = Color.White
                        )
                        if (!isModel) {
                            Text(
                                text = "Edit",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                                    .clickable { onEditMessage(messageModel) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Chat Bot",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPagePreview() {
    val mockViewModel = ChatViewModel().apply {
        messageList.addAll(
            listOf(
                MessageModel("Hello, how can I help you?", "model"),
                MessageModel("Can you tell me about Kotlin?", "user"),
                MessageModel("Kotlin is a modern, concise, and safe programming language!", "model")
            )
        )
    }

    ChatPage(viewModel = mockViewModel)
}

 */




/*
@Composable
fun ChatPage(modifier: Modifier = Modifier, viewModel: ChatViewModel) {
    var editMessage by remember { mutableStateOf<MessageModel?>(null) }

    Column(modifier = modifier) {
        AppHeader()
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList,
            onEditMessage = { message ->
                editMessage = message
            }
        )
        MessageInput(
            initialMessage = editMessage?.message.orEmpty(),
            onMessageSend = { newMessage ->
                if (editMessage != null) {
                    viewModel.updateMessage(editMessage!!, newMessage)
                    editMessage = null
                } else {
                    viewModel.sendMessage(newMessage)
                }
            },
            onCancelEdit = {
                editMessage = null
            },
            isEditing = editMessage != null
        )
    }
}

@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    messageList: List<MessageModel>,
    onEditMessage: (MessageModel) -> Unit
) {
    if (messageList.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Purple80,
            )
            Text(text = "Ask me anything", fontSize = 22.sp)
        }
    } else {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()) { message ->
                MessageRow(
                    messageModel = message,
                    onEditMessage = onEditMessage
                )
            }
        }
    }
}

@Composable
fun MessageRow(
    messageModel: MessageModel,
    onEditMessage: (MessageModel) -> Unit
) {
    val isModel = messageModel.role == "model"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if (isModel) ColorModelMessage else ColorUserMessage)
                    .padding(16.dp)
            ) {
                SelectionContainer {
                    Column {
                        Text(
                            text = messageModel.message,
                            fontWeight = FontWeight.W500,
                            color = Color.White
                        )
                        if (!isModel) {
                            Text(
                                text = "Edit",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 4.dp)
                                    .clickable { onEditMessage(messageModel) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInput(
    initialMessage: String,
    onMessageSend: (String) -> Unit,
    onCancelEdit: () -> Unit,
    isEditing: Boolean
) {
    var message by remember { mutableStateOf(initialMessage) }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = { message = it },
            placeholder = { Text("Type your message...") }
        )
        if (isEditing) {
            IconButton(onClick = {
                onCancelEdit()
                message = ""
            }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Edit"
                )
            }
        }
        IconButton(onClick = {
            if (message.isNotEmpty()) {
                onMessageSend(message)
                message = ""
            }
        }) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Chat Bot",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPagePreview() {
    val mockViewModel = ChatViewModel().apply {


        messageList.addAll(
            listOf(
                MessageModel("Hello, how can I help you?", "model"),
                MessageModel("Can you tell me about Kotlin?", "user"),
                MessageModel("Kotlin is a modern, concise, and safe programming language!", "model")
            )
        )
    }

    ChatPage(viewModel = mockViewModel)
}

*/


/*
@Composable
fun ChatPage(modifier: Modifier = Modifier,viewModel: ChatViewModel) {
    Column(
        modifier = modifier
    ) {
        AppHeader()
        MessageList(
            modifier = Modifier.weight(1f),
            messageList = viewModel.messageList
        )
        MessageInput(
            onMessageSend = {
                viewModel.sendMessage(it)
            }
        )
    }
}


@Composable
fun MessageList(modifier: Modifier = Modifier,messageList : List<MessageModel>) {
    if(messageList.isEmpty()){
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.size(60.dp),
                painter = painterResource(id = R.drawable.baseline_question_answer_24),
                contentDescription = "Icon",
                tint = Purple80,
            )
            Text(text = "Ask me anything", fontSize = 22.sp)
        }
    }else{
        LazyColumn(
            modifier = modifier,
            reverseLayout = true
        ) {
            items(messageList.reversed()){
                MessageRow(messageModel = it)
            }
        }
    }


}

@Composable
fun MessageRow(messageModel: MessageModel) {
    val isModel = messageModel.role=="model"

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if (isModel) ColorModelMessage else ColorUserMessage)
                    .padding(16.dp)
            ) {

                SelectionContainer {
                    Text(
                        text = messageModel.message,
                        fontWeight = FontWeight.W500,
                        color = Color.White
                    )
                }


            }

        }


    }


}



@Composable
fun MessageInput(onMessageSend : (String)-> Unit) {

    var message by remember {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = {
                message = it
            }
        )
        IconButton(onClick = {
            if(message.isNotEmpty()){
                onMessageSend(message)
                message = ""
            }

        }) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send"
            )
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Chat Bot",
            color = Color.White,
            fontSize = 22.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatPagePreview() {
    val mockViewModel = ChatViewModel().apply {
        /*messageList.addAll(
            listOf(
                MessageModel("Hello, how can I help you?", "model"),
                MessageModel("Can you tell me about Kotlin?", "user"),
                MessageModel("Kotlin is a modern, concise, and safe programming language!", "model")
            )
        )*/
    }

    ChatPage(viewModel = mockViewModel)
}
*/