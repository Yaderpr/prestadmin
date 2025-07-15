package com.gonzales.prestadmin.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.ui.theme.PrestAdminTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.gonzales.prestadmin.R
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.data.SessionPreferences
import com.gonzales.prestadmin.data.UsuarioPrefs
import com.gonzales.prestadmin.repository.UserRepository
import com.gonzales.prestadmin.ui.component.UserIcon
import com.gonzales.prestadmin.viewmodel.UserUiModel

class MainActivity : ComponentActivity() {
    private lateinit var darkThemePrefs: DarkThemePreferences
    private lateinit var usuarioPrefs: UsuarioPrefs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        darkThemePrefs = DarkThemePreferences(applicationContext)
        usuarioPrefs = UsuarioPrefs(applicationContext)
        val userRepository = UserRepository(usuarioPrefs)
        setContent {
            val isDarkMode by darkThemePrefs.isDarkMode.collectAsState(initial = false)
            PrestAdminTheme(darkTheme = isDarkMode) {
                MainScreen(userRepository = userRepository,
                    isDarkTheme = isDarkMode,
                    onToggleTheme = { enabled ->
                        lifecycleScope.launch {
                            darkThemePrefs.setDarkMode(enabled)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userRepository: UserRepository, isDarkTheme: Boolean,
               onToggleTheme: (Boolean) -> Unit) {
    val userUiModel by userRepository.userFlow.collectAsState(initial = UserUiModel("Cargando...", null))
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val drawerWidth = screenWidth * 0.75f

    val isDrawerOpen = drawerState.isOpen
    val rotation by animateFloatAsState(targetValue = if (isDrawerOpen) 180f else 0f, label = "menuRotation")

    // Estados para expansión de grupos
    var clientesExpanded by remember { mutableStateOf(false) }
    var pagosExpanded by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(isDarkTheme = isDarkTheme,
                drawerWidth = drawerWidth,
                onClose = { scope.launch { drawerState.close() } },
                clientesExpanded = clientesExpanded,
                onToggleClientes = { clientesExpanded = !clientesExpanded },
                pagosExpanded = pagosExpanded,
                onTogglePagos = { pagosExpanded = !pagosExpanded }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(userUiModel,
                    rotation = rotation,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResumenCard(
                        icon = Icons.Default.Group,
                        titulo = "Clientes",
                        valor = "0",
                        isDarkTheme = isDarkTheme
                    )

                    ResumenCard(
                        icon = Icons.Default.AttachMoney,
                        titulo = "Recaudado",
                        valor = "0.0",
                        isDarkTheme =isDarkTheme
                    )

                    ResumenCard(
                        icon = Icons.Default.Warning,
                        titulo = "Mora",
                        valor = "0.0",
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }

    }
}

@Composable
fun AppDrawer(
    isDarkTheme: Boolean,
    drawerWidth: Dp,
    onClose: () -> Unit,
    clientesExpanded: Boolean,
    onToggleClientes: () -> Unit,
    pagosExpanded: Boolean,
    onTogglePagos: () -> Unit,
) {
    val color: Color = DrawerDefaults.modalContainerColor
    ModalDrawerSheet(modifier = Modifier.width(drawerWidth), drawerContainerColor = color) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar menú")
            }
        }

        Image(
            painter = painterResource(id = R.drawable.ic_splash_logo),
            contentDescription = "Logo",
            colorFilter = ColorFilter.tint(color = if(isDarkTheme) Color.LightGray else Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(bottom = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            DrawerExpandableGroup(
                title = "Clientes",
                icon = Icons.Default.Person,
                expanded = clientesExpanded,
                onToggle = onToggleClientes,
                children = listOf(
                    "Listado" to Icons.AutoMirrored.Filled.List,
                    "Nuevo" to Icons.Default.PersonAdd,
                    "Morosos" to Icons.Default.Warning
                ),
                color = if(isDarkTheme) Color.LightGray else Color.Black,
                parentColor = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            DrawerExpandableGroup(
                title = "Pagos",
                icon = Icons.Default.AttachMoney,
                expanded = pagosExpanded,
                onToggle = onTogglePagos,
                children = listOf(
                    "Abonar" to Icons.Default.Payment
                ),
                color = if(isDarkTheme) Color.LightGray else Color.Black,
                parentColor = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(user: UserUiModel,
    rotation: Float,
    onMenuClick: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Inicio") },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menú",
                    modifier = Modifier.rotate(rotation)
                )
            }
        },
        actions = {
            UserIcon(
                fotoUri = user.fotoUri,
                onClick = { expandedMenu = true },
                size = 30.dp
            )

            UserDropdownMenu(user, expanded = expandedMenu, onDismiss = { expandedMenu = false })
        }
    )
}

@Composable
fun UserDropdownMenu(user: UserUiModel, expanded: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val sessionPrefs: SessionPreferences = remember { SessionPreferences(context) }
    var showDialog by remember { mutableStateOf(false) }

    // Menú desplegable principal
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(240.dp)
            .padding(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            UserIcon(
                fotoUri = user.fotoUri,
                onClick = {},
                size = 72.dp
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        DropdownMenuItem(
            text = { Text("Ajustes") },
            onClick = {
                onDismiss()
                context.startActivity(Intent(context, SettingsActivity::class.java))
            },
            leadingIcon = { Icon(imageVector = Icons.Default.Settings, contentDescription = null) }
        )

        DropdownMenuItem(
            text = { Text("Cerrar sesión") },
            onClick = {
                onDismiss()
                showDialog = true
            },
            leadingIcon = { Icon(imageVector = Icons.Default.Logout, contentDescription = null) }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    // Cerrar sesión con DataStore
                    activity?.lifecycleScope?.launch {
                        sessionPrefs.cerrarSesion()

                        // Navegar a LoginActivity y cerrar esta
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        activity.finish()
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

}


@Composable
fun DrawerExpandableGroup(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onToggle: () -> Unit,
    children: List<Pair<String, ImageVector>>,
    color: Color = MaterialTheme.colorScheme.onSurface,
    parentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val context = LocalContext.current
    Column {
        ListItem(
            headlineContent = { Text(title, color = color) },
            leadingContent = { Icon(icon, contentDescription = null, tint = color) },
            trailingContent = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = color
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            colors = ListItemDefaults.colors(containerColor = parentColor)
        )

        if (expanded) {
            children.forEach { (text, childIcon) ->
                ListItem(
                    headlineContent = { Text(text, color = color) },
                    leadingContent = { Icon(childIcon, contentDescription = null, tint = color) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .clickable { when(text) {
                            "Nuevo" -> context.startActivity(Intent(context, ClienteFormActivity::class.java))
                            "Listado" -> context.startActivity(Intent(context, ListadoClienteActivity::class.java))
                            else -> Unit
                        }

                        },
                    colors = ListItemDefaults.colors(containerColor = parentColor),
                )
            }
        }
    }
}
@Composable
fun ResumenCard(
    icon: ImageVector,
    titulo: String,
    valor: String,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.15f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if(isDarkTheme) Color.LightGray else Color.Black,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = valor,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

