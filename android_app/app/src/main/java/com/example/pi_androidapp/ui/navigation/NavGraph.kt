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
                    onMyPurchasesClick = { navController.navigate(Routes.MyPurchases.route) }
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

        // My Purchases Screen
        composable(Routes.MyPurchases.route) {
            val viewModel: MyPurchasesViewModel = hiltViewModel()

            MyPurchasesScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }
    }
}
