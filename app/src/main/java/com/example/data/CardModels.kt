package com.example.data

enum class CardType(val displayLabel: String) {
    CREDIT("Credit"),
    DEBIT("Debit")
}

enum class PaymentNetwork(
    val displayName: String,
    val shortBadge: String,
    val cardNumberLength: Int,
    val cvvLength: Int
) {
    VISA(
        displayName = "Visa",
        shortBadge = "VISA",
        cardNumberLength = 16,
        cvvLength = 3
    ),
    MASTERCARD(
        displayName = "Mastercard",
        shortBadge = "MC",
        cardNumberLength = 16,
        cvvLength = 3
    ),
    RUPAY(
        displayName = "RuPay",
        shortBadge = "RuPay",
        cardNumberLength = 16,
        cvvLength = 3
    ),
    AMEX(
        displayName = "American Express",
        shortBadge = "AMEX",
        cardNumberLength = 15,
        cvvLength = 4
    ),
    DISCOVER(
        displayName = "Discover",
        shortBadge = "DISCOVER",
        cardNumberLength = 16,
        cvvLength = 3
    )
}

object StarterVariants {
    val defaults: List<String> = listOf(
        "Signature",
        "Platinum",
        "Select",
        "Coral",
        "Regalia",
        "Millennia",
        "Sapphiro",
        "Infinite",
        "World",
        "Classic"
    )
}

object CardFormatting {
    fun normalizeVariantKey(raw: String): String =
        raw.trim().replace(Regex("\\s+"), " ").lowercase()

    fun cleanVariantDisplay(raw: String): String =
        raw.trim().replace(Regex("\\s+"), " ")

    fun formatCardNumber(rawDigits: String, network: PaymentNetwork): String {
        val digits = rawDigits.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return if (network == PaymentNetwork.AMEX) {
            buildString {
                digits.forEachIndexed { index, ch ->
                    append(ch)
                    if ((index == 3 || index == 9) && index != digits.lastIndex) {
                        append("  ")
                    }
                }
            }
        } else {
            digits.chunked(4).joinToString("  ")
        }
    }

    fun formatExpiry(month: Int, year: Int): String =
        "%02d/%04d".format(month.coerceIn(1, 12), year)
}
