package app.arteh.easydialer.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import app.arteh.easydialer.R
import app.arteh.easydialer.ui.PaddingSides
import app.arteh.easydialer.ui.noRippleClickable
import app.arteh.easydialer.ui.theme.AppColor

@Composable
fun SettingsScreen(padding: PaddingSides, settingsVM: SettingsVM = viewModel()) {

    val uiState by settingsVM.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                start = padding.start,
                top = padding.top,
                end = padding.end,
                bottom = padding.bottom
            )
    ) {
        LanguageSection(uiState.language.displayName, settingsVM.supportedLanguages)
        { settingsVM.onAction(SettingsAction.UpdateLanguage(it, context)) }
    }
}

@Composable
private fun LanguageSection(
    languageName: String, supportedLanguages: List<AppLanguage>, updateLanguage: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp), contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.language_),
                modifier = Modifier.padding(end = 8.dp),
                fontWeight = FontWeight.SemiBold
            )

            Row(
                Modifier
                    .weight(1f)
                    .border(
                        1.dp,
                        AppColor.Gray1.resolve().copy(alpha = 0.5f),
                        RoundedCornerShape(5.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(modifier = Modifier.weight(1f), text = languageName)

                Icon(
                    painterResource(R.drawable.arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
        ) {
            supportedLanguages.forEachIndexed { index, language ->
                DropdownMenuItem(
                    text = {
                        Text(
                            language.displayName,
                        )
                    },
                    onClick = {
                        expanded = false
                        updateLanguage(index)
                    }
                )
            }
        }
    }
}