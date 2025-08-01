package com.gonzales.prestadmin.presentation.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gonzales.prestadmin.App
import com.gonzales.prestadmin.R

import com.gonzales.prestadmin.data.repository.user.UserRepository
import com.gonzales.prestadmin.presentation.ui.client.ClientFormActivity
import com.gonzales.prestadmin.presentation.ui.login.LoginActivity
import com.gonzales.prestadmin.presentation.ui.reloan.ReloanFormActivity
import com.gonzales.prestadmin.presentation.ui.settings.SettingsActivity
import com.gonzales.prestadmin.presentation.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.presentation.viewmodel.main.MainActivityViewModel
import com.gonzales.prestadmin.util.AppViewModelFactory
import com.gonzales.prestadmin.util.ThemeViewModelProvider
import com.gonzales.prestadmin.util.UserIcon
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themeViewModel = ThemeViewModelProvider.getInstance()
        setContent {
            val isDarkMode by themeViewModel.isDarkMode
            print(isDarkMode)
            // Forzamos la recomposición completa al cambiar el tema
            key(isDarkMode) {
                PrestAdminTheme(darkTheme = isDarkMode) {
                    MainScreen(isDarkTheme = isDarkMode)
                }
            }
        }
    }
}

class ExpandableItem(val icon: ImageVector, val id: Int)
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    viewModel: MainActivityViewModel = viewModel(factory = AppViewModelFactory()), // Usar MainViewModel
    isDarkTheme: Boolean
) {
    // Observar el UserUiModel del MainViewModel
    val userUiModel by viewModel.user.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val drawerWidth = screenWidth * 0.75f

    val isDrawerOpen = drawerState.isOpen
    val rotation by animateFloatAsState(targetValue = if (isDrawerOpen) 180f else 0f, label = "menuRotation")

    var clientesExpanded by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                drawerWidth = drawerWidth,
                onClose = { scope.launch { drawerState.close() } },
                clientesExpanded = clientesExpanded,
                onToggleClientes = { clientesExpanded = !clientesExpanded },
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    user = userUiModel, // Pasar el UserUiModel del ViewModel
                    rotation = rotation,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                ActiveClientListSection(
                    totalClientsValue = "0",
                    recaudadoValue = "0.0",
                    moraValue = "0.0",
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}



@Composable
fun AppDrawer(
    drawerWidth: Dp,
    onClose: () -> Unit,
    clientesExpanded: Boolean,
    onToggleClientes: () -> Unit,
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
            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
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
                    "Nuevo" to ExpandableItem(Icons.Default.PersonAddAlt, 1),
                    "Represtamo" to ExpandableItem(Icons.Default.Repeat, 2)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(user: UserRepository.UserUiModel,
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
                imageUri = user.photoUrl,
                onClick = { expandedMenu = true },
                size = 30.dp
            )

            UserDropdownMenu(user, expanded = expandedMenu, onDismiss = { expandedMenu = false })
        }
    )
}

@Composable
fun UserDropdownMenu(user: UserRepository.UserUiModel, expanded: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
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
                imageUri = user.photoUrl,
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
            leadingIcon = { Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null) }
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
                        App.sessionManager.clearSession()
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
    children: List<Pair<String, ExpandableItem>>
) {
    val context = LocalContext.current
    Column {
        ListItem(
            headlineContent = { Text(title) },
            leadingContent = { Icon(icon, contentDescription = null) },
            trailingContent = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable { onToggle() },
            colors = ListItemDefaults.colors(
                containerColor = DrawerDefaults.modalContainerColor
            )
        )

        if (expanded) {
            children.forEach { (text, childIcon) ->
                ListItem(
                    headlineContent = { Text(text) },
                    leadingContent = { Icon(childIcon.icon, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { when(childIcon.id) {
                            1 -> context.startActivity(Intent(context, ClientFormActivity::class.java))
                            2 -> context.startActivity(Intent(context, ReloanFormActivity::class.java))
                            4 -> context.startActivity(Intent(context, ClientFormActivity::class.java))
                            else -> Toast.makeText(context, "No implementado", Toast.LENGTH_SHORT).show()
                        }

                        },
                    colors = ListItemDefaults.colors(
                        containerColor = DrawerDefaults.modalContainerColor
                    )
                )
            }
        }
    }
}