package com.example.palabro

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

object AppRoutes {
    const val GAME = "game"
    const val SETTINGS = "settings"
    const val STATS = "stats"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Creamos la instancia del ViewModel aquí, para que pueda ser compartida.
    val gameViewModel: GameViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                navController = navController,
                currentRoute = currentRoute,
                closeDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Palabro") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    // Añadimos la sección de "acciones" a la barra.
                    actions = {
                        // Solo mostramos el botón de pista si estamos en la pantalla del juego.
                        if (currentRoute == AppRoutes.GAME) {
                            IconButton(onClick = { gameViewModel.onHintPressed() }) {
                                Icon(Icons.Default.Lightbulb, contentDescription = "Pista")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                startDestination = AppRoutes.GAME
            ) {
                // Le pasamos la instancia del ViewModel a la pantalla del juego.
                composable(AppRoutes.GAME) {
                    GameScreen(gameViewModel)
                }
                composable(AppRoutes.SETTINGS) {
                    SettingsScreen()
                }
                composable(AppRoutes.STATS) {
                    StatsScreen()
                }
            }
        }
    }
}

@Composable
fun AppDrawerContent(
    navController: NavController,
    currentRoute: String?,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))

        val navigateToScreen: (String) -> Unit = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
            closeDrawer()
        }

        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Jugar") },
            selected = currentRoute == AppRoutes.GAME,
            onClick = { navigateToScreen(AppRoutes.GAME) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
            label = { Text("Estadísticas") },
            selected = currentRoute == AppRoutes.STATS,
            onClick = { navigateToScreen(AppRoutes.STATS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            label = { Text("Ajustes") },
            selected = currentRoute == AppRoutes.SETTINGS,
            onClick = { navigateToScreen(AppRoutes.SETTINGS) },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}