package com.sans.finance.domain.usecase

import com.sans.finance.domain.model.Installment
import com.sans.finance.domain.repository.InstallmentRepository


class CreateInstallmentPlanUseCase constructor(
    private val installmentRepository: InstallmentRepository
) {
    suspend operator fun invoke(
        expenseId: Long,
        totalAmount: Long,
        durationMonths: Int,
        startDate: Long
    ) {
        if (durationMonths <= 0) return

        val monthlyPayment = totalAmount / durationMonths

        val installment = Installment(
            expenseId = expenseId,
            totalAmount = totalAmount,
            monthlyPayment = monthlyPayment,
            durationMonths = durationMonths,
            remainingBalance = totalAmount,
            nextDueDate = startDate,
            status = "Active"
        )

        val installmentId = installmentRepository.createInstallment(installment)

        installmentRepository.createInstallmentItems(
            installmentId,
            durationMonths,
            totalAmount,
            startDate
        )
    }
}
