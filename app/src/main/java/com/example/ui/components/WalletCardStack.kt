package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CardEntity
import com.example.data.CardType
import com.example.ui.theme.EmeraldTeal
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.cardPaletteFor
import com.example.util.ClipboardHelper
import kotlinx.coroutines.delay

private enum class CopiedField {
    NUMBER,
    EXPIRY,
    CVV
}

@Composable
fun WalletCardStack(
    cards: List<CardEntity>,
    expandedCardId: Long?,
    onToggleExpand: (Long) -> Unit,
    onDeleteCard: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDeleteCard by remember { mutableStateOf<CardEntity?>(null) }

    val animatedGaps: List<Dp> = cards.mapIndexed { index, card ->
        key(card.id) {
            val isExpanded = card.id == expandedCardId
            val previousCardExpanded = index > 0 && cards[index - 1].id == expandedCardId
            val gap by animateDpAsState(
                targetValue = when {
                    index == 0 -> 0.dp
                    previousCardExpanded -> 14.dp
                    isExpanded -> 10.dp
                    else -> (-26).dp
                },
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "wallet_card_gap_${card.id}"
            )
            gap
        }
    }

    Layout(
        modifier = modifier.fillMaxWidth(),
        content = {
            cards.forEachIndexed { index, card ->
                key(card.id) {
                    val isExpanded = card.id == expandedCardId
                    val isLast = index == cards.lastIndex

                    val cardElevation by animateDpAsState(
                        targetValue = if (isExpanded) 14.dp else 4.dp,
                        animationSpec = tween(durationMillis = 240),
                        label = "wallet_card_elevation_${card.id}"
                    )

                    WalletCardItem(
                        card = card,
                        isExpanded = isExpanded,
                        isStackedUnderAnother = !isLast && !isExpanded,
                        shadowElevation = cardElevation,
                        onToggleExpand = { onToggleExpand(card.id) },
                        onRequestDelete = { pendingDeleteCard = card }
                    )
                }
            }
        }
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }

        if (placeables.isEmpty()) {
            return@Layout layout(constraints.maxWidth, 0) {}
        }

        val yPositions = IntArray(placeables.size)
        var currentY = 0
        var maxBottom = 0

        placeables.forEachIndexed { index, placeable ->
            val gapPx = animatedGaps.getOrElse(index) { 0.dp }.roundToPx()
            val y = if (index == 0) 0 else (currentY + gapPx).coerceAtLeast(0)
            yPositions[index] = y
            currentY = y + placeable.height
            if (currentY > maxBottom) {
                maxBottom = currentY
            }
        }

        val totalWidth = placeables.maxOfOrNull { it.width } ?: constraints.minWidth
        layout(totalWidth, maxBottom) {
            placeables.forEachIndexed { index, placeable ->
                val isExpanded = cards.getOrNull(index)?.id == expandedCardId
                val z = if (isExpanded) placeables.size + 2f else index.toFloat()
                placeable.placeRelative(x = 0, y = yPositions[index], zIndex = z)
            }
        }
    }

    pendingDeleteCard?.let { cardToDelete ->
        AlertDialog(
            onDismissRequest = { pendingDeleteCard = null },
            title = {
                Text(
                    text = stringResource(R.string.delete_dialog_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_dialog_message, cardToDelete.cardName),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCard(cardToDelete.id)
                        pendingDeleteCard = null
                    },
                    modifier = Modifier.testTag("confirm_delete_card_button")
                ) {
                    Text(
                        text = stringResource(R.string.delete_dialog_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDeleteCard = null },
                    modifier = Modifier.testTag("cancel_delete_card_button")
                ) {
                    Text(text = stringResource(R.string.delete_dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun WalletCardItem(
    card: CardEntity,
    isExpanded: Boolean,
    isStackedUnderAnother: Boolean,
    shadowElevation: androidx.compose.ui.unit.Dp,
    onToggleExpand: () -> Unit,
    onRequestDelete: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val palette = remember(card.network, card.cardType) {
        cardPaletteFor(card.network, card.cardType)
    }

    var copiedField by remember(card.id) { mutableStateOf<CopiedField?>(null) }

    LaunchedEffect(copiedField) {
        if (copiedField != null) {
            delay(1_600L)
            copiedField = null
        }
    }

    // Clear copied indicator cleanly when card collapses
    LaunchedEffect(isExpanded) {
        if (!isExpanded) copiedField = null
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 240),
        label = "chevron_rotation_${card.id}"
    )

    val bottomPadding by animateDpAsState(
        targetValue = when {
            isExpanded -> 18.dp
            isStackedUnderAnother -> 34.dp
            else -> 18.dp
        },
        animationSpec = tween(durationMillis = 240),
        label = "card_bottom_padding_${card.id}"
    )

    val cardShape = RoundedCornerShape(22.dp)

    Surface(
        shape = cardShape,
        shadowElevation = shadowElevation,
        border = BorderStroke(
            width = if (isExpanded) 1.2.dp else 0.9.dp,
            color = if (isExpanded) palette.borderGlow else Color.White.copy(alpha = 0.14f)
        ),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wallet_card_${card.id}")
            .clip(cardShape)
            .cardMetallicSurface(card.network, card.cardType)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.16f)),
                role = Role.Button,
                onClickLabel = stringResource(
                    if (isExpanded) R.string.cd_collapse_card else R.string.cd_expand_card
                ),
                onClick = onToggleExpand
            )
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.84f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 20.dp,
                    end = 18.dp,
                    top = 18.dp,
                    bottom = bottomPadding
                )
        ) {
            // --- Collapsed Header (Always visible: Card Name, Network, Variant ONLY) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = card.cardName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        ),
                        color = palette.contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.13f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = card.variant,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = palette.accentHighlight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = card.network.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.mutedContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NetworkBadge(network = card.network)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnimatedVisibility(
                            visible = !isExpanded,
                            enter = fadeIn(tween(180)),
                            exit = fadeOut(tween(140))
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Fingerprint,
                                    contentDescription = stringResource(R.string.cd_biometric_protected),
                                    tint = palette.accentHighlight,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = stringResource(
                                    if (isExpanded) R.string.cd_collapse_card
                                    else R.string.cd_expand_card
                                ),
                                tint = palette.mutedContentColor,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(chevronRotation)
                            )
                        }
                    }
                }
            }

            // --- Expanded Sensitive Details (Strictly hidden when collapsed) ---
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(220)) +
                    expandVertically(
                        animationSpec = spring(
                            dampingRatio = 0.85f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                exit = fadeOut(animationSpec = tween(160)) +
                    shrinkVertically(animationSpec = tween(200))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                ) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.12f),
                        thickness = 0.8.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Chip + Card Type Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetallicEmvChip(palette = palette)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(palette.accentHighlight.copy(alpha = 0.18f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    if (card.cardType == CardType.CREDIT) {
                                        R.string.card_type_credit
                                    } else {
                                        R.string.card_type_debit
                                    }
                                ),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.2.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = palette.accentHighlight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Card Number + Copy Action
                    CopyableFieldBlock(
                        label = stringResource(R.string.card_label_number),
                        value = card.formattedNumber,
                        isLargeValue = true,
                        isCopied = copiedField == CopiedField.NUMBER,
                        contentDescription = stringResource(R.string.cd_copy_card_number),
                        testTag = "copy_number_button_${card.id}",
                        mutedColor = palette.mutedContentColor,
                        valueColor = palette.contentColor,
                        onCopyClick = {
                            ClipboardHelper.copySensitiveText(
                                context = context,
                                label = "Card Number",
                                value = card.cardNumber
                            )
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            copiedField = CopiedField.NUMBER
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Expiry (MM/YYYY) + CVV + Copy Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CopyableFieldBlock(
                            label = stringResource(R.string.card_label_expiry),
                            value = card.formattedExpiry,
                            isLargeValue = false,
                            isCopied = copiedField == CopiedField.EXPIRY,
                            contentDescription = stringResource(R.string.cd_copy_expiry),
                            testTag = "copy_expiry_button_${card.id}",
                            mutedColor = palette.mutedContentColor,
                            valueColor = palette.contentColor,
                            modifier = Modifier.weight(1.25f),
                            onCopyClick = {
                                ClipboardHelper.copySensitiveText(
                                    context = context,
                                    label = "Card Expiry",
                                    value = card.formattedExpiry
                                )
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                copiedField = CopiedField.EXPIRY
                            }
                        )

                        CopyableFieldBlock(
                            label = stringResource(R.string.card_label_cvv),
                            value = card.cvv,
                            isLargeValue = false,
                            isCopied = copiedField == CopiedField.CVV,
                            contentDescription = stringResource(R.string.cd_copy_cvv),
                            testTag = "copy_cvv_button_${card.id}",
                            mutedColor = palette.mutedContentColor,
                            valueColor = palette.contentColor,
                            modifier = Modifier.weight(1f),
                            onCopyClick = {
                                ClipboardHelper.copySensitiveText(
                                    context = context,
                                    label = "Card CVV",
                                    value = card.cvv
                                )
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                copiedField = CopiedField.CVV
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Footer bar with subtle tap-to-hide hint & delete icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_tap_to_collapse_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.mutedContentColor.copy(alpha = 0.72f)
                        )
                        IconButton(
                            onClick = onRequestDelete,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("delete_card_button_${card.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.cd_delete_card),
                                tint = palette.mutedContentColor.copy(alpha = 0.85f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CopyableFieldBlock(
    label: String,
    value: String,
    isLargeValue: Boolean,
    isCopied: Boolean,
    contentDescription: String,
    testTag: String,
    mutedColor: Color,
    valueColor: Color,
    onCopyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.22f))
            .padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = mutedColor.copy(alpha = 0.85f)
                )
                AnimatedVisibility(
                    visible = isCopied,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(180))
                ) {
                    Text(
                        text = stringResource(R.string.card_copied_badge),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = EmeraldTeal
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (isLargeValue) 16.sp else 15.sp,
                letterSpacing = if (isLargeValue) 1.0.sp else 0.8.sp,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        IconButton(
            onClick = onCopyClick,
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag(testTag)
        ) {
            AnimatedContent(
                targetState = isCopied,
                transitionSpec = {
                    fadeIn(tween(180)) togetherWith fadeOut(tween(150))
                },
                label = "copy_icon_transition_$testTag"
            ) { copied ->
                if (copied) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EmeraldTeal.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = stringResource(R.string.card_copied_badge),
                            tint = EmeraldTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = contentDescription,
                            tint = valueColor.copy(alpha = 0.90f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
