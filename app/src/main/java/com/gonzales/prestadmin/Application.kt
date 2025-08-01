package com.gonzales.prestadmin

import android.annotation.SuppressLint
import android.app.Application
import android.content.res.Configuration
import com.gonzales.prestadmin.data.local.datastore.SessionManager
import com.gonzales.prestadmin.data.local.datastore.ThemeManager
import com.gonzales.prestadmin.data.repository.client.ClientRepository
import com.gonzales.prestadmin.data.repository.document.DocumentRepository
import com.gonzales.prestadmin.data.repository.evaluation.EvaluationRepository
import com.gonzales.prestadmin.data.repository.guarantee.GuaranteeRepository
import com.gonzales.prestadmin.data.repository.loan.LoanRepository
import com.gonzales.prestadmin.data.repository.payment.PaymentRepository
import com.gonzales.prestadmin.data.repository.user.UserRepository
import com.gonzales.prestadmin.data.service.ClientFormService
import com.gonzales.prestadmin.data.service.PaymentService

// Dentro de tu archivo de Application.kt (si no tienes uno, créalo)
class App : Application() {
    companion object {
        var systemDarkMode: Boolean = false
        lateinit var documentRepository: DocumentRepository
        @SuppressLint("StaticFieldLeak")
        lateinit var themeManager: ThemeManager
        @SuppressLint("StaticFieldLeak")
        lateinit var sessionManager: SessionManager
        @SuppressLint("StaticFieldLeak")
        lateinit var userRepository: UserRepository
        //Formulario cliente
        lateinit var clientRepository: ClientRepository
        lateinit var loanRepository: LoanRepository
        lateinit var guaranteeRepository: GuaranteeRepository
        lateinit var evaluationRepository: EvaluationRepository
        lateinit var  clientFormService: ClientFormService
        lateinit var paymentService: PaymentService
        lateinit var paymentRepository: PaymentRepository

    }

    override fun onCreate() {
        super.onCreate()
        // Aquí creamos una única instancia del SessionManager al inicio de la app.
        sessionManager = SessionManager(this)
        documentRepository = DocumentRepository()
        userRepository = UserRepository(this)
        themeManager = ThemeManager(this)
        //Instancias del formulario cliente
        clientRepository = ClientRepository()
        loanRepository = LoanRepository()
        guaranteeRepository = GuaranteeRepository()
        evaluationRepository = EvaluationRepository()
        clientFormService = ClientFormService()
        //Instancias de pago
        paymentRepository = PaymentRepository()
        paymentService = PaymentService()

        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        systemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}