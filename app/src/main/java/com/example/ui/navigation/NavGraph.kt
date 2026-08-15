package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.TokenManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.NovelRepository
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.details.NovelDetailsScreen
import com.example.ui.screens.details.NovelDetailsViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.reader.ReaderScreen
import com.example.ui.screens.reader.ReaderViewModel
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

sealed class Screen(val route: String, val title: String) {
    object Home : Screen("home", "Home")
    object Library : Screen("library", "Library")
    object Profile : Screen("profile", "Profile")
    object Login : Screen("login", "Login")
    object Register : Screen("register", "Register")
    object NovelDetails : Screen("novel_details/{slug}", "Novel Details") {
        fun createRoute(slug: String) = "novel_details/$slug"
    }
    object Reader : Screen("reader/{chapterSlug}", "Reader") {
        fun createRoute(chapterSlug: String) = "reader/$chapterSlug"
    }
}

@Composable
fun BlogVerseAppNavGraph() {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val authRepository = remember { AuthRepository(tokenManager) }
    val novelRepository = remember { NovelRepository(tokenManager) }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(Screen.Home.route, Screen.Library.route, Screen.Profile.route)
    val shouldShowBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Screen.Home.route,
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == Screen.Home.route) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.Library.route,
                        onClick = {
                            navController.navigate(Screen.Library.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == Screen.Library.route) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Library"
                            )
                        },
                        label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentRoute == Screen.Profile.route,
                        onClick = {
                            navController.navigate(Screen.Profile.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentRoute == Screen.Profile.route) Icons.Filled.Person else Icons.Outlined.PersonOutline,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = AmberPrimary,
                            indicatorColor = AmberPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                val homeViewModel = remember { HomeViewModel(novelRepository, authRepository) }
                HomeScreen(
                    viewModel = homeViewModel,
                    onNovelClick = { novel ->
                        navController.navigate(Screen.NovelDetails.createRoute(novel.slug))
                    },
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onLoginClick = { navController.navigate(Screen.Login.route) },
                    onSearchClick = { /* Integrated in Home filter pills */ }
                )
            }

            // Novel Details Screen
            composable(
                route = Screen.NovelDetails.route,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug") ?: ""
                val detailsViewModel = remember(slug) { NovelDetailsViewModel(slug, novelRepository, authRepository) }
                NovelDetailsScreen(
                    viewModel = detailsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onChapterClick = { chapterSlug ->
                        navController.navigate(Screen.Reader.createRoute(chapterSlug))
                    },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            // Reader Screen with 10-Second Ad Lock Engine
            composable(
                route = Screen.Reader.route,
                arguments = listOf(navArgument("chapterSlug") { type = NavType.StringType })
            ) { backStackEntry ->
                val chapterSlug = backStackEntry.arguments?.getString("chapterSlug") ?: ""
                val readerViewModel = remember(chapterSlug) { ReaderViewModel(chapterSlug, novelRepository, authRepository) }
                ReaderScreen(
                    viewModel = readerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateChapter = { targetSlug ->
                        navController.navigate(Screen.Reader.createRoute(targetSlug)) {
                            popUpTo(Screen.Reader.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            // Login Screen
            composable(Screen.Login.route) {
                val authViewModel = remember { AuthViewModel(authRepository) }
                LoginScreen(
                    viewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = { navController.popBackStack() }
                )
            }

            // Register Screen
            composable(Screen.Register.route) {
                val authViewModel = remember { AuthViewModel(authRepository) }
                RegisterScreen(
                    viewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onRegisterSuccess = { navController.popBackStack() }
                )
            }

            // Library Screen
            composable(Screen.Library.route) {
                LibraryScreen(
                    novelRepository = novelRepository,
                    onNovelClick = { novel ->
                        navController.navigate(Screen.NovelDetails.createRoute(novel.slug))
                    }
                )
            }

            // Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen(
                    authRepository = authRepository,
                    tokenManager = tokenManager,
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
        }
    }
}
