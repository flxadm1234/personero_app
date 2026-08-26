package com.electoral.personeros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.electoral.personeros.ui.screens.AdminDashboardScreen
import com.electoral.personeros.ui.screens.LoginScreen
import com.electoral.personeros.ui.screens.PersoneroCentroScreen
import com.electoral.personeros.ui.screens.ProfileScreen
import com.electoral.personeros.ui.screens.SolicitudesScreen
import com.electoral.personeros.ui.screens.SuperAdminScreen
import com.electoral.personeros.ui.theme.CleanBlueColorTheme
import com.electoral.personeros.ui.viewmodels.AdminVM
import com.electoral.personeros.ui.viewmodels.LoginViewModel
import com.electoral.personeros.ui.viewmodels.PersoneroCentroVM
import com.electoral.personeros.ui.viewmodels.SuperAdminVM
import com.electoral.personeros.utils.Constants

class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    private val superAdminVM: SuperAdminVM by viewModels()
    private val adminVM: AdminVM by viewModels()
    private val personeroCentroVM: PersoneroCentroVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CleanBlueColorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ElectoralPersonerosApp(
                        loginViewModel = loginViewModel,
                        superAdminVM = superAdminVM,
                        adminVM = adminVM,
                        personeroCentroVM = personeroCentroVM
                    )
                }
            }
        }
    }
}

sealed class Screen(val route: String, val label: String? = null) {
    object Login : Screen("login")
    object SuperAdmin : Screen("super_admin", "Inicio")
    object AdminDashboard : Screen("admin_dashboard", "Inicio")
    object Solicitudes : Screen("solicitudes", "Solicitudes")
    object PersoneroCentro : Screen("personero_centro", "Asignaciones")
    object Perfil : Screen("perfil", "Perfil")
    object Buscar : Screen("buscar", "Buscar")
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("", "Inicio", Icons.Default.Home),
    BottomNavItem(Screen.Solicitudes.route, "Solicitudes", Icons.Default.CalendarMonth),
    BottomNavItem(Screen.PersoneroCentro.route, "Asignaciones", Icons.Default.AssignmentInd),
    BottomNavItem(Screen.Buscar.route, "Buscar", Icons.Default.Search),
    BottomNavItem(Screen.Perfil.route, "Perfil", Icons.Default.Person)
)

@Composable
fun ElectoralPersonerosApp(
    loginViewModel: LoginViewModel,
    superAdminVM: SuperAdminVM,
    adminVM: AdminVM,
    personeroCentroVM: PersoneroCentroVM
) {
    val navController = rememberNavController()
    val loginState by loginViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(loginState.loginSuccess, loginState.tokenData?.rol) {
        if (loginState.loginSuccess) {
            val rol = loginState.tokenData?.rol?.uppercase() ?: Constants.ROLE_PERSONERO_MESA
            val route = when (rol) {
                Constants.ROLE_SUPER_ADMIN -> Screen.SuperAdmin.route
                Constants.ROLE_ADMINISTRADOR -> Screen.AdminDashboard.route
                else -> Screen.PersoneroCentro.route
            }
            navController.navigate(route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
            loginState.tokenData?.provinciaAsignada?.let { adminVM.setProvinciaAsignada(it) }
            loginState.tokenData?.distritoAsignado?.let { adminVM.setDistritoAsignado(it) }
        }
    }

    fun homeRouteFor(rol: String?): String {
        return when (rol?.uppercase()) {
            Constants.ROLE_SUPER_ADMIN -> Screen.SuperAdmin.route
            Constants.ROLE_ADMINISTRADOR -> Screen.AdminDashboard.route
            else -> Screen.PersoneroCentro.route
        }
    }

    val showBottomNav = currentRoute != null && currentRoute != Screen.Login.route

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = Color.White
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        val actualRoute = if (index == 0) homeRouteFor(loginState.tokenData?.rol) else item.route
                        NavigationBarItem(
                            selected = if (index == 0) {
                                currentRoute == Screen.SuperAdmin.route ||
                                    currentRoute == Screen.AdminDashboard.route
                            } else {
                                currentRoute == item.route
                            },
                            onClick = {
                                if (index == 3) {
                                    navController.navigate(Screen.PersoneroCentro.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    return@NavigationBarItem
                                }
                                navController.navigate(actualRoute) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(item.icon, contentDescription = item.label, modifier = Modifier.padding(bottom = 2.dp))
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomNav) {
                FloatingActionButton(
                    onClick = {
                        val rol = loginState.tokenData?.rol?.uppercase()
                        when {
                            rol == Constants.ROLE_SUPER_ADMIN -> navController.navigate(Screen.Solicitudes.route)
                            else -> navController.navigate(Screen.PersoneroCentro.route)
                        }
                    },
                    containerColor = Color(0xFF7B1FA2),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Nuevo")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = loginViewModel,
                    onNavigateToSolicitudes = {
                        navController.navigate(Screen.Solicitudes.route)
                    }
                )
            }
            composable(Screen.SuperAdmin.route) {
                SuperAdminScreen(
                    viewModel = superAdminVM,
                    onLogout = {
                        loginViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.AdminDashboard.route)
                    },
                    onNavigateToSolicitudes = {
                        navController.navigate(Screen.Solicitudes.route)
                    },
                    onNavigateToPersoneros = {
                        navController.navigate(Screen.PersoneroCentro.route)
                    }
                )
            }
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    viewModel = adminVM,
                    onLogout = {
                        loginViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSolicitudes = {
                        navController.navigate(Screen.Solicitudes.route)
                    },
                    onNavigateToPersoneros = {
                        navController.navigate(Screen.PersoneroCentro.route)
                    }
                )
            }
            composable(Screen.Solicitudes.route) {
                SolicitudesScreen(
                    viewModel = adminVM,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        loginViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.PersoneroCentro.route) {
                PersoneroCentroScreen(
                    viewModel = personeroCentroVM,
                    adminViewModel = adminVM,
                    provincia = loginState.tokenData?.provinciaAsignada,
                    distrito = loginState.tokenData?.distritoAsignado,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        loginViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Buscar.route) {
                PersoneroCentroScreen(
                    viewModel = personeroCentroVM,
                    adminViewModel = adminVM,
                    provincia = loginState.tokenData?.provinciaAsignada,
                    distrito = loginState.tokenData?.distritoAsignado,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        loginViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Perfil.route) {
                ProfileScreen(
                    tokenRol = loginState.tokenData?.rol,
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        loginViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
