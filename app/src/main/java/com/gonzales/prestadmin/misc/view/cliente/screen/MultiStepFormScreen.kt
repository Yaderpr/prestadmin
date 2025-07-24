import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gonzales.prestadmin.viewmodel.Cliente
import com.gonzales.prestadmin.viewmodel.ClienteDatos
import com.gonzales.prestadmin.viewmodel.EvaluacionDatos
import com.gonzales.prestadmin.viewmodel.Garantia
import com.gonzales.prestadmin.viewmodel.PrestamoDatos
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@SuppressLint("ContextCastToActivity")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiStepFormScreen(
    onGuardarAll: (
        ClienteDatos,
        PrestamoDatos,
        EvaluacionDatos,
        List<Garantia>
    ) -> Unit
) {
    var activity = LocalContext.current as? ComponentActivity
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf("Datos Personales", "Solicitud", "Evaluación", "Garantías")

    // --- Estados compartidos ---
    // 1) Datos Personales
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }

    // 2) Solicitud Préstamo
    var clienteSearch by remember { mutableStateOf("") }
    var clienteExp by remember { mutableStateOf(false) }
    var selectedCliente by remember { mutableStateOf<Cliente?>(null) }
    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var tasaInteres by remember { mutableStateOf("") }
    var tasaMulta by remember { mutableStateOf("") }
    var plazo by remember { mutableStateOf("Mensual") }
    var duracionDias by remember { mutableStateOf("") }
    val plazos = listOf("Mensual", "Semanal", "Diario")

    // 3) Evaluación
    var tipoNegocio by remember { mutableStateOf("") }
    var direccionNegocio by remember { mutableStateOf("") }

    // 4) Garantías
    var garantias by remember {
        mutableStateOf(listOf(Garantia("", "")))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alta Cliente y Préstamo") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    onGuardarAll(
                        ClienteDatos(nombres, apellidos, telefono, direccion, cedula),
                        PrestamoDatos(
                            clienteName = selectedCliente?.run { "$nombres $apellidos" } ?: "",
                            descripcion = descripcion,
                            monto = monto.toDoubleOrNull() ?: 0.0,
                            plazo = plazo,
                            tasaInteresPct = tasaInteres.toDoubleOrNull() ?: 0.0,
                            tasaMultaPct = tasaMulta.toDoubleOrNull() ?: 0.0,
                            duracionDias = duracionDias.toIntOrNull() ?: 0
                        ),
                        EvaluacionDatos(tipoNegocio, direccionNegocio),
                        garantias
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Guardar todo")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        label = { Text(title) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            when (selectedSection) {
                0 -> ClienteFormSection(
                    nombres, { nombres = it },
                    apellidos, { apellidos = it },
                    telefono, { telefono = it },
                    direccion, { direccion = it },
                    cedula, { cedula = it },
                    fotoFrontal = null,
                    onFotoFrontalChange = { bitmap -> },
                    fotoReversa = null,
                    onFotoReversaChange = { it?.height }
                )

                1 -> LoanRequestSection(
                    onGuardar = { a, b, c, d, e, f -> Unit }
                )

                2 -> EvaluacionSection(
                    tipoNegocio = tipoNegocio,
                    onTipoChange = { tipoNegocio = it },
                    direccionNegocio = direccionNegocio,
                    onDireccionNegocioChange = { direccionNegocio = it }
                )

                3 -> GarantiasSection(
                    garantias = garantias,
                    onAdd = { garantias = garantias + Garantia("", "") },
                    onRemove = { idx -> garantias = garantias.filterIndexed { i, _ -> i != idx } },
                    onItemChange = { idx, g ->
                        garantias = garantias.mapIndexed { i, old ->
                            if (i == idx) g else old
                        }
                    }
                )
            }
        }
    }
}

