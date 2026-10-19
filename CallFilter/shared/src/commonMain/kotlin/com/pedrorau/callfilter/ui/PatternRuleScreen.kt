package com.pedrorau.callfilter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedrorau.callfilter.engine.RuleEngine
import com.pedrorau.callfilter.resources.*
import com.pedrorau.callfilter.ui.theme.Primary
import com.pedrorau.callfilter.ui.theme.SuccessGreen
import com.pedrorau.callfilter.ui.theme.TextSecondary
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

private data class RegexExample(val pattern: String, val description: StringResource)

private val examples = listOf(
    RegexExample("^\\d{8}$", Res.string.pattern_example_digits),
    RegexExample("^\\+52", Res.string.pattern_example_country),
    RegexExample("^\\+511", Res.string.pattern_example_lima)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternRuleScreen(
    state: PatternRuleState,
    onPatternChanged: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        TopAppBar(
            title = {
                Text(stringResource(Res.string.pattern_title), fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Text("\u2190", fontSize = 24.sp)
                }
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.pattern_label),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = state.pattern,
                onValueChange = onPatternChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = { Text(stringResource(Res.string.pattern_placeholder, examples[1].pattern)) },
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Primary,
                    fontSize = 16.sp
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Primary.copy(alpha = 0.2f),
                    focusedContainerColor = Primary.copy(alpha = 0.05f),
                    unfocusedContainerColor = Primary.copy(alpha = 0.05f)
                ),
                isError = !state.isValid
            )

            Text(
                text = stringResource(Res.string.pattern_help),
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (state.pattern.isNotBlank()) {
                if (state.isValid) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = SuccessGreen.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("\u2713", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(
                                    stringResource(Res.string.pattern_valid_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SuccessGreen
                                )
                                Text(
                                    stringResource(Res.string.pattern_valid_desc),
                                    fontSize = 12.sp,
                                    color = SuccessGreen.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("\u2717", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(
                                    stringResource(Res.string.pattern_invalid_title),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                val errorMessage = when (state.error) {
                                    PatternError.TOO_LONG ->
                                        stringResource(Res.string.pattern_error_too_long, RuleEngine.MAX_PATTERN_LENGTH)
                                    PatternError.INVALID_SYNTAX ->
                                        stringResource(Res.string.pattern_error_syntax, state.errorDetail.orEmpty())
                                    null -> null
                                }
                                errorMessage?.let {
                                    Text(
                                        it,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.saved) {
                Text(
                    text = stringResource(Res.string.pattern_saved),
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Text(
                text = stringResource(Res.string.pattern_examples),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            examples.forEach { example ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPatternChanged(example.pattern) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = example.pattern,
                            fontFamily = FontFamily.Monospace,
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(example.description),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = state.isValid
            ) {
                Text(stringResource(Res.string.pattern_save), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.common_cancel), color = TextSecondary)
            }
        }
    }
}
