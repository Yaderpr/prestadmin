package com.gonzales.prestadmin.misc.view.main
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.LastPage
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.gonzales.prestadmin.R
import com.gonzales.prestadmin.data.DarkThemePreferences
import com.gonzales.prestadmin.data.SessionPreferences
import com.gonzales.prestadmin.data.UsuarioPrefs
import com.gonzales.prestadmin.presentation.ui.client.ClientFormActivity
import com.gonzales.prestadmin.misc.repository.UserRepository
import com.gonzales.prestadmin.misc.ui.component.UserIcon
import com.gonzales.prestadmin.misc.ui.theme.PrestAdminTheme
import com.gonzales.prestadmin.misc.view.LoanListActivity
import com.gonzales.prestadmin.misc.view.LoanRequestActivity
import com.gonzales.prestadmin.misc.view.LoginActivity
import com.gonzales.prestadmin.view.MultiStepFormActivity
import com.gonzales.prestadmin.misc.view.SettingsActivity
import com.gonzales.prestadmin.misc.view.cliente.ClientListActivity
import com.gonzales.prestadmin.viewmodel.UserUiModel
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private lateinit var darkThemePrefs: DarkThemePreferences
    private lateinit var usuarioPrefs: UsuarioPrefs
    @RequiresApi(Build.VERSION_CODES.O)
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
class ExpandableItem(val icon: ImageVector, val id: Int)
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
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

    var textoBusqueda by remember { mutableStateOf("") }
    var paginaActual by remember { mutableStateOf(0) }

    // Estados para expansión de grupos
    var clientesExpanded by remember { mutableStateOf(false) }
    var pagosExpanded by remember { mutableStateOf(false) }
    var prestamoExpanded by remember { mutableStateOf(false) }

    val listaClientes = listOf(
        ClienteUI("001", "PR001", "Carlos", "Perez", LocalDate.of(2025, 7, 17), LocalDate.of(2025, 9, 10)),
        ClienteUI("002", "PR002", "Lucia", "Martinez", LocalDate.of(2025, 7, 16), LocalDate.of(2025, 8, 16)),
        ClienteUI("003", "PR003", "Pedro", "Gomez", LocalDate.of(2025, 7, 15), LocalDate.of(2025, 7, 1)),
        ClienteUI("004", "PR004", "Sofia", "Ramirez", LocalDate.of(2025, 7, 14), LocalDate.of(2025, 8, 14)),
        ClienteUI("005", "PR005", "Andres", "Lopez", LocalDate.of(2025, 7, 13), LocalDate.of(2025, 10, 13)),
        ClienteUI("006", "PR006", "Fernanda", "Torres", LocalDate.of(2025, 7, 12), LocalDate.of(2025, 8, 12)),
        ClienteUI("007", "PR007", "Miguel", "Hernandez", LocalDate.of(2025, 7, 11), LocalDate.of(2025, 9, 11)),
        ClienteUI("008", "PR008", "Daniela", "Castillo", LocalDate.of(2025, 7, 10), LocalDate.of(2025, 8, 25)),
        ClienteUI("009", "PR009", "Luis", "Garcia", LocalDate.of(2025, 7, 9), LocalDate.of(2025, 10, 9)),
        ClienteUI("010", "PR010", "Paola", "Sanchez", LocalDate.of(2025, 7, 8), LocalDate.of(2025, 8, 20)),
        ClienteUI("011", "PR011", "Jorge", "Diaz", LocalDate.of(2025, 7, 7), LocalDate.of(2025, 9, 22)),
        ClienteUI("012", "PR012", "Mariana", "Vega", LocalDate.of(2025, 7, 6), LocalDate.of(2025, 9, 6)),
        ClienteUI("013", "PR013", "Ricardo", "Navarro", LocalDate.of(2025, 7, 5), LocalDate.of(2025, 8, 5)),
        ClienteUI("014", "PR014", "Valeria", "Ortega", LocalDate.of(2025, 7, 4), LocalDate.of(2025, 7, 4)),
        ClienteUI("015", "PR015", "Emilio", "Morales", LocalDate.of(2025, 7, 3), LocalDate.of(2025, 7, 3))
    )


    val clientesFiltrados = listaClientes.filter {
        it.abonado.contains(textoBusqueda, ignoreCase = true) ||
                it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                it.apellido.contains(textoBusqueda, ignoreCase = true)
    }

    val porPagina = 10
    val totalPaginas = (clientesFiltrados.size + porPagina - 1) / porPagina
    val clientesPagina = clientesFiltrados.drop(paginaActual * porPagina).take(porPagina)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(isDarkTheme = isDarkTheme,
                drawerWidth = drawerWidth,
                onClose = { scope.launch { drawerState.close() } },
                clientesExpanded = clientesExpanded,
                onToggleClientes = { clientesExpanded = !clientesExpanded },
                pagosExpanded = pagosExpanded,
                onTogglePagos = { pagosExpanded = !pagosExpanded },
                prestamoExpanded = prestamoExpanded,
                onTogglePrestamo = { prestamoExpanded = !prestamoExpanded}
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    user = userUiModel,
                    rotation = rotation,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                // Cards de resumen
                item {
                    ResumenCard(
                        icon = Icons.Default.Group,
                        titulo = "Clientes",
                        valor = "0",
                        isDarkTheme = isDarkTheme
                    )
                }
                item {
                    ResumenCard(
                        icon = Icons.Default.AttachMoney,
                        titulo = "Recaudado",
                        valor = "0.0",
                        isDarkTheme = isDarkTheme
                    )
                }
                item {
                    ResumenCard(
                        icon = Icons.Default.Warning,
                        titulo = "Mora",
                        valor = "0.0",
                        isDarkTheme = isDarkTheme
                    )
                }

                // Título + buscador
                item {
                    Text("Mis Clientes", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = textoBusqueda,
                        onValueChange = {
                            textoBusqueda = it
                            paginaActual = 0
                        },
                        label = { Text("Buscar abonado, nombre o apellido") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(MaterialTheme.colorScheme.onSurface)
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Código",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.surface
                        )
                        Text(
                            "Nombres",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.surface
                        )
                        Text(
                            "Apellidos",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                }


                items(clientesPagina) { cliente ->
                    val colorFondo = obtenerColorFila(cliente)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorFondo, shape = MaterialTheme.shapes.extraSmall)
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                            .clip(MaterialTheme.shapes.extraSmall),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("P_${cliente.abonado.padStart(3, '0')}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(cliente.nombre, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        Text(cliente.apellido, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }


                // Mensaje si no hay resultados
                if (clientesFiltrados.isEmpty()) {
                    item {
                        Text(
                            "No se encontraron resultados.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Paginación
                if (clientesFiltrados.isNotEmpty()) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { paginaActual = 0 }, enabled = paginaActual > 0) {
                                Icon(Icons.Default.FirstPage, contentDescription = "Primera página")
                            }
                            IconButton(onClick = { if (paginaActual > 0) paginaActual-- }, enabled = paginaActual > 0) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior")
                            }
                            Text("Página ${paginaActual + 1} de $totalPaginas")
                            IconButton(onClick = { if (paginaActual < totalPaginas - 1) paginaActual++ }, enabled = paginaActual < totalPaginas - 1) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente")
                            }
                            IconButton(onClick = { paginaActual = totalPaginas - 1 }, enabled = paginaActual < totalPaginas - 1) {
                                Icon(Icons.AutoMirrored.Filled.LastPage, contentDescription = "Última página")
                            }
                        }
                    }
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
    prestamoExpanded: Boolean,
    onTogglePrestamo: () -> Unit
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
                    "Nuevo" to ExpandableItem(Icons.Default.PersonAddAlt, 1),
                    /*
                    "Listado" to ExpandableItem(Icons.Default.PersonAdd, 2),
                    "Morosos" to ExpandableItem(Icons.Default.Warning, 3)*/
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
                    "Abonar" to ExpandableItem(Icons.Default.Payments, 4)
                ),
                color = if(isDarkTheme) Color.LightGray else Color.Black,
                parentColor = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            /*
            DrawerExpandableGroup(
                title = "Prestamos",
                icon = Icons.Default.Money,
                expanded = prestamoExpanded,
                onToggle = onTogglePrestamo,
                children = listOf(
                    "Nuevo" to ExpandableItem(Icons.Default.Payment, 5),
                    "Listado" to ExpandableItem(Icons.AutoMirrored.Filled.List, 6)
                ),
                color = if(isDarkTheme) Color.LightGray else Color.Black,
                parentColor = color
            ) */
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
    children: List<Pair<String, ExpandableItem>>,
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
                    leadingContent = { Icon(childIcon.icon, contentDescription = null, tint = color) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .clickable { when(childIcon.id) {
                            1 -> context.startActivity(Intent(context, MultiStepFormActivity::class.java))
                            2 -> context.startActivity(Intent(context, ClientListActivity::class.java))
                            4 -> context.startActivity(Intent(context, ClientFormActivity::class.java))
                            5 -> context.startActivity(Intent(context, LoanRequestActivity::class.java))
                            6 -> context.startActivity(Intent(context, LoanListActivity::class.java))
                            else -> Toast.makeText(context, "No implementado", Toast.LENGTH_SHORT).show()
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
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClientesSection(clientes: List<ClienteUI>) {
    var paginaActual by remember { mutableStateOf(0) }
    var textoBusqueda by remember { mutableStateOf("") }

    val clientesFiltrados = clientes.filter {
        it.abonado.contains(textoBusqueda, ignoreCase = true) ||
                it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                it.apellido.contains(textoBusqueda, ignoreCase = true)
    }

    val porPagina = 10
    val totalPaginas = (clientesFiltrados.size + porPagina - 1) / porPagina
    val clientesPagina = clientesFiltrados.drop(paginaActual * porPagina).take(porPagina)

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 32.dp)
    ) {
        Text(
            text = "Mis Clientes",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = {
                textoBusqueda = it
                paginaActual = 0 // Reinicia a la primera página al buscar
            },
            label = { Text("Buscar por abonado, nombre o apellido") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, false)
        ) {
            items(clientesPagina) { cliente ->
                val colorFondo = obtenerColorFila(cliente)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = colorFondo),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Abonado: ${cliente.abonado}", style = MaterialTheme.typography.bodyMedium)
                            Text("Código: ${cliente.codigoPrestamo}", style = MaterialTheme.typography.bodyMedium)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Nombre: ${cliente.nombre}", style = MaterialTheme.typography.bodyMedium)
                            Text("Apellido: ${cliente.apellido}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        if (clientesFiltrados.isEmpty()) {
            Text(
                "No se encontraron resultados.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Controles de paginación
        if (clientesFiltrados.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { paginaActual = 0 }, enabled = paginaActual > 0) {
                    Icon(Icons.Default.FirstPage, contentDescription = "Primera página")
                }

                IconButton(onClick = { if (paginaActual > 0) paginaActual-- }, enabled = paginaActual > 0) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior")
                }

                Text("Página ${paginaActual + 1} de $totalPaginas", style = MaterialTheme.typography.bodyMedium)

                IconButton(onClick = { if (paginaActual < totalPaginas - 1) paginaActual++ }, enabled = paginaActual < totalPaginas - 1) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente")
                }

                IconButton(onClick = { paginaActual = totalPaginas - 1 }, enabled = paginaActual < totalPaginas - 1) {
                    Icon(Icons.Default.LastPage, contentDescription = "Última página")
                }
            }
        }
    }
}


data class ClienteUI(
    val abonado: String,
    val codigoPrestamo: String,
    val nombre: String,
    val apellido: String,
    val fechaPrimeraCuota: LocalDate,
    val fechaVencimiento: LocalDate
)
@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun obtenerColorFila(cliente: ClienteUI): Color {
    val hoy = LocalDate.now()
    return when {
        hoy.isEqual(cliente.fechaPrimeraCuota) -> Color(0xFF4CAF50) // Verde
        //hoy.plusDays(1).isEqual(cliente.fechaPrimeraCuota) -> Color(0xFF1C937B) // Verde raro
        hoy.isAfter(cliente.fechaVencimiento) -> Color(0xFFFF0000) // Rojo
        else -> MaterialTheme.colorScheme.surfaceVariant // Normal
    }
}
