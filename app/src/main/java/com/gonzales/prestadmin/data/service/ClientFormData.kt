// File: com/gonzales/prestadmin/data/service/ClientFormData.kt
package com.gonzales.prestadmin.data.service

import android.graphics.Bitmap
import com.gonzales.prestadmin.domain.model.client.Client
import com.gonzales.prestadmin.domain.model.guarantee.Guarantee
import com.gonzales.prestadmin.domain.model.evaluation.Evaluation
import com.gonzales.prestadmin.domain.model.loan.Loan

data class ClientFormData(
    val client: Client,
    val loan: Loan,
    val guarantees: List<Guarantee>? = null,
    val evaluation: Evaluation? = null,
    val dniFrontImage: Bitmap?,
    val dniBackImage: Bitmap?,
)