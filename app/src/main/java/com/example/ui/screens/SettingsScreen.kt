package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CardFormatting
import com.example.data.CardType
import com.example.data.PaymentNetwork
import com.example.data.VariantEntity
import com.example.ui.AddCardFormState
import com.example.ui.CardVaultUiState
import com.example.ui.ValidationError
import com.example.ui.components.MetallicEmvChip
import com.example.ui.components.NetworkBadge
import com.example.ui.components.cardMetallicSurface
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.cardPaletteFor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: CardVaultUiState,
    scrollState: ScrollState,
    onCardNameChanged: (String) -> Unit,
    onCardNumberChanged: (String) -> Unit,
    onExpiryChanged: (String) -> Unit,
    onCvvChanged: (String) -> Unit,
    onCardTypeSelected: (CardType) -> Unit,
    onNetworkSelected: (PaymentNetwork) -> Unit,
    onVariantInputChanged: (String) -> Unit,
    onVariantSuggestionPicked: (String) -> Unit,
    onSubmitCard: () -> Unit,
    onResetForm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val form = uiState.addCardForm
    val focusManager = LocalFocusManager.current
    val suggestionScroll = rememberScrollState()

    val normalizedTypedVariant = remember(form.variantInput) {
        CardFormatting.normalizeVariantKey(form.variantInput)
    }
    val existingVariantKeys = remember(uiState.availableVariants) {
        uiState.availableVariants.map { it.normalizedKey }.toSet()
    }
    val isNewCustomVariant = normalizedTypedVariant.isNotEmpty() &&
        normalizedTypedVariant !in existingVariantKeys

    val filteredSuggestions = remember(form.variantInput, uiState.availableVariants) {
        val query = normalizedTypedVariant
        if (query.isEmpty()) {
            uiState.availableVariants
        } else {
            uiState.availableVariants.filter {
                it.normalizedKey.contains(query)
            }
        }
    }

    val cardNumberVisualTransformation = remember(form.network) {
        CardNumberVisualTransformation(form.network)
    }
    val expiryVisualTransformation = remember {
        ExpiryVisualTransformation()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // --- Header ---
            Column {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // --- Live Tactile Card Preview ---
            LiveCardPreview(form = form)

            // --- Form Container ---
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_section_card_details),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // 1. Card Name
                    OutlinedTextField(
                        value = form.cardName,
                        onValueChange = onCardNameChanged,
                        label = { Text(stringResource(R.string.settings_field_card_name)) },
                        placeholder = {
                            Text(stringResource(R.string.settings_field_card_name_placeholder))
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Badge,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        isError = form.cardNameError != null,
                        supportingText = form.cardNameError?.let { error ->
                            { Text(validationErrorMessage(error)) }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_name_input")
                    )

                    // 2. Card Type (Credit / Debit)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.settings_field_card_type),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CardType.entries.forEachIndexed { index, cardType ->
                                val selected = form.cardType == cardType
                                SegmentedButton(
                                    selected = selected,
                                    onClick = { onCardTypeSelected(cardType) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = CardType.entries.size
                                    ),
                                    modifier = Modifier.testTag(
                                        "card_type_${cardType.name.lowercase()}"
                                    )
                                ) {
                                    Text(
                                        text = cardType.displayLabel,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }

                    // 3. Payment Network Selector
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.settings_field_network),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PaymentNetwork.entries.forEach { network ->
                                val isSelected = form.network == network
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onNetworkSelected(network) },
                                    label = {
                                        Text(
                                            text = network.displayName,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier.testTag(
                                        "network_selector_${network.name.lowercase()}"
                                    )
                                )
                            }
                        }
                    }

                    // 4. Card Number
                    OutlinedTextField(
                        value = form.cardNumberDigits,
                        onValueChange = onCardNumberChanged,
                        label = { Text(stringResource(R.string.settings_field_card_number)) },
                        placeholder = {
                            Text(
                                text = if (form.network == PaymentNetwork.AMEX) {
                                    "3782 822463 10005"
                                } else {
                                    stringResource(R.string.settings_field_card_number_placeholder)
                                },
                                fontFamily = MonoFontFamily
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CreditCard,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = "${form.cardNumberDigits.length}/${form.network.cardNumberLength}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = MonoFontFamily,
                            letterSpacing = 0.8.sp
                        ),
                        visualTransformation = cardNumberVisualTransformation,
                        singleLine = true,
                        isError = form.cardNumberError != null,
                        supportingText = form.cardNumberError?.let { error ->
                            { Text(validationErrorMessage(error)) }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("card_number_input")
                    )

                    // 5. Expiry (MM/YYYY) & CVV Side-by-Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        OutlinedTextField(
                            value = form.expiryDigits,
                            onValueChange = onExpiryChanged,
                            label = { Text(stringResource(R.string.settings_field_expiry)) },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.settings_field_expiry_placeholder),
                                    fontFamily = MonoFontFamily
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = MonoFontFamily
                            ),
                            visualTransformation = expiryVisualTransformation,
                            singleLine = true,
                            isError = form.expiryError != null,
                            supportingText = form.expiryError?.let { error ->
                                { Text(validationErrorMessage(error)) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("expiry_input")
                        )

                        OutlinedTextField(
                            value = form.cvvDigits,
                            onValueChange = onCvvChanged,
                            label = { Text(stringResource(R.string.settings_field_cvv)) },
                            placeholder = {
                                Text(
                                    text = stringResource(
                                        if (form.network.cvvLength == 4) {
                                            R.string.settings_field_cvv_placeholder_4
                                        } else {
                                            R.string.settings_field_cvv_placeholder_3
                                        }
                                    ),
                                    fontFamily = MonoFontFamily
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = MonoFontFamily
                            ),
                            singleLine = true,
                            isError = form.cvvError != null,
                            supportingText = form.cvvError?.let { error ->
                                { Text(validationErrorMessage(error)) }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cvv_input")
                        )
                    }

                    // 6. Variant Field + Dynamic Suggestions & New Variant Badge
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(tween(200))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.settings_field_variant),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            AnimatedVisibility(
                                visible = isNewCustomVariant,
                                enter = fadeIn(tween(180)),
                                exit = fadeOut(tween(150))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .padding(horizontal = 10.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.settings_variant_new_badge),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = form.variantInput,
                            onValueChange = onVariantInputChanged,
                            placeholder = {
                                Text(stringResource(R.string.settings_field_variant_placeholder))
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Style,
                                    contentDescription = null
                                )
                            },
                            singleLine = true,
                            isError = form.variantError != null,
                            supportingText = form.variantError?.let { error ->
                                { Text(validationErrorMessage(error)) }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    onSubmitCard()
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("variant_input")
                        )

                        if (filteredSuggestions.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.settings_variant_suggestions_label),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(suggestionScroll),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filteredSuggestions.forEach { variant: VariantEntity ->
                                    val isCurrent = normalizedTypedVariant == variant.normalizedKey
                                    SuggestionChip(
                                        onClick = { onVariantSuggestionPicked(variant.displayName) },
                                        label = {
                                            Text(
                                                text = variant.displayName,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isCurrent) {
                                                MaterialTheme.colorScheme.secondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainer
                                            },
                                            labelColor = if (isCurrent) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag(
                                            "variant_suggestion_${variant.normalizedKey.replace(Regex("[^a-z0-9]+"), "_")}"
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 7. Submit + Reset Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                focusManager.clearFocus()
                                onResetForm()
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                            modifier = Modifier.testTag("reset_form_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.settings_button_reset),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                onSubmitCard()
                            },
                            enabled = !form.isSubmitting,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.settings_button_add_card),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // --- Vault Overview Summary Card ---
            VaultOverviewCard(
                totalCards = uiState.allCards.size,
                totalVariants = uiState.availableVariants.size,
                customVariants = uiState.customVariantCount
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LiveCardPreview(form: AddCardFormState) {
    val palette = remember(form.network, form.cardType) {
        cardPaletteFor(form.network, form.cardType)
    }
    val shape = RoundedCornerShape(22.dp)

    val displayName = form.cardName.trim().ifEmpty { "Your Card Name" }
    val displayVariant = form.variantInput.trim().ifEmpty { "Variant" }
    val displayNumber = if (form.cardNumberDigits.isEmpty()) {
        if (form.network == PaymentNetwork.AMEX) "••••  ••••••  •••••" else "••••  ••••  ••••  ••••"
    } else {
        CardFormatting.formatCardNumber(form.cardNumberDigits, form.network)
    }
    val displayExpiry = when {
        form.expiryDigits.isEmpty() -> "MM/YYYY"
        form.expiryDigits.length <= 2 -> form.expiryDigits
        else -> "${form.expiryDigits.substring(0, 2)}/${form.expiryDigits.substring(2)}"
    }
    val displayCvv = form.cvvDigits.ifEmpty {
        "•".repeat(form.network.cvvLength)
    }

    Surface(
        shape = shape,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, palette.borderGlow),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .cardMetallicSurface(form.network, form.cardType)
            .testTag("live_card_preview")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = palette.contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.14f))
                                .padding(horizontal = 9.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = displayVariant,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.accentHighlight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = form.cardType.displayLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = palette.mutedContentColor
                        )
                    }
                }
                NetworkBadge(network = form.network)
            }

            MetallicEmvChip(palette = palette)

            Text(
                text = displayNumber,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                letterSpacing = 1.2.sp,
                color = palette.contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.card_label_expiry),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.mutedContentColor.copy(alpha = 0.78f)
                    )
                    Text(
                        text = displayExpiry,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = palette.contentColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.card_label_cvv),
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.mutedContentColor.copy(alpha = 0.78f)
                    )
                    Text(
                        text = displayCvv,
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = palette.contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultOverviewCard(
    totalCards: Int,
    totalVariants: Int,
    customVariants: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_vault_overview_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatPill(
                    value = totalCards.toString(),
                    label = stringResource(R.string.settings_vault_cards_stat),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    value = totalVariants.toString(),
                    label = stringResource(R.string.settings_vault_variants_stat),
                    modifier = Modifier.weight(1f)
                )
                StatPill(
                    value = customVariants.toString(),
                    label = stringResource(R.string.settings_vault_custom_variants_stat),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatPill(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun validationErrorMessage(error: ValidationError): String = when (error) {
    ValidationError.CardNameRequired -> stringResource(R.string.error_card_name_required)
    is ValidationError.CardNumberInvalid -> stringResource(
        R.string.error_card_number_length,
        error.expectedDigits
    )
    ValidationError.ExpiryFormatInvalid -> stringResource(R.string.error_expiry_format)
    ValidationError.ExpiryMonthInvalid -> stringResource(R.string.error_expiry_month)
    ValidationError.ExpiryYearInvalid -> stringResource(R.string.error_expiry_year)
    is ValidationError.CvvInvalid -> stringResource(
        R.string.error_cvv_length,
        error.expectedDigits,
        error.networkName
    )
    ValidationError.VariantRequired -> stringResource(R.string.error_variant_required)
}

private class CardNumberVisualTransformation(
    private val network: PaymentNetwork
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = buildString {
            raw.forEachIndexed { index, ch ->
                append(ch)
                val shouldInsertSpace = if (network == PaymentNetwork.AMEX) {
                    (index == 3 || index == 9) && index != raw.lastIndex
                } else {
                    (index + 1) % 4 == 0 && index != raw.lastIndex
                }
                if (shouldInsertSpace) append(' ')
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                if (clamped == 0) return 0
                val spacesBefore = if (network == PaymentNetwork.AMEX) {
                    var s = 0
                    if (clamped > 4) s++
                    if (clamped > 10) s++
                    s
                } else {
                    (clamped - 1) / 4
                }
                return (clamped + spacesBefore).coerceAtMost(formatted.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                var digits = 0
                for (i in 0 until clamped) {
                    if (formatted[i] != ' ') digits++
                }
                return digits.coerceAtMost(raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

private class ExpiryVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val formatted = if (raw.length <= 2) {
            raw
        } else {
            "${raw.substring(0, 2)}/${raw.substring(2)}"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, raw.length)
                return if (clamped <= 2) clamped else clamped + 1
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                return if (clamped <= 2) clamped else (clamped - 1).coerceAtMost(raw.length)
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}
