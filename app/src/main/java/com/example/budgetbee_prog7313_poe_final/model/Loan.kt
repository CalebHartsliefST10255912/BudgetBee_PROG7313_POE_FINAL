package com.example.budgetbee_prog7313_poe_final.model

data class Loan(
    val id: String = "",
    val amount: Double = 0.0,                  // Total loan amount
    val interestRate: Double = 0.0,            // Annual interest rate (%)
    val minRepayment: Double = 0.0,            // Minimum monthly repayment
    val amountPaid: Double = 0.0,              // Total already paid
    val targetRepaymentMonths: Int = 36        // How many months user wants to repay in
) {
    val remainingAmount: Double
        get() = amount - amountPaid

    val monthlyPayment: Double
        get() {
            val monthlyInterest = interestRate / 12 / 100
            return if (monthlyInterest == 0.0) {
                (remainingAmount / targetRepaymentMonths).coerceAtLeast(minRepayment)
            } else {
                val numerator = monthlyInterest * remainingAmount
                val denominator = 1 - Math.pow(1 + monthlyInterest, -targetRepaymentMonths.toDouble())
                (numerator / denominator).coerceAtLeast(minRepayment)
            }
        }
}
