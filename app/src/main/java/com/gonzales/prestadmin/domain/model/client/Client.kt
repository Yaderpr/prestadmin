package com.gonzales.prestadmin.domain.model.client
import com.gonzales.prestadmin.domain.model.loan.Loan
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Client(

    @SerialName("client_id") val id: Int? = null, // client_id es autoincremental en la DB

    @SerialName("user_id") val userId: Int, // Foreign key a users.user_id

    @SerialName("first_name") val firstName: String,

    @SerialName("last_name") val lastName: String,

    @SerialName("phone_number") val phoneNumber: String,

    @SerialName("address") val address: String,

    @SerialName("identification_number") val identificationNumber: String, // Para la cédula

    @SerialName("registration_timestamp") val registrationTimestamp: String // TIMESTAMP de creación del cliente

)
data class ClienteUI(
    val id: Int, // ID del cliente
    val codigoPrestamo: Int, // ID del préstamo
    val nombre: String,
    val apellido: String,
    val abonado: String, // Número de teléfono del cliente
    val fechaPrimeraCuota: LocalDate,
    val fechaVencimiento: LocalDate,
    val modality: String,
    val dayOfWeek: String? = null, // El día de la semana para pagos semanales, o "Diario"
    val monthlyPaymentDay: Int? = null // El día del mes para pagos mensuales, o null si no hay
)

fun mapClientAndLoanToClienteUI(client: Client, loan: Loan): ClienteUI {
    return ClienteUI(
        id = client.id ?: -1,
        codigoPrestamo = loan.id as Int,
        nombre = client.firstName,
        apellido = client.lastName,
        abonado = client.phoneNumber,
        fechaPrimeraCuota = loan.firstInstallmentDate,
        fechaVencimiento = loan.dueDate,
        modality = loan.modality,
        dayOfWeek = loan.dayOfWeek,
        monthlyPaymentDay = loan.disbursementDate.day
    )
}