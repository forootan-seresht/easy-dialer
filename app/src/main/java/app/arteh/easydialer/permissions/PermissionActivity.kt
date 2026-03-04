package app.arteh.easydialer.permissions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.arteh.easydialer.ui.EdgePadding
import app.arteh.easydialer.ui.theme.EasyDialer

class PermissionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EasyDialer {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionScreen(padding = EdgePadding(innerPadding, 0.dp))
                }
            }
        }
    }
}