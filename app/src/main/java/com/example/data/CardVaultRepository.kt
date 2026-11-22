package com.example.data

import kotlinx.coroutines.flow.Flow

class CardVaultRepository(
    private val cardDao: CardDao,
    private val variantDao: VariantDao
) {
    val cards: Flow<List<CardEntity>> = cardDao.observeAllCards()
    val variants: Flow<List<VariantEntity>> = variantDao.observeAllVariants()

    suspend fun ensureStarterVariants() {
        val baseTimestamp = 1_000L
        val starterEntities = StarterVariants.defaults.mapIndexed { index, name ->
            VariantEntity(
                normalizedKey = CardFormatting.normalizeVariantKey(name),
                displayName = name,
                isCustom = false,
                createdAt = baseTimestamp + (StarterVariants.defaults.size - index)
            )
        }
        variantDao.insertVariantsIfAbsent(starterEntities)
    }

    suspend fun addCard(
        cardName: String,
        cardNumberDigits: String,
        expiryMonth: Int,
        expiryYear: Int,
        cvv: String,
        cardType: CardType,
        network: PaymentNetwork,
        rawVariant: String
    ): CardEntity {
        val cleanVariant = CardFormatting.cleanVariantDisplay(rawVariant)
        val normalizedKey = CardFormatting.normalizeVariantKey(cleanVariant)
        val existingVariant = variantDao.findByKey(normalizedKey)
        val canonicalVariantDisplay = existingVariant?.displayName ?: cleanVariant

        if (existingVariant == null) {
            variantDao.insertVariantIfAbsent(
                VariantEntity(
                    normalizedKey = normalizedKey,
                    displayName = canonicalVariantDisplay,
                    isCustom = true,
                    createdAt = System.currentTimeMillis()
                )
            )
        }

        val entity = CardEntity(
            cardName = cardName.trim(),
            cardNumber = cardNumberDigits.filter { it.isDigit() },
            expiryMonth = expiryMonth,
            expiryYear = expiryYear,
            cvv = cvv.filter { it.isDigit() },
            cardType = cardType,
            network = network,
            variant = canonicalVariantDisplay,
            createdAt = System.currentTimeMillis()
        )
        val generatedId = cardDao.insertCard(entity)
        return entity.copy(id = generatedId)
    }

    suspend fun deleteCard(cardId: Long) {
        cardDao.deleteCardById(cardId)
    }
}
