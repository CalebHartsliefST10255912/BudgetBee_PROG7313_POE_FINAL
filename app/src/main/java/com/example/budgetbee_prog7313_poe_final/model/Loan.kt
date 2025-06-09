package com.example.budgetbee_prog7313_poe_final.model

data class Loan(
    val id: String = "",
    val amount: Double = 0.0,                  // Total loan amount
    val interestRate: Double = 0.0,            // Annual interest rate (%)
    val minRepayment: Double = 0.0,            // Minimum monthly repayment
    val amountPaid: Double = 0.0,              // Total already paid
    val targetRepaymentMonths: Int = 1        // How many months user wants to repay in
) {
    /**
     * Now calculates:
     *   Principal + (Principal * rate * months/12)
     * minus whatever’s already paid.
     */
    val remainingAmount: Double
        get() {
            // Convert annual % to a fraction:
            val rateFraction = interestRate / 100.0
            // Total interest over the full period:
            val totalInterest = amount * rateFraction * (targetRepaymentMonths / 12.0)
            // Gross amount owed:
            val gross = amount + totalInterest
            // Subtract what the user’s already paid:
            return (gross - amountPaid).coerceAtLeast(0.0)
        }

    val monthlyPayment: Double
        get() {
            val gross = amount + (amount * (interestRate / 100.0) * (targetRepaymentMonths / 12.0))
            // Divide by months to get level payments, then ensure you meet the floor:
            return (gross / targetRepaymentMonths).coerceAtLeast(minRepayment)
        }
}
