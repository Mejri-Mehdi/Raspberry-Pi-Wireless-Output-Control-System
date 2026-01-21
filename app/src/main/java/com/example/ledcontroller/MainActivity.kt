package com.example.ledcontroller

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Allow network operations on the main thread (not recommended for production, but okay for demo)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val buttonOn = findViewById<Button>(R.id.button_led_on)
        val buttonOff = findViewById<Button>(R.id.button_led_off)

        // Replace with your Raspberry Pi's IP address
        val serverUrl = "http://192.168.100.11:5000/led"

        // Set listeners for buttons
        buttonOn.setOnClickListener { v: View? ->
            sendPostRequest(serverUrl, "on")
        }
        buttonOff.setOnClickListener { v: View? ->
            sendPostRequest(serverUrl, "off")
        }
    }

    private fun sendPostRequest(serverUrl: String, action: String) {
        var responseMessage: String? = null
        try {
            // Open a connection to the Flask server
            val url = URL(serverUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            // Prepare the POST data
            val postData = "action=$action"

            // Send POST data
            connection.outputStream.use { os ->
                val input = postData.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }

            // Read the server response
            val responseCode = connection.responseCode
            val responseMessage = connection.inputStream.bufferedReader().use { it.readText() }

            // Log server response and show Toast
            if (responseCode == HttpURLConnection.HTTP_OK) {
                runOnUiThread {
                    Toast.makeText(this, "LED turned $action", Toast.LENGTH_SHORT).show()
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "Error: $responseCode, $responseMessage", Toast.LENGTH_SHORT).show()
                }
            }

            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
