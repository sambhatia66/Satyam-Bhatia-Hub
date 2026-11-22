package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CardEntity
import com.example.data.CardFormatting
import com.example.data.CardType
import com.example.data.CardVaultRepository
import com.example.data.PaymentNetwork
import com.example.data.VariantEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class VaultTab {
    HOME,
    SETTINGS
}

sealed interface ValidationError {
    data object CardNameRequired : ValidationError
    data class CardNumberInvalid(val expectedDigits: Int) : ValidationError
    data object ExpiryFormatInvalid : ValidationError
    data object ExpiryMonthInvalid : ValidationError
    data object ExpiryYearInvalid : ValidationError
    data class CvvInvalid(val expectedDigits: Int, val networkName: String) : ValidationError
    data object VariantRequired : ValidationError
}

data class AddCardFormState(
    val cardName: String = "",
    val cardNumberDigits: String = "",
    val expiryDigits: String = "", // Raw MMYYYY digits up to 6 chars
    val cvvDigits: String = "",
    val cardType: CardType = CardType.CREDIT,
    val network: PaymentNetwork = PaymentNetwork.VISA,
    val variantInput: String = "",
    val cardNameError: ValidationError? = null,
    val cardNumberError: ValidationError? = null,
    val expiryError: ValidationError? = null,
    val cvvError: ValidationError? = null,
    val variantError: ValidationError? = null,
    val isSubmitting: Boolean = false
)

data class CardVaultUiState(
    val activeTab: VaultTab = VaultTab.HOME,
    val allCards: List<CardEntity> = emptyList(),
    val filteredCards: List<CardEntity> = emptyList(),
    val availableVariants: List<VariantEntity> = emptyList(),
    val selectedNetworks: Set<PaymentNetwork> = emptySet(),
    val selectedVariantKeys: Set<String> = emptySet(),
    val expandedCardId: Long? = null,
    val networkCounts: Map<PaymentNetwork, Int> = emptyMap(),
    val variantCounts: Map<String, Int> = emptyMap(),
    val customVariantCount: Int = 0,
    val addCardForm: AddCardFormState = AddCardFormState()
) {
    val hasActiveFilters: Boolean
        get() = selectedNetworks.isNotEmpty() || selectedVariantKeys.isNotEmpty()
}

sealed interface CardVaultEvent {
    data class CardAdded(val cardName: String) : CardVaultEvent
}

private data class InteractionState(
    val activeTab: VaultTab = VaultTab.HOME,
    val selectedNetworks: Set<PaymentNetwork> = emptySet(),
    val selectedVariantKeys: Set<String> = emptySet(),
    val expandedCardId: Long? = null
)