// Resto de los composables (ClienteFormSection, EvaluacionSection, GarantiasSection) permanecen sin cambios
@Composable
fun EvaluacionSection(
    tipoNegocio: String,
    onTipoChange: (String) -> Unit,
    direccionNegocio: String,
    onDireccionNegocioChange: (String) -> Unit
) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = tipoNegocio,
            onValueChange = onTipoChange,
            label = { Text("Tipo de negocio") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = direccionNegocio,
            onValueChange = onDireccionNegocioChange,
            label = { Text("Dirección del negocio") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun GarantiasSection(
    garantias: List<Garantia>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onItemChange: (Int, Garantia) -> Unit
) {
    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        garantias.forEachIndexed { idx, g ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = g.descripcion,
                    onValueChange = { onItemChange(idx, g.copy(descripcion = it)) },
                    label = { Text("Descripción") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = g.precioEstimado,
                    onValueChange = { onItemChange(idx, g.copy(precioEstimado = it)) },
                    label = { Text("Precio estimado") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(120.dp)
                )
                IconButton(onClick = { onRemove(idx) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = onAdd, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Default.Add, contentDescription = "Agregar")
            Spacer(Modifier.width(4.dp))
            Text("Agregar garantía")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteFormSection(
    nombres: String, onNombresChange: (String) -> Unit,
    apellidos: String, onApellidosChange: (String) -> Unit,
    telefono: String, onTelefonoChange: (String) -> Unit,
    direccion: String, onDireccionChange: (String) -> Unit,
    cedula: String, onCedulaChange: (String) -> Unit,
    fotoFrontal: Bitmap?, onFotoFrontalChange: (Bitmap?) -> Unit,
    fotoReversa: Bitmap?, onFotoReversaChange: (Bitmap?) -> Unit
) {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    // Errores de validación
    var errorNombres by remember { mutableStateOf<String?>(null) }
    var errorApellidos by remember { mutableStateOf<String?>(null) }
    var errorTelefono by remember { mutableStateOf<String?>(null) }
    var errorDireccion by remember { mutableStateOf<String?>(null) }
    var errorCedula by remember { mutableStateOf<String?>(null) }

    // Nombres de archivo
    var nameFrontal by remember { mutableStateOf<String?>(null) }
    var nameReversa by remember { mutableStateOf<String?>(null) }

    // Diálogo de fuente
    var showDialog by remember { mutableStateOf(false) }
    var isFrontalDialog by remember { mutableStateOf(true) }

    // Validaciones
    fun validateNombres(v: String) = when {
        v.length < 3 -> "Mínimo 3 caracteres"
        !v.all { it.isLetter() || it.isWhitespace() } -> "Sólo letras y espacios"
        else -> null
    }

    fun validateApellidos(v: String) = validateNombres(v)
    fun validateTelefono(v: String) = when {
        v.length < 8 -> "Mínimo 8 dígitos"
        !v.all { it.isDigit() } -> "Sólo números"
        else -> null
    }

    fun validateDireccion(v: String) = if (v.length < 3) "Mínimo 3 caracteres" else null
    fun validateCedula(v: String): String? {
        val regex = Regex("^\\d{3}-\\d{6}-\\d{4}[a-zA-Z]$|^\\w{13,}$")
        return if (!regex.matches(v)) "Formato inválido" else null
    }

    fun generateImageName(): String {
        val fmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return "IMG_${fmt.format(Calendar.getInstance().time)}.jpg"
    }

    // Launchers
    val cameraFrontal = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, tempUri)
            val name = generateImageName()
            nameFrontal = name
            onFotoFrontalChange(bmp)
        }
    }
    val cameraReversa = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, tempUri)
            val name = generateImageName()
            nameReversa = name
            onFotoReversaChange(bmp)
        }
    }
    val gallery = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bmp = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            val name = generateImageName()
            nameReversa = name
            onFotoReversaChange(bmp)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Seleccionar fuente") },
            text = { Text("¿Cámara o Galería?") },
            confirmButton = {
                Button(onClick = {
                    tempUri = createImageUri(context)
                    tempUri?.let {
                        if (isFrontalDialog) cameraReversa.launch(it)
                        else cameraFrontal.launch(it)
                    }
                    showDialog = false
                }) { Text("Cámara") }
            },

            dismissButton = {
                Button(onClick = {
                    if (isFrontalDialog) gallery.launch("image/*")
                    else gallery.launch("image/*")
                    showDialog = false
                }) { Text("Galería") }
            }
        )
    }

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nombres
        OutlinedTextField(
            value = nombres,
            onValueChange = {
                onNombresChange(it)
                errorNombres = validateNombres(it)
            },
            label = { Text("Nombres") },
            isError = errorNombres != null,
            supportingText = {
                errorNombres?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        // Apellidos
        OutlinedTextField(
            value = apellidos,
            onValueChange = {
                onApellidosChange(it)
                errorApellidos = validateApellidos(it)
            },
            label = { Text("Apellidos") },
            isError = errorApellidos != null,
            supportingText = {
                errorApellidos?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        // Teléfono
        OutlinedTextField(
            value = telefono,
            onValueChange = {
                onTelefonoChange(it)
                errorTelefono = validateTelefono(it)
            },
            label = { Text("Teléfono") },
            isError = errorTelefono != null,
            supportingText = {
                errorTelefono?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        // Dirección
        OutlinedTextField(
            value = direccion,
            onValueChange = {
                onDireccionChange(it)
                errorDireccion = validateDireccion(it)
            },
            label = { Text("Dirección") },
            isError = errorDireccion != null,
            supportingText = {
                errorDireccion?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
        // Cédula
        OutlinedTextField(
            value = cedula,
            onValueChange = {
                onCedulaChange(it)
                errorCedula = validateCedula(it)
            },
            label = { Text("Cédula") },
            isError = errorCedula != null,
            supportingText = {
                errorCedula?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Foto frontal
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Foto de cedula frontal:")
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                isFrontalDialog = true
                showDialog = true
            }) { Text("Seleccionar") }
        }
        nameFrontal?.let { Text("Archivo: $it", Modifier.padding(start = 16.dp)) }
        fotoFrontal?.let {
            Image(it.asImageBitmap(), contentDescription = null, Modifier.size(100.dp))
        }

        // Foto reversa
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Foto de cedula reversa:")
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                isFrontalDialog = false
                showDialog = true
            }) { Text("Seleccionar") }
        }
        nameReversa?.let { Text("Archivo: $it", Modifier.padding(start = 16.dp)) }
        fotoReversa?.let {
            Image(it.asImageBitmap(), contentDescription = null, Modifier.size(100.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanRequestSection(
    onGuardar: (
        capital: Double, interesPct: Int, modalidad: String, plazo: String,
        desembolso: LocalDate, observacion: String
    ) -> Unit
) {
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()

    val diasDeLaSemana = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    var diaSemanaEditable by remember { mutableStateOf("") }
    var diaSemanaExpanded by remember { mutableStateOf(false) }


    var capitalText by remember { mutableStateOf("") }
    val capital = capitalText.toDoubleOrNull() ?: 0.0

    var interesPct by remember { mutableStateOf(0) }

    val modalidades = listOf("Diario", "Semanal", "Mensual")
    var modalidadExpanded by remember { mutableStateOf(false) }
    var modalidad by remember { mutableStateOf(modalidades.first()) }

    var plazoExpanded by remember { mutableStateOf(false) }
    var plazo by remember { mutableStateOf("") }

    // Plazos dinámicos
    val opcionesPlazo = when (modalidad) {
        "Diario" -> buildList {
            add("20 cuotas a 1 mes")
            add("30 cuotas a 1.5 meses")
            add("40 cuotas a 2 meses")
            add("50 cuotas a 2.5 meses")
            add("60 cuotas a 3 meses")
            add("70 cuotas a 3.5 meses")
            add("80 cuotas a 4 meses")
            add("90 cuotas a 4.5 meses")
            add("100 cuotas a 5 meses")
            add("110 cuotas a 5.5 meses")
            add("120 cuotas a 6 meses")
            add("365 cuotas a 12 meses")
        }

        "Semanal" -> buildList {
            add("4 semanas a 1 mes")
            add("6 semanas a 1.5 meses")
            add("8 semanas a 2 meses")
            add("10 semanas a 2.5 meses")
            add("12 semanas a 3 meses")
            add("14 semanas a 3.5 meses")
            add("16 semanas a 4 meses")
            add("18 semanas a 4.5 meses")
            add("20 semanas a 5 meses")
            add("22 semanas a 5.5 meses")
            add("24 semanas a 6 meses")
            add("52 semanas a 12 meses")
        }

        "Mensual" -> (1..12).map { "$it cuota${if (it > 1) "s" else ""} a $it mes${if (it > 1) "es" else ""}" }
        else -> emptyList()
    }

    val cuotasCount = plazo.substringBefore(" ").toIntOrNull() ?: 0
    val totalDeuda = capital * (1 + interesPct / 100.0)
    val cuota = if (cuotasCount > 0) totalDeuda / cuotasCount else 0.0

    var desembolso by remember { mutableStateOf(today) }

    // Estado para el DatePicker
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = desembolso.toEpochDay() * 24 * 60 * 60 * 1000
        )
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        desembolso = selectedDate
                    }

                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .heightIn(max = 580.dp) // Ajusta a lo que necesites
                        .widthIn(max = 290.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }


        )
    }

    val primeraCuota = when (modalidad) {
        "Diario" -> desembolso.plusDays(2)
        "Semanal" -> desembolso.plusWeeks(1)
        "Mensual" -> desembolso.plusMonths(1)
        else -> desembolso
    }

    val diaSemana = when (modalidad) {
        "Diario" -> "Diario"
        "Semanal", "Mensual" -> diaSemanaEditable.ifEmpty {
            primeraCuota.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        }
        else -> ""
    }


    // Cálculo de la fecha de vencimiento ajustada para excluir sábados y domingos en "Diario"
    val fechaVencimiento = when (modalidad) {
        "Diario" -> calculateBusinessDays(primeraCuota, cuotasCount)
        "Semanal" -> primeraCuota.plusWeeks((cuotasCount - 1).toLong())
        "Mensual" -> primeraCuota.plusMonths((cuotasCount - 1).toLong())
        else -> primeraCuota
    }

    var observacion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Capital
        OutlinedTextField(
            value = capitalText,
            onValueChange = { capitalText = it },
            label = { Text("Capital") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Interés
        Text("Interés (%)")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = interesPct.toFloat(),
                onValueChange = { interesPct = it.toInt() },
                valueRange = 0f..50f,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = interesPct.toString(),
                onValueChange = {
                    interesPct = it.toIntOrNull()?.coerceIn(0, 50) ?: 0
                },
                label = { Text("Interés") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(80.dp)
            )
        }

        // Modalidad
        ExposedDropdownMenuBox(
            expanded = modalidadExpanded,
            onExpandedChange = { modalidadExpanded = !modalidadExpanded }
        ) {
            OutlinedTextField(
                value = modalidad,
                onValueChange = {},
                readOnly = true,
                label = { Text("Modalidad") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = modalidadExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = modalidadExpanded,
                onDismissRequest = { modalidadExpanded = false }
            ) {
                modalidades.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            modalidad = it
                            modalidadExpanded = false
                            plazo = ""
                        }
                    )
                }
            }
        }

        // Plazo
        ExposedDropdownMenuBox(
            expanded = plazoExpanded,
            onExpandedChange = { plazoExpanded = !plazoExpanded }
        ) {
            OutlinedTextField(
                value = plazo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Plazo") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = plazoExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = plazoExpanded,
                onDismissRequest = { plazoExpanded = false }
            ) {
                opcionesPlazo.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            plazo = it
                            plazoExpanded = false
                        }
                    )
                }
            }
        }

        // Cuota
        OutlinedTextField(
            value = if (cuota > 0) "C$ %.2f".format(cuota) else "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Cuota") },
            modifier = Modifier.fillMaxWidth()
        )

        // Fecha de desembolso (seleccionable con DatePicker de Compose)
        OutlinedTextField(
            value = desembolso.format(fmt),
            onValueChange = {},
            readOnly = true,
            label = {
                Text(text = "Fecha de desembolso", modifier = Modifier.clickable {
                    showDatePicker = true
                })
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDatePicker = true
                },
            trailingIcon = {
                IconButton(onClick = {
                    showDatePicker = true
                }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
                }
            }
        )

        // Primera cuota
        OutlinedTextField(
            value = primeraCuota.format(fmt),
            onValueChange = {},
            readOnly = true,
            label = { Text("Primera cuota") },
            modifier = Modifier.fillMaxWidth()
        )

        // Día de la semana (editable solo en modalidad Semanal o Mensual)
        if (modalidad == "Semanal" || modalidad == "Mensual") {
            ExposedDropdownMenuBox(
                expanded = diaSemanaExpanded,
                onExpandedChange = { diaSemanaExpanded = !diaSemanaExpanded }
            ) {
                OutlinedTextField(
                    value = diaSemana,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Día de la semana") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = diaSemanaExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = diaSemanaExpanded,
                    onDismissRequest = { diaSemanaExpanded = false }
                ) {
                    diasDeLaSemana.forEach { dia ->
                        DropdownMenuItem(
                            text = { Text(dia) },
                            onClick = {
                                diaSemanaEditable = dia
                                diaSemanaExpanded = false
                            }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = diaSemana,
                onValueChange = {},
                readOnly = true,
                label = { Text("Día de la semana") },
                modifier = Modifier.fillMaxWidth()
            )
        }


        // Fecha de vencimiento
        OutlinedTextField(
            value = fechaVencimiento.format(fmt),
            onValueChange = {},
            readOnly = true,
            label = { Text("Fecha de vencimiento") },
            modifier = Modifier.fillMaxWidth()
        )

        // Deuda Total
        OutlinedTextField(
            value = if (totalDeuda > 0) "C$ %.2f".format(totalDeuda) else "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Deuda Total") },
            modifier = Modifier.fillMaxWidth()
        )

        // Observación
        OutlinedTextField(
            value = observacion,
            onValueChange = { observacion = it },
            label = { Text("Observación") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )
    }
}

// Función para calcular la fecha de vencimiento sumando solo días laborables
@RequiresApi(Build.VERSION_CODES.O)
fun calculateBusinessDays(startDate: LocalDate, cuotas: Int): LocalDate {
    var currentDate = startDate
    var remainingCuotas = cuotas - 1 // La primera cuota ya es un día laborable

    while (remainingCuotas > 0) {
        currentDate = currentDate.plusDays(1)
        if (currentDate.dayOfWeek != DayOfWeek.SATURDAY && currentDate.dayOfWeek != DayOfWeek.SUNDAY) {
            remainingCuotas--
        }
    }
    return currentDate
}

fun createImageUri(context: Context): Uri? {
    val contentResolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Cedulas")
    }
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
}
