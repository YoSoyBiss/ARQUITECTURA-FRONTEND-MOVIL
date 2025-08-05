package com.example.clientemovil

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.clientemovil.network.laravel.LaravelRetrofitClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Greeting("Android")
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = LaravelRetrofitClient.api.getAllProducts()
                if (response.isSuccessful) {
                    val productos = response.body()
                    Log.d("API", "Productos: $productos")
                } else {
                    Log.e("API", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Excepción: ${e.message}")
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}
