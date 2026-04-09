package app.arteh.easydialer.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.arteh.easydialer.clog.CallLogVM
import app.arteh.easydialer.contacts.list.ContactsVM
import app.arteh.easydialer.dial.DialPadVM
import app.arteh.easydialer.ui.EdgePadding
import app.arteh.easydialer.ui.theme.EasyDialerTheme
import app.arteh.easydialer.utility.Holder

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Holder.contactRP.initialize(application)

        val contactsVM: ContactsVM by viewModels()
        val callLogVM: CallLogVM by viewModels()
        val dialPadVM: DialPadVM by viewModels()

        enableEdgeToEdge()

        setContent {
            EasyDialerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        contactsVM = contactsVM,
                        callLogVM = callLogVM,
                        dialPadVM = dialPadVM,
                        padding = EdgePadding(innerPadding, extra = 0.dp)
                    )
                }
            }
        }
    }
}