package com.example.expense_tracker.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.R

/**
 * M3 Expressive Bottom Navigation Bar.
 *
 * Design Spec:
 * - Height reduced to 64.dp (down from baseline 80.dp) for a sleeker profile.
 * - Morphing between Outlined (unselected) and Filled (selected) icons like Google apps.
 * - Dynamic pill indicator animation (expands/fades smoothly upon selection).
 * - Icon scale bounce and Center Add button elastic press feedback.
 * - Handles system navigation bar insets cleanly at the bottom.
 */
@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                .height(64.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            CompactNavItem(
                selected = currentRoute == NavRoutes.HOME,
                onClick = {
                    onNavigate(NavRoutes.HOME)
                },
                filledIcon = Icons.Filled.Home,
                outlinedIcon = Icons.Outlined.Home,
                label = stringResource(R.string.nav_home)
            )

            // Wallet
            CompactNavItem(
                selected = currentRoute == NavRoutes.WALLET,
                onClick = {
                    onNavigate(NavRoutes.WALLET)
                },
                filledIcon = Icons.Filled.AccountBalanceWallet,
                outlinedIcon = Icons.Outlined.AccountBalanceWallet,
                label = stringResource(R.string.nav_wallet)
            )

            // Add (Center Action Button)
            CompactNavAddButton(
                selected = NavMotion.isInputRoute(currentRoute),
                onClick = {
                    onNavigate(NavRoutes.inputRoute(null))
                }
            )

            // Summary
            CompactNavItem(
                selected = currentRoute == NavRoutes.SUMMARY,
                onClick = {
                    onNavigate(NavRoutes.SUMMARY)
                },
                filledIcon = Icons.Filled.PieChart,
                outlinedIcon = Icons.Outlined.PieChart,
                label = stringResource(R.string.nav_summary)
            )

            // Profile
            CompactNavItem(
                selected = currentRoute == NavRoutes.PROFILE,
                onClick = {
                    onNavigate(NavRoutes.PROFILE)
                },
                filledIcon = Icons.Filled.Person,
                outlinedIcon = Icons.Outlined.Person,
                label = stringResource(R.string.nav_profile)
            )
        }
    }
}

@Composable
private fun RowScope.CompactNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    filledIcon: ImageVector,
    outlinedIcon: ImageVector,
    label: String,
    contentDescription: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Indicator pill scale animation (deferred read inside graphicsLayer)
    val indicatorScale = animateFloatAsState(
        targetValue = if (selected) 1.0f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "nav_indicator_scale"
    )

    // Micro scale bounce when selected (deferred read inside graphicsLayer)
    val iconScale = animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "nav_icon_scale"
    )

    val animatedIconColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "nav_icon_color"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "nav_text_color"
    )
    val indicatorBgColor = MaterialTheme.colorScheme.secondaryContainer

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Indicator pill: fixed size, scaleX/alpha deferred to graphicsLayer (zero composition overhead)
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 30.dp)
                    .graphicsLayer {
                        val s = indicatorScale.value
                        scaleX = s
                        alpha = if (s > 0.05f) 1f else 0f
                    }
                    .background(color = indicatorBgColor, shape = CircleShape)
            )

            // Crossfade morph between filled (selected) and outlined (unselected) icons
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.85f, animationSpec = tween(200)))
                        .togetherWith(fadeOut(animationSpec = tween(150)))
                },
                label = "nav_icon_morph"
            ) { isSelected ->
                Icon(
                    imageVector = if (isSelected) filledIcon else outlinedIcon,
                    contentDescription = contentDescription,
                    tint = animatedIconColor,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            val s = iconScale.value
                            scaleX = s
                            scaleY = s
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = animatedTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RowScope.CompactNavAddButton(
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Button scale animation (deferred read inside graphicsLayer)
    val scaleState = animateFloatAsState(
        targetValue = if (isPressed) 0.88f else if (selected) 1.05f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "add_button_scale"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer {
                    val s = scaleState.value
                    scaleX = s
                    scaleY = s
                }
                .clip(RoundedCornerShape(14.dp))
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    role = Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.nav_add),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

