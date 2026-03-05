package app.arteh.easydialer.contacts.edit

import android.app.Application
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
import app.arteh.easydialer.ui.theme.EasyDialer

class EditContactActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContext = this.applicationContext as Application

        val editContactVM: EditContactVM by viewModels { EditContactVM.Factory(appContext) }

        enableEdgeToEdge()
        setContent {
            EasyDialer {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    EditScreen(editContactVM, padding = EdgePadding(innerPadding, extra = 16.dp))
                }
            }
        }
    }
}