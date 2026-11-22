package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val cardName: String,
    val cardNumber: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cvv: String,
    val cardType: CardType,
    val network: PaymentNetwork,
    val variant: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedExpiry: String
        get() = CardFormatting.formatExpiry(expiryMonth, expiryYear)

    val formattedNumber: String
        get() = CardFormatting.formatCardNumber(cardNumber, network)

    val normalizedVariantKey: String
        get() = CardFormatting.normalizeVariantKey(variant)
}

@Entity(tableName = "variants")
data class VariantEntity(
    @PrimaryKey val normalizedKey: String,
    val displayName: String,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class CardVaultConverters {
    @TypeConverter
    fun fromCardType(value: CardType): String = value.name

    @TypeConverter
    fun toCardType(value: String): CardType =
        runCatching { CardType.valueOf(value) }.getOrDefault(CardType.CREDIT)

    @TypeConverter
    fun fromPaymentNetwork(value: PaymentNetwork): String = value.name

    @TypeConverter
    fun toPaymentNetwork(value: String): PaymentNetwork =
        runCatching { PaymentNetwork.valueOf(value) }.getOrDefault(PaymentNetwork.VISA)
}
