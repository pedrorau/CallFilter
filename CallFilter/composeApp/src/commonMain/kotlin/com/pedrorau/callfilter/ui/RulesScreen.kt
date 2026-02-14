package com.pedrorau.callfilter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrorau.callfilter.model.RuleType
import com.pedrorau.callfilter.ui.theme.Primary
import com.pedrorau.callfilter.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    state: RulesState,
    onToggleRule: (String, Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onEditPattern: () -> Unit,
    onEditBlockedList: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    "Call Filtering Rules",
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("\u2190", fontSize = 24.sp)
                }
            }
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Section label
            Text(
                text = "ACTIVE FILTERS",
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
            )

            // Rules list
            state.rules.forEach { rule ->
                val (title, subtitle) = when (rule.type) {
                    RuleType.BLOCK_DIGIT_COUNT -> "Block 8-digit numbers" to "Common spam format"
                    RuleType.BLOCK_FROM_LIST -> "Block local list" to "Numbers you've added manually"
                    RuleType.BLOCK_REGEX -> "Pattern matching" to "Regex filtering"
                    RuleType.BLOCK_ALL -> "Block all calls" to "Reject every incoming call"
                }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = rule.enabled,
                            onCheckedChange = { onToggleRule(rule.id, it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                        )
                    }

                    // Action links for specific rules
                    if (rule.type == RuleType.BLOCK_FROM_LIST) {
                        TextButton(
                            onClick = onEditBlockedList,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        ) {
                            Text(
                                "Edit local list",
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    if (rule.type == RuleType.BLOCK_REGEX) {
                        TextButton(
                            onClick = onEditPattern,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        ) {
                            Text(
                                "Edit pattern",
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notifications toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notify on block",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Receive a notification when filtered",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = state.notificationsEnabled,
                    onCheckedChange = onToggleNotifications,
                    colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                )
            }
        }
    }
}
