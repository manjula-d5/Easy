package com.example.easy

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Utility class to save and load the chat history
class ChatHistoryManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ChatHistory", Context.MODE_PRIVATE)

    // Save chat history
    fun saveChatHistory(messageList: List<MessageModel>) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(messageList)
        editor.putString("chat_history", json)
        editor.apply() // Save the data asynchronously
    }

    // Load chat history
    fun loadChatHistory(): List<MessageModel> {
        val json = sharedPreferences.getString("chat_history", "[]") // Default to empty list if no history
        val type = object : TypeToken<List<MessageModel>>() {}.type
        return Gson().fromJson(json, type)
    }

    // Clear chat history
    fun clearChatHistory() {
        val editor = sharedPreferences.edit()
        editor.remove("chat_history") // Remove the stored chat history
        editor.apply()
    }
}
