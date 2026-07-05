package app.arteh.easydialer.contacts.show

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import app.arteh.easydialer.contacts.show.ui.ShowScreen
import app.arteh.easydialer.ui.EdgePadding
import app.arteh.easydialer.ui.theme.EasyDialerTheme

class ContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyDialerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShowScreen(padding = EdgePadding(innerPadding))
                }
            }
        }
    }
}