package com.example.clientemovil.ui.screens.more

/**
 * Define todas las rutas de navegación como objetos sellados.
 * Esto ayuda a mantener la seguridad de tipos y a evitar errores de ruta.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object Main : Screen("main_screen")
    object Home : Screen("home_screen")
    object Products : Screen("products_screen/{canEdit}") {
        fun createRoute(canEdit: Boolean): String {
            return "products_screen/$canEdit"
        }
    }
    object ProductForm : Screen("product_form_screen/{productId}")
    object Authors : Screen("authors_screen")
    object AuthorForm : Screen("author_form_screen/{authorId}")
    object Genres : Screen("genres_screen")
    object GenreForm : Screen("genre_form_screen/{genreId}")
    object Publishers : Screen("publishers_screen")
    object PublisherForm : Screen("publisher_form_screen/{publisherId}")
    object Roles : Screen("roles_screen")
    // Aquí está la ruta que faltaba
    object RoleForm : Screen("role_form_screen/{roleId}")
    object Users : Screen("users_screen")
    object UserForm : Screen("user_form_screen/{userId}")
    object Sales : Screen("sales_screen/{canEdit}") {
        fun createRoute(canEdit: Boolean): String {
            return "sales_screen/$canEdit"
        }
    }
    object SaleForm : Screen("sale_form_screen/{saleId}")
    object UsersWithRoles : Screen("users_with_roles_screen/{canEdit}") {
        fun createRoute(canEdit: Boolean): String {
            return "users_with_roles_screen/$canEdit"
        }
    }
    object About : Screen("about_screen")
}
