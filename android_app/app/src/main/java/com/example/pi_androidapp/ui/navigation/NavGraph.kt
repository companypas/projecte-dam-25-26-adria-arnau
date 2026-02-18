package com.example.pi_androidapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.pi_androidapp.ui.screens.auth.LoginScreen
import com.example.pi_androidapp.ui.screens.auth.LoginViewModel
import com.example.pi_androidapp.ui.screens.auth.RegisterScreen
import com.example.pi_androidapp.ui.screens.auth.RegisterViewModel
import com.example.pi_androidapp.ui.screens.chat.ChatScreen
import com.example.pi_androidapp.ui.screens.chat.ChatViewModel
import com.example.pi_androidapp.ui.screens.chat.ConversationsListScreen
import com.example.pi_androidapp.ui.screens.chat.ConversationsListViewModel
import com.example.pi_androidapp.ui.screens.home.HomeScreen
import com.example.pi_androidapp.ui.screens.home.HomeViewModel
import com.example.pi_androidapp.ui.screens.product.CreateProductScreen
import com.example.pi_androidapp.ui.screens.product.CreateProductViewModel
import com.example.pi_androidapp.ui.screens.product.ProductDetailScreen
import com.example.pi_androidapp.ui.screens.product.ProductDetailViewModel
import com.example.pi_androidapp.ui.screens.profile.ProfileScreen
import com.example.pi_androidapp.ui.screens.profile.ProfileViewModel
import com.example.pi_androidapp.ui.screens.purchases.MyPurchasesScreen
import com.example.pi_androidapp.ui.screens.purchases.MyPurchasesViewModel
import com.example.pi_androidapp.ui.screens.sales.MySalesScreen
import com.example.pi_androidapp.ui.screens.sales.MySalesViewModel
import com.example.pi_androidapp.ui.screens.seller.SellerProfileScreen
import com.example.pi_androidapp.ui.screens.seller.SellerProfileViewModel
import com.example.pi_androidapp.ui.screens.splash.SplashScreen
import com.example.pi_androidapp.ui.screens.splash.SplashViewModel

/**
 * Navegación principal de la aplicación. Define todas las rutas y sus pantallas correspondientes.
 *
 * @param navController Controlador de navegación
 * @param startDestination Destino inicial de la navegación
 */
@Composable
fun NavGraph(navController: NavHostController, startDestination: String = Routes.Splash.route) {
    NavHost(navController = navController, startDestination = startDestination) {
        // Splash Screen
        composable(Routes.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()

            LaunchedEffect(isLoggedIn) {
                when (isLoggedIn) {
                    true -> {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    }
                    false -> {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(Routes.Splash.route) { inclusive = true }
                        }
                    }
                    null -> {
                        /* Still loading */
                    }
                }
            }

            SplashScreen()
        }

        // Login Screen
        composable(Routes.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()

            LoginScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate(Routes.Register.route) },
                    onLoginSuccess = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    }
            )
        }

        // Register Screen
        composable(Routes.Register.route) {
            val viewModel: RegisterViewModel = hiltViewModel()

            RegisterScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    }
            )
        }

        // Home Screen
        composable(Routes.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()

            HomeScreen(
                    viewModel = viewModel,
                    onProductClick = { productId ->
                        navController.navigate(Routes.ProductDetail.createRoute(productId))
                    },
                    onCreateProductClick = { navController.navigate(Routes.CreateProduct.route) },
                    onProfileClick = { navController.navigate(Routes.Profile.route) },
                    onMyPurchasesClick = { navController.navigate(Routes.MyPurchases.route) },
                    onMySalesClick = { navController.navigate(Routes.MySales.route) },
                    onChatsClick = { navController.navigate(Routes.ConversationsList.route) }
            )
        }

        // Product Detail Screen
        composable(
                route = Routes.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            val viewModel: ProductDetailViewModel = hiltViewModel()

            LaunchedEffect(productId) { viewModel.loadProduct(productId) }

            ProductDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onPurchaseSuccess = {
                        navController.navigate(Routes.MyPurchases.route) {
                            popUpTo(Routes.Home.route)
                        }
                    },
                    onSellerClick = { sellerId ->
                        navController.navigate(Routes.SellerProfile.createRoute(sellerId))
                    },
                    onChatClick = { productoId ->
                        navController.navigate(Routes.ChatFromProduct.createRoute(productoId))
                    }
            )
        }

        // Create Product Screen
        composable(Routes.CreateProduct.route) {
            val viewModel: CreateProductViewModel = hiltViewModel()

            CreateProductScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onProductCreated = {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
            )
        }

        // Profile Screen
        composable(Routes.Profile.route) {
            val viewModel: ProfileViewModel = hiltViewModel()

            ProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
            )
        }

        // Seller Profile Screen
        composable(
                route = Routes.SellerProfile.route,
                arguments = listOf(navArgument("sellerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getInt("sellerId") ?: 0
            val viewModel: SellerProfileViewModel = hiltViewModel()

            LaunchedEffect(sellerId) { viewModel.loadSeller(sellerId) }

            SellerProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { productId ->
                        navController.navigate(Routes.ProductDetail.createRoute(productId))
                    }
            )
        }

        // My Purchases Screen
        composable(Routes.MyPurchases.route) {
            val viewModel: MyPurchasesViewModel = hiltViewModel()

            MyPurchasesScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // My Sales Screen
        composable(Routes.MySales.route) {
            val viewModel: MySalesViewModel = hiltViewModel()

            MySalesScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // Conversations List Screen
        composable(Routes.ConversationsList.route) {
            val viewModel: ConversationsListViewModel = hiltViewModel()

            ConversationsListScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onConversationClick = { conversacionId ->
                        navController.navigate(Routes.Chat.createRoute(conversacionId))
                    }
            )
        }

        // Chat Screen (existing conversation)
        composable(
                route = Routes.Chat.route,
                arguments = listOf(navArgument("conversacionId") { type = NavType.IntType })
        ) {
            val viewModel: ChatViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            ChatScreen(
                    viewModel = viewModel,
                    otroUsuarioNombre = null,
                    onBackClick = { navController.popBackStack() }
            )
        }

        // Chat Screen (from product — initiates chat first)
        composable(
                route = Routes.ChatFromProduct.route,
                arguments = listOf(navArgument("productoId") { type = NavType.IntType })
        ) {
            val viewModel: ChatViewModel = hiltViewModel()

            ChatScreen(
                    viewModel = viewModel,
                    otroUsuarioNombre = null,
                    onBackClick = { navController.popBackStack() }
            )
        }
    }
}

