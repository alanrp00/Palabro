package com.example.palabro

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val gameViewModel: GameViewModel = viewModel()
    val uiState by gameViewModel.uiState.collectAsState()

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
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentRoute == AppRoutes.GAME) {
                                WordLengthSelector(
                                    selectedLength = uiState.wordLength,
                                    onLengthSelected = { gameViewModel.changeWordLength(it) }
                                )
                            } else {
                                val titleText = when (currentRoute) {
                                    AppRoutes.SETTINGS -> "Ajustes"
                                    AppRoutes.STATS -> "Estadísticas"
                                    else -> ""
                                }
                                Text(titleText, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                    },
                    actions = {
                        // --- INICIO DE LA CORRECCIÓN ---
                        // El botón de la pista está aquí, y su visibilidad depende de la ruta.
                        if (currentRoute == AppRoutes.GAME) {
                            IconButton(onClick = { gameViewModel.onHintPressed() }) {
                                Icon(Icons.Default.Lightbulb, contentDescription = "Pista")
                            }
                        } else {
                            // Cuando no estamos en el juego, dejamos un espacio en blanco
                            // del mismo tamaño que un icono para que el título se centre bien.
                            Spacer(Modifier.width(48.dp))
                        }
                        // --- FIN DE LA CORRECCIÓN ---
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                startDestination = AppRoutes.GAME
            ) {
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


// --- VERSIÓN COMPACTA DEL WORDLENGTHSELECTOR RESTAURADA ---
@Composable
fun WordLengthSelector(
    selectedLength: Int,
    onLengthSelected: (Int) -> Unit
) {
    val options = listOf(5, 6, 7)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEachIndexed { index, length ->
            val isSelected = selectedLength == length
            val contentColor by androidx.compose.animation.animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(durationMillis = 300),
                label = "ContentColorAnimation"
            )

            Text(
                text = "$length Letras",
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable { onLengthSelected(length) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            if (index < options.size - 1) {
                Divider(
                    modifier = Modifier
                        .height(16.dp)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
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