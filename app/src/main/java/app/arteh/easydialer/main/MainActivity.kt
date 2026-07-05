package app.arteh.easydialer.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import app.arteh.easydialer.clog.CallLogVM
import app.arteh.easydialer.contacts.list.ContactsVM
import app.arteh.easydialer.db.AppDatabase
import app.arteh.easydialer.dial.DialPadVM
import app.arteh.easydialer.ui.EdgePadding
import app.arteh.easydialer.ui.theme.EasyDialerTheme
import app.arteh.easydialer.utility.Holder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val context = this.applicationContext

        val instance = AppDatabase.getInstance(context)

        lifecycleScope.launch {
            Holder.contactRP.initialize(context, instance)
        }

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