class CardVaultViewModel(
    private val repository: CardVaultRepository
) : ViewModel() {

    private val interactionState = MutableStateFlow(InteractionState())
    private val formState = MutableStateFlow(AddCardFormState())

    private val _events = MutableSharedFlow<CardVaultEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<CardVaultEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.ensureStarterVariants()
        }
    }

    val uiState: StateFlow<CardVaultUiState> = combine(
        repository.cards,
        repository.variants,
        interactionState,
        formState
    ) { cards, variants, interaction, form ->
        val networkCounts = cards.groupingBy { it.network }.eachCount()
        val variantCounts = cards.groupingBy { it.normalizedVariantKey }.eachCount()

        // Surface custom variants & variants present on saved cards first so newly added
        // variants are immediately visible in the Home filter bar.
        val sortedVariants = variants.sortedWith(
            compareByDescending<VariantEntity> { it.isCustom }
                .thenByDescending { (variantCounts[it.normalizedKey] ?: 0) > 0 }
                .thenByDescending { it.createdAt }
                .thenBy { it.displayName.lowercase() }
        )

        val filtered = cards.filter { card ->
            val matchesNetwork = interaction.selectedNetworks.isEmpty() ||
                card.network in interaction.selectedNetworks
            val matchesVariant = interaction.selectedVariantKeys.isEmpty() ||
                card.normalizedVariantKey in interaction.selectedVariantKeys
            matchesNetwork && matchesVariant
        }

        val validExpandedId = interaction.expandedCardId?.takeIf { id ->
            filtered.any { it.id == id }
        }

        CardVaultUiState(
            activeTab = interaction.activeTab,
            allCards = cards,
            filteredCards = filtered,
            availableVariants = sortedVariants,
            selectedNetworks = interaction.selectedNetworks,
            selectedVariantKeys = interaction.selectedVariantKeys,
            expandedCardId = validExpandedId,
            networkCounts = networkCounts,
            variantCounts = variantCounts,
            customVariantCount = variants.count { it.isCustom },
            addCardForm = form
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CardVaultUiState()
    )

    fun selectTab(tab: VaultTab) {
        interactionState.update { it.copy(activeTab = tab) }
    }

    fun toggleNetworkFilter(network: PaymentNetwork) {
        interactionState.update { state ->
            val updated = if (network in state.selectedNetworks) {
                state.selectedNetworks - network
            } else {
                state.selectedNetworks + network
            }
            state.copy(selectedNetworks = updated)
        }
    }

    fun toggleVariantFilter(normalizedKey: String) {
        interactionState.update { state ->
            val updated = if (normalizedKey in state.selectedVariantKeys) {
                state.selectedVariantKeys - normalizedKey
            } else {
                state.selectedVariantKeys + normalizedKey
            }
            state.copy(selectedVariantKeys = updated)
        }
    }

    fun clearAllFilters() {
        interactionState.update {
            it.copy(
                selectedNetworks = emptySet(),
                selectedVariantKeys = emptySet()
            )
        }
    }

    fun toggleCardExpansion(cardId: Long) {
        interactionState.update { state ->
            val nextId = if (state.expandedCardId == cardId) null else cardId
            state.copy(expandedCardId = nextId)
        }
    }

    fun expandCard(cardId: Long) {
        interactionState.update { state ->
            state.copy(expandedCardId = cardId)
        }
    }

    fun collapseExpandedCard() {
        interactionState.update { state ->
            if (state.expandedCardId != null) state.copy(expandedCardId = null) else state
        }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            if (interactionState.value.expandedCardId == cardId) {
                interactionState.update { it.copy(expandedCardId = null) }
            }
            repository.deleteCard(cardId)
        }
    }

    // --- Add Card Form actions ---

    fun onCardNameChanged(value: String) {
        formState.update {
            it.copy(
                cardName = value.take(40),
                cardNameError = null
            )
        }
    }

    fun onCardNumberChanged(value: String) {
        val network = formState.value.network
        val digitsOnly = value.filter { it.isDigit() }.take(network.cardNumberLength)
        formState.update {
            it.copy(
                cardNumberDigits = digitsOnly,
                cardNumberError = null
            )
        }
    }

    fun onExpiryChanged(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(6)
        formState.update {
            it.copy(
                expiryDigits = digitsOnly,
                expiryError = null
            )
        }
    }

    fun onCvvChanged(value: String) {
        val maxLen = formState.value.network.cvvLength
        val digitsOnly = value.filter { it.isDigit() }.take(maxLen)
        formState.update {
            it.copy(
                cvvDigits = digitsOnly,
                cvvError = null
            )
        }
    }

    fun onCardTypeSelected(cardType: CardType) {
        formState.update { it.copy(cardType = cardType) }
    }

    fun onNetworkSelected(network: PaymentNetwork) {
        formState.update { current ->
            current.copy(
                network = network,
                cardNumberDigits = current.cardNumberDigits.take(network.cardNumberLength),
                cvvDigits = current.cvvDigits.take(network.cvvLength),
                cardNumberError = null,
                cvvError = null
            )
        }
    }

    fun onVariantInputChanged(value: String) {
        formState.update {
            it.copy(
                variantInput = value.take(32),
                variantError = null
            )
        }
    }

    fun onVariantSuggestionPicked(displayName: String) {
        formState.update {
            it.copy(
                variantInput = displayName,
                variantError = null
            )
        }
    }

    fun resetForm() {
        formState.value = AddCardFormState()
    }

    fun submitCard() {
        val current = formState.value
        if (current.isSubmitting) return

        val nameError = if (current.cardName.isBlank()) {
            ValidationError.CardNameRequired
        } else {
            null
        }

        val expectedCardLen = current.network.cardNumberLength
        val numberError = if (current.cardNumberDigits.length != expectedCardLen) {
            ValidationError.CardNumberInvalid(expectedCardLen)
        } else {
            null
        }

        var parsedMonth = 0
        var parsedYear = 0
        val expiryError = when {
            current.expiryDigits.length != 6 -> ValidationError.ExpiryFormatInvalid
            else -> {
                parsedMonth = current.expiryDigits.substring(0, 2).toIntOrNull() ?: 0
                parsedYear = current.expiryDigits.substring(2, 6).toIntOrNull() ?: 0
                when {
                    parsedMonth !in 1..12 -> ValidationError.ExpiryMonthInvalid
                    parsedYear !in 2024..2099 -> ValidationError.ExpiryYearInvalid
                    else -> null
                }
            }
        }

        val expectedCvvLen = current.network.cvvLength
        val cvvError = if (current.cvvDigits.length != expectedCvvLen) {
            ValidationError.CvvInvalid(expectedCvvLen, current.network.displayName)
        } else {
            null
        }

        val cleanVariant = CardFormatting.cleanVariantDisplay(current.variantInput)
        val variantError = if (cleanVariant.isBlank()) {
            ValidationError.VariantRequired
        } else {
            null
        }

        if (listOf(nameError, numberError, expiryError, cvvError, variantError).any { it != null }) {
            formState.update {
                it.copy(
                    cardNameError = nameError,
                    cardNumberError = numberError,
                    expiryError = expiryError,
                    cvvError = cvvError,
                    variantError = variantError
                )
            }
            return
        }

        formState.update { it.copy(isSubmitting = true) }
        val savedName = current.cardName.trim()

        viewModelScope.launch {
            try {
                repository.addCard(
                    cardName = savedName,
                    cardNumberDigits = current.cardNumberDigits,
                    expiryMonth = parsedMonth,
                    expiryYear = parsedYear,
                    cvv = current.cvvDigits,
                    cardType = current.cardType,
                    network = current.network,
                    rawVariant = cleanVariant
                )
                formState.value = AddCardFormState()
                _events.emit(CardVaultEvent.CardAdded(savedName))
            } finally {
                formState.update { it.copy(isSubmitting = false) }
            }
        }
    }
}

class CardVaultViewModelFactory(
    private val repository: CardVaultRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CardVaultViewModel::class.java)) {
            return CardVaultViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
