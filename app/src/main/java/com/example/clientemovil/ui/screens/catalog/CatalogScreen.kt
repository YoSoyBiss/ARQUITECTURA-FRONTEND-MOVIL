package com.example.clientemovil

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Business
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Define las rutas de navegación y los iconos para las pantallas de catálogos.
 * Cada objeto representa una pantalla única con una ruta y un ícono asociados.
 */
sealed class CatalogScreen(val route: String, val label: String, val icon: ImageVector) {
    // Pantallas principales de los catálogos con sus listas
    object Authors : CatalogScreen("authors_screen", "Autores", Icons.Default.Person)
    object Genres : CatalogScreen("genres_screen", "Géneros", Icons.Default.List)
    object Publishers : CatalogScreen("publishers_screen", "Editoriales", Icons.Default.Business)

    // Pantallas de formulario para crear/editar elementos de los catálogos
    object AuthorsForm : CatalogScreen("authors_form/{authorId}", "Autor Form", Icons.Default.Person)
    object GenresForm : CatalogScreen("genres_form/{genreId}", "Género Form", Icons.Default.List)
    object PublishersForm : CatalogScreen("publishers_form/{publisherId}", "Editorial Form", Icons.Default.Business)
}

/**
 * Lista de los elementos de navegación para la barra inferior.
 * Solo incluye las pantallas de la lista (Autores, Géneros, Editoriales).
 */
val catalogItems = listOf(
    CatalogScreen.Authors,
    CatalogScreen.Genres,
    CatalogScreen.Publishers
)
