package com.example.clientemovil.ui.screens.more


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Pantalla de "Acerca de nosotros".
 * Muestra información de contacto.
 */
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Comunícate con nosotros para más información.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Email: contacto@ejemplo.com",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Teléfono: +52 123 456 7890",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
