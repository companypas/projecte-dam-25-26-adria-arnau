package com.example.pi_androidapp.ui.navigation

/** Define las rutas de navegación de la aplicación. */
sealed class Routes(val route: String) {

    /** Pantalla de splash/carga inicial */
    data object Splash : Routes("splash")

    /** Pantalla de login */
    data object Login : Routes("login")

    /** Pantalla de registro */
    data object Register : Routes("register")

    /** Pantalla principal con lista de productos */
    data object Home : Routes("home")

    /** Pantalla de detalle de producto */
    data object ProductDetail : Routes("product/{productId}") {
        fun createRoute(productId: Int) = "product/$productId"
    }

    /** Pantalla de creación de producto */
    data object CreateProduct : Routes("create_product")

    /** Pantalla de edición de producto */
    data object EditProduct : Routes("edit_product/{productId}") {
        fun createRoute(productId: Int) = "edit_product/$productId"
    }

    /** Pantalla de perfil del usuario */
    data object Profile : Routes("profile")

    /** Pantalla de perfil del vendedor */
    data object SellerProfile : Routes("seller/{sellerId}") {
        fun createRoute(sellerId: Int) = "seller/$sellerId"
    }

    /** Pantalla de mis compras */
    data object MyPurchases : Routes("my_purchases")

    /** Pantalla de mis ventas */
    data object MySales : Routes("my_sales")

    /** Pantalla de mis productos */
    data object MyProducts : Routes("my_products")

    /** Pantalla de lista de conversaciones */
    data object ConversationsList : Routes("conversations")

    /** Pantalla de chat con una conversación existente */
    data object Chat : Routes("chat/{conversacionId}") {
        fun createRoute(conversacionId: Int) = "chat/$conversacionId"
    }

    /** Pantalla de chat iniciado desde un producto */
    data object ChatFromProduct : Routes("chat_product/{productoId}") {
        fun createRoute(productoId: Int) = "chat_product/$productoId"
    }
}
