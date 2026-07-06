package app.arteh.easydialer.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.arteh.easydialer.ui.EdgePadding
import app.arteh.easydialer.ui.theme.EasyDialerTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsVM: SettingsVM by viewModels()

        setContent {
            EasyDialerTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingsScreen(padding = EdgePadding(innerPadding, extra = 0.dp), settingsVM)
                }
            }
        }
    }
}